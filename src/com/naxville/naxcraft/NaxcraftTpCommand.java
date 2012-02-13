package com.naxville.naxcraft;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.naxville.naxcraft.admin.SuperManager;
import com.naxville.naxcraft.autoareas.AutoAreaManager.Flag;
import com.naxville.naxcraft.autoareas.AutoBase;
import com.naxville.naxcraft.player.NaxPlayer;

public class NaxcraftTpCommand
{
	public static Naxcraft plugin;
	// target, teleportee
	protected HashMap<String, String> permission = new HashMap<String, String>();
	
	public NaxcraftTpCommand(Naxcraft instance)
	{
		plugin = instance;
	}
	
	public boolean getPermission(CommandSender sender, String maybe)
	{
		if (this.permission.containsKey(((Player) sender).getName().toLowerCase()))
		{
			Player player = (Player) sender;
			Player target = plugin.getServer().getPlayer(this.permission.get(player.getName().toLowerCase()));
			
			if (maybe.equalsIgnoreCase("yes"))
			{
				target.sendMessage(plugin.playerManager.getDisplayName(player) + Naxcraft.SUCCESS_COLOR + " accepted" + Naxcraft.MSG_COLOR + " your request.");
				player.sendMessage(Naxcraft.MSG_COLOR + "You accepted " + plugin.playerManager.getDisplayName(target) + Naxcraft.MSG_COLOR + "'s request.");
				teleport(target, player);
				
			}
			else
			{
				target.sendMessage(plugin.playerManager.getDisplayName(player) + Naxcraft.ERROR_COLOR + " refused your request.");
				player.sendMessage(Naxcraft.MSG_COLOR + "You refused " + plugin.playerManager.getDisplayName(target) + Naxcraft.MSG_COLOR + "'s request.");
			}
			
			this.permission.remove(player.getName().toLowerCase());
		}
		else
		{
			sender.sendMessage(Naxcraft.MSG_COLOR + "No one asked for your permission.");
		}
		
		return true;
	}
	
	public boolean runTpCommand(CommandSender sender, String[] args)
	{
		
		if (!(sender instanceof Player))
		{
			sender.sendMessage(Naxcraft.NOT_A_PLAYER);
			return true;
		}
		
		Player player = (Player) sender;
		NaxPlayer np = plugin.playerManager.getPlayer(player);
		
		if (np.isDemiAdminOrPatron())
		{
			if (args.length == 0) return false;
			
			AutoBase base = plugin.autoAreaManager.getBase(player);
			
			if (base != null && base.hasFlag(Flag.JAIL) && !plugin.autoAreaManager.isOwner(player, base) && !SuperManager.isSuper(player))
			{
				player.sendMessage(Naxcraft.MSG_COLOR + "You cannot teleport out of a jail!");
				return true;
			}
			
			if (np.isPatron() && !np.rank.isDemiAdmin())
			{
				if (args.length == 1)
				{
					
					Player target = plugin.getServer().getPlayer(args[0]);
					
					if (target != null)
					{
						
						this.permission.put(target.getName().toLowerCase(), player.getName().toLowerCase());
						target.sendMessage(plugin.getNickName(player) + Naxcraft.COMMAND_COLOR + " wants to teleport to you. Type " + Naxcraft.DEFAULT_COLOR + "/yes" + Naxcraft.COMMAND_COLOR + " or " + Naxcraft.DEFAULT_COLOR + "/no" + Naxcraft.COMMAND_COLOR + " to give permission.");
						player.sendMessage(Naxcraft.MSG_COLOR + "You are asking " + plugin.getNickName(target.getName().toLowerCase()) + Naxcraft.MSG_COLOR + " for teleportation permission.");
						
					}
					else
					{
						player.sendMessage(Naxcraft.ERROR_COLOR + "Couldn't teleport to " + Naxcraft.DEFAULT_COLOR + args[0] + Naxcraft.ERROR_COLOR + ".");
					}
					
					return true;
				}
				return false;
			}
			
			if (args.length == 1)
			{
				String target = args[0];
				
				if (target.equalsIgnoreCase("*"))
				{
					player.sendMessage(Naxcraft.ERROR_COLOR + "Incorrect usage of wildchar *");
					return true;
					
				}
				else
				{
					if (!teleport(player, target))
					{
						player.sendMessage(Naxcraft.ERROR_COLOR + "Could not teleport to " + target + ".");
						return true;
					}
					else
					{
						player.sendMessage(Naxcraft.MSG_COLOR + "Teleported to " + target + " successfully!");
						return true;
					}
				}
				
			}
			else if (args.length == 2)
			{
				String target = args[0];
				String dest = args[1];
				
				if (dest.equalsIgnoreCase("*"))
				{
					player.sendMessage(Naxcraft.ERROR_COLOR + "Incorrect usage of wildchar *");
					return true;
				}
				else
				{
					if (!teleport(target, dest))
					{
						player.sendMessage(Naxcraft.ERROR_COLOR + "Could not teleport " + target + " to " + dest + ".");
						return true;
					}
					else
					{
						player.sendMessage(Naxcraft.MSG_COLOR + "Teleported to " + target + " successfully!");
						return true;
					}
				}
				
			}
			else if (args.length == 3)
			{
				
				Double tx = null;
				Double ty = null;
				Double tz = null;
				try
				{
					tx = Double.valueOf(args[0]);
					ty = Double.valueOf(args[1]);
					tz = Double.valueOf(args[2]);
				}
				catch (NumberFormatException ex)
				{
					player.sendMessage(Naxcraft.ERROR_COLOR + "Not valid coordinates!");
					return false;
				}
				teleport(player, tx, ty, tz);
				return true;
			}
		}
		else
		{
			sender.sendMessage(String.format(Naxcraft.PERMISSIONS_FAIL, "/tp"));
		}
		
		return true;
	}
	
	protected boolean teleport(final Player target, final String destName)
	{
		Player destination = plugin.getServer().getPlayer(destName);
		return teleport(target, destination);
	}
	
	protected boolean teleport(final String targetName, final Player destination)
	{
		Player target = plugin.getServer().getPlayer(targetName);
		return teleport(target, destination);
	}
	
	protected boolean teleport(final String targetName, final Location location)
	{
		Player target = plugin.getServer().getPlayer(targetName);
		return target.teleport(location);
	}
	
	protected boolean teleport(final String targetName, final String destName)
	{
		Player destination = plugin.getServer().getPlayer(destName);
		
		if (targetName.equalsIgnoreCase("*"))
		{
			Player[] players = plugin.getServer().getOnlinePlayers();
			for (Player target : players)
			{
				if (!target.equals(destination))
				{
					teleport(target, destination);
				}
			}
			return true;
		}
		else
		{
			Player target = plugin.getServer().getPlayer(targetName);
			return teleport(target, destination);
		}
	}
	
	protected boolean teleport(final Player target, final Player destination)
	{
		if ((target == null) || (destination == null)) return false;
		
		if (target.getWorld().equals(destination.getWorld()))
		{
			addLastLocation(target);
			target.teleport(destination.getLocation());
		}
		else
		{
			addLastLocation(target);
			target.teleport(new Location(destination.getWorld(), destination.getWorld().getSpawnLocation().getX(), destination.getWorld().getSpawnLocation().getY(), destination.getWorld().getSpawnLocation().getZ()));
			target.teleport(destination.getLocation());
		}
		return true;
	}
	
	public boolean teleport(final Player target, Double x, Double y, Double z)
	{
		World world = target instanceof Player ? ((Player) target).getWorld() : plugin.getServer().getWorlds().get(0);
		if (target == null) return false;
		
		Player player = target;
		addLastLocation(player);
		player.teleport(new Location(world, x, y, z));
		return true;
	}
	
	public void addLastLocation(Player player)
	{
		plugin.playerManager.setLastLocation(player);
	}
}
