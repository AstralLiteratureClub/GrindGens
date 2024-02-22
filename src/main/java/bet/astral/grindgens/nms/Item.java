/*
 * Copyright (C) 2024 Astral.bet - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 */

package bet.astral.grindgens.nms;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Bukkit;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Item {
	private static final String CRAFTBUKKIT_PACKAGE = Bukkit.getServer().getClass().getPackage().getName();

	public static String cbClass(String clazz) {
		return CRAFTBUKKIT_PACKAGE + "." + clazz;
	}
	public static Method reflectMethod(String className, String name, Class<?>... params){
		Class<?> clazz = null;
		try {
			clazz = Class.forName(className);
			return clazz.getMethod(name, params);
		} catch (ClassNotFoundException | NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}



	public static ItemStack asNMSCopy(org.bukkit.inventory.ItemStack itemStack){
		try {
			return (ItemStack) reflectMethod(cbClass("inventory.CraftItemStack"), "asNMSCopy", itemStack.getClass()).invoke(null, itemStack);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	public static org.bukkit.inventory.ItemStack asBukkitCopy(ItemStack itemStack){
		try {
			return (org.bukkit.inventory.ItemStack) reflectMethod(cbClass("inventory.CraftItemStack"), "asBukkitCopy", itemStack.getClass()).invoke(null, itemStack);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	public static CompoundTag asNBT(org.bukkit.inventory.ItemStack itemStack){
		ItemStack nmsItem = asNMSCopy(itemStack);
		return nmsItem.save(new CompoundTag());
	}

	public static String asNBTString(org.bukkit.inventory.ItemStack itemStack){
		return asNBT(itemStack).getAsString();
	}
	public static String asNBTString(ItemStack itemStack){
		return asNBT(itemStack).getAsString();
	}

	public static CompoundTag asNBT(ItemStack itemStack){
		return itemStack.save(new CompoundTag());
	}

	public static ItemStack fromNBT(CompoundTag tag){
		return ItemStack.of(tag);
	}

	public static ItemStack fromNBT(String nbt){
		try {
			return ItemStack.of(TagParser.parseTag(nbt));
		} catch (CommandSyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	public static org.bukkit.inventory.ItemStack fromNBTBukkit(CompoundTag tag){
		return asBukkitCopy(fromNBT(tag));
	}

	public static org.bukkit.inventory.ItemStack fromNBTBukkit(String nbt){
		return asBukkitCopy(fromNBT(nbt));
	}
}
