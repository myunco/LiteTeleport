package ml.mcos.liteteleport;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SuppressWarnings("DuplicatedCode")
public class WarpInfo {
    public static LiteTeleport plugin = LiteTeleport.plugin;
    public static File warps = new File(plugin.getDataFolder(), "warps.yml");
    public static YamlConfiguration warpInfo;

    public static void loadWarpInfo() {
        if (!warps.exists()) {
            try {
                if (!warps.createNewFile()) {
                    plugin.getServer().getLogger().warning("错误：创建warps.yml失败！");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        warpInfo = YamlConfiguration.loadConfiguration(warps);
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

    @SuppressWarnings("ConstantConditions")
    public static void setWarpLocation(String warp, Location loc) {
        warpInfo.set(warp + ".world", loc.getWorld().getName());
        warpInfo.set(warp + ".x", loc.getX());
        warpInfo.set(warp + ".y", loc.getY());
        warpInfo.set(warp + ".z", loc.getZ());
        warpInfo.set(warp + ".yaw", loc.getYaw());
        warpInfo.set(warp + ".pitch", loc.getPitch());
        try {
            warpInfo.save(warps);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteWarp(String warp) {
        warpInfo.set(warp, null);
        try {
            warpInfo.save(warps);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
