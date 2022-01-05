package ml.mcos.liteteleport;

import ml.mcos.liteteleport.config.Config;
import ml.mcos.liteteleport.config.HomeInfo;
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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LiteTeleport extends JavaPlugin implements Listener {
    public static LiteTeleport plugin;
    public HashMap<UUID, TeleportRequest> tpList = new HashMap<>();
    public HashMap<UUID, Location> backList = new HashMap<>();
    public HashMap<UUID, Long> cdList = new HashMap<>();
    public static int mcVersion;
    public static int mcVersionPatch;
    public static Economy economy;
    public static PlayerPointsAPI pointsAPI;

    @Override
    public void onEnable() {
        plugin = this;
        mcVersion = getMinecraftVersion();
        initConfig();
        if (Config.useEconomy) {
            setupEconomy();
        }
        if (Config.usePoints) {
            setupPointsAPI();
        }
        getServer().getPluginManager().registerEvents(this, this);
        new Metrics(this, 12936);
    }

    @Override
    public void onDisable() {
        UpdateChecker.stop();
    }

    public void initConfig() {
        Config.loadConfig();
        HomeInfo.loadHomeInfo();
        SpawnInfo.loadSpawnInfo();
        TprInfo.loadTprInfo();
        WarpInfo.loadWarpInfo();
        if (Config.checkUpdate) {
            UpdateChecker.start();
        }
    }

    public void setupEconomy() {
        if (!getServer().getPluginManager().isPluginEnabled("Vault")) {
            getLogger().severe("未找到Vault，请检查是否正确安装Vault插件！");
            return;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            getLogger().severe("未找到经济系统，请检查是否正确安装经济提供插件！(如Essentials、CMI、Economy等)");
            return;
        }
        economy = rsp.getProvider();
    }

    public void setupPointsAPI() {
        Plugin playerPoints = getServer().getPluginManager().getPlugin("PlayerPoints");
        if (playerPoints == null || !playerPoints.isEnabled()) {
            getLogger().severe("未找到PlayerPoints，请检查是否正确安装点券插件！");
            return;
        }
        pointsAPI = PlayerPoints.getInstance().getAPI();
        //pointsAPI = ((PlayerPoints) playerPoints).getAPI();
        if (pointsAPI == null) {
            getLogger().severe("未能获取PlayerPointsAPI，可能是版本太旧，请尝试更新点券插件！");
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

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equals("LiteTeleport")) {
            if (args.length == 1) {
                switch (args[0].toLowerCase()) {
                    case "version":
                        sender.sendMessage("§a当前版本: §b" + getDescription().getVersion());
                        return true;
                    case "reload":
                        UpdateChecker.stop();
                        initConfig();
                        sender.sendMessage("§a配置文件重载完成。");
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
        return consume(player, consume, "§c本次传送将花费§a" + consume + "§c级经验。", "§c错误: §4你没有足够的等级支付本次传送花费。");
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
            player.sendMessage("§4传送冷却: §c" + cd + "§4秒。");
            return true;
        }
        return false;
    }

    private void commandBack(Player player) {
        Location backLoc = backList.get(player.getUniqueId());
        if (backLoc == null) {
            player.sendMessage("§c错误: §6没有上一位置可以回去。");
            return;
        }
        if (!isCooling(player) && teleportConsume(player, Config.backConsume)) {
            player.sendMessage("§6正在回到上一位置...");
            teleport(player, backLoc);
        }
    }

    private void commandDelhome(String[] args, Player player) {
        String playerName = player.getName(), homeName;
        if (args.length != 1) {
            player.sendMessage("§c错误: 请使用§6/delhome <家名称>");
            return;
        } else {
            homeName = args[0];
        }
        if (HomeInfo.isBanName(homeName)) {
            player.sendMessage("§c错误: §4无效的家名称!");
        } else if (HomeInfo.exist(playerName, homeName)) {
            HomeInfo.deleteHome(playerName, homeName);
            player.sendMessage("§6家§c" + homeName + "§6已被移除。");
        } else {
            player.sendMessage("§c错误: §4家§c" + homeName + "§4不存在!");

        }
    }

    private void commandDelwarp(String[] args, Player player) {
        if (args.length != 1) {
            player.sendMessage("§c错误: 请使用§6/delwarp <传送点名称>");
        } else if (WarpInfo.isBanName(args[0])) {
            player.sendMessage("§c错误: §4无效的传送点名称!");
        } else if (WarpInfo.exist(args[0])) {
            WarpInfo.deleteWarp(args[0]);
            player.sendMessage("§6传送点§c" + args[0] + "§6已被移除。");
        } else {
            player.sendMessage("§c错误: §4该传送点不存在。");
        }
    }

    private void commandHome(String[] args, Player player) {
        String playerName = player.getName(), homeName;
        if (args.length > 1) {
            player.sendMessage("§c错误: 请使用§6/home [家名称]");
            return;
        } else if (args.length == 0) {
            List<String> homeList = HomeInfo.getHomeList(playerName);
            if (homeList == null) {
                player.sendMessage("§6你还没有设置过家。");
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
                player.sendMessage("§6传送到§c" + homeName + "§6。");
                teleport(player, HomeInfo.getHomeLocation(playerName, homeName));
            }
        } else {
            player.sendMessage(HomeInfo.showHomeList(playerName));
        }
    }

    private void commandSethome(String[] args, Player player) {
        String playerName = player.getName(), homeName;
        if (args.length > 1) {
            player.sendMessage("§c错误: 请使用§6/sethome [家名称]");
            return;
        } else if (args.length == 0) {
            homeName = "home";
        } else {
            homeName = args[0];
        }
        if (HomeInfo.isBanName(homeName)) {
            player.sendMessage("§c错误: §4无效的家名称!");
            return;
        }
        if (Config.sethomeConsume > 0.0 && !hasFreeSethomePermission(player)) {
            List<String> homeList = HomeInfo.getHomeList(playerName);
            int n;
            if (homeList == null) {
                n = 1;
            } else if (homeList.contains(homeName)) {
                n = homeList.indexOf(homeName) + 1;
                player.sendMessage("§6家§c" + homeName + "§6已存在，将被重设为当前位置。");
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
            if (!consume(player, consume, "§c设置你的第§9" + n + "§c个家将花费§a" + consume + "§c级经验。", "§c错误: §4你没有足够的等级支付本次设置花费。")) {
                return;
            }
        }
        HomeInfo.setHome(playerName, homeName, player.getLocation());
        player.sendMessage("§6已设置家。");
    }

    private void commandSetspawn(Player player) {
        Location loc = player.getLocation();
        if (mcVersion > 12 || (mcVersion == 12 && mcVersionPatch == 2)) {
            player.getWorld().setSpawnLocation(loc);
        } else {
            player.getWorld().setSpawnLocation(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        }
        SpawnInfo.setSpawn(loc);
        player.sendMessage("§6已将世界出生点设为当前位置。");
    }

    private void commandSetwarp(String[] args, Player player) {
        if (args.length != 1) {
            player.sendMessage("§c错误: 请使用§6/setwarp <传送点名称>");
        } else if (WarpInfo.isBanName(args[0])) {
            player.sendMessage("§c错误: §4无效的传送点名称!");
        } else {
            WarpInfo.setWarpLocation(args[0], player.getLocation());
            player.sendMessage("§6已设置传送点§c" + args[0] + "§6。");
        }
    }

    private void commandSpawn(Player player) {
        if (!isCooling(player) && teleportConsume(player, Config.spawnConsume)) {
            player.sendMessage("§6正在传送...");
            Location loc = SpawnInfo.getSpawnLocation();
            //noinspection ConstantConditions
            teleport(player, loc == null ? getServer().getWorld(SpawnInfo.getSpawnWorld()).getSpawnLocation() : loc);
        }
    }

    private void commandTpa(String[] args, Player player) {
        Player target;
        if (args.length != 1 || player.getName().equals(args[0])) {
            player.sendMessage("§c错误: 请使用§6/tpa <玩家>");
            return;
        }
        target = getServer().getPlayer(args[0]);
        if (target == null) {
            player.sendMessage("§c错误: §4未找到该玩家。");
            return;
        }
        tpList.put(target.getUniqueId(), new TeleportRequest(player));
        target.sendMessage("§c" + player.getDisplayName() + "§6请求传送到你这里。");
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
            player.sendMessage("§c错误: §4你没有待处理的请求。");
        } else {
            tpList.remove(target.getUniqueId());
            player.sendMessage("§6传送请求已被取消。");
        }
    }

    private void commandTpaccept(Player player) {
        TeleportRequest tpRequest;
        tpRequest = tpList.remove(player.getUniqueId());
        if (tpRequest == null) {
            player.sendMessage("§c错误: §4你没有待处理的请求。");
            return;
        } else if (isCooling(player)) {
            return;
        }
        if (!Config.tpAcceptConsume.has(player) && !hasFreePermission(player)) {
            player.sendMessage("§c错误: §4你没有足够的等级来接受此请求。");
            tpRequest.getSource().sendMessage("§c错误: §4对方没有足够的等级来接受此请求。");
            return;
        } else if (!Config.tpSourceConsume.has(tpRequest.getSource()) && !hasFreePermission(tpRequest.getSource())) {
            player.sendMessage("§c错误: §4对方没有足够的等级支付本次传送花费。");
            tpRequest.getSource().sendMessage("§c错误: §4你没有足够的等级支付本次传送花费。");
            return;
        } else {
            if (Config.tpAcceptConsume.getAmount() != 0 && !hasFreePermission(player)) {
                Config.tpAcceptConsume.take(player);
            }
            if (Config.tpSourceConsume.getAmount() != 0 && !hasFreePermission(tpRequest.getSource())) {
                Config.tpSourceConsume.take(tpRequest.getSource());
            }
        }
        tpRequest.getSource().sendMessage("§c" + player.getDisplayName() + "§6接受了你的传送请求。");
        if (tpRequest.teleportType == 0) { //发起者传送到接受者
            player.sendMessage("§6已接受传送请求。");
            tpRequest.getSource().sendMessage("§6正在传送至§c" + player.getDisplayName() + "§6。");
            teleport(tpRequest.getSource(), player.getLocation());
        } else { //接受者传送到发起者
            player.sendMessage("§6正在传送...");
            teleport(player, tpRequest.location);
        }
    }

    private void commandTpahere(String[] args, Player player) {
        Player target;
        if (args.length != 1 || player.getName().equals(args[0])) {
            player.sendMessage("§c错误: 请使用§6/tpahere <玩家>");
            return;
        }
        target = getServer().getPlayer(args[0]);
        if (target == null) {
            player.sendMessage("§c错误: §4未找到该玩家。");
            return;
        }
        tpList.put(target.getUniqueId(), new TeleportRequest(1, player, player.getLocation()));
        target.sendMessage("§c" + player.getDisplayName() + "§6请求你传送到他那里。");
        sendRequestMsg(player, target);
    }

    private void sendRequestMsg(Player player, Player target) {
        target.sendMessage("§6若想接受传送，输入§c/tpaccept");
        target.sendMessage("§6若想拒绝传送，输入§c/tpdeny");
        if (!hasFreePermission(target)) {
            target.sendMessage("§c接受此请求将花费§a" + Config.tpAcceptConsume + "§c级经验。");
        }
        player.sendMessage("§6请求已发送给§c" + target.getDisplayName() + "§6。");
        player.sendMessage("§6若要取消这个请求，请输入§c/tpacancel");
        if (!hasFreePermission(player)) {
            player.sendMessage("§c传送时将花费§a" + Config.tpSourceConsume + "§c级经验。");
        }
    }

    private void commandTpdeny(Player player) {
        TeleportRequest tpRequest;
        tpRequest = tpList.remove(player.getUniqueId());
        if (tpRequest == null) {
            player.sendMessage("§c错误: §4你没有待处理的请求。");
        } else {
            tpRequest.getSource().sendMessage("§c" + player.getDisplayName() + "§6拒绝了你的传送请求。");
            player.sendMessage("§6已拒绝传送请求。");
        }
    }

    private void commandTpr(Player player) {
        String playerName = player.getName();
        if (!Config.allowTprWorld.contains(player.getWorld().getName())) {
            player.sendMessage("§c错误: §4你所在的世界不允许使用随机传送。");
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
            if (!consume(player, consume, "§c第§3" + n + "§c次随机传送将花费§a" + consume + "§c级经验。", "§c错误: §4你没有足够的等级支付本次传送花费。")) {
                return;
            }
        } else {
            consume = null;
        }
        Location loc;
        if (player.getWorld().getEnvironment() == World.Environment.NETHER) {
            if (mcVersion > 10) { //1.10没有sendTitle 而且1.12及以下版本区块加载比高版本快很多 不需要提示等待
                player.sendTitle("§a随机传送", "§b传送将在10秒内开始···", 20, 160, 20);
            }
            loc = RandomTeleport.getRandomLocByNether(player);
        } else {
            if (mcVersion > 10) { //1.10以及下不需要提示等待
                if (player.getWorld().getEnvironment() == World.Environment.THE_END) {
                    player.sendMessage("§3寻找安全位置可能需要花费一些时间，请耐心等待。");
                } else {
                    player.sendTitle("§a随机传送", "§b传送将在10秒内开始···", 20, 160, 20);
                }
            }
            loc = RandomTeleport.getRandomLoc(player);
        }
        if (mcVersion > 10) {
            player.sendTitle("", "", 0, 0, 0);
        }
        if (loc == null) {
            player.sendMessage("§c错误: §4未找到安全位置，请重试。");
            if (consume != null) {
                consume.give(player);
            }
            return;
        }
        TprInfo.setTprCount(playerName, n);
        player.sendMessage("§6正在传送...");
        teleport(player, loc);
    }

    private void commandWarp(String[] args, Player player) {
        if (args.length > 1) {
            player.sendMessage("§c错误: 请使用§6/warp <传送点名称>");
            return;
        } else if (args.length == 0) {
            player.sendMessage(WarpInfo.showWarpList());
            return;
        }
        if (WarpInfo.exist(args[0])) {
            if (!isCooling(player) && teleportConsume(player, Config.warpConsume)) {
                player.sendMessage("§6传送到§c" + args[0] + "§6。");
                teleport(player, WarpInfo.getWarpLocation(args[0]));
            }
        } else {
            player.sendMessage("§c错误: §4该传送点不存在。");
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
        player.sendMessage("§6使用§c/back§6命令回到死亡地点。");
        if (Config.deathGiveExp != 0) {
            event.setNewLevel(Config.deathGiveExp); //在1.17.1中实测doKeepInventory=true时 此设置无效
        }
    }
}
