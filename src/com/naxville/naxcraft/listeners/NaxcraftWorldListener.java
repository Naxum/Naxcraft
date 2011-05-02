package com.naxville.naxcraft.listeners;

import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldListener;

import com.naxville.naxcraft.Naxcraft;
import com.naxville.naxcraft.npcs.NaxcraftNpc;

public class NaxcraftWorldListener extends WorldListener {
	public Naxcraft plugin;
	
	public NaxcraftWorldListener(Naxcraft instance){
		plugin = instance;
	}
	
	public void onChunkUnload(ChunkUnloadEvent event){
		
		for (String id : plugin.npcCommand.npcs.keySet()) {
			/*NaxcraftNpc npc = plugin.npcCommand.npcs.get(id);
			if (Math.abs(event.getChunk().getX() - npc.getLocation().getBlock().getChunk().getX()) > 1){
				continue;
			}
			
			if (Math.abs(event.getChunk().getZ() - npc.getLocation().getBlock().getChunk().getZ()) > 1){
				continue;
			}*/
			
			return;
		}
	}
	
	public void onChunkLoad(ChunkLoadEvent event){		
		for (String id : plugin.npcCommand.npcs.keySet()) {
			/*
			NaxcraftNpc npc = plugin.npcCommand.npcs.get(id);
			if (Math.abs(event.getChunk().getX() - npc.getLocation().getBlock().getChunk().getX()) > 1){
				continue;
			}
			
			if (Math.abs(event.getChunk().getZ() - npc.getLocation().getBlock().getChunk().getZ()) > 1){
				continue;
			}*/
		
			//npc.reload();
			return;
		}
	}
}
