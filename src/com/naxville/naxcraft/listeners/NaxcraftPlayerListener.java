package com.naxville.naxcraft.listeners;

import java.util.HashMap;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.PoweredMinecart;
import org.bukkit.entity.StorageMinecart;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

import com.naxville.naxcraft.NaxColor;
import com.naxville.naxcraft.Naxcraft;
import com.naxville.naxcraft.admin.SuperManager;
import com.naxville.naxcraft.autoareas.WorldBoundary;
import com.naxville.naxcraft.player.PlayerManager.Title;

public class NaxcraftPlayerListener extends PlayerListener
{
	
	protected HashMap<String, String> groupNote = new HashMap<String, String>();
	
	public static Naxcraft plugin;
	
	public NaxcraftPlayerListener(Naxcraft instance)
	{
		plugin = instance;
	}
	
	public void onPlayerChat(PlayerChatEvent event)
	{
		Player player = event.getPlayer();
		String[] message = event.getMessage().split(" ");
		
		plugin.chatCommand.runChatCommand(player, "", message);
		event.setCancelled(true);
	}
	
	public void onPlayerPortal(PlayerPortalEvent event)
	{
		if (event.getPlayer().getWorld().getName().equalsIgnoreCase("aether"))
		{
			event.getPlayer().sendMessage(NaxColor.MSG + "Not here.");
			event.setCancelled(true);
		}
		
		if (event.getTo().getWorld().getName().contains("the_end"))
		{
			event.getPlayer().sendMessage(NaxColor.MSG + "You think... you... are ready to challenge me? Your time will come, human. But not now.");
			event.setCancelled(true);
		}
	}
	
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		event.getPlayer().sendMessage(Naxcraft.MSG_COLOR + "MOTD:" + plugin.motd);
		
		event.setJoinMessage(null);
		
		plugin.playerManager.handlePlayerJoin(event);
		
		if (plugin.destroyDrops.containsKey(event.getPlayer()))
		{
			event.getPlayer().sendMessage(Naxcraft.ADMIN_COLOR + "Your drops will be deleted on death.");
		}
		
		if (plugin.godCommand.godded(event.getPlayer().getName()))
		{
			event.getPlayer().sendMessage(Naxcraft.ADMIN_COLOR + "You are in god mode.");
		}
		
		if (plugin.stealthCommand.isInvisible(event.getPlayer()))
		{
			event.getPlayer().sendMessage(Naxcraft.ADMIN_COLOR + "You are invisible.");
		}
		
		plugin.bloodMoonManager.handlePlayerJoin(event.getPlayer());
		
		plugin.stealthCommand.handleJoin(event);
		plugin.mailManager.handlePlayerJoin(event);
		
		event.getPlayer().setDisplayName(plugin.getNickName(event.getPlayer().getName().toLowerCase()));
		
	}
	
	public void onPlayerChangedWorld(PlayerChangedWorldEvent event)
	{
		plugin.playerManager.handlePlayerWorldChange(event);
	}
	
	public void onPlayerKick(PlayerKickEvent event)
	{
		String s = event.getReason().charAt(0) + "";
		String r = event.getReason().substring(1);
		event.setLeaveMessage(plugin.getNickName(event.getPlayer().getName()) + Naxcraft.MSG_COLOR + " was kicked: " + Naxcraft.DEFAULT_COLOR + s.toLowerCase() + r);
	}
	
	@Override
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		Player p = event.getPlayer();
		
		if (p.isInsideVehicle())
		{
			p.getVehicle().remove();
		}
		
		plugin.autoAreaManager.handlePlayerQuit(event);
		plugin.playerManager.handlePlayerQuit(event);
		plugin.mailManager.handlePlayerQuit(event);
		
		if (plugin.warpingPlayers.containsKey(event.getPlayer()))
		{
			plugin.warpingPlayers.remove(event.getPlayer());
		}
	}
	
	public void onPlayerRespawn(PlayerRespawnEvent event)
	{
		event.getPlayer().setDisplayName(plugin.getNickName(event.getPlayer().getName().toLowerCase()));
		
		if (event.isBedSpawn()) return;
		
		Location city = plugin.cityManager.getNearestCityLocation(event.getPlayer());
		
		if (city != null) event.setRespawnLocation(city);
	}
	
	@Override
	public void onPlayerTeleport(PlayerTeleportEvent event)
	{
		if (event.isCancelled()) return;
		
		CraftPlayer p = (CraftPlayer) event.getPlayer();
		if (event.getFrom().getWorld() != event.getTo().getWorld() && p.getHandle().activeContainer != p.getHandle().defaultContainer)
		{
			p.getHandle().activeContainer = p.getHandle().defaultContainer;
			event.setCancelled(true);
		}
		
		if (event.isCancelled()) return;
		
		plugin.playerManager.handlePlayerTeleport(event);
		plugin.stealthCommand.handleTeleport(event);
		plugin.autoAreaManager.handlePlayerTeleport(event);
		
		event.getPlayer().setDisplayName(plugin.getNickName(event.getPlayer().getName().toLowerCase()));
	}
	
	public void onPlayerPickupItem(PlayerPickupItemEvent event)
	{
		plugin.joinCommand.handleItemPickup(event);
		plugin.playerManager.partyManager.handlePlayerPickupItem(event);
		plugin.playerManager.handlePlayerPickupItem(event);
		plugin.displayShopManager.handlePlayerPickupItem(event);
		
		if (plugin.stealthCommand.isInvisible(event.getPlayer()))
		{
			event.setCancelled(true);
		}
	}
	
	public void onPlayerMove(PlayerMoveEvent event)
	{
		if (event.isCancelled()) return;
		
		plugin.playerManager.handlePlayerMove(event);
		
		plugin.warpgateCommand.handleMove(event);
		plugin.autoAreaManager.handlePlayerMove(event);
		WorldBoundary.handlePlayerMove(event);
		
		if (plugin.frozenPlayers.containsKey(event.getPlayer()))
		{
			if (event.getTo().getX() != event.getFrom().getX() || event.getTo().getY() != event.getFrom().getY() || event.getTo().getZ() != event.getFrom().getZ())
			{
				event.getPlayer().teleport(event.getFrom());
				event.setCancelled(true);
			}
		}
	}
	
	public void onPlayerDropItem(PlayerDropItemEvent event)
	{
		Player player = event.getPlayer();
		
		if (plugin.destroyDrops.containsKey(player))
		{
			event.getItemDrop().remove();
			return;
		}
		
		if (plugin.playerManager.getPlayer(player).rank.isAdmin())
		{
			if (event.getItemDrop().getItemStack().getType() == Material.WATCH)
			{
				event.getItemDrop().remove();
			}
			if (event.getItemDrop().getItemStack().getType() == Material.COMPASS)
			{
				event.getItemDrop().remove();
			}
		}
		
		plugin.playerManager.handlePlayerDropItem(event);
		plugin.vehicleListener.handlePlayerDropItem(event);
	}
	
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		plugin.atmManager.handlePlayerInteract(event);
		plugin.autoAreaManager.handlePlayerInteract(event);
		plugin.superCommand.handleRightClick(event);
		plugin.shopManager.handleShopClick(event);
		plugin.mailManager.handlePlayerInteract(event);
		plugin.playerManager.handlePlayerInteract(event);
		plugin.displayShopManager.handlePlayerInteract(event);
		
		/*
		if (event.hasItem() && event.getItem().getType() == Material.GOLDEN_APPLE)
		{
			Player p = event.getPlayer();
			// plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new GoldAppleChecker(p, event.getItem()), 40);
		}
		*/
	}
	
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event)
	{
		if (event.getRightClicked() instanceof StorageMinecart || event.getRightClicked() instanceof PoweredMinecart)
		{
			if (event.getPlayer().getGameMode() == GameMode.CREATIVE && !SuperManager.isSuper(event.getPlayer()))
			{
				event.getPlayer().sendMessage(NaxColor.MSG + "You can't use inventories in creative!");
				event.setCancelled(true);
			}
		}
	}
	
	public class GoldAppleChecker implements Runnable
	{
		private Player p;
		private ItemStack i;
		private int amount;
		
		public GoldAppleChecker(Player p, ItemStack i)
		{
			this.p = p;
			this.i = i;
			this.amount = i.getAmount();
		}
		
		public void run()
		{
			if (i == null || i.getAmount() != amount - 1) return;
			
			for (ItemStack i : p.getInventory().getContents())
			{
				if (i == null) continue;
				
				if (!i.getType().isBlock())
				{
					if (i.getDurability() != 0)
					{
						i.setDurability((short) 0);
					}
				}
			}
			
			for (ItemStack i : p.getInventory().getArmorContents())
			{
				if (i != null) i.setDurability((short) 0);
			}
			
			plugin.announcer.announce(plugin.getNickName(p) + "'s tools and armor are magically healed by a gold apple!", p.getWorld());
			
			if (!plugin.playerManager.getPlayer(p).hasTitle(Title.GOLD_APPLE))
			{
				plugin.playerManager.getPlayer(p).awardTitle(Title.GOLD_APPLE);
			}
		}
	}
}
