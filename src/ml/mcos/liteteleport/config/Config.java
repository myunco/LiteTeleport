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
    public static int tpDelay;
    public static ConsumeInfo spawnConsume;
    public static ConsumeInfo tpSourceConsume;
    public static ConsumeInfo tpAcceptConsume;
    public static ConsumeInfo backConsume;
    public static int deathGiveExp;
    public static ConsumeInfo firstSethomeConsume;
    public static double sethomeConsume;
    public static ConsumeInfo.ConsumeType sethomeConsumeType;
    public static int sethomeMaxConsume;
    public static int sethomeMax;
    public static ConsumeInfo homeConsume;
    public static ConsumeInfo warpConsume;
    public static ConsumeInfo firstTprConsume;
    public static double tprConsume;
    public static ConsumeInfo.ConsumeType tprConsumeType;
    public static int tprMaxConsume;
    public static boolean tprCenter;
    public static int tprMinRadius;
    public static int tprMaxRadius;
    public static boolean tprMode;
    public static boolean tprAllowWater;
    public static int tprWaterBreathing;
    public static List<String> allowTprWorld;
    public static boolean useEconomy;
    public static boolean usePoints;

    public static void loadConfig() {
        plugin.saveDefaultConfig();
        YamlConfiguration config = updateConfiguration();
        language = config.getString("language", "zh_cn");
        assert language != null; //免得IDEA警告language可能为null
        Language.loadLanguage(language);
        checkUpdate = config.getBoolean("checkUpdate", true);
        tpCooldown = config.getInt("tpCooldown");
        tpDelay = config.getInt("tpDelay");
        spawnConsume = getConsumeInfo(config, "spawnConsume");
        tpSourceConsume = getConsumeInfo(config, "tpSourceConsume");
        tpAcceptConsume = getConsumeInfo(config, "tpAcceptConsume");
        backConsume = getConsumeInfo(config, "backConsume");
        deathGiveExp = config.getInt("deathGiveExp");
        firstSethomeConsume = getConsumeInfo(config, "firstSethomeConsume");
        sethomeConsume = getSethomeConsume(config);
        sethomeMaxConsume = config.getInt("sethomeMaxConsume");
        sethomeMax = config.getInt("sethomeMax");
        homeConsume = getConsumeInfo(config, "homeConsume");
        warpConsume = getConsumeInfo(config, "warpConsume");
        firstTprConsume = getConsumeInfo(config, "firstTprConsume");
        tprConsume = getTprConsume(config);
        tprMaxConsume = config.getInt("tprMaxConsume");
        tprCenter = config.getBoolean("tprCenter");
        tprMinRadius = config.getInt("tprMinRadius");
        if (tprMinRadius < 0) {
            tprMinRadius = 0;
        }
        tprMaxRadius = config.getInt("tprMaxRadius");
        if (tprMaxRadius <= tprMinRadius) {
            tprMaxRadius = tprMinRadius + 1;
        }
        tprMode = config.getBoolean("tprMode", true);
        tprAllowWater = config.getBoolean("tprAllowWater");
        tprWaterBreathing = config.getInt("tprWaterBreathing");
        allowTprWorld = config.getStringList("allowTprWorld");
        allowTprWorld.replaceAll(String::toLowerCase);
    }

    public static YamlConfiguration loadConfiguration(File file) {
        YamlConfiguration config = new YamlConfiguration();
        try {
            //noinspection IOStreamConstructor
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

    public static YamlConfiguration updateConfiguration() {
        File file = new File(plugin.getDataFolder(), "config.yml");
        YamlConfiguration config = loadConfiguration(file);
        if (!config.contains("tpDelay")) { //没有1.10.0版本新加的配置 需要升级
            //更新config会导致注释丢失 为避免这种情况 使用流来追加新内容
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8)) {
                writer.write("\r\n#传送延时 在延时期间移动将取消传送 单位：秒\r\n" +
                        "tpDelay: 0\r\n" +
                        "\r\n#设置家的数量上限 0表示无上限\r\n" +
                        "sethomeMax: 0\r\n");
                config = loadConfiguration(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!config.contains("tprMode")) { //没有1.11.0版本新加的配置 需要升级
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8)) {
                writer.write("\r\n#随机传送模式 true=矩形范围内随机传送 false=圆形范围内随机传送\r\n" +
                        "tprMode: true\r\n" +
                        "\r\n#允许随机传送到水面 true表示允许 false表示不允许 (如果世界中海洋非常多建议设置为true 否则可能导致多次加载随机区块寻找位置影响性能)\r\n" +
                        "tprAllowWater: false\r\n" +
                        "\r\n#如果允许传送到水面 在传送到水面时是否给予玩家水下呼吸效果(防止区块加载卡顿导致玩家淹死) 0表示不给予 10表示给予10秒 60表示给予60秒 以此类推\r\n" +
                        "tprWaterBreathing: 60\r\n");
                config = loadConfiguration(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return config;
    }

    public static void saveConfiguration(YamlConfiguration config, File file) {
        //noinspection IOStreamConstructor
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

    public static void setLocation(YamlConfiguration config, String path, Location loc) {
        //noinspection DataFlowIssue
        config.set(path + ".world", loc.getWorld().getName());
        config.set(path + ".x", loc.getX());
        config.set(path + ".y", loc.getY());
        config.set(path + ".z", loc.getZ());
        config.set(path + ".yaw", loc.getYaw());
        config.set(path + ".pitch", loc.getPitch());
    }

    public static void setLocation(YamlConfiguration config, String path, String world, double x, double y, double z, double yaw, double pitch) {
        config.set(path + ".world", world);
        config.set(path + ".x", x);
        config.set(path + ".y", y);
        config.set(path + ".z", z);
        config.set(path + ".yaw", yaw);
        config.set(path + ".pitch", pitch);
    }

    public static Location getLocation(YamlConfiguration config, String path) {
        //noinspection DataFlowIssue
        return new Location(plugin.getServer().getWorld(config.getString(path + ".world", "world")),
                config.getDouble(path + ".x"),
                config.getDouble(path + ".y"),
                config.getDouble(path + ".z"),
                (float) config.getDouble(path + ".yaw"),
                (float) config.getDouble(path + ".pitch"));
    }
}
