package com.naxville.naxcraft;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

import com.naxville.naxcraft.admin.MobCommand;
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
import com.naxville.naxcraft.admin.NaxcraftSuperCommand;
import com.naxville.naxcraft.admin.NaxcraftTimeCommand;
import com.naxville.naxcraft.admin.NaxcraftWarpgate;
import com.naxville.naxcraft.admin.NaxcraftWeatherCommand;
import com.naxville.naxcraft.atms.AtmManager;
import com.naxville.naxcraft.autoareas.AutoAreaManager;
import com.naxville.naxcraft.autoareas.PropertyCommand;
import com.naxville.naxcraft.land.NaxcraftAreaCommand;
import com.naxville.naxcraft.land.NaxcraftGroupCommand;
import com.naxville.naxcraft.listeners.NaxcraftBlockListener;
import com.naxville.naxcraft.listeners.NaxcraftEntityListener;
import com.naxville.naxcraft.listeners.NaxcraftPlayerListener;
import com.naxville.naxcraft.player.PlayerManager;
import com.naxville.naxcraft.player.PlayerManager.PlayerRank;
import com.naxville.naxcraft.player.RankCommand;
import com.naxville.naxcraft.shops.ShopManager;


public class Naxcraft extends JavaPlugin {
	//listeners
	protected final NaxcraftEntityListener entityListener = new NaxcraftEntityListener(this);
	protected final NaxcraftPlayerListener playerListener = new NaxcraftPlayerListener(this);
	public final NaxcraftBlockListener blockListener = new NaxcraftBlockListener(this);

	//announcer
	public final Announcer announcer = new Announcer(this);
	
	//autoareas
	public final AutoAreaManager autoAreaManager = new AutoAreaManager(this);
	public final AtmManager atmManager = new AtmManager(this);
	public final PlayerManager playerManager = new PlayerManager(this);
	public final ShopManager shopManager = new ShopManager(this);
	public final PropertyCommand propertyCommand = new PropertyCommand(this);
	public final RankCommand rankCommand = new RankCommand(this);
	
	//admins commands
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
	public final NaxcraftSuperCommand superCommand = new NaxcraftSuperCommand(this);
	public final NaxcraftSpawnMobCommand spawnMobCommand = new NaxcraftSpawnMobCommand(this);
	public final NaxcraftWarpgate warpgateCommand = new NaxcraftWarpgate(this);
	public final NaxcraftStealthCommand stealthCommand = new NaxcraftStealthCommand(this);
	public final NaxcraftStrikeCommand strikeCommand = new NaxcraftStrikeCommand(this);
	public final NaxcraftWeatherCommand weatherCommand = new NaxcraftWeatherCommand(this);
	public final MobCommand mobCommand = new MobCommand(this);
	public final JoinCommand joinCommand = new JoinCommand(this);
	
	//admin area commands
	public final NaxcraftAreaCommand areaCommand = new NaxcraftAreaCommand(this);
	public final NaxcraftGroupCommand groupCommand = new NaxcraftGroupCommand(this);
	
	//clan commands
	public final NaxcraftClanCommand clanCommand = new NaxcraftClanCommand(this);
	
	//public commands
	protected final NaxcraftHelpCommand helpCommand = new NaxcraftHelpCommand(this);
	protected final NaxcraftWhoCommand whoCommand = new NaxcraftWhoCommand(this);
	protected final NaxcraftColorCommand colorCommand = new NaxcraftColorCommand(this);
	protected final NaxcraftHomeCommand homeCommand = new NaxcraftHomeCommand(this);
	public final NaxcraftChatCommand chatCommand = new NaxcraftChatCommand(this);
	
	public HashMap<String, Integer> warpingPlayers = new HashMap<String, Integer>();
	
	//special values
	public Map<Player, Boolean> destroyDrops = new HashMap<Player, Boolean>();
	public Map<Player, Boolean> frozenPlayers = new HashMap<Player, Boolean>();
	
	public Map<Player, Location> position1 = new HashMap<Player, Location>();
	public Map<Player, Location> position2 = new HashMap<Player, Location>();
	
	public String motd = "Welcome to Naxville!"; //gets renamed in onEnable
	public Map<String, String> nickNames = new HashMap<String, String>();
	public Map<String, Integer> regenValues = new HashMap<String, Integer>();
	public boolean stopFire = false;
	
	public final Logger log = Logger.getLogger("Minecraft.Naxcraft");
	
	//files
	public final String filePath = "plugins/Naxcraft/";
	
	public static final String OLD_NAXVILLE = "old_naxville";
	
	//private String nickNameFile = "UserData.txt";
	public String warpFile = "Warps.txt";
	
	//chat colors
	public static ChatColor[] COLORS = {
		ChatColor.BLACK, //Admin color
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
		ChatColor.LIGHT_PURPLE}; //Admin color
	
	//constants
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
	
	public void onDisable() {
		System.out.println("Naxcaft plugin successfully disabled.");
	}
	
	public void onEnable() {
		PluginManager pm = getServer().getPluginManager();
		
		//entity listeners
		pm.registerEvent(Type.ENTITY_DAMAGE, this.entityListener, Event.Priority.Normal, this);
		pm.registerEvent(Type.ENTITY_COMBUST, this.entityListener, Event.Priority.Normal, this);
		pm.registerEvent(Type.ENTITY_DEATH, this.entityListener, Event.Priority.Normal, this);
		pm.registerEvent(Type.ENTITY_EXPLODE, this.entityListener, Event.Priority.Normal, this);
		pm.registerEvent(Type.ENTITY_TARGET, this.entityListener, Event.Priority.Normal, this);
		
		//block listeners
		pm.registerEvent(Type.BLOCK_IGNITE, this.blockListener, Event.Priority.Lowest, this);
		pm.registerEvent(Type.BLOCK_PLACE, this.blockListener, Event.Priority.Lowest, this);
		pm.registerEvent(Type.BLOCK_BREAK, this.blockListener, Event.Priority.Lowest, this);
		pm.registerEvent(Type.BLOCK_DAMAGE, this.blockListener, Event.Priority.Lowest, this);
		pm.registerEvent(Type.BLOCK_PHYSICS, this.blockListener, Event.Priority.Lowest, this);
		pm.registerEvent(Type.SIGN_CHANGE, this.blockListener, Event.Priority.Normal, this);
		
		//player listeners
		pm.registerEvent(Type.PLAYER_JOIN, this.playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_QUIT, this.playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_RESPAWN, this.playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_DROP_ITEM, this.playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_TELEPORT, this.playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_MOVE, this.playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_CHAT, this.playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_INTERACT, this.playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_PICKUP_ITEM, this.playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_KICK, this.playerListener, Event.Priority.Normal, this);
		
		PluginDescriptionFile pdfFile = this.getDescription();
		this.motd = Naxcraft.COMMAND_COLOR + " " + pdfFile.getName() + " plugin version " + pdfFile.getVersion() + " is on!";
		
		loadFiles();
		
		if (this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new NaxcraftRegenRunnable(this), 0, 20) == -1)
			this.log.log(Level.SEVERE, "Naxcraft Startup: Error starting health regen.");
		
		//if(!loadNickNames()) this.log.log(Level.SEVERE, "Naxcraft Startup: Error loading nick names.");
		
		log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is ready to rock!");
		
	}
	
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args){
		String commandName = command.getName();
		
		if(commandName.equalsIgnoreCase("transcend")){
			return this.superCommand.transcend(sender, args);
		}
		
		//clan commands
		if (commandName.equalsIgnoreCase("clan")){
			return this.clanCommand.runClanCommand(sender, args);
		}
		
		if(commandName.equalsIgnoreCase("ranks")){
			sender.sendMessage(Naxcraft.MSG_COLOR + "----");
			sender.sendMessage(Naxcraft.COMMAND_COLOR + "/ranks command:");
			
			for(PlayerRank rank : PlayerRank.getAllRanks())
			{
				sender.sendMessage(rank.getPrefix() + rank.getName());
			}
			
			return true;
		}
		
		//floating commands
		if(commandName.equalsIgnoreCase("god")){
			return this.godCommand.runGodCommand(sender, args);
		}
		
		if(commandName.equalsIgnoreCase("spawnmob")){
			return this.spawnMobCommand.runSpawnMobCommand(sender, args);
		}
		
		if(commandName.equalsIgnoreCase("super")){
			return this.superCommand.runSuperCommand(sender, args);
		}
		
		if(commandName.equalsIgnoreCase("give")){
			return this.giveCommand.runGiveCommand(sender, args);
		}
		
		if(commandName.equalsIgnoreCase("modkit")){
			return this.modkitCommand.runModkitCommand(sender, args);
		}
		if(commandName.equalsIgnoreCase("time")){
			return this.timeCommand.runTimeCommand(sender, args);
		}
		
		if(commandName.equalsIgnoreCase("drop")){
			return this.dropCommand.runDropCommand(sender, args);
		}
		
		if(commandName.equalsIgnoreCase("motd")){
			return this.motdCommand.runMotdCommand(sender, args);
		}
		
		if(commandName.equalsIgnoreCase("freeze")){
			return this.freezeCommand.runFreezeCommand(sender, args);
		}
		
		if(commandName.equalsIgnoreCase("wg") || commandName.equalsIgnoreCase("warpgate")){
			return this.warpgateCommand.runCommand(sender, args);
		}
		
		if(commandName.equalsIgnoreCase("tp")){
			return this.tpCommand.runTpCommand(sender, args);
		}
		
		if(commandName.equalsIgnoreCase("yes") || commandName.equalsIgnoreCase("no")){
			return this.tpCommand.getPermission(sender, commandName);
		}
		
		if(commandName.equalsIgnoreCase("tphere")) return this.tphereCommand.runTphereCommand(sender, args);
		
		if(commandName.equalsIgnoreCase("tpthere")) return this.tphereCommand.runTpThereCommand(sender, args);
		
		if(commandName.equalsIgnoreCase("stealth")) return this.stealthCommand.runCommand(sender, args);
		
		if(commandName.equalsIgnoreCase("strike")) return this.strikeCommand.runCommand(sender, args);
		
		if(commandName.equalsIgnoreCase("weather")) return this.weatherCommand.runCommand(sender, args);
		
		if(commandName.equalsIgnoreCase("promote")) return rankCommand.runCommand((Player)sender, true, args);
		
		if(commandName.equalsIgnoreCase("demote")) return rankCommand.runCommand((Player)sender, false, args);
		
		if(commandName.equalsIgnoreCase("join")) { joinCommand.runJoinCommand((Player)sender); return true; }
		
		if(commandName.equalsIgnoreCase("property")) return propertyCommand.runCommand((Player)sender, args);
		
		if(commandName.equalsIgnoreCase("bc")) return propertyCommand.runCommand((Player)sender, new String[] {"buy", "chunk"});
		
		if(commandName.equalsIgnoreCase("borders")) return propertyCommand.runCommand((Player)sender, new String[] {"borders"});
		
		if(commandName.equalsIgnoreCase("noborders")) return propertyCommand.runCommand((Player)sender, new String[] {"noborders"});
		
		if(commandName.equalsIgnoreCase("bf")) 
		{
			if(args.length == 1)
			{
				return propertyCommand.runCommand((Player)sender, new String[] {"buy", "flag", args[0]});
			}
			else
			{
				return autoAreaManager.flagsCommand((Player) sender, new String[] {});
			}
		}
		
		if(commandName.equalsIgnoreCase("flags")) return autoAreaManager.flagsCommand((Player) sender, args);
		
		if(commandName.equalsIgnoreCase("help")){
			return this.helpCommand.runHelpCommand(sender, args);
		}
		
		if(commandName.equalsIgnoreCase("who")){
			return this.whoCommand.runWhoCommand(sender, args);
		}
		
		if((commandName.equalsIgnoreCase("color"))||(commandName.equalsIgnoreCase("colour"))){
			return this.colorCommand.runColorCommand(sender, args);
		}
		
		if(commandName.equalsIgnoreCase("home")){
			return this.homeCommand.runHomeCommand(sender, args);
		}
		
		if(commandName.equalsIgnoreCase("sethome")) return homeCommand.runSetHomeCommand((Player)sender);
		
		if(commandName.equalsIgnoreCase("spawn")) return homeCommand.runSpawnCommand((Player)sender);
		
		if (commandName.equalsIgnoreCase("list")){
			return this.chatCommand.runChatCommand(sender, commandName, args);
		}
		
		if (commandName.equalsIgnoreCase("npcs")){
			return this.chatCommand.runChatCommand(sender, commandName, args);
		}

		if(commandName.equalsIgnoreCase("kill")){
			return this.killCommand.runKillCommand(sender, args);
		}
		
		if(commandName.equalsIgnoreCase("areainfo"))
		{
			sender.sendMessage(Naxcraft.COMMAND_COLOR + "Area information:");
			groupCommand.getGroupInfo((Player)sender);
			return true;
		}
		
		if(commandName.equalsIgnoreCase("mob"))
		{
			return mobCommand.runCommand((Player)sender, args);
		}
		
		//chat commands
		if (commandName.equalsIgnoreCase("g") || commandName.equalsIgnoreCase("l") || commandName.equalsIgnoreCase("c")
				|| commandName.equalsIgnoreCase("m") || commandName.equalsIgnoreCase("msg") || commandName.equalsIgnoreCase("r")){
			return this.chatCommand.runChatCommand(sender, commandName, args);
		}
		return false;
	}
	
	public String getNickName(Player player) 
	{
		return playerManager.getDisplayName(player);
	}
	
	public String getNickName(String name)
	{
		return playerManager.getDisplayName(name);
	}
	
	public String getWorldName(World world)
	{		
		String name = "";
		int i = 0;
		
		for(String piece : world.getName().replace("_nether", "").split("_"))
		{
			if(i > 0) name += " ";
			
			String start = piece.charAt(0) + "";
			String rest = piece.substring(1);
			
			name += start.toUpperCase() + rest;
			i++;
		}
		
		
		return name;
	}
	
	public String getWorldName(World world, boolean x)
	{
		String name = getWorldName(world);
		ChatColor c = WORLD_COLOR;
		
		if(!x)
		{
			return name;
		}
		else
		{
			if(world.getName().endsWith("_nether"))
			{
				c = NETHER_COLOR;
			}
			
			return c + name + Naxcraft.MSG_COLOR;
		}
	}
	
	private void loadFiles()
	{
		getServer().createWorld("naxville", Environment.NORMAL);
		getServer().createWorld("old_naxville", Environment.NORMAL);
		getServer().createWorld("galingale", Environment.NORMAL);
		
		if (!areaCommand.loadAreas()) this.log.log(Level.SEVERE, "Naxcraft Startup: Error loading areas.");
		
		if (!groupCommand.loadGroups()) this.log.log(Level.SEVERE, "Naxcraft Startup: Error loading Area Groups.");
		
		if (!clanCommand.loadClans()) this.log.log(Level.SEVERE, "Naxcraft Startup: Error loading Clans.");
		
		if (!shopManager.loadShops()) this.log.log(Level.SEVERE, "Naxcraft Startup: Error loading shops.");
		
		if (!warpgateCommand.loadWarpGates()) this.log.log(Level.SEVERE, "Naxcraft Startup: Error loading warpgates.");
		
		atmManager.loadAtms();
		autoAreaManager.loadAutoAreas();
		playerManager.loadPlayerData();
	}
}
