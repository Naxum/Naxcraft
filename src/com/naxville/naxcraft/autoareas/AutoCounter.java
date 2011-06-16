package com.naxville.naxcraft.autoareas;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.naxville.naxcraft.Naxcraft;

public class AutoCounter 
{
	public Player player;
	public List<Chunk> chunks = new ArrayList<Chunk>();
	public int blocks = 0;
	public World world;
	public int askPercentage = 0;
	public long lastBlockPlace = System.currentTimeMillis();
	public boolean shutup = false;
	
	public static final int TIMEOUT = 5 * 1000 * 60;
	public static final Material AREA_MATERIAL = Material.IRON_INGOT;
	public static final int AREA_AMOUNT = 32;
	
	public AutoCounter(Player player)
	{
		this.player = player;
		this.world = player.getWorld();
	}
	
	public void count(Block block)
	{
		if(shutup) return;
		checkWorld();
		checkTime();
		
		if(chunks.isEmpty())
		{
			chunks.add(block.getChunk());
		}
		else
		{
			if(withinCountedChunk(block.getChunk()))
			{
				chunks.add(block.getChunk());
				askPercentage += 10;
			}
			else if(isChunkNearby(block.getChunk()))
			{
				chunks.add(block.getChunk());
				askPercentage += 5;
			}
			else
			{
				reset();
				return;
			}
		}
		
		blocks++;
		lastBlockPlace = System.currentTimeMillis();
		
		if(askPercentage >= 100)
		{
			player.sendMessage(Naxcraft.MSG_COLOR + "You have been building in the wilderness for a while."); 
			player.sendMessage(Naxcraft.MSG_COLOR + "Would you like to buy the chunks you have been building in?"); 
			player.sendMessage(Naxcraft.MSG_COLOR + "Use the " + Naxcraft.DEFAULT_COLOR + "/property" + Naxcraft.MSG_COLOR + " command for more info.");
			askPercentage = 0;
		}
	}
	
	public void checkTime()
	{
		long now = System.currentTimeMillis();
		if(now - lastBlockPlace >= TIMEOUT)
		{
			reset();
		}
	}
	
	public void checkWorld()
	{
		if(player.getWorld().getName() != world.getName())
		{
			reset();
		}
	}
	
	public void reset()
	{
		chunks.clear();
		blocks = 0;
		askPercentage = 0;
	}
	
	public boolean withinCountedChunk(Chunk chunk)
	{
		for(Chunk c : chunks)
		{
			if(c.getX() == chunk.getX() && c.getZ() == chunk.getZ())
				return true;
		}
		
		return false;
	}
	
	public boolean isChunkNearby(Chunk chunk)
	{
		for(Chunk c : chunks)
		{
			if(chunk.getX() == c.getX() - 1 && chunk.getZ() == c.getZ() - 1) continue;
			if(chunk.getX() == c.getX() + 1 && chunk.getZ() == c.getZ() + 1) continue;
			if(chunk.getX() == c.getX() + 1 && chunk.getZ() == c.getZ() - 1) continue;
			if(chunk.getX() == c.getX() - 1 && chunk.getZ() == c.getZ() + 1) continue;
			
			if(chunk.getX() >= c.getX() - 1 && chunk.getX() <= c.getX() + 1)
			{
				if(chunk.getZ() >= c.getZ() - 1 && chunk.getZ() <= c.getZ() + 1)
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	public void addChunk(Chunk chunk)
	{
		if(!chunks.contains(chunk))
		{
			chunks.add(chunk);
		}
	}
}
