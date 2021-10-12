package ml.mcos.liteteleport;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"ConstantConditions", "unused"})
public class LiteTeleport extends JavaPlugin implements Listener {
    public static LiteTeleport plugin;
    public HashMap<Player, TeleportRequest> tpList = new HashMap<>();
    public HashMap<Player, Location> backList = new HashMap<>();
    public HashMap<String, List<String>> tabList = new HashMap<>();
    public static int mcVersion;

    @Override
    public void onEnable() {
        plugin = this;
        getServer().getPluginCommand("back").setExecutor(this);
        getServer().getPluginCommand("delhome").setExecutor(this);
        getServer().getPluginCommand("delwarp").setExecutor(this);
        getServer().getPluginCommand("home").setExecutor(this);
        getServer().getPluginCommand("sethome").setExecutor(this);
        getServer().getPluginCommand("setspawn").setExecutor(this);
        getServer().getPluginCommand("setwarp").setExecutor(this);
        getServer().getPluginCommand("spawn").setExecutor(this);
        getServer().getPluginCommand("tpa").setExecutor(this);
        getServer().getPluginCommand("tpacancel").setExecutor(this);
        getServer().getPluginCommand("tpaccept").setExecutor(this);
        getServer().getPluginCommand("tpahere").setExecutor(this);
        getServer().getPluginCommand("tpdeny").setExecutor(this);
        getServer().getPluginCommand("tpr").setExecutor(this);
        getServer().getPluginCommand("warp").setExecutor(this);
        getServer().getPluginManager().registerEvents(this, this);
        initConfig();
        mcVersion = Integer.parseInt(getServer().getClass().getPackage().getName().split("\\.")[3].split("_")[1]);
    }

    public void initConfig() {
        saveDefaultConfig();
        reloadConfig();
        Config.loadConfig(getConfig());
        HomeInfo.loadHomeInfo();
        SpawnInfo.loadSpawnInfo();
        RandomTeleport.loadTprInfo();
        WarpInfo.loadWarpInfo();
    }

    @Override
    @SuppressWarnings("notnull")
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equals("LiteTeleport")) {
            if (args.length == 1) {
                switch (args[0]) {
                    case "version":
                        sender.sendMessage("§a当前版本：§b" + getDescription().getVersion());
                        return true;
                    case "reload":
                        initConfig();
                        sender.sendMessage("§a配置文件重载完成。");
                        return true;
                }
            }
            return false;
        }
        if (sender instanceof Player) {
            Player player = (Player) sender;
            String playerName = player.getName();
            switch (command.getName()) {
                case "home":
                    commandHome(args, player, playerName);
                    break;
                case "back":
                    commandBack(args, player);
                    break;
                case "tpa":
                    commandTpa(args, player);
                    break;
                case "tpaccept":
                    commandTpaccept(args, player);
                    break;
                case "sethome":
                    commandSethome(args, player, playerName);
                    break;
                case "spawn":
                    commandSpawn(args, player);
                    break;
                case "tpahere":
                    commandTpahere(args, player);
                    break;
                case "tpdeny":
                    commandTpdeny(args, player);
                    break;
                case "delhome":
                    commandDelhome(args, player, playerName);
                    break;
                case "tpacancel":
                    commandTpacancel(args, player);
                    break;
                case "tpr":
                    commandTpr(args, player, playerName);
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
            sender.sendMessage("§c错误：§6本命令只能玩家使用。");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (tabList.isEmpty()) {
            tabList.put("LiteTeleport", Arrays.asList("version", "reload"));
            tabList.put("tpa", new ArrayList<>());
            tabList.put("tpahere", new ArrayList<>());
        }
        if (sender instanceof Player) {
            Player player = (Player) sender;
            switch (command.getName()) {
                case "home":
                case "delhome":
                    if (args.length == 1) {
                        return getTabList(args, HomeInfo.getHomeList(player.getName()));
                    }
                    break;
                case "warp":
                case "delwarp":
                    if (args.length == 1) {
                        return getTabList(args, WarpInfo.getWarpList());
                    }
            }
        }
        return getTabList(args, tabList.get(getTabPath(args, command.getName())));
    }

    private static String getTabPath(String[] args, String command) {
        StringBuilder builder = new StringBuilder(command);
        for (int i = 1; i < args.length; i++) {
            builder.append(".").append(args[i - 1].toLowerCase());
        }
        return builder.toString();
    }

    private static List<String> getTabList(String[] args, List<String> list) {
        List<String> ret = new ArrayList<>();
        if (list == null) {
            return ret;
        } else if (list.isEmpty()) {
            return null; //返回null时 游戏会用线玩家的名字列表作为候选
        } else if (args[args.length - 1].equals("")) {
            return list;
        }
        String arg = args[args.length - 1].toLowerCase();
        for (String value : list) {
            if (value.startsWith(arg)) {
                ret.add(value);
            }
        }
        return ret;
    }

    private void commandBack(String[] args, Player player) {
        if (args.length != 0) {
            player.sendMessage("§c错误：本命令没有参数，请使用§6/back");
            return;
        }
        Location backLoc = backList.get(player);
        if (backLoc == null) {
            player.sendMessage("§c错误：§6没有上一位置可以回去。");
            return;
        }
        player.sendMessage("§c本次传送将花费§a" + Config.backConsumeI + "§c级经验。");
        if (Config.backConsumeI != 0) {
            if (player.getLevel() < Config.backConsumeI) {
                player.sendMessage("§c错误：§4你没有足够的等级支付本次传送花费。");
                return;
            } else {
                player.setLevel(player.getLevel() - Config.backConsumeI);
            }
        }
        backList.put(player, player.getLocation());
        player.sendMessage("§6正在回到上一位置...");
        player.teleport(backLoc);
    }

    private void commandDelhome(String[] args, Player player, String playerName) {
        String homeName;
        if (args.length != 1) {
            player.sendMessage("§c错误：请使用§6/delhome [名字]");
            return;
        } else {
            homeName = args[0];
        }
        if (HomeInfo.isBanName(homeName)) {
            player.sendMessage("§c错误：§4无效的家名称!");
        } else if (HomeInfo.exist(playerName, homeName)) {
            HomeInfo.deleteHome(playerName, homeName);
            player.sendMessage("§6家§c" + homeName + "§6已被移除。");
        } else {
            player.sendMessage("§c错误：§4家§c" + homeName + "§4不存在!");

        }
    }

    private void commandDelwarp(String[] args, Player player) {
        if (args.length != 1) {
            player.sendMessage("§c错误：请使用§6/delwarp <名字>");
        } else if (WarpInfo.isBanName(args[0])) {
            player.sendMessage("§c错误：§4无效的传送点名称!");
        } else if (WarpInfo.exist(args[0])) {
            WarpInfo.deleteWarp(args[0]);
            player.sendMessage("§6传送点§c" + args[0] + "§6已被移除。");
        } else {
            player.sendMessage("§c错误：§4该传送点不存在。");
        }
    }

    private void commandHome(String[] args, Player player, String playerName) {
        String homeName;
        if (args.length > 1) {
            player.sendMessage("§c错误：请使用§6/home [名字]");
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
            player.sendMessage("§c本次传送将花费§a" + Config.homeConsumeI + "§c级经验。");
            if (Config.homeConsumeI != 0) {
                if (player.getLevel() < Config.homeConsumeI) {
                    player.sendMessage("§c错误：§4你没有足够的等级支付本次传送花费。");
                    return;
                } else {
                    player.setLevel(player.getLevel() - Config.homeConsumeI);
                }
            }
            backList.put(player, player.getLocation());
            //player.sendMessage("§6正在传送..");
            player.sendMessage("§6传送到§c" + homeName + "§6。");
            player.teleport(HomeInfo.getHomeLocation(playerName, homeName));
        } else {
            player.sendMessage(HomeInfo.showHomeList(playerName));
        }
    }

    private void commandSethome(String[] args, Player player, String playerName) {
        String homeName;
        if (args.length > 1) {
            player.sendMessage("§c错误：请使用§6/sethome [名字]");
            return;
        } else if (args.length == 0) {
            homeName = "home";
        } else {
            homeName = args[0];
        }
        if (HomeInfo.isBanName(homeName)) {
            player.sendMessage("§c错误：§4无效的家名称!");
            return;
        }
        if (Config.sethomeConsumeD != 0) {
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
            int consume = (int) Math.pow(n, Config.sethomeConsumeD);
            if (Config.sethomeMaxConsumeI != 0 && consume > Config.sethomeMaxConsumeI) {
                consume = Config.sethomeMaxConsumeI;
            }
            player.sendMessage("§c设置你的第§9" + n + "§c个家将花费§a" + consume + "§c级经验。");
            if (player.getLevel() < consume) {
                player.sendMessage("§c错误：§4你没有足够的等级支付本次设置花费。");
                return;
            } else {
                player.setLevel(player.getLevel() - consume);
            }
        }
        HomeInfo.saveHome(playerName, homeName, player.getLocation());
        player.sendMessage("§6已设置家。");
    }

    private void commandSetspawn(Player player) {
        player.getWorld().setSpawnLocation(player.getLocation());
        SpawnInfo.setSpawnWorld(player.getWorld().getName());
        player.sendMessage("§6已将世界出生点设为当前位置。");
    }

    private void commandSetwarp(String[] args, Player player) {
        if (args.length != 1) {
            player.sendMessage("§c错误：请使用§6/setwarp <名字>");
        } else if (WarpInfo.isBanName(args[0])) {
            player.sendMessage("§c错误：§4无效的传送点名称!");
        } else {
            WarpInfo.setWarpLocation(args[0], player.getLocation());
            player.sendMessage("§6已设置传送点§c" + args[0] + "§6。");
        }
    }

    private void commandSpawn(String[] args, Player player) {
        if (args.length != 0) {
            player.sendMessage("§c错误：本命令没有参数，请使用§6/spawn");
            return;
        }
        player.sendMessage("§c本次传送将花费§a" + Config.spawnConsumeI + "§c级经验。");
        if (Config.spawnConsumeI != 0) {
            if (player.getLevel() < Config.spawnConsumeI) {
                player.sendMessage("§c错误：§4你没有足够的等级支付本次传送花费。");
                return;
            } else {
                player.setLevel(player.getLevel() - Config.spawnConsumeI);
            }
        }
        backList.put(player, player.getLocation());
        player.sendMessage("§6正在传送...");
        player.teleport(getServer().getWorld(SpawnInfo.getSpawnWorld()).getSpawnLocation());
    }

    private void commandTpa(String[] args, Player player) {
        Player target;
        if (args.length != 1 || player.getName().equals(args[0])) {
            player.sendMessage("§c错误：请使用§6/tpa <玩家>");
            return;
        }
        target = getServer().getPlayer(args[0]);
        if (target == null) {
            player.sendMessage("§c错误：§4未找到该玩家。");
            return;
        }
        tpList.put(target, new TeleportRequest(player));
        target.sendMessage("§c" + player.getDisplayName() + "§6请求传送到你这里。");
        target.sendMessage("§6若想接受传送，输入§c/tpaccept");
        target.sendMessage("§6若想拒绝传送，输入§c/tpdeny");
        target.sendMessage("§c接受此请求将花费§a" + Config.tpAcceptConsumeI + "§c级经验。");
        player.sendMessage("§6请求已发送给§c" + target.getDisplayName() + "。");
        player.sendMessage("§6若要取消这个请求，请输入§c/tpacancel");
        player.sendMessage("§c传送时将花费§a" + Config.tpSourceConsumeI + "§c级经验。");
    }

    private void commandTpacancel(String[] args, Player player) {
        Player target = null;
        if (args.length != 0) {
            player.sendMessage("§c错误：本命令没有参数，请使用§6/tpacancel");
            return;
        }
        for (Map.Entry<Player, TeleportRequest> entry : tpList.entrySet()) {
            if (entry.getValue().source == player) {
                target = entry.getKey();
                break;
            }
        }
        if (target == null) {
            player.sendMessage("§c错误：§4你没有待处理的请求。");
        } else {
            tpList.remove(target);
            player.sendMessage("§6传送请求已被取消。");
        }
    }

    private void commandTpaccept(String[] args, Player player) {
        TeleportRequest tpRequest;
        if (args.length != 0) {
            player.sendMessage("§c错误：本命令没有参数，请使用§6/tpaccept");
            return;
        }
        tpRequest = tpList.remove(player);
        if (tpRequest == null) {
            player.sendMessage("§c错误：§4你没有待处理的请求。");
            return;
        }
        if (player.getLevel() < Config.tpAcceptConsumeI) {
            player.sendMessage("§c错误：§4你没有足够的等级来接受此请求。");
            tpRequest.source.sendMessage("§c错误：§4对方没有足够的等级来接受此请求。");
            return;
        } else if (tpRequest.source.getLevel() < Config.tpSourceConsumeI) {
            player.sendMessage("§c错误：§4对方没有足够的等级支付本次传送花费。");
            tpRequest.source.sendMessage("§c错误：§4你没有足够的等级支付本次传送花费。");
            return;
        } else {
            if (Config.tpAcceptConsumeI != 0) {
                player.setLevel(player.getLevel() - Config.tpAcceptConsumeI);
            }
            if (Config.tpSourceConsumeI != 0) {
                tpRequest.source.setLevel(tpRequest.source.getLevel() - Config.tpSourceConsumeI);
            }
        }
        tpRequest.source.sendMessage("§c" + player.getDisplayName() + "§6接受了你的传送请求。");
        if (tpRequest.teleportType == 0) { //发起者传送到接受者
            player.sendMessage("§6已接受传送请求。");
            backList.put(tpRequest.source, tpRequest.source.getLocation());
            tpRequest.source.sendMessage("§6正在传送至§c" + player.getDisplayName() + "§6。");
            tpRequest.source.teleport(player.getLocation());
        } else { //接受者传送到发起者
            backList.put(player, player.getLocation());
            player.sendMessage("§6正在传送...");
            player.teleport(tpRequest.location);
        }
    }

    private void commandTpahere(String[] args, Player player) {
        Player target;
        if (args.length != 1 || player.getName().equals(args[0])) {
            player.sendMessage("§c错误：请使用§6/tpahere <玩家>");
            return;
        }
        target = getServer().getPlayer(args[0]);
        if (target == null) {
            player.sendMessage("§c错误：§4未找到该玩家。");
            return;
        }
        tpList.put(target, new TeleportRequest(1, player, player.getLocation()));
        target.sendMessage("§c" + player.getDisplayName() + "§6请求你传送到他那里。");
        target.sendMessage("§6若想接受传送，输入§c/tpaccept");
        target.sendMessage("§6若想拒绝传送，输入§c/tpdeny");
        target.sendMessage("§c接受此请求将花费§a" + Config.tpAcceptConsumeI + "§c级经验。");
        player.sendMessage("§6请求已发送给§c" + target.getDisplayName() + "§6。");
        player.sendMessage("§6若要取消这个请求，请输入§c/tpacancel");
        player.sendMessage("§c传送时将花费§a" + Config.tpSourceConsumeI + "§c级经验。");
    }

    private void commandTpdeny(String[] args, Player player) {
        TeleportRequest tpRequest;
        if (args.length != 0) {
            player.sendMessage("§c错误：本命令没有参数，请使用§6/tpdeny");
            return;
        }
        tpRequest = tpList.remove(player);
        if (tpRequest == null) {
            player.sendMessage("§c错误：§4你没有待处理的请求。");
        } else {
            tpRequest.source.sendMessage("§c" + player.getDisplayName() + "§6拒绝了你的传送请求。");
            player.sendMessage("§6已拒绝传送请求。");
        }
    }

    private void commandTpr(String[] args, Player player, String playerName) {
        if (args.length != 0) {
            player.sendMessage("§c错误：请使用§6/tpr");
            return;
        }
        if (!Config.allowTprWorld.contains(player.getWorld().getName())) {
            player.sendMessage("§c错误：§4你所在的世界不允许使用随机传送。");
            return;
        }
        int consume = 0, n = RandomTeleport.getTprCount(playerName) + 1;
        if (Config.tprConsumeD != 0) {
            if (n == 1) {
                consume = Config.firstTprConsumeI;
            } else {
                consume = (int) Math.pow(n, Config.tprConsumeD);
                if (Config.tprMaxConsumeI != 0 && consume > Config.tprMaxConsumeI) {
                    consume = Config.tprMaxConsumeI;
                }
            }
            player.sendMessage("§c第§3" + n + "§c次随机传送将花费§a" + consume + "§c级经验。");
            if (player.getLevel() < consume) {
                player.sendMessage("§c错误：§4你没有足够的等级支付本次传送花费。");
                return;
            } else {
                player.setLevel(player.getLevel() - consume);
            }
        }
        Location loc;
        if (player.getWorld().getEnvironment() == World.Environment.NETHER) {
            if (mcVersion > 10) { //1.10没有sendTitle 而且1.10及以下版本区块加载比高版本快很多 不需要提示等待
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
            player.sendMessage("§c错误：§4未找到安全位置，请重试。");
            if (Config.tprConsumeD != 0) {
                player.setLevel(player.getLevel() + consume);
            }
            return;
        }
        RandomTeleport.setTprCount(playerName, n);
        backList.put(player, player.getLocation());
        player.sendMessage("§6正在传送...");
        player.teleport(loc);
    }

    private void commandWarp(String[] args, Player player) {
        if (args.length > 1) {
            player.sendMessage("§c错误：请使用§6/warp [名字]");
            return;
        } else if (args.length == 0) {
            player.sendMessage(WarpInfo.showWarpList());
            return;
        }
        if (WarpInfo.exist(args[0])) {
            player.sendMessage("§c本次传送将花费§a" + Config.warpConsumeI + "§c级经验。");
            if (Config.warpConsumeI != 0) {
                if (player.getLevel() < Config.warpConsumeI) {
                    player.sendMessage("§c错误：§4你没有足够的等级支付本次传送花费。");
                    return;
                } else {
                    player.setLevel(player.getLevel() - Config.warpConsumeI);
                }
            }
            backList.put(player, player.getLocation());
            player.sendMessage("§6传送到§c" + args[0] + "§6。");
            player.teleport(WarpInfo.getWarpLocation(args[0]));
        } else {
            player.sendMessage("§c错误：§4该传送点不存在。");
        }
    }

    @EventHandler
    public void playerDeathEvent(PlayerDeathEvent event) {
        Player player = event.getEntity();
        backList.put(player, player.getLocation());
        player.sendMessage("§6使用§c/back§6命令回到死亡地点。");
        /*// 在没有其他插件干预的情况下 下面各项结果：
        System.out.println("getKeepInventory: " + event.getKeepInventory()); //doKeepInventory=true时 此值为true
        System.out.println("getKeepLevel: " + event.getKeepLevel()); //doKeepInventory=true时 此值在1.13.2中为false 在1.17.1中为true
        System.out.println("getNewLevel: " + event.getNewLevel()); //无论doKeepInventory是否为true 此值都是0
        System.out.println("getNewExp: " + event.getNewExp()); //无论doKeepInventory是否为true 此值都是0
        System.out.println("getNewTotalExp: " + event.getNewTotalExp()); //无论doKeepInventory是否为true 此值都是0
        System.out.println("getDroppedExp: " + event.getDroppedExp()); //无论doKeepInventory是否为true 此值都是应该掉落的经验值 最高为100*/
        if (Config.deathGiveExpI != 0) {
            event.setNewLevel(Config.deathGiveExpI); //在1.17.1中实测doKeepInventory=true时 此设置无效
        }
    }

    @EventHandler
    public void playerJoinEvent(PlayerJoinEvent event) {
        for (Map.Entry<Player, Location> ele : backList.entrySet()) {
            if (ele.getKey().getName().equals(event.getPlayer().getName())) {
                backList.remove(ele.getKey());
                backList.put(event.getPlayer(), ele.getValue());
                return;
            }
        }
    }
}
