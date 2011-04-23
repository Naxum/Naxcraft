package com.naxville.naxcraft.rpg;
/*
import net.minecraft.server.ContainerWorkbench;
import net.minecraft.server.CraftingManager;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.ItemStack;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.naxville.naxcraft.Naxcraft;
*/
public class NaxCraftSchedule{ //implements Runnable {
	/*private final Naxcraft plugin;
	private final Player player; //change to NaxPlayer when it's actually working.
	private final EntityPlayer entityPlayer;
	private ContainerWorkbench workBench;
	
	protected NaxCraftSchedule (Naxcraft instance, Player player){
		this.plugin = instance;
		this.player = player;
		this.entityPlayer = ((CraftPlayer) player).getHandle();
	}
	
	@Override
	public void run(){
		if(entityPlayer == null) return;
		if(entityPlayer.activeContainer == entityPlayer.defaultContainer) return;
		
		try {
			workBench = (ContainerWorkbench)(entityPlayer.activeContainer);
		} catch (Exception e){
			return;
		}
		
		ItemStack output = CraftingManager.a().a(workBench.a);
		if(output != null){
			int materialId = output.id;
			//int damage = output.damage;
			
			if(materialId == 54){
				output = null;
			}
			workBench.b.a(0, output);
		}
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new NaxCraftSchedule(plugin, player), 2);
	}*/
}
