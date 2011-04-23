package com.naxville.naxcraft.admin;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.naxville.naxcraft.Naxcraft;

public class NaxcraftFireCommand {
	public static Naxcraft plugin;
	
	public NaxcraftFireCommand(Naxcraft instance){
		plugin = instance;
	}
	
	public boolean runFireCommand(CommandSender sender, String[] args){
		
		if((sender instanceof Player) && !(plugin.control.has((Player)sender, "fire"))){
			sender.sendMessage(String.format(Naxcraft.PERMISSIONS_FAIL, "/fire"));
			return true;
		}
		
		if(plugin.stopFire){
			plugin.stopFire = false;
			plugin.getServer().broadcastMessage(ChatColor.RED + "Fire has returned to normal.");
			return true;
		} else {
			plugin.stopFire = true;
			plugin.getServer().broadcastMessage(ChatColor.RED + "Fire spread has been disabled.");
			return true;
		}
	}

}
