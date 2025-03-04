package ml.mcos.liteteleport;

import ml.mcos.liteteleport.config.*;
import ml.mcos.liteteleport.consume.ConsumeInfo;
import ml.mcos.liteteleport.metrics.Metrics;
import ml.mcos.liteteleport.papi.LiteTeleportExpansion;
import ml.mcos.liteteleport.teleport.RandomTeleport;
import ml.mcos.liteteleport.teleport.TeleportRequest;
import ml.mcos.liteteleport.update.UpdateChecker;
import ml.mcos.liteteleport.update.UpdateNotification;
import ml.mcos.liteteleport.util.CompatibleEconomy;
import ml.mcos.liteteleport.util.TabComplete;
import ml.mcos.liteteleport.util.Version;
import net.milkbowl.vault.economy.Economy;
import net.myunco.folia.FoliaCompatibleAPI;
import net.myunco.folia.scheduler.CompatibleScheduler;
import net.myunco.folia.task.CompatibleTask;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class LiteTeleport extends JavaPlugin implements Listener {
    public static LiteTeleport plugin;
    public ConsoleCommandSender consoleSender;
    private final HashMap<UUID, TeleportRequest> tpList = new HashMap<>();
    private final HashMap<UUID, Location> backList = new HashMap<>();
    private final HashMap<UUID, Long> cdList = new HashMap<>();
    public static Version mcVersion;
    public static CompatibleEconomy economy;
    public static PlayerPointsAPI pointsAPI;
    private FoliaCompatibleAPI foliaCompatibleAPI;
    private CompatibleScheduler scheduler;

    @Override
    public void onEnable() {
        plugin = this;
        consoleSender = getServer().getConsoleSender();
        mcVersion = new Version(getServer().getBukkitVersion());
        // foliaCompatibleAPI = FoliaCompatibleAPI.getInstance();
        initFoliaCompatibleAPI();
        scheduler = foliaCompatibleAPI.getScheduler(this);
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
        economy = new CompatibleEconomy(rsp.getProvider());
        sendMessage("Using economy system: §3" + economy.getName() + " v" + rsp.getPlugin().getDescription().getVersion());
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

    public void initFoliaCompatibleAPI() {
        Plugin api = getServer().getPluginManager().getPlugin("FoliaCompatibleAPI");
        if (api == null) {
            getLogger().warning("FoliaCompatibleAPI not found!");
            File file = new File(getDataFolder().getParentFile(), "FoliaCompatibleAPI-1.2.0.jar");
            InputStream in = getResource("lib/FoliaCompatibleAPI-1.2.0.jar");
            try {
                saveResource(file, in);
                api = getServer().getPluginManager().loadPlugin(file);
                if (api == null) {
                    throw new Exception("FoliaCompatibleAPI load failed!");
                }
                getServer().getPluginManager().enablePlugin(api);
                api.onLoad();
            } catch (Exception e) {
                e.printStackTrace();
                getLogger().severe("未安装 FoliaCompatibleAPI ，本插件无法运行！");
                return;
            }
        } else if (api.getDescription().getVersion().equals("1.1.0")) {
            getLogger().warning("FoliaCompatibleAPI version is 1.1.0, please update to 1.2.0 or later!");
            /* // 自动更新
            try {
                File file = new File(api.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
                System.out.println("file.getAbsolutePath() = " + file.getAbsolutePath());
                saveResource(file, getResource("lib/FoliaCompatibleAPI-1.2.0.jar"));
            } catch (Exception ignored) {
            } */
        }
        foliaCompatibleAPI = (FoliaCompatibleAPI) api;
        getServer().getConsoleSender().sendMessage("[LiteTeleport] Found FoliaCompatibleAPI: §3v" + api.getDescription().getVersion());
    }

    private void saveResource(File target, InputStream source) throws Exception {
        if (source != null) {
            //noinspection IOStreamConstructor
            OutputStream out = new FileOutputStream(target);
            byte[] buf = new byte[8192];
            int len;
            while ((len = source.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            out.close();
            source.close();
        }
    }

    public void sendMessage(String msg) {
        consoleSender.sendMessage(Language.logPrefix + msg);
    }

    public ClassLoader classLoader() {
        return getClassLoader();
    }

    public CompatibleScheduler getScheduler() {
        return this.scheduler;
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
                    getScheduler().runTaskAsynchronously(() -> commandTpr(player));
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
                    if (args.length == 1 && player.hasPermission("LiteTeleport.warp.list")) {
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
        if (mcVersion.isLessThan(13, 2)) {
            // 1.13.2之前没有Consumer为参数的runTaskTimer方法 改用Runnable实现
            final CompatibleTask[] task = new CompatibleTask[1];
            task[0] = getScheduler().runTaskTimer(new Runnable() {
                int i = Config.tpDelay;
                final Location pos = player.getLocation();

                @Override
                public void run() {
                    i--;
                    if (!locationEqual(pos, player.getLocation())) {
                        player.sendMessage(Language.teleportCancel);
                        consume.give(player);
                        task[0].cancel();
                        return;
                    }
                    if (i == 0) {
                        teleportConfirm(player, location, message);
                        task[0].cancel();
                    }
                }
            }, 20, 20);
        } else {
            getScheduler().runTaskTimer(new Consumer<CompatibleTask>() {
                int i = Config.tpDelay;
                final Location pos = player.getLocation();

                @Override
                public void accept(CompatibleTask task) {
                    i--;
                    if (!locationEqual(pos, player.getLocation())) {
                        player.sendMessage(Language.teleportCancel);
                        consume.give(player);
                        task.cancel();
                        return;
                    }
                    if (i == 0) {
                        teleportConfirm(player, location, message);
                        task.cancel();
                    }
                }
            }, 20, 20);
        }
    }

    private void teleportConfirm(Player player, Location location, String message) {
        if (Config.tpCooldown > 0) {
            cdList.put(player.getUniqueId(), System.currentTimeMillis());
        }
        player.sendMessage(message);
        // player.teleport(location);
        if (foliaCompatibleAPI.isFolia()) {
            backList.put(player.getUniqueId(), player.getLocation());
        }
        foliaCompatibleAPI.teleport(player, location);
    }

    private boolean locationEqual(Location loc1, Location loc2) {
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
            }
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
        if (mcVersion.isGreaterThanOrEqualTo(12, 2)) {
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
        UUID target = null;
        for (Map.Entry<UUID, TeleportRequest> entry : tpList.entrySet()) {
            if (entry.getValue().source == player.getUniqueId()) {
                target = entry.getKey();
                break;
            }
        }
        if (target == null) {
            player.sendMessage(Language.teleportRequestDontExist);
        } else {
            tpList.remove(target);
            player.sendMessage(Language.commandTpacancel);
        }
    }

    private void commandTpaccept(Player player) {
        TeleportRequest tpRequest = tpList.remove(player.getUniqueId());
        if (tpRequest == null) {
            player.sendMessage(Language.teleportRequestDontExist);
            return;
        } else if (isCooling(player)) {
            return;
        }
        Player source = tpRequest.getSource();
        if (source == null) {
            player.sendMessage(Language.playerNotFound);
            return;
        }
        if (!Config.tpAcceptConsume.has(player) && !hasFreePermission(player)) {
            player.sendMessage(Language.replaceArgs(Language.commandTpacceptTargetConsumeNotEnough, Config.tpAcceptConsume.getConsumeName()));
            source.sendMessage(Language.replaceArgs(Language.commandTpacceptTargetConsumeNotEnoughSource, Config.tpAcceptConsume.getConsumeName()));
            return;
        } else if (!Config.tpSourceConsume.has(source) && !hasFreePermission(source)) {
            player.sendMessage(Language.replaceArgs(Language.commandTpacceptSourceConsumeNotEnough, Config.tpSourceConsume.getConsumeName()));
            source.sendMessage(Language.replaceArgs(Language.commandTpacceptSourceConsumeNotEnoughSource, Config.tpSourceConsume.getConsumeName()));
            return;
        } else {
            if (!hasFreePermission(player)) {
                Config.tpAcceptConsume.take(player);
            }
            if (!hasFreePermission(source)) {
                Config.tpSourceConsume.take(source);
            }
        }
        source.sendMessage(Language.replaceArgs(Language.commandTpacceptSource, player.getDisplayName()));
        if (tpRequest.teleportType == 0) { //发起者传送到接受者
            player.sendMessage(Language.commandTpaccept);
            teleport(source, player.getLocation(), Config.tpSourceConsume, Language.replaceArgs(Language.commandTpacceptTeleport, player.getDisplayName()));
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
        TeleportRequest tpRequest = tpList.remove(player.getUniqueId());
        if (tpRequest == null) {
            player.sendMessage(Language.teleportRequestDontExist);
        } else {
            Player source = tpRequest.getSource();
            if (source == null) {
                player.sendMessage(Language.playerNotFound);
                return;
            }
            source.sendMessage(Language.replaceArgs(Language.commandTpdenySource, player.getDisplayName()));
            player.sendMessage(Language.commandTpdeny);
        }
    }

    private void commandTpr(Player player) {
        String playerName = player.getName();
        if (!Config.allowTprWorld.contains(player.getWorld().getName().toLowerCase())) {
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
        if (Config.tpDelay > 0 && !player.hasPermission("LiteTeleport.delay.bypass")) {
            player.sendMessage(Language.replaceArgs(Language.teleportDelay, Config.tpDelay));
            final CompletableFuture<Boolean> future = new CompletableFuture<>();

            if (mcVersion.isLessThan(13, 2)) {
                // 1.13.2之前没有Consumer为参数的runTaskTimer方法 改用Runnable实现
                final CompatibleTask[] task = new CompatibleTask[1];
                task[0] = getScheduler().runTaskTimer(new Runnable() {
                    int i = Config.tpDelay;
                    final Location pos = player.getLocation();

                    @Override
                    public void run() {
                        System.out.println("【测试】 i = " + i);
                        i--;
                        if (!locationEqual(pos, player.getLocation())) {
                            player.sendMessage(Language.teleportCancel);
                            if (consume != null) {
                                consume.give(player);
                            }
                            future.complete(false); // 取消传送
                            task[0].cancel();
                            return;
                        }
                        if (i == 0) {
                            future.complete(true); // 继续传送
                            task[0].cancel();
                        }
                    }
                }, 20, 20);
            } else {
                getScheduler().runTaskTimer(new Consumer<CompatibleTask>() {
                    int i = Config.tpDelay;
                    final Location pos = player.getLocation();

                    @Override
                    public void accept(CompatibleTask task) {
                        System.out.println("【测试】 i = " + i);
                        i--;
                        if (!locationEqual(pos, player.getLocation())) {
                            player.sendMessage(Language.teleportCancel);
                            if (consume != null) {
                                consume.give(player);
                            }
                            future.complete(false); // 取消传送
                            task.cancel();
                            return;
                        }
                        if (i == 0) {
                            future.complete(true); // 继续传送
                            task.cancel();
                        }
                    }
                }, 20, 20);
            }
            if (!future.join()) {
                return;
            }
        }
        Location loc;
        if (player.getWorld().getEnvironment() == World.Environment.NETHER) {
            if (mcVersion.isGreaterThan(10)) {
                player.sendTitle(Language.commandTprTitle, Language.commandTprSubtitle, 20, 160, 20);
            }
            loc = RandomTeleport.getRandomLocByNether(player, getScheduler());
        } else {
            if (mcVersion.isGreaterThan(10)) {
                if (player.getWorld().getEnvironment() == World.Environment.THE_END) {
                    player.sendMessage(Language.commandTprTheEnd);
                } else {
                    player.sendTitle(Language.commandTprTitle, Language.commandTprSubtitle, 20, 160, 20);
                }
            }
            loc = RandomTeleport.getRandomLoc(player, getScheduler());
        }
        if (mcVersion.isGreaterThan(10)) {
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
        if (foliaCompatibleAPI.isFolia()) {
            teleportConfirm(player, loc, Language.teleport);
        } else {
            getScheduler().runTask(() -> teleportConfirm(player, loc, Language.teleport));
        }
    }

    private void commandWarp(String[] args, Player player) {
        if (args.length > 1) {
            player.sendMessage(Language.commandWarpUsage);
            return;
        } else if (args.length == 0) {
            if (player.hasPermission("LiteTeleport.warp.list")) {
                player.sendMessage(WarpInfo.showWarpList());
            } else {
                player.sendMessage(Language.commandWarpUsage);
            }
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
        backList.put(player.getUniqueId(), player.getLocation()); // 不管有没有权限都保存位置, 以免有玩家死亡时没有权限但后来又获得权限想回去却没法回去
        if (player.hasPermission("LiteTeleport.back")) {
            player.sendMessage(Language.playerDeathMessage);
            if (Config.deathGiveExp != 0 && Config.backConsume.getType() == ConsumeInfo.ConsumeType.LEVEL) {
                event.setNewLevel(Config.deathGiveExp);
            }
        }
    }
}
