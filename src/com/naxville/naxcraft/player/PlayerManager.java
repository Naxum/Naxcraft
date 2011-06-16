package com.naxville.naxcraft.player;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;

import com.naxville.naxcraft.Naxcraft;
import com.naxville.naxcraft.admin.NaxcraftConfiguration;

public class PlayerManager 
{
	public Naxcraft plugin;
	public NaxcraftConfiguration config;
	public final String FILE_NAME = "player_data.yml";
	
	public List<NaxPlayer> players = new ArrayList<NaxPlayer>();
	
	public PlayerManager (Naxcraft plugin)
	{
		this.plugin = plugin;
	}
	
	public String handlePlayerJoin(PlayerJoinEvent event)
	{
		NaxPlayer p = getPlayer(event.getPlayer());
		
		if(p == null)
		{
			p = createNaxPlayer(event.getPlayer());
			return Naxcraft.MSG_COLOR + "Welcome " + getDisplayName(event.getPlayer()) + " to " + plugin.getWorldName(event.getPlayer().getWorld(), true) + "!";
		}
		else
		{
			return getDisplayName(event.getPlayer()) + " has entered " + plugin.getWorldName(event.getPlayer().getWorld(), true) + ".";
		}
	}
	
	public String getDisplayName(String str)
	{
		for(NaxPlayer p : players)
		{
			if(p.name.equalsIgnoreCase(str))
			{
				return getDisplayName(p);
			}
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
		if(p == null)
		{
			return Naxcraft.DEFAULT_COLOR + player.getName() + Naxcraft.MSG_COLOR;
		}
		else if(p.displayName == null)
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
		if(p.displayName == null)
		{
			return p.rank.getPrefix() + Naxcraft.DEFAULT_COLOR + p.name + Naxcraft.MSG_COLOR;
		}
		else
		{
			return p.rank.getPrefix() + p.displayName + Naxcraft.MSG_COLOR;
		}
	}

	public NaxPlayer getPlayer(Player player)
	{
		for(NaxPlayer p : players)
		{
			if(p.name.equalsIgnoreCase(player.getName()))
			{
				if(!p.name.equals(player.getName()))
				{
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
		for(NaxPlayer p : players)
		{
			if(p.name.equalsIgnoreCase(string))
				return p;
		}
		
		return null;
	}
	
	public void loadFile()
	{
		File file = new File(plugin.filePath + FILE_NAME);
		
		try
		{
			if(!file.exists())
			{
				file.createNewFile();
				initializeFile(file);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		config = new NaxcraftConfiguration(file);
		config.load();
	}
	
	public void initializeFile(File file)
	{
		try
		{
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			
			writer.write("players: {}");
			writer.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void loadPlayerData()
	{
		loadFile();
		
		List<String> keys = config.getKeys("players");
		
		if(keys == null || keys.isEmpty())
		{
			System.out.println("No player data to load, continuing.");
			return;
		}
		
		for(String key : keys)
		{
			String prefix = "players." + key;
			
			String displayName = config.getString(prefix + ".displayName");
			PlayerRank rank = PlayerRank.getRank(config.getString(prefix + ".rank"));
			
			List<String> worlds = config.getKeys(prefix + ".homes");
			Map<World, Location> homes = new HashMap<World, Location>();
			
			if(worlds != null)
			{
				for(String world : worlds)
				{
					World w = plugin.getServer().getWorld(world);
					
					if(w != null)
					{
						homes.put(w, new Location
										 (
											w,
											Double.parseDouble(config.getString(prefix + ".homes." + world + ".x")),
											Double.parseDouble(config.getString(prefix + ".homes." + world + ".y")),
											Double.parseDouble(config.getString(prefix + ".homes." + world + ".z"))
										 ));
					}
				}
			}
			
			NaxPlayer np = new NaxPlayer(key, rank, displayName, homes);
			players.add(np);
		}
	}
	
	public NaxPlayer createNaxPlayer(Player p)
	{
		NaxPlayer np = new NaxPlayer(p.getName(), PlayerRank.NOOB, null, null);
		players.add(np);
		savePlayer(np);
		
		return np;
	}
	
	public void savePlayer(NaxPlayer p)
	{
		String prefix = "players." + p.name;
		
		config.setProperty(prefix + ".rank", p.rank.name());
		
		if(p.displayName != null)
		{
			config.setProperty(prefix + ".displayName", p.displayName);	
		}
		
		if(p.homes != null)
		{
			for(World w : p.homes.keySet())
			{
				config.setProperty(prefix + ".homes." + w.getName() + ".x", p.homes.get(w).getX());
				config.setProperty(prefix + ".homes." + w.getName() + ".y", p.homes.get(w).getY());
				config.setProperty(prefix + ".homes." + w.getName() + ".z", p.homes.get(w).getZ());
			}
		}
		
		config.save();
	}
	
	public enum PlayerRank 
	{		
		NOOB(0, ""),
		MEMBER(1, "+"),
		VETERAN(2, "*"),
		PATRON(3, "$"),
		MODERATOR(4, "#"),
		ADMIN(5, "@");
		
		private final int id;
		private final String prefix;
		private static final Map<String, PlayerRank> lookupName = new HashMap<String, PlayerRank>();
		
		PlayerRank(int id, String prefix)
		{
			this.id = id;
			this.prefix = prefix;
		}
		
		public int getId()
		{
			return id;
		}
		
		public String getName()
		{
			String s = this.name().charAt(0) + "";
			String r = this.name().substring(1);
			
			return Naxcraft.DEFAULT_COLOR + s.toUpperCase() + r.toLowerCase() + Naxcraft.MSG_COLOR;
		}
		
		public String getPrefix()
		{
			return Naxcraft.MSG_COLOR + prefix;
		}
		
		static
		{
			for(PlayerRank rank : values())
			{
				lookupName.put(rank.name().toLowerCase(), rank);
			}
		}
		
		public static PlayerRank getRank(String str)
		{
			str = str.toLowerCase();
			
			if(lookupName.containsKey(str))
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
			
			for(String s : lookupName.keySet())
			{
				result.add(lookupName.get(s));
			}
			
			return result;
		}

		public static PlayerRank getRank(int i) 
		{
			for(String s : lookupName.keySet())
			{
				if(lookupName.get(s).getId() == i)
				{
					return lookupName.get(s);
				}
			}
			return null;
		}

		public boolean isAdmin() 
		{
			return id >= PlayerRank.MODERATOR.getId();
		}
	}

	public void setDisplayName(Player player, ChatColor color) 
	{
		NaxPlayer p = getPlayer(player);
		
		p.displayName = color + p.name + Naxcraft.MSG_COLOR;
		savePlayer(p);
	}
}
