package com.naxville.naxcraft.admin;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.naxville.naxcraft.Naxcraft;

public class NaxcraftFreezeCommand {
	
	//NOTE: Does not seem to work, do not use this command until fixed.
	
	public static Naxcraft plugin;
	
	public NaxcraftFreezeCommand(Naxcraft instance){
		plugin = instance;
	}
	
	public boolean runFreezeCommand(CommandSender sender, String[] args){		
		if(sender instanceof Player && !plugin.playerManager.getPlayer((Player)sender).rank.isDemiAdmin()){
			sender.sendMessage(String.format(Naxcraft.PERMISSIONS_FAIL, "/freeze"));
			return true;
		}
		
		if(args.length == 0) {
			return false;
		}
		
		if(args.length >= 1){
			String messageOn = "Frozen players:";
			String messageOff = "Unfrozen players:";
			String messageFailed = "Players failed:";
			
			for (String target : args){
				
				if(plugin.getServer().getPlayer(target) == null){
					messageFailed += " " + target;
				}
				
				Player target_player = (Player)plugin.getServer().getPlayer(target);
				
				if(plugin.frozenPlayers.containsKey(target_player)){
					plugin.frozenPlayers.remove(target_player);
					target_player.sendMessage(Naxcraft.COMMAND_COLOR + "You have been unfrozen by an Admin.");
					messageOff += " " + target_player.getDisplayName();
				} else {
					plugin.frozenPlayers.put(target_player, true);
					target_player.sendMessage(Naxcraft.ADMIN_COLOR + "You have been frozen by an Admin!");
					messageOn += " " + plugin.getNickName(target_player.getName());
				}
			}
			if(!messageOn.equals("Frozen players:")) sender.sendMessage(Naxcraft.MSG_COLOR + messageOn);
			if(!messageOff.equals("Unfrozen players:")) sender.sendMessage(Naxcraft.MSG_COLOR + messageOff);
			if(messageFailed != "Players failed:") sender.sendMessage(Naxcraft.ERROR_COLOR + messageFailed);
		}
		
		return true;
	}

}
