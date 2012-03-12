package com.naxville.naxcraft.admin;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;

import com.naxville.naxcraft.Naxcraft;

public class MobCommand
{
	public Naxcraft plugin;
	public Map<Player, EntityType> list = new HashMap<Player, EntityType>();
	
	public MobCommand(Naxcraft plugin)
	{
		this.plugin = plugin;
	}
	
	public boolean runCommand(Player player, String args[])
	{
		if (args.length == 0)
		{
			list.put(player, EntityType.PIG);
		}
		else
		{
			EntityType type = null;
			try
			{
				type = EntityType.valueOf(args[0].toUpperCase());
			}
			catch (Exception e)
			{
			}
			
			if (type == null)
			{
				player.sendMessage(Naxcraft.ERROR_COLOR + "Error: " + args[0] + " is not a creature type.");
				String types = "Types: ";
				
				for (EntityType i : EntityType.values())
				{
					types += i.toString().toLowerCase() + ", ";
				}
				
				player.sendMessage(Naxcraft.COMMAND_COLOR + types);
				return true;
			}
			else
			{
				if (type == EntityType.WOLF || type == EntityType.GHAST || type == EntityType.GIANT)
				{
					if (!SuperManager.isSuper(player))
					{
						player.sendMessage(Naxcraft.MSG_COLOR + "That mob type is only for admins.");
						return true;
					}
				}
				
				player.sendMessage(Naxcraft.SUCCESS_COLOR + "Any mob spawners you place will now spawn " + Naxcraft.DEFAULT_COLOR + type.toString());
				list.put(player, type);
			}
		}
		
		return true;
	}
	
	public void handleBlockPlace(BlockPlaceEvent e)
	{
		if (e.isCancelled()) return;
		
		if (e.getBlock().getType() != Material.MOB_SPAWNER)
		{
			return;
		}
		else
		{
			if (list.containsKey(e.getPlayer()))
			{
				CreatureSpawner x = (CreatureSpawner) e.getBlock().getState();
				x.setSpawnedType(list.get(e.getPlayer()));
				e.getPlayer().sendMessage(Naxcraft.MSG_COLOR + "Creature Spawner changed to " + Naxcraft.DEFAULT_COLOR + x.getSpawnedType().toString() + Naxcraft.MSG_COLOR + " spawner.");
			}
		}
	}
}
