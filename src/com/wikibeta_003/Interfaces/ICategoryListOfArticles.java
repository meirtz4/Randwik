package com.wikibeta_003.Interfaces;

import LocalExceptions.UnimplementedException;

public interface ICategoryListOfArticles {
	public String getRandomArticle(Boolean noReturn)  throws UnimplementedException;
	public String getCatagoryName();
	public String[] getListOfArticlesNames();
}
