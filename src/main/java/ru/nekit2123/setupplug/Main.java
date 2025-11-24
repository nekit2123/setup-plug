package ua.nekit2123.setupplug;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import java.lang.reflect.Method;

public final class Main extends JavaPlugin {
    private UserManager userManager;
    private BrandListener brandListener;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.userManager = new UserManager(this);
        this.brandListener = new BrandListener(this);

        // register events and commands
        getServer().getPluginManager().registerEvents(new JoinQuitListener(this, userManager), this);
        this.getCommand("register").setExecutor(new AuthCommand(this, userManager));
        this.getCommand("login").setExecutor(new AuthCommand(this, userManager));
        this.getCommand("commands").setExecutor(new CommandsCommand(this));
        this.getCommand("lang").setExecutor(new LangCommand(this));

        // schedule tab updater
        new BukkitRunnable() {
            @Override
            public void run() {
                updateTabForAll();
            }
        }.runTaskTimer(this, 20L, 20L * 10); // every 10 seconds
    }

    @Override
    public void onDisable() {
        // plugin shutdown
    }

    public void sendDiscord(String content) {
        String webhook = getConfig().getString("discord-webhook-url");
        if (webhook == null || webhook.isEmpty()) return;
        WebhookUtil.sendWebhookAsync(webhook, content, getServer().getScheduler(), this);
    }

    private void updateTabForAll() {
        int online = Bukkit.getOnlinePlayers().size();
        int max = Bukkit.getMaxPlayers();
        String discord = getConfig().getString("discord-url", "discord.gg/rDKaumfPt9");

        Component header = Component.text("FSMPGAME").color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD)
                .append(Component.newline())
                .append(Component.text("Faculty of Maritime and International Law").color(NamedTextColor.GRAY));

        Component footerBase = Component.text("Online: ").color(NamedTextColor.YELLOW)
                .append(Component.text(online + " / " + max).color(NamedTextColor.WHITE))
                .append(Component.newline())
                .append(Component.text("Discord: ").color(NamedTextColor.YELLOW))
                .append(Component.text(discord).color(NamedTextColor.AQUA));

        for (Player p : Bukkit.getOnlinePlayers()) {
            boolean lunar = brandListener != null && brandListener.isLunar(p);
            Component footer = footerBase;
            // add login status
            String status = userManager.isLoggedIn(p.getName()) ? "Logged" : "Guest";
            footer = footer.append(Component.newline()).append(Component.text("Status: ").color(NamedTextColor.YELLOW))
                    .append(Component.text(status).color(NamedTextColor.WHITE));
            // if lunar, add marker
            if (lunar) footer = footer.append(Component.newline()).append(Component.text("Lunar Client").color(NamedTextColor.AQUA));

            // Use reflection to call Player.sendPlayerListHeaderFooter(Component, Component) if present,
            // otherwise fall back to legacy string method via reflection.
            try {
                // try modern method with Components
                Method method = p.getClass().getMethod("sendPlayerListHeaderFooter", Component.class, Component.class);
                method.invoke(p, header, footer);
            } catch (NoSuchMethodException ex) {
                // fallback to string method
                try {
                    String fallbackHeaderStr = "§6§lFSMPGAME\n§7Faculty of Maritime and International Law";
                    String fallbackFooterStr = "§eOnline: " + online + " / " + max + "\n§bDiscord: " + discord + "\n§eStatus: " + status + (lunar ? "\n§bLunar Client" : "");
                    Method legacy = p.getClass().getMethod("sendPlayerListHeaderFooter", String.class, String.class);
                    legacy.invoke(p, fallbackHeaderStr, fallbackFooterStr);
                } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | java.lang.reflect.InvocationTargetException ignored) {
                    // nothing we can do
                }
            } catch (IllegalAccessException | java.lang.reflect.InvocationTargetException ignored) {
                // ignore invocation errors
            }
        }
    }
}
