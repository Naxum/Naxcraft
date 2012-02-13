package com.naxville.naxcraft.autoareas;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowman;

public class SnowmenManager
{
	public AutoAreaManager aam;
	
	public Map<AutoBase, List<Player>> activeBases = new HashMap<AutoBase, List<Player>>();
	public Map<AutoBase, List<Player>> activeBasesToAdd = new HashMap<AutoBase, List<Player>>();
	public Map<AutoBase, List<Player>> activeBasesToRemove = new HashMap<AutoBase, List<Player>>();
	
	public SnowmenManager(AutoAreaManager aam)
	{
		this.aam = aam;
	}
	
	public void startSnowmenTargeter()
	{
		aam.plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(aam.plugin, new SnowmanTargeter(), 100L, 20L);
	}
	
	public void enterSnowmenBase(Player player, AutoBase base)
	{
		if (!activeBasesToAdd.containsKey(base))
		{
			activeBasesToAdd.put(base, new ArrayList<Player>());
		}
		
		activeBasesToAdd.get(base).add(player);
	}
	
	public void exitSnowmenBase(Player player, AutoBase base)
	{
		if (!activeBasesToRemove.containsKey(base))
		{
			activeBasesToRemove.put(base, new ArrayList<Player>());
		}
		
		activeBasesToRemove.get(base).add(player);
	}
	
	public List<Snowman> getNearbySnowmen(List<Snowman> snowmen, Player player)
	{
		List<Snowman> closeSnowmen = new ArrayList<Snowman>();
		
		for (Snowman s : snowmen)
		{
			if (s == null || s.getLocation() == null) continue;
			
			if (s.getLocation().distance(player.getLocation()) < 20)
			{
				closeSnowmen.add(s);
			}
		}
		
		return closeSnowmen;
	}
	
	public List<Snowman> getSnowmen(AutoBase base)
	{
		List<Snowman> snowmen = new ArrayList<Snowman>();
		
		for (Point p : base.chunks)
		{
			Entity[] entities = base.world.getChunkAt(p.x, p.y).getEntities();
			
			for (Entity e : entities)
			{
				if (e instanceof Snowman)
				{
					snowmen.add((Snowman) e);
				}
			}
		}
		
		return snowmen;
	}
	
	public class SnowmanTargeter implements Runnable
	{
		public void run()
		{
			// System.out.println("Checking " + activeBases.size() + " bases.");
			
			for (AutoBase base : activeBases.keySet())
			{
				List<Snowman> snowmen = getSnowmen(base);
				
				for (Player p : activeBases.get(base))
				{
					if (!p.isOnline() || p.getWorld() != base.world)
					{
						exitSnowmenBase(p, base);
						continue;
					}
					
					if (base.isOwner(p))
					{
						continue;
					}
					
					for (Snowman s : getNearbySnowmen(snowmen, p))
					{
						s.setTarget(p);
					}
				}
			}
			
			activeBases.putAll(activeBasesToAdd);
			activeBasesToAdd.clear();
			activeBases.remove(activeBasesToRemove);
			activeBasesToRemove.clear();
			
			for (AutoBase base : activeBases.keySet())
			{
				if (activeBases.get(base).isEmpty())
				{
					activeBases.remove(base);
				}
			}
		}
	}
}
