package ml.mcos.liteteleport.config;

import ml.mcos.liteteleport.LiteTeleport;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Language {
    public static LiteTeleport plugin = LiteTeleport.plugin;
    private static int version;
    public static String logPrefix;
    public static String messagePrefix;
    public static String languageVersionError;
    public static String languageUpdate;
    public static String languageUpdateComplete;
    public static String economyNotFoundVault;
    public static String economyNotFoundEconomy;
    public static String pointsNotFound;
    public static String consumeInvalid;
    public static String updateFoundNewVersion;
    public static String updateMajorUpdate;
    public static String updateDownloadLink;
    public static String updateCheckFailure;
    public static String updateCheckException;
    public static String homeInfoCreateFailure;
    public static String homeListEmpty;
    public static String homeNameInvalid;
    public static String homeList;
    public static String spawnInfoCreateFailure;
    public static String tprInfoCreateFailure;
    public static String warpInfoCreateFailure;
    public static String warpListEmpty;
    public static String warpList;
    public static String consumeInfoDescriptionLevel;
    public static String consumeInfoDescriptionEconomy;
    public static String consumeInfoDescriptionPoints;
    public static String consumeInfoNameLevel;
    public static String consumeInfoNameEconomy;
    public static String consumeInfoNamePoints;
    public static String teleportConsume;
    public static String teleportConsumeNotEnough;
    public static String teleportCooldown;
    public static String commandVersion;
    public static String commandReload;
    public static String commandBackNotBack;
    public static String commandBack;
    public static String commandDelhomeUsage;
    public static String commandDelhomeDontExist;
    public static String commandDelhome;
    public static String commandDelwarpUsage;
    public static String warpNameInvalid;
    public static String warpDontExist;
    public static String commandDelwarp;
    public static String commandHomeUsage;
    public static String commandHome;
    public static String commandSethomeUsage;
    public static String commandSethomeAlreadyExists;
    public static String commandSethomeConsume;
    public static String commandSethomeConsumeNotEnough;
    public static String commandSethome;
    public static String commandSethomeMax;
    public static String commandSetspawn;
    public static String commandSetwarpUsage;
    public static String commandSetwarp;
    public static String teleport;
    public static String commandTpaUsage;
    public static String playerNotFound;
    public static String commandTpaTarget;
    public static String teleportRequestTargetAccept;
    public static String teleportRequestTargetDeny;
    public static String teleportRequestTargetConsume;
    public static String teleportRequestSource;
    public static String teleportRequestSourceCancel;
    public static String teleportRequestSourceConsume;
    public static String teleportRequestDontExist;
    public static String commandTpacancel;
    public static String commandTpacceptTargetConsumeNotEnough;
    public static String commandTpacceptTargetConsumeNotEnoughSource;
    public static String commandTpacceptSourceConsumeNotEnough;
    public static String commandTpacceptSourceConsumeNotEnoughSource;
    public static String commandTpacceptSource;
    public static String commandTpaccept;
    public static String commandTpacceptTeleport;
    public static String commandTpahereUsage;
    public static String commandTpahereTarget;
    public static String commandTpdenySource;
    public static String commandTpdeny;
    public static String commandTprWorldNotAllow;
    public static String commandTprConsume;
    public static String commandTprConsumeNotEnough;
    public static String commandTprTitle;
    public static String commandTprSubtitle;
    public static String commandTprTheEnd;
    public static String commandTprNotFoundSafeLocation;
    public static String commandWarpUsage;
    public static String commandWarp;
    public static String playerDeathMessage;

    public static void loadLanguage(String language) {
        if (!language.matches("[a-zA-Z]{2}[_-][a-zA-Z]{2}")) {
            plugin.sendMessage("§4语言文件名称格式错误: " + language);
            language = "zh_cn";
        }
        String langPath = "lang/" + language + ".yml";
        File lang = new File(plugin.getDataFolder(), langPath);
        if (!lang.exists()) {
            if (plugin.classLoader.getResource(langPath) == null) {
                InputStream in = plugin.getResource("lang/zh_cn.yml");
                if (in != null) {
                    try {
                        OutputStream out = new FileOutputStream(lang);
                        byte[] buf = new byte[1024];
                        int len;
                        while ((len = in.read(buf)) != -1) {
                            out.write(buf, 0, len);
                        }
                        out.close();
                        in.close();
                        plugin.sendMessage("§a语言文件: " + language + ".yml 不存在, 已自动创建。");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    plugin.sendMessage("§4语言文件: " + language + ".yml 不存在, 并且在插件内找不到默认语言文件: zh_cn.yml");
                }
            } else {
                plugin.saveResource(langPath, false);
            }
        }
        YamlConfiguration config = Config.loadConfiguration(lang);
        version = config.getInt("version");
        logPrefix = config.getString("log-prefix", "[LiteTeleport] ");
        messagePrefix = config.getString("message-prefix", "§3[§6LiteTeleport§3] ");
        languageVersionError = config.getString("language-version-error", "语言文件版本错误: ");
        languageUpdate = config.getString("language-update", "§e当前语言文件版本：§a{0} §c最新版本：§b{1} §6需要更新.");
        languageUpdateComplete = config.getString("language-update-complete", "§a语言文件更新完成!");
        languageUpdate(config, lang);
        economyNotFoundVault = config.getString("economy-not-found-vault", "未找到Vault，请检查是否正确安装Vault插件！");
        economyNotFoundEconomy = config.getString("economy-not-found-economy", "未找到经济系统，请检查是否正确安装经济提供插件！(如Essentials、CMI、Economy等)");
        pointsNotFound = config.getString("points-not-found", "未找到PlayerPoints，请检查是否正确安装点券插件！");
        consumeInvalid = config.getString("consume-invalid", "无效的花费，请检查格式是否正确: ");
        updateFoundNewVersion = config.getString("update-found-new-version", "§c发现新版本可用! §b当前版本: {0} §d最新版本: {1}");
        updateMajorUpdate = config.getString("update-major-update", "§e(有大更新)");
        updateDownloadLink = config.getString("update-download-link", "§a下载地址: ");
        updateCheckFailure = config.getString("update-check-failure", "§e检查更新失败, 状态码: ");
        updateCheckException = config.getString("update-check-exception", "§4检查更新时发生IO异常.");
        homeInfoCreateFailure = config.getString("home-info-create-failure", "错误: 创建homes.yml失败！");
        homeListEmpty = config.getString("home-list-empty", "§6你还没有设置过家。");
        homeList = config.getString("home-list", "§6家：§f");
        spawnInfoCreateFailure = config.getString("spawn-info-create-failure", "错误: 创建spawn.yml失败！");
        tprInfoCreateFailure = config.getString("tpr-info-create-failure", "错误: 创建tpr.yml失败！");
        warpInfoCreateFailure = config.getString("warp-info-create-failure", "错误: 创建warps.yml失败！");
        warpListEmpty = config.getString("warp-list-empty", "§6没有已定义的传送点。");
        warpList = config.getString("warp-list", "§6传送点：§f");
        consumeInfoDescriptionLevel = config.getString("consume-info-description-level", "§a{0}§c级经验。");
        consumeInfoDescriptionEconomy = config.getString("consume-info-description-economy", "§a{0}§c金币。");
        consumeInfoDescriptionPoints = config.getString("consume-info-description-points", "§a{0}§c点券。");
        consumeInfoNameLevel = config.getString("consume-info-name-level", "等级");
        consumeInfoNameEconomy = config.getString("consume-info-name-economy", "金币");
        consumeInfoNamePoints = config.getString("consume-info-name-points", "点券");
        teleportConsume = config.getString("teleport-consume", "§c本次传送将花费{0}");
        teleportConsumeNotEnough = config.getString("teleport-consume-not-enough", "§c错误: §4你没有足够的{0}支付本次传送花费。");
        teleportCooldown = config.getString("teleport-cooldown", "§4传送冷却: §c{0}§4秒。");
        commandVersion = config.getString("command-version", "§a当前版本: §b");
        commandReload = config.getString("command-reload", "§a配置文件重载完成。");
        commandBackNotBack = config.getString("command-back-not-back", "§c错误: §6没有上一位置可以回去。");
        commandBack = config.getString("command-back", "§6正在回到上一位置...");
        commandDelhomeUsage = config.getString("command-delhome-usage", "§c错误: 请使用§6/delhome <家名称>");
        homeNameInvalid = config.getString("home-name-invalid", "§c错误: §4无效的家名称!");
        commandDelhomeDontExist = config.getString("command-delhome-dont-exist", "§c错误: §4家§c{0}§4不存在!");
        commandDelhome = config.getString("command-delhome", "§6家§c{0}§6已被移除。");
        commandDelwarpUsage = config.getString("command-delwarp-usage", "§c错误: 请使用§6/delwarp <传送点名称>");
        warpNameInvalid = config.getString("warp-name-invalid", "§c错误: §4无效的传送点名称!");
        warpDontExist = config.getString("warp-dont-exist", "§c错误: §4该传送点不存在。");
        commandDelwarp = config.getString("command-delwarp", "§6传送点§c{0}§6已被移除。");
        commandHomeUsage = config.getString("command-home-usage", "§c错误: 请使用§6/home [家名称]");
        commandHome = config.getString("command-home", "§6传送到§c{0}§6。");
        commandSethomeUsage = config.getString("command-sethome-usage", "§c错误: 请使用§6/sethome [家名称]");
        commandSethomeAlreadyExists = config.getString("command-sethome-already-exists", "§6家§c{0}§6已存在，将被重设为当前位置。");
        commandSethomeConsume = config.getString("command-sethome-consume", "§c设置你的第§9{0}§c个家将花费{1}");
        commandSethomeConsumeNotEnough = config.getString("command-sethome-consume-not-enough", "§c错误: §4你没有足够的{0}支付本次设置花费。");
        commandSethome = config.getString("command-sethome", "§6已设置家。");
        commandSethomeMax = config.getString("command-sethome-max", "§c你的家数量已达上限。");
        commandSetspawn = config.getString("command-setspawn", "§6已将世界出生点设为当前位置。");
        commandSetwarpUsage = config.getString("command-setwarp-usage", "§c错误: 请使用§6/setwarp <传送点名称>");
        commandSetwarp = config.getString("command-setwarp", "§6已设置传送点§c{0}§6。");
        teleport = config.getString("teleport", "§6正在传送...");
        commandTpaUsage = config.getString("command-tpa-usage", "§c错误: 请使用§6/tpa <玩家>");
        playerNotFound = config.getString("player-not-found", "§c错误: §4未找到该玩家。");
        commandTpaTarget = config.getString("command-tpa-target", "§c{0}§6请求传送到你这里。");
        teleportRequestTargetAccept = config.getString("teleport-request-target-accept", "§6若想接受传送，输入§c/tpaccept");
        teleportRequestTargetDeny = config.getString("teleport-request-target-deny", "§6若想拒绝传送，输入§c/tpdeny");
        teleportRequestTargetConsume = config.getString("teleport-request-target-consume", "§c接受此请求将花费{0}");
        teleportRequestSource = config.getString("teleport-request-source", "§6请求已发送给§c{0}§6。");
        teleportRequestSourceCancel = config.getString("teleport-request-source-cancel", "§6若要取消这个请求，请输入§c/tpacancel");
        teleportRequestSourceConsume = config.getString("teleport-request-source-consume", "§c传送时将花费{0}");
        teleportRequestDontExist = config.getString("teleport-request-dont-exist", "§c错误: §4你没有待处理的请求。");
        commandTpacancel = config.getString("command-tpacancel", "§6传送请求已被取消。");
        commandTpacceptTargetConsumeNotEnough = config.getString("command-tpaccept-target-consume-not-enough", "§c错误: §4你没有足够的{0}来接受此请求。");
        commandTpacceptTargetConsumeNotEnoughSource = config.getString("command-tpaccept-target-consume-not-enough-source", "§c错误: §4对方没有足够的{0}来接受此请求。");
        commandTpacceptSourceConsumeNotEnough = config.getString("command-tpaccept-source-consume-not-enough", "§c错误: §4对方没有足够的{0}支付本次传送花费。");
        commandTpacceptSourceConsumeNotEnoughSource = config.getString("command-tpaccept-source-consume-not-enough-source", "§c错误: §4你没有足够的{0}支付本次传送花费。");
        commandTpacceptSource = config.getString("command-tpaccept-source", "§c{0}§6接受了你的传送请求。");
        commandTpaccept = config.getString("command-tpaccept", "§6已接受传送请求。");
        commandTpacceptTeleport = config.getString("command-tpaccept-teleport", "§6正在传送至§c{0}§6。");
        commandTpahereUsage = config.getString("command-tpahere-usage", "§c错误: 请使用§6/tpahere <玩家>");
        commandTpahereTarget = config.getString("command-tpahere-target", "§c{0}§6请求你传送到他那里。");
        commandTpdenySource = config.getString("command-tpdeny-source", "§c{0}§6拒绝了你的传送请求。");
        commandTpdeny = config.getString("command-tpdeny", "§6已拒绝传送请求。");
        commandTprWorldNotAllow = config.getString("command-tpr-world-not-allow", "§c错误: §4你所在的世界不允许使用随机传送。");
        commandTprConsume = config.getString("command-tpr-consume", "§c第§3{0}§c次随机传送将花费{1}");
        commandTprConsumeNotEnough = config.getString("command-tpr-consume-not-enough", "§c错误: §4你没有足够的{0}支付本次随机传送花费。");
        commandTprTitle = config.getString("command-tpr-title", "§a随机传送");
        commandTprSubtitle = config.getString("command-tpr-subtitle", "§b传送将在10秒内开始···");
        commandTprTheEnd = config.getString("command-tpr-the-end", "§3寻找安全位置可能需要花费一些时间，请耐心等待。");
        commandTprNotFoundSafeLocation = config.getString("command-tpr-not-found-safe-location", "§c错误: §4未找到安全位置，请重试。");
        commandWarpUsage = config.getString("command-warp-usage", "§c错误: 请使用§6/warp <传送点名称>");
        commandWarp = config.getString("command-warp", "§6传送到§c{0}§6。");
        playerDeathMessage = config.getString("player-death-message", "§6使用§c/back§6命令回到死亡地点。");
    }

    public static void languageUpdate(YamlConfiguration config, File lang) {
        int currentVersion = 2;
        if (version < currentVersion) {
            plugin.sendMessage(replaceArgs(languageUpdate, version, currentVersion));
            switch (version) {
                case 1:
                    config.set("command-sethome-max", "§c你的家数量已达上限。");
                    break; //最后一个case再break，以便跨版本升级。
                default:
                    plugin.getLogger().warning(Language.languageVersionError + Language.version);
                    return;
            }
            plugin.sendMessage(languageUpdateComplete);
            config.set("version", currentVersion);
            Config.saveConfiguration(config, lang);
        }
    }

    public static String replaceArgs(String msg, Object... args) {
        for (int i = 0; i < args.length; i++) {
            //msg = msg.replace("{" + i + "}", args[i]);
            msg = msg.replace("{0}".replace('0', (char) (i + 0x30)), args[i].toString());
        }
        return msg;
    }

}
