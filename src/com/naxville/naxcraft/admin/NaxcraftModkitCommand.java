package com.naxville.naxcraft.admin;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.command.CommandSender;

import com.naxville.naxcraft.Naxcraft;

public class NaxcraftModkitCommand {
	public static Naxcraft plugin;
	
	public NaxcraftModkitCommand(Naxcraft instance){
		plugin = instance;
	}
	
	public boolean runModkitCommand(CommandSender sender, String[] args){
		if(!(sender instanceof Player)){
			sender.sendMessage(Naxcraft.NOT_A_PLAYER);
			return true;
		}
		
		Player player = (Player) sender;
		if(plugin.control.has(player, "modkit")){
		
			String message = "Modkit Given!";
			
			if(!player.getInventory().contains(Material.GOLD_SWORD)){
				player.getInventory().addItem(new ItemStack(Material.GOLD_SWORD, 1));
			}
			
			if(!player.getInventory().contains(Material.GOLD_PICKAXE)){
				player.getInventory().addItem(new ItemStack(Material.GOLD_PICKAXE, 1));
			}
			
			if(!player.getInventory().contains(Material.GOLD_SPADE)){
				player.getInventory().addItem(new ItemStack(Material.GOLD_SPADE, 1));
			}
			
			if(!player.getInventory().contains(Material.GOLD_AXE)){
				player.getInventory().addItem(new ItemStack(Material.GOLD_AXE, 1));
			}
			
			if(!player.getInventory().contains(Material.GOLD_HOE)){
				player.getInventory().addItem(new ItemStack(Material.GOLD_HOE, 1));
			} 
			
			player.sendMessage(Naxcraft.ADMIN_COLOR + message);
			
		} else {
			sender.sendMessage(String.format(Naxcraft.PERMISSIONS_FAIL, "/modkit"));
		}
		
		return true;		
	}
}
