package com.naxville.naxcraft.rpg;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.inventory.ItemStack;

import com.naxville.naxcraft.Naxcraft;

public class NaxSkillManager {
	public NaxRpg rpg;
	
	public NaxSkillManager(NaxRpg rpg){
		this.rpg = rpg;
	}
	
	public void handleBlockBreak(BlockBreakEvent event){
		Random rand = new Random();
		if(rand.nextInt(5) == 1){ //0-4
			rpg.plugin.getServer().getWorld(rpg.plugin.rpgWorld).dropItem(event.getBlock().getLocation(), new ItemStack(Material.GLASS, 5));
			event.getBlock().setTypeId(0);
			
			event.getPlayer().sendMessage(Naxcraft.MSG_COLOR + "Well aren't you lucky?");
		}
	}
	
	public void handleBlockDamage(BlockDamageEvent event){
		Random rand = new Random();
		if(event.getPlayer().getItemInHand().getType() == Material.GOLD_SPADE){
			if(rand.nextInt(5) == 1){ //0-4
				event.setInstaBreak(true);
				//this.handleBlockBreak(new BlockBreakEvent(event.getBlock(), event.getPlayer()));
			}
		}
	}

}
