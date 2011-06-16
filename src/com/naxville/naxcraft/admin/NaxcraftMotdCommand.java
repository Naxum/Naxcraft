package com.naxville.naxcraft.admin;

import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;

import com.naxville.naxcraft.Naxcraft;

public class NaxcraftMotdCommand {
	
	public static Naxcraft plugin;
	
	public NaxcraftMotdCommand(Naxcraft instance){
		plugin = instance;
	}
	
	public boolean runMotdCommand(CommandSender sender, String[] args){
		if(args.length == 0) {
			sender.sendMessage(Naxcraft.ADMIN_COLOR + "MOTD:" + plugin.motd);
			return true;
		}
		
		if(plugin.playerManager.getPlayer((Player)sender).rank.isAdmin()){
			if(args.length < 2) {
				return false;
				
			} else {
				
				String newMotd = "";
				int i = 0;
				for(String arg : args){
					if(i!=0){
						newMotd += " " + arg;
					}
					i++;
				}
				
				plugin.getServer().broadcastMessage(Naxcraft.ADMIN_COLOR + "MOTD:" + Naxcraft.COLORS[Integer.parseInt(args[0])] + newMotd);
				plugin.motd = Naxcraft.COLORS[Integer.parseInt(args[0])] + newMotd;
			}
		} else {
			sender.sendMessage(String.format(Naxcraft.PERMISSIONS_FAIL, "/motd"));
		}
		return true;
	}

}
