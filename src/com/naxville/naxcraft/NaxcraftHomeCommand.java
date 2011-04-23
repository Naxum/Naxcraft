package com.naxville.naxcraft;

import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;

import com.naxville.naxcraft.land.NaxcraftGroup;

public class NaxcraftHomeCommand {
	public static Naxcraft plugin;
	
	public NaxcraftHomeCommand(Naxcraft instance){
		plugin = instance;
	}

	public boolean runHomeCommand(CommandSender sender, String[] args){
		if(!(sender instanceof Player)){
			sender.sendMessage(Naxcraft.NOT_A_PLAYER);
			return true;
		}
		
		Player player = (Player) sender;
		if(plugin.control.has(player, "home")){
			
			String groupName = plugin.groupCommand.requestGroup(player.getWorld(), player.getLocation());
			if(groupName != ""){
				NaxcraftGroup group = plugin.groupCommand.getList(player).get(groupName);
				if(group.isFlag("jail")){
					if(!plugin.superCommand.isSuper(player.getName())){
						player.sendMessage(Naxcraft.MSG_COLOR + "You cannot warp out of a jail!");
						return true;
					}
				}
			}
			
			if(!plugin.warpingPlayers.containsKey(player.getName())){
				if(plugin.superCommand.isSuper(player.getName())){
					player.teleport(player.getWorld().getSpawnLocation());
					return true;
				}
				int id = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new NaxcraftWarpRunnable(plugin, player, "home"), 100);
				if(id == -1)
				{
					player.sendMessage(Naxcraft.ERROR_COLOR + "Error: Teleport task request failed.");
					return false;
				} else {
					player.sendMessage(Naxcraft.COMMAND_COLOR + "You begin to concentrate on where you want to go...");
					plugin.warpingPlayers.put(player.getName(), (Integer) id);
				}
			} else{
				player.sendMessage(Naxcraft.COMMAND_COLOR + "Two places at once? Not a good plan...");
			}
		} else{
			sender.sendMessage(String.format(Naxcraft.PERMISSIONS_FAIL, "/home"));
		}
		return true;
	}
}