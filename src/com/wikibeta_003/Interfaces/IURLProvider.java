package com.wikibeta_003.Interfaces;

import java.util.Stack;

public interface IURLProvider {
	public String getRandomPage(String[] catagories, Stack<String> previousPages) throws InterruptedException;
}
