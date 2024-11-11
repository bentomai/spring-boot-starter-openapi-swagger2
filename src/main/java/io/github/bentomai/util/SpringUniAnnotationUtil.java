package io.github.bentomai.util;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @see AnnotationUtils extend
 * @author Bento Mai
 */
public class SpringUniAnnotationUtil extends AnnotationUtils {

    /**
     * Does this class have specified annotations
     * @param <A>  the type of the annotation to check for
     * @param clazz  class
     * @param annotationType annotation class
     * @return boolean
     */
    public static <A extends Annotation> boolean isAnnotation(Class<?> clazz, @Nullable Class<A> annotationType) {
        return AnnotationUtils.findAnnotation(clazz, annotationType) != null;
    }

    /**
     * Does this method have specified annotations
     * @param <A>  the type of the annotation to check for
     * @param method  method
     * @param annotationType annotation class
     * @return boolean
     */
    public static <A extends Annotation> boolean isAnnotation(Method method, @Nullable Class<A> annotationType) {
        return AnnotationUtils.findAnnotation(method, annotationType) != null;
    }

}
