package targets.model9.q;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
public @interface FooBarAnnotation {
	Class<?>[] otherClasses() default {}; 
}