package ml.mcos.liteteleport.update;

import ml.mcos.liteteleport.LiteTeleport;
import ml.mcos.liteteleport.config.Language;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.UUID;

public class UpdateNotification implements Listener {
    private int day = LocalDate.now().getDayOfMonth();
    private final ArrayList<UUID> notifiedPlayers = new ArrayList<>();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (UpdateChecker.isUpdateAvailable && event.getPlayer().hasPermission("LiteTeleport.admin")) {
            if (day != LocalDate.now().getDayOfMonth()) {
                day = LocalDate.now().getDayOfMonth();
                if (!notifiedPlayers.isEmpty()) {
                    notifiedPlayers.clear();
                }
            }
            if (!notifiedPlayers.contains(event.getPlayer().getUniqueId())) {
                notifiedPlayers.add(event.getPlayer().getUniqueId());
                LiteTeleport.plugin.getScheduler().runTaskLaterAsynchronously(() -> {
                    event.getPlayer().sendMessage(Language.messagePrefix + UpdateChecker.newVersion);
                    event.getPlayer().sendMessage(Language.messagePrefix + UpdateChecker.downloadLink);
                }, 60);
            }
        }
    }

}
