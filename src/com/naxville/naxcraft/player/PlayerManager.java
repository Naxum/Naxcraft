package com.naxville.naxcraft.player;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.entity.PoweredMinecart;
import org.bukkit.entity.StorageMinecart;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

import com.naxville.naxcraft.NaxColor;
import com.naxville.naxcraft.NaxFile;
import com.naxville.naxcraft.Naxcraft;
import com.naxville.naxcraft.admin.NaxcraftWarpgate.NaxGate;
import com.naxville.naxcraft.admin.SuperManager;
import com.naxville.naxcraft.autoareas.AutoAreaManager;
import com.naxville.naxcraft.autoareas.AutoAreaManager.Flag;
import com.naxville.naxcraft.autoareas.AutoBase;
import com.naxville.naxcraft.player.PartyManager.Party;

public class PlayerManager
{
	public Naxcraft plugin;
	public NaxFile config;
	public NaxFile inventories;
	public final String FILE_NAME = "players";
	
	public final PartyManager partyManager = new PartyManager(this);
	public final CheatingManager cheatingManager = new CheatingManager(this);
	
	public List<NaxPlayer> players = new ArrayList<NaxPlayer>();
	public Map<Location, Integer> fireBlocks = new HashMap<Location, Integer>();
	
	public Map<Player, Boolean> relogs = new HashMap<Player, Boolean>();
	
	public List<Player> nofire = new ArrayList<Player>();
	
	public Map<Player, Integer> kickCount = new HashMap<Player, Integer>();
	public Map<Player, ArrayList<Date>> kickTime = new HashMap<Player, ArrayList<Date>>();
	
	public Map<Player, Date> worldFalls = new HashMap<Player, Date>();
	
	// for /back command
	public Map<Player, Location> backs = new HashMap<Player, Location>();
	
	public PlayerManager(Naxcraft plugin)
	{
		this.plugin = plugin;
	}
	
	public enum Title
	{
		PATRON("$", "Patron", "Donate $20", ChatColor.GREEN),
		SPATRON("$", "Super Patron", "Donate $50", ChatColor.GREEN),
		TREASURE("T", "Treasure Hunter", "Pillage secret places", ChatColor.AQUA),
		FRIENDS("F", "Gang's All Here", "Invite 10+ People", ChatColor.YELLOW),
		GOLD_APPLE("A", "Expensive Tastes", "Eat a Golden Apple", ChatColor.GOLD),
		REDSTONE("R", "Red Stoner", "Make something crazy with redstone", ChatColor.DARK_RED);
		
		private String symbol, name, description;
		private ChatColor color;
		
		private Title(String symbol, String name, String description, ChatColor color)
		{
			this.symbol = symbol;
			this.name = name;
			this.description = description;
			this.color = color;
		}
		
		public String getSymbol()
		{
			return color + symbol + NaxColor.MSG;
		}
		
		public String getName()
		{
			return color + name + NaxColor.MSG;
		}
		
		public String getDescription()
		{
			return description;
		}
		
		public String getCleanName()
		{
			return name;
		}
		
		public String getCleanSymbol()
		{
			return symbol;
		}
	}
	
	public void handleKick(Player player, String string)
	{
		if (!kickTime.containsKey(player) || !kickCount.containsKey(player))
		{
			kickTime.put(player, new ArrayList<Date>());
			kickCount.put(player, 0);
		}
		
		ArrayList<Date> kicks = kickTime.get(player);
		kicks.add(new Date());
		
		kickTime.put(player, kicks);
		kickCount.put(player, kickCount.get(player) + 1);
		
		if (kickCount.get(player) > 5)
		{
			if (new Date().getTime() - kickTime.get(player).get(kickTime.get(player).size() - 5).getTime() < 1000 * 60 * 2)
			{
				String str = "AutoKick: Suspicious behavior.";
				player.kickPlayer(str);
				
				System.out.println("--------- PLAYER KICKED ----------");
				System.out.println(player.getName() + " for " + str);
				System.out.println("------- END PLAYER KICKED --------");
				
				kickTime.put(player, new ArrayList<Date>());
				kickCount.put(player, 0);
			}
		}
	}
	
	public void handleBadChestClick(Player player, String type)
	{
		if (!kickTime.containsKey(player) || kickTime.get(player).size() < 1) return;
		
		if (new Date().getTime() - kickTime.get(player).get(kickTime.get(player).size() - 1).getTime() < 1000 * 3)
		{
			String str = "AutoKick: Suspicious behavior.";
			
			player.kickPlayer(str);
			
			System.out.println("--------- PLAYER KICKED ----------");
			System.out.println(player.getName() + " for " + str);
			System.out.println("------- END PLAYER KICKED --------");
			
			kickTime.put(player, new ArrayList<Date>());
			kickCount.put(player, 0);
		}
	}
	
	public void handleBadFurnaceClick(Player player)
	{
		if (!kickTime.containsKey(player) || kickTime.get(player).size() < 1) return;
		
		if (new Date().getTime() - kickTime.get(player).get(kickTime.get(player).size() - 1).getTime() < 1000 * 3)
		{
			String str = "AutoKick: Suspicious behavior.";
			
			player.kickPlayer(str);
			
			System.out.println("--------- PLAYER KICKED ----------");
			System.out.println(player.getName() + " for " + str);
			System.out.println("------- END PLAYER KICKED --------");
			
			kickTime.put(player, new ArrayList<Date>());
			kickCount.put(player, 0);
		}
	}
	
	public void handleUnownedBedClick(Player player)
	{
		if (!kickTime.containsKey(player)) return;
		
		if (new Date().getTime() - kickTime.get(player).get(kickTime.get(player).size() - 1).getTime() < 1000 * 3)
		{
			String str = "AutoKick: Suspicious behavior.";
			player.kickPlayer(str);
			
			System.out.println("--------- PLAYER KICKED ----------");
			System.out.println(player.getName() + " for " + str);
			System.out.println("------- END PLAYER KICKED --------");
			
			kickTime.put(player, new ArrayList<Date>());
			kickCount.put(player, 0);
		}
	}
	
	public void handlePlayerJoin(PlayerJoinEvent event)
	{
		NaxPlayer p = getPlayer(event.getPlayer());
		Player player = event.getPlayer();
		
		if (p == null)
		{
			p = createNaxPlayer(event.getPlayer());
			event.getPlayer().teleport(event.getPlayer().getWorld().getSpawnLocation());
			plugin.announcer.announce(Naxcraft.SUCCESS_COLOR + "Welcome " + getDisplayName(event.getPlayer()) + Naxcraft.SUCCESS_COLOR + " to " + plugin.getWorldName(event.getPlayer().getWorld(), true) + Naxcraft.SUCCESS_COLOR + "!");
		}
		else
		{
			if (relogs.containsKey(player))
			{
				relogs.put(player, false);
				plugin.announcer.announce(getDisplayName(player) + " relogged.", player.getWorld());
			}
			else
			{
				if (p.isSuperPatron() || p.rank.isDemiAdmin())
				{
					event.getPlayer().getWorld().strikeLightningEffect(event.getPlayer().getLocation());
				}
				plugin.announcer.announce(getDisplayName(player) + " has logged into " + plugin.getWorldName(player.getWorld(), true) + ".");
			}
		}
	}
	
	public String getDisplayName(String str)
	{
		for (NaxPlayer p : players)
		{
			if (p.name.equalsIgnoreCase(str)) { return getDisplayName(p); }
		}
		
		return str;
	}
	
	public String getDisplayName(Player player)
	{
		NaxPlayer p = getPlayer(player);
		
		return getDisplayName(p, player);
	}
	
	public String getDisplayName(NaxPlayer p, Player player)
	{
		if (p == null)
		{
			return Naxcraft.DEFAULT_COLOR + player.getName() + Naxcraft.MSG_COLOR;
		}
		else if (p.displayName == null || p.displayName == "")
		{
			return p.rank.getPrefix() + Naxcraft.DEFAULT_COLOR + p.name + Naxcraft.MSG_COLOR;
		}
		else
		{
			return p.rank.getPrefix() + p.displayName + Naxcraft.MSG_COLOR;
		}
	}
	
	public String getDisplayName(NaxPlayer p)
	{
		if (p.displayName == null)
		{
			return p.rank.getPrefix() + Naxcraft.DEFAULT_COLOR + p.name + Naxcraft.MSG_COLOR;
		}
		else
		{
			return p.rank.getPrefix() + p.displayName + Naxcraft.MSG_COLOR;
		}
	}
	
	public NaxPlayer getPlayer(OfflinePlayer player)
	{
		return getPlayer(player.getName());
	}
	
	public NaxPlayer getPlayer(Player player)
	{
		for (NaxPlayer p : players)
		{
			if (p.name.equalsIgnoreCase(player.getName()))
			{
				if (!p.name.equals(player.getName()))
				{
					removePlayerName(p);
					p.name = player.getName();
					savePlayer(p);
				}
				return p;
			}
		}
		
		return null;
	}
	
	public NaxPlayer getPlayer(String string)
	{
		for (NaxPlayer p : players)
		{
			if (p.name.equalsIgnoreCase(string)) return p;
			
			if (p.name.toLowerCase().startsWith(string.toLowerCase())) return p;
		}
		
		return null;
	}
	
	public void handleEntityDamage(EntityDamageEvent event)
	{
		if (!(event.getEntity() instanceof Player) || event.isCancelled()) { return; }
		
		// NaxPlayer p = getPlayer((Player) event.getEntity());
		Player player = (Player) event.getEntity();
		
		if (event instanceof EntityDamageByEntityEvent)
		{
			EntityDamageByEntityEvent ev = (EntityDamageByEntityEvent) event;
			if (!(ev.getDamager() instanceof Player)) { return; }
			
			if (hasMuted(player, (Player) ev.getDamager()) || hasMuted((Player) ev.getDamager(), player))
			{
				event.setCancelled(true);
			}
			
			partyManager.handleEntityDamageByEntity(ev);
		}
		
		/*
		if (p.rank.id < PlayerRank.SUPER_PATRON.getId()) { return; }
		
		for (ItemStack armor : player.getInventory().getArmorContents())
		{
			if (armor == null) continue;
			
			if (armor.getType() == Material.GOLD_HELMET || armor.getType() == Material.GOLD_CHESTPLATE ||
					armor.getType() == Material.GOLD_LEGGINGS || armor.getType() == Material.GOLD_BOOTS)
			{
				armor.setDurability((short) (armor.getType().getMaxDurability() / 2));
			}
		}
		
		*/
	}
	
	public void handlePlayerQuit(PlayerQuitEvent event)
	{
		saveInventory(event.getPlayer(), event.getPlayer().getWorld());
		
		event.setQuitMessage(null);
		
		plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new RelogChecker(event.getPlayer()), 60L);
		
		relogs.put(event.getPlayer(), true);
		
		partyManager.handlePlayerQuit(event);
	}
	
	public void handlePlayerWorldChange(PlayerChangedWorldEvent event)
	{
		Player player = event.getPlayer();
		
		plugin.announcer.announce(getDisplayName(player) + Naxcraft.MSG_COLOR + " is now in " + plugin.getWorldName(player.getWorld(), true), event.getFrom());
		plugin.announcer.announce(getDisplayName(player) + Naxcraft.MSG_COLOR + " is now in " + plugin.getWorldName(player.getWorld(), true), player.getWorld());
		
		String w1 = event.getFrom().getName().replace("_nether", "").replace("_the_end", "");
		String w2 = event.getPlayer().getWorld().getName().replace("_nether", "").replace("_the_end", "");
		
		if (!w1.contains(w2))
		{
			saveInventory(player, event.getFrom());
			player.getInventory().clear();
			player.getInventory().setArmorContents(new ItemStack[] { null, null, null, null });
			loadInventory(player);
			
			player.sendMessage(NaxColor.MSG + "Your inventory cannot be brought to this world.");
		}
	}
	
	public void saveInventory(Player player, World world)
	{
		String worldName = world.getName();
		
		if (world.getName().endsWith("_nether"))
		{
			worldName.replace("_nether", "");
		}
		else if (world.getName().endsWith("_the_end"))
		{
			worldName.replace("_the_end", "");
		}
		
		inventories.setProperty("world_inventories." + player.getName() + "." + world.getName(), "");
		
		for (int i = 0; i < player.getInventory().getSize(); i++)
		{
			ItemStack item = player.getInventory().getItem(i);
			
			String prefix = "players." + player.getName() + "." + worldName + "." + i;
			
			if (item == null)
			{
				inventories.setProperty(prefix + ".type", "0");
				inventories.setProperty(prefix + ".amount", "0");
				inventories.setProperty(prefix + ".damage", "0");
				
				continue;
			}
			
			inventories.setProperty(prefix + ".type", item.getTypeId());
			inventories.setProperty(prefix + ".amount", item.getAmount());
			inventories.setProperty(prefix + ".damage", (int) item.getDurability());
		}
		
		for (int i = 0; i < player.getInventory().getArmorContents().length; i++)
		{
			ItemStack item = null;
			
			switch (i)
			{
				case 0:
					item = player.getInventory().getBoots();
					break;
				
				case 1:
					item = player.getInventory().getLeggings();
					break;
				
				case 2:
					item = player.getInventory().getChestplate();
					break;
				
				case 3:
					item = player.getInventory().getHelmet();
					break;
			}
			
			String prefix = "players." + player.getName() + "." + worldName + "." + (i + 100);
			
			if (item == null)
			{
				inventories.setProperty(prefix + ".type", "0");
				inventories.setProperty(prefix + ".amount", "0");
				inventories.setProperty(prefix + ".damage", "0");
				
				continue;
			}
			
			inventories.setProperty(prefix + ".type", item.getTypeId());
			inventories.setProperty(prefix + ".amount", item.getAmount());
			inventories.setProperty(prefix + ".damage", (int) item.getDurability());
		}
		
		inventories.save();
	}
	
	public void loadInventory(Player player)
	{
		World world = player.getWorld();
		
		String worldName = world.getName();
		
		if (world.getName().endsWith("_nether"))
		{
			worldName.replace("_nether", "");
		}
		
		Set<String> items = inventories.getKeys("world_inventories." + player.getName() + "." + worldName);
		
		if (items == null || items.isEmpty())
		{
			if (world.getName().equalsIgnoreCase("aether"))
			{
				player.getInventory().addItem(new ItemStack(Material.LOG, 4));
			}
			return;
		}
		
		for (String slot : items)
		{
			String prefix = "players." + player.getName() + "." + world.getName() + "." + slot;
			
			if (Integer.parseInt(inventories.getString(prefix + ".type")) == 0)
			{
				continue;
			}
			
			ItemStack item = new ItemStack(
											Integer.parseInt(inventories.getString(prefix + ".type")),
											Integer.parseInt(inventories.getString(prefix + ".amount")),
											Short.parseShort(inventories.getString(prefix + ".damage"))
											);
			
			if (Integer.parseInt(slot) < 100)
			{
				player.getInventory().setItem(Integer.parseInt(slot), item);
			}
			else
			{
				switch (Integer.parseInt(slot))
				{
					case 100:
						player.getInventory().setBoots(item);
						break;
					
					case 101:
						player.getInventory().setLeggings(item);
						break;
					
					case 102:
						player.getInventory().setChestplate(item);
						break;
					
					case 103:
						player.getInventory().setHelmet(item);
						break;
				}
			}
		}
	}
	
	public void handlePlayerMove(PlayerMoveEvent event)
	{
		if (event.getTo().getY() < 0)
		{
			Player player = event.getPlayer();
			
			if (worldFalls.containsKey(player))
			{
				if (new Date().getTime() - worldFalls.get(player).getTime() < 10000) { return; }
			}
			
			worldFalls.put(event.getPlayer(), new Date());
			event.getPlayer().damage(50);
			plugin.announcer.announce(getDisplayName(event.getPlayer()) + " just fell off the edge of the world!");
			return;
		}
		
		if (fireBlocks.containsKey(event.getPlayer().getLocation()))
		{
			event.getPlayer().setFireTicks(0);
		}
		
		NaxPlayer p = getPlayer(event.getPlayer());
		
		if (p != null && !nofire.contains(event.getPlayer()))
		{
			if (p.isSuperPatron())
			{
				if (event.getFrom().getBlock() != event.getTo().getBlock())
				{
					Material mat = event.getFrom().getBlock().getType();
					if (mat != Material.WATER && mat != Material.LAVA && mat != Material.STATIONARY_WATER && mat != Material.STATIONARY_LAVA && mat != Material.SIGN && mat != Material.WALL_SIGN)
					{
						boolean stop = false;
						List<Player> players = new ArrayList<Player>();
						
						for (Player player : event.getFrom().getWorld().getPlayers())
						{
							double distance = player.getLocation().distance(event.getFrom().getBlock().getLocation().add(0.5, 0.5, 0.5));
							
							// System.out.println(distance);
							
							if (distance <= 100)
							{
								
								if (distance < 0.3)
								{
									stop = true;
								}
								
								players.add(player);
							}
						}
						
						if (stop) { return; }
						
						for (Player player : players)
						{
							player.sendBlockChange(event.getFrom(), Material.FIRE, (byte) 0);
						}
						
						fireBlocks.put(event.getFrom(), 5);
					}
				}
			}
		}
	}
	
	public void handlePlayerTeleport(PlayerTeleportEvent event)
	{
		if (event.isCancelled()) return;
		
		if (event.getFrom().getWorld() != event.getTo().getWorld())
		{
			if (!getPlayer(event.getPlayer()).isDemiAdminOrPatron())
			{
				boolean okay = false;
				
				for (Player player : plugin.getServer().getOnlinePlayers())
				{
					if (getPlayer(player).rank.isAdmin())
					{
						if (event.getTo().getBlock() == player.getLocation().getBlock())
						{
							okay = true;
							break;
						}
					}
				}
				
				if (!okay)
				{
					for (NaxGate gate : plugin.warpgateCommand.gates)
					{
						if (gate.getLandingLocation() == event.getTo())
						{
							okay = true;
							break;
						}
					}
				}
				
				if (!okay)
				{
					event.getPlayer().sendMessage(NaxColor.MSG + "Only patrons+ can teleport across worlds. Use /spawn and go through a portal first.");
					event.setCancelled(true);
				}
			}
		}
		
		if (event.getFrom().getWorld() != event.getTo().getWorld() || event.getFrom().distance(event.getTo()) > 50)
		{
			if (getPlayer(event.getPlayer()).isSuperPatron() && !nofire.contains(event.getPlayer()))
			{
				event.getTo().getWorld().strikeLightningEffect(event.getTo());
				event.getFrom().getWorld().strikeLightningEffect(event.getFrom());
			}
		}
	}
	
	public List<Player> handlePlayerChat(Player player, List<Player> players)
	{
		NaxPlayer p = getPlayer(player);
		
		Map<String, Player> targets = new HashMap<String, Player>();
		
		for (Player px : players)
		{
			targets.put(px.getName(), px);
		}
		
		if (p.ignored != null)
		{
			for (String s : p.ignored)
			{
				if (targets.containsKey(s))
				{
					players.remove(targets.get(s));
				}
			}
		}
		
		for (String player2 : targets.keySet())
		{
			NaxPlayer p2 = getPlayer(player2);
			
			if (player2.equalsIgnoreCase(player.getName())) continue;
			
			if (p2.ignored != null)
			{
				if (p2.ignored.contains(player.getName()))
				{
					players.remove(targets.get(player2));
				}
			}
		}
		
		return players;
	}
	
	public boolean hasMuted(Player player, Player target)
	{
		NaxPlayer p = getPlayer(player);
		
		if (p.ignored == null)
		{
			return false;
		}
		else
		{
			for (String s : p.ignored)
			{
				if (target.getName().equalsIgnoreCase(s))
				{
					NaxPlayer p2 = getPlayer(target);
					if (p2.rank.isDemiAdmin())
					{
						p.ignored.remove(s);
						savePlayer(p);
					}
					else
					{
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	public boolean isPlayerInParty(Player player)
	{
		return partyManager.isPlayerInParty(player);
	}
	
	public Party getParty(Player player)
	{
		return partyManager.getParty(player);
	}
	
	public boolean runFireCommand(Player player)
	{
		if (nofire.contains(player))
		{
			nofire.remove(player);
			player.sendMessage(Naxcraft.MSG_COLOR + "Fire trail + lightning turned on!");
		}
		else
		{
			nofire.add(player);
			player.sendMessage(Naxcraft.MSG_COLOR + "Fire trail + lightning turned off until restart.");
		}
		
		return true;
	}
	
	public boolean runStatsCommand(Player p, String[] args)
	{
		if (args.length == 0)
		{
			p.sendMessage(NaxColor.MSG + "----");
			p.sendMessage(Naxcraft.COMMAND_COLOR + "Stats Command: " + Naxcraft.DEFAULT_COLOR + players.size() + " Players ever");
			
			for (PlayerRank rank : PlayerRank.values())
			{
				int count = 0;
				
				for (NaxPlayer np : players)
				{
					if (np.rank == rank)
					{
						count++;
					}
				}
				
				p.sendMessage(rank.getPrefix() + rank.getName() + ": " + Naxcraft.DEFAULT_COLOR + count + Naxcraft.MSG_COLOR + " players");
			}
			
			int bases = plugin.autoAreaManager.bases.size();
			int chunks = 0;
			int flags = 0;
			
			for (AutoBase base : plugin.autoAreaManager.bases)
			{
				chunks += base.chunks.size();
				for (boolean f : base.flags.values())
				{
					if (f)
					{
						flags++;
					}
				}
			}
			
			p.sendMessage(Naxcraft.MSG_COLOR + "Total Bases: " + Naxcraft.DEFAULT_COLOR + bases + Naxcraft.MSG_COLOR + ", total chunks bought: "
					+ Naxcraft.DEFAULT_COLOR + chunks + Naxcraft.MSG_COLOR + ", total flags bought: " + Naxcraft.DEFAULT_COLOR + flags);
			
			p.sendMessage(NaxColor.MSG + "Use /stats [name] to see player stats.");
		}
		else if (args.length == 1)
		{
			NaxPlayer np = plugin.playerManager.getPlayer(args[0]);
			if (np == null)
			{
				p.sendMessage(args[0] + NaxColor.MSG + " is not a player.");
				return true;
			}
			
			OfflinePlayer target = plugin.getServer().getOfflinePlayer(np.name);
			
			System.out.println(target.getFirstPlayed() + ", " + target.getLastPlayed());
			
			String started = timeAgo(target.getFirstPlayed());
			String last = timeAgo(target.getLastPlayed());
			int[] ownership = plugin.autoAreaManager.getNumberOwnedBases(target.getName());
			
			String extras = "";
			
			if (target.isBanned())
			{
				extras += NaxColor.MSG + "Currently banned.";
			}
			
			p.sendMessage(NaxColor.MSG + "----");
			p.sendMessage(NaxColor.COMMAND + "/stats for " + np.displayName);
			p.sendMessage(NaxColor.MSG + "Joined " + NaxColor.WHITE + started);
			p.sendMessage(NaxColor.MSG + "Last played " + NaxColor.WHITE + last);
			if (extras != "") p.sendMessage(NaxColor.MSG + extras);
			
			p.sendMessage(NaxColor.MSG + "Currently holds the rank " + np.rank.getPrefix() + np.rank.getName());
			p.sendMessage(NaxColor.MSG + "Has founded " + NaxColor.WHITE + ownership[0] + NaxColor.MSG + " bases.");
			p.sendMessage(NaxColor.MSG + "Is a builder in " + NaxColor.WHITE + ownership[1] + NaxColor.MSG + " bases.");
			
			String titles = "";
			String hidden = "";
			if (np.titles != null && !np.titles.isEmpty())
			{
				for (Title t : np.titles)
				{
					if (titles != "") titles += NaxColor.MSG + ", ";
					titles += t.getName();
				}
			}
			if (np.hiddenTitles != null && !np.hiddenTitles.isEmpty())
			{
				for (Title t : np.hiddenTitles)
				{
					if (titles != "") hidden += NaxColor.MSG + ", ";
					hidden += t.getName();
				}
			}
			
			if (titles != "")
			{
				p.sendMessage(NaxColor.MSG + "Active titles: " + titles);
			}
			
			if (hidden != "")
			{
				p.sendMessage(NaxColor.MSG + "Hidden titles: " + titles);
			}
		}
		
		return true;
	}
	
	public String timeAgo(long then)
	{
		
		long now = new Date().getTime();
		long diff = now - then;
		
		diff /= 1000;
		
		if (diff < 60) return Math.round(diff) + " seconds ago";
		
		diff /= 60;
		
		if (diff < 60) return Math.round(diff) + " minutes ago";
		
		diff /= 60;
		
		if (diff < 24) return Math.round(diff) + " hours ago";
		
		diff /= 24;
		
		return Math.round(diff) + " days ago";
	}
	
	public void loadFile()
	{
		config = new NaxFile(plugin, FILE_NAME);
		inventories = new NaxFile(plugin, "world_inventories");
	}
	
	public void loadPlayerData()
	{
		loadFile();
		
		Set<String> keys = config.getKeys("players");
		
		if (keys == null || keys.isEmpty())
		{
			System.out.println("No player data to load, continuing.");
			return;
		}
		
		for (String key : keys)
		{
			String prefix = "players." + key;
			
			if (config.getString(prefix + ".rank") == null) continue;
			
			String displayName = config.getString(prefix + ".displayName");
			PlayerRank rank = PlayerRank.getRank(config.getString(prefix + ".rank"));
			
			Set<String> homes = config.getKeys(prefix + ".homes");
			Map<String, Location> homesList = new HashMap<String, Location>();
			
			Set<String> ignoredList = config.getKeys(prefix + ".ignored");
			List<String> ignored = null;
			
			if (ignoredList != null)
			{
				ignored = new ArrayList<String>();
				
				for (String s : ignoredList)
				{
					ignored.add(config.getString(prefix + ".ignored." + s));
				}
			}
			
			String setHome = config.getString(prefix + ".sethome");
			if (setHome == null) setHome = "";
			
			boolean resave = false;
			
			if (homes != null)
			{
				for (String id : homes)
				{
					World world = null;
					
					try
					{
						world = plugin.getServer().getWorld(config.getString(prefix + ".homes." + id + ".world"));
					}
					catch (Exception e)
					{
						resave = true;
					}
					
					if (world != null)
					{
						homesList.put(id, new Location
								(
										world,
										config.getDouble(prefix + ".homes." + id + ".x"),
										config.getDouble(prefix + ".homes." + id + ".y"),
										config.getDouble(prefix + ".homes." + id + ".z")
								));
					}
					
				}
			}
			
			List<String> stringTiles = config.getStringList(prefix + ".titles");
			List<String> stringHiddenTiles = config.getStringList(prefix + ".hiddenTitles");
			
			List<Title> titles = new ArrayList<Title>();
			List<Title> hiddenTitles = new ArrayList<Title>();
			
			if (stringTiles != null && !stringTiles.isEmpty())
			{
				titleLoop: for (String s : stringTiles)
				{
					for (Title t : Title.values())
					{
						if (s.equalsIgnoreCase(t.name()))
						{
							titles.add(t);
							continue titleLoop;
						}
					}
				}
			}
			
			if (stringHiddenTiles != null && !stringHiddenTiles.isEmpty())
			{
				titleLoop: for (String s : stringHiddenTiles)
				{
					for (Title t : Title.values())
					{
						if (s.equalsIgnoreCase(t.name()))
						{
							hiddenTitles.add(t);
							continue titleLoop;
						}
					}
				}
			}
			
			NaxPlayer np = new NaxPlayer(plugin, key, rank, displayName, homesList, setHome, ignored, titles, hiddenTitles);
			
			if (resave)
			{
				savePlayer(np);
			}
			
			players.add(np);
		}
	}
	
	public NaxPlayer createNaxPlayer(Player p)
	{
		NaxPlayer np = new NaxPlayer(plugin, p.getName(), PlayerRank.getRank(0), null, null, "", null, null, null);
		players.add(np);
		savePlayer(np);
		
		return np;
	}
	
	public void setDisplayName(Player player, ChatColor color)
	{
		NaxPlayer p = getPlayer(player);
		
		p.displayName = color + p.name + Naxcraft.MSG_COLOR;
		savePlayer(p);
	}
	
	private void removePlayerName(NaxPlayer p)
	{
		config.removeProperty("players." + p.name);
		config.save();
	}
	
	public void savePlayer(NaxPlayer p)
	{
		String prefix = "players." + p.name;
		
		config.setProperty(prefix + ".rank", p.rank.name());
		
		if (p.displayName != null)
		{
			config.setProperty(prefix + ".displayName", p.displayName);
		}
		
		int j = 0;
		if (p.ignored != null && !p.ignored.isEmpty())
		{
			for (String name : p.ignored)
			{
				config.setProperty(prefix + ".ignored." + j, name);
				j++;
			}
		}
		else
		{
			config.setProperty(prefix + ".ignored", "");
		}
		
		config.setProperty(prefix + ".homes", "");
		
		if (p.homes != null && !p.homes.isEmpty())
		{
			for (String i : p.homes.keySet())
			{
				Location l = p.homes.get(i);
				
				World w = l.getWorld();
				
				config.setProperty(prefix + ".homes." + i + ".x", l.getX());
				config.setProperty(prefix + ".homes." + i + ".y", l.getY());
				config.setProperty(prefix + ".homes." + i + ".z", l.getZ());
				config.setProperty(prefix + ".homes." + i + ".world", w.getName());
			}
		}
		
		if (p.setHome != null)
		{
			config.setProperty(prefix + ".sethome", p.setHome);
		}
		
		if (p.titles != null)
		{
			List<String> stringTitles = new ArrayList<String>();
			for (Title t : p.titles)
			{
				stringTitles.add(t.name());
			}
			
			config.set(prefix + ".titles", stringTitles);
		}
		
		if (p.hiddenTitles != null)
		{
			List<String> stringTitles = new ArrayList<String>();
			for (Title t : p.hiddenTitles)
			{
				stringTitles.add(t.name());
			}
			
			config.set(prefix + ".hiddenTitles", stringTitles);
		}
		
		config.save();
	}
	
	public void setLastLocation(Player player)
	{
		backs.put(player, player.getLocation());
	}
	
	public void handleBlockDamage(BlockDamageEvent event)
	{
		if (event.isCancelled()) return;
		
		if (getPlayer(event.getPlayer()).isSuperPatron())
		{
			if (event.getPlayer().getItemInHand().getType() == Material.GOLD_PICKAXE)
			{
				event.getPlayer().getItemInHand().setDurability((short) (event.getPlayer().getItemInHand().getDurability() + 1));
				if (new Random().nextInt(5) == 0)
				{
					breakNearbyOres(event.getPlayer(), event.getBlock(), 0);
				}
			}
		}
	}
	
	public void handleBlockBreak(BlockBreakEvent event)
	{
		if (event.isCancelled()) return;
		
		if (getPlayer(event.getPlayer()).isSuperPatron())
		{
			if (event.getPlayer().getItemInHand().getType() == Material.GOLD_AXE)
			{
				event.setCancelled(true);
				breakNearbyLogs(event.getPlayer(), event.getBlock(), 0);
			}
			else if (event.getPlayer().getItemInHand().getType() == Material.GOLD_PICKAXE)
			{
				event.setCancelled(true);
				breakNearbyOres(event.getPlayer(), event.getBlock(), 0);
			}
			else if (event.getPlayer().getItemInHand().getType() == Material.GOLD_SPADE)
			{
				event.setCancelled(true);
				breakNearbyGround(event.getPlayer(), event.getBlock(), 0);
			}
		}
		
		cheatingManager.handleBlockBreak(event);
	}
	
	public void breakNearbyLogs(Player player, Block block, int i)
	{
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new LogBreaker(plugin.autoAreaManager, player, block, i), 5);
	}
	
	public void breakNearbyOres(Player player, Block block, int i)
	{
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new OreBreaker(plugin.autoAreaManager, player, block, i), 5);
	}
	
	public void breakNearbyGround(Player player, Block block, int i)
	{
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new GroundBreaker(plugin.autoAreaManager, player, block, i), 5);
	}
	
	public class LogBreaker implements Runnable
	{
		private AutoAreaManager aam;
		private Player player;
		private Block block;
		private int i;
		
		public LogBreaker(AutoAreaManager aam, Player player, Block block, int i)
		{
			this.aam = aam;
			this.player = player;
			this.block = block;
			this.i = i + 1;
		}
		
		public void run()
		{
			AutoBase base = aam.getBase(block);
			if (base != null && !aam.isOwner(player, base)) { return; }
			
			if (block.getType() != Material.LOG && block.getType() != Material.LEAVES) { return; }
			
			if (i > 15) { return; }
			
			Material type = block.getType();
			byte data = block.getData();
			block.setType(Material.AIR);
			
			if (type == Material.LOG)
			{
				block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.LOG, 1, data));
				if (new Random().nextInt(6) == 0) player.getItemInHand().setDurability((short) (player.getItemInHand().getDurability() + 1));
				if (player.getItemInHand().getDurability() >= player.getItemInHand().getType().getMaxDurability())
				{
					player.setItemInHand(null);
					return;
				}
			}
			else
			{
				if (new Random().nextInt(50) == 0)
				{
					player.getItemInHand().setDurability((short) (player.getItemInHand().getDurability() + 1));
					block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.SAPLING, 1));
				}
				if (player.getItemInHand().getDurability() >= player.getItemInHand().getType().getMaxDurability())
				{
					player.setItemInHand(null);
					return;
				}
			}
			
			breakNearbyLogs(player, block.getRelative(BlockFace.UP), i);
			breakNearbyLogs(player, block.getRelative(BlockFace.EAST), i);
			breakNearbyLogs(player, block.getRelative(BlockFace.NORTH), i);
			breakNearbyLogs(player, block.getRelative(BlockFace.WEST), i);
			breakNearbyLogs(player, block.getRelative(BlockFace.SOUTH), i);
			breakNearbyLogs(player, block.getRelative(BlockFace.DOWN), i);
		}
	}
	
	public class OreBreaker implements Runnable
	{
		private AutoAreaManager aam;
		private Player player;
		private Block block;
		private int i;
		
		public OreBreaker(AutoAreaManager aam, Player player, Block block, int i)
		{
			this.aam = aam;
			this.player = player;
			this.block = block;
			this.i = i + 1;
		}
		
		public void run()
		{
			AutoBase base = aam.getBase(block);
			if (base != null && !aam.isOwner(player, base)) { return; }
			
			if (block.getType() != Material.COAL_ORE && block.getType() != Material.IRON_ORE && block.getType() != Material.GOLD_ORE
					&& block.getType() != Material.DIAMOND_ORE && block.getType() != Material.REDSTONE_ORE && block.getType() != Material.GLOWING_REDSTONE_ORE) { return; }
			
			if (i > 15) { return; }
			
			Material type = block.getType();
			block.setType(Material.AIR);
			
			Material dropType = type;
			int amount = 1;
			
			if (type == Material.COAL_ORE)
			{
				dropType = Material.COAL;
			}
			else if (type == Material.DIAMOND_ORE)
			{
				dropType = Material.DIAMOND;
			}
			else if (type == Material.REDSTONE_ORE || type == Material.GLOWING_REDSTONE_ORE)
			{
				dropType = Material.REDSTONE;
				amount = new Random().nextInt(8) + 1;
			}
			
			block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(dropType, amount));
			player.getItemInHand().setDurability((short) (player.getItemInHand().getDurability() + 1));
			if (player.getItemInHand().getDurability() >= player.getItemInHand().getType().getMaxDurability())
			{
				player.setItemInHand(null);
				return;
			}
			
			breakNearbyOres(player, block.getRelative(BlockFace.UP), i);
			breakNearbyOres(player, block.getRelative(BlockFace.EAST), i);
			breakNearbyOres(player, block.getRelative(BlockFace.NORTH), i);
			breakNearbyOres(player, block.getRelative(BlockFace.WEST), i);
			breakNearbyOres(player, block.getRelative(BlockFace.SOUTH), i);
			breakNearbyOres(player, block.getRelative(BlockFace.DOWN), i);
		}
	}
	
	public class GroundBreaker implements Runnable
	{
		private AutoAreaManager aam;
		private Player player;
		private Block block;
		private int i;
		
		public GroundBreaker(AutoAreaManager aam, Player player, Block block, int i)
		{
			this.aam = aam;
			this.player = player;
			this.block = block;
			this.i = i + 1;
		}
		
		public void run()
		{
			AutoBase base = aam.getBase(block);
			if (base != null && !aam.isOwner(player, base)) { return; }
			
			if (block.getType() != Material.GRAVEL && block.getType() != Material.SAND && block.getType() != Material.GRASS && block.getType() != Material.DIRT) { return; }
			
			if (i > 3) { return; }
			
			Material type = block.getType();
			
			if (type == Material.GRASS || type == Material.DIRT)
			{
				if (base == null) { return; }
			}
			
			block.setType(Material.AIR);
			
			Material dropType = type;
			
			if (type == Material.GRASS)
			{
				dropType = Material.DIRT;
			}
			else if (type == Material.GRAVEL)
			{
				if (new Random().nextInt(5) == 0)
				{
					dropType = Material.FLINT;
				}
			}
			
			block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(dropType, 1));
			
			if (new Random().nextInt(5) == 0) player.getItemInHand().setDurability((short) (player.getItemInHand().getDurability() + 1));
			if (player.getItemInHand().getDurability() >= player.getItemInHand().getType().getMaxDurability())
			{
				player.setItemInHand(null);
				return;
			}
			
			breakNearbyGround(player, block.getRelative(BlockFace.UP), i);
			breakNearbyGround(player, block.getRelative(BlockFace.EAST), i);
			breakNearbyGround(player, block.getRelative(BlockFace.NORTH), i);
			breakNearbyGround(player, block.getRelative(BlockFace.WEST), i);
			breakNearbyGround(player, block.getRelative(BlockFace.SOUTH), i);
			breakNearbyGround(player, block.getRelative(BlockFace.DOWN), i);
		}
	}
	
	public void updateIgnoredPlayers()
	{
		for (Player player : plugin.getServer().getOnlinePlayers())
		{
			NaxPlayer p = getPlayer(player);
			
			if (p.ignored == null || p.ignored.isEmpty()) continue;
			
			for (String s : p.ignored)
			{
				Player target = plugin.getServer().getPlayer(s);
				
				if (target == null) continue;
				
				plugin.stealthCommand.hideFrom(player, target);
				plugin.stealthCommand.hideFrom(target, player);
			}
		}
	}
	
	public boolean addIgnoredPlayer(NaxPlayer p, NaxPlayer p2)
	{
		boolean ignored = false;
		
		if (p.ignored == null)
		{
			p.ignored = new ArrayList<String>();
		}
		
		if (!p.ignored.contains(p2.name))
		{
			p.ignored.add(p2.name);
			ignored = true;
		}
		else
		{
			p.ignored.remove(p2.name);
		}
		
		savePlayer(p);
		
		return ignored;
	}
	
	public enum PlayerRank
	{
		NOOB(""),
		MEMBER("+"),
		VETERAN("&"),
		MASTER("*"),
		DEMI_MOD("%"),
		MODERATOR("#"),
		ADMIN("@");
		
		private int id;
		private String prefix;
		
		private static int count = 0;
		
		private static final Map<String, PlayerRank> lookupName = new HashMap<String, PlayerRank>();
		private static final List<PlayerRank> ranksInOrder = new ArrayList<PlayerRank>();
		
		PlayerRank(String prefix)
		{
			this.prefix = prefix;
			init();
		}
		
		private void init()
		{
			id = count;
			count++;
		}
		
		public int getId()
		{
			return id;
		}
		
		public String getName()
		{
			String str = "";
			
			for (String x : this.name().split("_"))
			{
				if (str != "") str += " ";
				
				String s = x.charAt(0) + "";
				String r = x.substring(1);
				
				str += s.toUpperCase() + r.toLowerCase();
			}
			
			NaxColor color = NaxColor.WHITE;
			
			if (isDemiAdmin()) color = NaxColor.DEMI_ADMIN;
			
			if (isAdmin()) color = NaxColor.ADMIN;
			
			return color + str + Naxcraft.MSG_COLOR;
		}
		
		public String getPrefix()
		{
			return Naxcraft.MSG_COLOR + prefix;
		}
		
		static
		{
			for (int i = 0; i < values().length; i++)
			{
				ranksInOrder.add(values()[i].id, values()[i]);
			}
			
			for (PlayerRank rank : values())
			{
				lookupName.put(rank.name().toLowerCase(), rank);
			}
		}
		
		public static PlayerRank getRank(String str)
		{
			str = str.toLowerCase();
			
			if (lookupName.containsKey(str))
			{
				return lookupName.get(str);
			}
			else
			{
				return null;
			}
		}
		
		public static List<PlayerRank> getAllRanks()
		{
			List<PlayerRank> result = new ArrayList<PlayerRank>();
			
			for (String s : lookupName.keySet())
			{
				result.add(lookupName.get(s));
			}
			
			return result;
		}
		
		public static PlayerRank getRank(int i)
		{
			for (String s : lookupName.keySet())
			{
				if (lookupName.get(s).getId() == i) { return lookupName.get(s); }
			}
			return null;
		}
		
		public boolean isAdmin()
		{
			return id >= PlayerRank.MODERATOR.getId();
		}
		
		public boolean isDemiAdmin()
		{
			return id >= PlayerRank.DEMI_MOD.getId();
		}
		
		public static List<PlayerRank> getAllRanksInOrder()
		{
			return ranksInOrder;
		}
	}
	
	public boolean creativeCommand(Player player)
	{
		if (SuperManager.isSuper(player))
		{
			toggleGameMode(player);
			return true;
		}
		
		AutoBase base = plugin.autoAreaManager.getBase(player);
		
		if (base == null || !base.hasFlag(Flag.CREATIVE))
		{
			player.sendMessage(NaxColor.MSG + "You cannot go /creative outside an area you own with a creative flag!");
		}
		else
		{
			if (base.hasFlag(Flag.PUBLIC))
			{
				toggleGameMode(player);
			}
			else if (plugin.autoAreaManager.isOwner(player, base))
			{
				toggleGameMode(player);
			}
			else
			{
				player.sendMessage(NaxColor.MSG + "You cannot go /creative in areas you don't own!");
			}
		}
		
		return true;
	}
	
	public void toggleGameMode(Player player)
	{
		boolean okay = true;
		
		if (player.getGameMode() == GameMode.SURVIVAL)
		{
			for (ItemStack i : player.getInventory().getContents())
			{
				if (i != null && i.getAmount() != 0)
				{
					okay = false;
				}
			}
			
			for (ItemStack i : player.getInventory().getArmorContents())
			{
				if (i != null && i.getAmount() != 0)
				{
					okay = false;
				}
			}
		}
		
		if (SuperManager.isSuper(player))
		{
			okay = true;
		}
		
		if (!okay)
		{
			player.sendMessage(NaxColor.MSG + "You must have an empty inventory to go into /creative mode!");
		}
		else
		{
			if (player.getGameMode() == GameMode.SURVIVAL)
			{
				player.setGameMode(GameMode.CREATIVE);
				player.sendMessage(NaxColor.MSG + "You are now in creative mode!");
			}
			else
			{
				player.getInventory().clear();
				player.getInventory().setArmorContents(new ItemStack[] { null, null, null, null });
				
				player.setGameMode(GameMode.SURVIVAL);
				player.sendMessage(NaxColor.MSG + "You are now in survival mode!");
			}
		}
	}
	
	public void handlePlayerInteract(PlayerInteractEvent event)
	{
		if (event.isCancelled()) return;
		
		if (event.getPlayer().getGameMode() == GameMode.CREATIVE && !SuperManager.isSuper(event.getPlayer().getName()))
		{
			if (event.hasBlock())
			{
				Block b = event.getClickedBlock();
				
				if (b.getType() == Material.CHEST ||
						b.getType() == Material.DISPENSER ||
						b.getType() == Material.FURNACE ||
						b.getType() == Material.BURNING_FURNACE ||
						b.getType() == Material.JUKEBOX ||
						b.getType() == Material.BREWING_STAND ||
						b.getType() == Material.ENCHANTMENT_TABLE)
				{
					if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
					{
						event.getPlayer().sendMessage(NaxColor.MSG + "You may not use inventories while creative!");
						event.setCancelled(true);
					}
				}
			}
		}
	}
	
	public void handlePlayerInteractEntity(PlayerInteractEntityEvent event)
	{
		if (event.getPlayer().getGameMode() == GameMode.CREATIVE && !SuperManager.isSuper(event.getPlayer().getName()))
		{
			if (event.getRightClicked() instanceof PoweredMinecart || event.getRightClicked() instanceof StorageMinecart)
			{
				event.getPlayer().sendMessage(NaxColor.MSG + "You may not use inventories while creative!");
				event.setCancelled(true);
			}
		}
	}
	
	public void handlePlayerDropItem(PlayerDropItemEvent event)
	{
		Player player = event.getPlayer();
		if (player.getGameMode() == GameMode.CREATIVE && !SuperManager.isSuper(player))
		{
			event.getItemDrop().remove();
		}
	}
	
	public void handlePlayerPickupItem(PlayerPickupItemEvent event)
	{
		Player player = event.getPlayer();
		
		if (player.getGameMode() == GameMode.CREATIVE && !SuperManager.isSuper(player))
		{
			event.setCancelled(true);
		}
	}
	
	public class RelogChecker implements Runnable
	{
		Player player;
		
		public RelogChecker(Player player)
		{
			this.player = player;
		}
		
		public void run()
		{
			if (relogs.get(player))
			{
				plugin.announcer.announce(plugin.playerManager.getDisplayName(player) + " has left the game.", player.getWorld());
				plugin.announcer.announceToOtherWorlds(plugin.playerManager.getDisplayName(player) + " has left the game from " + plugin.getWorldName(player.getWorld(), true) + NaxColor.MSG + ".", player.getWorld());
			}
			
			relogs.remove(player);
		}
	}
	
	public String getTitles(Player player)
	{
		NaxPlayer np = getPlayer(player);
		
		String result = "";
		
		for (Title t : np.getTitles())
		{
			result += t.getSymbol();
		}
		
		if (result != "") return NaxColor.MSG + "[" + result + NaxColor.MSG + "] ";
		else return "";
	}
}
