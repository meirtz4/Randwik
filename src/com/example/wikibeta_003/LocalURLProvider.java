package com.example.wikibeta_003;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Stack;

import LocalExceptions.UnimplementedException;

import com.example.wikibeta_003.Interfaces.ICategoryListOfArticles;
import com.example.wikibeta_003.Interfaces.IURLProvider;
import com.example.wikibeta_003.LocalDB.ExampleCategory;



public class LocalURLProvider implements IURLProvider{

	private static String pageLinkPrefix = "http://en.wikipedia.org/wiki/";
	private static IURLProvider singleProvider = null;
	private Map<ECategories, ICategoryListOfArticles> catagoriesDictionary = new HashMap<ECategories, ICategoryListOfArticles>();
	Random rand = new Random(System.currentTimeMillis());

	protected LocalURLProvider(){
		catagoriesDictionary.put(ECategories.Example, ExampleCategory.getCatagory());
	}
	
	public static IURLProvider getURLProvider() {
		if (singleProvider == null)
			singleProvider = new LocalURLProvider();
		return singleProvider;
	}
	
	public String getRandomPage(ECategories[] catagories, Stack<String> previousPages) throws InterruptedException {
		ECategories choosenCatagoryEnum = chooseFromCatagorysList(catagories);
		ICategoryListOfArticles choosenCatagory = catagoriesDictionary.get(choosenCatagoryEnum);
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

