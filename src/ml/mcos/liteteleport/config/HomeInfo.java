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
                    plugin.sendMessage(Language.homeInfoCreateFailure);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        homeInfo = Config.loadConfiguration(homes);
    }

    private static String escape(String homeName) {
        return homeName.equals("==") ? "== " : homeName;
    }

    private static String unescape(String homeName) {
        return homeName.equals("== ") ? "==" : homeName;
    }

    public static Location getHomeLocation(String player, String homeName) {
        return Config.getLocation(homeInfo, player + "." + escape(homeName));
    }

    public static void setHome(String player, String homeName, Location loc) {
        Config.setLocation(homeInfo, player + "." + escape(homeName), loc);
        Config.saveConfiguration(homeInfo, homes);
    }

    public static void setHome(String player, String homeName, String world, double x, double y, double z, double yaw, double pitch) {
        Config.setLocation(homeInfo, player + "." + escape(homeName), world, x, y, z, yaw, pitch);
    }

    public static void saveConfiguration() {
        Config.saveConfiguration(homeInfo, homes);
    }

    public static void deleteHome(String player, String homeName) {
        homeInfo.set(player + "." + escape(homeName), null);
        Config.saveConfiguration(homeInfo, homes);
    }

    public static boolean exist(String player, String homeName) {
        return homeInfo.contains(player + "." + escape(homeName));
    }

    public static List<String> getHomeList(String player) {
        ConfigurationSection section = homeInfo.getConfigurationSection(player);
        return section == null ? null : new ArrayList<>(section.getKeys(false));
    }

    public static String showHomeList(String player) {
        StringBuilder builder = new StringBuilder();
        List<String> homeList = getHomeList(player);
        if (homeList == null) {
            return Language.homeListEmpty;
        }
        for (int i = 0; i < homeList.size(); i++) {
            if (i == 0) {
                builder.append(Language.homeList).append(unescape(homeList.get(i)));
                continue;
            }
            builder.append(", ").append(unescape(homeList.get(i)));
        }
        return builder.toString();
    }

    public static boolean isBanName(String name) {
        return name.indexOf('.') != -1;
    }
}
