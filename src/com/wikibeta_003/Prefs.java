/**
 * TODO! add comments to this class
 */

package com.wikibeta_003;

import com.wikibeta_003.R;

import Utils.Utils;
import android.annotation.TargetApi;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

public class Prefs extends PreferenceActivity {

	private static int prefs=R.xml.topics;

	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		if (Utils.getUtils().isAPIAbove11())
			AddResourceApi11AndGreater();
		else
			AddResourceApiLessThan11();
	}

	@SuppressWarnings("deprecation")
	protected void AddResourceApiLessThan11()
	{
		addPreferencesFromResource(prefs);
	}

	@TargetApi(11)
	protected void AddResourceApi11AndGreater()
	{
		getFragmentManager().beginTransaction().replace(android.R.id.content,
				new PF()).commit();
	}

	@TargetApi(11)
	public static class PF extends PreferenceFragment {
		@Override
		public void onCreate(final Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.topics);
		}
	}
}