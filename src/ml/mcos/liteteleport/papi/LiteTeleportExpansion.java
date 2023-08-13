package ml.mcos.liteteleport.papi;

import joptsimple.internal.Strings;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import ml.mcos.liteteleport.config.*;
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
            case "homes":
                return player == null ? null : homeList(player.getName());
            case "warps":
                return warpList();
            case "tpr_count":
                return player == null ? null : String.valueOf(TprInfo.getTprCount(player.getName()));
            case "spawn_world":
                return SpawnInfo.getSpawnWorld();
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
        return "1.10.0";
    }
}
