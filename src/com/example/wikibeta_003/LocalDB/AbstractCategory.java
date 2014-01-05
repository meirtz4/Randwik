package com.example.wikibeta_003.LocalDB;

import java.util.Random;
import com.example.wikibeta_003.Interfaces.ICategoryListOfArticles;

import LocalExceptions.UnimplementedException;

public abstract class AbstractCategory implements ICategoryListOfArticles {

	protected Random rand = new Random(System.currentTimeMillis());
	protected String categoryName;

	public String getRandomArticle(Boolean noReturn) throws UnimplementedException {
		if (noReturn) throw new UnimplementedException();
		String[] listOfArticlesNames = getListOfArticlesNames();
		return listOfArticlesNames[rand.nextInt(listOfArticlesNames.length)];
	}

	public String getCatagoryName() {
		return categoryName;
	}

	public void setCatagoryName(String name) {
		this.categoryName = name;
	}
}
