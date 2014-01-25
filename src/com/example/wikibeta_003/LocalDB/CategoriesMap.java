/***
 * This is a local "database" for manage all the categories of the App,
 * we need to suggest a better database but meanwhile this will do the job.
 * To add categories please insert them in the class constructor as the example:
 * 		this.put("NameCategory", new NameCategory());
 * 		where NameCategory() should be an extension of AbstractCategory
 * CategoriesMap is a Singleton
 * @author Meir Levy
 * @version 1.1
 */

package com.example.wikibeta_003.LocalDB;

import java.util.HashMap;
import java.util.Random;

import android.util.Log;

import com.example.wikibeta_003.Interfaces.ICategoryDB;
import com.example.wikibeta_003.Interfaces.ICategoryListOfArticles;

public class CategoriesMap extends HashMap<String, AbstractCategory> implements ICategoryDB{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static CategoriesMap singleCategoriesMap = null;
	String[] listOfCategories;

	protected CategoriesMap(){
		/*******************************************/
		/** Here we should add ALL the categories **/
		/*******************************************/
		this.put("Arts", new ArtsCategory());
		this.put("Business", new BusinessCategory());
		this.put("Environment", new EnvironmentCategory());
		this.put("History", new HistoryCategory());
		this.put("Law", new LawCategory());
		this.put("Mathematics", new MathematicsCategory());
		this.put("Medicine", new MedicineCategory());
		this.put("Nature", new NatureCategory());
		this.put("Society", new SocietyCategory());
		this.put("Science", new ScienceCategory());
		this.put("Politics", new PoliticsCategory());
		this.put("Technology", new TechnologyCategory());
		this.put("Geography", new GeographyCategory());
		this.put("Health", new HealthCategory());
		this.put("Sports", new SportsCategory());
		/*******************************************/
		initCategories();
	}

	private void initCategories() {
		int initCounter = 0;
		listOfCategories = new String[this.size()];
		/* Go over all the categories and set them their given names */
		for(Entry<String, AbstractCategory> entry : this.entrySet()){
			listOfCategories[initCounter] = entry.getKey();
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
	public ICategoryListOfArticles getRandomCategoryFromAList(String[] catagories) {
		Random random = new Random();
		String choosenOne = catagories[random.nextInt(catagories.length)];
		return this.get(choosenOne);
	}

	@Override
	public String[] getAllCategoriesStrings() {
		return listOfCategories;
	}
}


