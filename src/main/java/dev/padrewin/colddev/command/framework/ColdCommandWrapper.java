package dev.padrewin.colddev.command.framework;

import dev.padrewin.colddev.ColdPlugin;
import dev.padrewin.colddev.config.CommentedFileConfiguration;
import dev.padrewin.colddev.manager.AbstractLocaleManager;
import dev.padrewin.colddev.utils.CommandMapUtils;
import dev.padrewin.colddev.utils.StringPlaceholders;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public class ColdCommandWrapper extends BukkitCommand {

    private final String namespace;
    private final File dataFolder;
    private final ColdPlugin coldPlugin;
    private final BaseColdCommand command;
    private final String commandPath; // Track command path for hierarchy

    public ColdCommandWrapper(ColdPlugin coldPlugin, BaseColdCommand command) {
        this(coldPlugin.getName().toLowerCase(), coldPlugin.getDataFolder(), coldPlugin, command);
    }

    public ColdCommandWrapper(String namespace, File dataFolder, ColdPlugin coldPlugin, BaseColdCommand command) {
        super("");

        this.namespace = namespace;
        this.dataFolder = dataFolder;
        this.coldPlugin = coldPlugin;
        this.command = command;
        this.commandPath = command.getCommandInfo().name();
    }

    /**
     * Registers the command to the Bukkit command map and creates a config file for it if it doesn't exist
     */
    public void register() {
        // Clear the command registry when registering commands (used during reload)
        CommandRegistry.clearRegistry();

        // Register commands
        File commandsDirectory = new File(this.dataFolder, "commands");
        commandsDirectory.mkdirs();

        String commandName = this.command.getCommandInfo().name();
        File commandConfigFile = new File(commandsDirectory, commandName + ".yml");
        boolean exists = commandConfigFile.exists();
        CommentedFileConfiguration commandConfig = CommentedFileConfiguration.loadConfiguration(commandConfigFile);

        AtomicBoolean modified = new AtomicBoolean(false);

        // Check if this is the main command
        boolean isMainCommand = this.command.getCommandInfo().name().equals(this.coldPlugin.getName().toLowerCase());

        if (!exists) {
            if (isMainCommand) {
                // Different comments for the main command
                commandConfig.addComments(
                        "This file lets you change the name, aliases, and set the priority for the main command.",
                        "If you edit the name/aliases at the top of this file, you can reload the plugin with /bits reload to apply the changes.",
                        "The main command cannot be disabled to ensure the reload functionality always works.",
                        "You can disable individual subcommands below.",
                        "Enabling the priority setting will make this command take priority over commands from other plugins on the server.");
            } else {
                // Regular comments for other commands
                commandConfig.addComments(
                        "This file lets you disable the command, change the name and aliases, and set the priority.",
                        "If you edit the name/aliases at the top of this file, you can reload the plugin with /bits reload to apply the changes.",
                        "Disabling a command only requires a plugin reload, not a full server restart.",
                        "Enabling the priority setting will make this command take priority over commands from other plugins on the server.");
            }
            modified.set(true);
        }

        if (!commandConfig.contains("name")) {
            commandConfig.set("name", commandName);
            modified.set(true);
        }

        if (!commandConfig.contains("aliases")) {
            commandConfig.set("aliases", new ArrayList<>(this.command.getCommandInfo().aliases()));
            modified.set(true);
        }

        if (!commandConfig.contains("priority")) {
            commandConfig.set("priority", this.command.hasPriority());
            modified.set(true);
        }

        // Write subcommands
        this.writeSubcommands(commandConfig, this.command, modified, commandName);

        // Always save if this is the main command and the enabled option was removed
        if (modified.get()) {
            commandConfig.save(commandConfigFile);
        }

        // Handle non-main commands - they can be disabled
        if (!isMainCommand) {
            boolean isReloadCommand = "reload".equalsIgnoreCase(commandName) ||
                    this.command.getCommandInfo().aliases().contains("reload");

            // Don't do anything else if the command is disabled (except for reload command)
            if (!commandConfig.getBoolean("enabled", true) && !isReloadCommand) {
                // Mark this command as disabled in the registry
                CommandRegistry.disableCommand(commandName);

                // Log that this command is disabled in configuration
                this.coldPlugin.getLogger().info("Command '" + commandName + "' is disabled in configuration");
                return;
            }
        }

        // Load command config values
        this.command.setNameAndAliases(commandConfig.getString("name"), commandConfig.getStringList("aliases"));

        // Load subcommand config values
        this.loadSubCommands(commandConfig, this.command, commandName);

        // Set this command's active values
        this.setName(this.command.getName());
        this.setAliases(this.command.getAliases());
        this.setPermission(this.command.getPermission());

        String descriptionKey = this.command.getDescriptionKey();
        if (descriptionKey != null) {
            AbstractLocaleManager localeManager = this.coldPlugin.getManager(AbstractLocaleManager.class);
            this.setDescription(localeManager.getCommandLocaleMessage(this.command.getDescriptionKey()));
        }

        // Finally, register the command with the server
        CommandMapUtils.registerCommand(this.namespace, this, commandConfig.getBoolean("priority", false));
    }

    private void writeSubcommands(ConfigurationSection section, ColdCommand command, AtomicBoolean modified, String parentPath) {
        CommandExecutionWalker walker = new CommandExecutionWalker(command);
        while (walker.hasNext()) {
            walker.step((cmd, argument) -> true, argument -> {
                List<BaseColdCommand> editableSubcommands = argument.subCommands().stream()
                        .filter(BaseColdCommand.class::isInstance)
                        .map(BaseColdCommand.class::cast)
                        .collect(Collectors.toList());

                if (editableSubcommands.isEmpty())
                    return null;

                ConfigurationSection subCommandsSection = section.getConfigurationSection("subcommands");
                if (subCommandsSection == null) {
                    subCommandsSection = section.createSection("subcommands");
                    modified.set(true);
                }

                for (BaseColdCommand subCommand : editableSubcommands) {
                    ConfigurationSection subCommandSection = subCommandsSection.getConfigurationSection(subCommand.getCommandInfo().name());
                    if (subCommandSection == null) {
                        subCommandSection = subCommandsSection.createSection(subCommand.getCommandInfo().name());
                        modified.set(true);
                    }

                    if (!subCommandSection.contains("name")) {
                        subCommandSection.set("name", subCommand.getCommandInfo().name());
                        modified.set(true);
                    }

                    if (!subCommandSection.contains("aliases")) {
                        subCommandSection.set("aliases", new ArrayList<>(subCommand.getCommandInfo().aliases()));
                        modified.set(true);
                    }

                    // Special handling for reload subcommand - don't allow it to be disabled
                    boolean isReloadSubCommand = "reload".equalsIgnoreCase(subCommand.getCommandInfo().name()) ||
                            subCommand.getCommandInfo().aliases().contains("reload");

                    if (isReloadSubCommand) {
                        // For reload subcommand, remove the enabled option
                        if (subCommandSection.contains("enabled")) {
                            subCommandSection.set("enabled", null);
                            modified.set(true);

                            // Log that we're fixing the config structure
                            this.coldPlugin.getLogger().info("Removing 'enabled' option from reload subcommand config - this option is not allowed for reload");
                        }
                    } else if (!subCommandSection.contains("enabled")) {
                        subCommandSection.set("enabled", true);
                        modified.set(true);
                    }

                    // Build the command path for this subcommand
                    String subCommandPath = parentPath + "." + subCommand.getCommandInfo().name();
                    this.writeSubcommands(subCommandSection, subCommand, modified, subCommandPath);
                }

                return null;
            });
        }
    }

    private void loadSubCommands(ConfigurationSection section, BaseColdCommand command, String parentPath) {
        CommandExecutionWalker walker = new CommandExecutionWalker(command);
        while (walker.hasNext()) {
            walker.step((cmd, argument) -> true, argument -> {
                List<BaseColdCommand> editableSubcommands = argument.subCommands().stream()
                        .filter(BaseColdCommand.class::isInstance)
                        .map(BaseColdCommand.class::cast)
                        .collect(Collectors.toList());

                if (editableSubcommands.isEmpty())
                    return null;

                ConfigurationSection subCommandsSection = section.getConfigurationSection("subcommands");
                if (subCommandsSection == null)
                    return null;

                for (BaseColdCommand subCommand : editableSubcommands) {
                    ConfigurationSection subCommandSection = subCommandsSection.getConfigurationSection(subCommand.getCommandInfo().name());
                    if (subCommandSection == null)
                        continue;

                    // Build the command path for this subcommand
                    String subCommandPath = parentPath + "." + subCommand.getCommandInfo().name();

                    // Special handling for reload subcommand - don't allow it to be disabled
                    boolean isReloadSubCommand = "reload".equalsIgnoreCase(subCommand.getCommandInfo().name()) ||
                            subCommand.getCommandInfo().aliases().contains("reload");

                    if (!isReloadSubCommand && subCommandSection.contains("enabled") && !subCommandSection.getBoolean("enabled", true)) {
                        // Mark this subcommand as disabled in the registry
                        CommandRegistry.disableCommand(subCommandPath);

                        // Log that this subcommand is disabled in configuration
                        this.coldPlugin.getLogger().info("Subcommand '" + subCommandPath + "' is disabled in configuration");
                        continue;
                    }

                    subCommand.setNameAndAliases(subCommandSection.getString("name"), subCommandSection.getStringList("aliases"));
                    this.loadSubCommands(subCommandSection, subCommand, subCommandPath);
                }

                return null;
            });
        }
    }

    /**
     * Removes the command from the Bukkit command map
     */
    public void unregister() {
        CommandMapUtils.unregisterCommand(this);
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        AbstractLocaleManager localeManager = this.coldPlugin.getManager(AbstractLocaleManager.class);
        if (this.command.isPlayerOnly() && !(sender instanceof Player)) {
            localeManager.sendCommandMessage(sender, "only-player");
            return true;
        }

        if (!this.command.canUse(sender)) {
            localeManager.sendCommandMessage(sender, "no-permission");
            return true;
        }

        CommandContext context = new CommandContext(this.coldPlugin, sender, commandLabel, args);
        CommandExecutionWalker walker = new CommandExecutionWalker(this.command);
        InputIterator inputIterator = new InputIterator(Arrays.asList(args));

        AtomicBoolean shownErrorMessage = new AtomicBoolean();
        boolean missingArgs = false;

        // Track the command path as we navigate through subcommands
        final StringBuilder[] currentCommandPath = {new StringBuilder(commandLabel)};

        while (walker.hasNext()) {
            if (!inputIterator.hasNext()) {
                List<Argument> remainingArguments = walker.walkRemaining();
                if (remainingArguments.stream().allMatch(Argument::optional))
                    break; // All remaining arguments are optional, this command execution is valid

                remainingArguments.forEach(context::put);
                missingArgs = true;
                break;
            }

            walker.step((command, argument) -> {
                // Skip the argument if the condition is not met, insert a null
                if (!argument.condition().test(context)) {
                    context.put(argument);
                    return true;
                }

                inputIterator.clearStack();
                InputIterator beforeState = inputIterator.clone();
                try {
                    ArgumentHandler<?> handler = argument.handler();
                    Object parsedArgument = handler.handle(context, argument, inputIterator);
                    context.put(argument, parsedArgument, inputIterator.getStack());
                    return true;
                } catch (ArgumentHandler.HandledArgumentException e) {
                    if (argument.optional() && walker.hasNextStep()) { // Skip if optional, and we have more arguments, try the next argument instead and insert a null
                        inputIterator.restore(beforeState);
                        context.put(argument);
                        return true;
                    }

                    String message = localeManager.getCommandLocaleMessage(e.getMessage(), e.getPlaceholders());
                    localeManager.sendCommandMessage(sender, "invalid-argument", StringPlaceholders.of("message", message));
                    if (!(walker.hasNextStep() && !inputIterator.hasNext())) // Show usage if this argument is invalid and there are still more arguments and no player input
                        shownErrorMessage.set(true);
                    context.put(argument);
                    return false;
                } catch (Exception e) {
                    e.printStackTrace();
                    localeManager.sendCommandMessage(sender, "unknown-command-error");
                    shownErrorMessage.set(true);
                    context.put(argument);
                    return false;
                }
            }, argument -> {
                // Skip the argument if the condition is not met
                if (!argument.condition().test(context))
                    return null;

                String input = inputIterator.next();
                ColdCommand match = argument.subCommands().stream()
                        .filter(subCommand -> Stream.concat(Stream.of(subCommand.getName()), subCommand.getAliases().stream()).anyMatch(s -> s.equalsIgnoreCase(input)))
                        .findFirst()
                        .orElse(null);

                if (match == null) {
                    localeManager.sendCommandMessage(sender, "invalid-subcommand");
                    shownErrorMessage.set(true);
                    context.put(argument);
                    return null;
                }

                if (match.isPlayerOnly() && !(sender instanceof Player)) {
                    localeManager.sendCommandMessage(sender, "only-player");
                    shownErrorMessage.set(true);
                    context.put(argument);
                    return null;
                }

                if (!match.canUse(sender)) {
                    localeManager.sendCommandMessage(sender, "no-permission");
                    shownErrorMessage.set(true);
                    context.put(argument);
                    return null;
                }

                // Update the command path with this subcommand
                String subCommandName = match.getName();
                String updatedCommandPath = currentCommandPath[0] + "." + subCommandName;

                // Check if this subcommand is disabled
                if (CommandRegistry.isDisabled(updatedCommandPath)) {
                    localeManager.sendCommandMessage(sender, "command-disabled",
                            StringPlaceholders.of("command", subCommandName));
                    shownErrorMessage.set(true);
                    context.put(argument);
                    return null;
                }

                // Update the current command path
                currentCommandPath[0] = new StringBuilder(updatedCommandPath);

                context.put(argument, null, Collections.singletonList(input));
                return match;
            });
        }

        if (walker.isCompleted() && !missingArgs) {
            ColdCommand commandToExecute = walker.getCurrentCommand();
            if (!commandToExecute.canUse(sender)) {
                localeManager.sendCommandMessage(sender, "no-permission");
                return true;
            }

            commandToExecute.invoke(context);
        } else if (!shownErrorMessage.get()) {
            List<Argument> allArguments = context.getArgumentsPath();
            allArguments.addAll(walker.walkRemaining());
            allArguments.addAll(walker.getUnconsumed());
            String argumentsString = ArgumentsDefinition.getParametersString(context, allArguments);
            localeManager.sendCommandMessage(sender, "command-usage", StringPlaceholders.of("cmd", context.getCommandLabel(), "args", argumentsString));
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String commandLabel, String[] args) {
        boolean isPlayer = sender instanceof Player;
        if (this.command.isPlayerOnly() && !isPlayer || !this.command.canUse(sender))
            return Collections.emptyList();

        CommandContext context = new CommandContext(this.coldPlugin, sender, commandLabel, args);
        CommandExecutionWalker walker = new CommandExecutionWalker(this.command);
        InputIterator inputIterator = new InputIterator(Arrays.asList(args));

        List<String> suggestions = new ArrayList<>();
        // Track the command path for tab completion
        final StringBuilder[] currentCommandPath = {new StringBuilder(commandLabel)};

        while (walker.hasNext()) {
            walker.step((command, argument) -> {
                // Skip the argument if the condition is not met
                if (!argument.condition().test(context))
                    return true;

                if (!inputIterator.hasNext()) {
                    suggestions.addAll(argument.handler().suggest(context, argument, new String[0]));
                    return argument.optional();
                }

                inputIterator.clearStack();
                InputIterator beforeState = inputIterator.clone();
                try {
                    ArgumentHandler<?> handler = argument.handler();
                    String input = inputIterator.peek();
                    if (input.isEmpty()) // Force into the catch block, empty input should never be valid
                        throw new ArgumentHandler.HandledArgumentException("");

                    Object parsedArgument = handler.handle(context, argument, inputIterator);
                    if (!inputIterator.hasNext()) { // No more player input, always show suggestions
                        String[] remainingArgs = inputIterator.getStack().toArray(new String[0]);
                        argument.handler().suggest(context, argument, remainingArgs).stream()
                                .filter(x -> StringUtil.startsWithIgnoreCase(x, String.join(" ", remainingArgs)))
                                .forEach(suggestions::add);
                        return argument.optional();
                    }

                    context.put(argument, parsedArgument, inputIterator.getStack());
                    return true;
                } catch (ArgumentHandler.HandledArgumentException e) {
                    List<String> remainingInput = new ArrayList<>(inputIterator.getStack());
                    while (inputIterator.hasNext())
                        remainingInput.add(inputIterator.next());

                    String[] remainingArgs = remainingInput.toArray(new String[0]);
                    argument.handler().suggest(context, argument, remainingArgs).stream()
                            .filter(x -> StringUtil.startsWithIgnoreCase(x, String.join(" ", remainingInput)))
                            .forEach(suggestions::add);

                    if (argument.optional() && walker.hasNextStep()) {
                        inputIterator.restore(beforeState);
                        return true;
                    }

                    return false;
                } catch (Exception e) {
                    return false;
                }
            }, argument -> {
                // Skip the argument if the condition is not met
                if (!argument.condition().test(context))
                    return null;

                if (!inputIterator.hasNext()) {
                    // Only suggest enabled subcommands
                    this.streamUsableSubCommands(argument, sender)
                            .filter(cmd -> {
                                String cmdPath = currentCommandPath[0] + "." + cmd.getName();

                                // Special case: always allow the reload subcommand
                                if (cmd.getName().equalsIgnoreCase("reload")) {
                                    return true;
                                }

                                return !CommandRegistry.isDisabled(cmdPath);
                            })
                            .flatMap(x -> Stream.concat(Stream.of(x.getName()), x.getAliases().stream()))
                            .forEach(suggestions::add);
                    return null;
                }

                String input = inputIterator.next();
                ColdCommand subCommand = this.streamUsableSubCommands(argument, sender)
                        .filter(x -> Stream.concat(Stream.of(x.getName()), x.getAliases().stream()).anyMatch(s -> s.equalsIgnoreCase(input)))
                        .findFirst()
                        .orElse(null);

                if (subCommand != null) {
                    // Update command path
                    String subCommandPath = currentCommandPath[0] + "." + subCommand.getName();

                    // Check if this subcommand is disabled
                    if (CommandRegistry.isDisabled(subCommandPath) && !subCommand.getName().equalsIgnoreCase("reload")) {
                        return null;
                    }

                    // Update current path
                    currentCommandPath[0] = new StringBuilder(subCommandPath);

                    if (!inputIterator.hasNext())
                        return null;
                    return subCommand;
                }

                // Only suggest matching enabled subcommands
                this.streamUsableSubCommands(argument, sender)
                        .filter(cmd -> {
                            String cmdPath = currentCommandPath[0] + "." + cmd.getName();

                            // Special case: always allow the reload subcommand
                            if (cmd.getName().equalsIgnoreCase("reload")) {
                                return true;
                            }

                            return !CommandRegistry.isDisabled(cmdPath);
                        })
                        .flatMap(x -> Stream.concat(Stream.of(x.getName()), x.getAliases().stream()))
                        .filter(x -> StringUtil.startsWithIgnoreCase(x, input))
                        .forEach(suggestions::add);

                return null;
            });
        }

        return suggestions;
    }

    private Stream<ColdCommand> streamUsableSubCommands(Argument.SubCommandArgument argument, CommandSender sender) {
        return argument.subCommands().stream()
                .filter(x -> x.canUse(sender))
                .filter(x -> !x.isPlayerOnly() || sender instanceof Player);
    }

    @Override
    public String getName() {
        return this.command.getName();
    }

    @Override
    public List<String> getAliases() {
        return this.command.getAliases();
    }

    @Override
    public String getPermission() {
        return this.command.getPermission();
    }

    public BaseColdCommand getWrappedCommand() {
        return this.command;
    }
}