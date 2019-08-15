package de.niklas.cramer.ezsettings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class SettingsBase {
    private Context context;
    private static Map<SettingsType, GetSettingFunc<Object>> settingsGetterMap = new HashMap<>();
    private static Map<SettingsType, SetSettingAction<Object>> settingsSetterMap = new HashMap<>();
    private SharedPreferences preferences;

    protected SettingsBase(final Context context) {
        this.context = context;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        readSettings(preferences);
    }

    protected SettingsBase(final String preferencesName, final Context context) {
        preferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE);
        readSettings(preferences);
    }

    static {
        initMaps();
    }

    private static void initMaps() {
        for (SettingsType settingsType : SettingsType.values()) {

            switch (settingsType) {
                case Integer:
                    settingsGetterMap.put(settingsType, (preferences, key, defaultValue) -> preferences.getInt(key, (Integer) defaultValue));
                    settingsSetterMap.put(settingsType, (editor, key, value) -> editor.putInt(key, (Integer) value));
                    break;
                case Long:
                    settingsGetterMap.put(settingsType, (preferences, key, defaultValue) -> preferences.getLong(key, (Long) defaultValue));
                    settingsSetterMap.put(settingsType, (editor, key, value) -> editor.putLong(key, (Long) value));
                    break;
                case Float:
                    settingsGetterMap.put(settingsType, (preferences, key, defaultValue) -> preferences.getFloat(key, (Float) defaultValue));
                    settingsSetterMap.put(settingsType, (editor, key, value) -> editor.putFloat(key, (Float) value));
                    break;
                case Boolean:
                    settingsGetterMap.put(settingsType, (preferences, key, defaultValue) -> preferences.getBoolean(key, (Boolean) defaultValue));
                    settingsSetterMap.put(settingsType, (editor, key, value) -> editor.putBoolean(key, (Boolean) value));
                    break;
                case String:
                    settingsGetterMap.put(settingsType, (preferences, key, defaultValue) -> preferences.getString(key, (String) defaultValue));
                    settingsSetterMap.put(settingsType, (editor, key, value) -> editor.putString(key, value + ""));
                    break;
            }
        }
    }

    public void save() {
        SharedPreferences.Editor edit = preferences.edit();

        for (Field field : ReflectionUtils.getAllFieldsWithAnnotation(getClass(), Preference.class)) {
            final Preference annotation = field.getAnnotation(Preference.class);
            field.setAccessible(true);
            if (annotation != null) {
                final String key = annotation.key();
                final SettingsType type = annotation.type();
                Class<?> fieldType = field.getType();

                Converter converter = getConverterForType(fieldType, type);
                Function<Object, Object> converterFunction = converter.getConverterFunction();

                final SetSettingAction<Object> setter = settingsSetterMap.get(type);
                try {
                    Object value = converterFunction.apply(field.get(this));
                    setter.invoke(edit, key, value);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("cannot save setting: " + key);
                }
            }
        }

        onSave(edit);
        edit.apply();
    }

    private Converter getConverterForType(Class<?> fieldType, final SettingsType type) {
        Class<?> objectType = builtInMap.getOrDefault(fieldType.getName(), fieldType);

        if (type.javaType == objectType) {
            return Converter.None;
        }

        return Arrays.stream(Converter.values()).filter(x -> x.getFieldType() == objectType).findFirst().orElse(Converter.None);
    }

    void readSettings(final SharedPreferences preferences) {
        List<Field> fields = ReflectionUtils.getAllFieldsWithAnnotation(getClass(), Preference.class);

        for (Field field : fields) {
            field.setAccessible(true);
            final Preference preference = field.getAnnotation(Preference.class);
            if (preference != null) {
                final String key = preference.key();
                final SettingsType type = preference.type();
                String defaultValue = preference.defaultValue();
                Class<?> fieldType = field.getType();

                Converter converterForType = getConverterForType(fieldType, type);
                Function<Object, Object> convertBackFunction = converterForType.getConvertBackFunction();
                final Function<Object, Object> converterFunction = converterForType.getConverterFunction();

                Object typedDefaultValue = converterFunction.apply(getDefaultValue(fieldType));
                if (!defaultValue.isEmpty()) {
                    try {
                        typedDefaultValue = getDefaultConverter(fieldType).apply(defaultValue);
                    } catch (Exception e) {
                        System.err.println("failed to parse default value for: " + key + " from: " + defaultValue);
                        e.printStackTrace();
                    }
                }

                final GetSettingFunc<Object> getterFunction = settingsGetterMap.get(type);
                final Object settingsValue = convertBackFunction.apply(getterFunction.get(preferences, key, typedDefaultValue));
                try {
                    field.set(this, settingsValue);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Object getDefaultValue(final Class<?> fieldType) {
        return defaultValueMap.get(builtInMap.get(fieldType.getName()));
    }

    private Function<String, Object> getDefaultConverter(Class fieldType) {
        return defaultValueConverterMap.get(builtInMap.get(fieldType.getName()));
    }

    protected abstract void onSave(final SharedPreferences.Editor edit);

    private static Map<String, Class> builtInMap = new HashMap<>();

    private static Map<Class, Object> defaultValueMap = new HashMap<>();

    private static Map<Class, Function<String, Object>> defaultValueConverterMap = new HashMap<>();

    static {
        initDefaultValueConverters();
        initDefaultValues();
        initBuiltinTypes();
    }

    private static void initBuiltinTypes() {
        builtInMap.put("int", Integer.class);
        builtInMap.put("long", Long.class);
        builtInMap.put("double", Double.class);
        builtInMap.put("float", Float.class);
        builtInMap.put("bool", Boolean.class);
        builtInMap.put("byte", Byte.class);
        builtInMap.put("void", Void.class);
        builtInMap.put("short", Short.class);
    }

    private static void initDefaultValues() {
        defaultValueMap.put(Integer.class, 0);
        defaultValueMap.put(Long.class, 0L);
        defaultValueMap.put(Double.class, 0D);
        defaultValueMap.put(Float.class, 0F);
        defaultValueMap.put(Boolean.class, false);
        defaultValueMap.put(Byte.class, 0x00);
        defaultValueMap.put(Void.class, null);
        defaultValueMap.put(Short.class, (short) 0);
    }

    private static void initDefaultValueConverters() {
        defaultValueConverterMap.put(Integer.class, Integer::parseInt);
        defaultValueConverterMap.put(Long.class, Long::parseLong);
        defaultValueConverterMap.put(Double.class, Double::parseDouble);
        defaultValueConverterMap.put(Float.class, Float::parseFloat);
        defaultValueConverterMap.put(Boolean.class, Boolean::parseBoolean);
        defaultValueConverterMap.put(Byte.class, Byte::parseByte);
        defaultValueConverterMap.put(Void.class, null);
        defaultValueConverterMap.put(Short.class, Short::parseShort);
    }
}
