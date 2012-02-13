package com.naxville.naxcraft.autoareas;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.block.CraftDispenser;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Snowman;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;

import com.naxville.naxcraft.NaxColor;
import com.naxville.naxcraft.NaxFile;
import com.naxville.naxcraft.Naxcraft;
import com.naxville.naxcraft.admin.SuperManager;
import com.naxville.naxcraft.player.NaxPlayer;
import com.naxville.naxcraft.player.PlayerManager.PlayerRank;

public class AutoAreaManager
{
	public Naxcraft plugin;
	public NaxFile config;
	public SnowmenManager snowmenManager = new SnowmenManager(this);
	
	public List<AutoBase> bases = new ArrayList<AutoBase>();
	public Map<Player, AutoCounter> counters = new HashMap<Player, AutoCounter>();
	public Map<Player, AutoBase> within = new HashMap<Player, AutoBase>();
	
	public Map<Player, World> cacheWorld = new HashMap<Player, World>();
	public Map<Player, Point> cachePosition = new HashMap<Player, Point>();
	public Map<Player, AutoBase> cacheBase = new HashMap<Player, AutoBase>();
	
	public List<String> ids = new ArrayList<String>();
	public List<Integer> keepDrops = new ArrayList<Integer>();
	public List<Player> enderTeleports = new ArrayList<Player>();
	
	public static final int CHUNK_BUFFER = 8;
	public static final int BASE_BUFFER = 4;
	public static final int SAME_OWNER_BUFFER = 10;
	
	public AutoAreaManager(Naxcraft plugin)
	{
		this.plugin = plugin;
	}
	
	public enum Flag
	{
		PVP("PvP", "Protects owners from player damage.", 32, null, PlayerRank.MEMBER.getId()),
		SAFE("Safe", "Protects owners from ALL damage.", 64, new Flag[] { Flag.PVP }, PlayerRank.MEMBER.getId()),
		WEATHER("Weather", "Stops ice/snow from forming.", 32, null, PlayerRank.MEMBER.getId()),
		FARMER("Farmer", "Protects animals from non-builder attacks.", 32, null, PlayerRank.MEMBER.getId()),
		FOOD("Food", "Randomly lowers chance of losing food points.", 16, null, PlayerRank.MEMBER.getId()),

		NO_PVP("NoPvP", "Protects everyone from player damage.", 32, new Flag[] { Flag.SAFE, Flag.PVP }, PlayerRank.VETERAN.getId()),
		TRAPDOORS("Trapdoors", "Locks trapdoors from non-builders.", 16, null, PlayerRank.VETERAN.getId()),
		CHESTS("Chests", "Locks chests and furnaces from non-builders.", 64, null, PlayerRank.VETERAN.getId()),
		MERCHANT("Merchant", "Allows creation of shops (see site).", 64, new Flag[] { Flag.CHESTS }, PlayerRank.VETERAN.getId()),
		HURT("Hurt", "Hurts any player attempting to alter an area.", 32, null, PlayerRank.VETERAN.getId()),
		RAILWAY("Railway", "Allows rail station functionality.", 32, null, PlayerRank.VETERAN.getId()),
		SNOW_GUARDS("SnowGuards", "Snowmen do damage.", 32, null, PlayerRank.VETERAN.getId()),
		SNOW_BOUNCERS("SnowBouncers", "Snowmen attack non-builders.", 32, null, PlayerRank.VETERAN.getId()),
		PEARLS("Pearls", "Ender pearls cause no damage on tp.", 16, null, PlayerRank.VETERAN.getId()),
		FORGES("Forges", "Furnaces don't use up lava buckets.", 64, null, PlayerRank.VETERAN.getId()),

		SANCTUARY("Sanctuary", "Protects everyone from ALL damage.", 64, new Flag[] { Flag.NO_PVP }, PlayerRank.MASTER.getId()),
		CREATIVE("Creative", "Allows creative mode + flight", 256, null, PlayerRank.MASTER.getId()),
		PUBLIC("Public", "Allows anyone to build in the area.", 32, new Flag[] { Flag.CREATIVE }, PlayerRank.MASTER.getId()),

		LOCK("Lock", "Area can't be edited.", 0, null, PlayerRank.MODERATOR.getId()),
		NO_EXPAND("NoExpand", "Area cannot expand.", 0, null, PlayerRank.MODERATOR.getId()),
		NO_ADD("NoAdd", "Area cannot get new builders.", 0, null, PlayerRank.MODERATOR.getId()),
		GRINDER("Grinder", "May not be functional.", 0, null, PlayerRank.MODERATOR.getId()),
		JAIL("Jail", "Not functional.", 0, null, PlayerRank.MODERATOR.getId());
		
		public String displayName;
		public String description;
		public int cost;
		public Flag[] required;
		public int minimumRankId;
		
		private Flag(String displayName, String description, int cost, Flag[] required, int minimumRankId)
		{
			this.displayName = displayName;
			this.description = description;
			this.cost = cost;
			this.required = required;
			this.minimumRankId = minimumRankId;
		}
		
		public String toString()
		{
			return "" + displayName;
		}
		
		public static Flag parseFlag(String s)
		{
			for (Flag f : values())
			{
				if (s.equalsIgnoreCase(f.displayName)) return f;
			}
			
			return null;
		}
	}
	
	public void shutup(Player player)
	{
		if (counters.containsKey(player))
		{
			counters.get(player).shutup = true;
		}
	}
	
	public boolean flagsCommand(Player player, String args[])
	{
		if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("help")))
		{
			player.sendMessage(Naxcraft.MSG_COLOR + "----");
			player.sendMessage(Naxcraft.COMMAND_COLOR + "Flags: (use /flags <name> to see more info)");
			NaxPlayer p = plugin.playerManager.getPlayer(player);
			
			if (p.rank != PlayerRank.NOOB)
			{
				for (Flag flag : Flag.values())
				{
					if (p.rank.getId() < flag.minimumRankId - 1) continue;
					if (!p.rank.isAdmin() && PlayerRank.getRank(flag.minimumRankId) == PlayerRank.MODERATOR) continue;
					
					NaxColor color = NaxColor.LOCAL;
					
					if (p.rank.getId() < flag.minimumRankId)
					{
						color = NaxColor.ERR;
					}
					
					String f = PlayerRank.getRank(flag.minimumRankId).getPrefix() + color + flag.displayName + NaxColor.DEMI_ADMIN + " " + flag.cost + "g " + NaxColor.MSG + ": ";
					
					f += Naxcraft.MSG_COLOR + flag.description;
					
					if (flag.required != null)
					{
						f += Naxcraft.ERROR_COLOR + " Reqs";
					}
					
					player.sendMessage(f);
				}
				if (p.rank == PlayerRank.MEMBER)
				{
					player.sendMessage(NaxColor.DEMI_ADMIN + "Use the /promote command with 32 gold ingots to unlock the red flags!");
				}
				else if (p.rank == PlayerRank.VETERAN)
				{
					player.sendMessage(NaxColor.DEMI_ADMIN + "Build some awesome stuff or donate to the server to unlock the red flags!");
				}
			}
			else
			{
				player.sendMessage(NaxColor.MSG + "You can't buy any flags until you rank up! Use /join with 16 iron on you.");
			}
		}
		else if (args.length == 1)
		{
			Flag flag = null;
			
			for (Flag f : Flag.values())
			{
				if (args[0].equalsIgnoreCase(f.toString()))
				{
					flag = f;
				}
			}
			
			if (flag == null)
			{
				player.sendMessage(Naxcraft.MSG_COLOR + "The flag " + Naxcraft.ERROR_COLOR + args[0] + Naxcraft.MSG_COLOR + " does not exist.");
				return true;
			}
			
			NaxColor color = NaxColor.WHITE;
			
			String f = color + flag.displayName + Naxcraft.MSG_COLOR + ": " + Naxcraft.DEFAULT_COLOR + flag.cost + "g" + Naxcraft.MSG_COLOR + " - " + flag.description;
			String req = "";
			
			if (flag.required != null)
			{
				req += Naxcraft.ERROR_COLOR + " Prerequisites: ";
				
				int i = 0;
				for (Flag r : flag.required)
				{
					if (i > 0) req += Naxcraft.MSG_COLOR + " or ";
					req += Naxcraft.DEFAULT_COLOR + "" + r;
					i++;
				}
			}
			
			player.sendMessage(Naxcraft.MSG_COLOR + "----");
			player.sendMessage(f);
			if (req != "") player.sendMessage(req);
		}
		
		return true;
	}
	
	public Point getPoint(Player player)
	{
		return new Point(player.getLocation().getBlock().getChunk().getX(), player.getLocation().getBlock().getChunk().getZ());
	}
	
	public void handleEntityDamage(EntityDamageEvent event)
	{
		if (event.getEntity() instanceof Player)
		{
			Player player = (Player) event.getEntity();
			
			AutoBase base = getBase(player);
			
			if (base != null)
			{
				if (event.getCause() == DamageCause.STARVATION)
				{
					if (base.hasFlag(Flag.FOOD))
					{
						if (new Random().nextInt(2) == 0)
						{
							event.setCancelled(true);
							player.setExhaustion(0);
						}
					}
				}
				else
				{
					if (base.hasFlag(Flag.SANCTUARY) && !causedByEnderPearl(event))
					{
						event.setCancelled(!plugin.bloodMoonManager.isBloodMoon(event.getEntity().getWorld()));
					}
					else if (base.hasFlag(Flag.SAFE) && !causedByEnderPearl(event))
					{
						if (isOwner(player, base))
						{
							if (plugin.bloodMoonManager.isBloodMoon(event.getEntity().getWorld()) && event instanceof EntityDamageByEntityEvent)
							{
								EntityDamageByEntityEvent ev = (EntityDamageByEntityEvent) event;
								
								event.setCancelled(ev.getDamager() instanceof Monster);
							}
							else
							{
								event.setCancelled(true);
							}
						}
					}
					else if (!causedByEnderPearl(event) && (base.hasFlag(Flag.PVP) || base.hasFlag(Flag.NO_PVP)))
					{
						if (event instanceof EntityDamageByEntityEvent)
						{
							EntityDamageByEntityEvent ev = (EntityDamageByEntityEvent) event;
							if (ev.getDamager() instanceof Player)
							{
								if (base.hasFlag(Flag.NO_PVP))
								{
									event.setCancelled(true);
								}
								else if (base.hasFlag(Flag.PVP) && isOwner(player, base))
								{
									event.setCancelled(true);
								}
							}
						}
					}
					else if (base.hasFlag(Flag.PEARLS) && causedByEnderPearl(event))
					{
						if (enderTeleports.remove(player))
						{
							System.out.println(player.getName() + " is getting hurt from ender pearling!");
							event.setCancelled(true);
						}
					}
				}
			}
			
			if (event.isCancelled())
			{
				player.setFireTicks(0);
			}
		}
		else if (event.getEntity() instanceof Animals)
		{
			AutoBase base = getBase(event.getEntity().getLocation());
			
			if (base != null && base.hasFlag(Flag.FARMER))
			{
				if (event instanceof EntityDamageByEntityEvent)
				{
					EntityDamageByEntityEvent ev = (EntityDamageByEntityEvent) event;
					
					Player player = null;
					
					if (ev.getDamager() instanceof Player)
					{
						player = (Player) ev.getDamager();
					}
					else if (ev.getDamager() instanceof Arrow)
					{
						Arrow arrow = (Arrow) ev.getDamager();
						
						if (arrow.getShooter() instanceof Player)
						{
							player = (Player) arrow.getShooter();
						}
					}
					
					if (player != null && !isOwner(player, base))
					{
						player.sendMessage(NaxColor.MSG + "You may not harm livestock in this area.");
						event.setCancelled(true);
					}
				}
			}
		}
		
		if (event.getEntity() instanceof Creature && !event.isCancelled())
		{
			Creature creature = (Creature) event.getEntity();
			
			if (creature.getHealth() - event.getDamage() <= 0)
			{
				AutoBase base = getBase(creature.getLocation().getBlock());
				
				if (event instanceof EntityDamageByEntityEvent)
				{
					if (base != null && base.hasFlag(Flag.CREATIVE)) return;
					
					keepDrops.add(creature.getEntityId());
				}
				else
				{
					if (base != null && base.hasFlag(Flag.GRINDER))
					{
						keepDrops.add(creature.getEntityId());
					}
				}
			}
		}
		
		if (event instanceof EntityDamageByEntityEvent && !event.isCancelled())
		{
			EntityDamageByEntityEvent ev = (EntityDamageByEntityEvent) event;
			
			if (ev.getDamager() instanceof Snowball && ((Snowball) ev.getDamager()).getShooter() instanceof Snowman)
			{
				Snowman s = (Snowman) ((Snowball) ev.getDamager()).getShooter();
				
				AutoBase base = getBase(s.getLocation());
				
				if (base != null && base.hasFlag(Flag.SNOW_GUARDS) && ev.getDamage() == 0)
				{
					ev.setDamage(1);
				}
			}
		}
	}
	
	private boolean causedByEnderPearl(EntityDamageEvent event)
	{
		return (event.getCause() == DamageCause.FALL && event instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) event).getDamager() instanceof EnderPearl);
	}
	
	public void refreshCache(Player player)
	{
		if (cachePosition.containsKey(player)) cachePosition.remove(player);
		
		if (cacheWorld.containsKey(player)) cacheWorld.remove(player);
		
		if (cacheBase.containsKey(player)) cacheBase.remove(player);
	}
	
	public void updateCaches(Point point)
	{
		for (Player player : cachePosition.keySet())
		{
			if (cachePosition.get(player) == point)
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
		
		if (player.getWorld().getName().startsWith("old_naxville"))
		{
			refreshCache(player);
			return;
		}
		
		if (!cachePosition.containsKey(player) || cachePosition.get(player) == null)
		{
			cachePosition.put(player, getPoint(player));
			cacheBase.put(player, getBase(player));
		}
		
		if (cacheWorld.containsKey(player))
		{
			if (cacheWorld.get(player).getName() != player.getWorld().getName())
			{
				cachePosition.put(player, null);
				cacheBase.put(player, null);
			}
		}
		
		if (cachePosition.get(player) == getPoint(player))
		{
			return;
		}
		else
		{
			cachePosition.put(player, getPoint(player));
			cacheBase.put(player, getBase(player));
		}
		
		AutoBase base = cacheBase.get(player);
		
		if (!within.containsKey(player)) within.put(player, null);
		
		if (base == null)
		{
			if (within.get(player) != null)
			{
				player.sendMessage(Naxcraft.MSG_COLOR + "You are now in unprotected land.");
				
				if (within.get(player).hasFlag(Flag.SNOW_BOUNCERS))
				{
					snowmenManager.exitSnowmenBase(player, base);
				}
				
				within.put(player, null);
				
				if (player.getGameMode() == GameMode.CREATIVE && !SuperManager.isSuper(player))
				{
					setSurvivalMode(player);
				}
			}
			
		}
		else
		{
			if (within.get(player) != base)
			{
				if (base.hasFlag(Flag.SNOW_BOUNCERS))
				{
					snowmenManager.enterSnowmenBase(player, base);
				}
				
				if (isOwner(player, base))
				{
					player.sendMessage(Naxcraft.MSG_COLOR + "You own this area; /property for help.");
					within.put(player, base);
				}
				else
				{
					if (base.otherOwners.isEmpty())
					{
						player.sendMessage(Naxcraft.MSG_COLOR + "This area is owned by " + base.getFounderName() + Naxcraft.MSG_COLOR + "; /property for help.");
					}
					else
					{
						player.sendMessage(Naxcraft.MSG_COLOR + "This area is owned by " + base.getFounderName() + Naxcraft.MSG_COLOR + ". Builders include "
								+ base.getBuilderNames() + Naxcraft.MSG_COLOR + ". /property for help.");
					}
					within.put(player, base);
					
					if (player.getGameMode() == GameMode.CREATIVE && !SuperManager.isSuper(player))
					{
						setSurvivalMode(player);
					}
				}
			}
		}
	}
	
	private void setSurvivalMode(Player player)
	{
		if (!SuperManager.isSuper(player))
		{
			player.getInventory().clear();
			player.getInventory().setArmorContents(new ItemStack[] { null, null, null, null });
		}
		player.setGameMode(GameMode.SURVIVAL);
		player.sendMessage(NaxColor.MSG + "You are now in survival mode.");
	}
	
	public void handleBlockDamage(BlockDamageEvent event)
	{
		AutoBase base = null;
		if (getPoint(event.getPlayer()) == getPoint(event.getBlock()))
		{
			base = cacheBase.get(event.getPlayer());
		}
		else
		{
			base = getBase(event.getBlock());
		}
		
		if (base == null)
		{
			if (event.getBlock().getType() == Material.TNT && event.getItemInHand().getType() == Material.FLINT_AND_STEEL)
			{
				if (!SuperManager.isSuper(event.getPlayer()))
				{
					event.setCancelled(true);
					event.getBlock().setType(Material.AIR);
					event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.TNT, 1));
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
		if (event.isCancelled()) return;
		
		AutoBase base = null;
		if (getPoint(event.getPlayer()) == getPoint(event.getBlock()))
		{
			base = cacheBase.get(event.getPlayer());
		}
		else
		{
			base = getBase(event.getBlock());
		}
		
		if (base != null)
		{
			if (!isOwner(event.getPlayer(), base))
			{
				if (base.hasFlag(Flag.HURT))
				{
					event.getPlayer().damage(4);
					event.getPlayer().sendMessage(NaxColor.ERR + "You will die if you continue to place/break blocks here.");
				}
				else
				{
					event.getPlayer().sendMessage(Naxcraft.MSG_COLOR + "This area is protected, you may not destroy here.");
				}
				
				plugin.playerManager.handleKick(event.getPlayer(), "protected areas");
				event.setCancelled(true);
			}
			else
			{
				Player player = event.getPlayer();
				if (base.hasFlag(Flag.CREATIVE) && player.getGameMode() == GameMode.SURVIVAL && !SuperManager.isSuper(player))
				{
					player.sendMessage(NaxColor.MSG + "You cannot break blocks while in a creative-flagged area when you're in survival mode!");
					event.setCancelled(true);
				}
			}
		}
		else
		{
			Player player = event.getPlayer();
			
			if (player.getGameMode() == GameMode.CREATIVE && !SuperManager.isSuper(player))
			{
				player.sendMessage(NaxColor.MSG + "You cannot break blocks outside of your creative area!");
				event.setCancelled(true);
			}
		}
	}
	
	public void handleBlockPlace(BlockPlaceEvent event)
	{
		if (event.isCancelled()) return;
		
		event.setCancelled(placeEventCancelled(event.getPlayer(), event.getBlock()));
	}
	
	public boolean placeEventCancelled(Player player, Block block)
	{
		AutoBase base = null;
		
		if (getPoint(player) == getPoint(block))
		{
			base = cacheBase.get(player);
		}
		else
		{
			base = getBase(block);
		}
		
		if (base != null)
		{
			if (base.hasFlag(Flag.LOCK))
			{
				if (!SuperManager.isSuper(player))
				{
					player.sendMessage(NaxColor.ERR + "You may not edit this area, it is locked by an admin.");
					return true;
				}
			}
			
			if (!isOwner(player, base) && !SuperManager.isSuper(player))
			{
				if (base.hasFlag(Flag.HURT))
				{
					player.damage(4);
					player.sendMessage(NaxColor.ERR + "You will die if you continue to place/break blocks here.");
				}
				else
				{
					player.sendMessage(Naxcraft.MSG_COLOR + "You may not build here, this area is protected.");
				}
				plugin.playerManager.handleKick(player, "protected areas");
				return true;
			}
		}
		else
		{
			if (player.getGameMode() == GameMode.CREATIVE && !SuperManager.isSuper(player))
			{
				player.sendMessage(NaxColor.MSG + "You cannot build outside of your creative area!");
				return true;
			}
			
			if (block.getType() == Material.TNT || block.getType() == Material.PISTON_BASE || block.getType() == Material.PISTON_STICKY_BASE)
			{
				if (!SuperManager.isSuper(player))
				{
					player.sendMessage(Naxcraft.MSG_COLOR + "You may not place that in an area you do not own.");
					return true;
				}
			}
			else
			{
				count(player, block);
			}
		}
		
		return false;
	}
	
	/**
	 * Only called on Ender Pearl teleport.
	 * 
	 * @param event
	 */
	
	public void handlePlayerTeleport(PlayerTeleportEvent event)
	{
		AutoBase base = getBase(event.getTo());
		Player player = (Player) event.getPlayer();
		
		if (base == null) return;
		
		if (event.getCause() == TeleportCause.ENDER_PEARL)
		{
			if (!isOwner(event.getPlayer(), base))
			{
				event.setCancelled(true);
			}
			else
			{
				System.out.println(event.getPlayer().getName() + " is enter pearling!");
				enderTeleports.add(event.getPlayer());
			}
		}
		
		if (!event.isCancelled() && player.getGameMode() == GameMode.CREATIVE)
		{
			AutoBase base2 = getBase(event.getFrom());
			
			if (!SuperManager.isSuper(player) && (base2 == null || base2 != base))
			{
				setSurvivalMode(player);
			}
		}
	}
	
	public void handlePlayerInteract(PlayerInteractEvent event)
	{
		if (event.isCancelled()) return;
		
		if (SuperManager.isSuper(event.getPlayer())) return;
		
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
		{
			if (event.getClickedBlock().getType() == Material.DISPENSER)
			{
				if (!canInteract(event.getPlayer(), event.getClickedBlock()))
				{
					event.setCancelled(true);
					event.getPlayer().sendMessage(Naxcraft.MSG_COLOR + "You may not open others' dispensers.");
				}
			}
			else if (event.getClickedBlock().getType() == Material.CHEST)
			{
				AutoBase base = getBase(event.getClickedBlock());
				
				if (base == null) return;
				
				if (!isOwner(event.getPlayer(), base))
				{
					if (base.hasFlag(Flag.CHESTS))
					{
						event.getPlayer().sendMessage(Naxcraft.MSG_COLOR + "You may not open locked chests.");
						plugin.playerManager.handleBadChestClick(event.getPlayer(), "locked");
						event.setCancelled(true);
					}
					else
					{
						plugin.playerManager.handleBadChestClick(event.getPlayer(), "others'");
					}
				}
				
			}
			else if (event.getClickedBlock().getType() == Material.FURNACE || event.getClickedBlock().getType() == Material.BURNING_FURNACE)
			{
				AutoBase base = getBase(event.getClickedBlock());
				
				if (base == null) return;
				
				if (!isOwner(event.getPlayer(), base))
				{
					if (base.hasFlag(Flag.CHESTS))
					{
						event.getPlayer().sendMessage(Naxcraft.MSG_COLOR + "You may not open locked furnaces.");
						event.setCancelled(true);
					}
					plugin.playerManager.handleBadFurnaceClick(event.getPlayer());
				}
				
			}
			else if (event.getClickedBlock().getType() == Material.BED_BLOCK)
			{
				if (!canInteract(event.getPlayer(), event.getClickedBlock()))
				{
					plugin.playerManager.handleUnownedBedClick(event.getPlayer());
				}
			}
			else if (event.getItem() != null &&
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
							event.getItem().getType() == Material.STORAGE_MINECART ||
					event.getItem().getType() == Material.INK_SACK
					))
			{
				if (!canInteract(event.getPlayer(), event.getClickedBlock()))
				{
					event.setCancelled(true);
					event.getPlayer().sendMessage(Naxcraft.MSG_COLOR + "You may not place that in this protected area.");
				}
			}
			else if (event.getClickedBlock().getType() == Material.TNT)
			{
				AutoBase base = getBase(event.getClickedBlock());
				
				if (base == null)
				{
					if (SuperManager.isSuper(event.getPlayer())) { return; }
					
					event.setCancelled(true);
					event.getPlayer().sendMessage(Naxcraft.MSG_COLOR + "You cannot activate TNT in the wilderness.");
				}
				else if (!isOwner(event.getPlayer(), base))
				{
					event.setCancelled(true);
					event.getPlayer().sendMessage(Naxcraft.MSG_COLOR + "Knock it off.");
				}
			}
			
			if (!event.isCancelled() && event.hasItem())
			{
				AutoBase base = getBase(event.getClickedBlock());
				ItemStack item = event.getItem();
				Player player = event.getPlayer();
				
				if (item.getType() == Material.INK_SACK && item.getData().getData() == 15)
				{
					if (base == null && !SuperManager.isSuper(event.getPlayer()))
					{
						player.sendMessage(NaxColor.MSG + "You cannot use bonemeal outside areas you own.");
						event.setCancelled(true);
					}
				}
				else if (item.getType() == Material.MONSTER_EGG)
				{
					if (!SuperManager.isSuper(player))
					{
						boolean cancelled = base == null || !base.isOwner(player) || item.getDurability() < 90 || item.getDurability() > 97; // 90 is pig, 97 is snow golem
						event.setCancelled(cancelled);
						
						if (cancelled)
						{
							player.sendMessage(NaxColor.MSG + "You cannot spawn monsters in areas you don't own. Also, you may only spawn animals, not enemies.");
						}
					}
				}
			}
		}
		
		if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK)
		{
			if (event.getClickedBlock().getType() == Material.TRAP_DOOR)
			{
				Player player = (Player) event.getPlayer();
				AutoBase base = getBase(event.getClickedBlock());
				
				if (base == null) { return; }
				
				if (!isOwner(player, base))
				{
					if (base.hasFlag(Flag.TRAPDOORS))
					{
						event.setCancelled(true);
						player.sendMessage(Naxcraft.MSG_COLOR + "You may not interact with players' locked trapdoors.");
						plugin.playerManager.handleKick(player, "trapdoors");
					}
				}
			}
		}
	}
	
	public boolean ownsBases(Player player)
	{
		for (AutoBase base : bases)
		{
			if (isOwner(player, base)) { return true; }
		}
		return false;
	}
	
	/**
	 * Gets number of owned bases by a player.
	 * 
	 * @param player
	 * @return An array of [Founded Bases, Builder Rank Bases]
	 */
	public int[] getNumberOwnedBases(String player)
	{
		int superOwned = 0;
		int owned = 0;
		
		for (AutoBase base : bases)
		{
			if (isSuperOwner(player, base)) superOwned++;
			else if (isOwner(player, base)) owned++;
		}
		return new int[] { superOwned, owned };
	}
	
	public boolean canInteract(Player player, Block block)
	{
		if (SuperManager.isSuper(player)) return true;
		
		AutoBase base = null;
		
		if (getPoint(player) == getPoint(block))
		{
			base = cacheBase.get(player);
		}
		else
		{
			base = getBase(block);
		}
		
		if (base == null) { return true; }
		
		if (isOwner(player, base))
		{
			return true;
		}
		else
		{
			if (block.getType() == Material.DISPENSER)
			{
				CraftDispenser d = new CraftDispenser(block);
				
				if (d.getInventory().contains(Material.GOLD_ORE) || d.getInventory().contains(Material.GOLD_INGOT) ||
						d.getInventory().contains(Material.DIAMOND_ORE) || d.getInventory().contains(Material.DIAMOND) ||
						d.getInventory().contains(Material.IRON_ORE) || d.getInventory().contains(Material.IRON_INGOT))
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
		Flag flag = Flag.parseFlag(args[2].toLowerCase());
		
		if (flag == null)
		{
			player.sendMessage(Naxcraft.MSG_COLOR + "The flag " + Naxcraft.ERROR_COLOR + args[2] + Naxcraft.MSG_COLOR + " does not exist.");
			return;
		}
		
		AutoBase base = getBase(player);
		
		if (base == null)
		{
			player.sendMessage(Naxcraft.MSG_COLOR + "You must be inside of your area before you purchase a flag.");
			return;
		}
		
		if (base.hasFlag(flag) && !SuperManager.isSuper(player))
		{
			player.sendMessage(Naxcraft.MSG_COLOR + "You have already purchased that flag for this area.");
			return;
		}
		
		NaxPlayer p = plugin.playerManager.getPlayer(player);
		
		if (p.rank.getId() < flag.minimumRankId)
		{
			if (!SuperManager.isSuper(player))
			{
				player.sendMessage(Naxcraft.MSG_COLOR + "You are not high enough ranked for that flag.");
				
				if (p.rank == PlayerRank.NOOB)
				{
					player.sendMessage(NaxColor.MSG + "Try using the /join command when you have 16 iron ingots to unlock more flags.");
				}
				else if (p.rank == PlayerRank.MEMBER)
				{
					player.sendMessage(NaxColor.MSG + "Try using the /promote command when you have 32 gold ingots to unlock more flags.");
				}
				else
				{
					player.sendMessage(NaxColor.MSG + "That flag is for admins only.");
				}
				
				return;
			}
		}
		else if (SuperManager.isSuper(player))
		{
			String s = base.toggleFlag(flag) ? "on" : "off";
			player.sendMessage(Naxcraft.MSG_COLOR + "Flag " + Naxcraft.SUCCESS_COLOR + flag + Naxcraft.MSG_COLOR + " is now " + s + "!");
			return;
		}
		
		if (flag.required != null && !SuperManager.isSuper(player))
		{
			boolean okay = false;
			
			for (Flag r : flag.required)
			{
				if (base.hasFlag(r))
				{
					okay = true;
				}
			}
			
			if (!okay)
			{
				// THAT'S NOT OKAY.
				player.sendMessage(Naxcraft.MSG_COLOR + "That flag requires other flags to be bought already. Check " + Naxcraft.DEFAULT_COLOR + "/flags " + flag + Naxcraft.MSG_COLOR + ".");
				return;
			}
		}
		
		if (SuperManager.isSuper(player) || charge(player, Material.GOLD_INGOT, flag.cost))
		{
			String s = base.toggleFlag(flag) ? "on" : "off";
			player.sendMessage(Naxcraft.MSG_COLOR + "Flag " + Naxcraft.SUCCESS_COLOR + flag + Naxcraft.MSG_COLOR + " is now " + s + "!");
		}
		else
		{
			player.sendMessage(Naxcraft.MSG_COLOR + "You don't have the " + Naxcraft.ERROR_COLOR + flag.cost + " " + Material.GOLD_INGOT.toString().toLowerCase().replace("_", " ") + Naxcraft.MSG_COLOR + " required for that flag.");
		}
		
	}
	
	public boolean charge(Player player, Material material, int cost)
	{
		int priceleft = cost;
		List<Integer> slotsToDestroy = new ArrayList<Integer>();
		
		boolean enoughMoney = false;
		
		for (int slot : player.getInventory().all(material).keySet())
		{
			priceleft -= player.getInventory().getItem(slot).getAmount();
			if (priceleft > 0)
			{
				slotsToDestroy.add(slot);
			}
			else if (priceleft == 0)
			{
				slotsToDestroy.add(slot);
				enoughMoney = true;
				break;
			}
			else if (priceleft < 0)
			{
				slotsToDestroy.add(slot);
				enoughMoney = true;
				break;
			}
		}
		
		if (enoughMoney)
		{
			for (int slot : slotsToDestroy)
			{
				player.getInventory().setItem(slot, null);
			}
			if (priceleft < 0)
			{
				player.getInventory().addItem(new ItemStack(material, priceleft * -1));
			}
		}
		
		return enoughMoney;
	}
	
	public void showBorders(Player player)
	{
		for (AutoBase base : bases)
		{
			if (base.playerClose(player))
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
		for (AutoBase base : bases)
		{
			if (base.playerClose(player))
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
		return getNearestBaseWithin(player, distance, true);
	}
	
	public AutoBase getNearestBaseWithin(Player player, int distance, boolean includeOwnedBases)
	{
		AutoBase base = null;
		
		int x = player.getLocation().getBlock().getChunk().getX();
		int z = player.getLocation().getBlock().getChunk().getZ();
		
		int distanceToBeat = distance + 5;
		
		for (AutoBase b : bases)
		{
			if (b.world != player.getWorld())
			{
				continue;
			}
			else if (includeOwnedBases && isOwner(player, b))
			{
				continue;
			}
			
			for (Point chunk : b.chunks)
			{
				if (Math.abs(chunk.x - x) > distance) continue;
				if (Math.abs(chunk.y - z) > distance) continue;
				
				int d = (int) Math.floor(Math.sqrt(Math.pow(chunk.x - x, 2) + Math.pow(chunk.y - z, 2)));
				
				if (d < distanceToBeat)
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
	
	public AutoBase getBase(Location location)
	{
		return getBase(location.getBlock());
	}
	
	public AutoBase getBase(Block block)
	{
		return getBase(block.getChunk());
	}
	
	public AutoBase getBase(Chunk chunk)
	{
		if (chunk == null)
		{
			System.out.println("Within null chunk - what the fuck");
			return null;
		}
		
		for (AutoBase base : bases)
		{
			if (base == null) continue;
			if (base.world == null) continue;
			
			if (base.world.getName() != chunk.getWorld().getName()) continue;
			
			for (Point p : base.chunks)
			{
				if (p.getX() != chunk.getX()) continue;
				if (p.getY() != chunk.getZ()) continue;
				
				return base;
			}
		}
		
		return null;
	}
	
	public boolean checkSurroundingChunks(Player player)
	{
		Chunk c1 = player.getWorld().getChunkAt(player.getLocation().getBlock().getChunk().getX() + 1, player.getLocation().getBlock().getChunk().getZ());
		Chunk c2 = player.getWorld().getChunkAt(player.getLocation().getBlock().getChunk().getX(), player.getLocation().getBlock().getChunk().getZ() + 1);
		Chunk c3 = player.getWorld().getChunkAt(player.getLocation().getBlock().getChunk().getX() - 1, player.getLocation().getBlock().getChunk().getZ());
		Chunk c4 = player.getWorld().getChunkAt(player.getLocation().getBlock().getChunk().getX(), player.getLocation().getBlock().getChunk().getZ() - 1);
		
		AutoBase base = getBase(c1);
		if (base != null) return true;
		
		base = getBase(c2);
		if (base != null) return true;
		
		base = getBase(c3);
		if (base != null) return true;
		
		base = getBase(c4);
		if (base != null) return true;
		
		return false;
	}
	
	public AutoBase getTouchingBase(Player player)
	{
		Chunk c1 = player.getWorld().getChunkAt(player.getLocation().getBlock().getChunk().getX() + 1, player.getLocation().getBlock().getChunk().getZ());
		Chunk c2 = player.getWorld().getChunkAt(player.getLocation().getBlock().getChunk().getX(), player.getLocation().getBlock().getChunk().getZ() + 1);
		Chunk c3 = player.getWorld().getChunkAt(player.getLocation().getBlock().getChunk().getX() - 1, player.getLocation().getBlock().getChunk().getZ());
		Chunk c4 = player.getWorld().getChunkAt(player.getLocation().getBlock().getChunk().getX(), player.getLocation().getBlock().getChunk().getZ() - 1);
		
		AutoBase base = getBase(c1);
		if (base != null) return base;
		
		base = getBase(c2);
		if (base != null) return base;
		
		base = getBase(c3);
		if (base != null) return base;
		
		base = getBase(c4);
		if (base != null) return base;
		
		return null;
	}
	
	public boolean newBase(Player player)
	{
		int x = player.getLocation().getBlock().getChunk().getX();
		int z = player.getLocation().getBlock().getChunk().getZ();
		
		int distanceToBeat = SAME_OWNER_BUFFER + 5;
		
		for (AutoBase b : bases)
		{
			if (!isOwner(player, b)) continue;
			if (b.world != player.getWorld()) continue;
			
			for (Point chunk : b.chunks)
			{
				if (Math.abs(chunk.x - x) > SAME_OWNER_BUFFER) continue;
				if (Math.abs(chunk.y - z) > SAME_OWNER_BUFFER) continue;
				
				int d = (int) Math.floor(Math.sqrt(Math.pow(chunk.x - x, 2) + Math.pow(chunk.y - z, 2)));
				
				if (d < distanceToBeat)
				{
					if (!SuperManager.isSuper(player)) { return false; }
				}
			}
		}
		return true;
	}
	
	public boolean isSuperOwner(String player, AutoBase base)
	{
		return player.equalsIgnoreCase(base.owner);
	}
	
	public boolean isSuperOwner(Player player, AutoBase base)
	{
		return player.getName().equalsIgnoreCase(base.owner);
	}
	
	public boolean isOwner(String player, AutoBase base)
	{
		if (base.hasFlag(Flag.PUBLIC)) return true;
		if (SuperManager.isSuper(player)) return true;
		
		if (base.owner.equalsIgnoreCase(player)) { return true; }
		
		for (String otherOwner : base.otherOwners)
		{
			if (otherOwner.equalsIgnoreCase(player)) { return true; }
		}
		
		return false;
	}
	
	public boolean isOwner(Player player, AutoBase base)
	{
		if (base.hasFlag(Flag.PUBLIC)) return true;
		if (SuperManager.isSuper(player)) return true;
		
		if (base.owner.equalsIgnoreCase(player.getName())) { return true; }
		
		for (String otherOwner : base.otherOwners)
		{
			if (otherOwner.equalsIgnoreCase(player.getName())) { return true; }
		}
		
		return false;
	}
	
	public boolean addBuilder(String builder, AutoBase base)
	{
		builder = builder.toLowerCase();
		
		if (base.otherOwners.contains(builder))
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
		
		if (!base.otherOwners.contains(builder))
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
		if (ownsBases(player)) return;
		
		if (player.getWorld().getName().startsWith("old_naxville")) return;
		
		if (!counters.containsKey(player))
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
		for (int i = 0; i < 1000; i++)
		{
			if (!ids.contains(owner + "_" + i))
			{
				ids.add(owner + "_" + i);
				return owner + "_" + i;
			}
		}
		
		return "IMPOSSIBLE_ID";
	}
	
	public void loadFile()
	{
		config = new NaxFile(plugin, "autobases");
	}
	
	public void loadAutoAreas()
	{
		loadFile();
		
		try
		{
			Set<String> keys = config.getKeys("autobases");
			
			for (String key : keys)
			{
				String prefix = "autobases." + key;
				
				String owner = config.getString(prefix + ".owner");
				
				if (!config.contains(prefix + ".world") || config.getString(prefix + ".world") == null)
				{
					continue;
				}
				
				World world = plugin.getServer().getWorld(config.getString(prefix + ".world"));
				
				if (world == null) continue;
				
				Set<String> flagKeys = config.getKeys(prefix + ".flags");
				Map<Flag, Boolean> flags = new HashMap<Flag, Boolean>();
				
				for (String flagKey : flagKeys)
				{
					Flag f = Flag.parseFlag(flagKey);
					if (f == null) continue;
					
					flags.put(f, Boolean.parseBoolean(config.getString(prefix + ".flags." + flagKey)));
				}
				
				Set<String> chunkList = config.getKeys(prefix + ".chunks");
				List<Point> chunks = new ArrayList<Point>();
				
				if (chunkList == null)
				{
					config.removeProperty(prefix);
					config.save();
					continue;
				}
				
				for (String chunk : chunkList)
				{
					chunks.add(new Point(config.getInt(prefix + ".chunks." + chunk + ".x"), config.getInt(prefix + ".chunks." + chunk + ".z")));
				}
				
				List<String> otherOwners = config.getStringList(prefix + ".otherOwners");
				
				AutoBase base = new AutoBase(this, key, owner, otherOwners, world, chunks, flags);
				bases.add(base);
				ids.add(key);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void saveBase(AutoBase base)
	{
		String prefix = "autobases." + base.id;
		
		config.set(prefix + ".owner", base.owner);
		config.set(prefix + ".world", base.world.getName());
		
		config.set(prefix + ".flags", "");
		
		for (Flag flag : base.flags.keySet())
		{
			config.set(prefix + ".flags." + flag, base.flags.get(flag).toString());
		}
		
		config.set(prefix + ".otherOwners", base.otherOwners);
		config.set(prefix + ".chunks", "");
		
		int i = 0;
		for (Point chunk : base.chunks)
		{
			config.set(prefix + ".chunks." + i + ".x", chunk.x);
			config.set(prefix + ".chunks." + i + ".z", chunk.y);
			i++;
		}
		
		config.save();
	}
	
	public void removeChunk(AutoBase base, Player player)
	{
		Point point = getPoint(player);
		if (base.chunks.contains(point))
		{
			base.chunks.remove(point);
		}
		
		saveBase(base);
	}
	
	public void removeBase(AutoBase base)
	{
		bases.remove(base);
		config.removeProperty("autobases." + base.id);
		config.save();
		
		refreshOwners(base);
	}
	
	public void addRemoveBuilder(Player player, String[] args)
	{
		AutoBase base = getBase(player);
		
		if (base == null || !isOwner(player, base))
		{
			player.sendMessage(Naxcraft.MSG_COLOR + "You are not within an area you own!");
			return;
		}
		
		if (!isSuperOwner(player, base))
		{
			player.sendMessage(Naxcraft.MSG_COLOR + "You are not the founder of this area!");
			return;
		}
		
		if (base.hasFlag(Flag.NO_ADD))
		{
			if (!SuperManager.isSuper(player))
			{
				player.sendMessage(NaxColor.ERR + "This area is locked from receiving new or removing any builders.");
				return;
			}
		}
		
		String s = args[2].replace("<", "").replace(">", "");
		String clean = "";
		
		NaxPlayer p = plugin.playerManager.getPlayer(s);
		
		if (p == null)
		{
			player.sendMessage(Naxcraft.MSG_COLOR + "There is no player by the name '" + s + "' at all.");
			
			return;
		}
		else
		{
			s = p.displayName;
			clean = p.name;
		}
		
		if (args[0].equalsIgnoreCase("add"))
		{
			if (SuperManager.isSuper(player) || charge(player, Material.GOLD_INGOT, 16))
			{
				if (addBuilder(clean, base))
				{
					player.sendMessage(clean + " has been added to the area!");
					refreshOwners(base);
				}
				else
				{
					player.sendMessage(clean + " was already added to this area!");
				}
			}
			else
			{
				player.sendMessage(Naxcraft.MSG_COLOR + "It costs 16 gold ingots in order to add/remove a builder.");
			}
		}
		else if (args[0].equalsIgnoreCase("remove"))
		{
			if (SuperManager.isSuper(player) || charge(player, Material.GOLD_INGOT, 16))
			{
				if (removeBuilder(clean, base))
				{
					player.sendMessage(Naxcraft.MSG_COLOR + clean + Naxcraft.MSG_COLOR + " has been removed from the area!");
					refreshOwners(base);
				}
				else
				{
					player.sendMessage(Naxcraft.MSG_COLOR + clean + " isn't a part of this area to begin with!");
				}
			}
			else
			{
				player.sendMessage(Naxcraft.MSG_COLOR + "It costs 16 gold ingots in order to add/remove a builder.");
			}
		}
		
	}
	
	public void refreshOwners(AutoBase base)
	{
		for (String s : base.getOwners())
		{
			for (Player p : plugin.getServer().getOnlinePlayers())
			{
				if (p.getName().equalsIgnoreCase(s))
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
