package com.naxville.naxcraft.listeners;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import com.naxville.naxcraft.Naxcraft;
import com.naxville.naxcraft.land.NaxcraftGroup;

public class NaxcraftBlockListener extends BlockListener {
	public static Naxcraft plugin;
	protected HashMap<Material, Boolean>tools = new HashMap<Material, Boolean>();
	
	public NaxcraftBlockListener(Naxcraft instance){
		plugin = instance;
		
		tools.put(Material.WOOD_AXE, true);
		tools.put(Material.WOOD_PICKAXE, true);
		tools.put(Material.WOOD_SPADE, true);
		tools.put(Material.WOOD_AXE, true);
		tools.put(Material.WOOD_HOE, true);
		
		tools.put(Material.STONE_AXE, true);
		tools.put(Material.STONE_PICKAXE, true);
		tools.put(Material.STONE_SPADE, true);
		tools.put(Material.STONE_AXE, true);
		tools.put(Material.STONE_HOE, true);
		
		tools.put(Material.IRON_AXE, true);
		tools.put(Material.IRON_PICKAXE, true);
		tools.put(Material.IRON_SPADE, true);
		tools.put(Material.IRON_AXE, true);
		tools.put(Material.IRON_HOE, true);
		
		tools.put(Material.DIAMOND_AXE, true);
		tools.put(Material.DIAMOND_PICKAXE, true);
		tools.put(Material.DIAMOND_SPADE, true);
		tools.put(Material.DIAMOND_AXE, true);
		tools.put(Material.DIAMOND_HOE, true);
		
		tools.put(Material.GOLD_AXE, true);
		tools.put(Material.GOLD_PICKAXE, true);
		tools.put(Material.GOLD_SPADE, true);
		tools.put(Material.GOLD_AXE, true);
		tools.put(Material.GOLD_HOE, true);
	}
	
	public void onBlockPhysics(BlockPhysicsEvent event){
		if(event.isCancelled()) return;
		
		plugin.warpgateCommand.handlePhysics(event);
		
		if(event.getBlock().getType() == Material.TNT){
			event.setCancelled(true);
		}
	}
	
	public void onBlockIgnite(BlockIgniteEvent event){
		if(plugin.stopFire){
			event.setCancelled(true);
			return;
			
		} else if (event.getCause() != IgniteCause.FLINT_AND_STEEL) {
			event.setCancelled(true);
			return;
		}
		
		event.setCancelled(requestBuild(event.getPlayer(), event.getBlock().getLocation()));
	}
	
	public void onBlockBurn(BlockBurnEvent event){
		if(plugin.stopFire){
			//event.setCancelled(true);
			//Cancelling this event will cause fire to burn indefinitely.
		}
	}
	
	public void onBlockDamage(BlockDamageEvent event){
		//TODO: PASSWORD PROTECTION OF CHESTS
		
		if(event.getBlock().getType() == Material.TNT){
			if(plugin.superCommand.isSuper(event.getPlayer().getName())){
				event.setCancelled(false);
				return;
			}
			
			String groupName = plugin.groupCommand.requestGroup(event.getBlock().getWorld(), event.getBlock().getLocation());
			if(groupName != ""){
				
				NaxcraftGroup group = plugin.groupCommand.getList(event.getBlock().getWorld()).get(groupName);
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
		
		if(!requestBuild(event.getPlayer(), event.getBlock().getLocation(), true)) {
			if(event.getBlock().getType() == Material.SPONGE){
				event.getBlock().setTypeId(0);
				return;
			}
		}
		
		if(!requestBuild(event.getPlayer(), event.getBlock().getLocation(), true)) {
			String group = plugin.groupCommand.requestGroup(event.getBlock().getWorld(), event.getBlock().getLocation()); 
			if(group != ""){
				NaxcraftGroup naxgroup = plugin.groupCommand.getList(event.getBlock().getWorld()).get(group);
				if(naxgroup.isFlag("creative")){					
					Player player = (Player)event.getPlayer();
					if(this.getBlockInstantBreak(player, event.getBlock())){
						event.setCancelled(true);
						event.getBlock().setType(Material.AIR);
					}
				}
			}
		}
	}
	
	public void onBlockBreak(BlockBreakEvent event){		
		if(event.isCancelled()) return;
		
		plugin.warpgateCommand.handleBreak(event);
		
		if(requestBuild(event.getPlayer(), event.getBlock().getLocation())){
			event.setCancelled(true);
			return;
			
		}
		
		String group = plugin.groupCommand.requestGroup(event.getBlock().getWorld(), event.getBlock().getLocation()); 
		if(group != ""){
			NaxcraftGroup naxgroup = plugin.groupCommand.getList(event.getBlock().getWorld()).get(group);
			if(naxgroup.isFlag("creative")){
				Player player = (Player)event.getPlayer();
				if(this.getBlockInstantBreak(player, event.getBlock())){
					event.setCancelled(true);
					event.getBlock().setType(Material.AIR);
				}
			}
		}
		if(event.isCancelled()) return;
			plugin.autoarea.handleBlockBreak(event);
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
	public void onBlockPlace(BlockPlaceEvent event){
		if(requestBuild(event.getPlayer(), event.getBlock().getLocation())){
			event.setCancelled(true);
			return;
			
		} else {
			String group = plugin.groupCommand.requestGroup(event.getBlock().getWorld(), event.getBlock().getLocation()); 
			if(group != ""){
				NaxcraftGroup naxgroup = plugin.groupCommand.getList(event.getBlock().getWorld()).get(group);
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
		
		if(event.isCancelled()) return;
		
		plugin.autoarea.handleBlockPlace(event);
	}
	
	//TRUE = CANCELED, FALSE = ALLOWED TO BUILD
	public boolean requestBuild (Player player, Location location){		
		if(plugin.superCommand.isSuper(player.getName())){
			return false;
		}
		
		String group = plugin.groupCommand.requestGroup(player.getWorld(), location); 
		if(group != ""){
			NaxcraftGroup naxgroup = plugin.groupCommand.getList(player.getWorld()).get(group);
			
			if(naxgroup.isFlag("public")){
				return false;
			}
			
			if(naxgroup.isLock()){
				if(plugin.control.has(player, "super")){
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
			
		} else {
			if(player.getWorld().getName().equals("world")){ //only the main world uses old areas.
				int area = plugin.areaCommand.withinOldSchoolArea(location);
				if(area != -1){
					
					if(plugin.areaCommand.isOldSchoolOwner(player.getName(), area)){
						
						player.sendMessage(Naxcraft.ADMIN_COLOR + "Please upgrade your area by talking to an Operator. (" + area + ")");
						return false;
						
					} else {
						player.sendMessage(Naxcraft.ERROR_COLOR + "Area " + Naxcraft.DEFAULT_COLOR + area + Naxcraft.ERROR_COLOR + " is owned by " + plugin.areaCommand.getOldSchoolOwners(area) + Naxcraft.ERROR_COLOR + ". You may not build here.");
						return true;
						
					}
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
			NaxcraftGroup naxgroup = plugin.groupCommand.getList(player.getWorld()).get(group);
			
			if(naxgroup.isFlag("public")){
				return false;
			}
			
			if(naxgroup.isLock()){
				if(plugin.control.has(player, "super")){
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
			
		} else {
			if(player.getWorld().getName().equals("world")){ //only the main world uses old areas.
				int area = plugin.areaCommand.withinOldSchoolArea(location);
				if(area != -1){
					
					if(plugin.areaCommand.isOldSchoolOwner(player.getName(), area)){
						return false;
						
					} else {
						return true;
						
					}
				}
			}
		}
		return false;
	}
	
	public boolean removeScaffolding(CommandSender sender){
		if(sender instanceof Player){
			
			Player player = (Player)sender;
			player.sendMessage(Naxcraft.MSG_COLOR + "No, you go delete them.");
			return true;
			/*
			String groupName = plugin.groupCommand.requestGroup(player.getWorld(), player.getLocation());
			if(groupName != ""){
				NaxcraftGroup group = plugin.groupCommand.getList(player).get(groupName);
				
				if(group.isFlag("creative")){
					if(!group.isFlag("public")){
						if(group.isOwner(player.getName()) || plugin.superCommand.superPlayers.containsKey(player.getName().toLowerCase())){
							player.sendMessage(Naxcraft.MSG_COLOR + "Removing scaffolding...");
							removeSponge(group);
							player.sendMessage(Naxcraft.SUCCESS_COLOR + "Scaffolding removed!");
							
						} else {
							player.sendMessage(Naxcraft.MSG_COLOR + "You do not own this area.");
						}
					} else {
						if(plugin.control.has(player, "super")){
							player.sendMessage(Naxcraft.MSG_COLOR + "Removing scaffolding...");
							removeSponge(group);
							player.sendMessage(Naxcraft.SUCCESS_COLOR + "Scaffolding removed!");
						} else {
							player.sendMessage(Naxcraft.MSG_COLOR + "You cannot remove the scaffolding from a public area.");
						}
					}
					
				} else {
					
				}
			} else {
				player.sendMessage(Naxcraft.MSG_COLOR + "You're not even in an area, let alone a creative one.");
			}
			*/
		} else {
			System.out.println("What the hell you're not a player.");
		}
		
		return true;
	}
	
	protected void removeSponge(NaxcraftGroup group){				
		for(Block block : plugin.groupCommand.getBlocks(group, 50)){
			if(block.getType() == Material.SPONGE){
				block.setType(Material.AIR);
			}
		}
	}
}
