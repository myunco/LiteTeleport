package ml.mcos.liteteleport.config;

import ml.mcos.liteteleport.LiteTeleport;
import ml.mcos.liteteleport.consume.ConsumeInfo;
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
    public static String language;
    public static boolean checkUpdate;
    public static int tpCooldown;
    public static ConsumeInfo spawnConsume;
    public static ConsumeInfo tpSourceConsume;
    public static ConsumeInfo tpAcceptConsume;
    public static ConsumeInfo backConsume;
    public static int deathGiveExp;
    public static ConsumeInfo firstSethomeConsume;
    public static double sethomeConsume;
    public static ConsumeInfo.ConsumeType sethomeConsumeType;
    public static int sethomeMaxConsume;
    public static int maxOfHomes;
    public static ConsumeInfo homeConsume;
    public static ConsumeInfo warpConsume;
    public static ConsumeInfo firstTprConsume;
    public static double tprConsume;
    public static ConsumeInfo.ConsumeType tprConsumeType;
    public static int tprMaxConsume;
    public static boolean tprCenter;
    public static int tprMinRadius;
    public static int tprMaxRadius;
    public static List<String> allowTprWorld;

    public static boolean useEconomy;
    public static boolean usePoints;

    public static void loadConfig() {
        plugin.saveDefaultConfig();
        YamlConfiguration config = loadConfiguration(new File(plugin.getDataFolder(), "config.yml"));
        language = config.getString("language", "zh_cn");
        assert language != null; //免得IDEA警告language可能为null
        Language.loadLanguage(language);
        checkUpdate = config.getBoolean("checkUpdate", true);
        tpCooldown = config.getInt("tpCooldown");
        spawnConsume = getConsumeInfo(config, "spawnConsume");
        tpSourceConsume = getConsumeInfo(config, "tpSourceConsume");
        tpAcceptConsume = getConsumeInfo(config, "tpAcceptConsume");
        backConsume = getConsumeInfo(config, "backConsume");
        deathGiveExp = config.getInt("deathGiveExp");
        firstSethomeConsume = getConsumeInfo(config, "firstSethomeConsume");
        sethomeConsume = getSethomeConsume(config);
        sethomeMaxConsume = config.getInt("sethomeMaxConsume");
        maxOfHomes = config.getInt("maxOfHomes");
        homeConsume = getConsumeInfo(config, "homeConsume");
        warpConsume = getConsumeInfo(config, "warpConsume");
        firstTprConsume = getConsumeInfo(config, "firstTprConsume");
        tprConsume = getTprConsume(config);
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

    private static ConsumeInfo getConsumeInfo(YamlConfiguration config, String path) {
        String consume = config.getString(path);
        ConsumeInfo.ConsumeType type = ConsumeInfo.ConsumeType.LEVEL;
        int amount = 0;
        if (consume != null) {
            if (!consume.matches("[0-9]+[GgPpLl]?")) {
                plugin.sendMessage(Language.consumeInvalid + path + ": " + consume);
            } else {
                type = ConsumeInfo.getConsumeType(consume);
                amount = ConsumeInfo.getConsumeAmount(consume);
                checkConsumeType(type);
            }
        }
        return new ConsumeInfo(type, amount);
    }

    private static double getConsumeDouble(String consume, String path) {
        if (consume == null) {
            return 0.0;
        } else {
            if (consume.indexOf('.') == -1) {
                int index = 0;
                for (; index < consume.length(); index++) {
                    char c = consume.charAt(index);
                    if (c > '9' || c < '0') {
                        break;
                    }
                }
                if (index == consume.length()) {
                    consume = consume + ".0";
                } else {
                    consume = consume.replace(consume.substring(index), ".0" + consume.substring(index));
                }
            }
            if (!consume.matches("[0-9]+\\.[0-9]+[GgPpLl]?")) {
                plugin.sendMessage(Language.consumeInvalid + path + ": " + consume);
            }
            return ConsumeInfo.getConsumeAmountDouble(consume);
        }
    }

    private static double getSethomeConsume(YamlConfiguration config) {
        String consume = config.getString("sethomeConsume");
        sethomeConsumeType = ConsumeInfo.getConsumeType(consume);
        checkConsumeType(sethomeConsumeType);
        return getConsumeDouble(consume, "sethomeConsume");
    }

    private static double getTprConsume(YamlConfiguration config) {
        String consume = config.getString("tprConsume");
        tprConsumeType = ConsumeInfo.getConsumeType(consume);
        checkConsumeType(tprConsumeType);
        return getConsumeDouble(consume, "tprConsume");
    }

    private static void checkConsumeType(ConsumeInfo.ConsumeType type) {
        if (!useEconomy && type == ConsumeInfo.ConsumeType.ECONOMY) {
            useEconomy = true;
        } else if (!usePoints && type == ConsumeInfo.ConsumeType.POINTS) {
            usePoints = true;
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
