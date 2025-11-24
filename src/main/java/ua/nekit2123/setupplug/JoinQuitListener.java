package ua.nekit2123.setupplug;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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
        String displayStripped = stripColor(display);
        boolean isUa = plugin.getConfig().getString("language", "en").equals("ua");
        String message = isUa ? String.format("Гравець %s приєднався до сервера.", displayStripped)
            : String.format("Player %s joined the server.", displayStripped);
        plugin.sendDiscord(message);

        // authentication checks and session handling
        String name = player.getName();
        long last = userManager.getLastLogin(name);
        long now = System.currentTimeMillis();
        int expiryDays = plugin.getConfig().getInt("registration_expiry_days", 2);

        if (!userManager.isRegistered(name)) {
            // not registered -> restrict until /register
            applyAuthRestrictions(player);
            player.sendMessage(isUa ? "Ви не зареєстровані. Використайте /register <пароль>." : "You are not registered. Use /register <password>.");
            // also send a chat notification
            player.sendMessage(isUa ? "Повідомлення: вам потрібно зареєструватися. Команда: /register <пароль>" : "Notice: you need to register. Command: /register <password>");
            return;
        }

        // registered
        if (last > 0) {
            if (isSameDay(last, now)) {
                // already logged in earlier today -> auto-auth
                userManager.setLoggedIn(name);
                try { player.removePotionEffect(PotionEffectType.BLINDNESS); } catch (Exception ignored) {}
                // notify player they have been auto-logged in
                player.sendMessage(isUa ? "Ви автоматично увійшли (маєте доступ до сервера сьогодні)." : "You were automatically logged in for today.");
                return;
            }

            long expiryMillis = expiryDays * 24L * 60L * 60L * 1000L;
            if (now - last > expiryMillis) {
                // registration expired -> require re-register
                userManager.removeRegistration(name);
                applyAuthRestrictions(player);
                player.sendMessage(isUa ? "Термін реєстрації минув — зареєструйтесь знову: /register <пароль>." : "Registration expired — please re-register with /register <password>.");
                player.sendMessage(isUa ? "Повідомлення: вам потрібно зареєструватися заново. Команда: /register <пароль>" : "Notice: you need to re-register. Command: /register <password>");
                return;
            }
        }

        // Otherwise the player is registered but not auto-authenticated -> require login
        if (!userManager.isLoggedIn(name)) {
            applyAuthRestrictions(player);
            player.sendMessage(isUa ? "Використайте /login <пароль> для входу." : "Use /login <password> to sign in.");
            player.sendMessage(isUa ? "Повідомлення: вам потрібно увійти. Команда: /login <пароль>" : "Notice: you need to log in. Command: /login <password>");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        String display = getPreferredDisplayName(player);
        String displayStripped = stripColor(display);
        boolean isUa = plugin.getConfig().getString("language", "en").equals("ua");
        String message = isUa ? String.format("Гравець %s покинув сервер.", displayStripped)
            : String.format("Player %s left the server.", displayStripped);
        plugin.sendDiscord(message);
        // remove temporary effects on quit
        try { player.removePotionEffect(PotionEffectType.BLINDNESS); } catch (Exception ignored) {}
        userManager.setLoggedOut(player.getName());
    }

    private void applyAuthRestrictions(Player player) {
        // blindness effect
        try {
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 1, false, false, false));
        } catch (Exception ignored) {}
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (!userManager.isLoggedIn(p.getName())) {
            if (e.getFrom().getBlockX() != e.getTo().getBlockX() || e.getFrom().getBlockZ() != e.getTo().getBlockZ() || e.getFrom().getY() != e.getTo().getY()) {
                e.setTo(e.getFrom());
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        if (!userManager.isLoggedIn(e.getPlayer().getName())) e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent e) {
        if (!userManager.isLoggedIn(e.getPlayer().getName())) e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player) {
            Player p = (Player) e.getWhoClicked();
            if (!userManager.isLoggedIn(p.getName())) e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (!userManager.isLoggedIn(p.getName())) e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e) {
        if (!userManager.isLoggedIn(e.getPlayer().getName())) e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        String cmd = e.getMessage().toLowerCase();
        if (!userManager.isLoggedIn(e.getPlayer().getName())) {
            if (cmd.startsWith("/login") || cmd.startsWith("/register") || cmd.startsWith("/lang")) {
                // allow
                return;
            }
            e.setCancelled(true);
        }
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

    private String stripColor(String s) {
        if (s == null) return "";
        // remove both § and & color codes followed by a hex/format char
        return s.replaceAll("(?i)(?:&|§)[0-9A-FK-OR]", "");
    }

    private boolean isSameDay(long t1, long t2) {
        java.time.ZoneId z = java.time.ZoneId.systemDefault();
        java.time.LocalDate d1 = java.time.Instant.ofEpochMilli(t1).atZone(z).toLocalDate();
        java.time.LocalDate d2 = java.time.Instant.ofEpochMilli(t2).atZone(z).toLocalDate();
        return d1.equals(d2);
    }
}
