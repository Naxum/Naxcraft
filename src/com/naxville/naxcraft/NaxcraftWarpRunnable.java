package com.naxville.naxcraft;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/*
 * Usage:
 * if(!plugin.warpingPlayers.containsKey(<warpingPlayer>.getName()))
 * {
 *      plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new NaxcraftWarpRunnable(plugin, <warpingPlayer>, <warpLocation>), <(delay in seconds * 20)>);
 * }
 */

public class NaxcraftWarpRunnable implements Runnable{
	private static Naxcraft plugin;
	private Player player;
	private Location location;
	
	public NaxcraftWarpRunnable(Naxcraft instance, Player player, Location location)
	{
		plugin = instance;
		this.player = player;
		this.location = location;
	}
	
	public void run()
	{
		plugin.homeCommand.warpPlayer(player, location);
		plugin.warpingPlayers.remove(player.getName());
		player.sendMessage(Naxcraft.SUCCESS_COLOR + "You made it!");
	}
	
}
