package ua.nekit2123.setupplug;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.Bukkit;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BrandListener implements Listener, PluginMessageListener {
    private final Main plugin;
    // track brands per player
    private final Map<UUID, String> brands = new ConcurrentHashMap<>();

    public BrandListener(Main plugin) {
        this.plugin = plugin;
        // register incoming plugin channel listener
        Bukkit.getMessenger().registerIncomingPluginChannel(plugin, "minecraft:brand", this);
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!"minecraft:brand".equals(channel)) return;
        try {
            String brand = new String(message, StandardCharsets.UTF_8);
            brands.put(player.getUniqueId(), brand);
        } catch (Exception ignored) {}
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        // request brand by sending a brand request? Not required; many clients send brand automatically
        // mark default if absent
        brands.putIfAbsent(e.getPlayer().getUniqueId(), "unknown");
    }

    public boolean isLunar(Player p) {
        String b = brands.get(p.getUniqueId());
        if (b == null) return false;
        b = b.toLowerCase();
        return b.contains("lunar") || b.contains("lunarclient");
    }
}
