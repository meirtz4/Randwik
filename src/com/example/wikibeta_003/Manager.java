package com.example.wikibeta_003;

import java.util.Stack;

import com.example.wikibeta_003.R;
import com.example.wikibeta_003.Interfaces.IURLProvider;

import android.os.Bundle;
import android.app.Activity;
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

	IURLProvider provider = LocalURLProvider.getURLProvider();
	
	Button getRand;
	Button back;
	WebView myWebView;
	PageLoader loader = new PageLoader();
	boolean pageIsLoading;
	boolean loadPrePage;
	String lastPageForStack = "";
	ECategories[] currentCatagories = {ECategories.Example};


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_manager);
		pageIsLoading = false;
		loadPrePage = false;

		myWebView = (WebView) findViewById(R.id.wvBrowser);
		myWebView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return false;
			}
		});

		getRand = (Button) findViewById(R.id.randWiki);
		getRand.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				loadPrePage = false;
				loader.run();
			}
		});

		back = (Button) findViewById(R.id.back);
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				loadPrePage = true;
				loader.run();
			}
		});

		loader.start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.pref_menu, menu);
		return true;
	}


	private class PageLoader extends Thread {

		private Stack<String> previousPages = new Stack<String>();

		@Override
		public void run() {
			if (pageIsLoading)
				return;
			pageIsLoading = true;
			loadPage();
			pageIsLoading = false;
		}


		private void loadPage(){
			try {
				String pageLink;
				if (loadPrePage && (!previousPages.isEmpty())){
					pageLink = previousPages.pop();
				}
				else if (loadPrePage && (previousPages.isEmpty())){
					return;
				}
				else{ 
					if (!lastPageForStack.equals(""))
						previousPages.push(lastPageForStack);
					pageLink = provider.getRandomPage(currentCatagories, previousPages);
					lastPageForStack = pageLink;
				}
				myWebView.loadUrl(pageLink);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}	
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
	
}
