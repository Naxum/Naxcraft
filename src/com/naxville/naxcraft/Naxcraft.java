package com.naxville.naxcraft;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.naxville.naxcraft.admin.DuplicateCommand;
import com.naxville.naxcraft.admin.EnchantCommand;
import com.naxville.naxcraft.admin.MobCommand;
import com.naxville.naxcraft.admin.NaxcraftBoomCommand;
import com.naxville.naxcraft.admin.NaxcraftDropCommand;
import com.naxville.naxcraft.admin.NaxcraftFreezeCommand;
import com.naxville.naxcraft.admin.NaxcraftGiveCommand;
import com.naxville.naxcraft.admin.NaxcraftGodCommand;
import com.naxville.naxcraft.admin.NaxcraftKillCommand;
import com.naxville.naxcraft.admin.NaxcraftModkitCommand;
import com.naxville.naxcraft.admin.NaxcraftMotdCommand;
import com.naxville.naxcraft.admin.NaxcraftSpawnMobCommand;
import com.naxville.naxcraft.admin.NaxcraftStealthCommand;
import com.naxville.naxcraft.admin.NaxcraftStrikeCommand;
import com.naxville.naxcraft.admin.NaxcraftTimeCommand;
import com.naxville.naxcraft.admin.NaxcraftWarpgate;
import com.naxville.naxcraft.admin.NaxcraftWeatherCommand;
import com.naxville.naxcraft.admin.SuperManager;
import com.naxville.naxcraft.admin.TitleCommand;
import com.naxville.naxcraft.atms.AtmManager;
import com.naxville.naxcraft.autoareas.AutoAreaManager;
import com.naxville.naxcraft.autoareas.CityManager;
import com.naxville.naxcraft.autoareas.PropertyCommand;
import com.naxville.naxcraft.bloodmoon.BloodMoonManager;
import com.naxville.naxcraft.listeners.NaxBlockListener;
import com.naxville.naxcraft.listeners.NaxEntityListener;
import com.naxville.naxcraft.listeners.NaxInventoryListener;
import com.naxville.naxcraft.listeners.NaxPlayerListener;
import com.naxville.naxcraft.listeners.NaxVehicleListener;
import com.naxville.naxcraft.mail.MailManager;
import com.naxville.naxcraft.player.BackCommand;
import com.naxville.naxcraft.player.FireWatcher;
import com.naxville.naxcraft.player.IgnoreCommand;
import com.naxville.naxcraft.player.KickCommand;
import com.naxville.naxcraft.player.NaxPlayer;
import com.naxville.naxcraft.player.PlayerManager;
import com.naxville.naxcraft.player.PlayerManager.PlayerRank;
import com.naxville.naxcraft.player.RankCommand;
import com.naxville.naxcraft.shops.DisplayShopManager;
import com.naxville.naxcraft.shops.ShopManager;
import com.naxville.naxcraft.skills.LevelCommand;

public class Naxcraft extends JavaPlugin
{
	// listeners
	protected final NaxEntityListener entityListener = new NaxEntityListener(this);
	protected final NaxPlayerListener playerListener = new NaxPlayerListener(this);
	protected final NaxBlockListener blockListener = new NaxBlockListener(this);
	protected final NaxInventoryListener inventoryListener = new NaxInventoryListener(this);
	public final NaxVehicleListener vehicleListener = new NaxVehicleListener(this);
	
	// announcer
	public final Announcer announcer = new Announcer(this);
	
	// new stuff
	public final AutoAreaManager autoAreaManager = new AutoAreaManager(this);
	public final BloodMoonManager bloodMoonManager = new BloodMoonManager(this);
	public final AtmManager atmManager = new AtmManager(this);
	public final PlayerManager playerManager = new PlayerManager(this);
	public final BackCommand backCommand = new BackCommand(playerManager);
	public final ShopManager shopManager = new ShopManager(this);
	public final PropertyCommand propertyCommand = new PropertyCommand(this);
	public final RankCommand rankCommand = new RankCommand(this);
	public final KickCommand kickCommand = new KickCommand(this);
	public final IgnoreCommand ignoreCommand = new IgnoreCommand(playerManager);
	public final MailManager mailManager = new MailManager(this);
	public final CityManager cityManager = new CityManager(this);
	public final LevelCommand levelCommand = new LevelCommand(this);
	public final EnchantCommand enchantCommand = new EnchantCommand(this);
	public final DuplicateCommand duplicateCommand = new DuplicateCommand(this);
	
	// admins commands
	public final NaxcraftGodCommand godCommand = new NaxcraftGodCommand(this);
	protected final NaxcraftMotdCommand motdCommand = new NaxcraftMotdCommand(this);
	public final NaxcraftTpCommand tpCommand = new NaxcraftTpCommand(this);
	protected final NaxcraftTphereCommand tphereCommand = new NaxcraftTphereCommand(this);
	protected final NaxcraftModkitCommand modkitCommand = new NaxcraftModkitCommand(this);
	protected final NaxcraftDropCommand dropCommand = new NaxcraftDropCommand(this);
	protected final NaxcraftFreezeCommand freezeCommand = new NaxcraftFreezeCommand(this);
	protected final NaxcraftKillCommand killCommand = new NaxcraftKillCommand(this);
	protected final NaxcraftGiveCommand giveCommand = new NaxcraftGiveCommand(this);
	protected final NaxcraftTimeCommand timeCommand = new NaxcraftTimeCommand(this);
	public final SuperManager superCommand = new SuperManager(this);
	public final NaxcraftSpawnMobCommand spawnMobCommand = new NaxcraftSpawnMobCommand(this);
	public final NaxcraftWarpgate warpgateCommand = new NaxcraftWarpgate(this);
	public final NaxcraftStealthCommand stealthCommand = new NaxcraftStealthCommand(this);
	public final NaxcraftStrikeCommand strikeCommand = new NaxcraftStrikeCommand(this);
	public final NaxcraftBoomCommand boomCommand = new NaxcraftBoomCommand(this);
	public final NaxcraftWeatherCommand weatherCommand = new NaxcraftWeatherCommand(this);
	public final MobCommand mobCommand = new MobCommand(this);
	public final JoinCommand joinCommand = new JoinCommand(this);
	public final DisplayShopManager displayShopManager = new DisplayShopManager(this);
	
	// public commands
	protected final NaxcraftHelpCommand helpCommand = new NaxcraftHelpCommand(this);
	protected final NaxcraftWhoCommand whoCommand = new NaxcraftWhoCommand(this);
	protected final NaxcraftColorCommand colorCommand = new NaxcraftColorCommand(this);
	protected final NaxcraftHomeCommand homeCommand = new NaxcraftHomeCommand(this);
	public final NaxcraftChatCommand chatCommand = new NaxcraftChatCommand(this);
	
	public HashMap<String, Integer> warpingPlayers = new HashMap<String, Integer>();
	
	// special values
	public Map<Player, Boolean> destroyDrops = new HashMap<Player, Boolean>();
	public Map<Player, Boolean> frozenPlayers = new HashMap<Player, Boolean>();
	
	public Map<Player, Location> position1 = new HashMap<Player, Location>();
	public Map<Player, Location> position2 = new HashMap<Player, Location>();
	
	public String motd = "Welcome to Naxville!"; // gets renamed in onEnable
	public Map<String, String> nickNames = new HashMap<String, String>();
	public Map<String, Integer> regenValues = new HashMap<String, Integer>();
	public boolean stopFire = false;
	
	public final Logger log = Logger.getLogger("Minecraft.Naxcraft");
	
	// files
	public final String filePath = "plugins/Naxcraft/";
	
	public static final String OLD_NAXVILLE = "old_naxville";
	
	// private String nickNameFile = "UserData.txt";
	public String warpFile = "Warps.txt";
	
	// chat colors
	public static ChatColor[] COLORS = {
			ChatColor.BLACK, // Admin color
			ChatColor.AQUA,
			ChatColor.DARK_AQUA,
			ChatColor.BLUE,
			ChatColor.DARK_BLUE,
			ChatColor.GOLD,
			ChatColor.GRAY,
			ChatColor.DARK_GRAY,
			ChatColor.GREEN,
			ChatColor.DARK_GREEN,
			ChatColor.DARK_PURPLE,
			ChatColor.RED,
			ChatColor.DARK_RED,
			ChatColor.WHITE,
			ChatColor.YELLOW,
			ChatColor.LIGHT_PURPLE }; // Admin color
	
	// constants
	public static String PERMISSIONS_FAIL = ChatColor.RED + "You do not have permission to use the %s command.";
	public static String NOT_A_PLAYER = "ERROR: This can only be done by a player";
	
	public static ChatColor DEFAULT_COLOR = ChatColor.WHITE;
	public static ChatColor COMMAND_COLOR = ChatColor.YELLOW;
	public static ChatColor ADMIN_COLOR = ChatColor.LIGHT_PURPLE;
	public static ChatColor ERROR_COLOR = ChatColor.RED;
	public static ChatColor SUCCESS_COLOR = ChatColor.GREEN;
	public static ChatColor MSG_COLOR = ChatColor.GRAY;
	public static ChatColor WORLD_COLOR = ChatColor.DARK_AQUA;
	public static ChatColor NETHER_COLOR = ChatColor.DARK_RED;
	public static ChatColor CLAN_COLOR = ChatColor.GOLD;
	public static ChatColor NPC_COLOR = ChatColor.AQUA;
	
	public void onDisable()
	{
		System.out.println("Naxcaft plugin successfully disabled.");
	}
	
	public void onEnable()
	{
		PluginManager pm = getServer().getPluginManager();
		
		pm.registerEvents(blockListener, this);
		pm.registerEvents(entityListener, this);
		pm.registerEvents(playerListener, this);
		pm.registerEvents(vehicleListener, this);
		pm.registerEvents(inventoryListener, this);
		
		PluginDescriptionFile pdfFile = this.getDescription();
		this.motd = Naxcraft.COMMAND_COLOR + " " + pdfFile.getName() + " plugin version " + pdfFile.getVersion() + " is on!";
		
		loadFiles();
		
		if (this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new NaxcraftRegenRunnable(this), 0, 20) == -1) this.log.log(Level.SEVERE, "Naxcraft Startup: Error starting health regen.");
		
		getServer().getScheduler().scheduleSyncDelayedTask(this, new FireWatcher(playerManager), 5);
		
		getServer().setSpawnRadius(0);
		
		getCommand("title").setExecutor(new TitleCommand(this));
		getCommand("titles").setExecutor(new TitlesCommand(this));
		
		log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is ready to rock!");
	}
	
	public static void download(Logger log, URL url, File file) throws IOException
	{
		if (!file.getParentFile().exists()) file.getParentFile().mkdir();
		
		if (file.exists()) file.delete();
		
		file.createNewFile();
		
		final int size = url.openConnection().getContentLength();
		log.info("Downloading " + file.getName() + " (" + size / 1024 + "kb) ...");
		final InputStream in = url.openStream();
		final OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
		final byte[] buffer = new byte[1024];
		int len, downloaded = 0, msgs = 0;
		final long start = System.currentTimeMillis();
		while ((len = in.read(buffer)) >= 0)
		{
			out.write(buffer, 0, len);
			downloaded += len;
			if ((int) ((System.currentTimeMillis() - start) / 500) > msgs)
			{
				log.info((int) ((double) downloaded / (double) size * 100d) + "%");
				msgs++;
			}
		}
		in.close();
		out.close();
		log.info("Download finished.");
	}
	
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args)
	{
		String commandName = command.getName();
		
		if (commandName.equalsIgnoreCase("save-all")) { return runSaveCommand(sender); }
		
		if (commandName.equalsIgnoreCase("transcend")) { return this.superCommand.transcend(sender, args); }
		
		if (commandName.equalsIgnoreCase("ranks"))
		{
			sender.sendMessage(Naxcraft.MSG_COLOR + "----");
			sender.sendMessage(Naxcraft.COMMAND_COLOR + "/ranks command:");
			
			for (PlayerRank rank : PlayerRank.getAllRanksInOrder())
			{
				sender.sendMessage(rank.getPrefix() + rank.getName());
			}
			
			return true;
		}
		
		// floating commands
		if (commandName.equalsIgnoreCase("god")) { return this.godCommand.runGodCommand(sender, args); }
		
		if (commandName.equalsIgnoreCase("spawnmob")) { return this.spawnMobCommand.runSpawnMobCommand(sender, args); }
		
		if (commandName.equalsIgnoreCase("super")) { return this.superCommand.runSuperCommand(sender, args); }
		
		if (commandName.equalsIgnoreCase("give")) { return this.giveCommand.runGiveCommand(sender, args); }
		
		if (commandName.equalsIgnoreCase("modkit")) { return this.modkitCommand.runModkitCommand(sender, args); }
		if (commandName.equalsIgnoreCase("time")) { return this.timeCommand.runTimeCommand(sender, args); }
		
		if (commandName.equalsIgnoreCase("drop")) { return this.dropCommand.runDropCommand(sender, args); }
		
		if (commandName.equalsIgnoreCase("motd")) { return this.motdCommand.runMotdCommand(sender, args); }
		
		if (commandName.equalsIgnoreCase("freeze")) { return this.freezeCommand.runFreezeCommand(sender, args); }
		
		if (commandName.equalsIgnoreCase("wg") || commandName.equalsIgnoreCase("warpgate")) { return this.warpgateCommand.runCommand(sender, args); }
		
		if (commandName.equalsIgnoreCase("tp")) { return this.tpCommand.runTpCommand(sender, args); }
		
		if (commandName.equalsIgnoreCase("yes") || commandName.equalsIgnoreCase("no")) { return this.tpCommand.getPermission(sender, commandName); }
		
		if (commandName.equalsIgnoreCase("tphere")) return this.tphereCommand.runTphereCommand(sender, args);
		
		if (commandName.equalsIgnoreCase("tpthere")) return this.tphereCommand.runTpThereCommand(sender, args);
		
		if (commandName.equalsIgnoreCase("stealth")) return this.stealthCommand.runCommand(sender, args);
		
		if (commandName.equalsIgnoreCase("strike")) return this.strikeCommand.runCommand(sender, args);
		
		if (commandName.equalsIgnoreCase("boom")) return this.boomCommand.runCommand(sender, args);
		
		if (commandName.equalsIgnoreCase("weather")) return this.weatherCommand.runCommand(sender, args);
		
		if (commandName.equalsIgnoreCase("creative")) return playerManager.creativeCommand((Player) sender);
		
		if (commandName.equalsIgnoreCase("feed"))
		{
			Player p = (Player) sender;
			
			if (playerManager.getPlayer(p).rank.isAdmin())
			{
				if (args.length == 0)
				{
					p.setFoodLevel(20);
					p.setExhaustion(0);
					
					p.sendMessage(NaxColor.MSG + "NOM NOM NOM");
				}
				else if (args.length == 1)
				{
					Player player = getServer().getPlayer(args[0]);
					
					if (player == null)
					{
						p.sendMessage(NaxColor.MSG + "That's not a player.");
						return true;
					}
					
					player.setFoodLevel(20);
					player.setExhaustion(0);
					
					p.sendMessage(getNickName(player) + " is full!");
					player.sendMessage(getNickName(p) + " fed you!");
				}
			}
			else
			{
				p.sendMessage(NaxColor.MSG + "Nope.");
			}
			
			return true;
		}
		
		if (commandName.equalsIgnoreCase("hat"))
		{
			Player p = (Player) sender;
			NaxPlayer np = playerManager.getPlayer(p);
			if (np.isPatron() || np.rank.isDemiAdmin())
			{
				if (p.getGameMode() == GameMode.CREATIVE && !SuperManager.isSuper(p))
				{
					p.sendMessage(NaxColor.MSG + "You cannot use this command while in creative.");
					return true;
				}
				
				if (p.getItemInHand() == null || p.getItemInHand().getAmount() == 0)
				{
					p.sendMessage(NaxColor.MSG + "No air, knock it off.");
					return true;
				}
				
				if (p.getInventory().getHelmet() == null || p.getInventory().getHelmet().getAmount() != 0)
				{
					if (!SuperManager.isSuper(p))
					{
						p.sendMessage(NaxColor.MSG + "Clear your hat slot first!");
						return true;
					}
				}
				
				if (p.getInventory().getItemInHand().getType().isBlock())
				{
					ItemStack i = p.getItemInHand().clone();
					i.setAmount(1);
					
					if (!SuperManager.isSuper(p))
					{
						p.getInventory().remove(i);
					}
					
					p.getInventory().setHelmet(i);
				}
			}
			else
			{
				p.sendMessage(NaxColor.MSG + "Nope.");
			}
			
			return true;
		}
		
		if (commandName.equalsIgnoreCase("enchant"))
		{
			enchantCommand.runCommand((Player) sender, args);
			return true;
		}
		
		if (commandName.equalsIgnoreCase("duplicate"))
		{
			duplicateCommand.runCommand((Player) sender, args);
			return true;
		}
		
		if (commandName.equalsIgnoreCase("mail"))
		{
			if (!(sender instanceof Player)) return true;
			
			mailManager.runMailCommand((Player) sender, args);
			return true;
		}
		
		if (commandName.equalsIgnoreCase("promote"))
		{
			
			if (sender instanceof Player)
			{
				Player p = (Player) sender;
				
				if (playerManager.getPlayer(p).rank.isAdmin() || p.getName().equalsIgnoreCase("naxum") || p.getName().equalsIgnoreCase("robospunk"))
				{
					return rankCommand.runCommand(p, true, args);
				}
				else
				{
					joinCommand.runPromoteCommand(p);
					return true;
				}
			}
			else
			{
				sender.sendMessage("Sorry, only an in-game player can promote people. :\\");
			}
		}
		
		if (commandName.equalsIgnoreCase("demote")) return rankCommand.runCommand((Player) sender, false, args);
		
		if (commandName.equalsIgnoreCase("kick")) return kickCommand.runKickCommand((Player) sender, args);
		
		if (commandName.equalsIgnoreCase("join"))
		{
			joinCommand.runJoinCommand((Player) sender);
			return true;
		}
		
		if (commandName.equalsIgnoreCase("city"))
		{
			if (!(sender instanceof Player)) return true;
			cityManager.runCityCommand((Player) sender, args);
			return true;
		}
		
		if (commandName.equalsIgnoreCase("property") || commandName.equalsIgnoreCase("pr")) return propertyCommand.runCommand((Player) sender, args);
		
		if (commandName.equalsIgnoreCase("bc")) return propertyCommand.runCommand((Player) sender, new String[] { "buy", "chunk" });
		
		if (commandName.equalsIgnoreCase("borders")) return propertyCommand.runCommand((Player) sender, new String[] { "borders" });
		
		if (commandName.equalsIgnoreCase("noborders")) return propertyCommand.runCommand((Player) sender, new String[] { "noborders" });
		
		if (commandName.equalsIgnoreCase("back")) return backCommand.runCommand((Player) sender, args);
		
		if (commandName.equalsIgnoreCase("bf"))
		{
			if (args.length == 1)
			{
				return propertyCommand.runCommand((Player) sender, new String[] { "buy", "flag", args[0] });
			}
			else
			{
				return autoAreaManager.flagsCommand((Player) sender, new String[] {});
			}
		}
		
		if (commandName.equalsIgnoreCase("flags")) return autoAreaManager.flagsCommand((Player) sender, args);
		
		if (commandName.equalsIgnoreCase("help")) { return this.helpCommand.runHelpCommand(sender, args); }
		
		if (commandName.equalsIgnoreCase("who")) { return this.whoCommand.runWhoCommand(sender, args); }
		
		if ((commandName.equalsIgnoreCase("color")) || (commandName.equalsIgnoreCase("colour"))) { return this.colorCommand.runColorCommand(sender, args); }
		
		if (commandName.equalsIgnoreCase("home")) { return this.homeCommand.runHomeCommand(sender, args); }
		
		if (commandName.equalsIgnoreCase("ignore")) { return this.ignoreCommand.runCommand((Player) sender, args); }
		
		if (commandName.equalsIgnoreCase("fire")) { return this.playerManager.runFireCommand((Player) sender); }
		
		if (commandName.equalsIgnoreCase("stats")) { return this.playerManager.runStatsCommand((Player) sender, args); }
		
		if (commandName.equalsIgnoreCase("setspawn")) return this.runSetSpawnCommand((Player) sender);
		
		if (commandName.equalsIgnoreCase("spawn")) return homeCommand.runSpawnCommand((Player) sender);
		
		if (commandName.equalsIgnoreCase("list")) { return this.chatCommand.runChatCommand(sender, commandName, args); }
		
		if (commandName.equalsIgnoreCase("npcs")) { return this.chatCommand.runChatCommand(sender, commandName, args); }
		
		if (commandName.equalsIgnoreCase("kill")) { return this.killCommand.runKillCommand(sender, args); }
		
		if (commandName.equalsIgnoreCase("level"))
		{
			if (sender instanceof Player) levelCommand.runCommand((Player) sender, args);
			return true;
		}
		
		if (commandName.equalsIgnoreCase("mob")) { return mobCommand.runCommand((Player) sender, args); }
		
		if (commandName.equalsIgnoreCase("party")) { return playerManager.partyManager.runPartyCommand((Player) sender, args); }
		
		// chat commands
		if (commandName.equalsIgnoreCase("g") || commandName.equalsIgnoreCase("local") || commandName.equalsIgnoreCase("l") || commandName.equalsIgnoreCase("p") || commandName.equalsIgnoreCase("chat")
				|| commandName.equalsIgnoreCase("m") || commandName.equalsIgnoreCase("msg") || commandName.equalsIgnoreCase("r") || commandName.equalsIgnoreCase("tell") || commandName.equalsIgnoreCase("me")) { return this.chatCommand.runChatCommand(sender, commandName, args); }
		
		return false;
	}
	
	public String getNickName(Player player)
	{
		return playerManager.getDisplayName(player);
	}
	
	public String getNickName(String name)
	{
		if (name.equalsIgnoreCase("server")) { return NaxColor.ADMIN + "Server"; }
		return playerManager.getDisplayName(name);
	}
	
	public String getWorldName(World world)
	{
		String name = "";
		int i = 0;
		
		for (String piece : world.getName().replace("_nether", "").replace("_the_end", "").split("_"))
		{
			if (i > 0) name += " ";
			
			String start = piece.charAt(0) + "";
			String rest = piece.substring(1);
			
			name += start.toUpperCase() + rest;
			i++;
		}
		
		return name;
	}
	
	public String getWorldName(World world, boolean returnColored)
	{
		String name = getWorldName(world);
		NaxColor c = NaxColor.MULTI;
		
		if (!returnColored)
		{
			return name;
		}
		else
		{
			if (world.getName().endsWith("_nether"))
			{
				c = NaxColor.NETHER;
			}
			else if (world.getName().endsWith("_the_end"))
			{
				c = NaxColor.THE_END;
			}
			
			return c + name + Naxcraft.MSG_COLOR;
		}
	}
	
	public boolean runSaveCommand(CommandSender sender)
	{
		if (sender instanceof Player)
		{
			if (!playerManager.getPlayer((Player) sender).rank.isAdmin())
			{
				((Player) sender).sendMessage(Naxcraft.MSG_COLOR + "Thanks for trying, but only an admin can save the server!");
				return true;
			}
			
			getServer().broadcastMessage(getNickName((Player) sender) + " is saving the server, wait for a sec.");
		}
		
		getServer().savePlayers();
		
		for (World world : getServer().getWorlds())
		{
			world.save();
		}
		
		if (sender instanceof Player)
		{
			getServer().broadcastMessage(Naxcraft.MSG_COLOR + "Save complete!");
		}
		
		return true;
	}
	
	public boolean runSetSpawnCommand(Player player)
	{
		World world = player.getWorld();
		
		if (!playerManager.getPlayer(player).rank.isAdmin()) { return true; }
		
		world.setSpawnLocation(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());
		
		player.sendMessage(Naxcraft.MSG_COLOR + "World spawn set.");
		
		return true;
	}
	
	private void loadFiles()
	{
		// getServer().createWorld("old_naxville", Environment.NORMAL);
		getServer().createWorld(new WorldCreator("naxville"));
		// getServer().createWorld(new WorldCreator("galingale"));
		
		if (!shopManager.loadShops()) this.log.log(Level.SEVERE, "Naxcraft Startup: Error loading shops.");
		
		if (!warpgateCommand.loadWarpGates()) this.log.log(Level.SEVERE, "Naxcraft Startup: Error loading warpgates.");
		
		atmManager.loadAtms();
		autoAreaManager.loadAutoAreas();
		playerManager.loadPlayerData();
		mailManager.loadMessages();
		cityManager.loadFile();
		displayShopManager.loadShops();
		
		autoAreaManager.snowmenManager.startSnowmenTargeter();
		inventoryListener.startForgeSmelter();
	}
	
	public String getTitles(Player player)
	{
		return playerManager.getTitles(player);
	}
}
