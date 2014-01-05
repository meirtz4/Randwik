package com.example.wikibeta_003;

import java.util.HashMap;
import java.util.Random;

import android.util.Log;

import com.example.wikibeta_003.Interfaces.ICategoryDB;
import com.example.wikibeta_003.Interfaces.ICategoryListOfArticles;
import com.example.wikibeta_003.LocalDB.AbstractCategory;
import com.example.wikibeta_003.LocalDB.NatureCategory;

public class CategoriesMap extends HashMap<String, AbstractCategory> implements ICategoryDB{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static CategoriesMap singleCategoriesMap = null;

	protected CategoriesMap(){
		/*******************************************/
		/** Here we should add ALL the categories **/
		/*******************************************/
		this.put("Nature", new NatureCategory());
		
		
		
		/*******************************************/
		initCategories();
	}

	private void initCategories() {
		int initCounter = 0;
		for(Entry<String, AbstractCategory> entry : this.entrySet()){
			entry.getValue().setCatagoryName(entry.getKey());
			initCounter++;
		}
		Log.e("CategoriesMap", "Init total of " + initCounter + " categories");
	}

	public static CategoriesMap getCategoriesMap() {
		if (singleCategoriesMap == null)
			singleCategoriesMap = new CategoriesMap();
		return singleCategoriesMap;
	}
	
	@Override
	public ICategoryListOfArticles getRandomCategoryOfAList(String[] catagories) {
		Random random = new Random();
		String choosenOne = catagories[random.nextInt(catagories.length)];
		return this.get(choosenOne);
	}
}


