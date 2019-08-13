package de.niklas.cramer.ezsettings;

import android.content.SharedPreferences;

interface SetSettingAction<T>
{
	void invoke(SharedPreferences.Editor editor, String key, T value);
}

