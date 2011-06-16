package com.naxville.naxcraft.atms;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.naxville.naxcraft.Naxcraft;

public class AtmChecker implements Runnable 
{
	private Player player;
	private AtmManager atmManager;
	private Naxcraft plugin;
	private String owner;
	
	public AtmChecker(Player player, Naxcraft plugin, AtmManager atmM, String owner)
	{
		this.player = player;
		this.atmManager = atmM;
		this.plugin = plugin;
		this.owner = owner;
	}
	
	@Override
	public void run() 
	{
		CraftPlayer p = (CraftPlayer)player;
		
		if(p.getHandle().activeContainer == p.getHandle().defaultContainer)
		{
			atmManager.saveAccount(player);
			player.sendMessage(Naxcraft.MSG_COLOR + "Thank you for using " + plugin.getNickName(owner) + Naxcraft.MSG_COLOR + "'s ATM!");
		}
		else
		{
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new AtmChecker(player, plugin, atmManager, owner), 5);
		}
	}

}
