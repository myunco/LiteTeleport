package ml.mcos.liteteleport.update;

import ml.mcos.liteteleport.LiteTeleport;
import ml.mcos.liteteleport.config.Language;

import java.util.Timer;
import java.util.TimerTask;

public class UpdateChecker {
    private static final LiteTeleport plugin = LiteTeleport.plugin;
    private static Timer timer;
    static boolean isUpdateAvailable;
    static String newVersion;
    static String downloadLink;
    static String url = "https://r2.699101.xyz/C8A05E18/LiteTeleport.txt";

    public static void start() {
        plugin.getScheduler().runTaskAsynchronously(() -> {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    CheckResult result = new CheckResult(url, plugin.getDescription().getVersion());
                    if (result.getResultType() == CheckResult.ResultType.SUCCESS) {
                        if (result.hasNewVersion()) {
                            isUpdateAvailable = true;
                            String str = Language.replaceArgs(Language.updateFoundNewVersion, result.getCurrentVersion(), result.getLatestVersion());
                            newVersion = result.hasMajorUpdate() ? Language.updateMajorUpdate + str : str;
                            downloadLink = Language.updateDownloadLink + result.getDownloadLink();
                            plugin.sendMessage(newVersion);
                            plugin.sendMessage(downloadLink);
                            plugin.sendMessage(result.getUpdateInfo());
                        } else {
                            isUpdateAvailable = false;
                        }
                    } else {
                        plugin.sendMessage(Language.updateCheckFailure + result.getErrorMessage());
                        url = "https://pub-860afec4fd3b4eaa9ed6e814d32c1379.r2.dev/C8A05E18/LiteTeleport.txt";
                    }
                }
            }, 7000, 12 * 60 * 60 * 1000);
        });
    }

    public static void stop() {
        if (timer != null) {
            timer.cancel();
        }
    }

}
