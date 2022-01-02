package ml.mcos.liteteleport.config;

import ml.mcos.liteteleport.LiteTeleport;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HomeInfo {
    public static LiteTeleport plugin = LiteTeleport.plugin;
    public static File homes = new File(plugin.getDataFolder(), "homes.yml");
    public static YamlConfiguration homeInfo;

    public static void loadHomeInfo() {
        if (!homes.exists()) {
            try {
                if (!homes.createNewFile()) {
                    plugin.getServer().getLogger().warning("错误: 创建homes.yml失败！");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        homeInfo = Config.loadConfiguration(homes);
    }

    public static Location getHomeLocation(String player, String homeName) {
        return Config.getLocation(homeInfo, player + "." + homeName);
    }

    public static void setHome(String player, String homeName, Location loc) {
        Config.setLocation(homeInfo, player + "." + homeName, loc);
        Config.saveConfiguration(homeInfo, homes);
    }

    public static void deleteHome(String player, String homeName) {
        homeInfo.set(player + "." + homeName, null);
        Config.saveConfiguration(homeInfo, homes);
    }

    public static boolean exist(String player, String homeName) {
        return homeInfo.contains(player + "." + homeName);
    }

    public static List<String> getHomeList(String player) {
        ConfigurationSection section = homeInfo.getConfigurationSection(player);
        return section == null ? null : new ArrayList<>(section.getKeys(false));
    }

    public static String showHomeList(String player) {
        StringBuilder builder = new StringBuilder();
        List<String> homeList = getHomeList(player);
        if (homeList == null) {
            return "§6你还没有设置过家。";
        }
        for (int i = 0; i < homeList.size(); i++) {
            if (i == 0) {
                builder.append("§6家：§f").append(homeList.get(i));
                continue;
            }
            builder.append(", ").append(homeList.get(i));
        }
        return builder.toString();
    }

    public static boolean isBanName(String name) {
        //return name.equals("null") || !name.matches("[0-9A-Za-z\u4e00-\u9fff`~!@#$%^&*()_+\\-=]*");
        return name.indexOf('.') != -1;
    }
}
