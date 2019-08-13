package de.niklas.cramer.ezsettings;
import android.content.SharedPreferences;
interface GetSettingFunc<T>
{
	T get(SharedPreferences preferences, String key, T defaultValue);
}
