package ua.nekit2123.setupplug;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;

public class LangCommand implements CommandExecutor {
    private final Main plugin;

    public LangCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) {
            sender.sendMessage("Usage: /lang <en|uk>");
            return true;
        }
        String lang = args[0].toLowerCase(Locale.ROOT);
        if (!lang.equals("en") && !lang.equals("ua")) {
            sender.sendMessage("Supported languages: en, ua");
            return true;
        }

        plugin.getConfig().set("language", lang);
        plugin.saveConfig();
        plugin.reloadConfig();

        String reply = lang.equals("ua") ? "Мову змінено на українську." : "Language changed to English.";
        sender.sendMessage(reply);
        return true;
    }
}
