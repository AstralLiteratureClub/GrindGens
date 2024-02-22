/*
 * Copyright (C) 2024 Astral.bet - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package bet.astral.grindgens;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class VaultEconomy implements Economy {
	private final GrindGens grindGens;
	private final Economy economy;

	public VaultEconomy(GrindGens grindGens) {
		this.grindGens = grindGens;
		this.economy = connect();
	}

	private @Nullable Economy connect(){
		if (grindGens.getServer().getPluginManager().getPlugin("Vault") == null){
			return null;
		}
		RegisteredServiceProvider<Economy> rsp = grindGens.getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null){
			return null;
		}
		return rsp.getProvider();
	}

	@Override
	public boolean isEnabled() {
		return economy != null && economy.isEnabled();
	}

	@Override
	public String getName() {
		return economy != null ? economy.getName() : null;
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
		if (economy == null){
			return "$"+amount;
		}
		return economy.format(amount);
	}

	@Override
	public String currencyNamePlural() {
		if (economy == null){
			return "$";
		}
		return economy.currencyNamePlural();
	}

	@Override
	public String currencyNameSingular() {
		if (economy == null){
			return "$";
		}
		return economy.currencyNameSingular();
	}

	@Override
	public boolean hasAccount(String playerName) {
		return economy.hasAccount(playerName);
	}

	@Override
	public boolean hasAccount(OfflinePlayer player) {
		return economy.hasAccount(player);
	}

	@Override
	public boolean hasAccount(String playerName, String worldName) {
		return economy.hasAccount(playerName, worldName);
	}

	@Override
	public boolean hasAccount(OfflinePlayer player, String worldName) {
		return economy.hasAccount(player, worldName);
	}

	@Override
	public double getBalance(String playerName) {
		return economy.getBalance(playerName);
	}

	@Override
	public double getBalance(OfflinePlayer player) {
		return economy.getBalance(player);
	}

	@Override
	public double getBalance(String playerName, String world) {
		return economy.getBalance(playerName, world);
	}

	@Override
	public double getBalance(OfflinePlayer player, String world) {
		return economy.getBalance(player, world);
	}

	@Override
	public boolean has(String playerName, double amount) {
		return economy.has(playerName, amount);
	}

	@Override
	public boolean has(OfflinePlayer player, double amount) {
		return economy.has(player, amount);
	}

	@Override
	public boolean has(String playerName, String worldName, double amount) {
		return economy.has(playerName, worldName, amount);
	}

	@Override
	public boolean has(OfflinePlayer player, String worldName, double amount) {
		return economy.has(player, worldName, amount);
	}

	@Override
	public EconomyResponse withdrawPlayer(String playerName, double amount) {
		return economy.withdrawPlayer(playerName, amount);
	}

	@Override
	public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
		return economy.withdrawPlayer(player, amount);
	}

	@Override
	public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
		return economy.withdrawPlayer(playerName, worldName, amount);
	}

	@Override
	public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
		return economy.withdrawPlayer(player, worldName, amount);
	}

	@Override
	public EconomyResponse depositPlayer(String playerName, double amount) {
		return economy.depositPlayer(playerName, amount);
	}

	@Override
	public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
		return economy.depositPlayer(player, amount);
	}

	@Override
	public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
		return economy.depositPlayer(playerName, worldName, amount);
	}

	@Override
	public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
		return economy.depositPlayer(player, worldName, amount);
	}

	@Override
	public EconomyResponse createBank(String name, String player) {
		return economy.createBank(name, player);
	}

	@Override
	public EconomyResponse createBank(String name, OfflinePlayer player) {
		return economy.createBank(name, player);
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
	public EconomyResponse isBankOwner(String name, String playerName) {
		return economy.isBankOwner(name, playerName);
	}

	@Override
	public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
		return economy.isBankOwner(name, player);
	}

	@Override
	public EconomyResponse isBankMember(String name, String playerName) {
		return economy.isBankMember(name, playerName);
	}

	@Override
	public EconomyResponse isBankMember(String name, OfflinePlayer player) {
		return economy.isBankMember(name, player);
	}

	@Override
	public List<String> getBanks() {
		return economy.getBanks();
	}

	@Override
	public boolean createPlayerAccount(String playerName) {
		return economy.createPlayerAccount(playerName);
	}

	@Override
	public boolean createPlayerAccount(OfflinePlayer player) {
		return economy.createPlayerAccount(player);
	}

	@Override
	public boolean createPlayerAccount(String playerName, String worldName) {
		return economy.createPlayerAccount(playerName, worldName);
	}

	@Override
	public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
		return economy.createPlayerAccount(player, worldName);
	}
}
