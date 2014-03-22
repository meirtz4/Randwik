package com.randwik.Interfaces;

public interface ICategoryDB {
	ICategoryListOfArticles getRandomCategoryFromAList(String[] catagories);
	String[] getAllCategoriesStrings();
}
