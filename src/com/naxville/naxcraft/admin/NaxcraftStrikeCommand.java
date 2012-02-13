package com.naxville.naxcraft.admin;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.naxville.naxcraft.Naxcraft;

public class NaxcraftStrikeCommand
{
	public Naxcraft plugin;
	private Map<Player, Date> messages = new HashMap<Player, Date>();
	private final static int MINUTES_UNTIL_NEXT_MESSAGE = 1;
	
	public NaxcraftStrikeCommand(Naxcraft plugin)
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
		String targetStr = "";
		
		if (args.length == 0)
		{
			loc = player.getTargetBlock(null, 150).getRelative(0, 1, 0).getLocation();
			targetStr = Naxcraft.WORLD_COLOR + plugin.getWorldName(player.getWorld());
			
		}
		else if (args.length == 1)
		{
			Player target = player.getServer().getPlayer(args[0]);
			
			if (target == null)
			{
				player.sendMessage(Naxcraft.MSG_COLOR + "No player named " + args[0] + " online.");
				return true;
			}
			
			targetStr = plugin.getNickName(target.getName());
			loc = target.getLocation();
			
		}
		else
		{
			return false;
		}
		
		if (messages.containsKey(player))
		{
			if ((new Date().getTime() - messages.get(player).getTime()) / (1000 * 60) > MINUTES_UNTIL_NEXT_MESSAGE)
			{
				messages.put(player, new Date());
				player.getServer().broadcastMessage(plugin.getNickName(player.getName()) + Naxcraft.MSG_COLOR + "'s fury has crashed down upon " + targetStr + Naxcraft.MSG_COLOR + "!");
			}
		}
		else
		{
			messages.put(player, new Date());
			player.getServer().broadcastMessage(plugin.getNickName(player.getName()) + Naxcraft.MSG_COLOR + "'s fury has crashed down upon " + targetStr + Naxcraft.MSG_COLOR + "!");
		}
		
		loc.getWorld().strikeLightning(loc);
		loc.getWorld().createExplosion(loc, 4f);
		
		return true;
	}
}
