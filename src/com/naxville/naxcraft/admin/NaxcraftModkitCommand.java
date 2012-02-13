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
		if(plugin.playerManager.getPlayer(player).rank.isAdmin()){
		
			String message = "Here you go, my leige!";
			
			if(!player.getInventory().contains(Material.WATCH)){
				player.getInventory().addItem(new ItemStack(Material.WATCH, 1));
			}
			
			if(!player.getInventory().contains(Material.COMPASS)){
				player.getInventory().addItem(new ItemStack(Material.COMPASS, 1));
			} 
			
			player.sendMessage(Naxcraft.ADMIN_COLOR + message);
			
		} else {
			sender.sendMessage(String.format(Naxcraft.PERMISSIONS_FAIL, "/modkit"));
		}
		
		return true;		
	}
}
