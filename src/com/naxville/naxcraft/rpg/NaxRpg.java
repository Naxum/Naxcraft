package com.naxville.naxcraft.rpg;

import org.bukkit.Location;
//import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import com.naxville.naxcraft.Naxcraft;

public class NaxRpg {
	public Naxcraft plugin;
	public NaxInventory inv;
	
	public NaxShopManager shops = new NaxShopManager(this);
	public NaxSkillManager skills = new NaxSkillManager(this);
	
	public NaxRpg (Naxcraft instance){
		plugin = instance;
		
		inv = new NaxInventory(plugin);
	}

	public void handleAnnouncement(Player player, Location to) {
		if(!player.getWorld().getName().equalsIgnoreCase(to.getWorld().getName())){
			plugin.getServer().broadcastMessage(plugin.getNickName(player.getName().toLowerCase()) + Naxcraft.MSG_COLOR + " is now in " + Naxcraft.WORLD_COLOR + plugin.getWorldName(to.getWorld()) + Naxcraft.MSG_COLOR + "!");
			//player.sendMessage(Naxcraft.MSG_COLOR + "You may not bring you inventory to this world. It is waiting for your return.");
		}
	}

	public void handleJoin(Player player){
		this.inv.inWorld.put(player.getName().toLowerCase(), player.getWorld().getName().toLowerCase());
		player.sendMessage(Naxcraft.WORLD_COLOR + "Welcome to " + Naxcraft.DEFAULT_COLOR + plugin.getWorldName(player.getWorld()) + Naxcraft.WORLD_COLOR + "!");
	}

	public void handleCraftClick(PlayerInteractEvent event) {
		/*if(event.getClickedBlock().getType() == Material.WORKBENCH){
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new NaxCraftSchedule(plugin, event.getPlayer()), 5);
		}*/
	}
	
}
