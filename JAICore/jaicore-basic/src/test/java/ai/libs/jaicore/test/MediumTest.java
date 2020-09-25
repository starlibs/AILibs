package ai.libs.jaicore.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Tag;

@Tag("long-test")
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface MediumTest {

}
