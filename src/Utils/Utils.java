package Utils;

import android.app.Activity;

public class Utils extends Activity{

	private static Utils utils = null;

	protected Utils() {

	}
	
	public static Utils getUtils() {
		if (utils == null)
			utils = new Utils();
		return utils;
	}
	
	public boolean isAPIBelow11(){
		try {
			getClass().getMethod("getFragmentManager");
			return false;
		} catch (NoSuchMethodException e) { //Api < 11
			return true;
		}
	}
	
	public boolean isAPIAbove11(){
		return !isAPIBelow11();
	}
}
