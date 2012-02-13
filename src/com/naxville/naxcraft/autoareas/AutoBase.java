package com.naxville.naxcraft.autoareas;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.naxville.naxcraft.Naxcraft;
import com.naxville.naxcraft.autoareas.AutoAreaManager.Flag;

public class AutoBase
{
	public AutoAreaManager autoAreaManager;
	public String id;
	public String owner;
	public List<String> otherOwners = new ArrayList<String>();
	public World world;
	public List<Point> chunks = new ArrayList<Point>();
	public Map<Flag, Boolean> flags = new HashMap<Flag, Boolean>();
	
	public AutoBase(AutoAreaManager autoAreaManager, String id, String owner, World world)
	{
		this.autoAreaManager = autoAreaManager;
		this.id = id;
		this.owner = owner;
		this.world = world;
		
		initiateFlags();
	}
	
	public AutoBase(AutoAreaManager autoAreaManager, String id, String owner, List<String> otherOwners, World world, List<Point> chunks, Map<Flag, Boolean> flags)
	{
		this.autoAreaManager = autoAreaManager;
		this.id = id;
		this.owner = owner;
		this.otherOwners = otherOwners;
		this.world = world;
		this.chunks = chunks;
		this.flags = flags;
		
		initiateFlags();
	}
	
	public void initiateFlags()
	{
		boolean resave = false;
		
		for (Flag f : Flag.values())
		{
			if (!flags.containsKey(f))
			{
				flags.put(f, false);
				
				resave = true;
			}
		}
		
		if (resave)
		{
			save();
		}
	}
	
	public boolean playerClose(Player player)
	{
		if (world.getName() != player.getWorld().getName()) { return false; }
		
		for (Point chunk : chunks)
		{
			Chunk c = world.getChunkAt(chunk.x, chunk.y);
			
			if (c.getBlock(8, 60, 8).getLocation().distance(player.getLocation()) < 400) { return true; }
		}
		return false;
	}
	
	public void showBorders(Player player)
	{
		for (Point chunk : chunks)
		{
			Chunk c = world.getChunkAt(chunk.x, chunk.y);
			for (int x = 0; x < 2; x++)
			{
				for (int z = 0; z < 2; z++)
				{
					for (int y = 0; y < 128; y += 20)
					{
						Block b = c.getBlock(x * 15, y, z * 15);
						
						if (b.getType() != Material.AIR) continue;
						
						if (autoAreaManager.isSuperOwner(player, this))
						{
							player.sendBlockChange(b.getLocation(), Material.GOLD_BLOCK, (byte) 0);
						}
						else if (autoAreaManager.isOwner(player, this))
						{
							player.sendBlockChange(b.getLocation(), Material.WOOL, (byte) 11);
						}
						else
						{
							player.sendBlockChange(b.getLocation(), Material.WOOL, (byte) 14);
						}
					}
				}
			}
		}
	}
	
	public void hideBorders(Player player)
	{
		for (Point chunk : chunks)
		{
			Chunk c = world.getChunkAt(chunk.x, chunk.y);
			for (int x = 0; x < 2; x++)
			{
				for (int z = 0; z < 2; z++)
				{
					for (int y = 0; y < 128; y += 20)
					{
						Block b = c.getBlock(x * 15, y, z * 15);
						
						if (b.getType() != Material.AIR) continue;
						
						player.sendBlockChange(b.getLocation(), b.getType(), b.getData());
					}
				}
			}
		}
	}
	
	public boolean toggleFlag(Flag flag)
	{
		flags.put(flag, !flags.get(flag));
		
		save();
		
		return flags.get(flag);
	}
	
	public boolean hasFlag(Flag flag)
	{
		return flags.get(flag);
	}
	
	public void save()
	{
		autoAreaManager.saveBase(this);
	}
	
	public String getBuilderNames()
	{
		String name = "";
		int i = 0;
		
		if (otherOwners != null && !otherOwners.isEmpty())
		{
			for (String otherOwner : otherOwners)
			{
				if (i > 0) name += Naxcraft.MSG_COLOR + ", ";
				
				name += autoAreaManager.plugin.getNickName(otherOwner);
				
				i++;
			}
		}
		
		if (i == 0) { return Naxcraft.DEFAULT_COLOR + "No Builders"; }
		
		return name;
	}
	
	public List<String> getOwners()
	{
		List<String> owners = new ArrayList<String>();
		
		if (otherOwners != null && !otherOwners.isEmpty())
		{
			for (String otherOwner : otherOwners)
			{
				owners.add(otherOwner);
			}
		}
		
		return owners;
	}
	
	public boolean isOwner(Player player)
	{
		return autoAreaManager.isOwner(player, this);
	}
	
	public String getFounderName()
	{
		return autoAreaManager.plugin.getNickName(owner);
	}
	
	public int getChunkMultiplier()
	{
		return (chunks.size() / 16) + 1;
	}
}
