package ml.mcos.liteteleport;

import ml.mcos.liteteleport.config.*;
import ml.mcos.liteteleport.consume.ConsumeInfo;
import ml.mcos.liteteleport.metrics.Metrics;
import ml.mcos.liteteleport.papi.LiteTeleportExpansion;
import ml.mcos.liteteleport.teleport.RandomTeleport;
import ml.mcos.liteteleport.teleport.TeleportRequest;
import ml.mcos.liteteleport.update.UpdateChecker;
import ml.mcos.liteteleport.update.UpdateNotification;
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
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

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
        if (Config.checkUpdate) {
            getServer().getPluginManager().registerEvents(new UpdateNotification(), this);
        }
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
        setupPAPI();
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

    public void setupPAPI() {
        Plugin papi = getServer().getPluginManager().getPlugin("PlaceholderAPI");
        if (papi != null && papi.isEnabled()) {
            sendMessage("Found PlaceHolderAPI: §3v" + papi.getDescription().getVersion());
            new LiteTeleportExpansion().register();
        }
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
        if (command.getName().equals("LiteTeleport")) {
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
                    case "migrate":
                        EssMigrate.migrateData(sender);
                        return true;
                }
            }
            return false;
        }
        if (sender instanceof Player) {
            Player player = (Player) sender;
            switch (command.getName()) {
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
                case "homes":
                    commandHomes(player);
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

    private void teleport(Player player, Location location, ConsumeInfo consume, String message) {
        if (Config.tpDelay <= 0 || player.hasPermission("LiteTeleport.delay.bypass")) {
            teleportConfirm(player, location, message);
            return;
        }
        player.sendMessage(Language.replaceArgs(Language.teleportDelay, Config.tpDelay));
        plugin.sendMessage(String.valueOf(System.currentTimeMillis()));
        getServer().getScheduler().runTaskTimer(plugin, new Consumer<BukkitTask>() {
            int i = Config.tpDelay;
            final Location pos = player.getLocation();
            @Override
            public void accept(BukkitTask bukkitTask) {
                i--;
                if (!compareLoc(pos, player.getLocation())) {
                    player.sendMessage(Language.teleportCancel);
                    consume.give(player);
                    bukkitTask.cancel();
                    return;
                }
                if (i == 0) {
                    plugin.sendMessage(String.valueOf(System.currentTimeMillis()));
                    teleportConfirm(player, location, message);
                    bukkitTask.cancel();
                }
            }
        }, 20, 20);

    }

    private void teleportConfirm(Player player, Location location, String message) {
        if (Config.tpCooldown > 0) {
            cdList.put(player.getUniqueId(), System.currentTimeMillis());
        }
        player.sendMessage(message);
        player.teleport(location);
    }

    private boolean compareLoc(Location loc1, Location loc2) {
        return loc1.getBlockX() == loc2.getBlockX() && loc1.getBlockY() == loc2.getBlockY() && loc1.getBlockZ() == loc2.getBlockZ();
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
            teleport(player, backLoc, Config.backConsume, Language.commandBack);
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
            if (homeList == null || homeList.isEmpty()) {
                player.sendMessage(Language.homeListEmpty);
                return;
            } else {
                homeName = homeList.get(0);
            }/*else if (homeList.size() == 1) {
                homeName = homeList.get(0);
            } else {
                player.sendMessage(HomeInfo.showHomeList(playerName));
                return;
            }*/
        } else {
            homeName = args[0];
        }
        if (HomeInfo.exist(playerName, homeName)) {
            if (!isCooling(player) && teleportConsume(player, Config.homeConsume)) {
                teleport(player, HomeInfo.getHomeLocation(playerName, homeName), Config.homeConsume, Language.replaceArgs(Language.commandHome, homeName));
            }
        } else {
            player.sendMessage(HomeInfo.showHomeList(playerName));
        }
    }

    private void commandHomes(Player player) {
        player.sendMessage(HomeInfo.showHomeList(player.getName()));
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
        List<String> homeList = HomeInfo.getHomeList(playerName);
        int count;
        if (homeList == null) {
            count = 0;
        } else if (homeList.contains(homeName)) {
            count = homeList.indexOf(homeName);
            player.sendMessage(Language.replaceArgs(Language.commandSethomeAlreadyExists, homeName));
        } else {
            count = homeList.size();
        }
        if (Config.sethomeMax != 0 && count >= Config.sethomeMax && !player.hasPermission("LiteTeleport.sethome.unlimited")) {
            player.sendMessage(Language.commandSethomeMax);
            return;
        }
        count++;
        if (Config.sethomeConsume > 0.0 && !player.hasPermission("LiteTeleport.free.sethome")) {
            ConsumeInfo consume;
            if (count == 1) {
                consume = Config.firstSethomeConsume;
            } else {
                int amount = (int) Math.pow(count, Config.sethomeConsume);
                if (Config.sethomeMaxConsume > 0 && amount > Config.sethomeMaxConsume) {
                    amount = Config.sethomeMaxConsume;
                }
                consume = new ConsumeInfo(Config.sethomeConsumeType, amount);
            }
            if (!consume(player, consume, Language.replaceArgs(Language.commandSethomeConsume, count, consume.getDescription()), Language.replaceArgs(Language.commandSethomeConsumeNotEnough, consume.getConsumeName()))) {
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
            Location loc = SpawnInfo.getSpawnLocation();
            //noinspection ConstantConditions
            teleport(player, loc == null ? getServer().getWorld(SpawnInfo.getSpawnWorld()).getSpawnLocation() : loc, Config.spawnConsume, Language.teleport);
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
            if (!hasFreePermission(player)) {
                Config.tpAcceptConsume.take(player);
            }
            if (!hasFreePermission(tpRequest.getSource())) {
                Config.tpSourceConsume.take(tpRequest.getSource());
            }
        }
        tpRequest.getSource().sendMessage(Language.replaceArgs(Language.commandTpacceptSource, player.getDisplayName()));
        if (tpRequest.teleportType == 0) { //发起者传送到接受者
            player.sendMessage(Language.commandTpaccept);
            teleport(tpRequest.getSource(), player.getLocation(), Config.tpSourceConsume, Language.replaceArgs(Language.commandTpacceptTeleport, player.getDisplayName()));
        } else { //接受者传送到发起者
            teleport(player, tpRequest.location, Config.tpAcceptConsume, Language.teleport);
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
        if (Config.tprConsume > 0.0 && !player.hasPermission("LiteTeleport.free.tpr")) {
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
            if (mcVersion > 10) { //1.10没有提供sendTitle方法 而且低版本区块加载比高版本快很多 不需要提示等待
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
        teleportConfirm(player, loc, Language.teleport); //随机传送不支持延时 一旦开始不能取消
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
                teleport(player, WarpInfo.getWarpLocation(args[0]), Config.warpConsume, Language.replaceArgs(Language.commandWarp, args[0]));
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
