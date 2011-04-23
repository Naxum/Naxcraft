package com.naxville.naxcraft.admin;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.InventoryLargeChest;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
//import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.naxville.naxcraft.Naxcraft;

public class NaxcraftSuperCommand {
	public static Naxcraft plugin;
	public Map<String, Boolean> superPlayers;
	public Map<String, Material> flyItems;
	public Map<String, Material> upItems;
	public Map<String, Material> downItems;
	public Map<String, Material> npcItems;
	public Map<String, Material> strikeItems;
	
	private TileEntityVirtualChest chest;
	private TileEntityVirtualChest chest2;
	private InventoryLargeChest lc;
	
	public NaxcraftSuperCommand (Naxcraft instance){
		plugin = instance;
		
		this.superPlayers = new HashMap<String, Boolean>();
		this.flyItems = new HashMap<String, Material>();
		this.upItems = new HashMap<String, Material>();
		this.downItems = new HashMap<String, Material>();
		this.npcItems = new HashMap<String, Material>();
		this.strikeItems = new HashMap<String, Material>();
		
		// Large chests are made up of two individual small chests
		// TileEntityVirtualChest extends the TileEntityChest class to remove some bothersome world checks
		// This would NOT work with regular TileEntityChest instances
		chest = new TileEntityVirtualChest();
		chest2 = new TileEntityVirtualChest();

		/* 
		 * How one would go about adding items to a chest. i = index of slot, s = ItemStack.
		 * 
		int type = res.getInt("stack.type");
        int amount = res.getInt("stack.amount");
        int damage = res.getInt("stack.damage");
        ItemStack s = new ItemStack(type, amount, damage);
        inv.a(i, s);
		*/
		
		// Set up the global chest
		// Note: this is NOT persisted across server restarts
		lc = new InventoryLargeChest("Large chest", chest, chest2);
		
	}
	
	public boolean runSuperCommand(CommandSender sender, String[] args){
		if(sender instanceof Player){
			if(!plugin.control.has((Player)sender, "/super")){
				sender.sendMessage(String.format(Naxcraft.PERMISSIONS_FAIL, "/super"));
				return true;
			}
			Player player = (Player) sender;
			
			if(args.length == 0){
				if(toggleSuper(player.getName())){
					player.sendMessage(Naxcraft.MSG_COLOR + "You are now in " + Naxcraft.SUCCESS_COLOR + "super mode" + Naxcraft.MSG_COLOR + ".");
					
				} else {
					player.sendMessage(Naxcraft.MSG_COLOR + "You are no longer in super mode.");
				}
				
				return true;
			}
			
			if(args.length == 1){
				if(args[0].equalsIgnoreCase("bank")){
					
					// Get the EntityPlayer handle from the sender
					EntityPlayer eh = ((CraftPlayer)sender).getHandle();

					// Chest time!
					eh.a(lc);
					
					return true;
				}
							
				if(args[0].equalsIgnoreCase("move")){
					
					if(this.flyItems.containsKey(player.getName().toLowerCase())){
						if(this.flyItems.containsKey(player.getName().toLowerCase())){
							this.flyItems.remove(player.getName().toLowerCase());
						}
						player.sendMessage(Naxcraft.MSG_COLOR + "Your super-fly tool is now unbound.");
						
					} else {
						this.flyItems.put(player.getName().toLowerCase(), player.getItemInHand().getType());
						player.sendMessage(Naxcraft.MSG_COLOR + "Your super-fly tool is now " + Naxcraft.DEFAULT_COLOR + player.getItemInHand().getType().toString().toLowerCase() + Naxcraft.MSG_COLOR + ".");
					}
					
					return true;
				}
				
				if(args[0].equalsIgnoreCase("up")){
					
					if(this.upItems.containsKey(player.getName().toLowerCase())){
						if(this.upItems.containsKey(player.getName().toLowerCase())){
							this.upItems.remove(player.getName().toLowerCase());
						}
						player.sendMessage(Naxcraft.MSG_COLOR + "Your super-fly-up tool is now unbound.");
						
					} else {
						this.upItems.put(player.getName().toLowerCase(), player.getItemInHand().getType());
						player.sendMessage(Naxcraft.MSG_COLOR + "Your super-fly-up tool is now " + Naxcraft.DEFAULT_COLOR + player.getItemInHand().getType().toString().toLowerCase() + Naxcraft.MSG_COLOR + ".");
					}
					
					return true;
				}
				
				if(args[0].equalsIgnoreCase("down")){
					
					if(this.downItems.containsKey(player.getName().toLowerCase())){
						if(this.downItems.containsKey(player.getName().toLowerCase())){
							this.downItems.remove(player.getName().toLowerCase());
						}
						player.sendMessage(Naxcraft.MSG_COLOR + "Your super-fly-down tool is now unbound.");
						
					} else {
						this.downItems.put(player.getName().toLowerCase(), player.getItemInHand().getType());
						player.sendMessage(Naxcraft.MSG_COLOR + "Your super-fly-down tool is now " + Naxcraft.DEFAULT_COLOR + player.getItemInHand().getType().toString().toLowerCase() + Naxcraft.MSG_COLOR + ".");
					}
					
					return true;
				}
				
				if(args[0].equalsIgnoreCase("npc")){
					
					if(this.npcItems.containsKey(player.getName().toLowerCase())){
						if(this.npcItems.containsKey(player.getName().toLowerCase())){
							this.npcItems.remove(player.getName().toLowerCase());
						}
						player.sendMessage(Naxcraft.MSG_COLOR + "Your super-npc tool is now unbound.");
						
					} else {
						this.npcItems.put(player.getName().toLowerCase(), player.getItemInHand().getType());
						player.sendMessage(Naxcraft.MSG_COLOR + "Your super-npc tool is now " + Naxcraft.DEFAULT_COLOR + player.getItemInHand().getType().toString().toLowerCase() + Naxcraft.MSG_COLOR + ".");
					}
					
					return true;
				}
				
				if(args[0].equalsIgnoreCase("strike")){
					
					if(this.strikeItems.containsKey(player.getName().toLowerCase())){
						if(this.strikeItems.containsKey(player.getName().toLowerCase())){
							this.strikeItems.remove(player.getName().toLowerCase());
						}
						player.sendMessage(Naxcraft.MSG_COLOR + "Your super-strike tool is now unbound.");
						
					} else {
						this.strikeItems.put(player.getName().toLowerCase(), player.getItemInHand().getType());
						player.sendMessage(Naxcraft.MSG_COLOR + "Your super-strike tool is now " + Naxcraft.DEFAULT_COLOR + player.getItemInHand().getType().toString().toLowerCase() + Naxcraft.MSG_COLOR + ".");
					}
					
					return true;
				}
				
				if(args[0].equalsIgnoreCase("unset")){
					
					if(this.npcItems.containsKey(player.getName().toLowerCase())){
						this.npcItems.remove(player.getName().toLowerCase());
					}
					if(this.upItems.containsKey(player.getName().toLowerCase())){
						this.upItems.remove(player.getName().toLowerCase());
					} 
					if(this.downItems.containsKey(player.getName().toLowerCase())){
						this.downItems.remove(player.getName().toLowerCase());
					} 
					if(this.flyItems.containsKey(player.getName().toLowerCase())){
						this.flyItems.remove(player.getName().toLowerCase());
					} 
					if(this.strikeItems.containsKey(player.getName().toLowerCase())){
						this.strikeItems.remove(player.getName().toLowerCase());
					} 
					
					player.sendMessage(Naxcraft.MSG_COLOR + "All of your super tools have been unbound.");
					return true;
				}
				
				if(args[0].equalsIgnoreCase("clear")){
					
					player.getInventory().clear();
					player.sendMessage(Naxcraft.MSG_COLOR + "Inventory cleared!");
					
					return true;
				}
				
				return false;
			}
		} else {
			System.out.println("Hey, /super is only for players right now.");
		}
		
		
		
		return true;
	}
	
	protected boolean toggleSuper(String player){
		player = player.toLowerCase();
		
		if(this.superPlayers.containsKey(player)){
			this.superPlayers.remove(player);
			return false;
			
		} else {
			this.superPlayers.put(player, true);
			return true;
		}
	}
	
	public boolean isSuper(String player){
		player = player.toLowerCase();
				
		if(this.superPlayers.containsKey(player)){
			return true;
		
		} else {
			return false;
		}
	}

	public boolean transcend(CommandSender sender, String[] args) {
		if(!plugin.control.has((Player)sender, "/super")){
			sender.sendMessage(String.format(Naxcraft.PERMISSIONS_FAIL, "/super"));
			return true;
		}
		
		if(args.length == 0) {
			if(sender instanceof Player){
				Player player = (Player)sender;
				System.out.println("Transcending player in " + player.getWorld().getName());
				if(player.getWorld().getName().equalsIgnoreCase("world")){
					World world = plugin.getServer().createWorld(plugin.rpgWorld, World.Environment.NORMAL);
					
					player.teleport(new Location(world, world.getSpawnLocation().getX(), world.getSpawnLocation().getY(), world.getSpawnLocation().getZ()));
					
				} else {
				
					World world = plugin.getServer().getWorld("world");
					player.teleport(new Location(world, world.getSpawnLocation().getX(), world.getSpawnLocation().getY(), world.getSpawnLocation().getZ()));
				}
				return true;
				
			} else { sender.sendMessage("Get the hell out of here."); return true; }
			
		} else {
			String success = "";
			String failed = "";
			for(String name : args){
				Player player = plugin.getServer().getPlayer(name);
				if(player != null){
					if(player.getWorld().getName().equalsIgnoreCase("world")){
						World world = plugin.getServer().createWorld(plugin.rpgWorld, World.Environment.NORMAL);
						
						player.teleport(new Location(world, world.getSpawnLocation().getX(), world.getSpawnLocation().getY(), world.getSpawnLocation().getZ()));
						
					} else {
					
						World world = plugin.getServer().getWorld("world");
						player.teleport(new Location(world, world.getSpawnLocation().getX(), world.getSpawnLocation().getY(), world.getSpawnLocation().getZ()));
					}
					success += name + " ";
					
				} else {
					failed += name + " ";
				}
			}
			sender.sendMessage(Naxcraft.MSG_COLOR + "Succeeded: " + success + ", failed: " + failed);
			return true;
		}
	}

	public void handleItemClick(PlayerInteractEvent event) {		
		/*if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK){
			event.setCancelled(handleItemUse(event.getPlayer(), event.getItem().getType()));
		}*/
	}
	
	public void handleAnimation(PlayerAnimationEvent event){
		if(event.getPlayer().getItemInHand().getTypeId() != 0){
			handleItemUse(event.getPlayer(), event.getPlayer().getItemInHand().getType());
		}
	}
	
	public boolean handleItemUse(Player player, Material item){
		if(flyItems.containsKey(player.getName().toLowerCase())){
			if(item == flyItems.get(player.getName().toLowerCase())){
				player.performCommand("/move");
				player.performCommand("/shift 1");
				return true;
			}
		}
		
		if(upItems.containsKey(player.getName().toLowerCase())){
			if(item == upItems.get(player.getName().toLowerCase())){
				player.performCommand("/move 1 up");
				player.performCommand("/shift 1 up");
				return true;
			}
		}
		
		if(downItems.containsKey(player.getName().toLowerCase())){
			if(item == downItems.get(player.getName().toLowerCase())){
				player.performCommand("/move 1 down");
				player.performCommand("/shift 1 down");
				return true;
			}
		}
		
		if(strikeItems.containsKey(player.getName().toLowerCase())){
			if(item == strikeItems.get(player.getName().toLowerCase())){
				player.performCommand("strike");
				return true;
			}
		}
		
		return false;
	}
}
