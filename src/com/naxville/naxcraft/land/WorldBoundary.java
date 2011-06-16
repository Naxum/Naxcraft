package com.naxville.naxcraft.land;

import org.bukkit.event.player.PlayerMoveEvent;

import com.naxville.naxcraft.Naxcraft;

public class WorldBoundary 
{
	public static final int NAXVILLE_RADIUS = 1000;
	
	public static void handlePlayerMove(PlayerMoveEvent event)
	{
		if(event.isCancelled())
		{
			return;
		}
		
		if(event.getPlayer().getWorld().getName().startsWith("naxville"))
		{
			int radius = NAXVILLE_RADIUS;
			
			if(event.getPlayer().getWorld().getName().endsWith("_nether"))
			{
				radius /= 8;
			}
			
			if(event.getTo().getX() > radius || event.getTo().getX() < -radius || event.getTo().getZ() > radius || event.getTo().getZ() < -radius)
			{
				if(event.getFrom().getX() > radius || event.getFrom().getX() < -radius || event.getFrom().getZ() > radius || event.getFrom().getZ() < -radius)
				{
					event.getPlayer().teleport(event.getPlayer().getWorld().getSpawnLocation());
					event.getPlayer().sendMessage(Naxcraft.MSG_COLOR + "Please do not try to leave the borders of the world.");
				}
				else
				{
					event.getPlayer().teleport(event.getFrom());
					event.getPlayer().sendMessage(Naxcraft.MSG_COLOR + "You have reached the boundary on this world, please go in another direction.");
				}
				event.setCancelled(true);
			}
		}
	}
}
