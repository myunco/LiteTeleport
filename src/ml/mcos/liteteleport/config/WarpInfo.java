package ml.mcos.liteteleport.config;

import ml.mcos.liteteleport.LiteTeleport;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class WarpInfo {
    public static LiteTeleport plugin = LiteTeleport.plugin;
    public static File warps = new File(plugin.getDataFolder(), "warps.yml");
    public static YamlConfiguration warpInfo;

    public static void loadWarpInfo() {
        if (!warps.exists()) {
            try {
                if (!warps.createNewFile()) {
                    plugin.getServer().getLogger().warning("错误: 创建warps.yml失败！");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        warpInfo = Config.loadConfiguration(warps);
    }

    @SuppressWarnings("ConstantConditions")
    public static Location getWarpLocation(String warp) {
        if (!warpInfo.contains(warp)) {
            return null;
        }
        return new Location(plugin.getServer().getWorld(warpInfo.getString(warp + ".world")), warpInfo.getDouble(warp + ".x"),
                warpInfo.getDouble(warp + ".y"), warpInfo.getDouble(warp + ".z"), (float) warpInfo.getDouble(warp + ".yaw"),
                (float) warpInfo.getDouble(warp + ".pitch"));
    }

    public static void setWarpLocation(String warp, Location loc) {
        Config.setLocation(warpInfo, warp, loc);
        Config.saveConfiguration(warpInfo, warps);
    }

    public static void deleteWarp(String warp) {
        warpInfo.set(warp, null);
        Config.saveConfiguration(warpInfo, warps);
    }

    public static boolean exist(String warp) {
        return warpInfo.contains(warp);
    }

    public static List<String> getWarpList() {
        Set<String> s = warpInfo.getKeys(false);
        return new ArrayList<>(s);
    }

    public static String showWarpList() {
        StringBuilder stringBuilder = new StringBuilder();
        List<String> warpList = getWarpList();
        if (warpList.size() == 0) {
            return "§c错误：§6没有已定义的传送点。";
        }
        for (int i = 0; i < warpList.size(); i++) {
            if (i == 0) {
                stringBuilder.append("§6传送点：§f").append(warpList.get(i));
                continue;
            }
            stringBuilder.append(", ").append(warpList.get(i));
        }
        return stringBuilder.toString();
    }

    public static boolean isBanName(String homeName) {
        return homeName.equals("null") || !homeName.matches("[0-9A-Za-z\u4E00-\u9FFF_+=-]*");
    }
}
