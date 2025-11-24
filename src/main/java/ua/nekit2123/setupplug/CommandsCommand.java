package ua.nekit2123.setupplug;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;

import java.util.Map;

public class CommandsCommand implements CommandExecutor {
    private final Main plugin;

    public CommandsCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        PluginDescriptionFile desc = plugin.getDescription();
        Map<String, Map<String, Object>> commands = desc.getCommands();
        sender.sendMessage("§6Available commands for " + desc.getName() + ":");
        if (commands == null || commands.isEmpty()) {
            sender.sendMessage("§7(no commands defined)");
            return true;
        }
        for (Map.Entry<String, Map<String, Object>> e : commands.entrySet()) {
            String cmd = e.getKey();
            Map<String, Object> meta = e.getValue();
            String descTxt = "";
            if (meta != null && meta.get("description") != null) descTxt = meta.get("description").toString();
            String usage = "";
            if (meta != null && meta.get("usage") != null) usage = meta.get("usage").toString();
            sender.sendMessage("§e/" + cmd + (usage.isEmpty() ? "" : " — " + usage) + " §7- " + descTxt);
        }
        return true;
    }
}
