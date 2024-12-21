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

    public static void start() {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    CheckResult result = new CheckResult("https://myunco.sinacloud.net/C8A05E18/LiteTeleport.txt", plugin.getDescription().getVersion());
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
