package com.naxville.naxcraft.autoareas;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import com.naxville.naxcraft.NaxColor;
import com.naxville.naxcraft.Naxcraft;

public class WorldBoundary
{
	
	public static final int VARINIUS_NORTH = 6000;
	public static final int VARINIUS_SOUTH = 6000;
	public static final int VARINIUS_EAST = 6000;
	public static final int VARINIUS_WEST = 6000;
	
	public static final int AEROVIA = 2000;
	
	public static void handlePlayerMove(PlayerMoveEvent event)
	{
		if (event.isCancelled()) { return; }
		
		if (event.getPlayer().getWorld().getName().startsWith("varinius"))
		{
			Location to = event.getTo();
			if (to.getX() < -VARINIUS_NORTH)
			{
				System.out.println("North");
				bounce(event, -1, 0);
			}
			else if (to.getX() > VARINIUS_SOUTH)
			{
				System.out.println("South");
				bounce(event, 1, 0);
			}
			else if (to.getZ() < -VARINIUS_EAST)
			{
				System.out.println("east");
				bounce(event, 0, -1);
			}
			else if (to.getZ() > VARINIUS_WEST)
			{
				System.out.println("west");
				bounce(event, 0, 1);
			}
		}
		else if(event.getPlayer().getWorld().getName().startsWith("aerovia"))
		{
			Location to = event.getTo();
			if (to.getX() < -AEROVIA)
			{
				bounce(event, -1, 0);
			}
			else if (to.getX() > AEROVIA)
			{
				bounce(event, 1, 0);
			}
			else if (to.getZ() < -AEROVIA)
			{
				bounce(event, 0, -1);
			}
			else if (to.getZ() > AEROVIA)
			{
				bounce(event, 0, 1);
			}
		}
	}
	
	private static void bounce(PlayerMoveEvent event, int x, int z)
	{
		event.setTo(event.getFrom().subtract(x, 0, z));
		event.getPlayer().sendMessage(Naxcraft.MSG_COLOR + "The world currently ends here. Build some towers!");
	}
	
	public static void handleBlockPlace(BlockPlaceEvent event)
	{
		if (event.getPlayer().getWorld().getName().endsWith("_nether") && event.getBlock().getType() == Material.OBSIDIAN)
		{
			event.setCancelled(true);
			event.getPlayer().sendMessage(NaxColor.MSG + "You cannot place obsidian here. Try using /spawn and teleporting to the main portal.");
		}
	}
}
