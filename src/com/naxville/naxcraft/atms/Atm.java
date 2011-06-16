package com.naxville.naxcraft.atms;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

public class Atm
{
	public String id;
	public String owner;
	public World world;
	public Location location;
	
	public boolean tax = false;
	public Material taxMaterial = null;
	public int taxAmount = 0;
	
	public Atm(String id, String owner, Location location)
	{
		this.id = id;
		this.owner = owner;
		this.location = location;
		this.world = location.getWorld();
	}
}
