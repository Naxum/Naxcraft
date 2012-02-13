package com.naxville.naxcraft.admin;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.naxville.naxcraft.NaxColor;
import com.naxville.naxcraft.Naxcraft;

public class NaxcraftBoomCommand
{
	public Naxcraft plugin;
	
	public NaxcraftBoomCommand(Naxcraft plugin)
	{
		this.plugin = plugin;
	}
	
	public boolean runCommand(CommandSender sender, String[] args)
	{
		if (!(sender instanceof Player))
		{
			sender.sendMessage("Get outta here.");
			return true;
		}
		
		Player player = (Player) sender;
		
		if (!plugin.playerManager.getPlayer(player).rank.isAdmin())
		{
			player.sendMessage(Naxcraft.MSG_COLOR + "Get outta here!");
			return true;
		}
		
		Location loc;
		boolean boom = false;
		
		if (args.length == 0)
		{
			loc = player.getTargetBlock(null, 150).getRelative(0, 1, 0).getLocation();
			
		}
		else if (args.length == 1)
		{
			Player target = player.getServer().getPlayer(args[0]);
			
			if (target == null)
			{
				player.sendMessage(Naxcraft.MSG_COLOR + "No player named " + args[0] + " online.");
				return true;
			}
			
			boom = true;
			loc = target.getLocation();
		}
		else
		{
			System.out.println(args.length);
			return false;
		}
		
		if (boom) player.sendMessage(NaxColor.MSG + "Boom!");
		
		loc.getWorld().createExplosion(loc, 5f, false);
		
		return true;
	}
}
