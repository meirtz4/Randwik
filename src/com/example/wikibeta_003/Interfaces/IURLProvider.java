package com.example.wikibeta_003.Interfaces;

import java.util.Stack;

import com.example.wikibeta_003.ECategories;

public interface IURLProvider {
	public String getRandomPage(ECategories[] catagories, Stack<String> previousPages) throws InterruptedException;
}
