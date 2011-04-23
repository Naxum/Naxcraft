package com.naxville.naxcraft;

import java.lang.Runnable;
import org.bukkit.entity.Player;

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
			String rank = plugin.control.getGroup(player.getName().toLowerCase(), true).getName();
			int x = 1;
			if(rank != null) {
				if(rank.equalsIgnoreCase("member")){
					x = 2;
					
				} else if (rank.equalsIgnoreCase("veteran")){
					x = 3;
					
				} else if (rank.equalsIgnoreCase("patron")){
					x = 4;
					
				} else if (rank.equalsIgnoreCase("moderator")){
					x = 5;
					
				} else if (rank.equalsIgnoreCase("admin")){
					x = 6;
					
				} 	
			}
			
			if(plugin.regenValues.containsKey(player.getName()))
			{
				if(plugin.regenValues.get(player.getName()) <= 1)
				{					
					if((player.getHealth() < 20) && (player.getHealth() > 0)) {
						player.setHealth(player.getHealth() + ((int)Math.ceil(1*(x/2))));
					}
					plugin.regenValues.put(player.getName(), (int)Math.ceil(10 / x));
					
				} else {
					plugin.regenValues.put(player.getName(), (plugin.regenValues.get(player.getName()) - 1));
				}
			} else {
				
				plugin.regenValues.put(player.getName(), (int)Math.ceil(10 / x));
			}
		}
		
		//plugin.stealthCommand.updateInvisibles();
	}
}
