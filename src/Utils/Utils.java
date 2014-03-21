package Utils;

import android.app.Activity;
import android.os.Build;

public class Utils extends Activity{

	private static Utils utils = null;
	private static String SHARE_TEXT = "Hi! I would like to share with you a Random Wikipedia Article from Randwik ";
	private static String PAGE_NOTLOADED = "Page Not Loaded Yet... ";

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
	
	public String createShareString(String link){
		if (link==null)
			return PAGE_NOTLOADED;
		return SHARE_TEXT + link.replace(" ", "%20");
	}
}