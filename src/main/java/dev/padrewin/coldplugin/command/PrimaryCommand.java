package dev.padrewin.coldplugin.command;

import dev.padrewin.coldplugin.ColdPlugin;
import dev.padrewin.coldplugin.command.framework.BaseColdCommand;
import dev.padrewin.coldplugin.command.framework.CommandContext;
import dev.padrewin.coldplugin.command.framework.annotation.ColdExecutable;
import dev.padrewin.coldplugin.manager.AbstractLocaleManager;
import dev.padrewin.coldplugin.utils.ColdDevUtils;
import dev.padrewin.coldplugin.utils.StringPlaceholders;

public abstract class PrimaryCommand extends BaseColdCommand {

    public PrimaryCommand(ColdPlugin coldPlugin) {
        super(coldPlugin);
    }

    @ColdExecutable
    public void execute(CommandContext context) {
        AbstractLocaleManager localeManager = this.coldPlugin.getManager(AbstractLocaleManager.class);

        String baseColor = localeManager.getLocaleMessage("base-command-color");
        localeManager.sendCustomMessage(context.getSender(), baseColor + "Running " + ColdDevUtils.GRADIENT + this.coldPlugin.getDescription().getName() + baseColor + " v" + this.coldPlugin.getDescription().getVersion());
        localeManager.sendCustomMessage(context.getSender(), baseColor + "Plugin created by: <g:#41E0F0:#FF8DCE>" + this.coldPlugin.getDescription().getAuthors().get(0));
        localeManager.sendSimpleMessage(context.getSender(), "base-command-help", StringPlaceholders.of("cmd", context.getCommandLabel()));
    }

}
