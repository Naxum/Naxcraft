package com.naxville.naxcraft.rpg.listeners;

import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;

import com.naxville.naxcraft.Naxcraft;

public class NaxRpgBlockListener extends BlockListener {
	public Naxcraft plugin;
	
	public NaxRpgBlockListener (Naxcraft instance){
		plugin = instance;
	}
	
	public void onBlockDamage(BlockDamageEvent event){
		/*
		 * Chance of instant drop, increase break speed?
		 */
		if(event.isCancelled()) return;
		
		plugin.naxrpg.skills.handleBlockDamage(event);
	}
	
	public void onBlockPlace(BlockPlaceEvent event){
		/*
		 * Chance of getting a block back?
		 */
	}
	
	public void onBlockBreak(BlockBreakEvent event){
		/*
		 * Chance of dropping more blocks, durability not going down
		 */
		if(event.isCancelled()) return;
		plugin.naxrpg.shops.handleShopBreak(event);
		
		if(event.getPlayer().getWorld().getName() == plugin.rpgWorld){
			plugin.naxrpg.skills.handleBlockBreak(event);
		}
	}
	
	public void onSignChange(SignChangeEvent event){
		if(event.isCancelled()) return;
		plugin.naxrpg.shops.handleSign(event.getPlayer(), event);
	}
}
