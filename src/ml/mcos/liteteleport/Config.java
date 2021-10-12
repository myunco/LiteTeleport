package ml.mcos.liteteleport;

import org.bukkit.configuration.file.FileConfiguration;
import java.util.List;

public class Config {
    public static int spawnConsumeI;
    public static int tpSourceConsumeI;
    public static int tpAcceptConsumeI;
    public static int backConsumeI;
    public static int deathGiveExpI;
    public static double sethomeConsumeD;
    public static int sethomeMaxConsumeI;
    public static int homeConsumeI;
    public static int warpConsumeI;
    public static int firstTprConsumeI;
    public static double tprConsumeD;
    public static int tprMaxConsumeI;
    public static int tprMinRadiusI;
    public static int tprMaxRadiusI;
    public static List<String> allowTprWorld;

    public static void loadConfig(FileConfiguration config) {
        spawnConsumeI = config.getInt("spawnConsume");
        tpSourceConsumeI = config.getInt("tpSourceConsume");
        tpAcceptConsumeI = config.getInt("tpAcceptConsume");
        backConsumeI = config.getInt("backConsume");
        deathGiveExpI = config.getInt("deathGiveExp");
        sethomeConsumeD = config.getDouble("sethomeConsume");
        sethomeMaxConsumeI = config.getInt("sethomeMaxConsume");
        homeConsumeI = config.getInt("homeConsume");
        warpConsumeI = config.getInt("warpConsume");
        firstTprConsumeI = config.getInt("firstTprConsume");
        tprConsumeD = config.getDouble("tprConsume");
        tprMaxConsumeI = config.getInt("tprMaxConsume");
        tprMinRadiusI = config.getInt("tprMinRadius");
        tprMaxRadiusI = config.getInt("tprMaxRadius");
        allowTprWorld = config.getStringList("allowTprWorld");
    }
}
