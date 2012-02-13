package com.naxville.naxcraft;

import org.bukkit.ChatColor;

public enum NaxColor
{
	MSG(ChatColor.GRAY),
	ERR(ChatColor.RED),
	WHITE(ChatColor.WHITE),
	COMMAND(ChatColor.YELLOW),
	MAIL(ChatColor.GOLD),
	LOCAL(ChatColor.GREEN),
	MULTI(ChatColor.DARK_AQUA),
	DEMI_ADMIN(ChatColor.GOLD),
	CITY(ChatColor.GOLD),
	HUB(ChatColor.AQUA),
	PARTY(ChatColor.AQUA),
	ADMIN(ChatColor.LIGHT_PURPLE),
	LEVEL(ChatColor.AQUA),
	BLOODMOON(ChatColor.DARK_RED),
	NETHER(ChatColor.RED),
	THE_END(ChatColor.DARK_PURPLE),
	SHOP(ChatColor.GREEN);
	
	private ChatColor c;
	
	private NaxColor(ChatColor c)
	{
		this.c = c;
	}
	
	public String toString()
	{
		return "" + c;
	}
}
