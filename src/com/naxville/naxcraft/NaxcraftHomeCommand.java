package com.naxville.naxcraft;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;

import com.naxville.naxcraft.autoareas.AutoBase;
import com.naxville.naxcraft.player.NaxPlayer;

public class NaxcraftHomeCommand {
	public static Naxcraft plugin;
	
	public NaxcraftHomeCommand(Naxcraft instance){
		plugin = instance;
	}

	public boolean runHomeCommand(CommandSender sender, String[] args){
		if(!(sender instanceof Player)){
			sender.sendMessage(Naxcraft.NOT_A_PLAYER);
			return true;
		}
		
		Player player = (Player) sender;
		Location home = player.getWorld().getSpawnLocation();
		
		NaxPlayer p = plugin.playerManager.getPlayer(player);
		if(p.homes != null && !p.homes.isEmpty() && p.homes.containsKey(player.getWorld()))
		{
			home = p.homes.get(player.getWorld());
		}
		
		if(!plugin.warpingPlayers.containsKey(player.getName()))
		{
			if(plugin.superCommand.isSuper(player.getName()))
			{
				player.teleport(player.getWorld().getSpawnLocation());
				return true;
			}
			
			int id = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new NaxcraftWarpRunnable(plugin, player, home), 100);
			if(id == -1)
			{
				player.sendMessage(Naxcraft.ERROR_COLOR + "Error: Teleport task request failed.");
				return false;
			} 
			else 
			{
				player.sendMessage(Naxcraft.COMMAND_COLOR + "You tap your shoes three times...");
				plugin.warpingPlayers.put(player.getName(), (Integer) id);
			}
		} 
		else
		{
			player.sendMessage(Naxcraft.COMMAND_COLOR + "Two places at once? Not a good plan...");
		}
		
		return true;
	}
	
	public boolean runSpawnCommand(Player player)
	{
		if(!plugin.warpingPlayers.containsKey(player.getName()))
		{
			if(plugin.superCommand.isSuper(player.getName()))
			{
				player.teleport(player.getWorld().getSpawnLocation());
				return true;
			}
			
			int id = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new NaxcraftWarpRunnable(plugin, player, player.getWorld().getSpawnLocation()), 100);
			if(id == -1)
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
	
	public boolean runSetHomeCommand(Player player)
	{
		AutoBase base = plugin.autoAreaManager.getBase(player);
		
		if(base == null || !plugin.autoAreaManager.isOwner(player, base))
		{
			player.sendMessage(Naxcraft.MSG_COLOR + "You can only set your home in an area you own!");
		}
		else
		{
			NaxPlayer p = plugin.playerManager.getPlayer(player);
			
			p.homes.put(player.getWorld(), player.getLocation());
			plugin.playerManager.savePlayer(p);
			
			player.sendMessage(Naxcraft.MSG_COLOR + "Your home has been set!");
		}
		
		return true;
	}

	public void warpPlayer(Player player, Location location) 
	{
		boolean okayToSpawn = false;
		
		int i = 0;
		while(!okayToSpawn)
		{
			if(location.getBlock().getType() != Material.AIR || location.getBlock().getRelative(0, 1, 0).getType() != Material.AIR)
			{
				location.setY(location.getY()+1);
			}
			else 
			{
				okayToSpawn = true;
			}
			
			i++;
			
			if(i > 130)
			{
				player.sendMessage(Naxcraft.ERROR_COLOR + "You can't teleport home because something is blocking your homepoint! Use /spawn");
				plugin.log.info("Player " + player.getName() + " attempted to go to /home, but I could not find a decent spawn point!");
				return;
			}
		}
		
		location.setX(location.getX()+.5);
		location.setZ(location.getZ()+.5);
		
		player.teleport(location);
	}
}