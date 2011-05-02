package com.naxville.naxcraft.npcs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class NpcList extends HashMap<String, NaxcraftNpc>{
	/**
	 *  For easy access to NaxcraftNpcs.
	 */
	private static final long serialVersionUID = 1L;

	public boolean containsBukkitEntity(Entity entity)
    {
        for(NaxcraftNpc bnpc : this.values())
        {
            //if(bnpc.getHuman().getBukkitEntity().getEntityId() == entity.getEntityId())
                return true;
        }

        return false;
    }

    public NaxcraftNpc getNpc(Entity entity)
    {
        for(NaxcraftNpc bnpc : this.values())
        {
            //if(bnpc.getHuman().getBukkitEntity().getEntityId() == entity.getEntityId())
                return bnpc;
        }

        return null;
    }
    
    public List<NaxcraftNpc> getNpcsWithinDistance(Player player, double distance){
    	Location loc1 = player.getLocation();
    	List<NaxcraftNpc> list = new ArrayList<NaxcraftNpc>();
    	/*
    	for(NaxcraftNpc npc : this.values()){
    		Location loc2 = npc.getLocation(); 
    		if(Math.ceil(Math.sqrt(Math.pow(loc1.getX() - loc2.getX(), 2) + Math.pow(loc1.getY() - loc2.getY(), 2) + Math.pow(loc1.getZ() - loc2.getZ(), 2))) <= distance){
    			list.add(npc);
    		}
    	}
    	*/
    	return list;
    }
}
