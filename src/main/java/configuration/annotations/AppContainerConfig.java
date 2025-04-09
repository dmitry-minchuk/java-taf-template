package configuration.annotations;

import configuration.appcontainer.AppContainerStartParameters;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(METHOD)
public @interface AppContainerConfig {
    AppContainerStartParameters startParams() default AppContainerStartParameters.EMPTY;
    String copyFileFromPath() default "";
    String copyFileToContainerPath() default "";
}
