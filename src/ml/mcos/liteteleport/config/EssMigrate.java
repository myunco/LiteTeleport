package ml.mcos.liteteleport.config;

import ml.mcos.liteteleport.LiteTeleport;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Set;

public class EssMigrate {
    public static LiteTeleport plugin = LiteTeleport.plugin;
    public static File essFolder = new File(plugin.getDataFolder().getParentFile(), "Essentials");

    public static void migrateData(CommandSender sender) {
        if (essFolder.exists() && essFolder.isDirectory()) {
            File warps = new File(essFolder, "warps");
            sender.sendMessage(Language.messagePrefix + "§6正在迁移传送点数据...");
            migrateWarps(warps);
            File userdata = new File(essFolder, "userdata");
            sender.sendMessage(Language.messagePrefix + "§6正在迁移家数据...");
            migrateUserdata(userdata);
            sender.sendMessage(Language.messagePrefix + "§a数据迁移完成。");
            return;
        }
        sender.sendMessage(Language.messagePrefix + "§c在plugins目录下未找到Essentials插件数据文件夹，无法迁移数据。");
    }

    public static void migrateWarps(File dataFolder) {
        File[] files = dataFolder.listFiles();
        if (files == null) {
            return;
        }
        if (files.length != 0) {
            for (File file : files) {
                YamlConfiguration warp = YamlConfiguration.loadConfiguration(file);
                String world = warp.getString("world-name");
                double x = warp.getDouble("x");
                double y = warp.getDouble("y");
                double z = warp.getDouble("z");
                float yaw = (float) warp.getDouble("yaw");
                float pitch = (float) warp.getDouble("pitch");
                WarpInfo.setWarpLocation(warp.getString("name"), world, x, y, z, yaw, pitch);
            }
            WarpInfo.saveConfiguration();
        }
    }

    public static void migrateUserdata(File dataFolder) {
        File[] files = dataFolder.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            YamlConfiguration userdata = YamlConfiguration.loadConfiguration(file);
            if (!userdata.contains("homes")) {
                continue;
            }
            String player = userdata.getString("last-account-name");
            ConfigurationSection section = userdata.getConfigurationSection("homes");
            assert section != null;
            Set<String> homes = section.getKeys(false);
            for (String home : homes) {
                String world = section.getString(home + ".world-name");
                double x = section.getDouble(home + ".x");
                double y = section.getDouble(home + ".y");
                double z = section.getDouble(home + ".z");
                float yaw = (float) section.getDouble(home + ".yaw");
                float pitch = (float) section.getDouble(home + ".pitch");
                HomeInfo.setHome(player, home, world, x, y, z, yaw, pitch);
            }
            HomeInfo.saveConfiguration();
        }
    }

}
