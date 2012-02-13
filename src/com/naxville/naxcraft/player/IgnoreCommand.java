package com.naxville.naxcraft.player;

import org.bukkit.entity.Player;

import com.naxville.naxcraft.Naxcraft;

public class IgnoreCommand
{
	public PlayerManager pm;
	
	public IgnoreCommand(PlayerManager pm)
	{
		this.pm = pm;
	}
	
	public boolean runCommand(Player player, String[] args)
	{
		if(args.length == 0)
		{
			printHelp(player);
			return true;
		}
		else if(args.length > 1)
		{
			player.sendMessage(Naxcraft.MSG_COLOR + "You may only ignore one person per /ignore command.");
			return true;
		}
		
		NaxPlayer p2 = pm.getPlayer(args[0]);
		
		if(p2 == null)
		{
			player.sendMessage(Naxcraft.MSG_COLOR + "There is no one who has that name. (" + args[0] + ")");
			return true;
		}
		else if (p2.name.equalsIgnoreCase(player.getName()))
		{
			player.sendMessage(Naxcraft.MSG_COLOR + "You cannot ignore yourself!");
			return true;
		}
		else if (p2.rank.isDemiAdmin())
		{
			player.sendMessage(Naxcraft.MSG_COLOR + "You cannot mute an Demi Moderator or higher.");
			return true;
		}
		
		NaxPlayer p = pm.getPlayer(player);
		
		boolean ignored = pm.addIgnoredPlayer(p, p2);
		
		if(ignored)
		{
			player.sendMessage(Naxcraft.MSG_COLOR + "The player " + pm.getDisplayName(p2) + " has been ignored!");
		}
		else
		{
			player.sendMessage(Naxcraft.MSG_COLOR + "The player " + pm.getDisplayName(p2)+ " is no longer ignored.");
		}
		
		
		return true;
	}
	
	public void printHelp(Player player)
	{
		player.sendMessage(Naxcraft.MSG_COLOR + "----");
		player.sendMessage(Naxcraft.MSG_COLOR + "In order to ignore someone or unignore them, do the command /ignore <name>");
	}
}
