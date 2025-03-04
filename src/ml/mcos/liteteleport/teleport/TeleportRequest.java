package ml.mcos.liteteleport.teleport;

import ml.mcos.liteteleport.LiteTeleport;
import ml.mcos.liteteleport.util.Version;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

public class TeleportRequest {
    private static final Version mcVersion = LiteTeleport.mcVersion;
    private static final Server server = LiteTeleport.plugin.getServer();
    public int teleportType;
    public UUID source;
    public Location location;

    public TeleportRequest(Player source) {
        this(0, source, null);
    }

    /**
     * 创建一个传送请求对象。
     * @param teleportType 0=发起者传送到目标者处 1=目标者传送到发起者处
     * @param source 请求发起者。
     * @param location 请求发起者发起请求时的位置。仅当teleportType=1时需要。
     */
    public TeleportRequest(int teleportType, Player source, Location location) {
        this.teleportType = teleportType;
        this.source = source.getUniqueId(); // 保存请求发起者的UUID 因为Player对象在某些情况下会改变
        if (teleportType == 1) {
            this.location = location;
        }
    }

    public Player getSource() {
        return mcVersion.isLessThan(7, 5) ? getPlayerByUUID(source) : server.getPlayer(source); // 1.7.5之前的版本没有 Server#getPlayer(UUID) 方法
    }

    private static Player getPlayerByUUID(UUID uuid) {
        for (Player player : getOnlinePlayers()) {
            if (player.getUniqueId().equals(uuid)) {
                return player;
            }
        }
        return null; // 如果没有找到匹配的玩家，返回null
    }

    private static Method getOnlinePlayers;
    private static Collection<? extends Player> getOnlinePlayers() {
        if (mcVersion.isGreaterThanOrEqualTo(7, 10)) {
            return server.getOnlinePlayers();
        }
        // 1.7.10之前返回值是数组
        try {
            if (getOnlinePlayers == null) {
                getOnlinePlayers = Class.forName("org.bukkit.Server").getMethod("getOnlinePlayers");
            }
            return Arrays.asList((Player[]) getOnlinePlayers.invoke(server));
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

}
