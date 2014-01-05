/***
 * 	Not in use at the moment
 * 	@author Meir Levy
 *	@version 1.1
 */


package com.example.wikibeta_003;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;
import java.util.Stack;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;

import com.example.wikibeta_003.Interfaces.IURLProvider;

public class HTTPURLProvider implements IURLProvider{

	private String category = "computer science";
	private String categoryString = "Category:";
	private int categoryStringLength = categoryString.length();
	private privateArticleNameGetter getter;
	private String answer;
	private static String pageLinkPrefix = "http://en.wikipedia.org/wiki/";
	private static String titleString = "title] => ";
	private static int titleStringLength = titleString.length();
	private Stack<String> pagesVisited = new Stack<String>();

	private static IURLProvider singleProvider = null;

	protected HTTPURLProvider(){
		
	}
	
	public static IURLProvider getURLProvider() {
		if (singleProvider == null)
			singleProvider = new LocalURLProvider();
		return singleProvider;
	}

	public String getRandomPage(String[] catagories, Stack<String> previousPages) throws InterruptedException {
		pagesVisited = previousPages;
		getter = new privateArticleNameGetter();
		getter.run();
		getter.join();
		String URLToReturn = pageLinkPrefix + ProviderParseRandomAnswerToArticleName();
		return URLToReturn;
	}

	private String ProviderParseRandomAnswerToArticleName() throws InterruptedException {

		privateArticleNameGetter categoryGetter = new privateArticleNameGetter();
		while(answer.length()>0){
			String firstChar = answer.substring(0, 1);
			answer = answer.substring(1);
			if(!firstChar.equals("[")){
				continue;
			}
			if(answer.substring(0, titleStringLength).equals(titleString)){
				if(answer.substring(titleStringLength,titleStringLength + categoryStringLength).equals(categoryString)){
					Random goDeeper = new Random();
					if (goDeeper.nextBoolean()){
						category = answer.substring(titleStringLength + categoryStringLength,answer.indexOf('\n'));
						categoryGetter.run();
						categoryGetter.join();
					}else{
						continue;
					}
				}else{
					Random keepGoing = new Random();
					if (keepGoing.nextBoolean()){		
						continue;
					}else{
						if (wasPageVisited(answer.substring(titleStringLength, answer.indexOf('\n')),pagesVisited)) {
							continue;
						} else {
							answer.substring(titleStringLength);
							return answer.substring(titleStringLength, answer.indexOf('\n'));
						}
					}
				}
			}
		}
		return "Wikipedia";
	}


	private boolean wasPageVisited(String substring, Stack<String> pagesVisited) {
		int j;
		for (j=0;j<pagesVisited.size();j++) {
			if (pagesVisited.get(j).contains(substring)) {
				return true;
			}
		}
		return false;
	}


	private class privateArticleNameGetter extends Thread{

		@Override
		public void run() {
			super.run();
			HttpClient client = new DefaultHttpClient(new BasicHttpParams());
			String json = "";
			category = category.replace(" ", "%20");
			try {
				String line = "";
				HttpGet request = new HttpGet("http://en.wikipedia.org/w/api.php?action=query&list=categorymembers&cmtitle=Category:" + category + "&format=txt&cmlimit=max");
				HttpResponse response = client.execute(request);
				BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				while ((line = rd.readLine()) != null) {
					json += line + System.getProperty("line.separator");
				}
			} catch (IllegalArgumentException e1) {
				e1.printStackTrace();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			answer = json;
		}
	}

}


