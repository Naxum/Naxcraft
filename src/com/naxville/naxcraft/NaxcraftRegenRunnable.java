package com.naxville.naxcraft;

import java.lang.Runnable;

import org.bukkit.entity.Player;

import com.naxville.naxcraft.player.PlayerManager.PlayerRank;

public class NaxcraftRegenRunnable implements Runnable{
	public static Naxcraft plugin;
	
	public NaxcraftRegenRunnable(Naxcraft instance)
	{
		plugin = instance;
	}
	
	public void run()
	{
		for(Player player : plugin.getServer().getOnlinePlayers())
		{	
			if(plugin.playerManager.getPlayer(player).rank == PlayerRank.NOOB)
			{
				continue;
			}
			
			int x = plugin.playerManager.getPlayer(player).rank.getId() + 1;
			
			if(plugin.regenValues.containsKey(player.getName()))
			{
				if(plugin.regenValues.get(player.getName()) <= 1)
				{					
					if((player.getHealth() < 20) && (player.getHealth() > 0)) {
						player.setHealth(player.getHealth() + ((int)Math.ceil(1*(x/2))));
					}
					plugin.regenValues.put(player.getName(), (int)Math.ceil(10 / x));
					
				} 
				else 
				{
					plugin.regenValues.put(player.getName(), (plugin.regenValues.get(player.getName()) - 1));
				}
			} 
			else 
			{
				plugin.regenValues.put(player.getName(), (int)Math.ceil(10 / x));
			}
		}
		
		//plugin.stealthCommand.updateInvisibles();
	}
}
