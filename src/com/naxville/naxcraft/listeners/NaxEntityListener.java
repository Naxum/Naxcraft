package com.naxville.naxcraft.listeners;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.painting.PaintingBreakByEntityEvent;
import org.bukkit.event.painting.PaintingBreakEvent;
import org.bukkit.event.painting.PaintingBreakEvent.RemoveCause;
import org.bukkit.event.painting.PaintingPlaceEvent;
import org.bukkit.inventory.ItemStack;

import com.naxville.naxcraft.NaxColor;
import com.naxville.naxcraft.Naxcraft;
import com.naxville.naxcraft.autoareas.AutoAreaManager.Flag;
import com.naxville.naxcraft.autoareas.AutoBase;

public class NaxEntityListener implements Listener
{
	public static Naxcraft plugin;
	public List<Location> tntExplosions = new ArrayList<Location>();
	
	public NaxEntityListener(Naxcraft instance)
	{
		plugin = instance;
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onExplosionPrime(ExplosionPrimeEvent event)
	{
		if (event.getEntity() instanceof TNTPrimed)
		{
			tntExplosions.add(event.getEntity().getLocation());
		}
		
		event.setFire(false);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onItemDespawn(ItemDespawnEvent event)
	{
		plugin.displayShopManager.handleItemDespawn(event);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPaintingBreak(PaintingBreakEvent event)
	{
		if(event.getCause() == RemoveCause.ENTITY)
		{
			Entity remover = ((PaintingBreakByEntityEvent)event).getRemover();
			
			AutoBase base = plugin.autoAreaManager.getBase(event.getPainting().getLocation());
			
			if(base == null) return;
			
			if(remover instanceof Player)
			{
				if(!base.isOwner((Player)remover))
				{
					((Player)remover).sendMessage(NaxColor.MSG + "You may not edit protected paintings.");
					event.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPaintingPlace(PaintingPlaceEvent event)
	{
		AutoBase base = plugin.autoAreaManager.getBase(event.getBlock());
		
		if(base == null) return;
		
		if(!base.isOwner(event.getPlayer()))
		{
			event.getPlayer().sendMessage(NaxColor.MSG + "You may not place paintings in this area.");
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onFoodLevelChange(FoodLevelChangeEvent event)
	{
		if (!(event.getEntity() instanceof Player)) return;
		
		Player player = (Player) event.getEntity();
		AutoBase base = plugin.autoAreaManager.getBase(player);
		
		if (base == null || base.hasFlag(Flag.FOOD))
		{
			if (player.getFoodLevel() > event.getFoodLevel())
			{
				if (new Random().nextInt(2) == 1)
				{
					player.setExhaustion(0);
					player.setSaturation(1);
					event.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onEntityExplode(EntityExplodeEvent event)
	{
		if (tntExplosions.contains(event.getLocation()))
		{
			tntExplosions.remove(event.getLocation());
			return;
		}
		
		List<Block> blocks = new ArrayList<Block>();
		List<Integer> materials = new ArrayList<Integer>();
		List<Byte> datas = new ArrayList<Byte>();
		
		plugin.displayShopManager.handleExplosion(event);
		
		if (event.isCancelled()) return;
		
		Iterator<Block> it = event.blockList().iterator();
		while (it.hasNext())
		{
			Block b = it.next();
			if (b.getType() == Material.CHEST || b.getType() == Material.DISPENSER || b.getType() == Material.FURNACE || b.getType() == Material.RAILS ||
					b.getType() == Material.POWERED_RAIL || b.getType() == Material.SIGN_POST || b.getType() == Material.WALL_SIGN || b.getType() == Material.OBSIDIAN)
			{
				event.setCancelled(true);
				return;
			}
			
			blocks.add(b);
			materials.add(b.getTypeId());
			datas.add(b.getData());
		}
		
		for (Block b : blocks)
		{
			b.setTypeId(0);
		}
		if (event.getLocation().getY() > (event.getLocation().getWorld().getMaxHeight() / 2) - 10 || plugin.autoAreaManager.getBase(event.getLocation()) != null)
		{
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new ExplosionFixer(blocks, materials, datas), 20);
		}
		
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onEntityTarget(EntityTargetEvent event)
	{
		if (event.getTarget() instanceof Player)
		{
			if (plugin.stealthCommand.isInvisible((Player) event.getTarget()))
			{
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onCreatureSpawn(CreatureSpawnEvent event)
	{
		plugin.bloodMoonManager.handleCreatureSpawn(event);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onEntityCombust(EntityCombustEvent event)
	{
		if (event.getEntity() instanceof Player)
		{
			Player player = (Player) event.getEntity();
			if (plugin.godCommand.godded(player.getName()))
			{
				event.setCancelled(true);
				event.getEntity().setFireTicks(0);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onEntityDamage(EntityDamageEvent event)
	{
		if (event.getEntity() instanceof Player)
		{
			if (plugin.godCommand.godded(((Player) event.getEntity()).getName()))
			{
				((Player) event.getEntity()).setFireTicks(0);
				event.setCancelled(true);
				return;
			}
		}
		
		if (event.getEntity() instanceof Wolf)
		{
			Wolf w = (Wolf) event.getEntity();
			if (w.isTamed())
			{
				if (event.getCause() == DamageCause.FALL)
				{
					event.setCancelled(true);
					return;
				}
				else if (event.getCause() == DamageCause.ENTITY_ATTACK && event instanceof EntityDamageByEntityEvent)
				{
					EntityDamageByEntityEvent ev = (EntityDamageByEntityEvent) event;
					if (ev.getDamager() == w.getOwner())
					{
						event.setCancelled(true);
						return;
					}
				}
			}
		}
		
		plugin.bloodMoonManager.handleEntityDamage(event);
		
		plugin.autoAreaManager.handleEntityDamage(event);
		
		plugin.playerManager.handleEntityDamage(event);
		plugin.cityManager.handleEntityDamage(event);
		
		if (!event.isCancelled() && event.getEntity() instanceof Player && !event.getEntity().getWorld().getName().startsWith("aether"))
		{
			plugin.regenValues.put(((Player) event.getEntity()).getName(), 30);
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onEntityChangeBlock(EntityChangeBlockEvent event)
	{
		event.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onEntityDeath(EntityDeathEvent event)
	{
		plugin.bloodMoonManager.handleEntityDeath(event);
		
		if (event.getEntity() instanceof Creature)
		{
			Creature creature = (Creature) event.getEntity();
			
			// if this creature was NOT killed by another entity, outside of a grinder, destroy its shit
			if (!plugin.autoAreaManager.keepDrops.contains(creature.getEntityId()))
			{
				event.getDrops().clear();
			}
			else
			{
				plugin.autoAreaManager.keepDrops.remove((Object) creature.getEntityId());
			}
			
			return;
		}
		
		if (event.getEntity() instanceof Player)
		{
			Player player = (Player) event.getEntity();
			
			plugin.announcer.handleEntityDeath(event);
			
			if (plugin.warpingPlayers.containsKey(player.getName()))
			{
				plugin.getServer().getScheduler().cancelTask(plugin.warpingPlayers.get(player.getName()));
				plugin.warpingPlayers.remove(player.getName());
				player.sendMessage(Naxcraft.ERROR_COLOR + "Way to get hurt while teleporting! I'm telling mom!");
			}
			
			if (plugin.regenValues.containsKey(player.getName()))
			{
				plugin.regenValues.put(player.getName(), 30);
			}
			
			if (plugin.destroyDrops.containsKey(player))
			{
				event.getDrops().clear();
				
				if (plugin.playerManager.getPlayer(player).rank.isAdmin())
				{
					event.getDrops().add(new ItemStack(Material.GOLDEN_APPLE, 1));
				}
			}
			
			if (plugin.playerManager.getPlayer(player).rank.isAdmin())
			{
				event.getDrops().remove(Material.COMPASS);
				event.getDrops().remove(Material.WATCH);
			}
		}
	}
	
	public class ExplosionFixer implements Runnable
	{
		public List<Block> blocks;
		List<Integer> materials;
		List<Byte> datas;
		
		public ExplosionFixer(List<Block> blocks, List<Integer> materials, List<Byte> datas)
		{
			this.blocks = blocks;
			this.materials = materials;
			this.datas = datas;
		}
		
		public void run()
		{
			if (blocks == null || blocks.size() == 0) { return; }
			
			Block b = blocks.get(0);
			
			b.setTypeIdAndData(materials.get(0), datas.get(0), false);
			
			for (Entity e : b.getWorld().getEntities())
			{
				if (e.getLocation().getBlockX() == b.getLocation().getX() && e.getLocation().getBlockZ() == b.getLocation().getBlockZ())
				{
					if (e.getLocation().getBlockY() == b.getY() || e.getLocation().getBlockY() + 1 == b.getY())
					{
						e.getLocation().setY(getHighestEmptyY(e.getLocation()) + 0.5);
					}
				}
			}
			
			blocks.remove(0);
			materials.remove(0);
			datas.remove(0);
			
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new ExplosionFixer(blocks, materials, datas), 1);
		}
		
		public int getHighestEmptyY(Location start)
		{
			Location current = start.clone();
			
			for (int i = 0; i < start.getWorld().getMaxHeight() - start.getY(); i++)
			{
				Block b = start.getWorld().getBlockAt(current.add(0, 1, 0));
				
				if (b == null || b.isEmpty()) return b.getY();
			}
			
			return start.getWorld().getMaxHeight() + 1;
		}
	}
}
