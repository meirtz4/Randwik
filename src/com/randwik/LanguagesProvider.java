/***
 * Language provider responsible of creating list of languages available for an Article.
 * This class is a singleton
 * @author Meir Levy
 * @version 1.3
 */

package com.randwik;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import Utils.Utils;
import android.util.Log;

public class LanguagesProvider extends Thread{

	/* Singleton language provider */
	protected static LanguagesProvider langProvider;
	/* Is main thread alive */
	protected boolean alive;
	/* Is work (get languages) done */
	protected boolean workDone;
	/* Current page */
	protected String page;

	protected String languagesXML;
	protected Object waitForWork = new Object();

	/* Parsing strings */
	protected static String LANG_STRING = "[lang] =>";
	protected static String LANG_CONT_STRING = "[*] =>";

	/* Structure to save languages data */
	protected String[][] prefixPrintEnglish;
	/* Measurement for work time */
	protected long timeInMS;

	protected LanguagesProvider(){
		alive = true;
		this.start();
	}

	public static LanguagesProvider getLanguagesProvider(){
		if (langProvider==null)
			langProvider = new LanguagesProvider();
		return langProvider;
	}

	public void kill(){
		alive = false;
	}

	@Override
	public void run() {
		super.run();
		alive = true;

		while (alive){
			try {
				/* Wait for work (new page loaded) */
				synchronized (waitForWork) {
					waitForWork.wait();
					Log.e("LanguagesProvider", "Woke for activity");
				}
			} catch (InterruptedException e) {
				Log.e("LanguagesProvider", e.getMessage());
			}

			if (page==null || page=="")
				continue;

			HttpClient client = new DefaultHttpClient(new BasicHttpParams());
			String json = "";
			page = page.replace(" ", "%20");
			try {
				String line = "";
				HttpGet request = new HttpGet("http://en.wikipedia.org/w/api.php?action=query&lllimit=500&titles=" + page + "&prop=langlinks&format=xml");
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

			client = null;
			languagesXML = json;
			convertXMLtoDictionary();
			/* Report that the work is done */
			workDone = true;
			Log.e("Language Provider - Done", "Time for work = " + (System.currentTimeMillis() - timeInMS) + "ms");
		}

	}

	public static Document loadXMLFromString(String xml) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource is = new InputSource(new StringReader(xml));
		return builder.parse(is);
	}

	private void convertXMLtoDictionary() {

		Document doc;
		Node languages;
		try {
			doc = loadXMLFromString(languagesXML);
			languages = doc.getFirstChild();
			while (languages.getChildNodes().getLength() == 1)
				languages = languages.getFirstChild();
		}catch(Exception e) {
			failParse();
			return;
		}
		
		NodeList languagesList = languages.getChildNodes();
		int numberOfLanguages = languagesList.getLength();
		String[][] tempPrefixPrintEnglish = new String[3][numberOfLanguages];
		List<String> sorted = new ArrayList<String>();
		int count = 0;
	
		for (int i = 0; i < numberOfLanguages ; i++) {
	        Node language = languagesList.item(i);
			/* Get the language key (e.g. "he") */
			String key = language.getAttributes().item(0).getTextContent();
			/* Get the article value (e.g. "Albertus Einstein") */
			String value = language.getTextContent();
			/* Get the language value in English (e.g. "Hebrew") */
			String inEnglish = new Locale(key).getDisplayName();
			if (inEnglish.charAt(0)<='A' || inEnglish.charAt(0)>='Z')
				continue;

			/* Save the data in a temporary unsorted structure */
			tempPrefixPrintEnglish[0][count] = key;
			tempPrefixPrintEnglish[1][count] = value;
			tempPrefixPrintEnglish[2][count++] = inEnglish;
			sorted.add(inEnglish);
		}
		
		
		/* Sort the languages by their English names */
		Collections.sort(sorted);
		prefixPrintEnglish = new String[3][count + 1];
		/* Insert first language (English) */
		prefixPrintEnglish[0][0] = "en";
		prefixPrintEnglish[1][0] = page;
		prefixPrintEnglish[2][0] = "English";
		int PPEIndex = 1;
		/* Fill the data base in the sorted data (find and fill) */
		for (String str: sorted){
			int tempIndex = 0;
			while(!str.equals(tempPrefixPrintEnglish[2][tempIndex++]));
			prefixPrintEnglish[0][PPEIndex] = tempPrefixPrintEnglish[0][--tempIndex];
			prefixPrintEnglish[1][PPEIndex] = tempPrefixPrintEnglish[1][tempIndex];
			prefixPrintEnglish[2][PPEIndex++] = tempPrefixPrintEnglish[2][tempIndex];
		}

		Log.e("LanguagesProvider","Number of langs: " + count);
	}

	private void failParse() {
		prefixPrintEnglish = new String[3][1];
		/* Insert first language (English) */
		prefixPrintEnglish[0][0] = "en";
		prefixPrintEnglish[1][0] = page;
		prefixPrintEnglish[2][0] = "English";
	}

	public void createLanguagesItems(String _page) {
		page = Utils.getUtils().getArticleNameFromLink(_page);
		synchronized (waitForWork) {
			workDone = false;
			timeInMS = System.currentTimeMillis();
			waitForWork.notifyAll();
		}
	}

	protected void waitForWorkDone(){
		while(!workDone)
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	}

	public String[][] getLangsStruct(){
		waitForWorkDone();
		return prefixPrintEnglish;
	}
}

