package ml.mcos.liteteleport.consume;

import ml.mcos.liteteleport.LiteTeleport;
import ml.mcos.liteteleport.config.Language;
import org.bukkit.entity.Player;

public class ConsumeInfo {

    public enum ConsumeType {
        LEVEL,
        ECONOMY,
        POINTS
    }

    private final ConsumeType type;
    private final int amount;

    public ConsumeInfo(ConsumeType type, int amount) {
        this.type = type;
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }

    public ConsumeType getType() {
        return type;
    }

    public boolean has(Player player) {
        switch (type) {
            case ECONOMY:
                return LiteTeleport.economy != null && LiteTeleport.economy.has(player, amount);
            case POINTS:
                return LiteTeleport.pointsAPI != null && LiteTeleport.pointsAPI.look(player.getUniqueId()) >= amount;
            default:
                return player.getLevel() >= amount;
        }
    }

    public boolean take(Player player) {
        switch (type) {
            case ECONOMY:
                return LiteTeleport.economy != null && LiteTeleport.economy.withdrawPlayer(player, amount).transactionSuccess();
            case POINTS:
                return LiteTeleport.pointsAPI != null && LiteTeleport.pointsAPI.take(player.getUniqueId(), amount);
            default:
                player.setLevel(player.getLevel() - amount);
                return true;
        }
    }

    public void give(Player player) {
        switch (type) {
            case ECONOMY:
                if (LiteTeleport.economy != null) {
                    LiteTeleport.economy.depositPlayer(player, amount);
                }
                break;
            case POINTS:
                if (LiteTeleport.pointsAPI != null) {
                    LiteTeleport.pointsAPI.give(player.getUniqueId(), amount);
                }
                break;
            default:
                player.setLevel(player.getLevel() + amount);
        }
    }

    public String getDescription() {
        switch (type) {
            case ECONOMY:
                return Language.replaceArgs(Language.consumeInfoDescriptionEconomy, amount);
            case POINTS:
                return Language.replaceArgs(Language.consumeInfoDescriptionPoints, amount);
            default:
                return Language.replaceArgs(Language.consumeInfoDescriptionLevel, amount);
        }
    }

    public String getConsumeName() {
        switch (type) {
            case ECONOMY:
                return Language.consumeInfoNameEconomy;
            case POINTS:
                return Language.consumeInfoNamePoints;
            default:
                return Language.consumeInfoNameLevel;
        }
    }

    public static ConsumeType getConsumeType(String consume) {
        if (consume == null) {
            return ConsumeType.LEVEL;
        }
        switch (consume.charAt(consume.length() - 1)) {
            case 'G':
            case 'g':
                return ConsumeType.ECONOMY;
            case 'P':
            case 'p':
                return ConsumeType.POINTS;
            default:
                return ConsumeType.LEVEL;
        }
    }

    public static int getConsumeAmount(String consume) {
        if (consume == null) {
            return 0;
        }
        char c = consume.charAt(consume.length() - 1);
        if (c >= '0' && c <= '9') {
            return Integer.parseInt(consume);
        } else {
            return Integer.parseInt(consume.substring(0, consume.length() - 1));
        }
    }

    public static double getConsumeAmountDouble(String consume) {
        if (consume == null) {
            return 0.0;
        }
        char c = consume.charAt(consume.length() - 1);
        if (c >= '0' && c <= '9') {
            return Double.parseDouble(consume);
        } else {
            return Double.parseDouble(consume.substring(0, consume.length() - 1));
        }
    }
}
