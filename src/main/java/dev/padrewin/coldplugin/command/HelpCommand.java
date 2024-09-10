package dev.padrewin.coldplugin.command;

import dev.padrewin.coldplugin.ColdPlugin;
import dev.padrewin.coldplugin.command.framework.Argument;
import dev.padrewin.coldplugin.command.framework.ArgumentsDefinition;
import dev.padrewin.coldplugin.command.framework.BaseColdCommand;
import dev.padrewin.coldplugin.command.framework.CommandContext;
import dev.padrewin.coldplugin.command.framework.CommandInfo;
import dev.padrewin.coldplugin.command.framework.ColdCommand;
import dev.padrewin.coldplugin.command.framework.annotation.ColdExecutable;
import dev.padrewin.coldplugin.manager.AbstractLocaleManager;
import dev.padrewin.coldplugin.utils.StringPlaceholders;

public class HelpCommand extends BaseColdCommand {

    protected final BaseColdCommand parent;
    private final CommandInfo commandInfo;
    private final boolean showCommandArgs;

    public HelpCommand(ColdPlugin coldPlugin, BaseColdCommand parent, CommandInfo commandInfo, boolean showCommandArgs) {
        super(coldPlugin);

        this.parent = parent;
        this.commandInfo = commandInfo;
        this.showCommandArgs = showCommandArgs;
    }

    public HelpCommand(ColdPlugin coldPlugin, BaseColdCommand parent, CommandInfo commandInfo) {
        this(coldPlugin, parent, commandInfo, true);
    }

    public HelpCommand(ColdPlugin coldPlugin, BaseColdCommand parent, boolean showCommandArgs) {
        this(coldPlugin, parent, CommandInfo.builder("help")
                .descriptionKey("command-help-description")
                .build(), showCommandArgs);
    }

    public HelpCommand(ColdPlugin coldPlugin, BaseColdCommand parent) {
        this(coldPlugin, parent, true);
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return this.commandInfo;
    }

    @ColdExecutable
    public void execute(CommandContext context) {
        AbstractLocaleManager localeManager = this.coldPlugin.getManager(AbstractLocaleManager.class);

        ArgumentsDefinition argumentsDefinition = this.parent.getCommandArguments();
        if (argumentsDefinition.size() != 1)
            throw new IllegalStateException("Help command parent must have exactly 1 argument.");

        Argument argument = argumentsDefinition.get(0);
        if (!(argument instanceof Argument.SubCommandArgument))
            throw new IllegalStateException("Help command parent must have a subcommand argument.");

        Argument.SubCommandArgument subCommandArgument = (Argument.SubCommandArgument) argument;
        localeManager.sendCommandMessage(context.getSender(), "command-help-title");
        for (ColdCommand command : subCommandArgument.subCommands()) {
            String descriptionKey = command.getDescriptionKey();
            if (!command.canUse(context.getSender()) || descriptionKey == null)
                continue;

            StringPlaceholders stringPlaceholders = StringPlaceholders.of(
                    "cmd", context.getCommandLabel().toLowerCase(),
                    "subcmd", command.getName().toLowerCase(),
                    "args", command.getParametersString(context),
                    "desc", localeManager.getLocaleMessage(descriptionKey)
            );

            localeManager.sendSimpleCommandMessage(context.getSender(), "command-help-list-description" + (command.getCommandArguments().size() == 0 || !this.showCommandArgs ? "-no-args" : ""), stringPlaceholders);
        }

        this.sendCustomHelpMessage(context);
    }

    protected void sendCustomHelpMessage(CommandContext context) {
        // Provides no default behavior
    }

}
