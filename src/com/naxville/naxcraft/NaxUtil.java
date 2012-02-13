package com.naxville.naxcraft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.naxville.naxcraft.admin.SuperManager;

public class NaxUtil
{
	
	public static HashMap<Material, String> materials;
	
	static
	{
		materials = new HashMap<Material, String>();
		materials.put(Material.BROWN_MUSHROOM, "B Mushroom");
		materials.put(Material.COBBLESTONE_STAIRS, "Cobble Stairs");
		materials.put(Material.FLINT_AND_STEEL, "Lighter");
		materials.put(Material.MILK_BUCKET, "Milk");
		materials.put(Material.WATER_BUCKET, "Water");
		materials.put(Material.MOSSY_COBBLESTONE, "Moss Stone");
		materials.put(Material.GLOWSTONE_DUST, "Glow Dust");
		materials.put(Material.BREWING_STAND_ITEM, "Brew Stand");
		
		materials.put(Material.LEATHER_BOOTS, "Leather Boots");
		materials.put(Material.LEATHER_HELMET, "Leather Helm");
		materials.put(Material.LEATHER_CHESTPLATE, "Leather Chest");
		materials.put(Material.LEATHER_LEGGINGS, "Leather Legs");
		
		materials.put(Material.IRON_BOOTS, "Iron Boots");
		materials.put(Material.IRON_HELMET, "Iron Helm");
		materials.put(Material.IRON_CHESTPLATE, "Iron Chest");
		materials.put(Material.IRON_LEGGINGS, "Iron Legs");
		
		materials.put(Material.GOLD_BOOTS, "Gold Boots");
		materials.put(Material.GOLD_HELMET, "Gold Helm");
		materials.put(Material.GOLD_CHESTPLATE, "Gold Chest");
		materials.put(Material.GOLD_LEGGINGS, "Gold Legs");
		
		materials.put(Material.CHAINMAIL_BOOTS, "Chain Boots");
		materials.put(Material.CHAINMAIL_HELMET, "Chain Helm");
		materials.put(Material.CHAINMAIL_CHESTPLATE, "Chain Chest");
		materials.put(Material.CHAINMAIL_LEGGINGS, "Chain Legs");
		
		materials.put(Material.DIAMOND_BOOTS, "Diamond Boots");
		materials.put(Material.DIAMOND_HELMET, "Diamond Helm");
		materials.put(Material.DIAMOND_CHESTPLATE, "Diamond Chest");
		materials.put(Material.DIAMOND_LEGGINGS, "Diamond Legs");
	}
	
	public static String materialName(Material m)
	{
		if (materials.containsKey(m))
		{
			return materials.get(m);
		}
		else
		{
			String result = "";
			String name = m.toString();
			
			for (String part : name.split("_"))
			{
				if (result != "") result += " ";
				result += part.substring(0, 1).toUpperCase() + part.substring(1).toLowerCase();
			}
			
			return result;
		}
	}
	
	public static void removeFromInventory(Inventory inv, ItemStack cost)
	{
		List<Integer> slotsToDestroy = new ArrayList<Integer>();
		int amountLeft = cost.getAmount();
		
		for (int i = 0; i < inv.getContents().length; i++)
		{
			ItemStack item = inv.getItem(i);
			if (item == null || item.getAmount() == 0 || item.getType() != cost.getType() || item.getDurability() != cost.getDurability()) continue;
			
			slotsToDestroy.add(i);
			
			amountLeft -= item.getAmount();
			
			if (amountLeft <= 0) break;
		}
		
		for (int slot : slotsToDestroy)
		{
			inv.setItem(slot, null);
		}
		
		if (amountLeft < 0)
		{
			ItemStack change = cost.clone();
			
			change.setAmount(-amountLeft);
			
			inv.addItem(change);
		}
	}
	
	public static boolean charge(Player player, ItemStack cost)
	{
		if (player.getGameMode() == GameMode.CREATIVE && !SuperManager.isSuper(player))
		{
			player.sendMessage(NaxColor.MSG + "You may not use transactions while in Creative mode.");
			return false;
		}
		
		int amountLeft = cost.getAmount();
		List<Integer> slotsToDestroy = new ArrayList<Integer>();
		boolean enoughMoney = false;
		
		for (int i = 0; i < player.getInventory().getContents().length; i++)
		{
			ItemStack item = player.getInventory().getContents()[i];
			
			if (item == null || item.getAmount() == 0)
			{
				continue;
			}
			else if (item.getType() != cost.getType() || item.getDurability() != cost.getDurability())
			{
				continue;
			}
			
			amountLeft -= item.getAmount();
			slotsToDestroy.add(i);
			
			if (amountLeft <= 0)
			{
				enoughMoney = true;
				break;
			}
		}
		
		if (!enoughMoney) return false;
		
		for (int slot : slotsToDestroy)
		{
			player.getInventory().setItem(slot, null);
		}
		
		if (amountLeft < 0)
		{
			ItemStack change = cost.clone();
			change.setAmount(-amountLeft);
			
			player.getInventory().addItem(change);
		}
		
		return true;
	}
	
	/**
	 * Attempts to charge a player a cost of material. Returns false if they
	 * can't afford it, true if they did afford it and the items were taken.
	 * 
	 * @param player
	 * @param material
	 * @param cost
	 * @return
	 */
	public static boolean charge(Player player, Material material, int cost)
	{
		if (player.getGameMode() == GameMode.CREATIVE && !SuperManager.isSuper(player))
		{
			player.sendMessage(NaxColor.MSG + "You may not use transactions while in Creative mode.");
			return false;
		}
		
		int priceleft = cost;
		List<Integer> slotsToDestroy = new ArrayList<Integer>();
		
		boolean enoughMoney = false;
		
		for (int slot : player.getInventory().all(material).keySet())
		{
			priceleft -= player.getInventory().getItem(slot).getAmount();
			if (priceleft > 0)
			{
				slotsToDestroy.add(slot);
			}
			else if (priceleft == 0)
			{
				slotsToDestroy.add(slot);
				enoughMoney = true;
				break;
			}
			else if (priceleft < 0)
			{
				slotsToDestroy.add(slot);
				enoughMoney = true;
				break;
			}
		}
		
		if (enoughMoney)
		{
			for (int slot : slotsToDestroy)
			{
				player.getInventory().setItem(slot, null);
			}
			if (priceleft < 0)
			{
				player.getInventory().addItem(new ItemStack(material, priceleft * -1));
			}
		}
		
		return enoughMoney;
	}
	
	/**
	 * Fast way to convert an array to a string.
	 * 
	 * @param array
	 * @return
	 */
	public static String arrayToString(String[] array)
	{
		StringBuffer buffer = new StringBuffer();
		String seperator = " ";
		int newWords = 0;
		if (array.length > 0)
		{
			for (int i = 0; i < array.length; i++)
			{
				if (!array[i].equals(""))
				{
					if (newWords != 0) buffer.append(seperator);
					buffer.append(array[i]);
					newWords++;
				}
			}
		}
		
		return buffer.toString();
	}
	
	public static BlockFace[] cardinals()
	{
		return new BlockFace[] { BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH };
	}
	
	/**
	 * Returns the direction of something placed against the block face.
	 * 
	 * @param f
	 * @return
	 */
	public static byte getDirectionFromBlockFace(BlockFace f)
	{
		if (f == BlockFace.NORTH)
		{
			return (byte) 2;
		}
		else if (f == BlockFace.SOUTH)
		{
			return (byte) 3;
		}
		else if (f == BlockFace.EAST)
		{
			return (byte) 5;
		}
		else if (f == BlockFace.WEST)
		{
			return (byte) 4;
		}
		else
		{
			System.out.println("Nax Util: Not cardinal direction! " + f.toString());
			return 0;
		}
	}
}
