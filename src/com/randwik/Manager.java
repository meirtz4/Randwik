/***
 * 	Manager class is in charge of invoking the URL from the requested provider.
 * 	Also to manage the communication between the user and the App.
 * 	@author Meir Levy
 *	@version 1.2
 */

package com.randwik;

import java.util.ArrayList;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.randwik.Interfaces.IURLProvider;
import com.randwik.LocalDB.CategoriesMap;
import com.randwik_pack.R;
import Utils.Utils;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log; 
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ShareActionProvider;

public class Manager extends Activity {

	/* Update here for switch to another provider */
	//IURLProvider provider = SimpleURLProvider.getURLProvider(); 
	IURLProvider provider = LocalURLProvider.getURLProvider();

	/* For the chosen categories from the user */
	protected static ArrayList<String> currentCategoriesList = new ArrayList<String>();
	protected static String[] currentCategories;

	/* Local thread that load the page */
	PageLoader loader = new PageLoader();

	/* Publisher id for google AdMob */
	protected static String PUBLISHER_ID = "ca-app-pub-xxxxxxxxxxxxxxxxxxx";

	/* Hold the current page the user is at */
	protected String currentPage;
	protected String previousPage;

	/* Variable for indication about the loading window */
	boolean doneLoadingPage = false;

	/* Both boolean variables are used to solve the double URL load issue (detailed below) */
	boolean loadingFinished = true;
	boolean redirect = false;

	ProgressDialog loadingWindow = null;

	/* Objects used for thread to wait */
	Object waitForFinishLoad = new Object();
	Object waitForNextRun = new Object();

	/* For share button */
	protected ShareActionProvider shareActionProvider; 
	protected Intent sendIntent;

	/* View Elements */
	Button buttonGetRandomWikiPage;
	ImageButton buttonPref;
	WebView webViewMain;
	LinearLayout layout;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTopBar();
		showLoadingWindow();
		setViewElements();
		loadAdView();
		loader.start();
	}

	private void loadAdView() {
		layout = (LinearLayout) findViewById(R.id.loMain);
		AdView ad = new AdView(this, AdSize.BANNER, PUBLISHER_ID);
		layout.addView(ad);
		ad.loadAd(new AdRequest()); 
	}

	private void setTopBar() {
		/* Set view by API */
		if (Utils.getUtils().isAPIAboveAnd11()){
			setContentView(R.layout.activity_manager);
			LinearLayout buttonsLayout = (LinearLayout) findViewById(R.id.buttonsLayout);
			buttonsLayout.setVisibility(LinearLayout.GONE);
		}
		else {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			setContentView(R.layout.activity_manager);
		}
	}


	protected void showLoadingWindow(){
		doneLoadingPage = false;
		loadingWindow = new ProgressDialog(Manager.this);
		loadingWindow.setTitle("Loading");
		loadingWindow.setMessage("");
		loadingWindow.show();
	}

	@SuppressLint("NewApi")
	private void updateIntent() {
		runOnUiThread(new Runnable() {  
			@Override
			public void run() {
				String textToSend = Utils.getUtils().createShareString(currentPage);
				sendIntent = new Intent();
				sendIntent.setAction(Intent.ACTION_SEND);
				sendIntent.putExtra(Intent.EXTRA_TEXT, textToSend);
				sendIntent.setType("text/plain");
				if(Utils.getUtils().isAPIAboveAnd14())
					if (shareActionProvider!=null)
						shareActionProvider.setShareIntent(sendIntent);
			}
		});
	}

	/* Create the option menu */
	@SuppressLint("NewApi")
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.pref_menu, menu);
		if(Utils.getUtils().isAPIAboveAnd14()){
			MenuItem item = menu.findItem(R.id.ACIshareArticle);
			shareActionProvider = (ShareActionProvider) item.getActionProvider();
		}
		updateIntent();
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.topics:
			Intent p = new Intent("android.intent.action.PREFS");
			startActivity(p);
			break;
		case R.id.about:
			AlertDialog.Builder dlgAbout = new AlertDialog.Builder(this);
			dlgAbout.setMessage(Utils.getUtils().getAboutContent());
			dlgAbout.setTitle(Utils.getUtils().getAboutTitle());
			dlgAbout.setIcon(R.drawable.ic_launcher_mdpi);
			dlgAbout.create().show();
			break;
		case R.id.ACIgetArticle:
			getButtonClicked();
			break;
		case R.id.ACIshareArticle:	
			startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.bShare)));
			break;
		case R.id.exit:
			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			    @Override
			    public void onClick(DialogInterface dialog, int which) {
			        switch (which){
			        case DialogInterface.BUTTON_POSITIVE:
						finish();
			            break;
			        case DialogInterface.BUTTON_NEGATIVE:
			            break;
			        }
			    }
			};
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", dialogClickListener).show();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	protected void backButtonClicked(){
		if (webViewMain.canGoBack()){
			webViewMain.goBack();
			if (previousPage!=null){
				Log.e("WebView - Back to: ", previousPage);
				currentPage = previousPage;
			}
			updateIntent();
		}
		else finish();
	}

	protected void getButtonClicked(){
		showLoadingWindow();
		synchronized (waitForNextRun) {
			waitForNextRun.notifyAll();
		}
	}

	private void setViewElements() {
		webViewMain = (WebView) findViewById(R.id.wvBrowser);
		buttonGetRandomWikiPage = (Button) findViewById(R.id.bRandWiki);
		buttonPref = (ImageButton) findViewById(R.id.bPref);

		/***
		 * The WebView has a know issue that the function onPageFinished is invoked
		 * more then once when loading the URL, those 3 functions below create a work-around
		 * that enables us to identify the real invoke of onPageFinished 
		 */
		webViewMain.setWebViewClient(new WebViewClient() {

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String urlNewString) {
				if (!loadingFinished) {
					redirect = true;
				}
				loadingFinished = false;
				webViewMain.loadUrl(urlNewString);
				return true;
			}

			@Override
			public void onPageStarted(WebView view, String url,  Bitmap favicon) {
				loadingFinished = false;
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				if(!redirect){
					loadingFinished = true;
				}
				if(loadingFinished && !redirect){
					/* Now the page REALLY finished loading */
					doneLoadingPage = true;
					synchronized (waitForFinishLoad) {
						waitForFinishLoad.notifyAll();
						Log.e("webViewMain THREAD INFO", "Page finished loading");
					}
				} else{
					redirect = false; 
				}
			}
		});

		buttonGetRandomWikiPage.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				getButtonClicked();
			}
		});

		buttonPref.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				openOptionsMenu(); 
			}
		});
	}

	/* Get categories from the topics list */
	private boolean fillCurrentCategories() {
		SharedPreferences topicData = PreferenceManager.getDefaultSharedPreferences(this);
		CategoriesMap catMap = CategoriesMap.getCategoriesMap();
		currentCategoriesList.clear();
		for (String cat : catMap.getAllCategoriesStrings()){
			boolean value = topicData.getBoolean(cat, false);
			if (value){
				currentCategoriesList.add(cat);
			}
		}

		if (currentCategoriesList.size() == 0){
			Log.e("fillCurrentCategories","currentCategories is EMPTY!");
			return false;
		}

		currentCategories = currentCategoriesList.toArray(new String[] {}); 

		return true;
	}

	@Override
	public void onBackPressed() {
		backButtonClicked();
	}



	/*********************
	 * Thread PageLoader
	 *********************/
	/* Local thread that load the page */
	private class PageLoader extends Thread {

		/* Invoked on startup, when buttons get and back are clicked */
		@Override
		public void run() {
			while (true) {
				loadPage();
				doneLoading();
				synchronized (waitForNextRun) {
					try {
						waitForNextRun.wait();
					} catch (InterruptedException e) {}
				}
			}
		}

		public void doneLoading() {
			while (!doneLoadingPage){
				try {
					synchronized (waitForFinishLoad) {
						waitForFinishLoad.wait();
						Log.e("PageLoader THREAD INFO", "Done loading finished");
					}
				} catch (InterruptedException e) {
					Log.e("Manager", e.getMessage());
				}
			}
			loadingWindow.dismiss(); /* Hide loading window */
		}

		private void loadPage(){
			try {
				String pageLink;
				if (!fillCurrentCategories()) {
					pageLink = (new SimpleURLProvider().getRandomPage(null, null));
					Log.e("Getting random page",pageLink);
				} else {/* Here we get the page link from the provider */
					pageLink = provider.getRandomPage(currentCategories, null);
					Log.e("Getting page from categories",pageLink);
				}
				previousPage = currentPage;
				Log.e("Got page link", pageLink);
				currentPage = pageLink;
				updateIntent();

				/* Loading the page */
				webViewMain.loadUrl(pageLink);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}  
}
