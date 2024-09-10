package dev.padrewin.coldplugin.command;

import dev.padrewin.coldplugin.ColdPlugin;
import dev.padrewin.coldplugin.command.framework.BaseColdCommand;
import dev.padrewin.coldplugin.command.framework.CommandContext;
import dev.padrewin.coldplugin.command.framework.CommandInfo;
import dev.padrewin.coldplugin.command.framework.annotation.ColdExecutable;
import dev.padrewin.coldplugin.manager.AbstractLocaleManager;

public class ReloadCommand extends BaseColdCommand {

    private final CommandInfo commandInfo;

    public ReloadCommand(ColdPlugin coldPlugin, CommandInfo commandInfo) {
        super(coldPlugin);

        this.commandInfo = commandInfo;
    }

    public ReloadCommand(ColdPlugin coldPlugin) {
        this(coldPlugin, CommandInfo.builder("reload")
                .descriptionKey("command-reload-description")
                .permission(coldPlugin.getName().toLowerCase() + ".reload")
                .build());
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return this.commandInfo;
    }

    @ColdExecutable
    public void execute(CommandContext context) {
        this.coldPlugin.reload();
        this.coldPlugin.getManager(AbstractLocaleManager.class).sendCommandMessage(context.getSender(), "command-reload-reloaded");
    }

}
