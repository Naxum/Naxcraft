package com.naxville.naxcraft;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.naxville.naxcraft.admin.SuperManager;
import com.naxville.naxcraft.autoareas.AutoBase;
import com.naxville.naxcraft.player.NaxPlayer;

public class NaxcraftHomeCommand
{
	public static Naxcraft plugin;
	private String[] disallowed = { "add", "set", "remove", "help", "list" };
	
	public NaxcraftHomeCommand(Naxcraft instance)
	{
		plugin = instance;
	}
	
	public boolean runHomeCommand(CommandSender sender, String[] args)
	{
		if (!(sender instanceof Player))
		{
			sender.sendMessage(Naxcraft.NOT_A_PLAYER);
			return true;
		}
		
		Player player = (Player) sender;
		Location home = player.getWorld().getSpawnLocation();
		String selectedHome = "";
		
		NaxPlayer p = plugin.playerManager.getPlayer(player);
		
		if (args.length != 0)
		{
			if (args[0].equalsIgnoreCase("list"))
			{
				
				player.sendMessage(Naxcraft.MSG_COLOR + "----");
				String msg = "Your Homes: ";
				
				if (p.homes.size() > 0)
				{
					int i = 0;
					for (String s : p.homes.keySet())
					{
						if (i != 0) msg += ", ";
						msg += Naxcraft.DEFAULT_COLOR + s + Naxcraft.MSG_COLOR + " in " + plugin.getWorldName(p.homes.get(s).getWorld(), true);
						i++;
					}
				}
				else
				{
					msg = "You have no homes.";
				}
				
				player.sendMessage(NaxColor.MSG + msg);
				
				return true;
			}
			else if (args[0].equalsIgnoreCase("help"))
			{
				player.sendMessage(Naxcraft.MSG_COLOR + "----");
				player.sendMessage(Naxcraft.MSG_COLOR + "/home " + Naxcraft.DEFAULT_COLOR + "help" + Naxcraft.MSG_COLOR + ": Shows you this message.");
				player.sendMessage(Naxcraft.MSG_COLOR + "/home " + Naxcraft.DEFAULT_COLOR + "list" + Naxcraft.MSG_COLOR + ": Lists your homes.");
				player.sendMessage(Naxcraft.MSG_COLOR + "/home " + Naxcraft.DEFAULT_COLOR + "" + Naxcraft.MSG_COLOR + ": Teleports you to your sethome.");
				player.sendMessage(Naxcraft.MSG_COLOR + "/home " + Naxcraft.DEFAULT_COLOR + "<name>" + Naxcraft.MSG_COLOR + ": Teleports you to that home.");
				player.sendMessage(Naxcraft.MSG_COLOR + "/home " + Naxcraft.DEFAULT_COLOR + "set <name>" + Naxcraft.MSG_COLOR + ": Sets your default /home.");
				player.sendMessage(Naxcraft.MSG_COLOR + "/home " + Naxcraft.DEFAULT_COLOR + "add <name>" + Naxcraft.MSG_COLOR + ": Adds a new home at your location.");
				player.sendMessage(Naxcraft.MSG_COLOR + "/home " + Naxcraft.DEFAULT_COLOR + "remove <name>" + Naxcraft.MSG_COLOR + ": Removes a home.");
				
				return true;
			}
			else if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("set"))
			{
				if (args.length != 2)
				{
					player.sendMessage(Naxcraft.MSG_COLOR + "Add or Remove... what? Try /home add/remove <name>");
				}
				else
				{
					if (args[0].equalsIgnoreCase("add"))
					{
						if (p.homes == null)
						{
							p.homes = new HashMap<String, Location>();
						}
						
						if (getHomeCountInWorld(p, player) <= getHomeCount(p))
						{
							for (String s : disallowed)
							{
								if (args[1].equalsIgnoreCase(s))
								{
									player.sendMessage(Naxcraft.MSG_COLOR + "You may not name a home a command name. (set, add, remove, help, list)");
								}
							}
							
							if (p.homes.containsKey(args[1].toLowerCase()))
							{
								player.sendMessage(Naxcraft.MSG_COLOR + "You cannot have two homes with the same name.");
								return true;
							}
							else
							{
								AutoBase base = plugin.autoAreaManager.getBase(player);
								
								if (base != null && plugin.autoAreaManager.isOwner(player, base))
								{
									p.homes.put(args[1].toLowerCase(), player.getLocation());
									plugin.playerManager.savePlayer(p);
									player.sendMessage(Naxcraft.MSG_COLOR + "You successfully added " + Naxcraft.DEFAULT_COLOR + args[1].toLowerCase() + Naxcraft.MSG_COLOR + " to your homes!");
								}
								else
								{
									player.sendMessage(Naxcraft.MSG_COLOR + "You may only have homes in bases you own!");
								}
								return true;
							}
						}
						else
						{
							player.sendMessage(Naxcraft.MSG_COLOR + "Sorry, your rank can only have " + Naxcraft.DEFAULT_COLOR + getHomeCount(p) + Naxcraft.MSG_COLOR + " homes in each world.");
							return true;
						}
					}
					else if (args[0].equalsIgnoreCase("remove"))
					{
						if (p.homes.size() > 0)
						{
							if (p.homes.containsKey(args[1].toLowerCase()))
							{
								p.homes.remove(args[1].toLowerCase());
								plugin.playerManager.savePlayer(p);
								
								player.sendMessage(Naxcraft.MSG_COLOR + "You successfully removed your " + Naxcraft.DEFAULT_COLOR + args[1].toLowerCase() + Naxcraft.MSG_COLOR + " home.");
							}
							else
							{
								player.sendMessage(Naxcraft.MSG_COLOR + "You don't have a home named " + Naxcraft.DEFAULT_COLOR + args[1].toLowerCase() + Naxcraft.MSG_COLOR + ".");
							}
						}
						else
						{
							player.sendMessage(Naxcraft.MSG_COLOR + "You don't have any homes!");
						}
					}
					else if (args[0].equalsIgnoreCase("set"))
					{
						if (p.homes.size() > 0)
						{
							if (p.homes.containsKey(args[1].toLowerCase()))
							{
								if (p.setHome.equalsIgnoreCase(args[1]))
								{
									player.sendMessage(Naxcraft.MSG_COLOR + "That is already your set home!");
								}
								else
								{
									p.setHome = args[1].toLowerCase();
									plugin.playerManager.savePlayer(p);
									
									player.sendMessage(Naxcraft.MSG_COLOR + "New default home set to " + Naxcraft.DEFAULT_COLOR + args[1].toLowerCase() + Naxcraft.MSG_COLOR + ".");
								}
							}
							else
							{
								player.sendMessage(Naxcraft.MSG_COLOR + "You don't have a home named " + Naxcraft.DEFAULT_COLOR + args[1].toLowerCase() + Naxcraft.MSG_COLOR + ".");
							}
						}
						else
						{
							player.sendMessage(Naxcraft.MSG_COLOR + "You don't have any homes.");
						}
					}
				}
				
				return true;
			}
			else
			{
				if (p.homes.containsKey(args[0].toLowerCase()))
				{
					home = p.homes.get(args[0].toLowerCase());
					selectedHome = args[0].toLowerCase();
				}
				else
				{
					player.sendMessage(Naxcraft.MSG_COLOR + "You do not have a home by the name of " + Naxcraft.DEFAULT_COLOR + args[0].toLowerCase() + Naxcraft.MSG_COLOR + ".");
					return true;
				}
			}
		}
		else
		{
			if (p.setHome != null && p.setHome != "")
			{
				if (p.homes.containsKey(p.setHome))
				{
					home = p.homes.get(p.setHome);
					selectedHome = p.setHome;
				}
				else
				{
					player.sendMessage(Naxcraft.MSG_COLOR + "Your default /home has been deleted, set a new default /home. Use " + Naxcraft.DEFAULT_COLOR + "/home help" + Naxcraft.MSG_COLOR + " for more info.");
					return true;
				}
			}
			else
			{
				player.sendMessage(Naxcraft.MSG_COLOR + "You haven't set your default /home. Check " + Naxcraft.DEFAULT_COLOR + "/home help" + Naxcraft.MSG_COLOR + " for more info.");
				return true;
			}
		}
		
		if (home == null) { return true; }
		
		if (!plugin.warpingPlayers.containsKey(player.getName()))
		{
			if (SuperManager.isSuper(player))
			{
				player.teleport(player.getWorld().getSpawnLocation());
				return true;
			}
			
			int id = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new NaxcraftWarpRunnable(plugin, player, home), 100);
			if (id == -1)
			{
				player.sendMessage(Naxcraft.ERROR_COLOR + "Error: Teleport task request failed.");
				return false;
			}
			else
			{
				if (selectedHome != "")
				{
					player.sendMessage(Naxcraft.COMMAND_COLOR + "You concentrate on your " + Naxcraft.DEFAULT_COLOR + selectedHome + Naxcraft.COMMAND_COLOR + " home!");
				}
				else
				{
					player.sendMessage(Naxcraft.COMMAND_COLOR + "You concentrate on the spawn point!");
				}
				plugin.warpingPlayers.put(player.getName(), (Integer) id);
			}
		}
		else
		{
			player.sendMessage(Naxcraft.COMMAND_COLOR + "Two places at once? Not a good plan...");
		}
		
		return true;
	}
	
	private int getHomeCount(NaxPlayer p)
	{
		int amount = p.rank.getId() + 1;
		
		if (p.isPatron()) amount += 2;
		if (p.isSuperPatron()) amount += 3;
		
		return amount;
	}
	
	private int getHomeCountInWorld(NaxPlayer p, Player player)
	{
		int i = 0;
		
		for (Location l : p.homes.values())
		{
			if (l.getWorld() == player.getWorld())
			{
				i++;
			}
		}
		
		return i;
	}
	
	public boolean runSpawnCommand(Player player)
	{
		if (player.getWorld().getName().equalsIgnoreCase("varinius"))
		{
			player.sendMessage(NaxColor.MSG + "This command is not used in this world. Check out /city.");
			return true;
		}
		
		if (!plugin.warpingPlayers.containsKey(player.getName()))
		{
			if (SuperManager.isSuper(player))
			{
				player.teleport(player.getWorld().getSpawnLocation());
				return true;
			}
			
			int id = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new NaxcraftWarpRunnable(plugin, player, player.getWorld().getSpawnLocation()), 100);
			if (id == -1)
			{
				player.sendMessage(Naxcraft.ERROR_COLOR + "Error: Teleport task request failed.");
				return false;
			}
			else
			{
				player.sendMessage(Naxcraft.COMMAND_COLOR + "Respawning in 5...");
				plugin.warpingPlayers.put(player.getName(), (Integer) id);
			}
		}
		else
		{
			player.sendMessage(Naxcraft.COMMAND_COLOR + "Two places at once? Not a good plan...");
		}
		
		return true;
	}
	
	public void warpPlayer(Player player, Location location)
	{
		boolean okayToSpawn = false;
		
		int i = (int) location.getY();
		plugin.warpingPlayers.remove(player.getName());
		
		while (!okayToSpawn)
		{
			if (i >= 128)
			{
				player.sendMessage(Naxcraft.ERROR_COLOR + "You can't teleport home because something is blocking your homepoint! Use /spawn");
				plugin.log.info("Player " + player.getName() + " attempted to go to /home, but I could not find a decent spawn point!");
				return;
			}
			
			if (location.getBlock().getType() != Material.AIR || location.getBlock().getRelative(0, 1, 0).getType() != Material.AIR)
			{
				location.setY(location.getY() + 1);
			}
			else
			{
				okayToSpawn = true;
			}
			
			i++;
		}
		plugin.playerManager.setLastLocation(player);
		player.teleport(location);
	}
}
