package dev.padrewin.coldplugin.command.argument;

import dev.padrewin.coldplugin.command.framework.Argument;
import dev.padrewin.coldplugin.command.framework.ArgumentHandler;
import dev.padrewin.coldplugin.command.framework.CommandContext;
import dev.padrewin.coldplugin.command.framework.InputIterator;
import dev.padrewin.coldplugin.utils.NMSUtil;
import dev.padrewin.coldplugin.utils.StringPlaceholders;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import java.lang.reflect.Method;

public class OfflinePlayerArgumentHandler extends ArgumentHandler<OfflinePlayer> {

    protected OfflinePlayerArgumentHandler() {
        super(OfflinePlayer.class);
    }

    @Override
    public OfflinePlayer handle(CommandContext context, Argument argument, InputIterator inputIterator) throws HandledArgumentException {
        String input = inputIterator.next();
        OfflinePlayer offlinePlayer = null;

        if (NMSUtil.isPaper()) {
            try {
                Method getOfflinePlayerIfCached = Bukkit.class.getMethod("getOfflinePlayerIfCached", String.class);
                offlinePlayer = (OfflinePlayer) getOfflinePlayerIfCached.invoke(null, input);
            } catch (NoSuchMethodException | IllegalAccessException | java.lang.reflect.InvocationTargetException e) {

                offlinePlayer = Bukkit.getOfflinePlayer(input);
            }
        } else {
            offlinePlayer = Bukkit.getOfflinePlayer(input);
        }

        if (offlinePlayer == null || !offlinePlayer.hasPlayedBefore())
            throw new HandledArgumentException("argument-handler-player", StringPlaceholders.of("input", input));

        return offlinePlayer;
    }

    @Override
    public List<String> suggest(CommandContext context, Argument argument, String[] args) {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .collect(Collectors.toList());
    }

}