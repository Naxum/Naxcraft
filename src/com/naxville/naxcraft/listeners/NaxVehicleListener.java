package com.naxville.naxcraft.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.event.vehicle.VehicleUpdateEvent;
import org.bukkit.util.Vector;

import com.naxville.naxcraft.NaxColor;
import com.naxville.naxcraft.Naxcraft;
import com.naxville.naxcraft.admin.SuperManager;
import com.naxville.naxcraft.autoareas.AutoAreaManager.Flag;
import com.naxville.naxcraft.autoareas.AutoBase;

public class NaxVehicleListener implements Listener
{
	public Naxcraft plugin;
	
	public List<Material> rails = new ArrayList<Material>();
	public List<Material> carts = new ArrayList<Material>();
	
	public Map<Vehicle, Integer> minecarts = new HashMap<Vehicle, Integer>();
	public Map<Vehicle, Integer> stopped = new HashMap<Vehicle, Integer>();
	
	public NaxVehicleListener(Naxcraft plugin)
	{
		this.plugin = plugin;
		rails.add(Material.RAILS);
		rails.add(Material.POWERED_RAIL);
		rails.add(Material.DETECTOR_RAIL);
		
		carts.add(Material.MINECART);
		carts.add(Material.POWERED_MINECART);
		carts.add(Material.STORAGE_MINECART);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onVehicleDamage(VehicleDamageEvent event)
	{
		AutoBase base = plugin.autoAreaManager.getBase(event.getVehicle().getLocation());
		
		if (base != null && base.hasFlag(Flag.RAILWAY))
		{
			if (!event.getVehicle().isEmpty() && !(event.getAttacker() instanceof Player && SuperManager.isSuper(((Player) event.getAttacker()))))
			{
				if (event.getAttacker() instanceof Player) ((Player) event.getAttacker()).sendMessage(NaxColor.MSG + "You may not break a minecart with a rider in a railway!");
				event.setCancelled(true);
				return;
			}
			
			event.getVehicle().remove();
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onVehicleDestroy(VehicleDestroyEvent event)
	{
		AutoBase base = plugin.autoAreaManager.getBase(event.getVehicle().getLocation());
		
		if (base != null && base.hasFlag(Flag.RAILWAY))
		{
			event.getVehicle().remove();
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onVehicleEnter(VehicleEnterEvent event)
	{
		System.out.println("Entering.");
		if (!event.getVehicle().isEmpty()) return;
		
		AutoBase base = plugin.autoAreaManager.getBase(event.getVehicle().getLocation());
		
		if (base == null || !base.hasFlag(Flag.RAILWAY)) return;
		
		activateTrack(event.getVehicle());
	}
	
	public void activateTrack(Vehicle v)
	{
		Block b = v.getLocation().getBlock();
		Block below = b.getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN);
		
		if (b.getType() == Material.POWERED_RAIL && !b.isBlockPowered() && below.getType() == Material.GLASS)
		{
			below.setType(Material.REDSTONE_TORCH_ON);
			
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new RedstoneTorchResetter(below), 20);
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onVehicleExit(VehicleExitEvent event)
	{
		System.out.println("Exiting.");
		AutoBase base = plugin.autoAreaManager.getBase(event.getVehicle().getLocation());
		
		if (base == null || !base.hasFlag(Flag.RAILWAY) || !(event.getExited() instanceof Player)) return;
		
		Block rails = event.getVehicle().getLocation().getBlock();
		Block below = rails.getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN);
		
		if (rails.getType() != Material.POWERED_RAIL || rails.getBlockPower() != 0 || (below.getType() != Material.GLASS && below.getType() != Material.BRICK))
		{
			if (event.getExited() instanceof Player) ((Player) event.getExited()).sendMessage(NaxColor.MSG + "You may not leave until you reach your destination!");
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new PassengerReturner(event.getExited(), event.getVehicle()), 1);
		}
		else
		{
			List<Block> checked = new ArrayList<Block>();
			Block current = below;
			
			for (int i = 0; i < 10; i++)
			{
				Block b1 = current.getRelative(BlockFace.NORTH);
				Block b2 = current.getRelative(BlockFace.EAST);
				Block b3 = current.getRelative(BlockFace.WEST);
				Block b4 = current.getRelative(BlockFace.SOUTH);
				
				if (b1.getType() == Material.GLASS && !checked.contains(b1))
				{
					current = b1;
					checked.add(b1);
					continue;
				}
				else if (b2.getType() == Material.GLASS && !checked.contains(b2))
				{
					current = b2;
					checked.add(b2);
					continue;
				}
				else if (b3.getType() == Material.GLASS && !checked.contains(b3))
				{
					current = b3;
					checked.add(b3);
					continue;
				}
				else if (b4.getType() == Material.GLASS && !checked.contains(b4))
				{
					current = b4;
					checked.add(b4);
					continue;
				}
				else
				{
					break;
				}
			}
			
			Entity e = event.getVehicle().getPassenger();
			event.getVehicle().remove();
			
			System.out.println("Final stop is " + current.getLocation().distance(below.getLocation()) + " blocks away.");
			
			if (e instanceof Player) ((Player) e).sendMessage(NaxColor.MSG + "You have reached your destination!");
			
			Location l = current.getRelative(BlockFace.UP).getRelative(BlockFace.UP).getLocation().add(0.5, 0, 0.5);
			
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new PlayerTeleporter(e, l), 1);
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onVehicleUpdate(VehicleUpdateEvent event)
	{
		Vehicle v = event.getVehicle();
		Block b = v.getLocation().getBlock();
		
		if (minecarts.containsKey(event.getVehicle()) &&
				(b.getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN).getType() == Material.BRICK ||
				b.getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN).getType() == Material.GLASS) &&
				v.getVelocity().equals(new Vector(0, 0, 0))) { return; }
		
		AutoBase base = plugin.autoAreaManager.getBase(event.getVehicle().getLocation());
		
		if (base == null || !base.hasFlag(Flag.RAILWAY)) return;
		
		if (v.isEmpty())
		{
			if (!minecarts.containsKey(v))
			{
				minecarts.put(v, 25);
				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new EmptyCartChecker(v), 2);
				
				return;
			}
		}
		
		if (b.getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN).getType() == Material.GLASS &&
				b.getType() == Material.POWERED_RAIL && b.getBlockPower() == 0 &&
				v.getVelocity().equals(new Vector(0, 0, 0)))
		{
			if (stopped.containsKey(v)) return;
			
			stopped.put(v, 15);
			
			if (v.getPassenger() instanceof Player) ((Player) v.getPassenger()).sendMessage(NaxColor.MSG + "If you want to get off at this stop, right click your Minecart, otherwise wait.");
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new StoppedCartChecker(v), 2);
			
			return;
		}
		
		if (b.getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN).getType() == Material.BRICK &&
				b.getType() == Material.POWERED_RAIL && b.getBlockPower() == 0 &&
				v.getVelocity().equals(new Vector(0, 0, 0)))
		{
			v.eject();
			v.remove();
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onVehicleMove(VehicleMoveEvent event)
	{
		AutoBase from = plugin.autoAreaManager.getBase(event.getFrom());
		
		if (from == null) return;
		
		AutoBase to = plugin.autoAreaManager.getBase(event.getTo());
		
		if (to == null || from == to) return;
		
		if ((from.hasFlag(Flag.RAILWAY) && !to.hasFlag(Flag.RAILWAY)) || (!from.hasFlag(Flag.RAILWAY) && to.hasFlag(Flag.RAILWAY)))
		{
			event.getVehicle().remove();
		}
		
	}
	
	public void onVehicleEntityCollision(VehicleEntityCollisionEvent event)
	{
		AutoBase base = plugin.autoAreaManager.getBase(event.getVehicle().getLocation());
		
		if (base == null || !base.hasFlag(Flag.RAILWAY)) return;
		
		/**
		 * TODO: Deal with colliding with players/entities!
		 */
		
		if (event.getVehicle().isEmpty())
		{
			event.getVehicle().remove();
		}
		
		event.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void handlePlayerDropItem(PlayerDropItemEvent event)
	{
		if (event.isCancelled()) return;
		
		AutoBase base = plugin.autoAreaManager.getBase(event.getPlayer());
		
		if (base == null || !base.hasFlag(Flag.RAILWAY)) return;
		
		if (event.getPlayer().getVehicle() != null)
		{
			event.setCancelled(true);
		}
		
		if (rails.contains(event.getItemDrop().getLocation().getBlock().getRelative(BlockFace.DOWN).getType()) ||
				rails.contains(event.getItemDrop().getLocation().getBlock().getRelative(BlockFace.NORTH).getRelative(BlockFace.DOWN).getType()) ||
				rails.contains(event.getItemDrop().getLocation().getBlock().getRelative(BlockFace.SOUTH).getRelative(BlockFace.DOWN).getType()) ||
				rails.contains(event.getItemDrop().getLocation().getBlock().getRelative(BlockFace.EAST).getRelative(BlockFace.DOWN).getType()) ||
				rails.contains(event.getItemDrop().getLocation().getBlock().getRelative(BlockFace.WEST).getRelative(BlockFace.DOWN).getType()))
		{
			event.setCancelled(true);
			return;
		}
		else
		{
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new RailwayItemChecker(event.getPlayer(), event.getItemDrop()), 1);
		}
	}
	
	public class PassengerReturner implements Runnable
	{
		public LivingEntity e;
		public Vehicle v;
		
		public PassengerReturner(LivingEntity e, Vehicle v)
		{
			this.e = e;
			this.v = v;
		}
		
		public void run()
		{
			v.setPassenger(e);
		}
		
	}
	
	public class RailwayItemChecker implements Runnable
	{
		public Player p;
		public Item i;
		
		public RailwayItemChecker(Player p, Item i)
		{
			this.p = p;
			this.i = i;
		}
		
		public void run()
		{
			if (i.getLocation().getBlock().isEmpty())
			{
				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new RailwayItemChecker(p, i), 1);
			}
			else
			{
				if (rails.contains(i.getLocation().getBlock().getType()) ||
						rails.contains(i.getLocation().getBlock().getRelative(BlockFace.NORTH).getType()) ||
						rails.contains(i.getLocation().getBlock().getRelative(BlockFace.EAST).getType()) ||
						rails.contains(i.getLocation().getBlock().getRelative(BlockFace.SOUTH).getType()) ||
						rails.contains(i.getLocation().getBlock().getRelative(BlockFace.WEST).getType()))
				{
					p.getInventory().addItem(i.getItemStack());
					i.remove();
					
					p.sendMessage(NaxColor.MSG + "Do not try to litter the railway.");
				}
			}
		}
		
	}
	
	public class EmptyCartChecker implements Runnable
	{
		public Vehicle v;
		
		public EmptyCartChecker(Vehicle v)
		{
			this.v = v;
		}
		
		public void run()
		{
			int ticksLeft = minecarts.get(v);
			
			if (ticksLeft <= 0)
			{
				minecarts.remove(v);
				v.remove();
			}
			else if (v.isDead() || !v.isEmpty())
			{
				minecarts.remove(v);
			}
			else
			{
				minecarts.put(v, ticksLeft - 1);
				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new EmptyCartChecker(v), 2);
			}
			
		}
		
	}
	
	public class StoppedCartChecker implements Runnable
	{
		public Vehicle v;
		
		public StoppedCartChecker(Vehicle v)
		{
			this.v = v;
		}
		
		public void run()
		{
			int ticksLeft = stopped.get(v);
			
			if (ticksLeft <= 0)
			{
				activateTrack(v);
				stopped.remove(v);
			}
			else if (v.isDead() || v.isEmpty())
			{
				stopped.remove(v);
			}
			else
			{
				stopped.put(v, ticksLeft - 1);
				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new StoppedCartChecker(v), 2);
			}
			
		}
		
	}
	
	public class RedstoneTorchResetter implements Runnable
	{
		public Block b;
		
		public RedstoneTorchResetter(Block b)
		{
			this.b = b;
		}
		
		public void run()
		{
			b.setType(Material.GLASS);
		}
		
	}
	
	public class PlayerTeleporter implements Runnable
	{
		public Entity e;
		public Location l;
		
		public PlayerTeleporter(Entity e, Location l)
		{
			this.e = e;
			this.l = l;
		}
		
		public void run()
		{
			e.teleport(l);
		}
		
	}
}
