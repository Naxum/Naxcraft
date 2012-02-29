package com.naxville.naxcraft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.naxville.naxcraft.admin.NaxcraftWarpgate.NaxGate;
import com.naxville.naxcraft.player.NaxPlayer;

public class NaxcraftChatCommand
{
	
	private Naxcraft plugin;
	private Server server;
	
	private static final double LOCAL_DISTANCE = 250;
	private static final String CHAT_SEPERATOR = " ";
	
	private Map<Player, ChatChannel> playerChannels = new HashMap<Player, ChatChannel>();
	private Map<Player, Player> playerWhisper = new HashMap<Player, Player>();
	
	private int debug_players = 0; // supposed to be 2
	
	public NaxcraftChatCommand(Naxcraft plugin)
	{
		this.plugin = plugin;
		this.server = plugin.getServer();
	}
	
	public boolean runChatCommand(CommandSender sender, String commandLabel, String[] args)
	{
		
		if (server == null) server = plugin.getServer();
		
		if (!(sender instanceof Player)) { return true; }
		Player player = (Player) sender;
		
		for(String s : args)
		{
			for(ChatColor c : Naxcraft.COLORS)
			{
				s.replaceAll(c + "", "");
			}
		}
		
		if (commandLabel.equalsIgnoreCase("list"))
		{
			getList(player);
			return true;
		}
		
		if (commandLabel.equalsIgnoreCase("msg") || commandLabel.equalsIgnoreCase("tell"))
		{
			if (args.length < 0)
			{
				player.sendMessage(NaxColor.MSG + "No player found");
				return false;
			}
			else
			{
				Player p = server.getPlayer(args[0]);
				if (p == null)
				{
					player.sendMessage(NaxColor.MSG + "Player not found");
					return true;
				}
				args[0] = "";
				
				if (plugin.playerManager.hasMuted(player, p))
				{
					player.sendMessage(NaxColor.MSG + "You have muted this person.");
					return true;
				}
				else if (plugin.playerManager.hasMuted(p, player))
				{
					player.sendMessage(NaxColor.MSG + "This player has muted you.");
					return true;
				}
				
				String message = parseMessage(player, NaxUtil.arrayToString(args));
				player.sendMessage(NaxColor.MSG + "<" + plugin.getNickName(player.getName()) + NaxColor.MSG + " -> " + p.getDisplayName() + NaxColor.MSG + "> " + message);
				p.sendMessage(NaxColor.MSG + "<" + plugin.getNickName(p.getName()) + NaxColor.MSG + " <- " + plugin.getNickName(player.getName()) + NaxColor.WHITE + "> " + message);
				plugin.log.info(NaxColor.MSG + "<" + plugin.getNickName(player.getName()) + NaxColor.MSG + " -> " + p.getDisplayName() + NaxColor.MSG + "> " + message);
				playerWhisper.put(player, p);
				return true;
			}
		}
		
		if (commandLabel.equalsIgnoreCase("r"))
		{
			Player p;
			if (playerWhisper.containsKey(player))
			{
				p = playerWhisper.get(player);
			}
			else
			{
				p = server.getPlayer(args[0]);
				if (p == null)
				{
					player.sendMessage(NaxColor.MSG + "No player found");
					return false;
				}
				else
				{
					playerWhisper.put(player, p);
					args[0] = "";
				}
			}
			String message = parseMessage(player, NaxUtil.arrayToString(args));
			player.sendMessage(NaxColor.MSG + "<" + plugin.getNickName(player.getName()) + NaxColor.MSG + " -> " + p.getDisplayName() + NaxColor.MSG + "> " + message);
			p.sendMessage(NaxColor.MSG + "<" + plugin.getNickName(p.getName()) + NaxColor.MSG + " <- " + plugin.getNickName(player.getName()) + NaxColor.WHITE + "> " + message);
			if (args.length > 0) plugin.log.info(NaxColor.MSG + "<" + plugin.getNickName(player.getName()) + NaxColor.MSG + " -> " + p.getDisplayName() + NaxColor.MSG + "> " + message);
			playerWhisper.put(player, p);
			return true;
			
		}
		
		if (args.length < 1)
		{
			
			if (commandLabel.equalsIgnoreCase("g"))
			{
				setPlayerChannel(player, ChatChannel.GENERAL);
			}
			else if (commandLabel.equalsIgnoreCase("local") || commandLabel.equalsIgnoreCase("l"))
			{
				setPlayerChannel(player, ChatChannel.LOCAL);
			}
			else if (commandLabel.equalsIgnoreCase("m"))
			{
				setPlayerChannel(player, ChatChannel.MULTI);
			}
			else if (commandLabel.equalsIgnoreCase("p"))
			{
				if (plugin.playerManager.isPlayerInParty(player))
				{
					setPlayerChannel(player, ChatChannel.PARTY);
				}
				else
				{
					player.sendMessage(NaxColor.MSG + "You are not in a party.");
				}
			}
			else if (commandLabel.equalsIgnoreCase("chat"))
			{
				player.sendMessage(NaxColor.MSG + "----");
				player.sendMessage(NaxColor.COMMAND + "Chat Commands:");
				player.sendMessage(NaxColor.WHITE + "/g" + NaxColor.MSG + " for " + NaxColor.WHITE + "General Chat");
				player.sendMessage(NaxColor.LOCAL + "/local" + NaxColor.MSG + " for " + NaxColor.LOCAL + "Local Chat");
				player.sendMessage(NaxColor.MULTI + "/m" + NaxColor.MSG + " for " + NaxColor.MULTI + "Multi World Chat");
				player.sendMessage(NaxColor.PARTY + "/p" + NaxColor.MSG + " for " + NaxColor.PARTY + "Party Chat");
				
				player.sendMessage(NaxColor.MSG + "/msg [name] <text> for whispering");
				player.sendMessage(NaxColor.MSG + "/r <text> for replying to a sent whisper");
			}
		}
		else
		{
			if (commandLabel.equalsIgnoreCase("g"))
			{
				sendMessage(player, ChatChannel.GENERAL, NaxUtil.arrayToString(args));
			}
			else if (commandLabel.equalsIgnoreCase("local") || commandLabel.equalsIgnoreCase("l"))
			{
				sendMessage(player, ChatChannel.LOCAL, NaxUtil.arrayToString(args));
			}
			else if (commandLabel.equalsIgnoreCase("m"))
			{
				sendMessage(player, ChatChannel.MULTI, NaxUtil.arrayToString(args));
			}
			else if (commandLabel.equalsIgnoreCase("p"))
			{
				if (plugin.playerManager.isPlayerInParty(player))
				{
					plugin.playerManager.getParty(player).chat(player, NaxUtil.arrayToString(args));
				}
				else
				{
					player.sendMessage(NaxColor.MSG + "You are not in a party.");
				}
			}
			else
			{
				sendMessage(player, null, NaxUtil.arrayToString(args));
			}
		}
		
		return true;
	}
	
	private void getList(Player player)
	{
		List<Player> players = plugin.playerManager.handlePlayerChat(player, getLocalVisiblePlayers(player));
		if (players.size() == 0)
		{
			player.sendMessage(NaxColor.MSG + "No one can hear you squeal!");
			return;
		}
		else
		{
			String message = players.size() + "" + NaxColor.MSG + " Nearby players: ";
			Iterator<Player> iter = players.iterator();
			for (int i = 0; iter.hasNext(); i++)
			{
				Player p = iter.next();
				if (i != 0) message += NaxColor.MSG + ", ";
				message += p.getDisplayName();
			}
			player.sendMessage(message);
			return;
		}
	}
	
	private void sendMessage(Player player, ChatChannel channel, String message)
	{
		if (channel == null) channel = playerChannels.get(player);
		if (channel == null) channel = ChatChannel.GENERAL;
		
		if (channel == ChatChannel.PARTY)
		{
			if (plugin.playerManager.isPlayerInParty(player))
			{
				plugin.playerManager.getParty(player).chat(player, message);
				return;
			}
			else
			{
				player.sendMessage(NaxColor.MSG + "You are no longer in a party, switching to General Chat.");
				setPlayerChannel(player, ChatChannel.GENERAL);
				channel = ChatChannel.GENERAL;
			}
		}
		
		message = parseMessage(player, message);
		
		switch (channel)
		{
			case MULTI:
				List<Player> allPlayers = plugin.playerManager.handlePlayerChat(player, getAllWorldPlayers(player));
				
				ChatColor color = Naxcraft.WORLD_COLOR;
				
				if (allPlayers.size() < debug_players)
				{
					player.sendMessage(NaxColor.MSG + "There is no one. No. One.");
					break;
				}
				
				if (player.getWorld().getName().endsWith("_nether"))
				{
					color = Naxcraft.NETHER_COLOR;
				}
				
				for (Player p : allPlayers)
				{
					p.sendMessage(NaxColor.MSG + "[" + color + plugin.getWorldName(player.getWorld()) + NaxColor.MSG + "] " + color + "<" + plugin.getNickName(player.getName()) + color + "> " + NaxColor.WHITE + message);
				}
				plugin.log.info(NaxColor.MSG + "[" + color + plugin.getWorldName(player.getWorld()) + NaxColor.MSG + "] " + color + "<" + plugin.getNickName(player.getName()) + color + "> " + NaxColor.WHITE + message);
				break;
			case GENERAL:
				List<Player> global = plugin.playerManager.handlePlayerChat(player, getGlobalPlayers(player));
				if (global.size() < debug_players)
				{
					player.sendMessage(NaxColor.MSG + "You're just a small town girl...");
					break;
				}
				// player.sendMessage(NaxColor.MSG + "<" + plugin.getNickName(player.getName()) + NaxColor.MSG + ">" + NaxColor.WHITE + CHAT_SEPERATOR + message);
				for (Player p : global)
				{
					p.sendMessage(plugin.getTitles(player) + NaxColor.WHITE + "<" + plugin.getNickName(player) + NaxColor.WHITE + ">" + NaxColor.WHITE + CHAT_SEPERATOR + message);
				}
				plugin.log.info(plugin.getTitles(player) + NaxColor.WHITE + "<" + plugin.getNickName(player) + NaxColor.WHITE + ">" + NaxColor.WHITE + CHAT_SEPERATOR + message);
				break;
			case LOCAL:
				List<Player> local = plugin.playerManager.handlePlayerChat(player, getLocalPlayers(player));
				int visibleLocalPlayers = getLocalNumber(player);
				
				if (local.size() < 1)
				{
					player.sendMessage(NaxColor.MSG + "Nobody is close enough to hear you scream.");
					break;
				}
				player.sendMessage(NaxColor.LOCAL + "[L " + NaxColor.WHITE + visibleLocalPlayers + NaxColor.LOCAL + "] <" + plugin.getNickName(player.getName()) + NaxColor.LOCAL + ">" + NaxColor.WHITE + CHAT_SEPERATOR + message);
				for (Player p : local)
				{
					p.sendMessage(NaxColor.LOCAL + "[L " + NaxColor.WHITE + visibleLocalPlayers + NaxColor.LOCAL + "] <" + plugin.getNickName(player.getName()) + NaxColor.LOCAL + ">" + NaxColor.WHITE + CHAT_SEPERATOR + message);
				}
				plugin.log.info(NaxColor.LOCAL + "[L " + NaxColor.WHITE + visibleLocalPlayers + NaxColor.LOCAL + "] <" + plugin.getNickName(player.getName()) + NaxColor.LOCAL + ">" + NaxColor.WHITE + CHAT_SEPERATOR + message);
				break;
		}
	}
	
	private void setPlayerChannel(Player player, ChatChannel channel)
	{
		switch (channel)
		{
			case GENERAL:
				if (!playerChannels.containsKey(player) || playerChannels.get(player) == ChatChannel.GENERAL)
				{
					player.sendMessage(NaxColor.MSG + "You are already in the " + NaxColor.MSG + "general" + NaxColor.MSG + " channel.");
					return;
				}
				else
				{
					playerChannels.put(player, channel);
					player.sendMessage(NaxColor.MSG + "You are now in the " + NaxColor.WHITE + "general" + NaxColor.MSG + " channel.");
					return;
				}
			case LOCAL:
				if (playerChannels.containsKey(player) && playerChannels.get(player) == ChatChannel.LOCAL)
				{
					player.sendMessage(NaxColor.MSG + "You are already in the " + NaxColor.MSG + "local" + NaxColor.MSG + " channel.");
					return;
				}
				else
				{
					playerChannels.put(player, channel);
					player.sendMessage(NaxColor.MSG + "You are now in the " + NaxColor.LOCAL + "local" + NaxColor.MSG + " channel.");
					return;
				}
				
			case PARTY:
				if (playerChannels.containsKey(player) && playerChannels.get(player) == ChatChannel.PARTY)
				{
					player.sendMessage(NaxColor.MSG + "You are already in the " + NaxColor.MSG + "party" + NaxColor.MSG + " channel.");
				}
				else
				{
					playerChannels.put(player, channel);
					player.sendMessage(NaxColor.MSG + "You are now in the " + NaxColor.PARTY + "party" + NaxColor.MSG + " channel.");
				}
				break;
			
			case MULTI:
				if (playerChannels.containsKey(player) && playerChannels.get(player) == ChatChannel.MULTI)
				{
					player.sendMessage(NaxColor.MSG + "You are already in the " + NaxColor.MSG + "multi world" + NaxColor.MSG + " channel.");
					return;
				}
				else
				{
					playerChannels.put(player, channel);
					player.sendMessage(NaxColor.MSG + "You are now in the " + Naxcraft.WORLD_COLOR + "multi world" + NaxColor.MSG + " channel.");
					return;
				}
		}
		
		return;
	}
	
	private List<Player> getLocalPlayers(Player player)
	{
		List<Player> players = new ArrayList<Player>();
		for (Player p : server.getOnlinePlayers())
		{
			if (!player.equals(p) && getDistance(player, p) <= LOCAL_DISTANCE)
			{
				if (player.getWorld().equals(p.getWorld()))
				{
					players.add(p);
				}
			}
		}
		return players;
	}
	
	private List<Player> getLocalVisiblePlayers(Player player)
	{
		List<Player> players = new ArrayList<Player>();
		for (Player p : server.getOnlinePlayers())
		{
			if (!player.equals(p) && getDistance(player, p) <= LOCAL_DISTANCE)
			{
				if (player.getWorld().equals(p.getWorld()))
				{
					if (!plugin.stealthCommand.isInvisible(p))
					{
						players.add(p);
					}
				}
			}
		}
		return players;
	}
	
	private int getLocalNumber(Player player)
	{
		int num = 0;
		
		for (Player p : server.getOnlinePlayers())
		{
			if (!player.equals(p) && getDistance(player, p) <= LOCAL_DISTANCE)
			{
				if (player.getWorld().equals(p.getWorld()))
				{
					if (!plugin.stealthCommand.isInvisible(p))
					{
						num++;
					}
				}
			}
		}
		
		return num;
	}
	
	private List<Player> getGlobalPlayers(Player player)
	{
		List<Player> players = new ArrayList<Player>();
		for (Player p : server.getOnlinePlayers())
		{
			if (player.getWorld().equals(p.getWorld()))
			{
				players.add(p);
			}
		}
		return players;
	}
	
	private List<Player> getAllWorldPlayers(Player player)
	{
		List<Player> players = new ArrayList<Player>();
		for (Player p : server.getOnlinePlayers())
		{
			players.add(p);
		}
		return players;
	}
	
	private double getDistance(Player p1, Player p2)
	{
		Location loc1 = p1.getLocation();
		Location loc2 = p2.getLocation();
		
		return Math.sqrt(Math.pow(loc1.getX() - loc2.getX(), 2) + Math.pow(loc1.getY() - loc2.getY(), 2) + Math.pow(loc1.getZ() - loc2.getZ(), 2));
	}
	
	public String parseMessage(Player player, String str)
	{
		str = str.replaceAll("<3+", ChatColor.RED + "<3" + ChatColor.WHITE);
		str = str.replaceAll("E>+", ChatColor.RED + "E>" + ChatColor.WHITE);
		
		for (String s : plugin.cityManager.citySpawns.keySet())
		{
			str = str.replaceAll("(?i)" + s, NaxColor.CITY + s + NaxColor.WHITE);
		}
		
		for (NaxGate g : plugin.warpgateCommand.gates)
		{
			str = str.replaceAll("(?i)" + g.getName(), NaxColor.CITY + g.getName() + NaxColor.WHITE);
		}
		
		for (NaxPlayer p : plugin.playerManager.players)
		{
			if (p.rank.isDemiAdmin())
			{
				str = str.replaceAll("(?i)" + p.name, p.getChatName() + NaxColor.WHITE);
			}
		}
		
		List<Integer> counts = new ArrayList<Integer>();
		
		for (int i = 0; i < str.length(); i++)
		{
			if (str.charAt(i) == '@') counts.add(i);
		}
		
		for (int start : counts)
		{
			int i = 1;
			
			while (start + i < str.length() && str.charAt(start + i) != ' ')
			{
				i++;
			}
			
			String name = str.substring(start + 1, start + i);
			plugin.getLogger().log(Level.INFO, name);
			NaxPlayer p = plugin.playerManager.getPlayer(name);
			
			if (p == null)
			{
				str.replaceFirst("@", "");
			}
			else
			{
				str = str.replaceFirst("@" + name, p.getChatName());
			}
		}
		
		String str2 = str.replaceFirst(">>>", ">"); // disabled
		if (!str.equals(str2))
		{
			player.sendMessage(NaxColor.MSG + "Nice try.");
		}
		
		return str;
	}
}

enum ChatChannel
{
	GENERAL,
	LOCAL,
	MULTI,
	PARTY
}
