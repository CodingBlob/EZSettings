package de.niklas.cramer.ezsettings;

public enum SettingsType
{
	Integer(Integer.class),
	Long(Long.class),
	Float(java.lang.Float.class),
	Boolean(java.lang.Boolean.class),
	String(java.lang.String.class);

	public Class<?> javaType;

	SettingsType(final Class<?> javaType)
	{
		this.javaType = javaType;
	}
}
