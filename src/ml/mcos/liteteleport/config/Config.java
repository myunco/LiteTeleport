package ml.mcos.liteteleport.config;

import org.bukkit.configuration.file.FileConfiguration;
import java.util.List;

public class Config {
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

    public static void loadConfig(FileConfiguration config) {
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
}
