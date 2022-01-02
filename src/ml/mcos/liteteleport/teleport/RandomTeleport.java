package ml.mcos.liteteleport.teleport;

import ml.mcos.liteteleport.LiteTeleport;
import ml.mcos.liteteleport.config.Config;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Random;

public class RandomTeleport {

    public static boolean isUnsafeLoc(World world, int x, int y, int z) {
        int mcVersion = LiteTeleport.mcVersion;
        Block block = world.getBlockAt(x, y, z);
        Material type = block.getType();
        if (mcVersion < 10) {
            //如果方块是岩浆、水、仙人掌或空气，视为非安全
            if (type == Material.valueOf("STATIONARY_LAVA") || type == Material.LAVA || type == Material.valueOf("STATIONARY_WATER") || type == Material.WATER || type == Material.CACTUS || block.isEmpty()) {
                return true;
            }
        } else if (mcVersion < 13) {
            //如果方块是岩浆、水、仙人掌、岩浆块或空气，视为非安全
            if (type == Material.valueOf("STATIONARY_LAVA") || type == Material.LAVA || type == Material.valueOf("STATIONARY_WATER") || type == Material.WATER || type == Material.CACTUS || type == Material.valueOf("MAGMA") || block.isEmpty()) {
                return true;
            }
        } else {
            //如果方块是岩浆、水、仙人掌、岩浆块或任何可以自由通过的方块，视为非安全
            if (type == Material.LAVA || type == Material.WATER || type == Material.CACTUS || type == Material.MAGMA_BLOCK || block.isPassable()) {
                return true;
            }
        }
        Block blockR1 = block.getRelative(0, 1, 0);
        Block blockR2 = block.getRelative(0, 2, 0);
        Material typeR1 = blockR1.getType();
        Material typeR2 = blockR2.getType();
        if (mcVersion < 13) {
            if (!blockR1.isEmpty() || !blockR2.isEmpty()) { //如果上面两个方块不是空气 视为非安全
                return true;
            }
        } else {
            if (!blockR1.isPassable() || !blockR2.isPassable()) { //如果上面两个方块不能自由通过 视为非安全
                return true;
            }
            if (typeR1 == Material.LAVA || typeR1 == Material.WATER || typeR2 == Material.LAVA || typeR2 == Material.WATER) { //如果上面两个方块是岩浆或水 视为非安全
                return true;
            }
        }
        return typeR1.toString().toUpperCase().endsWith("FIRE"); //如果上面一个方块是火或者灵魂火，视为非安全
    }

    public static Location getRandomLoc(Player player) {
        Location centerLoc = Config.tprCenter ? player.getWorld().getSpawnLocation() : player.getLocation(), randomLoc = centerLoc.clone();
        boolean flag = true;
        for (int i = 0; i < 14; i++) {
            randomXZ(randomLoc, Config.tprMaxRadius, Config.tprMinRadius);
            randomLoc.setY(player.getWorld().getHighestBlockYAt(randomLoc));
            if (LiteTeleport.mcVersion < 15) {
                randomLoc.setY(randomLoc.getY() - 1);
            }
            if (isUnsafeLoc(player.getWorld(), (int) randomLoc.getX(), (int) randomLoc.getY(), (int) randomLoc.getZ())) {
                randomLoc.setX(centerLoc.getX());
                randomLoc.setZ(centerLoc.getZ());
                continue;
            }
            flag = false;
            break;
        }
        if (flag) {
            return null;
        }
        randomLoc.setX((int) randomLoc.getX() + 0.5);
        randomLoc.setY((int) randomLoc.getY() + 1.0);
        randomLoc.setZ((int) randomLoc.getZ() + 0.5);
        return randomLoc;
    }

    public static Location getRandomLocByNether(Player player) {
        Location centerLoc = Config.tprCenter ? player.getWorld().getSpawnLocation() : player.getLocation(), randomLoc = centerLoc.clone();
        boolean flag = true;
        a:
        for (int i = 0; i < 14; i++) {
            randomXZ(randomLoc, Config.tprMaxRadius, Config.tprMinRadius);
            for (int j = 122; j > 30; j--) {
                randomLoc.setY(j);
                if (isUnsafeLoc(player.getWorld(), (int) randomLoc.getX(), (int) randomLoc.getY(), (int) randomLoc.getZ())) {
                    continue;
                }
                flag = false;
                break a;
            }
            randomLoc.setX(centerLoc.getX());
            randomLoc.setZ(centerLoc.getZ());
        }
        if (flag) {
            return null;
        }
        randomLoc.setX((int) randomLoc.getX() + 0.5);
        randomLoc.setY((int) randomLoc.getY() + 1.0);
        randomLoc.setZ((int) randomLoc.getZ() + 0.5);
        return randomLoc;
    }

    public static void randomXZ(Location loc, int max, int min) {
        Random random = new Random();
        int x = (int) loc.getX(), z = (int) loc.getZ();
        int rx = random.nextInt(max + 1);
        int rz = random.nextInt(max + 1);
        if (Math.max(x + rx, z + rz) < min) {
            if ((random.nextInt(100) + 1) % 2 == 1) {
                rx += min;
            } else {
                rz += min;
            }
        }
        if ((random.nextInt(100) + 1) % 2 == 0) {
            loc.setX(x + rx);
        } else {
            loc.setX(x - rx);
        }
        if ((random.nextInt(100) + 1) % 2 == 1) {
            loc.setZ(z + rz);
        } else {
            loc.setZ(z - rz);
        }
    }
}
