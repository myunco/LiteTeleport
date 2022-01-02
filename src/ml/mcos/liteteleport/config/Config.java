package ml.mcos.liteteleport.config;

import ml.mcos.liteteleport.LiteTeleport;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Config {
    public static LiteTeleport plugin = LiteTeleport.plugin;
    public static int spawnConsume;
    public static int tpSourceConsume;
    public static int tpAcceptConsume;
    public static int backConsume;
    public static int deathGiveExp;
    public static int firstSethomeConsume;
    public static double sethomeConsume;
    public static int sethomeMaxConsume;
    public static int homeConsume;
    public static int warpConsume;
    public static int firstTprConsume;
    public static double tprConsume;
    public static int tprMaxConsume;
    public static boolean tprCenter;
    public static int tprMinRadius;
    public static int tprMaxRadius;
    public static List<String> allowTprWorld;

    public static void loadConfig() {
        plugin.saveDefaultConfig();
        YamlConfiguration config = loadConfiguration(new File(plugin.getDataFolder(), "config.yml"));
        spawnConsume = config.getInt("spawnConsume");
        tpSourceConsume = config.getInt("tpSourceConsume");
        tpAcceptConsume = config.getInt("tpAcceptConsume");
        backConsume = config.getInt("backConsume");
        deathGiveExp = config.getInt("deathGiveExp");
        firstSethomeConsume = config.getInt("firstSethomeConsume");
        sethomeConsume = config.getDouble("sethomeConsume");
        sethomeMaxConsume = config.getInt("sethomeMaxConsume");
        homeConsume = config.getInt("homeConsume");
        warpConsume = config.getInt("warpConsume");
        firstTprConsume = config.getInt("firstTprConsume");
        tprConsume = config.getDouble("tprConsume");
        tprMaxConsume = config.getInt("tprMaxConsume");
        tprCenter = config.getBoolean("tprCenter");
        tprMinRadius = config.getInt("tprMinRadius");
        tprMaxRadius = config.getInt("tprMaxRadius");
        allowTprWorld = config.getStringList("allowTprWorld");
    }

    public static YamlConfiguration loadConfiguration(File file) {
        YamlConfiguration config = new YamlConfiguration();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    builder.append(line).append('\n');
                }
            } finally {
                reader.close();
            }
            config.loadFromString(builder.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return config;
    }

    public static void saveConfiguration(YamlConfiguration config, File file) {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            writer.write(config.saveToString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("ConstantConditions")
    public static void setLocation(YamlConfiguration config, String path, Location loc) {
        config.set(path + ".world", loc.getWorld().getName());
        config.set(path + ".x", loc.getX());
        config.set(path + ".y", loc.getY());
        config.set(path + ".z", loc.getZ());
        config.set(path + ".yaw", loc.getYaw());
        config.set(path + ".pitch", loc.getPitch());
    }

    @SuppressWarnings("ConstantConditions")
    public static Location getLocation(YamlConfiguration config, String path) {
        return new Location(plugin.getServer().getWorld(config.getString(path + ".world", "world")),
                config.getDouble(path + ".x"),
                config.getDouble(path + ".y"),
                config.getDouble(path + ".z"),
                (float) config.getDouble(path + ".yaw"),
                (float) config.getDouble(path + ".pitch"));
    }
}
