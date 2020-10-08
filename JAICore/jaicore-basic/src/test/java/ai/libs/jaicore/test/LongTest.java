package ai.libs.jaicore.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Tag;

/**
 * Annotation for JUnit tests that take longer than 5 seconds.
 *
 * @author mwever
 */
@Tag("long-test")
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface LongTest {

}
