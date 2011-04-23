package com.naxville.naxcraft;

import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;

public class NaxcraftWhoCommand {
	public static Naxcraft plugin;
	
	public NaxcraftWhoCommand(Naxcraft instance){
		plugin = instance;
	}
	
	public boolean runWhoCommand(CommandSender sender, String[] args){
		if((plugin.control.has((Player) sender, "who")&&(sender instanceof Player))||!(sender instanceof Player)){
			Player[] players = plugin.getServer().getOnlinePlayers();
			
			String s = players.length > 1 ? "s" : "";
			
			String message = Naxcraft.MSG_COLOR + "" + players.length + " Player" + s + " online: " + Naxcraft.DEFAULT_COLOR;
			
			String naxville = Naxcraft.WORLD_COLOR + "-Naxville: ";
			String naxvillePlayers = "";
			String rpg = Naxcraft.WORLD_COLOR + "-" + plugin.rpgWorldName +": ";
			String rpgPlayers = "";
			
			for(Player player : players){
				if(player.getWorld().getName().equalsIgnoreCase("world")){
					if(naxvillePlayers != "") naxvillePlayers += Naxcraft.MSG_COLOR + ", ";
					naxvillePlayers += plugin.getNickName(player.getName().toLowerCase());
					
				} else if(player.getWorld().getName().equalsIgnoreCase(plugin.rpgWorld)){
					if(rpgPlayers != "") rpgPlayers += Naxcraft.MSG_COLOR + ", ";
					rpgPlayers += plugin.getNickName(player.getName().toLowerCase());
				}
			}
			
			rpgPlayers = (rpgPlayers != "") ? rpgPlayers : Naxcraft.MSG_COLOR + "Empty";
			naxvillePlayers = (naxvillePlayers != "") ? naxvillePlayers : Naxcraft.MSG_COLOR + "Empty";
			
			sender.sendMessage(message);
			sender.sendMessage(naxville + naxvillePlayers);
			sender.sendMessage(rpg + rpgPlayers);
		} else {
			sender.sendMessage(String.format(Naxcraft.PERMISSIONS_FAIL, "/who"));
		}
		
		return true;
	}
}
