package com.naxville.naxcraft.admin;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.naxville.naxcraft.Naxcraft;

public class NaxcraftDropCommand {
	
	public static Naxcraft plugin;
	
	public NaxcraftDropCommand (Naxcraft instance){
		plugin = instance;
	}
	
	public boolean runDropCommand(CommandSender sender, String[] args){
		if(!(sender instanceof Player)){
			sender.sendMessage(Naxcraft.NOT_A_PLAYER);
			return true;
		}
		Player player = (Player)sender;
		
		if (!plugin.control.has(player, "drop")){
			sender.sendMessage(String.format(Naxcraft.PERMISSIONS_FAIL, "/drop"));
			return true;
		}
		
		if(args.length == 0){
			if(plugin.destroyDrops.containsKey(player)){
				plugin.destroyDrops.remove(player);
				player.sendMessage(Naxcraft.COMMAND_COLOR + "You can now drop items normally.");
			} else {
				plugin.destroyDrops.put(player, true);
				player.sendMessage(Naxcraft.ERROR_COLOR + "Anything you drop will now be destroyed");
			}
		}
		
		if(args.length >= 1){
			String messageOn = "Drops to destroy:";
			String messageOff = "Drops to live:";
			String messageFailed = "Players failed:";
			
			for (String target : args){
				
				if(plugin.getServer().getPlayer(target) == null){
					messageFailed += " " + target;
				}
				
				Player target_player = (Player)plugin.getServer().getPlayer(target);
				
				if(plugin.destroyDrops.containsKey(target_player)){
					plugin.destroyDrops.remove(target_player);
					target_player.sendMessage(Naxcraft.ADMIN_COLOR + "You can now drop items normally.");
					messageOff += " " + target_player.getDisplayName();
				} else {
					plugin.destroyDrops.put(target_player, true);
					target_player.sendMessage(Naxcraft.ADMIN_COLOR + "Anything you drop will now be destroyed.");
					messageOn += " " + target_player.getDisplayName();
				}
			}
			if(!messageOn.equals("Drops to destroy:")) player.sendMessage(Naxcraft.ADMIN_COLOR + messageOn);
			if(!messageOff.equals("Drops to live:")) player.sendMessage(Naxcraft.ADMIN_COLOR + messageOff);
			if(messageFailed != "Players failed:") player.sendMessage(Naxcraft.ERROR_COLOR + messageFailed);
		}
		
		return true;
	}

}
