package com.naxville.naxcraft.player;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.World;

import com.naxville.naxcraft.player.PlayerManager.PlayerRank;

public class NaxPlayer
{
	public String name;
	public PlayerRank rank;
	public String displayName;
	public Map<World, Location> homes = new HashMap<World, Location>();
	//TODO: Tickets
	//TODO: Mail
	
	public NaxPlayer(String name, PlayerRank rank, String displayName, Map<World, Location> homes)
	{
		this.name = name;
		this.rank = rank;
		this.displayName = displayName;
		this.homes = homes;
	}
}
