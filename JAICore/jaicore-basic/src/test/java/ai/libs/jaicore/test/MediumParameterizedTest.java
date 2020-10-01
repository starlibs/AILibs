package ai.libs.jaicore.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;

/**
 * Annotation for *parameterized* JUnit tests that take between 1 and 5 seconds.
 *
 * @author mwever
 */
@Tag("medium-test")
@ParameterizedTest
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface MediumParameterizedTest {

}
