package com.naxville.naxcraft.listeners;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.inventory.ItemStack;

import com.naxville.naxcraft.Naxcraft;
import com.naxville.naxcraft.autoareas.AutoAreaManager.Flag;
import com.naxville.naxcraft.autoareas.AutoBase;

public class NaxInventoryListener implements Listener
{
	Naxcraft plugin;
	public List<Location> forges = new ArrayList<Location>();
	public List<Location> forgesToAdd = new ArrayList<Location>();
	
	public NaxInventoryListener(Naxcraft plugin)
	{
		this.plugin = plugin;
	}
	
	public void startForgeSmelter()
	{
		plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, new ForgeSmelter(), 100L, 5L);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onFurnaceBurn(FurnaceBurnEvent event)
	{
		AutoBase base = plugin.autoAreaManager.getBase(event.getBlock());
		
		if (base != null && base.hasFlag(Flag.FORGES))
		{
			if (!forges.contains(event.getBlock().getLocation()))
			{
				forgesToAdd.add(event.getBlock().getLocation());
			}
			
			if (event.getFuel().getType() == Material.LAVA_BUCKET)
			{
				Furnace f = (Furnace) event.getBlock().getState();
				
				f.setBurnTime((short) 600);
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onFurnaceSmelt(FurnaceSmeltEvent event)
	{
		if (forges.contains(event.getBlock().getLocation())) return;
		
		AutoBase base = plugin.autoAreaManager.getBase(event.getBlock());
		
		if (base != null && base.hasFlag(Flag.FORGES))
		{
			forgesToAdd.add(event.getBlock().getLocation());
		}
	}
	
	public class ForgeSmelter implements Runnable
	{
		public void run()
		{
			Iterator<Location> i = forges.iterator();
			while (i.hasNext())
			{
				Location l = i.next();
				
				if (l.getBlock() == null)
				{
					i.remove();;
					continue;
				}
				
				Block b = l.getBlock();
				
				if (b.getType() != Material.FURNACE && b.getType() != Material.BURNING_FURNACE)
				{
					i.remove();;
					continue;
				}
				
				Furnace f = (Furnace) b.getState();
				
				ItemStack source = f.getInventory().getItem(0);
				ItemStack feul = f.getInventory().getItem(1);
				// ItemStack result = f.getInventory().getItem(2);
				
				if (source == null || source.getAmount() == 0 || feul == null || feul.getAmount() == 0 || f.getBurnTime() == 0)
				{
					i.remove();;
					continue;
				}
				
				if (feul.getType() == Material.COAL)
				{
					f.setCookTime((short) (f.getCookTime() + 10));
				}
				else if (feul.getType() == Material.LAVA_BUCKET)
				{
					f.setBurnTime((short) 300);
					f.setCookTime((short) (f.getCookTime() + 25));
				}
			}
			
			forges.addAll(forgesToAdd);
			forgesToAdd.clear();
		}
	}
	
}
