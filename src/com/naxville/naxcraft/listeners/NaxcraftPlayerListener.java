package com.naxville.naxcraft.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.craftbukkit.block.CraftDispenser;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

import com.naxville.naxcraft.Naxcraft;
import com.naxville.naxcraft.NaxcraftClan;
import com.naxville.naxcraft.land.NaxcraftGroup;

public class NaxcraftPlayerListener extends PlayerListener {
	
	protected HashMap<String, String> groupNote = new HashMap<String, String>();
	
	public static Naxcraft plugin;
	
	public NaxcraftPlayerListener(Naxcraft instance){
		plugin = instance;
	}
	
	public void onPlayerChat(PlayerChatEvent event){
		Player player = event.getPlayer();
		String[] message = event.getMessage().split(" ");
		
		plugin.chatCommand.runChatCommand(player, "", message);
		event.setCancelled(true);
	}

	@Override
	public void onPlayerJoin (PlayerJoinEvent event){
		event.getPlayer().sendMessage(Naxcraft.ADMIN_COLOR + "MOTD:" + plugin.motd);
		event.setJoinMessage(plugin.getNickName(event.getPlayer().getName()) + Naxcraft.MSG_COLOR + " has joined " + Naxcraft.WORLD_COLOR + plugin.getWorldName(event.getPlayer().getLocation().getWorld()));
		NaxcraftClan clan = plugin.clanCommand.getPlayerClan(event.getPlayer());
		if (clan != null){
			event.getPlayer().sendMessage(clan.getMOTD());
		}
		
		if(plugin.destroyDrops.containsKey(event.getPlayer())){
			event.getPlayer().sendMessage(Naxcraft.ADMIN_COLOR + "Your drops will be deleted on death.");
		}
		
		if(plugin.godCommand.godded(event.getPlayer().getName())){
			event.getPlayer().sendMessage(Naxcraft.ADMIN_COLOR + "You are in god mode.");
		}
		
		plugin.stealthCommand.handleJoin(event);
		
		event.getPlayer().setDisplayName(plugin.getNickName(event.getPlayer().getName().toLowerCase()));
		
	}
	
	@Override
	public void onPlayerQuit (PlayerQuitEvent event){
		event.setQuitMessage(plugin.getNickName(event.getPlayer().getName().toLowerCase()) + Naxcraft.MSG_COLOR + " has left the game.");
	}
	
	public void onPlayerRespawn (PlayerRespawnEvent event){
		Player player = event.getPlayer();
		if(plugin.deathMessage.containsKey(player)){
			
			if(plugin.deathMessage.get(player).equals("dead")){
				plugin.deathMessage.remove(player);
			}
		}
		event.getPlayer().setDisplayName(plugin.getNickName(event.getPlayer().getName().toLowerCase()));
	}
	
	@Override
	public void onPlayerTeleport(PlayerTeleportEvent event){
		if(event.isCancelled()) return;
		
		//plugin.stealthCommand.handleTeleport(event);
		event.getPlayer().setDisplayName(plugin.getNickName(event.getPlayer().getName().toLowerCase()));
	}
	
	public void onPlayerMove(PlayerMoveEvent event){
		if(event.isCancelled()) return;
		
		plugin.warpgateCommand.handleMove(event);
		
		if(plugin.frozenPlayers.containsKey(event.getPlayer())){
			if(event.getTo().getX() != event.getFrom().getX() || event.getTo().getY() != event.getFrom().getY() || event.getTo().getZ() != event.getFrom().getZ()){
				event.getPlayer().teleport(event.getFrom());
				event.setCancelled(true);
			}
		}
		
		Player player = event.getPlayer();
		String groupName = plugin.groupCommand.requestGroup(player.getWorld(), player.getLocation()); 
		if(groupName != ""){
			NaxcraftGroup group = plugin.groupCommand.getList(player).get(groupName); 
			if(this.groupNote.containsKey(player.getName())){
				if(this.groupNote.get(player.getName()).equalsIgnoreCase(group.getName())){
					return;
				}
			}
			
			if(player.getInventory().contains(Material.SPONGE)){
				//player.sendMessage(Naxcraft.MSG_COLOR + "Your sponge has been removed.");
				player.getInventory().remove(Material.SPONGE);
			}
			
			List<String> extras = new ArrayList<String>();
			
			String safety = "";
			String owner = "";
			String message = ""; 
			if(group.isFlag("pvp")) safety = "%s are protected from " + Naxcraft.ERROR_COLOR + "PvP damage" + Naxcraft.MSG_COLOR + " here.";
			if(group.isSafe()) safety = "%s are protected from " + Naxcraft.ERROR_COLOR + "all damage" + Naxcraft.MSG_COLOR + " here.";
			
			if(group.isFlag("nopvp")){
				if(safety != "") safety += " ";
				safety += "Everyone is protected from " + Naxcraft.ERROR_COLOR + "PvP damage" + Naxcraft.MSG_COLOR + " here.";
			}
			if(group.isSanc()) safety = "This is a " + Naxcraft.ADMIN_COLOR + "sanctuary" + Naxcraft.MSG_COLOR + ", no one may be harmed here.";
			
			if(group.isFlag("jail")) extras.add("This is a jail, you may not warp out of here.");
			
			if(group.isFlag("public")){
				owner = "";
				message = Naxcraft.MSG_COLOR + "You are now in a " + Naxcraft.SUCCESS_COLOR + "public" + Naxcraft.MSG_COLOR + " area.";
				
				if(group.isFlag("creative")) {
					extras.add("This is a " + Naxcraft.ADMIN_COLOR + "creative" + Naxcraft.MSG_COLOR + " area for everyone!");
					player.getInventory().addItem(new ItemStack(Material.SPONGE, 1));
				}
				
				if(group.isLock()){
					extras.add("This area is " + Naxcraft.ADMIN_COLOR + "locked" + Naxcraft.MSG_COLOR + " and can only be unlocked by an admin."); 
				}

			} else {
				if(!group.isOwner(player.getName())){
					message = Naxcraft.MSG_COLOR + "You are now in land owned by " + Naxcraft.DEFAULT_COLOR + group.getOwner() + Naxcraft.MSG_COLOR + ".";
					owner = "The owners";
					
					if(group.isFlag("creative")) {
						extras.add("This is a " + Naxcraft.ADMIN_COLOR + "creative" + Naxcraft.MSG_COLOR + " area, the owner has infinite blocks.");
						
						if(plugin.superCommand.superPlayers.containsKey(player.getName().toLowerCase())) {
							player.getInventory().addItem(new ItemStack(Material.SPONGE, 1));
						}
					}
					
				} else {
					message = Naxcraft.MSG_COLOR + "You are in an area you own.";
					owner = "You";
					
					if(group.isFlag("creative")) {
						extras.add("You get infinite stuff, this is a " + Naxcraft.ADMIN_COLOR + "creative" + Naxcraft.MSG_COLOR + " area!");
						player.getInventory().addItem(new ItemStack(Material.SPONGE, 1));
					}
					
					if(group.isLock()){
						extras.add("This area is " + Naxcraft.ADMIN_COLOR + "locked" + Naxcraft.MSG_COLOR + " and can only be unlocked by an admin."); 
					}
				}
			}
			
			boolean includeOwner = false;
			
			if(group.isFlag("pvp")) includeOwner = true;
			if(group.isFlag("nopvp")) includeOwner = false;
			if(group.isSafe()) includeOwner = true;
			if(group.isSanc()) includeOwner = false;
			
			if(includeOwner){
				safety = String.format(safety, owner);
			}
			
			this.groupNote.put(player.getName(), group.getName());
			player.sendMessage("");
			player.sendMessage(message);
			
			String extraMessage = "";
			
			if(safety != "") extraMessage += safety + " ";
			
			for(String extra : extras){
				extraMessage += extra + " ";
			}
			player.sendMessage(Naxcraft.MSG_COLOR + extraMessage);
			
		} else {
			if(this.groupNote.containsKey(player.getName())){
				player.sendMessage("");
				player.sendMessage(Naxcraft.MSG_COLOR + "You are now in the wilderness." );
				
				if(player.getInventory().contains(Material.SPONGE)){
					//player.sendMessage(Naxcraft.MSG_COLOR + "Your creative sponge scaffolding block has been removed.");
					player.getInventory().remove(Material.SPONGE);
				}
				
				this.groupNote.remove(player.getName());
			}
		}
	}
	
	public void onPlayerDropItem(PlayerDropItemEvent event){
		Player player = event.getPlayer();
		
		if(plugin.destroyDrops.containsKey(player)){
			event.getItemDrop().remove();
			return;
		}
		
		if(event.getItemDrop().getItemStack().getType() == Material.SPONGE){
			event.getItemDrop().remove();
			return;
		}
		
		if(plugin.control.has(player, "modkit")){
			if(event.getItemDrop().getItemStack().getType() == Material.GOLD_SWORD){
				event.getItemDrop().remove();
			}
			if(event.getItemDrop().getItemStack().getType() == Material.GOLD_PICKAXE){
				event.getItemDrop().remove();
			}
			if(event.getItemDrop().getItemStack().getType() == Material.GOLD_AXE){
				event.getItemDrop().remove();
			}
			if(event.getItemDrop().getItemStack().getType() == Material.GOLD_SPADE){
				event.getItemDrop().remove();
			}
			if(event.getItemDrop().getItemStack().getType() == Material.GOLD_HOE){
				event.getItemDrop().remove();
			}
			return;
		}
	}
	
	public void onPlayerAnimation(PlayerAnimationEvent event){
		plugin.superCommand.handleAnimation(event);
	}
	
	public void onPlayerInteract(PlayerInteractEvent event){		
		if(event.isCancelled()) return;
		
		plugin.autoarea.handleBlockInteract(event);
		plugin.superCommand.handleItemClick(event);
		
		Player player = event.getPlayer();
		if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
			
			if(event.getClickedBlock().getType() == Material.CHEST) {
				//TODO: PASSWORD PROTECTION OF CHESTS
				//event.setCancelled(true);
				return;
			}
			
			if(event.getClickedBlock().getType() == Material.DISPENSER){		
				CraftDispenser d = new CraftDispenser(event.getClickedBlock());
				
				if(d.getInventory().contains(Material.GOLD_ORE) || d.getInventory().contains(Material.GOLD_INGOT) ||
				   d.getInventory().contains(Material.DIAMOND_ORE) || d.getInventory().contains(Material.DIAMOND) ||
				   d.getInventory().contains(Material.IRON_ORE) || d.getInventory().contains(Material.IRON_INGOT)){
					player.sendMessage(Naxcraft.MSG_COLOR + "This dispenser is unlocked because it contains valuables.");
					event.setCancelled(false);
					
				} else {
					if(plugin.blockListener.requestBuild(player, event.getClickedBlock().getLocation(), true)){
						player.sendMessage(Naxcraft.MSG_COLOR + "You cannot open dispensers owned by others.");
						event.setCancelled(true);
					}
				}
				
				return;
			}
			
			if(event.getClickedBlock().getType() == Material.PAINTING){
				//TODO: FIND OUT IF THIS IS NEEDED, NO ONE LIKES PAINTINGS.
				event.setCancelled(plugin.blockListener.requestBuild(player, event.getClickedBlock().getLocation()));
				return;
			}
		}
		
		if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
			if(event.getItem() == null) return;
			
			if(event.getItem().getType() == Material.PAINTING || 
					   event.getItem().getType() == Material.BUCKET ||
					   event.getItem().getType() == Material.LAVA_BUCKET ||
					   event.getItem().getType() == Material.WATER_BUCKET ||
					   event.getItem().getType() == Material.SIGN ||
					   event.getItem().getType() == Material.SIGN_POST || 
					   event.getItem().getType() == Material.BOAT ||
					   event.getItem().getType() == Material.FLINT_AND_STEEL ||
					   event.getItem().getType() == Material.MINECART ||
					   event.getItem().getType() == Material.POWERED_MINECART ||
					   event.getItem().getType() == Material.REDSTONE ||
					   event.getItem().getType() == Material.REDSTONE_TORCH_ON ||
					   event.getItem().getType() == Material.REDSTONE_TORCH_OFF ||
					   event.getItem().getType() == Material.REDSTONE_WIRE || 
					   event.getItem().getType() == Material.STORAGE_MINECART){
						
				if(plugin.superCommand.isSuper(event.getPlayer().getName())){
					event.setCancelled(false);
					return;
				}
				
				event.setCancelled(plugin.blockListener.requestBuild(event.getPlayer(), event.getClickedBlock().getLocation()));
				return;
			}
			
			if(plugin.control.has(event.getPlayer(), "modkit")){
				
				//TODO: MODKIT COMMAND SHOULD HANDLE THIS
				if(event.getItem().getType() == Material.GOLD_SPADE){
					
					//if the player has set position1 already
					if(plugin.position1.containsKey(player)){
						
						//if the player has set both positions
						if(plugin.position2.containsKey(player)){
							
							//remove all positions
							plugin.position1.remove(player);
							plugin.position2.remove(player);
							
							//set position 1
							plugin.position1.put(player, event.getClickedBlock().getLocation());
							player.sendMessage(Naxcraft.COMMAND_COLOR + "Position 1 set: " + Naxcraft.DEFAULT_COLOR +
																					       + event.getClickedBlock().getLocation().getBlockX() + ", " 
																						   + event.getClickedBlock().getLocation().getBlockY() + ", "
																						   + event.getClickedBlock().getLocation().getBlockZ());
																		
						} else {
							//position 2 free? set it.
							plugin.position2.put(player, event.getClickedBlock().getLocation());
							player.sendMessage(Naxcraft.COMMAND_COLOR + "Position 2 set: " + Naxcraft.DEFAULT_COLOR +
																					       + event.getClickedBlock().getLocation().getBlockX() + ", " 
																						   + event.getClickedBlock().getLocation().getBlockY() + ", "
																						   + event.getClickedBlock().getLocation().getBlockZ());
						}
						
					} else {
						//if the player has not set position1
						plugin.position1.put(player, event.getClickedBlock().getLocation());
						player.sendMessage(Naxcraft.COMMAND_COLOR + "Position 1 set: " + Naxcraft.DEFAULT_COLOR +
																				       + event.getClickedBlock().getLocation().getBlockX() + ", " 
																					   + event.getClickedBlock().getLocation().getBlockY() + ", "
																					   + event.getClickedBlock().getLocation().getBlockZ());
					}
				}
					
				if(event.getItem().getType() == Material.GOLD_PICKAXE){
					if(plugin.superCommand.superPlayers.containsKey(event.getPlayer().getName().toLowerCase())){
						event.getClickedBlock().setType(Material.AIR);
						return;
					}
					
					if(event.getClickedBlock().getType() != Material.CHEST && event.getClickedBlock().getType() != Material.FURNACE && event.getClickedBlock().getType() != Material.BED && 
					   event.getClickedBlock().getType() != Material.BED_BLOCK && event.getClickedBlock().getType() != Material.BOAT && event.getClickedBlock().getType() != Material.BURNING_FURNACE &&
					   event.getClickedBlock().getType() != Material.DISPENSER && event.getClickedBlock().getType() != Material.MINECART && event.getClickedBlock().getType() != Material.WOOD_DOOR &&
					   event.getClickedBlock().getType() != Material.IRON_DOOR && event.getClickedBlock().getType() != Material.IRON_DOOR_BLOCK){
						event.getClickedBlock().setType(Material.AIR);
						return;
					}
				}
				
				if(event.getItem().getType() == Material.GOLD_SWORD){
					String groupName = plugin.groupCommand.requestGroup(event.getClickedBlock().getWorld(), event.getClickedBlock().getLocation());
					if(groupName != ""){
						NaxcraftGroup group = plugin.groupCommand.getList(event.getClickedBlock().getWorld()).get(groupName);
						event.getPlayer().sendMessage(plugin.groupCommand.groupInfo(group));
						
					} else {
						
						int area = plugin.areaCommand.withinOldSchoolArea(event.getClickedBlock().getLocation());
						if(area != -1){
							String owners = plugin.areaCommand.getOldSchoolOwners(area);
							event.getPlayer().sendMessage(Naxcraft.MSG_COLOR + "Old area: " + Naxcraft.DEFAULT_COLOR + area + Naxcraft.MSG_COLOR + " owned by " + owners);
						} else {
							event.getPlayer().sendMessage(Naxcraft.MSG_COLOR + "You are not within an area. (" + event.getClickedBlock().getLocation().getBlockX() + ", " + event.getClickedBlock().getLocation().getBlockZ() + ")");
						}
					
						area = plugin.areaCommand.withinArea(event.getClickedBlock().getLocation());
						if(area != -1){
							event.getPlayer().sendMessage(Naxcraft.MSG_COLOR + "You are in a new unowned area: " + area);
						}
					}
				}
			}
			
		} 
		
		if (event.getAction().equals(Action.RIGHT_CLICK_AIR)){
			if(event.getItem().getType() == Material.PAPER){
				//TODO: MAIL?
				
				event.getPlayer().sendMessage(Naxcraft.MSG_COLOR + "The game.");
				event.getPlayer().setHealth(event.getPlayer().getHealth() + 1);
				return;
			}
			
			if(plugin.control.has(event.getPlayer(), "modkit")){
				if(event.getItem().getType() == Material.GOLD_SPADE){
					
					//if the player has set position1 already
					if(plugin.position1.containsKey(player)){
						
						//if the player has set both positions
						if(plugin.position2.containsKey(player)){
							
							//remove all positions
							plugin.position1.remove(player);
							plugin.position2.remove(player);
							
							//set position 1
							plugin.position1.put(player, player.getLocation());
							player.sendMessage(Naxcraft.COMMAND_COLOR + "Position 1 set: " + Naxcraft.DEFAULT_COLOR +
																					       + player.getLocation().getBlockX() + ", " 
																						   + player.getLocation().getBlockY() + ", "
																						   + player.getLocation().getBlockZ());
																		
						} else {
							//position 2 free? set it.
							plugin.position2.put(player, player.getLocation());
							player.sendMessage(Naxcraft.COMMAND_COLOR + "Position 2 set: " + Naxcraft.DEFAULT_COLOR +
																					       + player.getLocation().getBlockX() + ", " 
																						   + player.getLocation().getBlockY() + ", "
																						   + player.getLocation().getBlockZ());
						}
						
					} else {
						//if the player has not set position1
						plugin.position1.put(player, player.getLocation());
						player.sendMessage(Naxcraft.COMMAND_COLOR + "Position 1 set: " + Naxcraft.DEFAULT_COLOR +
																				       + player.getLocation().getBlockX() + ", " 
																					   + player.getLocation().getBlockY() + ", "
																					   + player.getLocation().getBlockZ());
					}
					return;
				}
			}
		}
	}
}
