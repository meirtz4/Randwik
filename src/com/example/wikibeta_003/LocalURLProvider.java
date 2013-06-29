package com.example.wikibeta_003;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Stack;

import LocalExceptions.UnimplementedException;

import com.example.wikibeta_003.Interfaces.ICatagoryListOfArticles;
import com.example.wikibeta_003.Interfaces.IURLProvider;
import com.example.wikibeta_003.LocalDB.ExampleCatagory;



public class LocalURLProvider implements IURLProvider{

	private static String pageLinkPrefix = "http://en.wikipedia.org/wiki/";
	private static IURLProvider singleProvider = null;
	private Map<ECategories, ICatagoryListOfArticles> catagoriesDictionary = new HashMap<ECategories, ICatagoryListOfArticles>();
	Random rand = new Random(System.currentTimeMillis());

	protected LocalURLProvider(){
		catagoriesDictionary.put(ECategories.Example, ExampleCatagory.getCatagory());
	}
	
	public static IURLProvider getURLProvider() {
		if (singleProvider == null)
			singleProvider = new LocalURLProvider();
		return singleProvider;
	}
	
	public String getRandomPage(ECategories[] catagories, Stack<String> previousPages) throws InterruptedException {
		ECategories choosenCatagoryEnum = chooseFromCatagorysList(catagories);
		ICatagoryListOfArticles choosenCatagory = catagoriesDictionary.get(choosenCatagoryEnum);
		String URLToReturn, choosenPage;
		Boolean pageAlreadyVisited = true;
		
		try {
			do {
				choosenPage = choosenCatagory.getRandomArticle(false);
				if (previousPages.search(choosenPage) < 0) 
				{
					pageAlreadyVisited = false;
				}
			} while (pageAlreadyVisited);
			
			URLToReturn = pageLinkPrefix + choosenPage;
		} catch (UnimplementedException e) {
			return "wikipedia";
		}
		
		return URLToReturn;
	}

	protected ECategories chooseFromCatagorysList(ECategories[] catagorys) {
		return catagorys[rand.nextInt(catagorys.length)];
	}
}

