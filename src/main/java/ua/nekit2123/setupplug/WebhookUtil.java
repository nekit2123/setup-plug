package ua.nekit2123.setupplug;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class WebhookUtil {
    public static void sendWebhookAsync(String webhookUrl, String content, BukkitScheduler scheduler, Plugin plugin) {
        scheduler.runTaskAsynchronously(plugin, () -> sendWebhook(webhookUrl, content));
    }

    private static void sendWebhook(String webhookUrl, String content) {
        try {
            URL url = new URL(webhookUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setDoOutput(true);

            String json = String.format("{\"content\": \"%s\"}", escapeJson(content));
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            connection.setFixedLengthStreamingMode(bytes.length);

            try (OutputStream os = connection.getOutputStream()) {
                os.write(bytes);
            }

            int code = connection.getResponseCode();
            // optional: log non-204/200 codes
            if (code >= 400) {
                // ignore for now
            }
        } catch (Exception e) {
            // ignore to avoid spam in server console
        }
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
