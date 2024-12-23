package dev.padrewin.colddev.command.framework;

import dev.padrewin.colddev.command.framework.annotation.ColdExecutable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.permissions.Permissible;

public interface ColdCommand {

    /**
     * The invoke method to be called after the {@link CommandContext} has been fully populated with arguments
     * based on the {@link ArgumentsDefinition} returned from {@link #getCommandArguments()}.
     *
     * @param context the CommandContext populated with arguments
     */
    void invoke(CommandContext context);

    /**
     * @return the name of the command
     */
    String getName();

    /**
     * @return the aliases of the command
     */
    List<String> getAliases();

    /**
     * @return the permission required to use the command
     */
    String getPermission();

    /**
     * @return true if the command can only be used by players, false otherwise
     */
    boolean isPlayerOnly();

    /**
     * @return the description key of the command
     */
    String getDescriptionKey();

    /**
     * @return the {@link ArgumentsDefinition} of the command
     */
    ArgumentsDefinition getCommandArguments();

    /**
     * @return a displayable output of this command's parameters
     */
    default String getParametersString(CommandContext context) {
        return this.getCommandArguments().getParametersString(context);
    }

    /**
     * @return the methods annotated with {@link ColdExecutable}
     */
    default List<Method> getExecuteMethods() {
        return Stream.of(this.getClass().getMethods())
                .filter(m -> m.isAnnotationPresent(ColdExecutable.class))
                .collect(Collectors.toList());
    }

    /**
     * @return true if the permissible can use the command, false otherwise
     */
    default boolean canUse(Permissible permissible) {
        String permission = this.getPermission();
        return permission == null || permissible.hasPermission(permission);
    }

}
