package ml.mcos.liteteleport.teleport;

import ml.mcos.liteteleport.LiteTeleport;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TeleportRequest {
    public static Server server = LiteTeleport.plugin.getServer();
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
        this.source = source.getUniqueId();
        if (teleportType == 1) {
            this.location = location;
        }
    }

    public Player getSource() {
        return server.getPlayer(source);
    }
}
