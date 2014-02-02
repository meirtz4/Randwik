/***
 * 	Manager class is in charge of invoking the URL from the requested provider.
 * 	Also to manage the communication between the user and the App.
 * 	@author Meir Levy
 *	@version 1.1
 */

package com.wikibeta_003;

import java.util.ArrayList;
import java.util.Stack;

import com.wikibeta_003.R;
import com.wikibeta_003.Interfaces.IURLProvider;
import com.wikibeta_003.LocalDB.CategoriesMap;

import Utils.Utils;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class Manager extends Activity {

	/* Update here for switch to another provider */
	//IURLProvider provider = SimpleURLProvider.getURLProvider(); 
	IURLProvider provider = LocalURLProvider.getURLProvider();

	/* For the chosen categories from the user */
	private static ArrayList<String> currentCategoriesList = new ArrayList<String>();
	private static String[] currentCategories;

	/* Local thread that load the page */
	PageLoader loader = new PageLoader();

	/* Stack for saving previous pages,
	 * lastPageForStack is to save the current page when getting a new one */
	private Stack<String> previousPages = new Stack<String>();
	String lastPageForStack = "";

	boolean backButtonClicked = false;
	boolean doneLoadingPage = false;

	/* Both boolean variables are used to solve the double URL load issue (detailed below) */
	boolean loadingFinished = true;
	boolean redirect = false;

	ProgressDialog loadingWindow = null;

	/* Objects used for thread to wait */
	Object waitForFinishLoad = new Object();
	Object waitForNextRun = new Object();

	/* View Elements */
	Button buttonGetRandomWikiPage;
	Button buttonGoBack;
	ImageButton buttonPref;
	WebView webViewMain;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTopBar();
		showLoadingWindow();
		setViewElements();
		loader.start();

	}

	private void setTopBar() {
		/* Set view by API */
		if (Utils.getUtils().isAPIAbove11()){
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

	/* Create the option menu */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.pref_menu, menu);
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
			break;
		case R.id.ACIgetArticle:
			getButtonClicked();
			break;
		case R.id.ACIback:
			backButtonClicked();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	protected void backButtonClicked(){
		if (previousPages.isEmpty())
			return;
		backButtonClicked = true;
		showLoadingWindow();
		synchronized (waitForNextRun) {
			waitForNextRun.notifyAll();
		}	
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
		buttonGoBack = (Button) findViewById(R.id.bBack);
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

		buttonGoBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				backButtonClicked();
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
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Exit Wikrand");
		builder.setMessage("Are You Sure?");

		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				finish();
			}
		});

		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
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
				backButtonClicked = false;
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
				if (backButtonClicked){
					pageLink = previousPages.pop();
				}
				else{ 
					if (!lastPageForStack.equals(""))
						previousPages.push(lastPageForStack);

					if (!fillCurrentCategories()) {
						pageLink = (new SimpleURLProvider().getRandomPage(null, previousPages));
						Log.e("Getting random page",pageLink);
					} else {/* Here we get the page link from the provider */
						pageLink = provider.getRandomPage(currentCategories, previousPages);
						Log.e("Getting page from categories",pageLink);
					}

					lastPageForStack = pageLink;
				}
				Log.e("Got page link", pageLink);

				/* Loading the page */
				webViewMain.loadUrl(pageLink);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}  
}
