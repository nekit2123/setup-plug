package ua.nekit2123.setupplug;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AuthCommand implements CommandExecutor {
    private final Main plugin;
    private final UserManager userManager;

    public AuthCommand(Main plugin, UserManager userManager) {
        this.plugin = plugin;
        this.userManager = userManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this.");
            return true;
        }
        Player p = (Player) sender;
        if (command.getName().equalsIgnoreCase("register")) {
            if (args.length < 1) { p.sendMessage("Usage: /register <password>"); return true; }
            String pass = args[0];
            if (userManager.isRegistered(p.getName())) {
                p.sendMessage(plugin.getConfig().getString("language", "en").equals("ua") ? "Ви вже зареєстровані." : "You are already registered.");
                return true;
            }
            userManager.register(p.getName(), pass);
            p.sendMessage(plugin.getConfig().getString("language", "en").equals("ua") ? "Ви успішно зареєстровані." : "You have registered successfully.");
            return true;
        }

        if (command.getName().equalsIgnoreCase("login")) {
            if (args.length < 1) { p.sendMessage("Usage: /login <password>"); return true; }
            String pass = args[0];
            if (userManager.checkPassword(p.getName(), pass)) {
                userManager.setLoggedIn(p.getName());
                p.sendMessage(plugin.getConfig().getString("language", "en").equals("ua") ? "Ви увійшли." : "You are logged in.");
            } else {
                p.sendMessage(plugin.getConfig().getString("language", "en").equals("ua") ? "Невірний пароль." : "Invalid password.");
            }
            return true;
        }

        return false;
    }
}
