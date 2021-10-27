package ml.mcos.liteteleport.config;

import ml.mcos.liteteleport.LiteTeleport;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class SpawnInfo {
    public static LiteTeleport plugin = LiteTeleport.plugin;
    public static File spawn = new File(plugin.getDataFolder(), "spawn.yml");
    public static YamlConfiguration spawnInfo;

    public static void loadSpawnInfo() {
        if (!spawn.exists()) {
            try {
                if (!spawn.createNewFile()) {
                    plugin.getServer().getLogger().warning("错误：创建spawn.yml失败！");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        spawnInfo = YamlConfiguration.loadConfiguration(spawn);
    }

    public static String getSpawnWorld() {
        return spawnInfo.contains("world") ? spawnInfo.getString("world") : "world";
    }

    public static void setSpawnWorld(String world) {
        spawnInfo.set("world", world);
        try {
            spawnInfo.save(spawn);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
