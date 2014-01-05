/***
 * 	Manager class is in charge of invoking the URL from the requested provider.
 * 	Also to manage the communication between the user and the App.
 * 	@author Meir Levy
 *	@version 1.1
 */

package com.example.wikibeta_003;

import java.util.Stack;
import com.example.wikibeta_003.R;
import com.example.wikibeta_003.Interfaces.IURLProvider;

import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
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

public class Manager extends Activity {

	/* Update here for switch to another provider */
	//IURLProvider provider = SimpleURLProvider.getURLProvider(); 
	IURLProvider provider = LocalURLProvider.getURLProvider();
	
	/* TODO! Remove this when we can get the chosen categories from the user */
	private static String[] currentCategories = {"Nature"};
	
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
	WebView webViewMain;
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_manager);
		showLoadingWindow();
		setViewElements();
		loader.start();

	}

	protected void showLoadingWindow(){
		doneLoadingPage = false;
		loadingWindow = new ProgressDialog(Manager.this);
		loadingWindow.setTitle("Loading");
		loadingWindow.setMessage("");
		loadingWindow.show();
	}

	/* TODO! Create the option menu */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.pref_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.preferences:
			Intent p = new Intent("android.intent.action.PREFS");
			startActivity(p);			
			break;
		case R.id.exit:
			finish();
			break;
		}
		return false;
	}

	private void setViewElements() {
		webViewMain = (WebView) findViewById(R.id.wvBrowser);
		buttonGetRandomWikiPage = (Button) findViewById(R.id.randWiki);
		buttonGoBack = (Button) findViewById(R.id.back);

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
				//SHOW LOADING IF IT ISNT ALREADY VISIBLE  
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				if(!redirect){
					loadingFinished = true;
				}
				if(loadingFinished && !redirect){
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
				showLoadingWindow();
				synchronized (waitForNextRun) {
					waitForNextRun.notifyAll();
				}
			}
		});

		buttonGoBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (previousPages.isEmpty())
					return;
				backButtonClicked = true;
				showLoadingWindow();
				synchronized (waitForNextRun) {
						waitForNextRun.notifyAll();
				}
			}
		});
	}

	
	private class PageLoader extends Thread {

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
					pageLink = provider.getRandomPage(currentCategories, previousPages);
					lastPageForStack = pageLink;
				}
				Log.e("Got page link", pageLink);
				webViewMain.loadUrl(pageLink);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}	
	}  
}
