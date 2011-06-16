package com.naxville.naxcraft.autoareas;

import org.bukkit.entity.Player;

public class BorderShower implements Runnable
{
	private Player player;
	private AutoBase base;
	
	public BorderShower(Player player, AutoBase base)
	{
		this.player = player;
		this.base = base;
	}
	
	public void run()
	{
		base.showBorders(player);
	}
}
