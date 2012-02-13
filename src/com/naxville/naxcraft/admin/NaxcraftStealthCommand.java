package com.naxville.naxcraft.admin;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.Packet20NamedEntitySpawn;
import net.minecraft.server.Packet29DestroyEntity;

import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.naxville.naxcraft.Naxcraft;

public class NaxcraftStealthCommand
{
	public Naxcraft plugin;
	protected List<String> invisiblePlayers = new ArrayList<String>();
	protected int spamCount = 0; // for not spamming the server for invis updates
	
	public NaxcraftStealthCommand(Naxcraft instance)
	{
		plugin = instance;
	}
	
	public boolean runCommand(CommandSender sender, String[] args)
	{
		
		if (!(sender instanceof Player)) return true;
		
		Player player = (Player) sender;
		
		if (!plugin.playerManager.getPlayer(player).rank.isDemiAdmin()) return true;
		
		if (args.length == 0)
		{
			if (isInvisible(player))
			{
				unhide(player);
				player.sendMessage(Naxcraft.MSG_COLOR + "You are now visible.");
				
			}
			else
			{
				hide(player);
				player.sendMessage(Naxcraft.ADMIN_COLOR + "You are now invisible!");
			}
		}
		else if (args.length == 1 && args[0].equalsIgnoreCase("quit"))
		{
			plugin.announcer.announce(plugin.playerManager.getDisplayName(player) + " has left the game.", player.getWorld());
		}
		else if (args.length == 1 && args[0].equalsIgnoreCase("join"))
		{
			plugin.announcer.announce(plugin.playerManager.getDisplayName(player) + " has logged into " + plugin.getWorldName(player.getWorld(), true) + ".");
		}
		
		return true;
	}
	
	public boolean isInvisible(Player player)
	{
		if (this.invisiblePlayers.isEmpty()) return false;
		
		if (this.invisiblePlayers.contains(player.getName().toLowerCase())) return true;
		
		return false;
	}
	
	public void addInvisible(Player player)
	{
		if (this.isInvisible(player)) return;
		
		this.invisiblePlayers.add(player.getName().toLowerCase());
		
	}
	
	public void removeInvisible(Player player)
	{
		if (this.isInvisible(player)) this.invisiblePlayers.remove(player.getName().toLowerCase());
		
	}
	
	public void handleJoin(PlayerJoinEvent event)
	{
		if (this.isInvisible(event.getPlayer()))
		{
			event.getPlayer().sendMessage(Naxcraft.ADMIN_COLOR + "You are invisible!");
			this.hideFromAll(event.getPlayer());
		}
		
		if (!this.invisiblePlayers.isEmpty())
		{
			for (String name : this.invisiblePlayers)
			{
				Player player = plugin.getServer().getPlayer(name);
				if (player != null)
				{
					hideFrom(player, event.getPlayer());
				}
			}
		}
	}
	
	public void handleTeleport(PlayerTeleportEvent event)
	{
		if (this.isInvisible(event.getPlayer()))
		{
			event.getPlayer().sendMessage(Naxcraft.ADMIN_COLOR + "You are invisible!");
			this.hideFromAll(event.getPlayer());
		}
		
		if (!this.invisiblePlayers.isEmpty())
		{
			for (String name : this.invisiblePlayers)
			{
				Player player = plugin.getServer().getPlayer(name);
				if (player != null)
				{
					hideFrom(player, event.getPlayer());
				}
			}
		}
	}
	
	protected void hide(Player player)
	{
		this.addInvisible(player);
		this.hideFromAll(player);
	}
	
	protected void unhide(Player player)
	{
		this.removeInvisible(player);
		this.unhideFromAll(player);
	}
	
	protected void hideFromAll(Player player)
	{
		for (Player other : plugin.getServer().getOnlinePlayers())
		{
			hideFrom(player, other);
		}
	}
	
	protected void unhideFromAll(Player player)
	{
		for (Player other : plugin.getServer().getOnlinePlayers())
		{
			unhideFrom(player, other);
		}
	}
	
	public void hideFrom(Player p1, Player p2)
	{
		if (p1.getName().equalsIgnoreCase(p2.getName())) return;
		if (this.isInvisible(p2)) return;
		
		CraftPlayer stealthed = (CraftPlayer) p1;
		CraftPlayer person = (CraftPlayer) p2;
		
		// person.getHandle().a.b(new Packet29DestroyEntity(stealthed.getEntityId())); //originally person.getHandle().a.b(new Packet());
		person.getHandle().netServerHandler.sendPacket(new Packet29DestroyEntity(stealthed.getEntityId()));
	}
	
	protected void unhideFrom(Player p1, Player p2)
	{
		if (p1.getName().equalsIgnoreCase(p2.getName())) return;
		
		CraftPlayer unstealthed = (CraftPlayer) p1;
		CraftPlayer person = (CraftPlayer) p2;
		
		// person.getHandle().a.b(new Packet20NamedEntitySpawn(unstealthed.getHandle()));
		person.getHandle().netServerHandler.sendPacket(new Packet20NamedEntitySpawn(unstealthed.getHandle()));
	}
	
	public void updateInvisibles()
	{
		this.spamCount++;
		
		if (this.spamCount != 4) return;
		
		this.spamCount = 0;
		
		if (!this.invisiblePlayers.isEmpty())
		{
			for (String name : this.invisiblePlayers)
			{
				Player player = plugin.getServer().getPlayer(name);
				
				if (player != null)
				{
					for (Player p2 : plugin.getServer().getOnlinePlayers())
					{
						if (player.equals(p2)) continue;
						hideFrom(player, p2);
					}
				}
			}
		}
		
		plugin.playerManager.updateIgnoredPlayers();
	}
}
