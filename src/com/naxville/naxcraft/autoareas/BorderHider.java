package com.naxville.naxcraft.autoareas;

import org.bukkit.entity.Player;

public class BorderHider implements Runnable
{
	private Player player;
	private AutoBase base;
	
	public BorderHider(Player player, AutoBase base)
	{
		this.player = player;
		this.base = base;
	}
	
	public void run()
	{
		base.hideBorders(player);
	}
}
