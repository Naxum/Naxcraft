package com.naxville.naxcraft.admin;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.naxville.naxcraft.Naxcraft;

public class NaxcraftWmodCommand {
	private static Naxcraft plugin;
	
	public NaxcraftWmodCommand(Naxcraft instance){
		plugin = instance;
	}
	
	public boolean runWmodCommand(CommandSender sender, String[] args){
		if ((sender instanceof Player && !plugin.control.has((Player)sender, "wmod"))){
			sender.sendMessage(String.format(Naxcraft.PERMISSIONS_FAIL, "/wmod"));
			return true;
		}
		
		if(args.length == 1){
			if (args[0].equalsIgnoreCase("list")){
				
				String totalWarps = "";
				
				for(String warp : plugin.warpCommand.warps.keySet()){
					
					if(totalWarps != "") totalWarps += Naxcraft.COMMAND_COLOR + ", ";
					totalWarps += Naxcraft.DEFAULT_COLOR + warp;
				}
				
				sender.sendMessage(Naxcraft.COMMAND_COLOR + "Total Warps (" + Naxcraft.DEFAULT_COLOR + plugin.warpCommand.warps.size() + Naxcraft.COMMAND_COLOR + "): " + totalWarps);
				return true;
			}
		}
		
		// /wmod create name, delete name
		if(args.length == 2){
			if (!(sender instanceof Player)) {
				sender.sendMessage(Naxcraft.NOT_A_PLAYER);
			}
			Player player = (Player)sender;

			if(args[0].equalsIgnoreCase("create")){
				plugin.warpCommand.warps.put(args[1], player.getLocation());
				plugin.warpCommand.writeWarps();
				
				player.sendMessage(Naxcraft.SUCCESS_COLOR + "Warp " + args[1] + " created successfully at " + Math.floor(player.getLocation().getX()) + ", " + Math.floor(player.getLocation().getY()) + ", " + Math.floor(player.getLocation().getZ())+ " !");
				
				return true;
				
			} else if(args[0].equalsIgnoreCase("delete")){
				
				if(!plugin.warpCommand.warps.containsKey(args[1].toLowerCase())){
					player.sendMessage(Naxcraft.ERROR_COLOR + "That warp does not exist!");
					return true;
					
				} else {
					plugin.warpCommand.warps.remove(args[1].toLowerCase());
					plugin.warpCommand.writeWarps();
					player.sendMessage(Naxcraft.SUCCESS_COLOR + "Warp " + args[1] + " was deleted!");
				}
				
				return true;
			} 
		}
		
		return false;
	}
}
