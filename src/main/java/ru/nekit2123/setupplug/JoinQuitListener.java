package ua.nekit2123.setupplug;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

public class JoinQuitListener implements Listener {
    private final Main plugin;
    private final UserManager userManager;
    private final Plugin essentialsPlugin; // may be null

    public JoinQuitListener(Main plugin, UserManager userManager) {
        this.plugin = plugin;
        this.userManager = userManager;
        this.essentialsPlugin = plugin.getServer().getPluginManager().getPlugin("Essentials") != null
                ? plugin.getServer().getPluginManager().getPlugin("Essentials")
                : plugin.getServer().getPluginManager().getPlugin("EssentialsX");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        String display = getPreferredDisplayName(player);
        boolean isUa = plugin.getConfig().getString("language", "en").equals("ua");
        String message = isUa ? String.format("Гравець %s приєднався до сервера.", display)
                : String.format("Player %s joined the server.", display);
        plugin.sendDiscord(message);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        String display = getPreferredDisplayName(player);
        boolean isUa = plugin.getConfig().getString("language", "en").equals("ua");
        String message = isUa ? String.format("Гравець %s покинув сервер.", display)
                : String.format("Player %s left the server.", display);
        plugin.sendDiscord(message);
    }

    private String getPreferredDisplayName(Player player) {
        // Prefer Essentials nickname/display if plugin present, fall back to player.getDisplayName()/getName()
        if (essentialsPlugin != null) {
            try {
                // Essentials has method getUser(Player) returning User, which may have getNickname() or getDisplayName()
                Method getUser = essentialsPlugin.getClass().getMethod("getUser", org.bukkit.entity.Player.class);
                Object user = getUser.invoke(essentialsPlugin, player);
                if (user != null) {
                    // try getNickname()
                    try {
                        Method getNick = user.getClass().getMethod("getNickname");
                        Object nick = getNick.invoke(user);
                        if (nick instanceof String && !((String) nick).isEmpty()) return (String) nick;
                    } catch (NoSuchMethodException ignored) {}
                    // try getDisplayName()
                    try {
                        Method getDisplay = user.getClass().getMethod("getDisplayName");
                        Object d = getDisplay.invoke(user);
                        if (d instanceof String && !((String) d).isEmpty()) return (String) d;
                    } catch (NoSuchMethodException ignored) {}
                }
            } catch (Exception ignored) {
                // reflection failed, fall through to default
            }
        }
        // fallback to Bukkit/vanilla display name
        String disp = player.getDisplayName();
        return (disp != null && !disp.isEmpty()) ? disp : player.getName();
    }
}
