package ai.libs.jaicore.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Annotation for JUnit tests that are faster than a second to execute.
 *
 * @author mwever
 */
@Tag("short-test")
@Test
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ShortTest {

}
