package com.naxville.naxcraft;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.entity.CraftCreature;
import org.bukkit.craftbukkit.entity.CraftPigZombie;
import org.bukkit.craftbukkit.entity.CraftSkeleton;
import org.bukkit.craftbukkit.entity.CraftSpider;
import org.bukkit.craftbukkit.entity.CraftWolf;
import org.bukkit.craftbukkit.entity.CraftZombie;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageByProjectileEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.naxville.naxcraft.autoareas.AutoBase;

public class Announcer 
{
	Naxcraft plugin;
	private Map<Player, String> killMessages = new HashMap<Player, String>();
	
	public Announcer(Naxcraft plugin)
	{
		this.plugin = plugin;
	}
	
	public void announce(String str)
	{
		plugin.getServer().broadcastMessage(str);
	}
	
	public void announce(String str, World world)
	{
		for(Player player : world.getPlayers())
		{
			player.sendMessage(str);
		}
	}

	public void handleEntityDamage(EntityDamageEvent event) 
	{
		if(!(event.getEntity() instanceof Player))
			return;
		
		Player player = (Player)event.getEntity();
		
		if(player.getHealth() - event.getDamage() <= 0)
		{
			killMessages.put(player, getKiller(event));
		}
	}
	
	public void handleEntityDeath(EntityDeathEvent event)
	{
		if(!(event.getEntity() instanceof Player))
			return;
		
		Player player = (Player)event.getEntity();
		
		if(killMessages.containsKey(player) && killMessages.get(player) != null)
		{
			if(killMessages.get(player) == "")
			{
				announce(plugin.playerManager.getDisplayName(player) + Naxcraft.MSG_COLOR + " has died!", player.getWorld());
			}
			else
			{
				announce(plugin.playerManager.getDisplayName(player) + Naxcraft.MSG_COLOR + " was killed by " + killMessages.get(player) + Naxcraft.MSG_COLOR + "!", player.getWorld());
			}
			killMessages.put(player, null);
		}
	}
	
	public String getKiller(EntityDamageEvent event){
		String cause = "";
		
		if(event instanceof EntityDamageByEntityEvent)
		{
			EntityDamageByEntityEvent ev = (EntityDamageByEntityEvent)event;
            Entity e = ev.getDamager();
            
            if(e instanceof Player)
            {		            	
            	String item = "";
            	if(((Player)e).getItemInHand().getType() != Material.AIR)
            	{
            		item += "a " + ((Player)e).getItemInHand().getType().toString().replace("_", " ").toLowerCase().replace("spade", "shovel");
            	} 
            	else 
            	{
            		item += "their bare hands";
            	}
            	cause = plugin.getNickName(((Player)e)) + Naxcraft.MSG_COLOR + " with " + item;
            	
            } 
            else 
            {
            	if(e instanceof CraftCreature)
            	{
            		CraftCreature c = (CraftCreature)e;
            		if(c instanceof CraftSkeleton)
            		{ 
	            		cause = "trying to steal bones";
	            	} 
            		else if (c instanceof CraftSpider) 
            		{
	            		cause = "getting all silky";
	            	}  
            		else if (c instanceof Ghast) 
            		{
	            		cause = "ghastsmack";
	            	} 
            		else if (c instanceof CraftPigZombie)
            		{
	            		cause = "zombie pork";
	            	} 
            		else if (c instanceof CraftZombie) 
            		{
	            		cause = "donating his brain";
	            	} 
            		else if (c instanceof Slime)
            		{
	            		cause = "getting all slimey";
	            	} 
            		else if (c instanceof CraftWolf)
            		{
	            		cause = "playing fetch";
	            	}
            	} 
            }
		} 
		else if (event instanceof EntityDamageByProjectileEvent)
		{
			EntityDamageByProjectileEvent ev = 	(EntityDamageByProjectileEvent)event;
			AutoBase base = plugin.autoAreaManager.getBase(ev.getDamager().getLocation().getBlock());
			
			if(base != null)
			{
				cause = "a trap owned by " + base.getFounderName();
			} 
			else 
			{
				cause = "in the wild by a dispenser";
			}
			
		} 
		else 
		{
			switch(event.getCause())
			{
				case BLOCK_EXPLOSION:
					cause = "cat smack";
					break;
				
				case CONTACT:
					cause = "snuggling with desert plants";
					break;
				
				case FIRE_TICK:
					cause = "roastin'";
					break;
					
				case SUFFOCATION:
					cause = "attempting a block transfusion";
					break;
				
				case DROWNING:
					cause = "failing at holding spacebar";
					break;
					
				case FALL:
					cause = "challenging virtual gravity";
					break;
					
				case LAVA:
					cause = "having skeleton legs";
					break;
					
				case FIRE:
					cause = "not walking into water";
					break;
				
				case ENTITY_EXPLOSION:
					cause = "chillin' with creepers";
					break;
					
				case LIGHTNING:
					cause = "having a shocking old time";
					break;
				
				case ENTITY_ATTACK:
					cause = "getting beaten up";
					break;
					
				case VOID:
					cause = "attempting to explore the great unknown";
					break;
				
				default:
					cause = event.getCause().toString().toLowerCase().replace("_", " ");
					break;
			}
		}
		
		return cause;
	}

	public void handlePlayerTeleport(PlayerTeleportEvent event) 
	{
		if(event.getFrom().getWorld().getName() != event.getTo().getWorld().getName())
		{
			plugin.getServer().broadcastMessage(plugin.getNickName(event.getPlayer()) + Naxcraft.MSG_COLOR + " is now in " + plugin.getWorldName(event.getTo().getWorld(), true));
		}
		
	}
}
