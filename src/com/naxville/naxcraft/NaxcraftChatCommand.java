package com.naxville.naxcraft;

import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NaxcraftChatCommand {
	
	private Naxcraft plugin;
	private Server server;
	
	private static final double LOCAL_DISTANCE = 250;
	private static final String CHAT_SEPERATOR = " ";
	
	private Map<Player, ChatChannel> playerChannels = new HashMap<Player, ChatChannel>();
	private Map<Player, Player> playerWhisper = new HashMap<Player, Player>();
	
	public NaxcraftChatCommand(Naxcraft instance){
		this.plugin = instance;
	}
	
	public boolean runChatCommand(CommandSender sender, String commandLabel, String[] args){
		
		if (server == null)
			server = plugin.getServer(); 
		
		if (!(sender instanceof Player)){
			sender.sendMessage("Only players can use the clan chat system.  Sorry :(");
			return true;
		}
		Player player = (Player)sender;
		
		if (commandLabel.equalsIgnoreCase("list")){
			getList(player);
			return true;
		}
		
		if (commandLabel.equalsIgnoreCase("msg")){
			if (args.length < 0){
				player.sendMessage(Naxcraft.ERROR_COLOR + "No player found");
				return false;
			} else { 
				Player p = server.getPlayer(args[0]);
				if (p == null){
					player.sendMessage(Naxcraft.ERROR_COLOR + "Player not found");
					return true;
				}
				args[0] = "";

				String message = arrayToString(args);
				player.sendMessage(Naxcraft.MSG_COLOR + "<" + plugin.getNickName(player.getName()) + Naxcraft.MSG_COLOR +" -> " + p.getDisplayName() + Naxcraft.MSG_COLOR + "> " + message);
				p.sendMessage(Naxcraft.MSG_COLOR + "<" + plugin.getNickName(p.getName()) + Naxcraft.MSG_COLOR + " <- " + plugin.getNickName(player.getName()) + Naxcraft.DEFAULT_COLOR + "> " + message);
				playerWhisper.put(player, p);
				return true;
			}
		}
		
		if (commandLabel.equalsIgnoreCase("r")){
			Player p;
			if (playerWhisper.containsKey(player)){
				p = playerWhisper.get(player);
			} else {
				p = server.getPlayer(args[0]);
				if (p == null){
					player.sendMessage(Naxcraft.ERROR_COLOR + "No player found");
					return false;
				} else {
					playerWhisper.put(player, p);
					args[0] = "";
				}
			}
			String message = arrayToString(args);
			player.sendMessage(Naxcraft.MSG_COLOR + "<" + plugin.getNickName(player.getName()) + Naxcraft.MSG_COLOR +" -> " + p.getDisplayName() + Naxcraft.MSG_COLOR + "> " + message);
			p.sendMessage(Naxcraft.MSG_COLOR + "<" + plugin.getNickName(p.getName()) + Naxcraft.MSG_COLOR + " <- " + plugin.getNickName(player.getName()) + Naxcraft.DEFAULT_COLOR + "> " + message);
			plugin.log.info("MSG: " + player.getName() + " -> " + p.getName() + ": " + message);
			playerWhisper.put(player, p);
			return true;
			
		}
		
		if (args.length < 1){

			if (commandLabel.equalsIgnoreCase("g")) {
				setPlayerChannel(player, ChatChannel.GENERAL);
				return true;
			} else if (commandLabel.equalsIgnoreCase("l")) {
				setPlayerChannel(player, ChatChannel.LOCAL);
				return true;
			} else if (commandLabel.equalsIgnoreCase("c")) {
				setPlayerChannel(player, ChatChannel.CLAN);
				return true;
			} else if (commandLabel.equalsIgnoreCase("m")){
				setPlayerChannel(player, ChatChannel.MULTI);
				return true;
			}
		} else {
			if (commandLabel.equalsIgnoreCase("g")){
				sendMessage(player, ChatChannel.GENERAL, arrayToString(args));
				return true;
			} else if (commandLabel.equalsIgnoreCase("l")){
				sendMessage(player, ChatChannel.LOCAL, arrayToString(args));
				return true;
			} else if (commandLabel.equalsIgnoreCase("c")){
				sendMessage(player, ChatChannel.CLAN, arrayToString(args));
				return true;
			} else if (commandLabel.equalsIgnoreCase("m")){
				sendMessage(player, ChatChannel.MULTI, arrayToString(args));
				return true;
			} else {
				sendMessage(player, null, arrayToString(args));
				return true;
			} 
		}
		
		return false;
	}
	
	private void getList(Player player){
		Set<Player> players = getLocalVisiblePlayers(player);
		if (players.size() == 0){
			player.sendMessage(Naxcraft.MSG_COLOR + "No one can hear you squeal!");
			return;
		} else {
			String message = players.size() + "" + Naxcraft.MSG_COLOR + " Nearby players: ";
			Iterator<Player> iter = players.iterator();
			for (int i = 0; iter.hasNext(); i++){
				Player p = iter.next();
				if (i != 0) message += Naxcraft.MSG_COLOR + ", ";
				message += p.getDisplayName();
			}
			player.sendMessage(message);
			return;
		}
	}
	
	private void sendMessage(Player player, ChatChannel channel, String message){
		if (channel == null) channel = playerChannels.get(player);
		if (channel == null) channel = ChatChannel.GENERAL;
		
		int debug_players = 2; //supposed to be 2
		
		switch (channel){
		case MULTI:
			Set<Player>allPlayers = getAllWorldPlayers(player);
			
			ChatColor color = Naxcraft.WORLD_COLOR;
			
			if(allPlayers.size() < debug_players){
				player.sendMessage(Naxcraft.MSG_COLOR + "There is no one. No. One.");
				break;
			}
			
			if(player.getWorld().getName().endsWith("_nether"))
			{
				color = Naxcraft.NETHER_COLOR;
			}
			
			for(Player p : allPlayers){
				p.sendMessage(Naxcraft.MSG_COLOR + "[" + color + plugin.getWorldName(player.getWorld()) + Naxcraft.MSG_COLOR + "] " + color + "<" + plugin.getNickName(player.getName()) + color + "> " + Naxcraft.DEFAULT_COLOR + message);
			}
			plugin.log.info("M: " + player.getName() + ": " + message);
			break;
		case GENERAL:
			Set<Player> global = getGlobalPlayers(player);
			if (global.size() < debug_players){
				player.sendMessage(Naxcraft.MSG_COLOR + "You're just a small town girl...");
				break;
			}
			//player.sendMessage(Naxcraft.MSG_COLOR + "<" + plugin.getNickName(player.getName()) + Naxcraft.MSG_COLOR + ">" + Naxcraft.DEFAULT_COLOR + CHAT_SEPERATOR + message);
			for (Player p : global){
				p.sendMessage(Naxcraft.DEFAULT_COLOR + "<" + plugin.getNickName(player.getName()) + Naxcraft.DEFAULT_COLOR + ">" + Naxcraft.DEFAULT_COLOR + CHAT_SEPERATOR + message);
			}
			plugin.log.info("G: " + player.getName() + ": " + message);
			break;
		case LOCAL:
			Set<Player> local = getLocalPlayers(player);
			int visibleLocalPlayers = getLocalNumber(player); 
			
			if (local.size() < 1){
				player.sendMessage(Naxcraft.MSG_COLOR + "Nobody is close enough to hear you scream.");
				break;
			}
			player.sendMessage(ChatColor.GREEN + "[L " + Naxcraft.DEFAULT_COLOR + visibleLocalPlayers + ChatColor.GREEN  + "] <" + plugin.getNickName(player.getName()) + ChatColor.GREEN + ">" + Naxcraft.DEFAULT_COLOR + CHAT_SEPERATOR + message);
			for (Player p : local){
				p.sendMessage(ChatColor.GREEN + "[L " + Naxcraft.DEFAULT_COLOR + visibleLocalPlayers + ChatColor.GREEN  + "] <" + plugin.getNickName(player.getName()) + ChatColor.GREEN + ">" + Naxcraft.DEFAULT_COLOR + CHAT_SEPERATOR + message);
			}
			plugin.log.info("L: " + player.getName() + ": " + message);
			break;
		case CLAN:
			//TODO: ADD WORLD PREFIX IF OUTSIDE TARGET WORLD.
			NaxcraftClan clan = plugin.clanCommand.getPlayerClan(player);
			if (clan == null){
				player.sendMessage(Naxcraft.MSG_COLOR + "You don't belong to any clans");
				break;
			}
			if(clan.getOnlineMembers().size() < debug_players){
				player.sendMessage(Naxcraft.MSG_COLOR + "None of your members are online.");
				break;
			}
			for(Player p : clan.getOnlineMembers()){
				if(player.getWorld().equals(p.getWorld())){
					p.sendMessage(Naxcraft.CLAN_COLOR + "[C] <" + plugin.getNickName(player.getName()) + Naxcraft.CLAN_COLOR + ">" + Naxcraft.MSG_COLOR + CHAT_SEPERATOR + Naxcraft.DEFAULT_COLOR + message);
				
				} else {
					p.sendMessage(Naxcraft.CLAN_COLOR + "[C "+ Naxcraft.WORLD_COLOR + plugin.getWorldName(player.getWorld()) + Naxcraft.CLAN_COLOR + "] <" + plugin.getNickName(player.getName())  + Naxcraft.CLAN_COLOR + ">" + CHAT_SEPERATOR + Naxcraft.DEFAULT_COLOR + message);
				}
			}
			plugin.log.info("C: " + player.getName() + ": " + message);
			return;
		}
	}
	
	private void setPlayerChannel(Player player, ChatChannel channel){
		switch (channel){
		case GENERAL:
			if (!playerChannels.containsKey(player) || playerChannels.get(player) == ChatChannel.GENERAL){
				player.sendMessage(Naxcraft.MSG_COLOR + "You are already in the " + Naxcraft.MSG_COLOR + "general" + Naxcraft.MSG_COLOR +" channel.");
				return;
			} else {
				playerChannels.put(player, channel);
				player.sendMessage(Naxcraft.MSG_COLOR + "You are now in the " + Naxcraft.DEFAULT_COLOR + "general" + Naxcraft.MSG_COLOR +" channel.");
				return;
			}
		case LOCAL:
			if (playerChannels.containsKey(player) && playerChannels.get(player) == ChatChannel.LOCAL){
				player.sendMessage(Naxcraft.MSG_COLOR + "You are already in the " + Naxcraft.MSG_COLOR + "local" + Naxcraft.MSG_COLOR +" channel.");
				return;
			} else {
				playerChannels.put(player, channel);
				player.sendMessage(Naxcraft.MSG_COLOR + "You are now in the " + ChatColor.GREEN + "local" + Naxcraft.MSG_COLOR +" channel.");
				return;
			}
		case CLAN:
			if (playerChannels.containsKey(player) && playerChannels.get(player) == ChatChannel.CLAN){
				player.sendMessage(Naxcraft.MSG_COLOR + "You are already in the " + Naxcraft.MSG_COLOR + "clan" + Naxcraft.MSG_COLOR +" channel.");
				return;
			} else {
				playerChannels.put(player, channel);
				player.sendMessage(Naxcraft.MSG_COLOR + "You are now in the " + Naxcraft.CLAN_COLOR + "clan" + Naxcraft.MSG_COLOR +" channel.");
				return;
			}
		case MULTI:
			if(playerChannels.containsKey(player) && playerChannels.get(player) == ChatChannel.MULTI){
				player.sendMessage(Naxcraft.MSG_COLOR + "You are already in the " + Naxcraft.MSG_COLOR + "multi world" + Naxcraft.MSG_COLOR +" channel.");
				return;
			} else {
				playerChannels.put(player, channel);
				player.sendMessage(Naxcraft.MSG_COLOR + "You are now in the " + Naxcraft.WORLD_COLOR + "multi world" + Naxcraft.MSG_COLOR +" channel.");
				return;
			}
		}
			
		return;
	}
	
	private Set<Player>getLocalPlayers(Player player){
		Set<Player>players = new HashSet<Player>();
		for(Player p : server.getOnlinePlayers()){
			if (!player.equals(p) &&getDistance(player, p) <= LOCAL_DISTANCE){
				if(player.getWorld().equals(p.getWorld())){
					players.add(p);
				}
			}
		}
		return players;
	}
	
	private Set<Player>getLocalVisiblePlayers(Player player){
		Set<Player>players = new HashSet<Player>();
		for(Player p : server.getOnlinePlayers()){
			if (!player.equals(p) &&getDistance(player, p) <= LOCAL_DISTANCE){
				if(player.getWorld().equals(p.getWorld())){
					if(!plugin.stealthCommand.isInvisible(p)){
						players.add(p);
					}
				}
			}
		}
		return players;
	}
	
	private int getLocalNumber(Player player){
		int num = 0;
		
		for(Player p : server.getOnlinePlayers()){
			if (!player.equals(p) && getDistance(player, p) <= LOCAL_DISTANCE){
				if(player.getWorld().equals(p.getWorld())){
					num++;
				}
			}
		}
		
		return num;
	}
	
	
	private Set<Player>getGlobalPlayers(Player player){
		Set<Player>players = new HashSet<Player>();
		for(Player p : server.getOnlinePlayers()){
			if(player.getWorld().equals(p.getWorld())){
				players.add(p);
			}
		}
		return players;
	}
	
	private Set<Player>getAllWorldPlayers(Player player){
		Set<Player>players = new HashSet<Player>();
		for(Player p : server.getOnlinePlayers()){
			players.add(p);
		}
		return players;
	}
	
	private double getDistance(Player p1, Player p2){
		Location loc1 = p1.getLocation();
		Location loc2 = p2.getLocation();
		
		return Math.sqrt(Math.pow(loc1.getX() - loc2.getX(), 2) + Math.pow(loc1.getY() - loc2.getY(), 2) + Math.pow(loc1.getZ() - loc2.getZ(), 2));
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

}

enum ChatChannel{
	GENERAL,
	LOCAL,
	CLAN,
	MULTI
}
