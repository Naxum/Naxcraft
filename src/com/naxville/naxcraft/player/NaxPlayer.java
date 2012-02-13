package com.naxville.naxcraft.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;

import com.naxville.naxcraft.NaxColor;
import com.naxville.naxcraft.Naxcraft;
import com.naxville.naxcraft.player.PlayerManager.PlayerRank;
import com.naxville.naxcraft.player.PlayerManager.Title;

public class NaxPlayer
{
	private Naxcraft plugin;
	public String name;
	public PlayerRank rank;
	public String displayName;
	public Map<String, Location> homes = new HashMap<String, Location>();
	public String setHome;
	public List<String> ignored;
	public List<Title> titles;
	public List<Title> hiddenTitles;
	
	public NaxPlayer(Naxcraft plugin, String name, PlayerRank rank, String displayName, Map<String, Location> homes, String setHome, List<String> ignored, List<Title> titles, List<Title> hiddenTitles)
	{
		this.plugin = plugin;
		this.name = name;
		this.rank = rank;
		this.displayName = displayName;
		this.homes = homes;
		this.setHome = setHome;
		this.ignored = ignored;
		this.titles = titles;
		this.hiddenTitles = hiddenTitles;
		
		boolean resave = false;
		
		if (titles == null)
		{
			titles = new ArrayList<Title>();
			resave = true;
		}
		
		if (hiddenTitles == null)
		{
			hiddenTitles = new ArrayList<Title>();
			resave = true;
		}
		
		if (resave)
		{
			save();
		}
		
	}
	
	public boolean isPatron()
	{
		if (titles == null)
		{
			titles = new ArrayList<Title>();
			save();
		}
		
		if (hiddenTitles == null)
		{
			hiddenTitles = new ArrayList<Title>();
			save();
		}
		
		if (titles != null && hiddenTitles == null) return titles.contains(Title.PATRON);
		else if (titles == null && hiddenTitles == null) return false;
		return titles.contains(Title.PATRON) || hiddenTitles.contains(Title.PATRON);
	}
	
	public boolean isSuperPatron()
	{
		if (titles != null && hiddenTitles == null) return titles.contains(Title.SPATRON);
		else if (titles == null && hiddenTitles == null) return false;
		return titles.contains(Title.SPATRON) || hiddenTitles.contains(Title.SPATRON);
	}
	
	public boolean isMasterOrPatron()
	{
		return titles.contains(Title.PATRON) || hiddenTitles.contains(Title.PATRON) || rank.getId() >= PlayerRank.MASTER.getId();
	}
	
	public boolean isMasterOrSuperPatron()
	{
		return titles.contains(Title.SPATRON) || hiddenTitles.contains(Title.SPATRON) || rank.getId() >= PlayerRank.MASTER.getId();
	}
	
	public boolean isDemiAdminOrPatron()
	{
		return titles.contains(Title.PATRON) || hiddenTitles.contains(Title.PATRON) || rank.isDemiAdmin();
	}
	
	public boolean isAdminOrPatron()
	{
		return titles.contains(Title.PATRON) || hiddenTitles.contains(Title.PATRON) || rank.isAdmin();
	}
	
	public boolean hasTitle(Title t)
	{
		return titles.contains(t) || hiddenTitles.contains(t);
	}
	
	public void awardTitle(Title t)
	{
		plugin.announcer.announce(getChatName() + " has earned the title " + t.getName() + "!");
		titles.add(t);
	}
	
	public void save()
	{
		plugin.playerManager.savePlayer(this);
	}
	
	public String getChatName()
	{
		if (rank == null) System.out.println("Rank is null for NaxPlayer: " + name);
		if (displayName == null) return rank.getPrefix() + NaxColor.WHITE + name;
		return rank.getPrefix() + displayName;
	}
	
	public List<Title> getTitles()
	{
		if (titles == null)
		{
			titles = new ArrayList<Title>();
			save();
		}
		
		return titles;
	}
	
}
