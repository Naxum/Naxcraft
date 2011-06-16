package com.naxville.naxcraft;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.naxville.naxcraft.admin.NaxcraftConfiguration;
import com.naxville.naxcraft.player.PlayerManager.PlayerRank;

public class NaxcraftClanCommand {

	public static Naxcraft plugin;
	
	//config file
	private NaxcraftConfiguration config;
	
	//clan help messages
	private final String createHelp = "/clan create <Name> - Creates a new clan";
	private final String disbandHelp = "/clan disband <Name> - Disband an existing clan";
	private final String whoHelp = "/clan who <Player> - Shows name of clan player belongs to";
	private final String addHelp = "/clan add <Clan> <Player> - Adds player to clan";
	private final String removeHelp = "/clan remove <Clan> <Player> - Remove player from clan";
	private final String motdHelp = "/clan motd <message> view the clan motd";
	private final String setleaderHelp = "/clan setleader <Clan> <Player> - Promotes player to clan leader";
	private final String infoHelp = "/clan info [Clan] - View information about your current clan or another clan";
	private final String saveHelp = "/clan save - Forces save of all clan data";
	
	//collections
	private HashMap<String, NaxcraftClan>clans;
	
	public NaxcraftClanCommand(Naxcraft instance){
		plugin = instance;
		
		clans = new HashMap<String, NaxcraftClan>();
		
		loadClanFile();
	}
	
	private void loadClanFile(){
		File file = new File(plugin.filePath);
		try {
		if (!file.exists()){
			file.mkdir();
		}
		file = new File(plugin.filePath + "ClanData.yml");
		if (!file.exists()){
			file.createNewFile();
			initalizeFile();
		}
		} catch (IOException Ex) {
			System.out.println("Shit, problem creating new ClanData file");
			return;
		}
		
		config = new NaxcraftConfiguration(file);
		config.load();
	}
	
	private void initalizeFile(){
		File file = new File(plugin.filePath + "ClanData.yml");
		try {
			BufferedWriter output = new BufferedWriter(new FileWriter(file));
			output.write("clans:");
			output.close();
		} catch (Exception Ex) {
			System.out.println("Error initalizing Clans file.  Clans cant save :(");
		}
	}
	
	protected boolean loadClans(){
		try {
			List<String>clanList = config.getKeys("clans");
			NaxcraftClan clan;
			for (String string : clanList){
				clan = new NaxcraftClan(plugin, string);
				List<String> members = config.getStringList("clans." + string + ".members", null);
				String leader = config.getString("clans." + string + ".leader");
				String motd = config.getString("clans." + string + ".motd");
				
				if (members != null)
					clan.loadMembers(members);
				if (leader != null)
					clan.loadLeader(leader);
				if (motd != null)
					clan.loadMOTD(motd);
				this.clans.put(string.toLowerCase(), clan);
			}
			
		} catch (Exception ex) {
			initalizeFile();
			return false;
		} 
		return true;
	}
	
	protected void saveClan(NaxcraftClan clan){
		String key = clan.getName();
		config.setProperty("clans." + key + ".members", clan.saveMembers());
		config.setProperty("clans." + key + ".leader", clan.saveLeader());
		config.setProperty("clans." + key + ".motd", clan.saveMOTD());
		config.save();
		return;
	}
	
	protected void removeClan(NaxcraftClan clan){
		config.removeProperty("clans." + clan.getName());
		config.save();
	}
	
	private String[] getTrimmedArray(String[] args, int startIndex) {
		if (args.length >= startIndex) {
			String[] ret = new String[args.length - startIndex];
			int count = 0;
			for (int i = startIndex; i < args.length; i++) {
				ret[count] = args[i];
				count++;
			}
			return ret;
		} else {
			return args;
		}
	}
	
	private String getPlayerListToString(List<Player> players){
		StringBuilder message = new StringBuilder();
		for (Player player : players) {
			message.append(player.getDisplayName());
			message.append(" ");
		}
		return message.toString();
	}
	
	public NaxcraftClan getPlayerClan(Player player){
		for (NaxcraftClan clan : clans.values()){
			if (clan.isMember(player)){
				return clan;
			}
		}
		return null;
	}
	
	protected NaxcraftClan getPlayerClan(String player){
		for (NaxcraftClan clan : clans.values()) {
			if (clan.isMember(player.toLowerCase())){
				return clan;
			}
		}
		return null;
	}
	
	public NaxcraftClan getClan(String clan){
		if (clans.containsKey(clan.toLowerCase())) {
			return clans.get(clan.toLowerCase());
		}
		return null;
	}
	
	private boolean canSender(CommandSender sender, String command){
		boolean isConsole = !(sender instanceof Player);
		boolean permission = false;
		boolean isLeader = false;
		
		if (!isConsole){
			Player player = (Player)sender;
			if (!command.contains("")){
			permission = plugin.playerManager.getPlayer(player).rank.getId() >= PlayerRank.MODERATOR.getId();
			}
			else {
				permission = plugin.playerManager.getPlayer(player).rank.getId() >= PlayerRank.MODERATOR.getId();
			}
			NaxcraftClan clan = getPlayerClan(player);
			if (clan != null)
				isLeader = clan.isPlayerLeader(player);
		}
		if (command.equalsIgnoreCase("save")){
			return isConsole || permission;
		} else if (command.equalsIgnoreCase("create")){
			return isConsole || permission;
		} else if (command.equalsIgnoreCase("disband")){
			return isConsole || permission || isLeader;
		} else if (command.equalsIgnoreCase("who")){
			return true;
		} else if (command.equalsIgnoreCase("")){
			return true;
		} else if (command.equalsIgnoreCase("info")){
			return true;
		} else if (command.equalsIgnoreCase("help")){
			return true;
		} else if (command.equalsIgnoreCase("add")){
			return isConsole || permission;
		} else if (command.equalsIgnoreCase("remove")){
			return isConsole || permission || isLeader;
		} else if (command.equalsIgnoreCase("motd")){
			return (!isConsole);
		} else if (command.equalsIgnoreCase("setleader")) {
			return isConsole || permission || isLeader;
		}
		sender.sendMessage(String.format(Naxcraft.PERMISSIONS_FAIL, "/clan " + command));
		return false;
	}

	protected boolean runClanCommand(CommandSender sender, String[] args){
		String command = "";
		if (args.length > 0)
			command = args[0];
		String[] trimmedArgs = getTrimmedArray(args, 1);

		if (canSender(sender, command)){
			if (command.equalsIgnoreCase("")){
				return getInfo(sender, trimmedArgs);
			} else if (command.equalsIgnoreCase("save")){
				return saveAllClans(sender);
			} else if (command.equalsIgnoreCase("info")){
				return getInfo(sender, trimmedArgs);
			} else if (command.equalsIgnoreCase("help")){
				return getHelp(sender, trimmedArgs);
			} else if (command.equalsIgnoreCase("create")){
				return createClan(sender, trimmedArgs);
			} else if (command.equalsIgnoreCase("disband")){
				return disbandClan(sender, trimmedArgs);
			} else if (command.equalsIgnoreCase("add")){
				return addMember(sender, trimmedArgs);
			} else if (command.equalsIgnoreCase("remove")){
				return removeMember(sender, trimmedArgs);
			} else if (command.equalsIgnoreCase("setleader")){
				return setLeader(sender, trimmedArgs);
			} else if (command.equalsIgnoreCase("motd")){
				return setMOTD(sender, trimmedArgs);
			} else if (command.equalsIgnoreCase("who")){
				return getWho(sender, trimmedArgs);
			}
		}
		return false;
	}
	
	protected boolean saveAllClans(CommandSender sender){
		for (NaxcraftClan clan : clans.values()){
			saveClan(clan);
		}
		sender.sendMessage(Naxcraft.COMMAND_COLOR + "Clans saved!");
		return true;
	}
	
	private boolean createClan(CommandSender sender, String[] args){
		if (args.length < 1){
			sender.sendMessage(Naxcraft.ERROR_COLOR + "You must provide a new clan name");
			sender.sendMessage(createHelp);
			return true;
		}
		if (!clans.containsKey(args[0].toLowerCase())){
			NaxcraftClan clan = new NaxcraftClan(plugin, args[0]);
			clan.setMOTD("Welcome to clan " + clan.getName() + "! (/clan motd <text>)");
			
			clans.put(args[0].toLowerCase(), clan);
			sender.sendMessage(Naxcraft.SUCCESS_COLOR + "Clan " + Naxcraft.DEFAULT_COLOR + clan.getName() + Naxcraft.SUCCESS_COLOR + " has been created");
			this.saveClan(clan);
			return true;
		} else { 
			sender.sendMessage(Naxcraft.ERROR_COLOR + "Clan " + Naxcraft.DEFAULT_COLOR + args[0] + Naxcraft.ERROR_COLOR + " already exists.");
			return true;
		}
	}

	private boolean disbandClan(CommandSender sender, String[] args){
		if (args.length < 1){
			sender.sendMessage(Naxcraft.ERROR_COLOR + "You must provide a clan name to disband");
			sender.sendMessage(disbandHelp);
			return true;
		}
		if (clans.containsKey(args[0].toLowerCase())){
			NaxcraftClan clan = clans.get(args[0].toLowerCase());
			if (sender instanceof Player){
				if (!clan.isPlayerLeader((Player)sender) && !sender.isOp()){
					sender.sendMessage(Naxcraft.ERROR_COLOR + "You do not have permission for the disband command");
					return true;
				}
			}
			if (clan.disband()){
				sender.sendMessage(Naxcraft.COMMAND_COLOR + "Clan " + Naxcraft.DEFAULT_COLOR + clan.getName() + Naxcraft.COMMAND_COLOR + " has been disbaned");
				clans.remove(args[0].toLowerCase());
				this.removeClan(clan);
				return true;
			} else {
				sender.sendMessage(Naxcraft.ERROR_COLOR + "Unable to disband " + clan.getName() + ". Thats weird...");
				return true;
			}
		} else {
			sender.sendMessage(Naxcraft.ERROR_COLOR + "No clan named " + Naxcraft.DEFAULT_COLOR + args[0] + Naxcraft.ERROR_COLOR + " exists");
			return true;
		}
	}
	
	private boolean addMember(CommandSender sender, String[] args){
		if (args.length < 2){
			sender.sendMessage(Naxcraft.ERROR_COLOR + "You must provide a clan and player");
			sender.sendMessage(addHelp);
			return true;
		}
		NaxcraftClan clan;
		Player player;
		if (clans.containsKey(args[0].toLowerCase())){
			clan = clans.get(args[0].toLowerCase());
		} else {
			sender.sendMessage(Naxcraft.ERROR_COLOR + "No clan named " + Naxcraft.DEFAULT_COLOR + args[0] + Naxcraft.ERROR_COLOR + " exists");
			return true;
		}
		player = plugin.getServer().getPlayer(args[1]);
		if (player == null){
			sender.sendMessage(Naxcraft.ERROR_COLOR + "No player named "+ Naxcraft.DEFAULT_COLOR + args[1] + Naxcraft.ERROR_COLOR + " is currently online");
			return true;
		}
		if (getPlayerClan(player) != null){
			sender.sendMessage(player.getDisplayName() + Naxcraft.ERROR_COLOR + " is already in clan " + Naxcraft.DEFAULT_COLOR + getPlayerClan(player).getName());
			return true;
		}
		if (clan.addMember(player)){
			saveClan(clan);
			sender.sendMessage(Naxcraft.SUCCESS_COLOR + "Player added to clan successfully.");
			return true;
		}
		return false;
	}

	private boolean removeMember(CommandSender sender, String[] args){
		if (args.length < 2){
			sender.sendMessage(Naxcraft.ERROR_COLOR + "You must provide a clan and player");
			sender.sendMessage(removeHelp);
			return true;
		}
		NaxcraftClan clan;
		Player player;
		
		if (clans.containsKey(args[0].toLowerCase())){
			clan = clans.get(args[0].toLowerCase());
			if (sender instanceof Player){
				if (!clan.isPlayerLeader((Player)sender) && !sender.isOp()){
					sender.sendMessage(Naxcraft.ERROR_COLOR + "You do not have permission for the command setleader command");
					return true;
				}
			}
		} else {
			sender.sendMessage(Naxcraft.ERROR_COLOR + "No clan named " + Naxcraft.DEFAULT_COLOR + args[0] + Naxcraft.ERROR_COLOR + " exists");
			return true;
		}
		player = plugin.getServer().getPlayer(args[1]);
		if (player == null){
			if (clan.isPlayerLeader(args[1])){
				sender.sendMessage(plugin.getNickName(args[1].toLowerCase()) + Naxcraft.COMMAND_COLOR + " is leader of " + Naxcraft.DEFAULT_COLOR + clan.getName());
				sender.sendMessage(Naxcraft.COMMAND_COLOR + "Promote a new leader before removing " + args[1]);
				return true;
			} else if (clan.removeMember(args[1])){
				sender.sendMessage(Naxcraft.COMMAND_COLOR + args[1] + " has been removed from " + clan.getName());
				saveClan(clan);
				return true;
			} else {
				sender.sendMessage(Naxcraft.ERROR_COLOR + args[1] + " is not a member of " + clan.getName());
				return true;
			}
		} else {
			if (clan.isPlayerLeader(player)){
				sender.sendMessage(Naxcraft.COMMAND_COLOR + player.getDisplayName() + " is leader of " + clan.getName());
				sender.sendMessage(Naxcraft.COMMAND_COLOR + "Promote a new leader before removing " + player.getDisplayName());
				return true;
			} else if (clan.removeMember(player)){
				sender.sendMessage(Naxcraft.COMMAND_COLOR + player.getDisplayName() + " has been removed from " + clan.getName());
				saveClan(clan);
				return true;
			} else {
				sender.sendMessage(Naxcraft.ERROR_COLOR + player.getDisplayName() + " is not a member of " + clan.getName());
				return true;
			}
		}
	}

	private boolean setLeader(CommandSender sender, String[] args){
		if (args.length < 2){
			sender.sendMessage(Naxcraft.ERROR_COLOR + "You must provide a clan and player");
			sender.sendMessage(setleaderHelp);
			return true;
		}
		NaxcraftClan clan;
		Player player;
		
		if (clans.containsKey(args[0].toLowerCase())){
			clan = clans.get(args[0].toLowerCase());
			if (sender instanceof Player){
				if (!clan.isPlayerLeader((Player)sender) && !sender.isOp()){
					sender.sendMessage(Naxcraft.ERROR_COLOR + "You do not have permission for the command setleader command");
					return true;
				}
			}
		} else {
			sender.sendMessage(Naxcraft.ERROR_COLOR + "No clan named " + args[0] + " exists");
			return true;
		}
		player = plugin.getServer().getPlayer(args[1]);
		if (player == null){
			sender.sendMessage(Naxcraft.ERROR_COLOR + "No player named " + args[1] + " is currently online");
		} else {
			if (clan != getPlayerClan(player)){
				sender.sendMessage(Naxcraft.ERROR_COLOR + player.getDisplayName() + " is not a member of " + clan.getName());
				return true;
			} else {
				if (clan.setLeader(player)){
					sender.sendMessage(Naxcraft.COMMAND_COLOR + player.getDisplayName() + " is now the leader of " + clan.getName());
					saveClan(clan);
					return true;
				} else {
					sender.sendMessage(Naxcraft.ERROR_COLOR + "Unable to make " + player.getDisplayName() + " leader of " + clan.getName() + ". Thats odd");
					return true;
				}
			}
		}
		return false;
	}

	private boolean setMOTD(CommandSender sender, String[] args){
		if (!(sender instanceof Player)){
			sender.sendMessage(Naxcraft.ERROR_COLOR + "Only clan member may view or change the MOTD");
			return true;
		}
		Player player = (Player)sender;
		NaxcraftClan clan = getPlayerClan(player);
		if (clan == null){
			return true;
		}
		if (args.length == 0){
			sender.sendMessage(clan.getMOTD());
			return true;
		}
		if (clan.isPlayerLeader(player)){
			clan.setMOTD(arrayToString(args));
			saveClan(clan);
			return true;
		}
		return true;
	}
	
	private String arrayToString(String[] array){
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

	private boolean getWho(CommandSender sender, String[] args){
		if (args.length < 1){
			sender.sendMessage(Naxcraft.ERROR_COLOR + "You must specify a player");
			sender.sendMessage(whoHelp);
			return true;
		}
		Player player = plugin.getServer().getPlayer(args[0]);
		if (player == null){
			sender.sendMessage(Naxcraft.ERROR_COLOR + "No player named " + args[0] + " found online");
			return true;
		} else {
			NaxcraftClan clan = getPlayerClan(player);
			if (clan == null){
				sender.sendMessage(Naxcraft.COMMAND_COLOR + player.getDisplayName() + " is not a member of any clans");
				return true;
			} else {
				sender.sendMessage(Naxcraft.COMMAND_COLOR + player.getDisplayName() + " is a member of " + clan.getName());
				return true;
			}
		}
	}

	private boolean getInfo(CommandSender sender, String[] args){
		NaxcraftClan clan;
		if (args.length == 0){
			if (!(sender instanceof Player)){
				sender.sendMessage(Naxcraft.COMMAND_COLOR + "Hey, your not one of us. No clan info for 'joo");
				return true;
			}
			clan = getPlayerClan((Player)sender);
			if (clan == null){
				sender.sendMessage(Naxcraft.ERROR_COLOR + "You are not a member of any clan");
				return true;
			} else {
				sender.sendMessage(Naxcraft.COMMAND_COLOR + "Clan info for " + clan.getName());
				sender.sendMessage(Naxcraft.COMMAND_COLOR + "Online members:");
				sender.sendMessage(Naxcraft.COMMAND_COLOR + getPlayerListToString(clan.getOnlineMembers()));
				return true;
			}
		}
		if (clans.containsKey(args[0].toLowerCase())){
			clan = clans.get(args[0].toLowerCase());
			sender.sendMessage(Naxcraft.COMMAND_COLOR + "Clan info for " + clan.getName());
			sender.sendMessage(Naxcraft.COMMAND_COLOR + "Online members:");
			sender.sendMessage(Naxcraft.COMMAND_COLOR + getPlayerListToString(clan.getOnlineMembers()));
			return true;
		} else {
			sender.sendMessage(Naxcraft.ERROR_COLOR + "No clan named " + args[0] + " exists");
			return true;
		}
	}

	private boolean getHelp(CommandSender sender, String[] args){
		List<String> message = new ArrayList<String>();
		if (canSender(sender, "info"))
			message.add(infoHelp);
		if (canSender(sender, "create"))
			message.add(createHelp);
		if (canSender(sender, "disband"))
			message.add(disbandHelp);
		if (canSender(sender, "help"))
			message.add(whoHelp);
		if (canSender(sender, "add"))
			message.add(addHelp);
		if (canSender(sender, "remove"))
			message.add(removeHelp);
		if (canSender(sender, "motd"))
			message.add(motdHelp);
		if (canSender(sender, "setleader"))
			message.add(setleaderHelp);
		if (canSender(sender, "save"))
			message.add(saveHelp);
		
		int page = 1;
		try {
			if (args.length > 0)
				page = Integer.parseInt(args[0]);
		} catch (Exception Ex){
			
		}
		if (page < 1)
			page = 1;
		if (page >= 1) {
			if (((page - 1) * 7) < (message.size())) {
				Iterator<String> iter = message.listIterator(((page - 1) * 7));
				int pageCap = message.size() / 7;
				if ((message.size() % 7) != 0)
					pageCap += 1;

				sender.sendMessage("");
				sender.sendMessage(Naxcraft.DEFAULT_COLOR + "Page " + page
						+ " of " + pageCap + ":");

				for (int i = 0; (iter.hasNext() && (i < 7)); i++) {
					sender.sendMessage(iter.next());
				}
			}
		}
		return true;
	}
}
