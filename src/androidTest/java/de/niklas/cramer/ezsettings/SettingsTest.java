package de.niklas.cramer.ezsettings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class SettingsTest
{
	@Test
	public void saveAndReadSettings()
	{
		// Context of the app under test.
		Context appContext = InstrumentationRegistry.getContext();

		TestSettings testSettings = new TestSettings(appContext);

		PreferenceManager.getDefaultSharedPreferences(appContext).edit().clear().apply();

		final int intValue = 10;
		final int longValue = 242424;
		final float floatValue = 2456.4643f;
		final String stringValue = "A test string";
		final float convertedFloatValue = 2425.135636f;
		testSettings.intValue = intValue;
		testSettings.longValue = longValue;
		testSettings.floatValue = floatValue;
		testSettings.stringValue = stringValue;
		testSettings.convertedFloatValue = convertedFloatValue;

		testSettings.save();

		TestSettings settings2 = new TestSettings(appContext);

		assertEquals(intValue, settings2.intValue);
		assertEquals(20, settings2.intDefaultValue);
		assertEquals(longValue, settings2.longValue);
		assertEquals(floatValue, settings2.floatValue, 0.01);
		assertEquals(stringValue, settings2.stringValue);
		assertEquals(convertedFloatValue, settings2.convertedFloatValue, 0.01);
	}

	private class TestSettings extends SettingsBase
	{
		@Preference(key = "IntValue", type = SettingsType.Integer)
		private int    intValue;
		@Preference(key = "IntDefaultValue", type = SettingsType.Integer, defaultValue = "20")
		private int    intDefaultValue;
		@Preference(key = "LongValue", type = SettingsType.Long)
		private long   longValue;
		@Preference(key = "FloatValue", type = SettingsType.Float)
		private float  floatValue;
		@Preference(key = "StringValue", type = SettingsType.String)
		private String stringValue;
		@Preference(key = "ConvertedFloatValue", type = SettingsType.String)
		private float  convertedFloatValue;

		TestSettings(Context context)
		{
			super(context);
		}

		@Override
		protected void onSave(SharedPreferences.Editor edit)
		{
			// no custom preferences
		}

		@Override
		public String toString()
		{
			return "TestSettings{" + "intValue=" + intValue + ", intDefaultValue=" + intDefaultValue + ", longValue=" + longValue + ", floatValue=" + floatValue + ", stringValue='" + stringValue + '\'' + ", convertedFloatValue=" + convertedFloatValue + '}';
		}
	}
}
