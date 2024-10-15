package dev.padrewin.colddev.command;

import dev.padrewin.colddev.ColdPlugin;
import dev.padrewin.colddev.command.framework.BaseColdCommand;
import dev.padrewin.colddev.command.framework.CommandContext;
import dev.padrewin.colddev.command.framework.annotation.ColdExecutable;
import dev.padrewin.colddev.manager.AbstractLocaleManager;
import dev.padrewin.colddev.utils.ColdDevUtils;
import dev.padrewin.colddev.utils.StringPlaceholders;

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
