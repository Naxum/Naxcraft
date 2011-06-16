package com.naxville.naxcraft.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;

import com.naxville.naxcraft.Naxcraft;
import com.naxville.naxcraft.land.NaxcraftGroup;

public class NaxcraftBlockListener extends BlockListener {
	public static Naxcraft plugin;
	
	public NaxcraftBlockListener(Naxcraft instance){
		plugin = instance;
	}
	
	public void onBlockPhysics(BlockPhysicsEvent event){
		if(event.isCancelled()) return;
		
		plugin.warpgateCommand.handlePhysics(event);
		
		if(event.getBlock().getType() == Material.TNT){
			event.setCancelled(true);
		}
	}
	
	public void onBlockIgnite(BlockIgniteEvent event){
		if (event.getCause() != IgniteCause.FLINT_AND_STEEL) 
		{
			event.setCancelled(true);
			return;
		}
		event.setCancelled(plugin.autoAreaManager.canInteract(event.getPlayer(), event.getBlock()));
		event.setCancelled(requestBuild(event.getPlayer(), event.getBlock().getLocation()));
	}
	
	public void onSignChange(SignChangeEvent event)
	{
		plugin.shopManager.handleSign(event);
	}
	
	public void onBlockDamage(BlockDamageEvent event)
	{
		if(event.isCancelled()) return;
		
		plugin.autoAreaManager.handleBlockDamage(event);
		
		if(event.isCancelled()) return;
		
		if(event.getBlock().getType() == Material.TNT){
			if(plugin.superCommand.isSuper(event.getPlayer().getName())){
				event.setCancelled(false);
				return;
			}
			
			String groupName = plugin.groupCommand.requestGroup(event.getBlock().getWorld(), event.getBlock().getLocation());
			if(groupName != ""){
				
				NaxcraftGroup group = plugin.groupCommand.groups.get(groupName);
				if(group.isOwner(event.getPlayer().getName())){
					if(group.isFlag("public") || group.isFlag("creative")){
						event.setCancelled(true);
						event.getPlayer().sendMessage(Naxcraft.MSG_COLOR + "You cannot detonate TNT in a public or creative area.");
					}
					
				} else {
					event.setCancelled(true);
					event.getPlayer().sendMessage(Naxcraft.MSG_COLOR + "You cannot detonate TNT another's area.");
				}
				
			} else {				
				event.setCancelled(true);
				event.getPlayer().sendMessage(Naxcraft.MSG_COLOR + "You cannot detonate TNT in the wilderness.");
			}
			
			return;
		}
	}
	
	public void onBlockBreak(BlockBreakEvent event){		
		if(event.isCancelled()) return;
		
		plugin.autoAreaManager.handleBlockBreak(event);
		
		if(event.isCancelled()) return;
		plugin.atmManager.handleBlockBreak(event);
		plugin.warpgateCommand.handleBreak(event);
		plugin.shopManager.handleBlockBreak(event);
		
		if(event.isCancelled()) return;
		
		if(requestBuild(event.getPlayer(), event.getBlock().getLocation())){
			event.setCancelled(true);
			return;
			
		}
		
		String group = plugin.groupCommand.requestGroup(event.getBlock().getWorld(), event.getBlock().getLocation()); 
		if(group != ""){
			NaxcraftGroup naxgroup = plugin.groupCommand.groups.get(group);
			if(naxgroup.isFlag("creative")){
				Player player = (Player)event.getPlayer();
				if(this.getBlockInstantBreak(player, event.getBlock())){
					event.setCancelled(true);
					event.getBlock().setType(Material.AIR);
				}
			}
		}
		if(event.isCancelled()) return;
	}
	
	public boolean getBlockInstantBreak(Player player, Block block){
		if(plugin.superCommand.isSuper(player.getName())){
			return true;
		}
		
		if(player.getItemInHand().getType().equals(Material.GOLD_AXE) || player.getItemInHand().getType().equals(Material.GOLD_HOE) || player.getItemInHand().getType().equals(Material.GOLD_SPADE)||
				player.getItemInHand().getType().equals(Material.GOLD_SWORD) || player.getItemInHand().getType().equals(Material.GOLD_PICKAXE)){
			return false;
		}
			
		
		if(block.getType().equals(Material.BED) || block.getType().equals(Material.BED_BLOCK) || block.getType().equals(Material.BEDROCK) || 
		   block.getType().equals(Material.CAKE) || block.getType().equals(Material.CAKE_BLOCK) || block.getType().equals(Material.IRON_DOOR_BLOCK) || 
		   block.getType().equals(Material.IRON_DOOR) || block.getType().equals(Material.LEVER) || block.getType().equals(Material.NOTE_BLOCK) || 
		   block.getType().equals(Material.SIGN) || block.getType().equals(Material.SIGN_POST) || block.getType().equals(Material.WALL_SIGN) || 
		   block.getType().equals(Material.WOOD_DOOR) || block.getType().equals(Material.WOODEN_DOOR)){
			   return false;
		}
		
		return true;
	}
	
	//@SuppressWarnings("deprecation")
	public void onBlockPlace(BlockPlaceEvent event)
	{
		if(event.isCancelled()) return;
		
		plugin.autoAreaManager.handleBlockPlace(event);
		plugin.mobCommand.handleBlockPlace(event);
		plugin.atmManager.handleBlockPlace(event);
		
		if(event.isCancelled()) return;
		
		if(requestBuild(event.getPlayer(), event.getBlock().getLocation())){
			event.setCancelled(true);
			return;
			
		} else {
			String group = plugin.groupCommand.requestGroup(event.getBlock().getWorld(), event.getBlock().getLocation()); 
			if(group != ""){
				NaxcraftGroup naxgroup = plugin.groupCommand.groups.get(group);
				if(naxgroup.isFlag("creative")){
					event.getPlayer().getItemInHand().setAmount(event.getPlayer().getItemInHand().getAmount()+1);
					//event.getPlayer().updateInventory();
					
					if(event.getBlock().getType() == Material.TNT && !plugin.superCommand.isSuper(event.getPlayer().getName())){
						event.setCancelled(true);
					}
				}
				
			} else {
				if(event.getBlock().getType() == Material.TNT && !plugin.superCommand.isSuper(event.getPlayer().getName())){
					
					event.getPlayer().sendMessage(Naxcraft.MSG_COLOR + "You may not place TNT outside of areas you own.");
					event.setCancelled(true);
				}
			}
		}
	}
	
	//TRUE = CANCELED, FALSE = ALLOWED TO BUILD
	public boolean requestBuild (Player player, Location location){		
		if(plugin.superCommand.isSuper(player.getName()) || !player.getWorld().getName().startsWith("old_naxville")){
			return false;
		}
		
		String group = plugin.groupCommand.requestGroup(player.getWorld(), location); 
		if(group != ""){
			NaxcraftGroup naxgroup = plugin.groupCommand.groups.get(group);
			
			if(naxgroup.isFlag("public")){
				return false;
			}
			
			if(naxgroup.isLock()){
				if(plugin.playerManager.getPlayer(player).rank.isAdmin()){
					return false;
					
				} else {
					player.sendMessage(Naxcraft.ADMIN_COLOR + "WARNING: This area is locked. Talk to an admin"); 
					return true;
				}
			}
			
			if(plugin.frozenPlayers.containsKey(player.getName())){
				return true;
			}
			
			if(naxgroup.isOwner(player.getName().toLowerCase())){
			
				return false;
				
			} else {
				
				if(naxgroup.isHurt()){
					String owners = naxgroup.getOwners();
					player.damage(2);
					player.sendMessage(Naxcraft.ERROR_COLOR + "This area is owned by " + owners + Naxcraft.ERROR_COLOR + ". If you attempt to build here you will die.");
					return true;
					
				} else {
					String owners = naxgroup.getOwner();
					player.sendMessage(Naxcraft.ERROR_COLOR + "This area is owned by " + Naxcraft.DEFAULT_COLOR + owners + Naxcraft.ERROR_COLOR + ". You may not build here.");
					return true;
				}
				
			}
			
		}
			
		
		return false;
	}
	
	public boolean requestBuild (Player player, Location location, Boolean no_messages){		
		if(plugin.superCommand.isSuper(player.getName())){
			return false;
		}
		
		String group = plugin.groupCommand.requestGroup(player.getWorld(), location); 
		if(group != ""){
			NaxcraftGroup naxgroup = plugin.groupCommand.groups.get(group);
			
			if(naxgroup.isFlag("public")){
				return false;
			}
			
			if(naxgroup.isLock()){
				if(plugin.playerManager.getPlayer(player).rank.isAdmin()){
					return false;
					
				} else {
					return true;
				}
			}
			
			if(plugin.frozenPlayers.containsKey(player.getName())){
				return true;
			}
			
			if(naxgroup.isOwner(player.getName().toLowerCase())){
			
				return false;
				
			} else {
				
				if(naxgroup.isHurt()){
					player.damage(2);
					return true;
					
				} else {
					return true;
				}
				
			}
			
		}
		return false;
	}
}
