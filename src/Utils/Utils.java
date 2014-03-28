/***
 * 	Utils is an assistant class that returns data about the current API and handles strings for share content and about
 * 	@author Meir Levy
 *	@version 1.2
 */

package Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Build;

public class Utils extends Activity{

	protected static Utils utils = null;
	protected AlertDialog.Builder dlgAbout;
	protected static String SHARE_TEXT = "Interesting Wikipedia Article from Randwik: ";
	protected static String PAGE_NOTLOADED = "Page Not Loaded Yet... ";
	protected static String ABOUT_TITLE = "Randwik 2.1";
	protected static String ABOUT_CONTENT = "RandWik let's you enjoy in your free time from Wikipedia Articles.\n\n" +
											"By tapping the W button Randwik will load you a random article\n" +
											"You can use Randwik to focus the search on your favourite subjects by marking them in Topics menu,\n" +
											"You can also share the current article with your friends !\n\n" +
											"Enjoy,\nFatboyD team,\n2014.";
	protected static String ABOUT_CONTENT_LOW_API = "Enjoy, FatboyD team - 2014.";

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
		String linkName = link.substring(link.lastIndexOf("/") + 1, link.length());
		return SHARE_TEXT + linkName + " - " + link.replace(" ", "%20");
	}

	public String getAboutTitle() {
		return ABOUT_TITLE;
	}

	public String getAboutContent() {
		if (Utils.getUtils().isAPIBelow14())
			return ABOUT_CONTENT_LOW_API;
		return ABOUT_CONTENT;
	}
}