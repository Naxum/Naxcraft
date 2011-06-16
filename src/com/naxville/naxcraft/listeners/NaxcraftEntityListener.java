package com.naxville.naxcraft.listeners;

import org.bukkit.Material;
import org.bukkit.craftbukkit.entity.CraftCreeper;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.ItemStack;

import com.naxville.naxcraft.Naxcraft;

public class NaxcraftEntityListener extends EntityListener {
	
	public static Naxcraft plugin;
	
	public NaxcraftEntityListener(Naxcraft instance) {
		plugin = instance;		
	}
	
	public void onEntityExplode(EntityExplodeEvent event){
		
		if(event.getEntity() instanceof CraftCreeper){
			if(event.getLocation().getBlockY() > 62 || plugin.autoAreaManager.getBase(event.getEntity().getLocation().getBlock()) != null){
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
	
	public void onEntityDamage (EntityDamageEvent event)
	{	
		if(event.getEntity() instanceof Player)
		{
			if(plugin.godCommand.godded(((Player)event.getEntity()).getName()))
			{
				event.setCancelled(true);
				return;
			}
		}
		
		plugin.autoAreaManager.handleEntityDamage(event);
		plugin.groupCommand.handleEntityDamage(event);
		plugin.announcer.handleEntityDamage(event);
		
		if(!event.isCancelled() && event.getEntity() instanceof Player)
			plugin.regenValues.put(((Player)event.getEntity()).getName(), 20);
	}
	
	public void onEntityDeath(EntityDeathEvent event){
		if(event.getEntity() instanceof Creature){
			Creature creature = (Creature)event.getEntity();
			
			//if this creature was NOT killed by another entity, outside of a grinder, destroy its shit
			if(!plugin.autoAreaManager.keepDrops.contains(creature.getEntityId()))
			{
				event.getDrops().clear();	
			} 
			else 
			{
				plugin.autoAreaManager.keepDrops.remove((Object)creature.getEntityId());
			}
			
			return;
		}
		
		if(event.getEntity() instanceof Player){
			Player player  = (Player)event.getEntity();
			
			plugin.announcer.handleEntityDeath(event);
			
			if(plugin.warpingPlayers.containsKey(player.getName()))
			{
				plugin.getServer().getScheduler().cancelTask(plugin.warpingPlayers.get(player.getName()));
				plugin.warpingPlayers.remove(player.getName());
				player.sendMessage(Naxcraft.ERROR_COLOR + "Way to get hurt while teleporting! I'm telling mom!");
			}
			
			if(plugin.regenValues.containsKey(player.getName()))
			{
				plugin.regenValues.put(player.getName(), 30);
			}
			
			if(plugin.destroyDrops.containsKey(player)){
				event.getDrops().clear();
				
				if(plugin.playerManager.getPlayer(player).rank.isAdmin()){
					event.getDrops().add(new ItemStack(Material.GOLDEN_APPLE, 1));
				}
			}
			
			if(plugin.playerManager.getPlayer(player).rank.isAdmin()){
				event.getDrops().remove(Material.GOLD_SWORD);
				event.getDrops().remove(Material.GOLD_PICKAXE);
				event.getDrops().remove(Material.GOLD_AXE);
				event.getDrops().remove(Material.GOLD_SPADE);
				event.getDrops().remove(Material.GOLD_HOE);
			}
		}
	}
}
