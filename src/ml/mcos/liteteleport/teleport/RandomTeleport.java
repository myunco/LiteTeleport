package ml.mcos.liteteleport.teleport;

import com.mojang.datafixers.kinds.IdF;
import ml.mcos.liteteleport.LiteTeleport;
import ml.mcos.liteteleport.config.Config;
import ml.mcos.liteteleport.util.Version;
import net.myunco.folia.scheduler.CompatibleScheduler;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class RandomTeleport {
    private static final Version mcVersion = LiteTeleport.mcVersion;
    private static Material STATIONARY_WATER;
    private static Material STATIONARY_LAVA;
    private static Material MAGMA;

    static {
        if (mcVersion.isLessThan(13)) {
            STATIONARY_WATER = Material.valueOf("STATIONARY_WATER");
            STATIONARY_LAVA = Material.valueOf("STATIONARY_LAVA");
            if (mcVersion.isGreaterThan(9)) {
                MAGMA = Material.valueOf("MAGMA");
            }
        }
    }

    private static boolean isUnsafeLoc(Block block) {
        Material type = block.getType();
        // System.out.println("type = " + type); //TODO remove
        // System.out.println("x = " + block.getX() + ", y = " + block.getY() + ", z = " + block.getZ()); //TODO remove

        if (type == Material.AIR) {
            // System.out.println("不安全原因：方块是空气"); //TODO remove
            return true;
        }
        // 如果方块是水
        if (blockIsWater(type)) {
            //如果允许传送到水中，则跳过此项检查
            if (!Config.tprAllowWater) {
                return true;
            }
        } else {
            if (mcVersion.isLessThan(10)) {
                //如果方块是岩浆、仙人掌或空气，视为非安全
                if (type == STATIONARY_LAVA || type == Material.LAVA || type == Material.CACTUS || block.isEmpty()) {
                    return true;
                }
            } else if (mcVersion.isLessThan(13)) {
                //如果方块是岩浆、仙人掌、岩浆块或空气，视为非安全
                if (type == STATIONARY_LAVA || type == Material.LAVA || type == Material.CACTUS || type == MAGMA || block.isEmpty()) {
                    return true;
                }
            } else {
                //如果方块是岩浆、仙人掌、岩浆块或任何可以自由通过的方块，视为非安全
                if (type == Material.LAVA || type == Material.CACTUS || type == Material.MAGMA_BLOCK || block.isPassable()) {
                    // System.out.println("不安全原因：岩浆、水、仙人掌、岩浆块或方块可以自由通过"); //TODO remove
                    return true;
                }
            }
        }
        Block blockR1 = block.getRelative(0, 1, 0);
        Block blockR2 = block.getRelative(0, 2, 0);
        Material typeR1 = blockR1.getType();
        Material typeR2 = blockR2.getType();
        if (mcVersion.isLessThan(13)) {
            if (!blockR1.isEmpty() || !blockR2.isEmpty()) { //如果上面两个方块不是空气 视为非安全
                // System.out.println("不安全原因：上面两个方块不是空气"); //TODO remove
                return true;
            }
        } else {
            if (!blockR1.isPassable() || !blockR2.isPassable()) { //如果上面两个方块不能自由通过 视为非安全
                // System.out.println("不安全原因：上面两个方块不能自由通过"); //TODO remove
                return true;
            }
            if (typeR1 == Material.LAVA || typeR1 == Material.WATER || typeR2 == Material.LAVA || typeR2 == Material.WATER) { //如果上面两个方块是岩浆或水 视为非安全
                // System.out.println("不安全原因：上面两个方块是岩浆或水"); //TODO remove
                return true;
            }
        }
        return typeR1.toString().endsWith("FIRE"); //如果上面一个方块是火或者灵魂火，视为非安全
    }

    public static Location getRandomLoc(Player player, CompatibleScheduler scheduler) {
        final Location centerLoc = Config.tprCenter ? player.getWorld().getSpawnLocation() : player.getLocation();
        centerLoc.setX((int) centerLoc.getX());
        centerLoc.setZ((int) centerLoc.getZ());
        final Location randomLoc = centerLoc.clone();
        boolean flag = true;
        for (int i = 0; i < 14; i++) { //尝试生成14次随机位置
            if (Config.tprMode) {
                randomRectangular(randomLoc, Config.tprMinRadius, Config.tprMaxRadius);
            } else {
                randomCircular(randomLoc, Config.tprMinRadius, Config.tprMaxRadius);
            }
            final CompletableFuture<Boolean> future = new CompletableFuture<>();
            scheduler.runTask(() -> {
                System.out.println("randomLoc.x = " + randomLoc.getX() + ", z = " + randomLoc.getZ()); //TODO remove
                randomLoc.setY(player.getWorld().getHighestBlockYAt(randomLoc));
                if (mcVersion.isLessThan(15)) {
                    randomLoc.setY(randomLoc.getY() - 1);
                }
                if (isUnsafeLoc(randomLoc.getBlock())) {
                    // 如果这个位置不安全 先尝试遍历整个区块内的位置
                    System.out.println("尝试在同一区块内找到安全位置"); //TODO remove
                    int cx = randomLoc.getChunk().getX() * 16;
                    int cz = randomLoc.getChunk().getZ() * 16;
                    for (int x = cx; x < cx + 16; x++) {
                        for (int z = cz; z < cz + 16; z++) {
                            randomLoc.setX(x);
                            randomLoc.setZ(z);
                            randomLoc.setY(player.getWorld().getHighestBlockYAt(x, z));
                            if (mcVersion.isLessThan(15)) {
                                randomLoc.setY(randomLoc.getY() - 1);
                            }
                            if (isUnsafeLoc(randomLoc.getBlock())) {
                                continue;
                            }
                            System.out.println("在同一区块内找到安全位置"); //TODO remove
                            future.complete(true); //找到安全位置
                            return;
                        }
                    }
                    System.out.println("同一区块内未找到安全位置 再次随机"); //TODO remove
                    //如果随机位置不安全 重置X、Z坐标
                    randomLoc.setX(centerLoc.getX());
                    randomLoc.setZ(centerLoc.getZ());
                    future.complete(false); //未找到安全位置
                    return;
                }
                System.out.println("randomLoc.getBlock().toString() = " + randomLoc.getBlock()); //TODO remove
                if (blockIsWater(randomLoc.getBlock().getType())) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, Config.tprWaterBreathing * 20, 0), true);
                }
                System.out.println("一次就找到安全位置"); //TODO remove
                future.complete(true); //找到安全位置
            }, randomLoc);
            boolean result = future.join();
            if (!result) {
                continue;
            }
            flag = false;
            break;
        }
        if (flag) {
            return null;
        }
        randomLoc.add(0.5, 1.0, 0.5);
        System.out.println("randomLoc = " + randomLoc); //TODO remove
        return randomLoc;
    }

    private static boolean blockIsWater(Material type) {
        if (mcVersion.isLessThan(13) && type == STATIONARY_WATER) {
            return true;
        }
        return type == Material.WATER;
    }

    public static Location getRandomLocByNether(Player player, CompatibleScheduler scheduler) {
        final Location centerLoc = Config.tprCenter ? player.getWorld().getSpawnLocation() : player.getLocation();
        centerLoc.setX((int) centerLoc.getX());
        centerLoc.setZ((int) centerLoc.getZ());
        final Location randomLoc = centerLoc.clone();
        boolean flag = true;
        for (int i = 0; i < 14; i++) { //尝试14次
            if (Config.tprMode) {
                randomRectangular(randomLoc, Config.tprMinRadius, Config.tprMaxRadius);
            } else {
                randomCircular(randomLoc, Config.tprMinRadius, Config.tprMaxRadius);
            }
            final CompletableFuture<Boolean> future = new CompletableFuture<>();
            scheduler.runTask(() -> {
                for (int y = 122; y > 30; y--) { //由于下界的特殊地形 从Y=122到Y=31 逐个检查安全位置
                    randomLoc.setY(y);
                    if (isUnsafeLoc(randomLoc.getBlock())) {
                        continue;
                    }
                    System.out.println("一次就找到安全位置"); //TODO remove
                    future.complete(true); //找到安全位置
                    return;
                }
                // 如果这个位置不安全 先尝试遍历整个区块内的位置
                System.out.println("尝试在同一区块内找到安全位置"); //TODO remove
                int cx = randomLoc.getChunk().getX() * 16;
                int cz = randomLoc.getChunk().getZ() * 16;
                for (int x = cx; x < cx + 16; x++) {
                    for (int z = cz; z < cz + 16; z++) {
                        randomLoc.setX(x);
                        randomLoc.setZ(z);
                        for (int y = 122; y > 30; y--) {
                            randomLoc.setY(y);
                            if (isUnsafeLoc(randomLoc.getBlock())) {
                                continue;
                            }
                            System.out.println("在同一区块内找到安全位置"); //TODO remove
                            future.complete(true); //找到安全位置
                            return;
                        }
                    }
                }
                System.out.println("同一区块内未找到安全位置 再次随机"); //TODO remove
                future.complete(false); //未找到安全位置
            }, randomLoc);
            boolean result = future.join();
            if (result) { //找到安全位置 跳出循环
                flag = false;
                break;
            }
            //未找到安全位置 重置X、Z坐标
            randomLoc.setX(centerLoc.getX());
            randomLoc.setZ(centerLoc.getZ());
        }
        if (flag) { //循环结束 未找到安全位置
            return null;
        }
        randomLoc.add(0.5, 1.0, 0.5);
        return randomLoc;
    }

    private static void randomRectangular(Location center, int min, int max) {
        Random random = new Random();
        int rx = randomOffset(random, max);
        int rz = randomOffset(random, max);
        if (Math.max(Math.abs(rx), Math.abs(rz)) < min) {
            if (random.nextBoolean()) {
                rx = randomOffset(random, min, max);
            } else {
                rz = randomOffset(random, min, max);
            }
        }
        center.add(rx, 0, rz);
    }

    private static int randomOffset(Random random, int max) {
        int offset = random.nextInt(max + 1);
        return random.nextBoolean() ? offset : -offset;
    }

    private static int randomOffset(Random random, int min, int max) {
        int offset = min + random.nextInt(max - min + 1);
        return random.nextBoolean() ? offset : -offset;
    }

    private static void randomCircular(Location center, int min, int max) {
        Random random = new Random();
        int minSq = min * min;
        int maxSq = max * max;
        // double radius = min + (max - min) * random.nextDouble();
        double radius = Math.sqrt(random.nextDouble() * (maxSq - minSq) + minSq);
        double angle = random.nextDouble() * 2 * Math.PI;
        int rx = (int) Math.round(radius * Math.cos(angle));
        int rz = (int) Math.round(radius * Math.sin(angle));
        center.add(rx, 0, rz);
    }

}
