package eu.endermite.commandwhitelist.bukkit.listeners;

import eu.endermite.commandwhitelist.bukkit.CommandWhitelistBukkit;
import eu.endermite.commandwhitelist.common.CWPermission;
import eu.endermite.commandwhitelist.common.CommandUtil;
import eu.endermite.commandwhitelist.common.ConfigCache;
import eu.endermite.commandwhitelist.common.commands.CWCommand;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.HashSet;
import java.util.Locale;

public class PlayerCommandPreProcessListener implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void PlayerExecuteCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission(CWPermission.BYPASS.permission())) return;
        String caseSensitiveLabel = CommandUtil.getCommandLabel(event.getMessage());
        String label = caseSensitiveLabel.toLowerCase();
        String messageWithoutSlash = event.getMessage().startsWith("/") ? event.getMessage().substring(1) : event.getMessage();

        BukkitAudiences audiences = CommandWhitelistBukkit.getAudiences();
        ConfigCache config = CommandWhitelistBukkit.getConfigCache();

        HashSet<String> commands = CommandWhitelistBukkit.getCommands(player);
        if (!commands.contains(label)) {
            event.setCancelled(true);
            Component message = CWCommand.getParsedErrorMessage(
                    messageWithoutSlash,
                    config.prefix + CommandWhitelistBukkit.getCommandDeniedMessage(label)
            );
            switch (config.messageType) {
                case CHAT:
                    audiences.player(player).sendMessage(message);
                    break;
                case ACTIONBAR:
                    audiences.player(player).sendActionBar(message);
                    break;
            }
            return;
        }

        HashSet<String> bannedSubCommands = CommandWhitelistBukkit.getSuggestions(player);

        for (String bannedSubCommand : bannedSubCommands) {
            if (messageWithoutSlash.startsWith(bannedSubCommand)) {
                event.setCancelled(true);
                audiences.player(player).sendMessage(CWCommand.miniMessage.deserialize(config.prefix + config.subcommand_denied));
                return;
            }
        }

        if (!event.isCancelled()) {
            String[] commandParts = event.getMessage().split(" ", 2);
            if (commandParts.length > 1) {
                event.setMessage(commandParts[0].toLowerCase(Locale.ROOT) + " " + commandParts[1]);
            } else {
                event.setMessage(event.getMessage().toLowerCase(Locale.ROOT));
            }
        }

    }
}
