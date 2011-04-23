package com.naxville.naxcraft;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;

public class NaxcraftTphereCommand {
	public static Naxcraft plugin;
	
	public NaxcraftTphereCommand(Naxcraft instance){
		plugin = instance;
	}
	
	public boolean runTphereCommand(CommandSender sender, String[] args){
		
		if(!(sender instanceof Player)){
			sender.sendMessage(Naxcraft.NOT_A_PLAYER);
			return true;
		}
		
		Player player = (Player) sender;
		if(plugin.control.has(player, "tphere")){
		
			if(args.length == 0) {
				return false;
			}
			
			if(args.length >= 1){
				
				String message = "Teleported: ";
				String failedMessage = "Failed: ";
				
				for (String target : args) {
					
	                if(plugin.getServer().getPlayer(target) != null){
	                	if(plugin.tpCommand.teleport(target, player)){
	                		message += target + ", ";
	                	} else {
	                		failedMessage += target + " ";
	                	}
	                } else {
	            		failedMessage += target + " ";
	                }
	            }
				
				if(!message.equals("Teleported: ")){
					player.sendMessage(Naxcraft.COMMAND_COLOR + message);
				}
				if(!failedMessage.equals("Failed: ")){
					player.sendMessage(Naxcraft.ERROR_COLOR + failedMessage);
				}
				
				return true;
				
			}
			
		} else {
			sender.sendMessage(String.format(Naxcraft.PERMISSIONS_FAIL, "/tphere"));
			return true;
		}
		
		return false;
	}
	
	public boolean runTpThereCommand(CommandSender sender, String[] args){
		if(!(sender instanceof Player)){
			sender.sendMessage(Naxcraft.NOT_A_PLAYER);
			return true;
		}
		
		Player player = (Player) sender;
		if(plugin.control.has(player, "tpthere")){
			
			if(args.length == 0) {
				return false;
			}
			
			Block block = player.getTargetBlock(null, 150);
			Location location = new Location(block.getWorld(), block.getLocation().getBlockX(), block.getLocation().getBlockY()+1, block.getLocation().getBlockZ());
			
			if(args.length >= 1){
				
				String message = "Teleported: ";
				String failedMessage = "Failed: ";
				
				for (String target : args) {
					
	                if(plugin.getServer().getPlayer(target) != null){
	                	if(plugin.tpCommand.teleport(target, location)){
	                		message += target + ", ";
	                	} else {
	                		failedMessage += target + " ";
	                	}
	                } else {
	            		failedMessage += target + " ";
	                }
	            }
				
				if(!message.equals("Teleported: ")){
					player.sendMessage(Naxcraft.COMMAND_COLOR + message);
				}
				if(!failedMessage.equals("Failed: ")){
					player.sendMessage(Naxcraft.ERROR_COLOR + failedMessage);
				}
				
				return true;
				
			}
			
		} else {
			sender.sendMessage(String.format(Naxcraft.PERMISSIONS_FAIL, "/tpthere"));
			return true;
		}
		
		return true;
	}

}
