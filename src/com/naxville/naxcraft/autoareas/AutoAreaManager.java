package com.naxville.naxcraft.autoareas;

import java.awt.Point;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.block.CraftDispenser;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import com.naxville.naxcraft.Naxcraft;
import com.naxville.naxcraft.NaxcraftClan;
import com.naxville.naxcraft.admin.NaxcraftConfiguration;

public class AutoAreaManager 
{
	public Naxcraft plugin;
	public NaxcraftConfiguration config;
	
	public List<AutoBase> bases = new ArrayList<AutoBase>();
	public Map<Player, AutoCounter> counters = new HashMap<Player, AutoCounter>();
	public Map<Player, AutoBase> within = new HashMap<Player, AutoBase>();
	
	public Map<Player, World> cacheWorld = new HashMap<Player, World>();
	public Map<Player, Point> cachePosition = new HashMap<Player, Point>();
	public Map<Player, AutoBase> cacheBase = new HashMap<Player, AutoBase>();
	
	public List<FlagInfo> flags = new ArrayList<FlagInfo>();
	public List<Material> uncreative = new ArrayList<Material>(); //blocks that won't instant break in creative areas.
	public List<String> ids = new ArrayList<String>();
	public List<Integer> keepDrops = new ArrayList<Integer>();
	
	public static final String FILE_NAME = "AutoArea_Data";
	public static final int CHUNK_BUFFER = 6;
	public static final int BASE_BUFFER = 2;
	public static final int SAME_OWNER_BUFFER = 10;
	
	public AutoAreaManager(Naxcraft plugin)
	{
		this.plugin = plugin;
		
		//public
		flags.add(new FlagInfo("PvP", "Protects owners from player damage.", 32, null));
		flags.add(new FlagInfo("Safe", "Protects owners from ALL damage.", 64, "pvp"));
		flags.add(new FlagInfo("NoPvP", "Protects everyone from player damage.", 64, "safe,pvp"));
		flags.add(new FlagInfo("Sanctuary", "Protects everyone from ALL damage.", 128, "nopvp"));
		
		//admin
		flags.add(new FlagInfo("Creative", "Infinite blocks + instant break"));
		flags.add(new FlagInfo("Public", "Allows anyone to build in the area."));
		flags.add(new FlagInfo("Hurt", "Hurts any player attempting to alter an area."));
		flags.add(new FlagInfo("Lock", "Area is locked by an Admin."));
		flags.add(new FlagInfo("Grinder", "Allows mobs to be farmed for items."));
		flags.add(new FlagInfo("Jail", "You cannot escape!"));
		
		uncreative.add(Material.BED_BLOCK);
		uncreative.add(Material.CAKE_BLOCK);
		uncreative.add(Material.BEDROCK);
		uncreative.add(Material.IRON_DOOR_BLOCK);
		uncreative.add(Material.IRON_DOOR);
		uncreative.add(Material.LEVER);
		uncreative.add(Material.NOTE_BLOCK);
		uncreative.add(Material.SIGN);
		uncreative.add(Material.SIGN_POST);
		uncreative.add(Material.WALL_SIGN);
		uncreative.add(Material.WOOD_DOOR);
		uncreative.add(Material.WOODEN_DOOR);
	}
	
	public void shutup(Player player)
	{
		if(counters.containsKey(player))
		{
			counters.get(player).shutup = true;
		}
	}
	
	public boolean flagsCommand(Player player, String args[])
	{
		if(args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("help")))
		{
			player.sendMessage(Naxcraft.MSG_COLOR + "----");
			player.sendMessage(Naxcraft.COMMAND_COLOR + "Flags: (use /flags <name> to see more info)");
			
			for(FlagInfo flag : flags)
			{
				ChatColor color = Naxcraft.SUCCESS_COLOR;
				
				if(flag.adminOnly)
				{
					color = Naxcraft.ADMIN_COLOR;
				}
				
				String f = color + flag.name + Naxcraft.MSG_COLOR + ": ";
				
				if(!flag.adminOnly)
				{
					f += "" + Naxcraft.DEFAULT_COLOR + flag.cost + "g" + Naxcraft.MSG_COLOR + " - ";
				}
				
				f += Naxcraft.MSG_COLOR + flag.description;
				
				if(flag.required != null)
				{
					f += Naxcraft.ERROR_COLOR + " Reqs";
				}
				
				player.sendMessage(f);
			}
		}
		else if(args.length == 1)
		{
			FlagInfo flag = null;
			
			for(FlagInfo f : flags)
			{
				if(args[0].equalsIgnoreCase(f.name))
				{
					flag = f;
				}
			}
			
			if(flag == null)
			{
				player.sendMessage(Naxcraft.MSG_COLOR + "The flag " + Naxcraft.ERROR_COLOR + args[0] + Naxcraft.MSG_COLOR + " does not exist.");
				return true;
			}
			
			ChatColor color = Naxcraft.SUCCESS_COLOR;
			
			if(flag.adminOnly)
			{
				color = Naxcraft.ADMIN_COLOR;
			}
			
			String f = color + flag.name + Naxcraft.MSG_COLOR + ": " + Naxcraft.DEFAULT_COLOR +  flag.cost + "g" + Naxcraft.MSG_COLOR + " - " + flag.description;
			String req = "";
			
			if(flag.required != null)
			{
				req += Naxcraft.ERROR_COLOR + " Prerequisites: ";
				
				int i = 0;
				for(String p : flag.required.split(","))
				{
					if(i > 0) req += Naxcraft.MSG_COLOR + " or ";
					req += Naxcraft.DEFAULT_COLOR + p;
					i++;
				}
			}
			
			if(flag.adminOnly)
			{
				req += Naxcraft.MSG_COLOR + "This flag is only given by admins and cannot be bought.";
			}
			
			player.sendMessage(Naxcraft.MSG_COLOR + "----");
			player.sendMessage(f);
			if(req != "") player.sendMessage(req);
		}
		
		return true;
	}
	
	public Point getPoint(Player player)
	{
		return new Point(player.getLocation().getBlock().getChunk().getX(), player.getLocation().getBlock().getChunk().getZ());
	}
	
	public void handleEntityDamage(EntityDamageEvent event)
	{
		if(event.getEntity() instanceof Player)
		{
			Player player = (Player)event.getEntity();
			
			AutoBase base = getBase(player);
			
			if(base != null)
			{
				if(base.getFlag("sanctuary"))
				{
					event.setCancelled(true);
				}
				else if (base.getFlag("safe"))
				{
					if(isOwner(player, base))
					{
						event.setCancelled(true);
					}
				}
				else if (base.getFlag("pvp") || base.getFlag("nopvp"))
				{
					if(event instanceof EntityDamageByEntityEvent)
					{
						EntityDamageByEntityEvent ev = (EntityDamageByEntityEvent)event;
						if(ev.getDamager() instanceof Player)
						{
							if(base.getFlag("nopvp"))
							{
								event.setCancelled(true);
							}
							else if (base.getFlag("pvp"))
							{
								if(isOwner(player, base))
								{
									event.setCancelled(true);
								}
							}
						}
					}
				}
			}
		}
		else if (event.getEntity() instanceof Creature)
		{
			Creature creature = (Creature)event.getEntity();
			if(creature.getHealth() - event.getDamage() <= 0)
			{
				if(event instanceof EntityDamageByEntityEvent)
				{
					keepDrops.add(creature.getEntityId());
				}
				else
				{
					AutoBase base = getBase(creature.getLocation().getBlock());
					
					if(base != null && base.getFlag("grinder"))
					{
						keepDrops.add(creature.getEntityId());
					}
				}
			}
		}
	}
	
	public void refreshCache(Player player)
	{
		if(cachePosition.containsKey(player))
			cachePosition.remove(player);
		
		if(cacheWorld.containsKey(player))
			cacheWorld.remove(player);
		
		if(cacheBase.containsKey(player))
			cacheBase.remove(player);
	}
	
	public void updateCaches(Point point)
	{
		for(Player player : cachePosition.keySet())
		{
			if(cachePosition.get(player) == point)
			{
				cachePosition.put(player, null);
				cacheBase.put(player, null);
			}
		}
	}
	
	public void handlePlayerQuit(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();
		
		cachePosition.remove(player);
		cacheWorld.remove(player);
		cacheBase.remove(player);
	}
	
	public void handlePlayerMove(PlayerMoveEvent event)
	{	
		Player player = event.getPlayer();
		
		if(player.getWorld().getName().startsWith("old_naxville"))
		{
			refreshCache(player);
			return;
		}
		
		if(!cachePosition.containsKey(player) || cachePosition.get(player) == null)
		{
			cachePosition.put(player, getPoint(player));
			cacheBase.put(player, getBase(player));
		}
		
		if(cacheWorld.containsKey(player))
		{
			if(cacheWorld.get(player).getName() != player.getWorld().getName())
			{
				cachePosition.put(player, null);
				cacheBase.put(player, null);
			}
		}
		
		if(cachePosition.get(player) == getPoint(player))
		{
			return;
		}
		else
		{
			cachePosition.put(player, getPoint(player));
			cacheBase.put(player, getBase(player));
		}
		
		AutoBase base = cacheBase.get(player);
		
		if(!within.containsKey(player))
			within.put(player, null);
		
		if(base == null)
		{
			if(within.get(player) != null)
			{
				player.sendMessage(Naxcraft.MSG_COLOR + "You are now in unprotected land.");
				within.put(player, null);
			}
			
		}
		else
		{			
			if(within.get(player) != base)
			{
				if(isOwner(player, base))
				{
					player.sendMessage(Naxcraft.MSG_COLOR + "You are now in land you own. Use /property for more info.");
					within.put(player, base);
				}
				else
				{
					player.sendMessage(Naxcraft.MSG_COLOR + "You are now in " + base.getFounderName() + Naxcraft.MSG_COLOR + "'s land. Use /property for more info.");
					within.put(player, base);
				}
			}
		}
	}
	
	public void handleBlockDamage(BlockDamageEvent event)
	{	
		AutoBase base = null;
		if(getPoint(event.getPlayer()) == getPoint(event.getBlock()))
		{
			base = cacheBase.get(event.getPlayer());
		} 
		else
		{
			base = getBase(event.getBlock());
		}
				
		if(base != null)
		{
			if(base.getFlag("creative") && isOwner(event.getPlayer(), base) && !uncreative.contains(event.getBlock().getType()))
			{
				if(!event.getPlayer().getItemInHand().getType().toString().toLowerCase().startsWith("gold_"))
				{
					event.setInstaBreak(true);
				}
			}
		}
	}
	
	private Point getPoint(Block block) 
	{
		return new Point(block.getChunk().getX(), block.getChunk().getZ());
	}

	public void handleBlockBreak(BlockBreakEvent event)
	{
		if(event.isCancelled()) return;
		
		AutoBase base = null;
		if(getPoint(event.getPlayer()) == getPoint(event.getBlock()))
		{
			base = cacheBase.get(event.getPlayer());
		} 
		else
		{
			base = getBase(event.getBlock());
		}
			
		if(base != null)
		{
			if(isOwner(event.getPlayer(), base))
			{				
				if(base.getFlag("creative"))
				{					
					event.getBlock().setType(Material.AIR);
				}
			}
			else
			{
				event.getPlayer().sendMessage(Naxcraft.MSG_COLOR + "This area is protected, you may not destroy here.");
				event.setCancelled(true);
			}
		}
	}
	
	public void handleBlockPlace(BlockPlaceEvent event)
	{
		if(event.isCancelled()) return;
		
		AutoBase base = null;
		Player player = event.getPlayer();
		
		if(getPoint(event.getPlayer()) == getPoint(event.getBlock()))
		{
			base = cacheBase.get(event.getPlayer());
		} 
		else
		{
			base = getBase(event.getBlock());
		}
		
		if(base != null)
		{
			
			if(!isOwner(player, base) && !plugin.superCommand.isSuper(player.getName()))
			{			
				event.getPlayer().sendMessage(Naxcraft.MSG_COLOR + "You may not build here, this area is protected.");
				event.setCancelled(true);
			}
			else
			{
				if(base.getFlag("creative"))
				{
					event.getPlayer().getItemInHand().setAmount(event.getPlayer().getItemInHand().getAmount()+1);
				}
			}
		}
		else
		{
			count(player, event.getBlock());
		}
	}
	
	public void handlePlayerInteract(PlayerInteractEvent event)
	{
		if(event.isCancelled()) return;
		
		if(plugin.superCommand.isSuper(event.getPlayer().getName())) return;
		
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK)
		{
			if(event.getClickedBlock().getType() == Material.DISPENSER)
			{
				if(!canInteract(event.getPlayer(), event.getClickedBlock()))
				{
					event.setCancelled(true);
					event.getPlayer().sendMessage(Naxcraft.MSG_COLOR + "You may not open others' dispensers.");
				}
			}
			else if(event.getClickedBlock().getType() == Material.TRAP_DOOR)
			{
				if(!canInteract(event.getPlayer(), event.getClickedBlock()))
				{
					event.setCancelled(true);
					event.getPlayer().sendMessage(Naxcraft.MSG_COLOR + "You may not interact with others' trapdoors.");
				}
			}
			else if 
			(
			   event.getItem() != null &&
			   (
			   event.getItem().getType() == Material.PAINTING || 
			   event.getItem().getType() == Material.BUCKET ||
			   event.getItem().getType() == Material.LAVA_BUCKET ||
			   event.getItem().getType() == Material.WATER_BUCKET ||
			   event.getItem().getType() == Material.SIGN ||
			   event.getItem().getType() == Material.SIGN_POST || 
			   event.getItem().getType() == Material.BOAT ||
			   event.getItem().getType() == Material.FLINT_AND_STEEL ||
			   event.getItem().getType() == Material.MINECART ||
			   event.getItem().getType() == Material.POWERED_MINECART ||
			   event.getItem().getType() == Material.REDSTONE ||
			   event.getItem().getType() == Material.REDSTONE_TORCH_ON ||
			   event.getItem().getType() == Material.REDSTONE_TORCH_OFF ||
			   event.getItem().getType() == Material.REDSTONE_WIRE || 
			   event.getItem().getType() == Material.STORAGE_MINECART
			   )
			)
			{
				if(!canInteract(event.getPlayer(), event.getClickedBlock()))
				{
					event.setCancelled(true);
					event.getPlayer().sendMessage(Naxcraft.MSG_COLOR + "You may not place that in this protected area.");
				}
			}
		}
		else if (event.getAction() == Action.LEFT_CLICK_BLOCK)
		{
			if(event.getClickedBlock().getType() == Material.TNT)
			{
				AutoBase base = getBase(event.getClickedBlock());
				
				if(base == null)
				{
					if(plugin.superCommand.isSuper(event.getPlayer().getName()))
					{
						return;
					}
					
					event.setCancelled(true);
					event.getPlayer().sendMessage(Naxcraft.MSG_COLOR + "You cannot activate TNT in the wilderness.");
				}
				else if(!isOwner(event.getPlayer(), base))
				{
					event.setCancelled(true);
					event.getPlayer().sendMessage(Naxcraft.MSG_COLOR + "Knock it off.");
				}
				else //is the owner
				{
					if(plugin.superCommand.isSuper(event.getPlayer().getName()))
					{
						return;
					}
					
					if(base.getFlag("creative"))
					{
						event.setCancelled(true);
						event.getPlayer().sendMessage(Naxcraft.MSG_COLOR + "You cannot detonate TNT in creative areas.");
					}
				}
			}
		}
	}
		
	private boolean ownsBases(Player player) 
	{
		for(AutoBase base : bases)
		{
			if(isOwner(player, base))
			{
				return true;
			}
		}
		return false;
	}

	public boolean canInteract(Player player, Block block)
	{
		if(plugin.superCommand.isSuper(player.getName())) return true;
		
		AutoBase base = null;
		
		if(getPoint(player) == getPoint(block))
		{
			base = cacheBase.get(player);
		} 
		else
		{
			base = getBase(block);
		}
		
		if(base == null)
		{
			return true;
		}
		
		if(isOwner(player, base))
		{
			return true;
		}
		else
		{
			if(block.getType() == Material.DISPENSER)
			{
				CraftDispenser d = new CraftDispenser(block);
				
				if
				(
				   d.getInventory().contains(Material.GOLD_ORE) || d.getInventory().contains(Material.GOLD_INGOT) ||
				   d.getInventory().contains(Material.DIAMOND_ORE) || d.getInventory().contains(Material.DIAMOND) ||
				   d.getInventory().contains(Material.IRON_ORE) || d.getInventory().contains(Material.IRON_INGOT)
				)
				{	
					return true;
				} 
				else 
				{
					return false;
				}
			}
			
			return false;
		}
	}
	
	public void buyFlag(Player player, String[] args) 
	{	
		String flag = args[2];
		
		if(getFlagInfo(flag) == null)
		{
			player.sendMessage(Naxcraft.MSG_COLOR + "The flag " + Naxcraft.ERROR_COLOR + args[2] + Naxcraft.MSG_COLOR + " does not exist.");
			return;
		}
		
		AutoBase base = getBase(player);
		
		if(base == null)
		{
			player.sendMessage(Naxcraft.MSG_COLOR + "You must be inside of your area before you purchase a flag.");
			return;
		}
		
		if(base.getFlag(flag) && !plugin.superCommand.isSuper(player.getName()))
		{
			player.sendMessage(Naxcraft.MSG_COLOR + "You have already purchased that flag for this area.");
			return;
		}
		
		if(getFlagInfo(flag).adminOnly)
		{
			if(!plugin.superCommand.isSuper(player.getName()))
			{
				player.sendMessage(Naxcraft.MSG_COLOR + "You cannot buy this admin flag without being super.");
				return;
			}
			else
			{
				String s = base.toggleFlag(flag) ? "on" : "off";
				player.sendMessage(Naxcraft.MSG_COLOR + "Flag " + Naxcraft.SUCCESS_COLOR + flag + Naxcraft.MSG_COLOR + " is now " + s + "!");
				return;
			}
		}
		
		if(getFlagInfo(flag).required != null && !plugin.superCommand.isSuper(player.getName()))
		{			
			boolean okay = false;
			
			for(String flug : getFlagInfo(flag).required.split(","))
			{
				if(base.getFlag(flug))
				{
					okay = true;
				}
			}
			
			if(!okay)
			{
				//THAT'S NOT OKAY.
				player.sendMessage(Naxcraft.MSG_COLOR + "That flag requires other flags to be bought already. Check " + Naxcraft.DEFAULT_COLOR + "/flags " + flag + Naxcraft.MSG_COLOR +".");
				return;
			}
		}
		
		if(plugin.superCommand.isSuper(player.getName()) || charge(player, Material.GOLD_INGOT, getFlagInfo(flag).cost))
		{
			String s = base.toggleFlag(flag) ? "on" : "off";
			player.sendMessage(Naxcraft.MSG_COLOR + "Flag " + Naxcraft.SUCCESS_COLOR + flag + Naxcraft.MSG_COLOR + " is now " + s + "!");
		}
		else
		{
			player.sendMessage(Naxcraft.MSG_COLOR + "You don't have the " + Naxcraft.ERROR_COLOR + getFlagInfo(flag).cost + " " + Material.GOLD_INGOT.toString().toLowerCase().replace("_", " ") + Naxcraft.MSG_COLOR + " required for that flag.");
		}
			
	}
	
	public FlagInfo getFlagInfo(String name)
	{
		FlagInfo flag = null;
		
		for(FlagInfo flug : flags)
		{
			if(flug.name.equalsIgnoreCase(name))
			{
				return flug;
			}
		}
		
		return flag;
	}
	
	public boolean charge(Player player, Material material, int cost)
	{		
		int priceleft = cost;
		List<Integer> slotsToDestroy = new ArrayList<Integer>();
		
		boolean enoughMoney = false;
		
		for(int slot : player.getInventory().all(material).keySet())
		{
			priceleft -= player.getInventory().getItem(slot).getAmount();
			if(priceleft > 0) 
			{
				slotsToDestroy.add(slot);
			} 
			else if(priceleft == 0) 
			{
				slotsToDestroy.add(slot);
				enoughMoney = true;
				break;
			} 
			else if(priceleft < 0) 
			{
				slotsToDestroy.add(slot);
				enoughMoney = true;
				break;
			}
		}
		
		if(enoughMoney){
			for(int slot : slotsToDestroy){
				player.getInventory().setItem(slot, null);
			}
			if(priceleft < 0){
				player.getInventory().addItem(new ItemStack(material, priceleft*-1));
			}		
		}
		
		return enoughMoney;
	}
	
	public void showBorders(Player player)
	{
		for(AutoBase base : bases)
		{
			if(base.playerClose(player))
			{
				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new BorderShower(player, base), 2);
			}
		}
		
		player.sendMessage(ChatColor.GOLD + "Gold" + Naxcraft.MSG_COLOR + ": Your founded areas. " + ChatColor.BLUE + "Blue" + Naxcraft.MSG_COLOR + 
						   ": Areas you may build in. " + ChatColor.RED + "Red" + Naxcraft.MSG_COLOR + ": Areas you may not build in. " +
						   ChatColor.GREEN + "Green" + Naxcraft.MSG_COLOR + ": The current chunk you are in. Remove borders with " + Naxcraft.DEFAULT_COLOR + 
						   "/property noborders" + Naxcraft.MSG_COLOR + " or relogging.");
	}
	
	public void hideBorders(Player player)
	{
		for(AutoBase base : bases)
		{
			if(base.playerClose(player))
			{
				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new BorderHider(player, base), 2);
			}
		}
	}
	
	public AutoBase getNearestBase(Player player)
	{
		return getNearestBaseWithin(player, 10000);
	}
	
	public AutoBase getNearestBaseWithinBuffer(Player player)
	{
		return getNearestBaseWithin(player, CHUNK_BUFFER);
	}
	
	public AutoBase getNearestBaseWithin(Player player, int distance)
	{
		AutoBase base = null;
		
		int x = player.getLocation().getBlock().getChunk().getX();
		int z = player.getLocation().getBlock().getChunk().getZ();
		
		int distanceToBeat = distance + 5;
		
		for(AutoBase b : bases)
		{
			for(Point chunk : b.chunks)
			{
				if(Math.abs(chunk.x - x) > distance) continue;
				if(Math.abs(chunk.y - z) > distance) continue;
				
				int d = (int)Math.floor(Math.sqrt(Math.pow(chunk.x - x, 2) + Math.pow(chunk.y - z, 2)));
				
				if(d < distanceToBeat)
				{
					base = b;
					distanceToBeat = d;
				}
			}
		}
		
		return base;
	}
	
	public AutoBase getBase(Player player)
	{
		return getBase(player.getLocation().getBlock());
	}
	
	public AutoBase getBase(Block block)
	{
		return getBase(block.getChunk());
	}
	
	public AutoBase getBase(Chunk chunk)
	{
		for(AutoBase base : bases)
		{
			if(base.world.getName() != chunk.getWorld().getName()) continue;
			
			for(Point p : base.chunks)
			{
				if(p.getX() != chunk.getX()) continue;
				if(p.getY() != chunk.getZ()) continue;
				
				return base;
			}
		}
		
		return null;
	}
	
	public boolean checkSurroundingChunks(Player player) 
	{
		Chunk c1 = player.getWorld().getChunkAt(player.getLocation().getBlock().getChunk().getX()+1, player.getLocation().getBlock().getChunk().getZ());
		Chunk c2 = player.getWorld().getChunkAt(player.getLocation().getBlock().getChunk().getX(), player.getLocation().getBlock().getChunk().getZ()+1);
		Chunk c3 = player.getWorld().getChunkAt(player.getLocation().getBlock().getChunk().getX()-1, player.getLocation().getBlock().getChunk().getZ());
		Chunk c4 = player.getWorld().getChunkAt(player.getLocation().getBlock().getChunk().getX(), player.getLocation().getBlock().getChunk().getZ()-1);
		
		AutoBase base = getBase(c1);
		if(base != null) return true;
		
		base = getBase(c2);
		if(base != null) return true;
		
		base = getBase(c3);
		if(base != null) return true;
		
		base = getBase(c4);
		if(base != null) return true;
		
		return false;
	}
	
	public AutoBase getTouchingBase(Player player) 
	{
		Chunk c1 = player.getWorld().getChunkAt(player.getLocation().getBlock().getChunk().getX()+1, player.getLocation().getBlock().getChunk().getZ());
		Chunk c2 = player.getWorld().getChunkAt(player.getLocation().getBlock().getChunk().getX(), player.getLocation().getBlock().getChunk().getZ()+1);
		Chunk c3 = player.getWorld().getChunkAt(player.getLocation().getBlock().getChunk().getX()-1, player.getLocation().getBlock().getChunk().getZ());
		Chunk c4 = player.getWorld().getChunkAt(player.getLocation().getBlock().getChunk().getX(), player.getLocation().getBlock().getChunk().getZ()-1);
		
		AutoBase base = getBase(c1);
		if(base != null) return base;
		
		base = getBase(c2);
		if(base != null) return base;
		
		base = getBase(c3);
		if(base != null) return base;
		
		base = getBase(c4);
		if(base != null) return base;
		
		return null;
	}

	public boolean newBase(Player player) 
	{		
		int x = player.getLocation().getBlock().getChunk().getX();
		int z = player.getLocation().getBlock().getChunk().getZ();
		
		int distanceToBeat = SAME_OWNER_BUFFER + 5;
		
		for(AutoBase b : bases)
		{
			if(!isOwner(player, b)) continue;
			
			for(Point chunk : b.chunks)
			{
				if(Math.abs(chunk.x - x) > SAME_OWNER_BUFFER) continue;
				if(Math.abs(chunk.y - z) > SAME_OWNER_BUFFER) continue;
				
				int d = (int)Math.floor(Math.sqrt(Math.pow(chunk.x - x, 2) + Math.pow(chunk.y - z, 2)));
				
				if(d < distanceToBeat)
				{
					return false;
				}
			}
		}
		return true;
	}
	
	public boolean isSuperOwner(Player player, AutoBase base)
	{		
		if(base.owner.startsWith("clan:"))
		{
			NaxcraftClan clan = plugin.clanCommand.getPlayerClan(player);
			
			if(clan == null) return false;
			
			return clan.name.equalsIgnoreCase(base.owner.replace("clan:", ""));
		}
		else
		{
			return player.getName().equalsIgnoreCase(base.owner);
		}
	}
	
	public boolean isOwner(Player player, AutoBase base)
	{
		if(base.getFlag("public")) return true;
		if(plugin.superCommand.isSuper(player.getName())) return true;
		
		if(base.owner.equalsIgnoreCase(player.getName()))
		{
			return true;
		}
		else if (base.owner.contains("clan:"))
		{
			NaxcraftClan clan = plugin.clanCommand.getPlayerClan(player);
			
			if(clan != null)
			{
				if(clan.name.equalsIgnoreCase(base.owner.replace("clan:", "")))
				{
					return true;
				}
			}
		}
		
		for(String otherOwner : base.otherOwners)
		{
			if(otherOwner.equalsIgnoreCase(player.getName()))
			{
				return true;
			}
			else if (otherOwner.contains("clan:"))
			{
				NaxcraftClan clan = plugin.clanCommand.getPlayerClan(player);
				
				if(clan == null) continue;
				
				if(clan.name.equalsIgnoreCase(otherOwner.replace("clan:", "")))
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	public boolean addBuilder(String builder, AutoBase base)
	{
		builder = builder.toLowerCase();
		
		if(base.otherOwners.contains(builder))
		{
			return false;
		}
		else
		{
			base.otherOwners.add(builder);
			base.save();
			return true;
		}
	}
	
	public boolean removeBuilder(String builder, AutoBase base)
	{
		builder = builder.toLowerCase();
		
		if(!base.otherOwners.contains(builder))
		{
			return false;
		}
		else
		{
			base.otherOwners.remove(builder);
			base.save();
			return true;
		}
	}
	
	public void count(Player player, Block block)
	{
		if(ownsBases(player))
			return;
		
		if(player.getWorld().getName().startsWith("old_naxville"))
			return;
		
		if(!counters.containsKey(player))
		{
			counters.put(player, new AutoCounter(player));
		}
		
		AutoCounter counter = counters.get(player);
		
		counter.count(block);
	}
	
	public AutoBase createNewBase(Player player) 
	{
		AutoBase base = new AutoBase(this, getNewId(player.getName()), player.getName(), player.getWorld());
		
		this.bases.add(base);
		return base;
	}
	
	public String getNewId(String owner)
	{
		for(int i = 0; i < 1000; i++)
		{
			if(!ids.contains(owner + "_" + i))
			{
				ids.add(owner + "_" + i);
				return owner + "_" + i;
			}
		}
		
		return "IMPOSSIBLE_ID";
	}
	
	public void loadFile()
	{
		File file = new File(plugin.filePath + FILE_NAME + ".yml");
		try
		{
			if(!file.exists())
			{
				file.createNewFile();
				initializeFile(file);
			}
		}
		catch(IOException e)
		{
			System.out.println("Error loading AutoArea file.");
			e.printStackTrace();
		}
		
		config = new NaxcraftConfiguration(file);
		config.load();
	}
	
	public void initializeFile(File file)
	{
		try
		{
			BufferedWriter output = new BufferedWriter(new FileWriter(file));
			output.write("autobases: {}");
			output.close();
		}
		catch(IOException e)
		{
			System.out.println("Error initializing AutoArea file.");
			e.printStackTrace();
		}
	}
	
	public void loadAutoAreas()
	{
		loadFile();
		
		try
		{
			List<String> keys = config.getKeys("autobases");
			
			for(String key : keys)
			{
				String prefix = "autobases." + key;
				
				String owner = config.getString(prefix + ".owner");
				World world = plugin.getServer().getWorld(config.getString(prefix + ".world"));
				
				List<String> flagKeys = config.getKeys(prefix + ".flags");
				Map<String, Boolean> flags = new HashMap<String, Boolean>();
				
				for(String flagKey : flagKeys)
				{
					flags.put(flagKey, Boolean.parseBoolean(config.getString(prefix + ".flags." + flagKey)));
				}
				
				List<String> chunkList = config.getKeys(prefix + ".chunks");
				List<Point> chunks = new ArrayList<Point>();
				
				if(chunkList == null)
				{
					config.removeProperty(prefix);
					config.save();
					continue;
				}
				
				for(String chunk : chunkList)
				{
					chunks.add(new Point(Integer.parseInt(config.getString(prefix + ".chunks." + chunk + ".x")), Integer.parseInt(config.getString(prefix + ".chunks." + chunk + ".z"))));
				}
				
				List<String> otherOwners = config.getStringList(prefix + ".otherOwners", null);
				
				AutoBase base = new AutoBase(this, key, owner, otherOwners, world, chunks, flags);
				bases.add(base);
				ids.add(key);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void saveBase(AutoBase base)
	{
		String prefix = "autobases." + base.id;
		
		config.setProperty(prefix + ".owner", base.owner);
		config.setProperty(prefix + ".world", base.world.getName());
		
		for(String flag : base.flags.keySet())
		{
			config.setProperty(prefix + ".flags." + flag, base.flags.get(flag).toString());
		}
		
		config.setProperty(prefix + ".otherOwners", base.otherOwners);
		
		int i = 0;
		for(Point chunk : base.chunks)
		{
			config.setProperty(prefix + ".chunks." + i + ".x", chunk.x);
			config.setProperty(prefix + ".chunks." + i + ".z", chunk.y);
			i++;
		}
		
		config.save();
	}
	
	public void removeBase(AutoBase base)
	{
		refreshOwners(base);
		config.removeProperty("autobases." + base.id);
		config.save();
	}

	public void addRemoveBuilder(Player player, String[] args) 
	{
		AutoBase base = getBase(player);
		
		if(base == null || !isOwner(player, base))
		{
			player.sendMessage(Naxcraft.MSG_COLOR + "You are not within an area you own!");
			return;
		}
		
		if(!isSuperOwner(player, base))
		{
			player.sendMessage(Naxcraft.MSG_COLOR + "You are not the founder of this area!");
			return;
		}
		
		if(charge(player, Material.GOLD_INGOT, 16))
		{
			if(args[0].equalsIgnoreCase("add"))
			{
				if(addBuilder(args[2].replace("<", "").replace(">", ""), base))
				{
					player.sendMessage(Naxcraft.MSG_COLOR + args[2] + Naxcraft.MSG_COLOR + " has been added to the area!");
					
					refreshOwners(base);
				}
				else
				{
					player.sendMessage(Naxcraft.MSG_COLOR + args[2] + " was already added to this area!");
				}
			}
			else if (args[0].equalsIgnoreCase("remove"))
			{
				if(removeBuilder(args[2], base))
				{
					player.sendMessage(Naxcraft.MSG_COLOR + args[2] + Naxcraft.MSG_COLOR + " has been removed from the area!");
					refreshOwners(base);
				}
				else
				{
					player.sendMessage(Naxcraft.MSG_COLOR + args[2] + " isn't a part of this area to begin with!");
				}
			}
		}
		else
		{
			player.sendMessage(Naxcraft.MSG_COLOR + "It costs 16 gold ingots in order to add/remove a builder.");
		}
	}
	
	public void refreshOwners(AutoBase base)
	{
		for(String s : base.getOwners())
		{
			for(Player p : plugin.getServer().getOnlinePlayers())
			{
				if(p.getName().equalsIgnoreCase(s))
				{
					refreshCache(p);
				}
			}
		}
	}
	
	public enum CacheResponse
	{
		OWNER,
		GOOD,
		BAD,
		NONE
	}
}










