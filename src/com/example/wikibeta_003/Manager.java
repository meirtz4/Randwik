package com.example.wikibeta_003;

import java.util.Stack;

import com.example.wikibeta_003.R;
import com.example.wikibeta_003.Interfaces.IURLProvider;

import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
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

	private static ECategories[] currentCategories = {ECategories.Example};
	IURLProvider provider = LocalURLProvider.getURLProvider();
	PageLoader loader = new PageLoader();
	private Stack<String> previousPages = new Stack<String>();

	boolean backButtonClicked = false;
	String lastPageForStack = "";
	ProgressDialog loadingWindow = null;

	// View Elements
	Button buttonGetRandomWikiPage;
	Button buttonGoBack;
	WebView webViewMain;

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_manager);
		setViewElements();
		showLoadingWindow();
		loader.start();
	}

	
	protected void showLoadingWindow(){
		loadingWindow = new ProgressDialog(Manager.this);
		loadingWindow.setTitle("Loading");
		loadingWindow.setMessage("Wait while loading...");
		loadingWindow.show();
	}


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
		webViewMain.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return false;
			}
		});

		buttonGetRandomWikiPage = (Button) findViewById(R.id.randWiki);
		buttonGetRandomWikiPage.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showLoadingWindow();
				backButtonClicked = false;
				loader.run();
			}
		});

		buttonGoBack = (Button) findViewById(R.id.back);
		buttonGoBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showLoadingWindow();
				backButtonClicked = true;
				loader.run();
			}
		});
	}


	
	
	private class PageLoader extends Thread {

		@Override
		public void run() {
			loadPage();
			doneLoading();
		}

		public void doneLoading() {
			backButtonClicked = false;
			loadingWindow.dismiss(); // Hide loading window
		}

		private void loadPage(){
			try {
				String pageLink;
				if (backButtonClicked && (!previousPages.isEmpty())){
					pageLink = previousPages.pop();
				}
				else if (backButtonClicked && (previousPages.isEmpty())){
					return;
				}
				else{ 
					if (!lastPageForStack.equals(""))
						previousPages.push(lastPageForStack);
					pageLink = provider.getRandomPage(currentCategories, previousPages);
					lastPageForStack = pageLink;
				}
				webViewMain.loadUrl(pageLink);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}	
	}  



}
