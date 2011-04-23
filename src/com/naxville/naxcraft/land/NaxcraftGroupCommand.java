package com.naxville.naxcraft.land;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.naxville.naxcraft.Naxcraft;
import com.naxville.naxcraft.admin.NaxcraftConfiguration;

public class NaxcraftGroupCommand {
	Naxcraft plugin;
	private NaxcraftConfiguration config;
	
	private Map<String, NaxcraftGroup>groups;
	private Map<String, NaxcraftGroup>rpgGroups;
	
	protected String[] allFlags = { "high", "safe", "lock", "sanc", "pvp", "nopvp", "hurt", "creative", "public", "grinder", "jail" };
	
	public NaxcraftGroupCommand(Naxcraft instance){
		plugin = instance;
		
		groups = new HashMap<String, NaxcraftGroup>();
		rpgGroups = new HashMap<String, NaxcraftGroup>();
		
		loadGroupFile();
	}
	
	public boolean getFlags(CommandSender sender){
		sender.sendMessage(Naxcraft.COMMAND_COLOR + "Area Flags" + Naxcraft.SUCCESS_COLOR + " (Buyable, Price) " + Naxcraft.ADMIN_COLOR + "(Admin Only)"+Naxcraft.DEFAULT_COLOR +":"); 
		sender.sendMessage(Naxcraft.SUCCESS_COLOR + "High, 16g" + Naxcraft.MSG_COLOR + ": Protects from bedrock to sky. ");
		sender.sendMessage(Naxcraft.SUCCESS_COLOR + "PvP, 32g" + Naxcraft.MSG_COLOR + ": Protects owners from player damage.");
		sender.sendMessage(Naxcraft.SUCCESS_COLOR + "Safe, 64g" + Naxcraft.MSG_COLOR + ": Protects owners from ALL damage. (buy PvP first)");
		sender.sendMessage(Naxcraft.SUCCESS_COLOR + "Grinder, 64g" + Naxcraft.MSG_COLOR + ": Allows mobs to be farmed for items.");	
		sender.sendMessage(Naxcraft.ADMIN_COLOR + "Sanctuary" + Naxcraft.MSG_COLOR + ": Protects everyone from ALL damage.");
		sender.sendMessage(Naxcraft.ADMIN_COLOR + "NoPvP" + Naxcraft.MSG_COLOR + ": Protects everyone from PvP damage.");
		sender.sendMessage(Naxcraft.ADMIN_COLOR + "Creative" + Naxcraft.MSG_COLOR + ": Infinite blocks and tool health.");
		sender.sendMessage(Naxcraft.ADMIN_COLOR + "Public" + Naxcraft.MSG_COLOR + ": Everyone is free to build here.");
		sender.sendMessage(Naxcraft.ADMIN_COLOR + "Hurt" + Naxcraft.MSG_COLOR + ": Non-owners take damage when attempting to build.");
		sender.sendMessage(Naxcraft.ADMIN_COLOR + "Lock" + Naxcraft.MSG_COLOR + ": Area locked by an admin.");
		sender.sendMessage(Naxcraft.ADMIN_COLOR + "Jail" + Naxcraft.MSG_COLOR + ": Cannot /home or /warp your way out!");
		
		return true;
	}
	
	public boolean runGroupCommand(CommandSender sender, String[] args){
		if(sender instanceof Player && !plugin.control.has((Player)sender, "group")){
			sender.sendMessage(String.format(Naxcraft.PERMISSIONS_FAIL, "/group"));
			return true;
		}
		
		Player player;
		
		if(sender instanceof Player){
			//getting worlds, bro, fuck your console.
			player = (Player)sender;
			
		} else return true;
		
		if(args.length == 0) return false;
		
		if(args[0].equalsIgnoreCase("reload")){
			for(String group : this.getList(player).keySet()){
				this.saveGroup(this.getList(player).get(group));
			}
			sender.sendMessage("Done");
			return true;
		}
		
		if(args[0].equalsIgnoreCase("list")){
			if(args.length == 1){
				int total = 0;
				String groupNames = "";
				
				for(String key : this.getList(player).keySet()){
					if(groupNames != "") groupNames += Naxcraft.COMMAND_COLOR + ", ";
					groupNames += Naxcraft.DEFAULT_COLOR + this.getList(player).get(key).getName();
					total++;
				}
				
				sender.sendMessage(Naxcraft.COMMAND_COLOR + "" + total + " total groups: " + groupNames + Naxcraft.COMMAND_COLOR + ".");
			}
			
			if(args.length == 2){
				if(this.getList(player).containsKey(args[1].toLowerCase())){
					NaxcraftGroup group = this.getList(player).get(args[1].toLowerCase());					
					sender.sendMessage(groupInfo(group));
					
				} else {
					sender.sendMessage(Naxcraft.ERROR_COLOR + "Group " + Naxcraft.DEFAULT_COLOR + args[1] + Naxcraft.ERROR_COLOR + " does not exist.");
				}
			}
			
			if(args.length == 3 && args[1].equalsIgnoreCase("ownedby")){
				int total = 0;
				String groupNames = "";
				
				for(String key : this.getList(player).keySet()){
					if(this.getList(player).get(key).isOwner(args[2].toLowerCase())){
						if(groupNames != "") groupNames += Naxcraft.COMMAND_COLOR + ", ";
						groupNames += Naxcraft.DEFAULT_COLOR + this.getList(player).get(key).getName();
						total++;
					}
				}
				
				sender.sendMessage(plugin.getNickName(args[2].toLowerCase()) + Naxcraft.COMMAND_COLOR + " owns " + Naxcraft.DEFAULT_COLOR + total + Naxcraft.COMMAND_COLOR + " groups: " + groupNames + Naxcraft.COMMAND_COLOR + ".");
			} /*else {
				sender.sendMessage(Naxcraft.ERROR_COLOR + "Try /group list ownedby <name>.");
			}*/
			
			return true;
		}
		
		if(args[0].equalsIgnoreCase("create")){
			String owner = "";
			
			if(args.length != 3) {
				sender.sendMessage(Naxcraft.MSG_COLOR + "You did not include the type (player/clan), so it defaulted to player.");
				//return true;
				
				owner = "player";
			}
			
			else if(args.length == 3){
				owner = args[2].toLowerCase();
			}
			
			if (!this.getList(player).containsKey(args[1].toLowerCase())){
				NaxcraftGroup group = new NaxcraftGroup(plugin, args[1].toLowerCase());
				group.setType(owner);
				group.setWorldName(player.getWorld().getName());
				
				HashMap<String, String> flags = new HashMap<String, String>();
				group.setFlags(flags);
				
				List<Integer> areas = new ArrayList<Integer>();
				group.setAreas(areas);
				group.setOwner(args[1].toLowerCase());
				
				this.getList(player).put(args[1].toLowerCase(), group);
				sender.sendMessage(Naxcraft.SUCCESS_COLOR + "Group " + Naxcraft.DEFAULT_COLOR + group.getName() + Naxcraft.SUCCESS_COLOR + " has been created and is owned by the " + Naxcraft.DEFAULT_COLOR + group.getType() + " " + group.getOwner() + Naxcraft.SUCCESS_COLOR + "!");
				this.saveGroup(group);
				return true;
			} else {
				
				sender.sendMessage(Naxcraft.ERROR_COLOR + "A group already exists with that name.");
				return true;
			}
		}
		
		if(args[1].equalsIgnoreCase("add")){
			if(args.length != 3) return false;
			
			if(this.getList(player).containsKey(args[0].toLowerCase())){
				
				int index = 0;
				
				try {
					index = Integer.parseInt(args[2]);
					
				} catch (NumberFormatException e){
					sender.sendMessage(Naxcraft.ERROR_COLOR + args[2] + " is not a number.");
					return false;
				}
				
				if(plugin.areaCommand.areas.get(index) != null){
					if(this.getList(player).get(args[0].toLowerCase()).addArea(args[2])){
						sender.sendMessage(Naxcraft.MSG_COLOR + "Area " + Naxcraft.DEFAULT_COLOR + args[2] + Naxcraft.MSG_COLOR + " added to group " + Naxcraft.DEFAULT_COLOR + args[0] + Naxcraft.MSG_COLOR + ".");
						this.saveGroup(this.getList(player).get(args[0].toLowerCase()));
						return true;
						
					} else {
						sender.sendMessage(Naxcraft.ERROR_COLOR + "Error adding area " + Naxcraft.DEFAULT_COLOR + args[2] + Naxcraft.ERROR_COLOR + " to group " + Naxcraft.DEFAULT_COLOR + args[0] + Naxcraft.ERROR_COLOR + ".");
						return true;
					}
				} else {
					sender.sendMessage(Naxcraft.ERROR_COLOR + "That area does not exist.");
					return false;
				}
				
			} else {
				sender.sendMessage(Naxcraft.ERROR_COLOR + "There is no area group by the name " + Naxcraft.DEFAULT_COLOR + args[0] + Naxcraft.ERROR_COLOR + ".");
				return true;
			}
		}
		
		if(args[1].equalsIgnoreCase("remove")){
			if(args.length != 3) return false;
			
			if(this.getList(player).containsKey(args[0].toLowerCase())){
				
				int index = 0;
				
				try {
					index = Integer.parseInt(args[2]);
					
				} catch (NumberFormatException e){
					sender.sendMessage(Naxcraft.ERROR_COLOR + args[2] + " is not a number.");
					return false;
				}
				
				if(plugin.areaCommand.areas.get(index) != null){
				
					if(this.getList(player).get(args[0].toLowerCase()).removeArea(args[2])){
						sender.sendMessage(Naxcraft.SUCCESS_COLOR + "Area " + Naxcraft.DEFAULT_COLOR + args[2] + Naxcraft.SUCCESS_COLOR + " removed from group " + Naxcraft.DEFAULT_COLOR + args[0] + Naxcraft.SUCCESS_COLOR + ".");
						this.saveGroup(this.getList(player).get(args[0].toLowerCase()));
						return true;
						
					} else {
						sender.sendMessage(Naxcraft.ERROR_COLOR + "Error removing area " + Naxcraft.DEFAULT_COLOR + args[2] + Naxcraft.ERROR_COLOR + " from group " + Naxcraft.DEFAULT_COLOR + args[0] + Naxcraft.ERROR_COLOR + ".");
						return true;
					}
					
				} else {
					sender.sendMessage(Naxcraft.ERROR_COLOR + "That area does not exist.");
					return false;
				}
				
			} else {
				sender.sendMessage(Naxcraft.ERROR_COLOR + "There is no area group by the name " + Naxcraft.DEFAULT_COLOR + args[0] + Naxcraft.ERROR_COLOR + ".");
				return true;
			}
		}
		
		if(args[1].equalsIgnoreCase("owner")){
			if(args.length == 3) {
				if(this.getList(player).containsKey(args[0].toLowerCase())){
					
					this.getList(player).get(args[0].toLowerCase()).setOwner(args[2].toLowerCase());
					sender.sendMessage(Naxcraft.SUCCESS_COLOR + "Group " + Naxcraft.DEFAULT_COLOR + args[0].toLowerCase() + Naxcraft.SUCCESS_COLOR + " is now owned by " + Naxcraft.DEFAULT_COLOR + args[2].toLowerCase() + Naxcraft.SUCCESS_COLOR + ".");
					this.saveGroup(this.getList(player).get(args[0].toLowerCase()));
					return true;
					
				} else {
					sender.sendMessage(Naxcraft.ERROR_COLOR + "There is no area group by the name " + Naxcraft.DEFAULT_COLOR + args[0] + Naxcraft.ERROR_COLOR + ".");
					return true;
				}
			}
			
			if(args.length == 4){
				if(args[2].equalsIgnoreCase("add")){
					if(this.getList(player).containsKey(args[0].toLowerCase())){
						NaxcraftGroup group = this.getList(player).get(args[0].toLowerCase());
						
						if(group.getType().equalsIgnoreCase("player")){
							
							if(group.addOwner(args[3])){
								sender.sendMessage(plugin.getNickName(args[3].toLowerCase()) + Naxcraft.SUCCESS_COLOR + " is now an owner of " + Naxcraft.DEFAULT_COLOR + group.getName() + Naxcraft.SUCCESS_COLOR + ".");
								this.saveGroup(this.getList(player).get(args[0].toLowerCase()));
								return true;
								
							} else {
								sender.sendMessage(plugin.getNickName(args[3].toLowerCase()) + Naxcraft.ERROR_COLOR + " already is an owner of " + Naxcraft.DEFAULT_COLOR + group.getName() + Naxcraft.ERROR_COLOR + ".");
								return true;
							}
							
							
						} else {
							sender.sendMessage(Naxcraft.ERROR_COLOR + "This group is not owned by a player, therefore you cannot add more owners.");
							return true;
						}
						
					} else {
						sender.sendMessage(Naxcraft.ERROR_COLOR + "There is no area group by the name " + Naxcraft.DEFAULT_COLOR + args[0] + Naxcraft.ERROR_COLOR + ".");
						return true;
					}
				}
			} else if(args[2].equalsIgnoreCase("remove")){
				if(this.getList(player).containsKey(args[0].toLowerCase())){
					NaxcraftGroup group = this.getList(player).get(args[0].toLowerCase());
					
					if(group.getType().equalsIgnoreCase("player")){
						
						if(group.removeOwner(args[3])){
							sender.sendMessage(plugin.getNickName(args[3].toLowerCase()) + Naxcraft.SUCCESS_COLOR + " is no longer an owner of " + Naxcraft.DEFAULT_COLOR + group.getName() + Naxcraft.SUCCESS_COLOR + ".");
							this.saveGroup(this.getList(player).get(args[0].toLowerCase()));
							return true;
							
						} else {
							sender.sendMessage(plugin.getNickName(args[3].toLowerCase()) + Naxcraft.ERROR_COLOR + " does not have " + Naxcraft.DEFAULT_COLOR + group.getName() + Naxcraft.ERROR_COLOR + " as an owner.");
							return true;
						}
						
						
					} else {
						sender.sendMessage(Naxcraft.ERROR_COLOR + "This group is not owned by a player, therefore you cannot remove any owners.");
						return true;
					}
					
				} else {
					sender.sendMessage(Naxcraft.ERROR_COLOR + "There is no area group by the name " + Naxcraft.DEFAULT_COLOR + args[0] + Naxcraft.ERROR_COLOR + ".");
					return true;
				}
			}
		}
		
		if(args[1].equalsIgnoreCase("type")){
			if(args.length != 3) return false;
			
			if(args[2].equalsIgnoreCase("clan") || args[2].equalsIgnoreCase("player")){
				if(this.getList(player).containsKey(args[0].toLowerCase())){
					
					this.getList(player).get(args[0].toLowerCase()).setType(args[2].toLowerCase());
					sender.sendMessage(Naxcraft.SUCCESS_COLOR + "Group " + Naxcraft.DEFAULT_COLOR + args[0] + Naxcraft.SUCCESS_COLOR + " can now be owned by a " + Naxcraft.DEFAULT_COLOR + args[2] + Naxcraft.SUCCESS_COLOR + ".");
					this.saveGroup(this.getList(player).get(args[0].toLowerCase()));
					return true;
					
				} else {
					sender.sendMessage(Naxcraft.ERROR_COLOR + "There is no area group by the name " + Naxcraft.DEFAULT_COLOR + args[0] + Naxcraft.ERROR_COLOR + ".");
					return true;
				}
			} else {
				sender.sendMessage(Naxcraft.ERROR_COLOR + "You tried to change its type to " + Naxcraft.DEFAULT_COLOR + args[2]);
				return false;
			}
		}

		if(args[1].equalsIgnoreCase("flag")){
			if(args.length != 3) return false;
			
			if(this.getList(player).containsKey(args[0].toLowerCase())){
				
				boolean flagExists = false;
				
				for(String flag : allFlags){
					if(args[2].equalsIgnoreCase(flag)){
						flagExists = true;
						break;
					}
				}
				
				if(flagExists){
					String result = this.getList(player).get(args[0].toLowerCase()).toggleFlag(args[2]);
					sender.sendMessage(Naxcraft.SUCCESS_COLOR + "Group " + Naxcraft.DEFAULT_COLOR + args[0] + Naxcraft.SUCCESS_COLOR + "'s " + Naxcraft.DEFAULT_COLOR + args[2] + Naxcraft.SUCCESS_COLOR + " flag is now " + Naxcraft.DEFAULT_COLOR + result + Naxcraft.SUCCESS_COLOR + ".");
					this.saveGroup(this.getList(player).get(args[0].toLowerCase()));
					return true;
					
				} else {
					sender.sendMessage(Naxcraft.ERROR_COLOR + "The flag " + Naxcraft.DEFAULT_COLOR + args[2].toLowerCase() + Naxcraft.ERROR_COLOR + " does not exist!");
					return true;
				}
				
			} else {
				sender.sendMessage(Naxcraft.ERROR_COLOR + "There is no area group by the name " + Naxcraft.DEFAULT_COLOR + args[0] + Naxcraft.ERROR_COLOR + ".");
				return true;
			}
		}
		
		if(args[1].equalsIgnoreCase("rename")){
			if(args.length != 3) return false;
			
			if(this.getList(player).containsKey(args[0].toLowerCase()) || rpgGroups.containsKey(args[0].toLowerCase())){
				
				String first = args[0].toLowerCase();
				String second = args[2].toLowerCase();
				
				if(!this.getList(player).containsKey(second)){
					this.getList(player).get(first.toLowerCase()).setName(second);
					
					sender.sendMessage(Naxcraft.SUCCESS_COLOR + "Group " + Naxcraft.DEFAULT_COLOR + first + Naxcraft.SUCCESS_COLOR + " is now named " + Naxcraft.DEFAULT_COLOR + second + Naxcraft.SUCCESS_COLOR + ".");
					
					this.getList(player).put(second.toLowerCase(), this.getList(player).get(first.toLowerCase()));
					this.removeGroup(this.getList(player).get(first.toLowerCase()));
					this.getList(player).remove(first.toLowerCase());
					this.saveGroup(this.getList(player).get(second.toLowerCase()));
					
					return true;
					
				} else {
					sender.sendMessage(Naxcraft.ERROR_COLOR + "There is another group with the name " + Naxcraft.DEFAULT_COLOR + args[2] + Naxcraft.ERROR_COLOR + ".");
					return true;
				}
				
			} else {
				sender.sendMessage(Naxcraft.ERROR_COLOR + "There is no area group by the name " + Naxcraft.DEFAULT_COLOR + args[0] + Naxcraft.ERROR_COLOR + ".");
				return true;
			}
		}
		
		return true;
	}
	
	private void loadGroupFile(){
		File file = new File(plugin.filePath);
		try {
		if (!file.exists()){
			file.mkdir();
		}
		file = new File(plugin.filePath + "AreaGroups.yml");
		if (!file.exists()){
			file.createNewFile();
			initalizeFile();
		}
		} catch (IOException Ex) {
			System.out.println("Shit, problem creating new area groups file");
			return;
		}
		
		config = new NaxcraftConfiguration(file);
		config.load();
	}
	
	private void initalizeFile(){
		File file = new File(plugin.filePath + "AreaGroups.yml");
		try {
			for(World world : plugin.getServer().getWorlds()){
				BufferedWriter output = new BufferedWriter(new FileWriter(file));
				output.write(world.getName() + ":");
				output.close();
			}
		} catch (Exception Ex) {
			System.out.println("Error initalizing area groups file.  Groups cant save >:(");
		}
	}
	
	public boolean loadGroups(){
		try {
			if(plugin.getServer().getWorld(plugin.rpgWorld) == null){
				plugin.getServer().createWorld(plugin.rpgWorld, Environment.NORMAL);
			}
			for(World world : plugin.getServer().getWorlds()){
				List<String>groupList = config.getKeys(world.getName());
				NaxcraftGroup group;
				for (String string : groupList){
				
					group = new NaxcraftGroup(plugin, string);
					String type = config.getString(world.getName() + "." + string + ".type");
					String owner = config.getString(world.getName() + "." + string + ".owner");
					List<Integer> areas = config.getIntList(world.getName() + "." + string + ".areas", null);
					List<String> flagsConfig = config.getKeys(world.getName() + "." + string + ".flags"); 
					HashMap<String, String> flags = new HashMap<String, String>();
					
					for(String flag : flagsConfig){
						flags.put(flag, config.getString(world.getName() + "."+string+".flags."+flag));
						//System.out.println("Group " + string + "'s " + flag + " flag is " + config.getString("groups."+string+".flags."+flag));
					}
					
					group.setType(type);
					group.setOwner(owner);
					group.setAreas(areas);
					group.setFlags(flags);
					group.setWorldName(world.getName());
					
					if(areas.isEmpty()){
						this.removeGroup(group);
						//this.saveGroup(group);
					} else {
						if(world.getName().equalsIgnoreCase("world")){
							this.groups.put(string.toLowerCase(), group);
							
						} else {
							this.rpgGroups.put(string.toLowerCase(), group);
						}
					}
				}
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
			//initalizeFile();
			return false;
		} 
		return true;
	}
	
	protected void saveGroup(NaxcraftGroup group){
		String key = group.getName();
		config.setProperty(group.getWorldName() + "." + key + ".type", group.saveType());
		config.setProperty(group.getWorldName() + "." + key + ".owner", group.saveOwner());
		config.setProperty(group.getWorldName() + "." + key + ".areas", group.saveAreas());
		//config.setProperty("groups." + key + ".flags", group.saveFlags());
		
		HashMap<String, String>flags = group.saveFlags();
		
		for(String flag : flags.keySet()){
			config.setProperty(group.getWorldName() + "." + key + ".flags."+flag, flags.get(flag));
		}
		
		//System.out.println("Group saving: " + group.getWorldName() + "." + group.getName());
		
		config.save();
		return;
	}
	
	protected void removeGroup(NaxcraftGroup group){
		config.removeProperty(group.getWorldName() + "." + group.getName());
		config.save();
	}
	
	public String requestGroup(World world, Location location){
		for(String name : this.getList(world).keySet()){
			NaxcraftGroup group = this.getList(world).get(name);
			if(group.withinGroup(world, location)){
				return group.getName();
			}
		}
		
		return "";
	}
	
	public String requestGroup(Player player, Location location){
		return this.requestGroup(player.getWorld(), location);
	}
	public Map<String, NaxcraftGroup> getList(Player player){
		if(player.getWorld().getName().equalsIgnoreCase("world")){
			return this.groups;
			
		} else {
			return this.rpgGroups;
		}
	}
	public Map<String, NaxcraftGroup> getList(World world) {
		if(world.getName().equalsIgnoreCase("world")){
			return this.groups;
			
		} else {
			return this.rpgGroups;
		}
	}

	public String groupInfo(NaxcraftGroup group){		
		String flags = "";
		for(String key : group.getFlags().keySet()){
			if(flags != "") flags += Naxcraft.COMMAND_COLOR + ", ";
			
			String result = group.getFlags().get(key);
			if(result.equals("true")){
				flags += Naxcraft.SUCCESS_COLOR + key;
			}
			
		}
		
		String areas = "";
		int count = 0;
		for(int i : group.getAreas()){
			if(areas != "") areas += Naxcraft.COMMAND_COLOR + ", ";
			areas += Naxcraft.DEFAULT_COLOR + "" + i;
			count++;
		}
		
		return (Naxcraft.COMMAND_COLOR + "Group: " + Naxcraft.DEFAULT_COLOR + group.getName() + Naxcraft.COMMAND_COLOR + 
				" owned by " + Naxcraft.DEFAULT_COLOR + group.getOwner() + Naxcraft.COMMAND_COLOR + ". Its flags are: " + flags + Naxcraft.COMMAND_COLOR +
				". It contains " + Naxcraft.DEFAULT_COLOR + count + Naxcraft.COMMAND_COLOR + " areas: " + areas);
	}
	
	public String groupInfo(String groupName, Player player){
		NaxcraftGroup group = this.getList(player).get(groupName);
		
		String flags = "";
		for(String key : group.getFlags().keySet()){
			if(flags != "") flags += Naxcraft.COMMAND_COLOR + ", ";
			
			String result = group.getFlags().get(key);
			if(result.equals("true")){
				flags += Naxcraft.SUCCESS_COLOR + key;
			}
			
		}
		
		int area = plugin.areaCommand.getArea(player);
		return (Naxcraft.COMMAND_COLOR + "Group: " + Naxcraft.DEFAULT_COLOR + group.getName() + Naxcraft.COMMAND_COLOR + 
				" owned by " + Naxcraft.DEFAULT_COLOR + group.getOwner() + Naxcraft.COMMAND_COLOR + " (" + player.getLocation().getBlockX() + ", " + 
				player.getLocation().getBlockZ() + ") Area " + Naxcraft.DEFAULT_COLOR + area + Naxcraft.COMMAND_COLOR + ". Its flags are: " + flags);
	}
	
	protected List<Block> getBlocks(NaxcraftGroup group){
		List<Block> blocks = new ArrayList<Block>();
			
		for(int area : group.getAreas()){
			plugin.areaCommand.areas.get(area).getBlocks(blocks, group.isHigh(), 0);
		}
		
		return blocks;
	}
	
	public List<Block> getBlocks(NaxcraftGroup group, int yStart){
		List<Block> blocks = new ArrayList<Block>();
		
		for(int area : group.getAreas()){
			plugin.areaCommand.areas.get(area).getBlocks(blocks, group.isHigh(), yStart);
		}
		
		return blocks;
	}
}
