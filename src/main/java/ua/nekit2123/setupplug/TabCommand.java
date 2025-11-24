package ua.nekit2123.setupplug;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TabCommand implements CommandExecutor {
    private final Main plugin;
    private final TabManager tabManager;

    public TabCommand(Main plugin, TabManager tabManager) {
        this.plugin = plugin;
        this.tabManager = tabManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("setupplug.tab.admin")) {
            sender.sendMessage("You don't have permission to use this command.");
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage("Usage: /tabreload — reload tab.yml");
            return true;
        }
        if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("reloadfile") || args[0].equalsIgnoreCase("r")) {
            tabManager.load();
            sender.sendMessage("Tab configuration reloaded.");
            return true;
        }
        return false;
    }
}
