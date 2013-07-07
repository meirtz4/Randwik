package com.example.wikibeta_003;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Stack;

import LocalExceptions.CatagoryMismatchException;
import LocalExceptions.UnimplementedException;

import com.example.wikibeta_003.Interfaces.ICategoryListOfArticles;
import com.example.wikibeta_003.Interfaces.IURLProvider;
import com.example.wikibeta_003.LocalDB.ExampleCategory;



public class LocalURLProvider implements IURLProvider{

	private static String pageLinkPrefix = "http://en.wikipedia.org/wiki/";
	private static IURLProvider singleProvider = null;
	//private Map<ECategories, ICategoryListOfArticles> catagoriesDictionary = new HashMap<ECategories, ICategoryListOfArticles>();
	private List<ICategoryListOfArticles> catagoriesList = new ArrayList<ICategoryListOfArticles>();
	Random rand = new Random(System.currentTimeMillis());

	protected LocalURLProvider(){
		catagoriesList.add(ExampleCategory.getCatagory());
	}
	
	public static IURLProvider getURLProvider() {
		if (singleProvider == null)
			singleProvider = new LocalURLProvider();
		return singleProvider;
	}
	
	public String getRandomPage(ECategories[] catagories, Stack<String> previousPages) throws InterruptedException {
		String URLToReturn, choosenPage;
		Boolean pageAlreadyVisited = true;
		try {
			ICategoryListOfArticles choosenCatagory = chooseFromCatagorysList(catagories);
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
		} catch (CatagoryMismatchException e) {
			return "wikipedia";
		}
		return URLToReturn;
	}
	
	protected ICategoryListOfArticles chooseFromCatagorysList(ECategories[] catagorys) throws CatagoryMismatchException {
		ECategories choosenCatagoryEnum = catagorys[rand.nextInt(catagorys.length)];
		for(ICategoryListOfArticles cat : catagoriesList){
			if(choosenCatagoryEnum.equals(cat.getCatagoryEnum()))
				return cat;
		}
		throw new CatagoryMismatchException();
	}
}

