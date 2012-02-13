package com.naxville.naxcraft.autoareas;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

import com.naxville.naxcraft.NaxColor;
import com.naxville.naxcraft.NaxFile;
import com.naxville.naxcraft.Naxcraft;
import com.naxville.naxcraft.player.NaxPlayer;

public class CityManager
{
	public Naxcraft plugin;
	public NaxFile config;
	
	public Map<String, Location> citySpawns = new HashMap<String, Location>();
	public Map<Player, Boolean> teleporting = new HashMap<Player, Boolean>(); // boolean is true if teleporting, false if hurt
	
	public CityManager(Naxcraft p)
	{
		this.plugin = p;
	}
	
	public void runCityCommand(Player p, String[] args)
	{
		NaxPlayer np = plugin.playerManager.getPlayer(p);
		
		if (args.length == 0)
		{
			if (!np.rank.isAdmin())
			{
				printNormalHelp(p);
			}
			else
			{
				printAdminHelp(p);
			}
			
			return;
		}
		else if (args.length == 1 && args[0].equalsIgnoreCase("list"))
		{
			String result = "";
			
			for (String city : citySpawns.keySet())
			{
				Location l = citySpawns.get(city);
				
				if (l.getWorld() != p.getWorld()) continue;
				
				if (result != "") result += NaxColor.MSG + ", ";
				result += NaxColor.CITY + city;
			}
			
			p.sendMessage(NaxColor.MSG + "Cities in this world: " + result);
			return;
		}
		else if (args.length >= 1 && args[0].equalsIgnoreCase("nearest"))
		{
			p.sendMessage(NaxColor.MSG + getNearestCity(p));
			return;
		}
		else if (args.length == 1 && (args[0].equalsIgnoreCase("tp") || args[0].equalsIgnoreCase("teleport")))
		{
			teleportToCity(p, getNearestCityLocation(p));
			return;
		}
		
		if (!np.rank.isAdmin()) return;
		
		if (args.length == 2)
		{
			if (args[0].equalsIgnoreCase("add"))
			{
				addCity(args[1], p.getLocation());
				p.sendMessage(NaxColor.MSG + "City " + args[1] + " successfully added!");
			}
			else if (args[0].equalsIgnoreCase("remove"))
			{
				if (citySpawns.containsKey(args[1]))
				{
					removeCity(args[1]);
					p.sendMessage(NaxColor.MSG + "City removed!");
				}
				else
				{
					p.sendMessage(NaxColor.MSG + "No city exists with the name " + args[1]);
				}
			}
			else if (args[0].equalsIgnoreCase("tp"))
			{
				if (citySpawns.containsKey(args[1]))
				{
					teleportToCity(p, citySpawns.get(args[1]), true);
				}
				else
				{
					p.sendMessage(NaxColor.MSG + "No city exists with the name " + args[1]);
				}
			}
			else
			{
				printAdminHelp(p);
			}
		}
		else
		{
			printAdminHelp(p);
		}
	}
	
	public void handleEntityDamage(EntityDamageEvent event)
	{
		if (!(event.getEntity() instanceof Player)) return;
		
		Player p = (Player) event.getEntity();
		
		if (teleporting.containsKey(p))
		{
			if (teleporting.get(p))
			{
				p.sendMessage(NaxColor.MSG + "You got hurt while teleporting! Cancelled!");
				teleporting.put(p, false);
			}
		}
	}
	
	private void teleportToCity(Player p, Location l)
	{
		teleportToCity(p, l, false);
	}
	
	private void teleportToCity(Player p, Location l, boolean instant)
	{
		if (teleporting.containsKey(p))
		{
			p.sendMessage(NaxColor.MSG + "You are already waiting to teleport! Be patient.");
			return;
		}
		
		teleporting.put(p, true);
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new CityTeleporter(p, l), 20 * getSecondDistance(p, l, instant));
		
		p.sendMessage(NaxColor.MSG + "Teleporting to " + getNearestCityName(p) + "! This will take " + NaxColor.CITY + getSecondDistance(p, l, instant) + NaxColor.MSG + " seconds.");
	}
	
	private int getSecondDistance(Player p, Location l, boolean instant)
	{
		if (p.getWorld() != l.getWorld()) return 1;
		if (instant) return 1;
		
		return (int) (l.distance(p.getLocation()) / 500) + 5;
	}
	
	private void printNormalHelp(Player p)
	{
		p.sendMessage(NaxColor.MSG + "----");
		p.sendMessage(NaxColor.COMMAND + "/city command help:");
		p.sendMessage(NaxColor.MSG + "/city " + NaxColor.WHITE + "list" + NaxColor.MSG + ": " + "Lists city names.");
		p.sendMessage(NaxColor.MSG + "/city " + NaxColor.WHITE + "tp" + NaxColor.MSG + ": " + "Teleport to nearest city.");
		p.sendMessage(NaxColor.MSG + "/city " + NaxColor.WHITE + "teleport" + NaxColor.MSG + ": " + "Teleport to nearest city.");
		p.sendMessage(NaxColor.MSG + "/city " + NaxColor.WHITE + "help" + NaxColor.MSG + ": " + "Shows this message.");
		p.sendMessage(NaxColor.MSG + "/city " + NaxColor.WHITE + "nearest" + NaxColor.MSG + ": " + "Displays nearest city.");
	}
	
	private void printAdminHelp(Player p)
	{
		printNormalHelp(p);
		p.sendMessage(NaxColor.MSG + "/city " + NaxColor.WHITE + "add [name]" + NaxColor.MSG + ": " + "Adds city spawn at your location.");
		p.sendMessage(NaxColor.MSG + "/city " + NaxColor.WHITE + "remove [name]" + NaxColor.MSG + ": " + "Removes city with name.");
		p.sendMessage(NaxColor.MSG + "/city " + NaxColor.WHITE + "tp [name]" + NaxColor.MSG + ": " + "Teleport to city with name.");
	}
	
	private void addCity(String string, Location location)
	{
		citySpawns.put(string, location);
		saveLocation(string, location);
	}
	
	private void removeCity(String name)
	{
		citySpawns.remove(name);
		config.setProperty("cities." + name, "");
		config.save();
	}
	
	private void saveLocation(String id, Location loc)
	{
		String prefix = "cities." + id;
		config.setProperty(prefix, "");
		
		config.setProperty(prefix + ".world", loc.getWorld().getName());
		config.setProperty(prefix + ".x", loc.getX());
		config.setProperty(prefix + ".y", loc.getY());
		config.setProperty(prefix + ".z", loc.getZ());
		
		config.save();
	}
	
	private String getNearestCity(Player p)
	{
		String city = "";
		double distanceToBeat = 10000;
		
		for (String c : citySpawns.keySet())
		{
			Location l = citySpawns.get(c);
			
			if (l.distance(p.getLocation()) < distanceToBeat)
			{
				city = c;
				distanceToBeat = l.distance(p.getLocation());
			}
		}
		
		if (city != "") return "City of " + NaxColor.CITY + city + NaxColor.MSG + " is " + distanceToBeat + " blocks away.";
		else return "There are no cities near you in this world.";
	}
	
	private String getNearestCityName(Player p)
	{
		String city = "";
		double distanceToBeat = 10000;
		
		for (String c : citySpawns.keySet())
		{
			Location l = citySpawns.get(c);
			
			if (l.distance(p.getLocation()) < distanceToBeat)
			{
				city = c;
				distanceToBeat = l.distance(p.getLocation());
			}
		}
		
		if (city != "") return NaxColor.CITY + city + NaxColor.MSG;
		else return null;
	}
	
	public Location getNearestCityLocation(Player p)
	{
		Location result = null;
		double distanceToBeat = 0;
		
		for (String c : citySpawns.keySet())
		{
			Location l = citySpawns.get(c);
			
			if (l.getWorld() != p.getWorld()) continue;
			
			if (result == null || l.distance(p.getLocation()) < distanceToBeat)
			{
				result = l;
				distanceToBeat = l.distance(p.getLocation());
			}
		}
		
		return result;
	}
	
	public void loadFile()
	{
		config = new NaxFile(plugin, "cities");
		
		Set<String> ids = config.getKeys("cities");
		if (ids == null || ids.isEmpty()) { return; }
		
		for (String id : ids)
		{
			String prefix = "cities." + id;
			World world = null;
			
			world = plugin.getServer().getWorld(config.getString(prefix + ".world"));
			System.out.println(config.getString(prefix + ".world") + " is not a world?");
			
			if (world == null)
			{
				continue;
			}
			else
			{
				Location l = new Location(
						world,
						config.getDouble(prefix + ".x"),
						config.getDouble(prefix + ".y"),
						config.getDouble(prefix + ".z")
						);
				
				citySpawns.put(id, l);
			}
		}
	}
	
	public class CityTeleporter implements Runnable
	{
		public Player p;
		public Location l;
		
		public CityTeleporter(Player p, Location l)
		{
			this.p = p;
			this.l = l;
		}
		
		public void run()
		{
			if (teleporting.containsKey(p) && teleporting.get(p))
			{
				p.teleport(l);
			}
			
			teleporting.remove(p);
		}
	}
}
