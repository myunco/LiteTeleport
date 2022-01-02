package ml.mcos.liteteleport.config;

import ml.mcos.liteteleport.LiteTeleport;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class TprInfo {
    public static LiteTeleport plugin = LiteTeleport.plugin;
    public static File tpr = new File(plugin.getDataFolder(), "tpr.yml");
    public static YamlConfiguration tprInfo;

    public static void loadTprInfo() {
        if (!tpr.exists()) {
            try {
                if (!tpr.createNewFile()) {
                    plugin.getServer().getLogger().warning("错误: 创建tpr.yml失败！");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        tprInfo = Config.loadConfiguration(tpr);
    }

    public static int getTprCount(String player) {
        return tprInfo.getInt(player + ".tprCount");
    }

    public static void setTprCount(String player, int count) {
        tprInfo.set(player + ".tprCount", count);
        Config.saveConfiguration(tprInfo, tpr);
    }

}
