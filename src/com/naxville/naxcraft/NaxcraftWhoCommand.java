package com.naxville.naxcraft;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;

public class NaxcraftWhoCommand {
	public static Naxcraft plugin;
	
	public NaxcraftWhoCommand(Naxcraft instance){
		plugin = instance;
	}
	
	public boolean runWhoCommand(CommandSender sender, String[] args)
	{
		Player[] players = plugin.getServer().getOnlinePlayers();
		
		String s = players.length > 1 ? "s" : "";
		
		sender.sendMessage(Naxcraft.MSG_COLOR + "----");
		
		String online = Naxcraft.COMMAND_COLOR + "/Who Command: " + Naxcraft.DEFAULT_COLOR + players.length + Naxcraft.COMMAND_COLOR + " Player" + s + " online. ";
		
		sender.sendMessage(online);
		
		for(World world : plugin.getServer().getWorlds())
		{
			String message = Naxcraft.MSG_COLOR + "[" + plugin.getWorldName(world, true) + "]: ";
			int i = 0;
			
			for(Player player : world.getPlayers())	
			{
				if(i > 0) message += Naxcraft.MSG_COLOR + ", ";
				message += plugin.getNickName(player.getName());
				i++;
			}
			
			if(i > 0)
			{
				sender.sendMessage(message);
			}
		}
		
		return true;
	}
}
