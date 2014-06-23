/***
 * Local provider is getting the articles from a per created local data base
 * LocalURLProvider is a Singleton and uses CategoriesMap as his a
 * @author Meir Levy
 * @version 1.2
 */

package com.randwik;

import java.util.Random;
import java.util.Stack;

import LocalExceptions.UnimplementedException;

import android.util.Log;

import com.randwik.Interfaces.ICategoryDB;
import com.randwik.Interfaces.ICategoryListOfArticles;
import com.randwik.Interfaces.IURLProvider;
import com.randwik.LocalDB.CategoriesMap;

public class LocalURLProvider implements IURLProvider{

	private static String pageLinkPrefix = "http://he.wikipedia.org/wiki/";
	private static IURLProvider singleProvider = null;
	private ICategoryDB categoryDB = CategoriesMap.getCategoriesMap();
	Random rand = new Random(System.currentTimeMillis());

	protected LocalURLProvider(){
	}
	
	public static IURLProvider getURLProvider() {
		if (singleProvider == null)
			singleProvider = new LocalURLProvider();
		return singleProvider;
	}
	
	/**
	 * @param catagories - The list of possible categories chosen by the user
	 */
	public String getRandomPage(String[] catagories, Stack<String> previousPages) throws InterruptedException {
		String URLToReturn, choosenPage;
		Boolean pageAlreadyVisited = true;
		try {
			/* Choose a category out of the possibilities of the user */
			ICategoryListOfArticles choosenCatagory = categoryDB.getRandomCategoryFromAList(catagories);
			Log.e("LocalURLProvider", "Choosen category is - " + choosenCatagory.getCatagoryName());
			do {
				/* Get a random article from the category */
				choosenPage = choosenCatagory.getRandomArticle(false);
				/* Make sure the page is not in the previous pages stack - please suggest a better solution for that */
				if (previousPages==null || previousPages.search(choosenPage) < 0) 
				{
					pageAlreadyVisited = false;
				}
			} while (pageAlreadyVisited);
			
			URLToReturn = pageLinkPrefix + choosenPage;
		} catch (UnimplementedException e) {
			Log.e("getRandomPage", "FAIL to get random page!");
			return "wikipedia";
		}
		return URLToReturn;
	}
}

