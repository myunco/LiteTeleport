package ml.mcos.liteteleport;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SuppressWarnings("DuplicatedCode")
public class HomeInfo {
    public static LiteTeleport plugin = LiteTeleport.plugin;
    public static File homes = new File(plugin.getDataFolder(), "homes.yml");
    public static YamlConfiguration homeInfo;

    public static void loadHomeInfo() {
        if (!homes.exists()) {
            try {
                if (!homes.createNewFile()) {
                    plugin.getServer().getLogger().warning("错误：创建homes.yml失败！");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        homeInfo = YamlConfiguration.loadConfiguration(homes);
    }

    @SuppressWarnings("ConstantConditions")
    public static Location getHomeLocation(String player, String homeName) {
        String path = player + "." + homeName;
        if (!homeInfo.contains(path)) {
            return null;
        }
        return new Location(plugin.getServer().getWorld(homeInfo.getString(path + ".world")), homeInfo.getDouble(path + ".x"),
                homeInfo.getDouble(path + ".y"), homeInfo.getDouble(path + ".z"), (float) homeInfo.getDouble(path + ".yaw"),
                (float) homeInfo.getDouble(path + ".pitch"));
    }

    @SuppressWarnings("ConstantConditions")
    public static void saveHome(String player, String homeName, Location loc) {
        String path = player + "." + homeName;
        homeInfo.set(path + ".world", loc.getWorld().getName());
        homeInfo.set(path + ".x", loc.getX());
        homeInfo.set(path + ".y", loc.getY());
        homeInfo.set(path + ".z", loc.getZ());
        homeInfo.set(path + ".yaw", loc.getYaw());
        homeInfo.set(path + ".pitch", loc.getPitch());
        try {
            homeInfo.save(homes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteHome(String player, String homeName) {
        homeInfo.set(player + "." + homeName, null);
        try {
            homeInfo.save(homes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean exist(String player, String homeName) {
        return homeInfo.contains(player + "." + homeName);
    }

    public static List<String> getHomeList(String player) {
        ConfigurationSection cs = homeInfo.getConfigurationSection(player);
        if (cs == null) {
            return null;
        }
        Set<String> s = cs.getKeys(false);
        return new ArrayList<>(s);
    }

    public static String showHomeList(String player) {
        StringBuilder stringBuilder = new StringBuilder();
        List<String> homeList = getHomeList(player);
        if (homeList == null) {
            return "§6你还没有设置过家。";
        }
        for (int i = 0; i < homeList.size(); i++) {
            if (i == 0) {
                stringBuilder.append("§6家：§f").append(homeList.get(i));
                continue;
            }
            stringBuilder.append(", ").append(homeList.get(i));
        }
        return stringBuilder.toString();
    }

    public static boolean isBanName(String homeName) {
        return homeName.equals("null") || !homeName.matches("[0-9A-Za-z\u4e00-\u9fff_]*");
    }
}
