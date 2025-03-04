package ml.mcos.liteteleport.util;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;

import java.util.List;

/**
 * 由于Vault 1.4之前的版本没有使用OfflinePlayer player为参数的方法，所以本类对旧版本的Economy接口进行兼容
 * 当为旧版本时，本类会自动以player.getName()调用同名的使用String playerName的方法
 */
@SuppressWarnings("deprecation")
public class CompatibleEconomy implements Economy {
    private final Economy economy;
    private final boolean old;

    public CompatibleEconomy(Economy economy) {
        this.economy = economy;
        boolean isOld = false;
        try {
            Economy.class.getMethod("hasAccount", OfflinePlayer.class);
        } catch (Exception e) {
            isOld = true;
        }
        old = isOld;
    }

    @Override
    public boolean isEnabled() {
        return economy.isEnabled();
    }

    @Override
    public String getName() {
        return economy.getName();
    }

    @Override
    public boolean hasBankSupport() {
        return economy.hasBankSupport();
    }

    @Override
    public int fractionalDigits() {
        return economy.fractionalDigits();
    }

    @Override
    public String format(double amount) {
        return economy.format(amount);
    }

    @Override
    public String currencyNamePlural() {
        return economy.currencyNamePlural();
    }

    @Override
    public String currencyNameSingular() {
        return economy.currencyNameSingular();
    }

    @Override
    @Deprecated
    public boolean hasAccount(String playerName) {
        return economy.hasAccount(playerName);
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return old ? economy.hasAccount(player.getName()) : economy.hasAccount(player);
    }

    @Override
    @Deprecated
    public boolean hasAccount(String playerName, String worldName) {
        return economy.hasAccount(playerName, worldName);
    }

    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return old ? economy.hasAccount(player.getName(), worldName) : economy.hasAccount(player, worldName);
    }

    @Override
    @Deprecated
    public double getBalance(String playerName) {
        return economy.getBalance(playerName);
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return old ? economy.getBalance(player.getName()) : economy.getBalance(player);
    }

    @Override
    @Deprecated
    public double getBalance(String playerName, String worldName) {
        return economy.getBalance(playerName, worldName);
    }

    @Override
    public double getBalance(OfflinePlayer player, String worldName) {
        return old ? economy.getBalance(player.getName(), worldName) : economy.getBalance(player, worldName);
    }

    @Override
    @Deprecated
    public boolean has(String playerName, double amount) {
        return economy.has(playerName, amount);
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return old ? economy.has(player.getName(), amount) : economy.has(player, amount);
    }

    @Override
    @Deprecated
    public boolean has(String playerName, String worldName, double amount) {
        return economy.has(playerName, worldName, amount);
    }

    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return old ? economy.has(player.getName(), worldName, amount) : economy.has(player, worldName, amount);
    }

    @Override
    @Deprecated
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        return economy.withdrawPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        return old ? economy.withdrawPlayer(player.getName(), amount) : economy.withdrawPlayer(player, amount);
    }

    @Override
    @Deprecated
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return economy.withdrawPlayer(playerName, worldName, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return old ? economy.withdrawPlayer(player.getName(), worldName, amount) : economy.withdrawPlayer(player, worldName, amount);
    }

    @Override
    @Deprecated
    public EconomyResponse depositPlayer(String playerName, double amount) {
        return economy.depositPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        return old ? economy.depositPlayer(player.getName(), amount) : economy.depositPlayer(player, amount);
    }

    @Override
    @Deprecated
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return economy.depositPlayer(playerName, worldName, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return old ? economy.depositPlayer(player.getName(), worldName, amount) : economy.depositPlayer(player, worldName, amount);
    }

    @Override
    @Deprecated
    public EconomyResponse createBank(String name, String worldName) {
        return economy.createBank(name, worldName);
    }

    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player) {
        return old ? economy.createBank(name, player.getName()) : economy.createBank(name, player);
    }

    @Override
    public EconomyResponse deleteBank(String name) {
        return economy.deleteBank(name);
    }

    @Override
    public EconomyResponse bankBalance(String name) {
        return economy.bankBalance(name);
    }

    @Override
    public EconomyResponse bankHas(String name, double amount) {
        return economy.bankHas(name, amount);
    }

    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        return economy.bankWithdraw(name, amount);
    }

    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        return economy.bankDeposit(name, amount);
    }

    @Override
    @Deprecated
    public EconomyResponse isBankOwner(String name, String playerName) {
        return economy.isBankOwner(name, playerName);
    }

    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        return old ? economy.isBankOwner(name, player.getName()) : economy.isBankOwner(name, player);
    }

    @Override
    @Deprecated
    public EconomyResponse isBankMember(String name, String playerName) {
        return economy.isBankMember(name, playerName);
    }

    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer player) {
        return old ? economy.isBankMember(name, player.getName()) : economy.isBankMember(name, player);
    }

    @Override
    public List<String> getBanks() {
        return economy.getBanks();
    }

    @Override
    @Deprecated
    public boolean createPlayerAccount(String playerName) {
        return economy.createPlayerAccount(playerName);
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        return old ? economy.createPlayerAccount(player.getName()) : economy.createPlayerAccount(player);
    }

    @Override
    @Deprecated
    public boolean createPlayerAccount(String playerName, String worldName) {
        return economy.createPlayerAccount(playerName, worldName);
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        return old ? economy.createPlayerAccount(player.getName(), worldName) : economy.createPlayerAccount(player, worldName);
    }
}
