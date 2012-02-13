package com.naxville.naxcraft.player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class FireWatcher implements Runnable
{
	private PlayerManager pm;
	
	public FireWatcher(PlayerManager pm)
	{
		this.pm = pm;
	}
	
	public void run()
	{	
		Iterator<Entry<Location, Integer>> i = pm.fireBlocks.entrySet().iterator();
		while(i.hasNext())
		{
			
			Location loc = i.next().getKey();
			World world = loc.getWorld();
			List<Player> players = new ArrayList<Player>();
			
			boolean stop = false;
			
			for(Player player : world.getPlayers())
			{
				double distance = player.getLocation().distance(loc);
				if(distance <= 100)
				{
					
					if(distance < 0.4)
					{
						stop = true;
					}
					
					players.add(player);
				}
			}
			
			if(stop)
			{
				for(Player player : players)
				{
					player.sendBlockChange(loc, world.getBlockAt(loc).getType(), world.getBlockAt(loc).getData());
				}
				
				i.remove();
				continue;
			}
			
			if(pm.fireBlocks.get(loc) > 0)
			{				
				pm.fireBlocks.put(loc, pm.fireBlocks.get(loc)-1);
			}
			else if (pm.fireBlocks.get(loc) <= 0)
			{
				for(Player player : players)
				{
					player.sendBlockChange(loc, world.getBlockAt(loc).getType(), world.getBlockAt(loc).getData());
				}
				
				i.remove();
			}
		}
		
		pm.plugin.getServer().getScheduler().scheduleSyncDelayedTask(pm.plugin, new FireWatcher(pm), 2);
	}
}
