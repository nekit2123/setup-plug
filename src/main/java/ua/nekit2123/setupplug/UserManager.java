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
    private final long DEFAULT_EXPIRY_DAYS = 2; // default days after which re-registration is required

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
        usersConfig.set(player + ".lastLogin", System.currentTimeMillis());
        usersConfig.set(player + ".lastIp", "");
        save();
    }

    public boolean checkPassword(String player, String password) {
        if (!isRegistered(player)) return false;
        String stored = usersConfig.getString(player + ".password");
        return stored != null && stored.equals(hash(password));
    }

    public void setLoggedIn(String player) { loggedIn.add(player); }
    public boolean isLoggedIn(String player) { return loggedIn.contains(player); }

    public void setLoggedOut(String player) { loggedIn.remove(player); }

    public long getLastLogin(String player) {
        return usersConfig.getLong(player + ".lastLogin", -1L);
    }

    public void setLastLogin(String player, long epochMillis) {
        usersConfig.set(player + ".lastLogin", epochMillis);
        save();
    }

    public String getLastIp(String player) {
        return usersConfig.getString(player + ".lastIp", "");
    }

    public void setLastIp(String player, String ip) {
        usersConfig.set(player + ".lastIp", ip);
        save();
    }

    public void removeRegistration(String player) {
        usersConfig.set(player, null);
        save();
        loggedIn.remove(player);
    }

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
