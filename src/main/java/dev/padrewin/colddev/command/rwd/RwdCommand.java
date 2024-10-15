package dev.padrewin.colddev.command.rwd;

import dev.padrewin.colddev.ColdPlugin;
import dev.padrewin.colddev.command.framework.BaseColdCommand;
import dev.padrewin.colddev.command.framework.CommandContext;
import dev.padrewin.colddev.command.framework.CommandInfo;
import dev.padrewin.colddev.command.framework.annotation.ColdExecutable;
import dev.padrewin.colddev.objects.ColdPluginData;
import dev.padrewin.colddev.utils.HexUtils;
import dev.padrewin.colddev.utils.ColdDevUtils;
import java.util.ArrayList;
import java.util.List;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.ComponentBuilder.FormatRetention;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

@SuppressWarnings("deprecation")
public class RwdCommand extends BaseColdCommand {

    public RwdCommand(ColdPlugin coldPlugin) {
        super(coldPlugin);
    }

    @Override
    protected CommandInfo createCommandInfo() {
        return CommandInfo.builder("rwd")
                .permission("colddev.rwd")
                .build();
    }

    @ColdExecutable
    public void execute(CommandContext context) {
        List<ColdPluginData> pluginData = this.coldPlugin.getLoadedColdPluginsData();

        ComponentBuilder builder = new ComponentBuilder();
        builder.append(TextComponent.fromLegacyText(HexUtils.colorify(
                ColdDevUtils.PREFIX + "&7Plugins installed using " + ColdDevUtils.GRADIENT + "ColdDev &7by " + ColdDevUtils.GRADIENT + "Cold Development&7. Click to view info: ")));

        boolean first = true;
        for (ColdPluginData data : pluginData) {
            if (!first)
                builder.append(TextComponent.fromLegacyText(HexUtils.colorify("&7, ")), FormatRetention.NONE);
            first = false;

            String updateVersion = data.updateVersion();
            String website = data.website();

            List<Text> content = new ArrayList<>();
            content.add(new Text(TextComponent.fromLegacyText(HexUtils.colorify("&cVersion: &4" + data.version()))));
            content.add(new Text(TextComponent.fromLegacyText(HexUtils.colorify("\n&cColdDev Version: &4" + data.coldDevVersion()))));
            if (updateVersion != null)
                content.add(new Text(TextComponent.fromLegacyText(HexUtils.colorify("\n&cAn update (&4" + updateVersion + "&c) is available! Click to open the GitHub page."))));

            TextComponent pluginName = new TextComponent(TextComponent.fromLegacyText(HexUtils.colorify(ColdDevUtils.GRADIENT + data.name())));
            pluginName.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, content.toArray(new Text[0])));

            if (website != null)
                pluginName.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, data.website()));

            builder.append(pluginName);
        }

        context.getSender().spigot().sendMessage(builder.create());
    }

}
