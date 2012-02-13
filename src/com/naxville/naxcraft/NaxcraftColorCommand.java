package com.naxville.naxcraft;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.naxville.naxcraft.player.PlayerManager.PlayerRank;

public class NaxcraftColorCommand
{
	public static Naxcraft plugin;
	
	public NaxcraftColorCommand(Naxcraft instance)
	{
		plugin = instance;
	}
	
	public boolean runColorCommand(CommandSender sender, String[] args)
	{
		
		if (!(sender instanceof Player))
		{
			sender.sendMessage(Naxcraft.NOT_A_PLAYER);
			return true;
		}
		
		Player player = (Player) sender;
		
		if (plugin.playerManager.getPlayer(player).rank.getId() == 0)
		{
			player.sendMessage(Naxcraft.MSG_COLOR + "You must be a member to use this command. Get 16 iron first and use /join.");
			return true;
		}
		
		if (args.length > 0)
		{
			int color = 13;
			try
			{
				color = Integer.parseInt(args[0]);
			}
			catch (Exception e)
			{
				return false;
			}
			
			if ((
					(
						color > 0
					)
					||
					(
						(
								plugin.playerManager.getPlayer(player).rank.getId() > PlayerRank.getRank("moderator").getId()
						)
						&&
						(
							color == 0
						)
					)
					)
					&&
					(
					(
						color < 15
					)
					||
					(
						(
								plugin.playerManager.getPlayer(player).rank.getId() > PlayerRank.getRank("moderator").getId()
						)
						&&
						(
							color < 16)
						)
					))
			{
				player.setDisplayName(Naxcraft.COLORS[color] + player.getName() + Naxcraft.MSG_COLOR);
				plugin.playerManager.setDisplayName(player, Naxcraft.COLORS[color]);
				player.sendMessage((Naxcraft.COMMAND_COLOR + "Your display color is now " + Naxcraft.COLORS[color] + color));
			}
			else
			{
				player.sendMessage((Naxcraft.COMMAND_COLOR + "Invalid color index: " + args[0]));
			}
			return true;
		}
		String colorCodes = Naxcraft.COMMAND_COLOR + "Color codes: ";
		for (int i = 1; i < 15; i++)
		{
			colorCodes += " " + Naxcraft.COLORS[i];
			colorCodes += i;
		}
		player.sendMessage(colorCodes);
		return true;
	}
}
