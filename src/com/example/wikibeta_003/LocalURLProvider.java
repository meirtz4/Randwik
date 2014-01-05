package com.example.wikibeta_003;

import java.util.Random;
import java.util.Stack;

import LocalExceptions.UnimplementedException;

import android.util.Log;

import com.example.wikibeta_003.Interfaces.ICategoryDB;
import com.example.wikibeta_003.Interfaces.ICategoryListOfArticles;
import com.example.wikibeta_003.Interfaces.IURLProvider;

public class LocalURLProvider implements IURLProvider{

	private static String pageLinkPrefix = "http://en.wikipedia.org/wiki/";
	private static IURLProvider singleProvider = null;
	private ICategoryDB categoryDB = CategoriesMap.getCategoriesMap();
	//private static Map<ECategories, AbstractCategory> catagoriesList;
	Random rand = new Random(System.currentTimeMillis());

	protected LocalURLProvider(){
	}
	
	public static IURLProvider getURLProvider() {
		if (singleProvider == null)
			singleProvider = new LocalURLProvider();
		return singleProvider;
	}
	
	public String getRandomPage(String[] catagories, Stack<String> previousPages) throws InterruptedException {
		String URLToReturn, choosenPage;
		Boolean pageAlreadyVisited = true;
		try {
			// go to options menu and update categories
			ICategoryListOfArticles choosenCatagory = categoryDB.getRandomCategoryOfAList(catagories);
			Log.e("LocalURLProvider", "Choosen category is - " + choosenCatagory.getCatagoryName());
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
}

