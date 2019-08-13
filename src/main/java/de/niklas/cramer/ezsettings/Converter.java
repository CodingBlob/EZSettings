package de.niklas.cramer.ezsettings;

import java.util.function.Function;

import static java.lang.Float.parseFloat;
import static java.lang.Integer.numberOfLeadingZeros;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;

public enum Converter {
    None(Object.class, x -> x, x -> x),
    StringToString(String.class, x -> x, x -> x),
    IntToString(Integer.class, Object::toString, x -> parseInt(x.toString())),
    LongToString(Long.class, Object::toString, x -> parseLong(x.toString())),
    FloatToString(Float.class, Object::toString, x -> parseFloat(x.toString())),
    BooleanToString(Boolean.class, Object::toString, x -> Boolean.parseBoolean(x.toString()));

    private final Class<?> fieldType;
    private final Function<Object, Object> converterFunction;
    private final Function<Object, Object> convertBackFunction;

    Converter(Class<?> fieldType, Function<Object, Object> converterFunction, Function<Object, Object> convertBackFunction) {
        this.fieldType = fieldType;
        this.converterFunction = converterFunction;
        this.convertBackFunction = convertBackFunction;
    }

    public Function<Object, Object> getConverterFunction() {
        return converterFunction;
    }

    public Function<Object, Object> getConvertBackFunction() {
        return convertBackFunction;
    }

    public Class<?> getFieldType() {
        return fieldType;
    }
}
