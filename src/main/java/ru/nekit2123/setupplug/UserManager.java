package ua.nekit2123.setupplug;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Set;

public class UserManager {
    private final Plugin plugin;
    private final File usersFile;
    private FileConfiguration usersConfig;
    private final Set<String> loggedIn = new HashSet<>();

    public UserManager(Plugin plugin) {
        this.plugin = plugin;
        this.usersFile = new File(plugin.getDataFolder(), "users.yml");
        if (!usersFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try { usersFile.createNewFile(); } catch (Exception ignored) {}
        }
        usersConfig = YamlConfiguration.loadConfiguration(usersFile);
    }

    public boolean isRegistered(String player) {
        return usersConfig.contains(player + ".password");
    }

    public void register(String player, String password) {
        usersConfig.set(player + ".password", hash(password));
        save();
    }

    public boolean checkPassword(String player, String password) {
        if (!isRegistered(player)) return false;
        String stored = usersConfig.getString(player + ".password");
        return stored != null && stored.equals(hash(password));
    }

    public void setLoggedIn(String player) { loggedIn.add(player); }
    public boolean isLoggedIn(String player) { return loggedIn.contains(player); }

    private void save() {
        try { usersConfig.save(usersFile); } catch (Exception ignored) {}
    }

    private static String hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] b = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte by : b) sb.append(String.format("%02x", by));
            return sb.toString();
        } catch (Exception e) {
            return Integer.toString(input.hashCode());
        }
    }
}
