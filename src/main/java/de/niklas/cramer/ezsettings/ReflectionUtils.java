package de.niklas.cramer.ezsettings;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

class ReflectionUtils {
    static List<Field> getAllFieldsWithAnnotation(Class<?> clazz, Class<? extends Annotation> annotation) {

        Predicate<Field> annotationPredicate = x -> x.getAnnotation(annotation) != null;
        List<Field> result = Arrays.stream(clazz.getDeclaredFields()).filter(annotationPredicate).collect(Collectors.toList());

        while (clazz.getSuperclass() != Object.class) {
            List<Field> superFields = Arrays.stream(clazz.getSuperclass().getDeclaredFields()).filter(annotationPredicate).collect(Collectors.toList());
            result.addAll(superFields);
            clazz = clazz.getSuperclass();
        }

        return result;
    }
}