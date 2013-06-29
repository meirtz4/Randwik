package com.example.wikibeta_003.Interfaces;

import com.example.wikibeta_003.ECategories;

import LocalExceptions.UnimplementedException;

public interface ICategoryListOfArticles {

	public String getRandomArticle(Boolean noReturn)  throws UnimplementedException;
	public String getCatagoryName();
	public ECategories getCatagoryEnum();
}
