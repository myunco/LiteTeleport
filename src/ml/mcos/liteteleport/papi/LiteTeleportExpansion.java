package ml.mcos.liteteleport.papi;

import joptsimple.internal.Strings;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import ml.mcos.liteteleport.LiteTeleport;
import ml.mcos.liteteleport.config.*;
import ml.mcos.liteteleport.consume.ConsumeInfo;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;

import static ml.mcos.liteteleport.config.WarpInfo.warpInfo;

public class LiteTeleportExpansion extends PlaceholderExpansion {
    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        switch (params) {
            case "homes": //家列表
                return player == null ? null : homeList(player.getName());
            case "warps": //传送点列表
                return warpList();
            case "home_amount": //家数量
                return homeAmount(player.getName());
            case "warp_amount": //传送点数量
                return String.valueOf(warpInfo.getKeys(false).size());
            case "tpr_count": //已经随机传送的次数
                return player == null ? null : String.valueOf(TprInfo.getTprCount(player.getName()));
            case "next_home": //获取设置下一个家需要的花费
                return nextHomeCost(player.getName());
            case "next_tpr" : //获取下一次随机传送需要的花费
                return nextTprCost(player.getName());
            default:
                return null;
        }
    }

    public static String homeList(String player) {
        List<String> homeList = HomeInfo.getHomeList(player);
        if (homeList == null) {
            return "无";
        }
        return Strings.join(homeList, ", ");
    }

    public static String warpList() {
        Set<String> keys = warpInfo.getKeys(false);
        if (keys.isEmpty()) {
            return "无";
        }
        return Strings.join(keys, ", ");
    }

    public static String homeAmount(String player) {
        List<String> homeList = HomeInfo.getHomeList(player);
        return homeList == null ? "0" : String.valueOf(homeList.size());
    }

    public static String nextHomeCost(String player) {
        List<String> homeList = HomeInfo.getHomeList(player);
        ConsumeInfo consume;
        if (homeList == null) {
            consume = Config.firstSethomeConsume;
        } else {
            int amount = (int) Math.pow(homeList.size() + 1, Config.sethomeConsume);
            if (Config.sethomeMaxConsume > 0 && amount > Config.sethomeMaxConsume) {
                amount = Config.sethomeMaxConsume;
            }
            consume = new ConsumeInfo(Config.sethomeConsumeType, amount);
        }
        return consume.getDescription();
    }

    public static String nextTprCost(String player) {
        ConsumeInfo consume;
        int count = TprInfo.getTprCount(player);
        if (count == 0) {
            consume = Config.firstTprConsume;
        } else {
            int amount = (int) Math.pow(count + 1, Config.tprConsume);
            if (Config.tprMaxConsume > 0 && amount > Config.tprMaxConsume) {
                amount = Config.tprMaxConsume;
            }
            consume = new ConsumeInfo(Config.tprConsumeType, amount);
        }
        return consume.getDescription();
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        return onRequest(player, params);
    }

    @Override
    public String getIdentifier() {
        return "LiteTeleport";
    }

    @Override
    public String getAuthor() {
        return "myunco";
    }

    @Override
    public String getVersion() {
        return LiteTeleport.plugin.getDescription().getVersion();
    }
}
