/***
 * The simple provider is returning totally random articles from wikipedia,
 * it has no used but to make testing over the application with simpler provider
 * SimpleURLProvider is a Singleton (like the others providers)
 * @author Meir Levy
 * @version 1.1
 */

package com.wikibeta_003;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Stack;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;

import com.wikibeta_003.Interfaces.IURLProvider;

public class SimpleURLProvider implements IURLProvider {

	private privateArticleNameGetter getter;
	private String answer;
	private static String pageLinkPrefix = "http://en.wikipedia.org/wiki/";
	private static String titleString = "title] => ";
	private static int titleStringLength = titleString.length();

	private static SimpleURLProvider simpleProvider = null;

	protected SimpleURLProvider() {

	}

	public static SimpleURLProvider getURLProvider() {
		if (simpleProvider == null)
			simpleProvider = new SimpleURLProvider();
		return simpleProvider;
	}

	private String ProviderParseRandomAnswerToArticleName(String answer) {
		while(answer.length()>0){
			String firstChar = answer.substring(0, 1);
			answer = answer.substring(1);
			if(!firstChar.equals("[")){
				continue;
			}
			if(answer.substring(0, titleStringLength).equals(titleString)){
				answer.substring(titleStringLength);
				return answer.substring(titleStringLength, answer.indexOf('\n'));
			}
		}
		return "Wikipedia";
	}

	private class privateArticleNameGetter extends Thread{

		@Override
		public void run() {
			super.run();

			HttpClient client = new DefaultHttpClient(new BasicHttpParams());
			String json = "";
			try {
				String line = "";
				HttpGet request = new HttpGet("http://en.wikipedia.org/w/api.php?action=query&list=random&format=txt&rnnamespace=0&rnlimit=1");
				HttpResponse response = client.execute(request);
				BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				while ((line = rd.readLine()) != null) {
					json += line + System.getProperty("line.separator");
				}
			} catch (IllegalArgumentException e1) {
				e1.printStackTrace();
			} catch (IOException e2) {
				e2.printStackTrace();
			} finally {

			}
			answer = json;
			client = null;
		}
	}

	@Override
	public String getRandomPage(String[] catagories,
			Stack<String> previousPages) throws InterruptedException {
		getter = new privateArticleNameGetter();
		getter.run();
		getter.join();
		return pageLinkPrefix + ProviderParseRandomAnswerToArticleName(answer);
	}
}

