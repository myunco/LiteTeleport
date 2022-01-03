package ml.mcos.liteteleport.update;

import ml.mcos.liteteleport.LiteTeleport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class UpdateChecker {
    public static LiteTeleport plugin = LiteTeleport.plugin;
    public static String currentVersion = plugin.getDescription().getVersion();
    public static String[] current = currentVersion.split("\\.");
    public static Timer timer;

    public static void start() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    CheckResult result = checkVersionUpdate("https://myunco.sinacloud.net/C8A05E18/version.txt");
                    if (result.getResultType() == CheckResult.ResultType.SUCCESS) {
                        if (result.hasNewVersion()) {
                            String str = "§c发现新版本可用! §b当前版本: {current} §d最新版本: {latest}"
                                    .replace("{current}", currentVersion)
                                    .replace("{latest}", result.getLatestVersion());
                            plugin.getLogger().info(result.hasMajorUpdate() ? "§e(有大更新)" + str : str);
                            plugin.getLogger().info("§a下载地址: " + "https://www.mcbbs.net/thread-1268795-1-1.html");
                        }
                    } else {
                        plugin.getLogger().info("§e检查更新失败, 状态码: " + result.getResponseCode());
                    }
                } catch (IOException e) {
                    plugin.getLogger().severe("§4检查更新时发生IO异常.");
                    e.printStackTrace();
                }
            }
        }, 14000, 12 * 60 * 60 * 1000);
    }

    public static void stop() {
        if (timer != null) {
            timer.cancel();
        }
    }

    public static CheckResult checkVersionUpdate(String url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        int code = conn.getResponseCode();
        if (code == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String latestVersion = reader.readLine();
            reader.close();
            conn.disconnect();
            if (currentVersion.equals(latestVersion)) {
                return new CheckResult(null, false, code, CheckResult.ResultType.SUCCESS);
            } else {
                String[] latest = latestVersion.split("\\.");
                boolean majorUpdate;
                if (!latest[0].equals(current[0])) {
                    majorUpdate = true;
                } else {
                    majorUpdate = !latest[1].equals(current[1]);
                }
                return new CheckResult(latestVersion, majorUpdate, code, CheckResult.ResultType.SUCCESS);
            }
        } else {
            return new CheckResult(code, CheckResult.ResultType.FAILURE);
        }
    }

}
