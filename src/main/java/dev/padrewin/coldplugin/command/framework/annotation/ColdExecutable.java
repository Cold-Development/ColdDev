package dev.padrewin.coldplugin.command.framework.annotation;

import dev.padrewin.coldplugin.command.framework.ColdCommand;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to mark a method in a {@link ColdCommand} as an executable command
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ColdExecutable {
}
