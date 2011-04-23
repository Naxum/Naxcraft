package com.naxville.naxcraft;

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
	private String warp;
	
	public NaxcraftWarpRunnable(Naxcraft instance, Player player, String warp)
	{
		plugin = instance;
		this.player = player;
		this.warp = warp;
	}
	
	public void run()
	{
		plugin.warpCommand.warpPlayer(player, warp);
		plugin.warpingPlayers.remove(player.getName());
		player.sendMessage(Naxcraft.SUCCESS_COLOR + "You made it in one piece! Oh god! Where is your left hand?!");
	}
	
}
