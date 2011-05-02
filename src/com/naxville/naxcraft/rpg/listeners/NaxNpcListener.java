package com.naxville.naxcraft.rpg.listeners;
/*
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityTargetEvent;

import com.naxville.naxcraft.Naxcraft;
import com.naxville.naxcraft.npcs.NaxcraftNpc;
import com.naxville.naxcraft.npcs.NaxcraftNpc.NpcInput;
*/
public class NaxNpcListener { //extends EntityListener {
	/*
	public static Naxcraft plugin;
	
	public NaxNpcListener(Naxcraft instance) {
		plugin = instance;
	}
	public void onEntityTarget(EntityTargetEvent event) {
		
        if (event instanceof NpcEntityTargetEvent) {
            NpcEntityTargetEvent nevent = (NpcEntityTargetEvent)event;

            NaxcraftNpc npc = plugin.npcCommand.npcs.getNpc(event.getEntity());

            if (npc != null && event.getTarget() instanceof Player) {
            	Player player = (Player)event.getTarget();      	
            	
                if (nevent.getNpcReason() == NpcTargetReason.CLOSEST_PLAYER) {
                    npc.respond(NpcInput.PROXIMITY, player, "");
                    event.setCancelled(true);

                } else if (nevent.getNpcReason() == NpcTargetReason.NPC_RIGHTCLICKED) {
                	npc.respond(NpcInput.RIGHT_CLICK, player, "");
                    event.setCancelled(true);
                    
                } else if (nevent.getNpcReason() == NpcTargetReason.NPC_BOUNCED) {
                	npc.respond(NpcInput.BOUNCE, player, "");
                    event.setCancelled(true);
                }
            }
        }

    }
	
	public void onEntityDamage (EntityDamageEvent event){	
		
		if(event instanceof EntityDamageByEntityEvent){
    		EntityDamageByEntityEvent event2 = (EntityDamageByEntityEvent)event;
	        if (event.getEntity() instanceof HumanEntity) {
	            NaxcraftNpc npc = plugin.npcCommand.npcs.getNpc(event.getEntity());
	            
	            if (npc != null && event2.getDamager() instanceof Player) {
	            	
	                Player p = (Player) event2.getDamager();
	                
	                if(plugin.superCommand.npcItems.containsKey(p.getName().toLowerCase())){
	                	if(plugin.superCommand.npcItems.get(p.getName().toLowerCase()).equals(p.getItemInHand().getType())){
	                		p.performCommand("npc delete " + npc.getUniqueId());
	                		return;
	                	}
	            	}
	                npc.respond(NpcInput.HURT, p, "");
	                
	                event.setCancelled(true);
	            }
	        }
    	}
	}
	*/
}