package au.gov.ga.worldwind.animator.animation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates a parameter is editable by a property editor.
 * <p/>
 * Allows bounds to be provided for the value.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EditableParameter
{
	boolean bound() default false;
	double minValue() default 0.0;
	double maxValue() default 1.0;
}
