package com.naxville.naxcraft.admin;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.naxville.naxcraft.NaxColor;
import com.naxville.naxcraft.Naxcraft;

public class EnchantCommand
{
	public Naxcraft plugin;
	
	public EnchantCommand(Naxcraft plugin)
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
		
		if (args.length != 0 && (args[0].equalsIgnoreCase("unsafe") || args[0].equalsIgnoreCase("all")))
		{
			for (Enchantment c : Enchantment.values())
			{
				item.addUnsafeEnchantment(c, c.getMaxLevel());
			}
		}
		else if (args.length != 0 && args[0].equalsIgnoreCase("list"))
		{
			for (Enchantment c : Enchantment.values())
			{
				if (c.canEnchantItem(item))
				{
					player.sendMessage(NaxColor.WHITE + c.getName() + NaxColor.MSG + " [" + c.getMaxLevel() + "] " + "(" + c.getItemTarget() + ")");
				}
			}
		}
		else if (args.length != 0 && args[0].equalsIgnoreCase("listall"))
		{
			String msg = "";
			
			for (Enchantment c : Enchantment.values())
			{
				if (msg != "") msg += ", ";
				player.sendMessage(NaxColor.WHITE + c.getName() + NaxColor.MSG + " [" + c.getMaxLevel() + "] " + "(" + c.getItemTarget() + ")");
				msg += NaxColor.WHITE + c.getName() + NaxColor.MSG + "(" + c.getMaxLevel() + ")";
			}
			player.sendMessage(NaxColor.MSG + "Item enchanted with: " + msg);
		}
		else if (args.length != 0 && args[0].equalsIgnoreCase("base"))
		{
			String msg = "";
			
			for (Enchantment c : Enchantment.values())
			{
				if (c.canEnchantItem(item))
				{
					if (msg != "") msg += ", ";
					item.addEnchantment(c, c.getMaxLevel());
					msg += NaxColor.WHITE + c.getName() + NaxColor.MSG + "(" + c.getMaxLevel() + ")";
				}
			}
			
			player.sendMessage(NaxColor.MSG + "Item enchanted with: " + msg);
		}
		else if (args.length == 0)
		{
			player.sendMessage(NaxColor.MSG + "----");
			player.sendMessage(NaxColor.COMMAND + "Enchant Command:");
			player.sendMessage(NaxColor.MSG + "/enchant " + NaxColor.WHITE + "list" + NaxColor.MSG + ": " + "Lists all enchantments for item.");
			player.sendMessage(NaxColor.MSG + "/enchant " + NaxColor.WHITE + "listall" + NaxColor.MSG + ": " + "Lists all enchantments.");
			player.sendMessage(NaxColor.MSG + "/enchant " + NaxColor.WHITE + "base" + NaxColor.MSG + ": " + "Enchants item with all applicable enchants.");
			player.sendMessage(NaxColor.MSG + "/enchant " + NaxColor.WHITE + "all" + NaxColor.MSG + ": " + "Enchants item with all enchants.");
			player.sendMessage(NaxColor.MSG + "/enchant " + NaxColor.WHITE + "<name> [level]" + NaxColor.MSG + ": " + "Enchants item with specific enchantment.");
		}
		else if (args.length != 0)
		{
			Enchantment en = null;
			int level = 0;
			
			for (Enchantment c : Enchantment.values())
			{
				if (c.getName().equalsIgnoreCase(args[0]))
				{
					en = c;
				}
			}
			
			if (en == null)
			{
				player.sendMessage(NaxColor.MSG + args[0] + " is not an enchantment.");
				return;
			}
			
			if (args.length > 1)
			{
				try
				{
					level = Integer.parseInt(args[1]);
				}
				catch (NumberFormatException e)
				{
					player.sendMessage(NaxColor.MSG + args[1] + " is not a number.");
					return;
				}
			}
			else
			{
				level = en.getMaxLevel();
			}
			
			player.getItemInHand().addUnsafeEnchantment(en, level);
			
			player.sendMessage(NaxColor.MSG + "Item enchanted with " + NaxColor.WHITE + en.getName() + NaxColor.MSG + " at level " + level + "!");
		}
	}
}
