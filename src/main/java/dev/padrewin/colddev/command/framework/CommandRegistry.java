package dev.padrewin.colddev.command.framework;

import java.util.HashMap;
import java.util.Map;

/**
 * A registry to keep track of which commands/subcommands are disabled via configuration
 */
public class CommandRegistry {
    private static final Map<String, Boolean> disabledCommands = new HashMap<>();

    /**
     * Mark a command or subcommand as disabled
     *
     * @param commandPath The full path of the command (e.g., "bits.give")
     */
    public static void disableCommand(String commandPath) {
        disabledCommands.put(commandPath.toLowerCase(), true);
    }

    /**
     * Check if a command or subcommand is disabled
     *
     * @param commandPath The full path of the command (e.g., "bits.give")
     * @return True if the command is disabled, false otherwise
     */
    public static boolean isDisabled(String commandPath) {
        return disabledCommands.getOrDefault(commandPath.toLowerCase(), false);
    }

    /**
     * Clear all disabled command entries - use when reloading
     */
    public static void clearRegistry() {
        disabledCommands.clear();
    }
}