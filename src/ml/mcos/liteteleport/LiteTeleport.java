package ml.mcos.liteteleport;

import ml.mcos.liteteleport.config.Config;
import ml.mcos.liteteleport.config.HomeInfo;
import ml.mcos.liteteleport.config.Language;
import ml.mcos.liteteleport.config.SpawnInfo;
import ml.mcos.liteteleport.config.TprInfo;
import ml.mcos.liteteleport.config.WarpInfo;
import ml.mcos.liteteleport.consume.ConsumeInfo;
import ml.mcos.liteteleport.metrics.Metrics;
import ml.mcos.liteteleport.teleport.RandomTeleport;
import ml.mcos.liteteleport.teleport.TeleportRequest;
import ml.mcos.liteteleport.update.UpdateChecker;
import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LiteTeleport extends JavaPlugin implements Listener {
    public static LiteTeleport plugin;
    public ConsoleCommandSender consoleSender = getServer().getConsoleSender();
    private final HashMap<UUID, TeleportRequest> tpList = new HashMap<>();
    private final HashMap<UUID, Location> backList = new HashMap<>();
    private final HashMap<UUID, Long> cdList = new HashMap<>();
    public ClassLoader classLoader = getClassLoader();
    public static int mcVersion;
    public static int mcVersionPatch;
    public static Economy economy;
    public static PlayerPointsAPI pointsAPI;

    @Override
    public void onEnable() {
        plugin = this;
        mcVersion = getMinecraftVersion();
        initConfig();
        getServer().getPluginManager().registerEvents(this, this);
        new Metrics(this, 12936);
    }

    @Override
    public void onDisable() {
        UpdateChecker.stop();
    }

    public void initConfig() {
        Config.loadConfig();
        if (Config.checkUpdate) {
            UpdateChecker.start();
        }
        if (Config.useEconomy && economy == null) {
            setupEconomy();
        }
        if (Config.usePoints && pointsAPI == null) {
            setupPointsAPI();
        }
        HomeInfo.loadHomeInfo();
        SpawnInfo.loadSpawnInfo();
        TprInfo.loadTprInfo();
        WarpInfo.loadWarpInfo();
    }

    public void setupEconomy() {
        if (!getServer().getPluginManager().isPluginEnabled("Vault")) {
            sendMessage(Language.economyNotFoundVault);
            return;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            sendMessage(Language.economyNotFoundEconomy);
            return;
        }
        economy = rsp.getProvider();
        sendMessage("Using economy system: §3" + economy.getName());
    }

    public void setupPointsAPI() {
        PlayerPoints playerPoints = (PlayerPoints) getServer().getPluginManager().getPlugin("PlayerPoints");
        if (playerPoints == null || !playerPoints.isEnabled()) {
            sendMessage(Language.pointsNotFound);
            return;
        }
        pointsAPI = playerPoints.getAPI();
        sendMessage("Found PlayerPoints: §3v" + playerPoints.getDescription().getVersion());
    }

    private int getMinecraftVersion() {
        String[] version = getServer().getBukkitVersion().replace('-', '.').split("\\.");
        try {
            mcVersionPatch = Integer.parseInt(version[2]);
        } catch (NumberFormatException ignored) {
        }
        return Integer.parseInt(version[1]);
    }

    public void sendMessage(String msg) {
        consoleSender.sendMessage(Language.logPrefix + msg);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCasa("LiteTeleport")) {
            if (args.length == 1) {
                switch (args[0].toLowerCase()) {
                    case "version":
                        sender.sendMessage(Language.messagePrefix + Language.commandVersion + getDescription().getVersion());
                        return true;
                    case "reload":
                        UpdateChecker.stop();
                        initConfig();
                        sender.sendMessage(Language.messagePrefix + Language.commandReload);
                        return true;
                }
            }
            return false;
        }
        if (sender instanceof Player) {
            Player player = (Player) sender;
            switch (command.getName().toLowerCase()) {
                case "home":
                    commandHome(args, player);
                    break;
                case "back":
                    commandBack(player);
                    break;
                case "tpa":
                    commandTpa(args, player);
                    break;
                case "tpaccept":
                    commandTpaccept(player);
                    break;
                case "sethome":
                    commandSethome(args, player);
                    break;
                case "spawn":
                    commandSpawn(player);
                    break;
                case "tpahere":
                    commandTpahere(args, player);
                    break;
                case "tpdeny":
                    commandTpdeny(player);
                    break;
                case "delhome":
                    commandDelhome(args, player);
                    break;
                case "tpacancel":
                    commandTpacancel(player);
                    break;
                case "tpr":
                    commandTpr(player);
                    break;
                case "warp":
                    commandWarp(args, player);
                    break;
                case "setspawn":
                    commandSetspawn(player);
                    break;
                case "setwarp":
                    commandSetwarp(args, player);
                    break;
                case "delwarp":
                    commandDelwarp(args, player);
                    break;
            }
        } else {
            sender.sendMessage("§c错误: §6本命令只能玩家使用。");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            switch (command.getName()) {
                case "home":
                case "delhome":
                    if (args.length == 1) {
                        return TabComplete.getCompleteList(args, HomeInfo.getHomeList(player.getName()), true);
                    }
                    break;
                case "warp":
                case "delwarp":
                    if (args.length == 1) {
                        return TabComplete.getCompleteList(args, WarpInfo.getWarpList(), true);
                    }
            }
        }
        return TabComplete.getCompleteList(args, TabComplete.getTabList(args, command.getName()));
    }

    private boolean consume(Player player, ConsumeInfo consume, String msg, String failMsg) {
        if (consume.getAmount() != 0) {
            player.sendMessage(msg);
            if (consume.has(player) && consume.take(player)) {
                return true;
            } else {
                player.sendMessage(failMsg);
                return false;
            }
        }
        return true;
    }

    private boolean teleportConsume(Player player, ConsumeInfo consume) {
        if (hasFreePermission(player)) {
            return true;
        }
        return consume(player, consume, Language.replaceArgs(Language.teleportConsume, consume.getDescription()), Language.replaceArgs(Language.teleportConsumeNotEnough, consume.getConsumeName()));
    }

    private boolean hasFreePermission(Player player) {
        return player.hasPermission("LiteTeleport.free");
    }

    private boolean hasFreeTprPermission(Player player) {
        return player.hasPermission("LiteTeleport.free.tpr");
    }

    private boolean hasFreeSethomePermission(Player player) {
        return player.hasPermission("LiteTeleport.free.sethome");
    }
    
    private void teleport(Player pLayer, Location location) {
        if (Config.tpCooldown > 0) {
            cdList.put(pLayer.getUniqueId(), System.currentTimeMillis());
        }
        pLayer.teleport(location);
    }

    private int getCooldown(Player player) {
        if (Config.tpCooldown > 0 && !player.hasPermission("LiteTeleport.cooldown.bypass")) {
            Long time = cdList.getOrDefault(player.getUniqueId(), 0L);
            return Config.tpCooldown - (int) ((System.currentTimeMillis() - time) / 1000);
        }
        return 0;
    }

    private boolean isCooling(Player player) {
        int cd = getCooldown(player);
        if (cd > 0) {
            player.sendMessage(Language.replaceArgs(Language.teleportCooldown, cd));
            return true;
        }
        return false;
    }

    private void commandBack(Player player) {
        Location backLoc = backList.get(player.getUniqueId());
        if (backLoc == null) {
            player.sendMessage(Language.commandBackNotBack);
            return;
        }
        if (!isCooling(player) && teleportConsume(player, Config.backConsume)) {
            player.sendMessage(Language.commandBack);
            teleport(player, backLoc);
        }
    }

    private void commandDelhome(String[] args, Player player) {
        String playerName = player.getName(), homeName;
        if (args.length != 1) {
            player.sendMessage(Language.commandDelhomeUsage);
            return;
        } else {
            homeName = args[0];
        }
        if (HomeInfo.isBanName(homeName)) {
            player.sendMessage(Language.homeNameInvalid);
        } else if (HomeInfo.exist(playerName, homeName)) {
            HomeInfo.deleteHome(playerName, homeName);
            player.sendMessage(Language.replaceArgs(Language.commandDelhome, homeName));
        } else {
            player.sendMessage(Language.replaceArgs(Language.commandDelhomeDontExist, homeName));
        }
    }

    private void commandDelwarp(String[] args, Player player) {
        if (args.length != 1) {
            player.sendMessage(Language.commandDelwarpUsage);
        } else if (WarpInfo.isBanName(args[0])) {
            player.sendMessage(Language.warpNameInvalid);
        } else if (WarpInfo.exist(args[0])) {
            WarpInfo.deleteWarp(args[0]);
            player.sendMessage(Language.replaceArgs(Language.commandDelwarp, args[0]));
        } else {
            player.sendMessage(Language.warpDontExist);
        }
    }

    private void commandHome(String[] args, Player player) {
        String playerName = player.getName(), homeName;
        if (args.length > 1) {
            player.sendMessage(Language.commandHomeUsage);
            return;
        } else if (args.length == 0) {
            List<String> homeList = HomeInfo.getHomeList(playerName);
            if (homeList == null) {
                player.sendMessage(Language.homeListEmpty);
                return;
            } else if (homeList.size() == 1) {
                homeName = homeList.get(0);
            } else {
                player.sendMessage(HomeInfo.showHomeList(playerName));
                return;
            }
        } else {
            homeName = args[0];
        }
        if (HomeInfo.exist(playerName, homeName)) {
            if (!isCooling(player) && teleportConsume(player, Config.homeConsume)) {
                player.sendMessage(Language.replaceArgs(Language.commandHome, homeName));
                teleport(player, HomeInfo.getHomeLocation(playerName, homeName));
            }
        } else {
            player.sendMessage(HomeInfo.showHomeList(playerName));
        }
    }

    private void commandSethome(String[] args, Player player) {
        String playerName = player.getName(), homeName;
        if (args.length > 1) {
            player.sendMessage(Language.commandSethomeUsage);
            return;
        } else if (args.length == 0) {
            homeName = "home";
        } else {
            homeName = args[0];
        }
        if (HomeInfo.isBanName(homeName)) {
            player.sendMessage(Language.homeNameInvalid);
            return;
        }
        if (Config.sethomeConsume > 0.0 && !hasFreeSethomePermission(player)) {
            List<String> homeList = HomeInfo.getHomeList(playerName);
            int n;
            if (homeList == null) {
                n = 1;
            } else if (homeList.contains(homeName)) {
                n = homeList.indexOf(homeName) + 1;
                player.sendMessage(Language.replaceArgs(Language.commandSethomeAlreadyExists, homeName));
            } else {
                n = homeList.size() + 1;
            }
            ConsumeInfo consume;
            if (n == 1) {
                consume = Config.firstSethomeConsume;
            } else {
                int amount = (int) Math.pow(n, Config.sethomeConsume);
                if (Config.sethomeMaxConsume > 0 && amount > Config.sethomeMaxConsume) {
                    amount = Config.sethomeMaxConsume;
                }
                consume = new ConsumeInfo(Config.sethomeConsumeType, amount);
            }
            if (!consume(player, consume, Language.replaceArgs(Language.commandSethomeConsume, n, consume.getDescription()), Language.replaceArgs(Language.commandSethomeConsumeNotEnough, consume.getConsumeName()))) {
                return;
            }
        }
        HomeInfo.setHome(playerName, homeName, player.getLocation());
        player.sendMessage(Language.commandSethome);
    }

    private void commandSetspawn(Player player) {
        Location loc = player.getLocation();
        if (mcVersion > 12 || (mcVersion == 12 && mcVersionPatch == 2)) {
            player.getWorld().setSpawnLocation(loc);
        } else {
            player.getWorld().setSpawnLocation(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        }
        SpawnInfo.setSpawn(loc);
        player.sendMessage(Language.commandSetspawn);
    }

    private void commandSetwarp(String[] args, Player player) {
        if (args.length != 1) {
            player.sendMessage(Language.commandSetwarpUsage);
        } else if (WarpInfo.isBanName(args[0])) {
            player.sendMessage(Language.warpNameInvalid);
        } else {
            WarpInfo.setWarpLocation(args[0], player.getLocation());
            player.sendMessage(Language.replaceArgs(Language.commandSetwarp, args[0]));
        }
    }

    private void commandSpawn(Player player) {
        if (!isCooling(player) && teleportConsume(player, Config.spawnConsume)) {
            player.sendMessage(Language.teleport);
            Location loc = SpawnInfo.getSpawnLocation();
            //noinspection ConstantConditions
            teleport(player, loc == null ? getServer().getWorld(SpawnInfo.getSpawnWorld()).getSpawnLocation() : loc);
        }
    }

    private void commandTpa(String[] args, Player player) {
        Player target;
        if (args.length != 1 || player.getName().equals(args[0])) {
            player.sendMessage(Language.commandTpaUsage);
            return;
        }
        target = getServer().getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(Language.playerNotFound);
            return;
        }
        tpList.put(target.getUniqueId(), new TeleportRequest(player));
        target.sendMessage(Language.replaceArgs(Language.commandTpaTarget, player.getDisplayName()));
        sendRequestMsg(player, target);
    }

    private void commandTpacancel(Player player) {
        Player target = null;
        for (Map.Entry<UUID, TeleportRequest> entry : tpList.entrySet()) {
            if (entry.getValue().source == player.getUniqueId()) {
                target = getServer().getPlayer(entry.getKey());
                break;
            }
        }
        if (target == null) {
            player.sendMessage(Language.teleportRequestDontExist);
        } else {
            tpList.remove(target.getUniqueId());
            player.sendMessage(Language.commandTpacancel);
        }
    }

    private void commandTpaccept(Player player) {
        TeleportRequest tpRequest;
        tpRequest = tpList.remove(player.getUniqueId());
        if (tpRequest == null) {
            player.sendMessage(Language.teleportRequestDontExist);
            return;
        } else if (isCooling(player)) {
            return;
        }
        if (!Config.tpAcceptConsume.has(player) && !hasFreePermission(player)) {
            player.sendMessage(Language.replaceArgs(Language.commandTpacceptTargetConsumeNotEnough, Config.tpAcceptConsume.getConsumeName()));
            tpRequest.getSource().sendMessage(Language.replaceArgs(Language.commandTpacceptTargetConsumeNotEnoughSource, Config.tpAcceptConsume.getConsumeName()));
            return;
        } else if (!Config.tpSourceConsume.has(tpRequest.getSource()) && !hasFreePermission(tpRequest.getSource())) {
            player.sendMessage(Language.replaceArgs(Language.commandTpacceptSourceConsumeNotEnough, Config.tpSourceConsume.getConsumeName()));
            tpRequest.getSource().sendMessage(Language.replaceArgs(Language.commandTpacceptSourceConsumeNotEnoughSource, Config.tpSourceConsume.getConsumeName()));
            return;
        } else {
            if (Config.tpAcceptConsume.getAmount() != 0 && !hasFreePermission(player)) {
                Config.tpAcceptConsume.take(player);
            }
            if (Config.tpSourceConsume.getAmount() != 0 && !hasFreePermission(tpRequest.getSource())) {
                Config.tpSourceConsume.take(tpRequest.getSource());
            }
        }
        tpRequest.getSource().sendMessage(Language.replaceArgs(Language.commandTpacceptSource, player.getDisplayName()));
        if (tpRequest.teleportType == 0) { //发起者传送到接受者
            player.sendMessage(Language.commandTpaccept);
            tpRequest.getSource().sendMessage(Language.replaceArgs(Language.commandTpacceptTeleport, player.getDisplayName()));
            teleport(tpRequest.getSource(), player.getLocation());
        } else { //接受者传送到发起者
            player.sendMessage(Language.teleport);
            teleport(player, tpRequest.location);
        }
    }

    private void commandTpahere(String[] args, Player player) {
        Player target;
        if (args.length != 1 || player.getName().equals(args[0])) {
            player.sendMessage(Language.commandTpahereUsage);
            return;
        }
        target = getServer().getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(Language.playerNotFound);
            return;
        }
        tpList.put(target.getUniqueId(), new TeleportRequest(1, player, player.getLocation()));
        target.sendMessage(Language.replaceArgs(Language.commandTpahereTarget, player.getDisplayName()));
        sendRequestMsg(player, target);
    }

    private void sendRequestMsg(Player player, Player target) {
        target.sendMessage(Language.teleportRequestTargetAccept);
        target.sendMessage(Language.teleportRequestTargetDeny);
        if (!hasFreePermission(target)) {
            target.sendMessage(Language.replaceArgs(Language.teleportRequestTargetConsume, Config.tpAcceptConsume.getDescription()));
        }
        player.sendMessage(Language.replaceArgs(Language.teleportRequestSource, target.getDisplayName()));
        player.sendMessage(Language.teleportRequestSourceCancel);
        if (!hasFreePermission(player)) {
            player.sendMessage(Language.replaceArgs(Language.teleportRequestSourceConsume, Config.tpSourceConsume.getDescription()));
        }
    }

    private void commandTpdeny(Player player) {
        TeleportRequest tpRequest;
        tpRequest = tpList.remove(player.getUniqueId());
        if (tpRequest == null) {
            player.sendMessage(Language.teleportRequestDontExist);
        } else {
            tpRequest.getSource().sendMessage(Language.replaceArgs(Language.commandTpdenySource, player.getDisplayName()));
            player.sendMessage(Language.commandTpdeny);
        }
    }

    private void commandTpr(Player player) {
        String playerName = player.getName();
        if (!Config.allowTprWorld.contains(player.getWorld().getName())) {
            player.sendMessage(Language.commandTprWorldNotAllow);
            return;
        } else if (isCooling(player)) {
            return;
        }
        int n = TprInfo.getTprCount(playerName) + 1;
        ConsumeInfo consume;
        if (Config.tprConsume > 0.0 && !hasFreeTprPermission(player)) {
            if (n == 1) {
                consume = Config.firstTprConsume;
            } else {
                int amount = (int) Math.pow(n, Config.tprConsume);
                if (Config.tprMaxConsume > 0 && amount > Config.tprMaxConsume) {
                    amount = Config.tprMaxConsume;
                }
                consume = new ConsumeInfo(Config.tprConsumeType, amount);
            }
            if (!consume(player, consume, Language.replaceArgs(Language.commandTprConsume, n, consume.getDescription()), Language.replaceArgs(Language.commandTprConsumeNotEnough, consume.getConsumeName()))) {
                return;
            }
        } else {
            consume = null;
        }
        Location loc;
        if (player.getWorld().getEnvironment() == World.Environment.NETHER) {
            if (mcVersion > 10) { //1.10没有sendTitle 而且低版本区块加载比高版本快很多 不需要提示等待
                player.sendTitle(Language.commandTprTitle, Language.commandTprSubtitle, 20, 160, 20);
            }
            loc = RandomTeleport.getRandomLocByNether(player);
        } else {
            if (mcVersion > 10) { //1.10以及下不需要提示等待
                if (player.getWorld().getEnvironment() == World.Environment.THE_END) {
                    player.sendMessage(Language.commandTprTheEnd);
                } else {
                    player.sendTitle(Language.commandTprTitle, Language.commandTprSubtitle, 20, 160, 20);
                }
            }
            loc = RandomTeleport.getRandomLoc(player);
        }
        if (mcVersion > 10) {
            player.sendTitle("", "", 0, 0, 0);
        }
        if (loc == null) {
            player.sendMessage(Language.commandTprNotFoundSafeLocation);
            if (consume != null) {
                consume.give(player);
            }
            return;
        }
        TprInfo.setTprCount(playerName, n);
        player.sendMessage(Language.teleport);
        teleport(player, loc);
    }

    private void commandWarp(String[] args, Player player) {
        if (args.length > 1) {
            player.sendMessage(Language.commandWarpUsage);
            return;
        } else if (args.length == 0) {
            player.sendMessage(WarpInfo.showWarpList());
            return;
        }
        if (WarpInfo.exist(args[0])) {
            if (!isCooling(player) && teleportConsume(player, Config.warpConsume)) {
                player.sendMessage(Language.replaceArgs(Language.commandWarp, args[0]));
                teleport(player, WarpInfo.getWarpLocation(args[0]));
            }
        } else {
            player.sendMessage(Language.warpDontExist);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void playerTeleportEvent(PlayerTeleportEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.PLUGIN || event.getCause() == PlayerTeleportEvent.TeleportCause.COMMAND) {
            backList.put(event.getPlayer().getUniqueId(), event.getFrom());
        }
    }

    @EventHandler
    public void playerDeathEvent(PlayerDeathEvent event) {
        Player player = event.getEntity();
        backList.put(player.getUniqueId(), player.getLocation());
        player.sendMessage(Language.playerDeathMessage);
        if (Config.deathGiveExp != 0 && Config.backConsume.getType() == ConsumeInfo.ConsumeType.LEVEL) {
            event.setNewLevel(Config.deathGiveExp);
        }
    }
}
