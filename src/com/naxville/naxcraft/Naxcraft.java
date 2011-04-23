package com.naxville.naxcraft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

import com.naxville.bukkit.permissions.*;
import com.naxville.naxcraft.admin.NaxcraftDropCommand;
import com.naxville.naxcraft.admin.NaxcraftFireCommand;
import com.naxville.naxcraft.admin.NaxcraftFreezeCommand;
import com.naxville.naxcraft.admin.NaxcraftGiveCommand;
import com.naxville.naxcraft.admin.NaxcraftGodCommand;
import com.naxville.naxcraft.admin.NaxcraftKillCommand;
import com.naxville.naxcraft.admin.NaxcraftModkitCommand;
import com.naxville.naxcraft.admin.NaxcraftMotdCommand;
import com.naxville.naxcraft.admin.NaxcraftSpawnCommand;
import com.naxville.naxcraft.admin.NaxcraftStealthCommand;
import com.naxville.naxcraft.admin.NaxcraftStrikeCommand;
import com.naxville.naxcraft.admin.NaxcraftSuperCommand;
import com.naxville.naxcraft.admin.NaxcraftTimeCommand;
import com.naxville.naxcraft.admin.NaxcraftWarpgate;
import com.naxville.naxcraft.admin.NaxcraftWeatherCommand;
import com.naxville.naxcraft.admin.NaxcraftWmodCommand;
import com.naxville.naxcraft.land.NaxcraftAreaCommand;
import com.naxville.naxcraft.land.NaxcraftAutoarea;
import com.naxville.naxcraft.land.NaxcraftGroupCommand;
import com.naxville.naxcraft.listeners.NaxcraftBlockListener;
import com.naxville.naxcraft.listeners.NaxcraftEntityListener;
import com.naxville.naxcraft.listeners.NaxcraftPlayerListener;
import com.naxville.naxcraft.listeners.NaxcraftWorldListener;
import com.naxville.naxcraft.npcs.NaxcraftNpcCommand;
import com.naxville.naxcraft.rpg.NaxRpg;
import com.naxville.naxcraft.rpg.listeners.NaxNpcListener;
import com.naxville.naxcraft.rpg.listeners.NaxRpgBlockListener;
import com.naxville.naxcraft.rpg.listeners.NaxRpgPlayerListener;


public class Naxcraft extends JavaPlugin {
	//listeners
	protected final NaxcraftEntityListener entityListener = new NaxcraftEntityListener(this);
	protected final NaxcraftPlayerListener playerListener = new NaxcraftPlayerListener(this);
	public final NaxcraftBlockListener blockListener = new NaxcraftBlockListener(this);
	public final NaxcraftWorldListener worldListener = new NaxcraftWorldListener(this);
	
	//naxrpg listeners
	protected final NaxNpcListener npcListener = new NaxNpcListener(this);
	protected final NaxRpgPlayerListener rpgPlayerListener = new NaxRpgPlayerListener(this);
	protected final NaxRpgBlockListener rpgBlockListener = new NaxRpgBlockListener(this);

	//autoareas
	public final NaxcraftAutoarea autoarea = new NaxcraftAutoarea(this);
	
	//admins commands
	public final NaxcraftGodCommand godCommand = new NaxcraftGodCommand(this);
	protected final NaxcraftMotdCommand motdCommand = new NaxcraftMotdCommand(this);
	protected final NaxcraftFireCommand fireCommand = new NaxcraftFireCommand(this);
	public final NaxcraftTpCommand tpCommand = new NaxcraftTpCommand(this);
	protected final NaxcraftTphereCommand tphereCommand = new NaxcraftTphereCommand(this);
	protected final NaxcraftModkitCommand modkitCommand = new NaxcraftModkitCommand(this);
	protected final NaxcraftDropCommand dropCommand = new NaxcraftDropCommand(this);
	protected final NaxcraftFreezeCommand freezeCommand = new NaxcraftFreezeCommand(this);
	protected final NaxcraftKillCommand killCommand = new NaxcraftKillCommand(this);
	protected final NaxcraftWmodCommand wmodCommand = new NaxcraftWmodCommand(this);
	protected final NaxcraftGiveCommand giveCommand = new NaxcraftGiveCommand(this);
	protected final NaxcraftTimeCommand timeCommand = new NaxcraftTimeCommand(this);
	public final NaxcraftSuperCommand superCommand = new NaxcraftSuperCommand(this);
	public final NaxcraftNpcCommand npcCommand = new NaxcraftNpcCommand(this);
	public final NaxcraftSpawnCommand spawnCommand = new NaxcraftSpawnCommand(this);
	public final NaxcraftWarpgate warpgateCommand = new NaxcraftWarpgate(this);
	public final NaxcraftStealthCommand stealthCommand = new NaxcraftStealthCommand(this);
	public final NaxcraftStrikeCommand strikeCommand = new NaxcraftStrikeCommand(this);
	public final NaxcraftWeatherCommand weatherCommand = new NaxcraftWeatherCommand(this);
	
	//permission commands
	public final PermissionsControl control = new PermissionsControl();
	protected final PermissionCommand permissionCommand = new PermissionCommand(this, control);
	
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
	public final NaxcraftWarpCommand warpCommand = new NaxcraftWarpCommand(this);
	
	public HashMap<String, Integer> warpingPlayers = new HashMap<String, Integer>();
	
	//special values
	public Map<Player, String> deathMessage = new HashMap<Player, String>();
	public Map<Player, Boolean> destroyDrops = new HashMap<Player, Boolean>();
	public Map<Player, Boolean> frozenPlayers = new HashMap<Player, Boolean>();
	
	public Map<Player, Location> position1 = new HashMap<Player, Location>();
	public Map<Player, Location> position2 = new HashMap<Player, Location>();
	
	public String motd = "Welcome to Naxville! (Under construction)"; //gets renamed in onEnable
	public Map<String, String> nickNames = new HashMap<String, String>();
	public Map<String, Integer> regenValues = new HashMap<String, Integer>();
	public boolean stopFire = false;
	
	public final Logger log = Logger.getLogger("Minecraft.Naxcraft");
	
	public String rpgWorld = "rpg"; // world name
	public String rpgWorldName = "Galingale";
	
	//read from files
	public List<String> areas = new ArrayList<String>();
	
	//files
	public final String filePath = "plugins/Naxcraft/";
	
	private String nickNameFile = "UserData.txt";
	private String areaFile = filePath + "AreaData.txt";
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
	public static ChatColor CLAN_COLOR = ChatColor.GOLD;
	public static ChatColor NPC_COLOR = ChatColor.AQUA;
	
	
	//aww yeah
	public final NaxRpg naxrpg = new NaxRpg(this);
	
	public void onDisable() {
		System.out.println("Naxcaft plugin successfully disabled.");
	}
	
	public void onEnable() {
		PluginManager pm = getServer().getPluginManager();
		
		//world listeners
		pm.registerEvent(Type.CHUNK_UNLOAD, this.worldListener, Event.Priority.Normal, this);
		pm.registerEvent(Type.CHUNK_LOAD, this.worldListener, Event.Priority.Normal, this);
		//pm.registerEvent(Type.CHUNK_GENERATION, this.worldListener, Event.Priority.Normal, this);
		
		//entity listeners
		pm.registerEvent(Type.ENTITY_DAMAGE, this.entityListener, Event.Priority.Normal, this);
		pm.registerEvent(Type.ENTITY_COMBUST, this.entityListener, Event.Priority.Normal, this);
		pm.registerEvent(Type.ENTITY_DEATH, this.entityListener, Event.Priority.Normal, this);
		pm.registerEvent(Type.ENTITY_EXPLODE, this.entityListener, Event.Priority.Normal, this);
		pm.registerEvent(Type.ENTITY_TARGET, this.entityListener, Event.Priority.Normal, this);
		
		//block listeners
		pm.registerEvent(Type.BLOCK_IGNITE, this.blockListener, Event.Priority.Lowest, this);
		pm.registerEvent(Type.BLOCK_BURN, this.blockListener, Event.Priority.Lowest, this);
		pm.registerEvent(Type.BLOCK_PLACE, this.blockListener, Event.Priority.Lowest, this);
		pm.registerEvent(Type.BLOCK_BREAK, this.blockListener, Event.Priority.Lowest, this);
		pm.registerEvent(Type.BLOCK_DAMAGE, this.blockListener, Event.Priority.Lowest, this);
		pm.registerEvent(Type.BLOCK_PHYSICS, this.blockListener, Event.Priority.Lowest, this);
		
		//player listeners
		pm.registerEvent(Type.PLAYER_JOIN, this.playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_QUIT, this.playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_RESPAWN, this.playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_DROP_ITEM, this.playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_TELEPORT, this.playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_MOVE, this.playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_CHAT, this.playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_INTERACT, this.playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_ANIMATION, this.playerListener, Event.Priority.Normal, this);
		
		//naxrpg listeners
		pm.registerEvent(Type.PLAYER_TELEPORT, this.rpgPlayerListener, Event.Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_JOIN, this.rpgPlayerListener, Event.Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_INTERACT, this.rpgPlayerListener, Event.Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_MOVE, this.rpgPlayerListener, Event.Priority.Normal, this);
		pm.registerEvent(Type.INVENTORY_OPEN, this.rpgPlayerListener, Event.Priority.Normal, this);
		
		pm.registerEvent(Type.SIGN_CHANGE, this.rpgBlockListener, Event.Priority.Normal, this);
		
		pm.registerEvent(Type.BLOCK_BREAK, this.rpgBlockListener, Event.Priority.Normal, this);
		pm.registerEvent(Type.BLOCK_DAMAGE, this.rpgBlockListener, Event.Priority.Normal, this);
		
		pm.registerEvent(Type.ENTITY_DAMAGE, this.npcListener, Event.Priority.Normal, this);
		pm.registerEvent(Type.ENTITY_TARGET, this.npcListener, Event.Priority.Normal, this);
		
		PluginDescriptionFile pdfFile = this.getDescription();
		this.motd = Naxcraft.COMMAND_COLOR + " " + pdfFile.getName() + " plugin version " + pdfFile.getVersion() + " is on!";
		
		if(!loadAreas()) this.log.log(Level.SEVERE, "Naxcraft Startup: Error loading old area format.");
		
		if (!areaCommand.loadAreas()) this.log.log(Level.SEVERE, "Naxcraft Startup: Error loading areas.");
		
		if (!warpCommand.loadWarps()) this.log.log(Level.SEVERE, "Naxcraft Startup: Error loading warps.");
		
		if (!groupCommand.loadGroups()) this.log.log(Level.SEVERE, "Naxcraft Startup: Error loading Area Groups.");
		
		if (!npcCommand.loadNpcs()) this.log.log(Level.SEVERE, "Naxcraft Startup: Error loading NPCs.");
		
		if (!clanCommand.loadClans()) this.log.log(Level.SEVERE, "Naxcraft Startup: Error loading Clans.");
		
		if (!naxrpg.shops.loadShops()) this.log.log(Level.SEVERE, "Naxcraft Startup: Error loading shops.");
		
		if (!warpgateCommand.loadWarpGates()) this.log.log(Level.SEVERE, "Naxcraft Startup: Error loading warpgates.");
		
		if (this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new NaxcraftRegenRunnable(this), 0, 20) == -1)
			this.log.log(Level.SEVERE, "Naxcraft Startup: Error starting health regen.");
		
		if(!loadNickNames()) this.log.log(Level.SEVERE, "Naxcraft Startup: Error loading nick names.");
		
		System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is ready to rock!");
		
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
		
		//permission commands
		if (commandName.equalsIgnoreCase("bless") || commandName.equalsIgnoreCase("grant")) {
			return this.permissionCommand.runPermissionCommand(sender, commandName, args);
		}
		
		if (commandName.equalsIgnoreCase("promote") || commandName.equalsIgnoreCase("demote")) {
			return this.permissionCommand.runPermissionCommand(sender, commandName, args);
		}
		
		if (commandName.equalsIgnoreCase("savepermission")) {
			return this.permissionCommand.runPermissionCommand(sender, commandName, args);
		}
		
		if(commandName.equalsIgnoreCase("ranks")){
			return this.getRanks(sender);
		}
		
		if(commandName.equalsIgnoreCase("flags")){
			return this.groupCommand.getFlags(sender);
		}
		
		//floating commands
		if(commandName.equalsIgnoreCase("god")){
			return this.godCommand.runGodCommand(sender, args);
		}
		
		if(commandName.equalsIgnoreCase("spawn")){
			return this.spawnCommand.runSpawnCommand(sender, args);
		}
		
		if(commandName.equalsIgnoreCase("npc")){
			return this.npcCommand.runNpcCommand(sender, args);
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
		
		if(commandName.equalsIgnoreCase("area")){
			return this.areaCommand.runAreaCommand(sender, args);
		}
		if(commandName.equalsIgnoreCase("areas")){
			this.areaCommand.getOwnedAreas(((Player)sender).getName());
			return true;
		}
		
		if(commandName.equalsIgnoreCase("group")){
			return this.groupCommand.runGroupCommand(sender, args);
		}
		
		if(commandName.equalsIgnoreCase("drop")){
			return this.dropCommand.runDropCommand(sender, args);
		}
		
		if(commandName.equalsIgnoreCase("motd")){
			return this.motdCommand.runMotdCommand(sender, args);
		}
		
		if(commandName.equalsIgnoreCase("fire")){
			return this.fireCommand.runFireCommand(sender, args);
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
		
		if(commandName.equalsIgnoreCase("wmod")){
			return this.wmodCommand.runWmodCommand(sender, args);
		}
		
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
		
		if (commandName.equalsIgnoreCase("list")){
			return this.chatCommand.runChatCommand(sender, commandName, args);
		}
		
		if (commandName.equalsIgnoreCase("npcs")){
			return this.chatCommand.runChatCommand(sender, commandName, args);
		}

		if(commandName.equalsIgnoreCase("kill")){
			return this.killCommand.runKillCommand(sender, args);
		}
		
		if((commandName.equalsIgnoreCase("warp") || commandName.equalsIgnoreCase("shrine"))){
			if(commandName.equalsIgnoreCase("shrine")) sender.sendMessage(Naxcraft.ADMIN_COLOR + "/shrine is changing to /warp soon. Get used to /warp.");
			return this.warpCommand.runWarpCommand(sender, args);
		}
		
		if(commandName.equalsIgnoreCase("done")){
			return this.blockListener.removeScaffolding(sender);
		}
		
		//chat commands
		if (commandName.equalsIgnoreCase("g") || commandName.equalsIgnoreCase("l") || commandName.equalsIgnoreCase("c")
				|| commandName.equalsIgnoreCase("n") || commandName.equalsIgnoreCase("m") || commandName.equalsIgnoreCase("msg") || commandName.equalsIgnoreCase("r")){
			return this.chatCommand.runChatCommand(sender, commandName, args);
		}
		return false;
	}
	
	public String arrayToString(String[] array){
		StringBuffer buffer = new StringBuffer();
		String seperator = " ";
		int newWords = 0;
		if(array.length > 0){
			for(int i = 0; i < array.length; i++){
				if(!array[i].equals("")){
					if(newWords != 0) buffer.append(seperator);
					buffer.append(array[i]);
					newWords++;
				}
			}
		}
		
		return buffer.toString();
	}
	

	public String arrayToString(String[] array, String seperator){
		if(seperator == null) return arrayToString(array);
		
		StringBuffer buffer = new StringBuffer();
		int newWords = 0;
		if(array.length > 0){
			for(int i = 0; i < array.length; i++){
				if(!array[i].equals("")){
					if(newWords != 0) buffer.append(seperator);
					buffer.append(array[i]);
					newWords++;
				}
			}
		}
		
		return buffer.toString();
	}
	
	public String getNickName(String name){
		String rank = control.getGroup(name.toLowerCase(), true).getName();
		String prefix = "";
		if(rank != null){
			if(rank.equalsIgnoreCase("member")){
				prefix = "+";
				
			} else if (rank.equalsIgnoreCase("veteran")){
				prefix = "*";
				
			} else if (rank.equalsIgnoreCase("patron")){
				prefix = "$";
				
			} else if (rank.equalsIgnoreCase("moderator")){
				prefix = "#";
				
			} else if (rank.equalsIgnoreCase("admin")){
				prefix = "@";
				
			} 	
		
		}
		if(this.nickNames.containsKey(name.toLowerCase())){
			return Naxcraft.MSG_COLOR + prefix + this.nickNames.get(name.toLowerCase());
		}
		
		return (Naxcraft.MSG_COLOR + prefix + Naxcraft.DEFAULT_COLOR + name + Naxcraft.DEFAULT_COLOR);
	}
	
	public boolean getRanks(CommandSender sender){
		sender.sendMessage(Naxcraft.COMMAND_COLOR + "Ranks: " +  
						   "Noob" + Naxcraft.COMMAND_COLOR + ", " + Naxcraft.DEFAULT_COLOR +
						   "+" + Naxcraft.COMMAND_COLOR + "Member, " + Naxcraft.DEFAULT_COLOR +
						   "*" + Naxcraft.COMMAND_COLOR + "Veteran, " + Naxcraft.DEFAULT_COLOR +
						   "$" + Naxcraft.COMMAND_COLOR + "Patron, " + Naxcraft.DEFAULT_COLOR +
						   "#" + Naxcraft.COMMAND_COLOR + "Moderator, " + Naxcraft.DEFAULT_COLOR +
						   "@" + Naxcraft.ADMIN_COLOR + "Admin");	
		return true;
	}
	
	public String getWorldName(World world){
		String name = "";
		
		if(world.getName().equals("world")){
			name = "Naxville";
		} else {
			name = "Galingale";
		}
		
		return name;
	}
	
	public boolean loadAreas(){
		
		File areasFile = new File(this.areaFile);
	    
		if(areasFile.exists()){
				
			try {
				BufferedReader input =  new BufferedReader(new FileReader(areasFile));
				
				try{
					String line = null; 
					while (( line = input.readLine()) != null){
						areas.add(line);
					}
				} catch (Exception e){
					e.printStackTrace();
				}
				input.close();
				
			} catch (IOException e){
			  e.printStackTrace();
			  return false;
			}
		}
		
		return true;
	}
	
	public boolean writeAreas(){
		File areasFile = new File(this.areaFile);
	    
		try {
			BufferedWriter output = new BufferedWriter(new FileWriter(areasFile));
			
			try{
				for(String area : areas){
					output.append(area);
					output.append("\r\n");
				}
			} catch (Exception e){
				e.printStackTrace();
			}
			output.close();
			
		} catch (IOException e){
		  e.printStackTrace();
		  return false;
		}
		
		return true;
	}
	
	private boolean loadNickNames(){
		File nickNamesFile = new File(this.nickNameFile);
		
		if (nickNamesFile.exists())
		{
			try
			{
				BufferedReader input = new BufferedReader(new FileReader(nickNamesFile));
				String line;
				
				try{
				while((line = input.readLine())!=null)
					{	
						String name = line.substring(0, (line.indexOf(':')));
						String nick = line.substring((line.indexOf(':')+1));					
						
						this.nickNames.put(name.toLowerCase(), nick);
					}
				} catch (Exception e){
					e.printStackTrace();
				}
				input.close();
			}
			catch (Exception e)
			{
				System.out.println("Fatal Error: " + e);
				e.printStackTrace();
				return false;
			}
		}
		
		return true;
	}
	
	public boolean saveNickNames(){
		File nickNamesFile = new File(this.nickNameFile);
		
		try
		{
			BufferedWriter output = new BufferedWriter(new FileWriter(nickNamesFile));
			
			try{
				for(String key : (nickNames.keySet())){
					output.append(key + ":" + nickNames.get(key) + "\r\n");
				}
			} catch (Exception e){
				e.printStackTrace();
			}
			
			output.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}	
		
		return true;
	}
}
