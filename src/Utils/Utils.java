package Utils;

import android.app.Activity;
import android.os.Build;

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
		return (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB);
	}
	
	public boolean isAPIAboveAnd11(){
		return !isAPIBelow11();
	}

	
	public boolean isAPIBelow14(){
		return (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH);
	}
	
	
	public boolean isAPIAboveAnd14(){
		return !isAPIBelow14();
	}
}
