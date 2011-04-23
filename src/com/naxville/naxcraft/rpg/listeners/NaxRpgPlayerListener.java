package com.naxville.naxcraft.rpg.listeners;

import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerInventoryEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.naxville.naxcraft.Naxcraft;

public class NaxRpgPlayerListener extends PlayerListener {
	public Naxcraft plugin;
	
	public NaxRpgPlayerListener (Naxcraft instance){
		plugin = instance;
	}
	
	public void onPlayerTeleport(PlayerTeleportEvent event){
		if(event.isCancelled()) return;
		
		plugin.naxrpg.inv.handleInventory(event.getPlayer(), event.getTo().getWorld().getName().toLowerCase());
		plugin.naxrpg.handleAnnouncement(event.getPlayer(), event.getTo());
	}
	
	public void onPlayerJoin(PlayerJoinEvent event){
		plugin.naxrpg.handleJoin(event.getPlayer());
	}
	
	public void onPlayerHeldChange(PlayerItemHeldEvent event){
		/*
		 * This is for scrolling the wheel. Who cares?
		 */
	}
	
	public void onPlayerQuit(PlayerEvent event){
		/*
		 * See you later? I dunno.
		 */
	}
	
	public void onPlayerMove(PlayerMoveEvent event){
		/*
		 * Count movement? Increase speed?
		 */
	}
	
	public void onPlayerRespawn (PlayerRespawnEvent event){
		/*
		 * Give kit?
		 */
	}
	
	public void onPlayerInteract(PlayerInteractEvent event){
		/*
		 * Lots of possibilities here
		 */
		if(event.isCancelled()) return;
		
		if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
			plugin.naxrpg.shops.handleShopClick(event);
			
			if(event.getPlayer().getWorld().getName() == plugin.rpgWorld)
				plugin.naxrpg.handleCraftClick(event);
		}
	}
	
	public void onPlayerEggThrow(PlayerEggThrowEvent event){
		/*
		 * Make thing?
		 */
	}
	
	public void onPlayerInventoryOpen(PlayerInventoryEvent event){
		/*
		 * Check what they're making
		 */
		plugin.log.info("Inventory Opened!");
	}
	
	public void onPlayerDropItem(PlayerDropItemEvent event){
		/*
		 * Drop more/less?
		 */
	}
	
	public void onPlayerPickupItem(PlayerPickupItemEvent event){
		/*
		 * Not entirely sure, really. Maybe for checking stack size?
		 */
	}
	
	public void onPlayerBucketEmpty (PlayerBucketEmptyEvent event){
		/*
		 * Drinking!
		 */
	}
}
