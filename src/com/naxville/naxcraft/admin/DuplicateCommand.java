package com.naxville.naxcraft.admin;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.naxville.naxcraft.NaxColor;
import com.naxville.naxcraft.Naxcraft;

public class DuplicateCommand
{
	public Naxcraft plugin;
	
	public DuplicateCommand(Naxcraft plugin)
	{
		this.plugin = plugin;
	}
	
	public void runCommand(Player player, String[] args)
	{
		if (!plugin.playerManager.getPlayer(player).rank.isAdmin())
		{
			player.sendMessage(NaxColor.MSG + "Get out of here.");
			return;
		}
		
		ItemStack item = player.getItemInHand();
		int amount = 1;
		
		if (args.length != 0)
		{
			try
			{
				amount = Integer.parseInt(args[0]);
			}
			catch (NumberFormatException e)
			{
				player.sendMessage(NaxColor.MSG + "It's /duplicate #, you entered something else.");
				return;
			}
		}
		
		if (item != null)
		{
			for (int i = 0; i < amount; i++)
			{
				ItemStack x = item.clone();
				
				for (Enchantment c : item.getEnchantments().keySet())
				{
					x.addEnchantment(c, item.getEnchantmentLevel(c));
				}
				
				player.getInventory().addItem(x);
			}
			
			player.sendMessage(NaxColor.MSG + "Item duplicated " + NaxColor.WHITE + amount + NaxColor.MSG + " times.");
		}
		else
		{
			player.sendMessage(NaxColor.MSG + "You need something in your hand.");
		}
	}
}
