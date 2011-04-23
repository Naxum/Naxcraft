package com.naxville.naxcraft.listeners;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.entity.CraftCreeper;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageByProjectileEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.ItemStack;

import com.naxville.naxcraft.Naxcraft;
import com.naxville.naxcraft.land.NaxcraftGroup;

public class NaxcraftEntityListener extends EntityListener {
	
	public static Naxcraft plugin;
	protected Map<Integer, Boolean> keepDrops;
	
	public NaxcraftEntityListener(Naxcraft instance) {
		plugin = instance;
		
		this.keepDrops = new HashMap<Integer, Boolean>();
	}
	
	public void onEntityExplode(EntityExplodeEvent event){
		
		if(event.getEntity() instanceof CraftCreeper){
			if(event.getLocation().getBlockY() > 62 || plugin.groupCommand.requestGroup(event.getEntity().getWorld(), event.getLocation()) != ""){
				event.setCancelled(true);
				return;
			}
			
		}
	}
	
	public void onEntityTarget(EntityTargetEvent event){
		if(event.getTarget() instanceof Player){
			if(plugin.stealthCommand.isInvisible((Player)event.getTarget())){
				event.setCancelled(true);
			}
		}
	}
	
	public void onEntityCombust(EntityCombustEvent event){
		if(event.getEntity() instanceof Player){
			Player player = (Player)event.getEntity();
			if(plugin.godCommand.godded(player.getName())){
				event.setCancelled(true);
				event.getEntity().setFireTicks(event.getEntity().getMaxFireTicks());
			}
		}
	}
	
	public void onEntityDamage (EntityDamageEvent event){			
		if(event.getEntity() instanceof Creature){
			Creature creature = (Creature)event.getEntity();
			
			if((creature.getHealth() - event.getDamage()) <= 0){
				if(event instanceof EntityDamageByEntityEvent){
					
					this.keepDrops.put(creature.getEntityId(), true);
					
				} else {
					String groupName = plugin.groupCommand.requestGroup(creature.getWorld(), creature.getLocation());
					if(groupName != ""){
						if(plugin.groupCommand.getList(event.getEntity().getWorld()).get(groupName).isFlag("grinder")){
							this.keepDrops.put(creature.getEntityId(), true);
						}
					}
				}
				
				return;
			}
		}
		if(event.getEntity() instanceof Player) {
			
			Player player = (Player)event.getEntity();
			
			if(plugin.godCommand.godded(player.getName())){
				event.setCancelled(true);
				return;
			}
			
			//are we getting hurt in a safe area?
			String groupCheck = plugin.groupCommand.requestGroup(player.getWorld(), player.getLocation());
			if(groupCheck != ""){
				NaxcraftGroup group = plugin.groupCommand.getList(player).get(groupCheck);
				
				if(group.isSanc()){
					//player.sendMessage(Naxcraft.MSG_COLOR + "You are protected by this sanctuary!");
					event.setCancelled(true);
					return;
				}
				
				if(group.isSafe()){
					if(group.isOwner(player.getName().toLowerCase())){ //only owners are safe!
						event.setCancelled(true);
						return;
					}
				}
				
				if(group.isFlag("pvp")){
					if(event instanceof EntityDamageByEntityEvent){
						EntityDamageByEntityEvent ev = (EntityDamageByEntityEvent)event;
						if(ev.getDamager() instanceof Player || ev.getDamager() instanceof Wolf){
							if(group.isOwner(player.getName().toLowerCase())){
								event.setCancelled(true);
								return;
							}
						}
					}
				}
				
				if(group.isFlag("nopvp")){
					if(event instanceof EntityDamageByEntityEvent){
						EntityDamageByEntityEvent ev = (EntityDamageByEntityEvent)event;
						if(ev.getDamager() instanceof Player || ev.getDamager() instanceof Wolf){
							event.setCancelled(true);
							return;
						}
					}
				}
			}
			
			if((player.getHealth() - event.getDamage()) <= 0){
				
				String cause = this.getKiller(event);
				
				if(plugin.deathMessage.containsKey(player)){
					return;
					
				}  else {
					plugin.deathMessage.put(player, plugin.getNickName(player.getName()) + Naxcraft.MSG_COLOR + " was killed by " + cause + Naxcraft.MSG_COLOR + "!");
				}
			}
			
			if(plugin.warpingPlayers.containsKey(player.getName()))
			{
				plugin.getServer().getScheduler().cancelTask(plugin.warpingPlayers.get(player.getName()));
				plugin.warpingPlayers.remove(player.getName());
				player.sendMessage(Naxcraft.ERROR_COLOR + "Way to get hurt while teleporting! I'm telling mom!");
			}
			
			if(plugin.regenValues.containsKey(player.getName()))
			{
				plugin.regenValues.put(player.getName(), 20);
			}
			
		} else return;
	}
	
	public void onEntityDeath(EntityDeathEvent event){
		if(event.getEntity() instanceof Creature){
			Creature creature = (Creature)event.getEntity();
			
			//if this creature was NOT killed by another entity, outside of a grinder, destroy its shit
			if(!this.keepDrops.containsKey(creature.getEntityId())){
				event.getDrops().clear();
				
			} else {
				this.keepDrops.remove(creature.getEntityId());
			}
			
			return;
		}
		
		if(event.getEntity() instanceof Player){
			Player player = (Player)event.getEntity();
			
			if(plugin.deathMessage.containsKey(player)){
				
				if(!plugin.deathMessage.get(player).equalsIgnoreCase("dead")){
					
					plugin.getServer().broadcastMessage(plugin.deathMessage.get(player));
					
					/*
					 * Thanks tombstone, I love you.
					 */
					
					Block signBlock = player.getWorld().getBlockAt(player.getLocation());
					
					if(signBlock.getType() == Material.AIR){
						signBlock.setType(Material.SIGN_POST);
			        	Sign sign = (Sign)signBlock.getState();	
			        	sign.setLine(0, ChatColor.DARK_GRAY + "RIP");
			        	sign.setLine(1, plugin.getNickName(player.getName().toLowerCase()));
			        	String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
			        	String time = new SimpleDateFormat("hh:mm a").format(new Date());
			        	sign.setLine(2, date); //TODO: "BY"
			        	sign.setLine(3, time); //TODO: NAME
			        	sign.update();
					}
					
					plugin.deathMessage.put(player, "dead");
				}
			}
			
			if(plugin.destroyDrops.containsKey(event.getEntity())){
				event.getDrops().clear();
				
				if(plugin.control.has(player, "drop")){
					event.getDrops().add(new ItemStack(Material.GOLDEN_APPLE, 1));
				}
			}
			
			if(plugin.control.has((Player) event.getEntity(), "modkit")){
				event.getDrops().remove(Material.GOLD_SWORD);
				event.getDrops().remove(Material.GOLD_PICKAXE);
				event.getDrops().remove(Material.GOLD_AXE);
				event.getDrops().remove(Material.GOLD_SPADE);
				event.getDrops().remove(Material.GOLD_HOE);
			}
		}
	}
	
	public String getKiller(EntityDamageEvent event){
		String cause = "... I dunno, really.";
		
		if(event instanceof EntityDamageByEntityEvent){
			
			EntityDamageByEntityEvent ev = (EntityDamageByEntityEvent)event;
            Entity e = ev.getDamager();
            
            if(e instanceof Player){		            	
            	String item = "";
            	if(((Player)e).getItemInHand().getType() != Material.AIR){
            		//TODO: FUNNIER THINGS
            		item += "a " + ((Player)e).getItemInHand().getType().toString().replace("_", " ").toLowerCase().replace("spade", "shovel");
            	} else {
            		//TODO: FUNNIER THINGS
            		item += "their bare hands";
            	}
            	cause = plugin.getNickName(((Player)e).getName()) + Naxcraft.MSG_COLOR + " with " + item;
            	
            } else {
            	//TODO: FUNNIER THINGS
            	
            	if(e instanceof Creature){
            		Creature c = (Creature)e;
            		if(c instanceof Skeleton){ 
	            		cause = "trying to steal bones";
	            		
	            	} else if (c instanceof Spider) {
	            		cause = "getting all silky";
	            		
	            	}  else if (c instanceof Ghast) {
	            		cause = "how the FUCK is there a Ghast here";
	            		
	            	} else if (c instanceof PigZombie){
	            		cause = "zombie pork";
	            		
	            	} else if (c instanceof Zombie) {
	            		cause = "donating his brain";
	            		
	            	} else if (c instanceof Slime){
	            		cause = "getting all slimey";
	            		
	            	} else if (c instanceof Wolf){
	            		cause = "playing fetch";
	            	}
            		
            	} 
            		
            }
			
		} else if (event instanceof EntityDamageByProjectileEvent){
			EntityDamageByProjectileEvent ev = 	(EntityDamageByProjectileEvent)event;
			String groupName = plugin.groupCommand.requestGroup(ev.getEntity().getWorld(), ev.getProjectile().getLocation());
			
			if(groupName != ""){
				
				NaxcraftGroup group = plugin.groupCommand.getList(event.getEntity().getWorld()).get(groupName);
				cause = "a trap owned by " + Naxcraft.DEFAULT_COLOR + group.getOwner();
				
			} else {
				cause = "in the wild by a dispenser";
			}
			
		} else {
			switch(event.getCause()){
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

}
