package ua.nekit2123.setupplug;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.lang.reflect.Method;

public class TabManager {
    private final Main plugin;
    private File file;
    private FileConfiguration cfg;
    private long lastModified = 0L;
    private String headerTemplate;
    private String footerTemplate;

    public TabManager(Main plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "tab.yml");
        if (!file.exists()) {
            plugin.saveResource("tab.yml", false);
        }
        load();
    }

    public synchronized void load() {
        this.cfg = YamlConfiguration.loadConfiguration(file);
        this.headerTemplate = cfg.getString("header", "&6&lFSMPGAME\n&7Faculty of Maritime and International Law");
        this.footerTemplate = cfg.getString("footer", "&eOnline: %online% / %max%\n&bDiscord: %discord%\n&eStatus: %status%");
        this.lastModified = file.lastModified();
    }

    public synchronized void reloadIfNeeded() {
        if (file.exists() && file.lastModified() != this.lastModified) load();
    }

    public Component buildHeader(Player p, int online, int max, String discord, String status) {
        reloadIfNeeded();
        String s = headerTemplate
            .replace("%player%", p.getName())
            .replace("%prefix%", getPrefix(p))
            .replace("%suffix%", getSuffix(p))
            .replace("%online%", String.valueOf(online))
            .replace("%max%", String.valueOf(max))
            .replace("%discord%", discord == null ? "" : discord)
            .replace("%status%", status == null ? "" : status);
        return LegacyComponentSerializer.legacyAmpersand().deserialize(s);
    }

    public Component buildFooter(Player p, int online, int max, String discord, String status) {
        reloadIfNeeded();
        String s = footerTemplate
                .replace("%player%", p.getName())
                .replace("%prefix%", getPrefix(p))
                .replace("%suffix%", getSuffix(p))
                .replace("%online%", String.valueOf(online))
                .replace("%max%", String.valueOf(max))
                .replace("%discord%", discord == null ? "" : discord)
                .replace("%status%", status == null ? "" : status);
        return LegacyComponentSerializer.legacyAmpersand().deserialize(s);
    }

    private String getPrefix(Player p) {
        try {
            Class<?> chatClass = Class.forName("net.milkbowl.vault.chat.Chat");
            Object reg = plugin.getServer().getServicesManager().getRegistration(chatClass);
            if (reg != null) {
                // RegisteredServiceProvider has getProvider()
                Object provider = reg.getClass().getMethod("getProvider").invoke(reg);
                if (provider != null) {
                    try {
                        Method mp = provider.getClass().getMethod("getPlayerPrefix", String.class, String.class);
                        String world = p.getWorld().getName();
                        Object res = mp.invoke(provider, world, p.getName());
                        return res == null ? "" : res.toString();
                    } catch (NoSuchMethodException ignored) {
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            // Vault not present
        } catch (Exception ignored) {}
        return "";
    }

    private String getSuffix(Player p) {
        try {
            Class<?> chatClass = Class.forName("net.milkbowl.vault.chat.Chat");
            Object reg = plugin.getServer().getServicesManager().getRegistration(chatClass);
            if (reg != null) {
                Object provider = reg.getClass().getMethod("getProvider").invoke(reg);
                if (provider != null) {
                    try {
                        Method ms = provider.getClass().getMethod("getPlayerSuffix", String.class, String.class);
                        String world = p.getWorld().getName();
                        Object res = ms.invoke(provider, world, p.getName());
                        return res == null ? "" : res.toString();
                    } catch (NoSuchMethodException ignored) {
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            // Vault not present
        } catch (Exception ignored) {}
        return "";
    }
}
