package com.naxville.naxcraft.player;

import org.bukkit.entity.Player;

import com.naxville.naxcraft.Naxcraft;

public class KickCommand 
{
	public Naxcraft plugin;
	
	public KickCommand(Naxcraft plugin)
	{
		this.plugin = plugin;
	}
	
	public boolean runKickCommand(Player player, String[] args)
	{
		if(!plugin.playerManager.getPlayer(player).rank.isDemiAdmin())
		{
			return true;
		}
		
		if(args.length == 0)
		{
			player.sendMessage(Naxcraft.MSG_COLOR + "/kick <name> [reason for kicking (optional)]");
			return true;
		}
		
		Player kick = plugin.getServer().getPlayer(args[0]);
		
		if(kick == null)
		{
			player.sendMessage(Naxcraft.MSG_COLOR + "The player " + args[0] + " does not exist or is offline.");
		}
		else
		{
			if(args.length == 1)
			{
				kick.kickPlayer(Naxcraft.MSG_COLOR + "Kicked by " + plugin.getNickName(player));
			}
			else
			{
				String str = "";
				
				for(int i = 1; i < args.length; i++)
				{
					if(i > 1)
						str += " ";
					
					str += args[i];
				}
				
				kick.kickPlayer(Naxcraft.MSG_COLOR + "Kicked by " + plugin.getNickName(player) + " for " + str);
			}
			plugin.log.severe("------- KICK ------");
			plugin.log.severe(player.getName() + " kicked " + kick.getName());
			plugin.log.severe("------- /KICK ------");
		}
		
		return true;
	}
}
