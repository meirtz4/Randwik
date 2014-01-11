package com.example.wikibeta_003.Interfaces;

public interface ICategoryDB {
	ICategoryListOfArticles getRandomCategoryFromAList(String[] catagories);
	String[] getAllCategoriesStrings();
}
