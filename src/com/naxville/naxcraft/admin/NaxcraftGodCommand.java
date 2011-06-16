package com.naxville.naxcraft.admin;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
//import org.bukkit.ChatColor;

import com.naxville.naxcraft.Naxcraft;

public class NaxcraftGodCommand {
	
	public static Naxcraft plugin;
	public List<String> goddedPlayers = new ArrayList<String>();
	
	public NaxcraftGodCommand(Naxcraft instance) {
		plugin = instance;
		
		//runGodCommand();
	}
	
	public boolean runGodCommand(CommandSender sender, String[] args){		
		//is the player an Op? To be changed later.
		if(sender instanceof Player && plugin.playerManager.getPlayer((Player)sender).rank.isAdmin()) {
			
			//if there are no arguments (/god)
			if(args.length == 0){
				
				//set full health and toggle god mode.
				if (!(sender instanceof Player)) {
					sender.sendMessage(Naxcraft.NOT_A_PLAYER);
					return true;
				}
				Player player = (Player)sender;
				player.setHealth(20);
				if(godMode(((Player)sender).getName())){
					player.sendMessage(Naxcraft.MSG_COLOR + "You are now a god!");
				} else {
					player.sendMessage(Naxcraft.MSG_COLOR + "You are now a mortal.");
				}
				
			//if there are arguments (at least 1)
			} else if(args[0] != null){
				
				//if the first argument equals a ?
				if(args[0].equalsIgnoreCase("?")){
					
					if(args.length == 1){
						//zero is the loneliest number
						if(godded(((Player)sender).getName())){
							sender.sendMessage(Naxcraft.COMMAND_COLOR + "You are indeed a god, my lord.");
						} else {
							sender.sendMessage(Naxcraft.COMMAND_COLOR + "You are a mortal, my lord.");
						}
						
						return true;
						
					} else if(args.length == 2){
						//if there's additional arguments
						
						Player target;
						
						//is the player online
						if(plugin.getServer().getPlayer(args[1]) != null){
							
							target = plugin.getServer().getPlayer(args[1]);
							
							if (godded(target.getName())){
								sender.sendMessage(Naxcraft.COMMAND_COLOR + target.getName() + " is in god mode.");
							} else {
								sender.sendMessage(Naxcraft.COMMAND_COLOR + target.getName() + " is not in god mode.");
							}
							
							return true;
							
						} else {
							sender.sendMessage(Naxcraft.ERROR_COLOR + "Player is not online or does not exist.");
							return true;
						}
						
					}
					
				} else {
					Player target;
					
					//if the player is online
					if(plugin.getServer().getPlayer(args[0]) != null){
						target = plugin.getServer().getPlayer(args[0]);
						target.setHealth(20);
						if(godMode(target.getName())){
							Player player = (Player)sender;
							player.sendMessage(plugin.getNickName(target.getName()) + Naxcraft.MSG_COLOR + " is " + Naxcraft.SUCCESS_COLOR + "now" + Naxcraft.MSG_COLOR + " a god!");
							target.sendMessage(plugin.getNickName(player.getName()) + Naxcraft.ADMIN_COLOR + " has turned you into a god!");
						} else {
							Player player = (Player)sender;
							player.sendMessage(plugin.getNickName(target.getName()) + Naxcraft.MSG_COLOR + " is " + Naxcraft.ERROR_COLOR + "no longer" + Naxcraft.MSG_COLOR + " a god!");
							target.sendMessage(plugin.getNickName(player.getName()) + Naxcraft.ADMIN_COLOR + " has turned you back into a mortal.");
						}
						
					} else {
						sender.sendMessage(Naxcraft.ERROR_COLOR + "Player doesn't exist.");
						return true;
					}
				}
				
			} else {
				sender.sendMessage(Naxcraft.ERROR_COLOR + "Who are you trying to bless, my lord?");
			}
		} else {
			sender.sendMessage(String.format(Naxcraft.PERMISSIONS_FAIL, "/god"));
			return true;
		}
		return true;
	}
	
	public boolean godMode(String player){
		player = player.toLowerCase();
		
		if(!goddedPlayers.contains(player)){
			goddedPlayers.add(player);
			return true;
			
		} else {
			goddedPlayers.remove(player);
			return false;
		}
	}
	
	public boolean godded(String player){
		if(goddedPlayers.contains(player.toLowerCase())){
			//System.out.println("Player is indeed a god.");
			return true;
		} else {
			return false;
		}
	}
}
