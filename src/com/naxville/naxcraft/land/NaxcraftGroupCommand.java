package com.naxville.naxcraft.land;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import com.naxville.naxcraft.Naxcraft;
import com.naxville.naxcraft.admin.NaxcraftConfiguration;

public class NaxcraftGroupCommand {
	Naxcraft plugin;
	private NaxcraftConfiguration config;
	
	public Map<String, NaxcraftGroup>groups;
	
	protected String[] allFlags = { "high", "safe", "lock", "sanc", "pvp", "nopvp", "hurt", "creative", "public", "grinder", "jail" };
	
	public NaxcraftGroupCommand(Naxcraft instance){
		plugin = instance;
		
		groups = new HashMap<String, NaxcraftGroup>();
		
		loadGroupFile();
	}
	
	public Map<String, NaxcraftGroup> getGroups(String world)
	{
		return groups;
	}
	
	public boolean runGroupCommand(CommandSender sender, String[] args){
		/*
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
			}
			
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
		*/
		sender.sendMessage(Naxcraft.ERROR_COLOR + "None of that anymore.");
		return true;
	}
	
	private void loadGroupFile(){
		File file = new File(plugin.filePath);
		
		if (!file.exists()){
			file.mkdir();
		}
		file = new File(plugin.filePath + "AreaGroups.yml");
		if (!file.exists()){
			//file.createNewFile();
			//initalizeFile();
			
			//no one cares
			return;
		}
		
		config = new NaxcraftConfiguration(file);
		config.load();
	}
	
	public boolean loadGroups(){
		try {
			for(World world : plugin.getServer().getWorlds()){
				if(!world.getName().equals(Naxcraft.OLD_NAXVILLE))
				{
					continue;
				}
				
				List<String>groupList = config.getKeys(Naxcraft.OLD_NAXVILLE);
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
						this.groups.put(string.toLowerCase(), group);
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
		config.setProperty(Naxcraft.OLD_NAXVILLE + "." + key + ".type", group.saveType());
		config.setProperty(Naxcraft.OLD_NAXVILLE + "." + key + ".owner", group.saveOwner());
		config.setProperty(Naxcraft.OLD_NAXVILLE + "." + key + ".areas", group.saveAreas());
		//config.setProperty("groups." + key + ".flags", group.saveFlags());
		
		HashMap<String, String>flags = group.saveFlags();
		
		for(String flag : flags.keySet()){
			config.setProperty(Naxcraft.OLD_NAXVILLE + "." + key + ".flags."+flag, flags.get(flag));
		}
		
		//System.out.println("Group saving: " + group.getWorldName() + "." + group.getName());
		
		config.save();
		return;
	}
	
	protected void removeGroup(NaxcraftGroup group){
		config.removeProperty(Naxcraft.OLD_NAXVILLE + "." + group.getName());
		config.save();
	}
	
	public String requestGroup(World world, Location location){		
		if(!world.getName().equalsIgnoreCase(Naxcraft.OLD_NAXVILLE))
		{
			return "";
		}
		
		for(String name : groups.keySet()){
			NaxcraftGroup group = groups.get(name);
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
			return this.groups;			
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
	
	public void getGroupInfo(Player player)
	{
		String groupName = plugin.groupCommand.requestGroup(player.getWorld(), player.getLocation());
		if(groupName != "")
		{
			NaxcraftGroup group = plugin.groupCommand.getList(player).get(groupName); 
			
			List<String> extras = new ArrayList<String>();
			
			String safety = "";
			String owner = "";
			String message = ""; 
			if(group.isFlag("pvp")) safety = "%s are protected from " + Naxcraft.ERROR_COLOR + "PvP damage" + Naxcraft.MSG_COLOR + " here.";
			if(group.isSafe()) safety = "%s are protected from " + Naxcraft.ERROR_COLOR + "all damage" + Naxcraft.MSG_COLOR + " here.";
			
			if(group.isFlag("nopvp"))
			{
				if(safety != "") safety += " ";
				safety += "Everyone is protected from " + Naxcraft.ERROR_COLOR + "PvP damage" + Naxcraft.MSG_COLOR + " here.";
			}
			if(group.isSanc()) 
				safety = "This is a " + Naxcraft.ADMIN_COLOR + "sanctuary" + Naxcraft.MSG_COLOR + ", no one may be harmed here.";
			
			if(group.isFlag("jail")) 
				extras.add("This is a jail, you may not warp out of here.");
			
			if(group.isFlag("public"))
			{
				owner = "";
				message = Naxcraft.MSG_COLOR + "You are now in a " + Naxcraft.SUCCESS_COLOR + "public" + Naxcraft.MSG_COLOR + " area.";
				
				if(group.isFlag("creative")) 
				{
					extras.add("This is a " + Naxcraft.ADMIN_COLOR + "creative" + Naxcraft.MSG_COLOR + " area for everyone!");
					player.getInventory().addItem(new ItemStack(Material.SPONGE, 1));
				}
				
				if(group.isLock())
				{
					extras.add("This area is " + Naxcraft.ADMIN_COLOR + "locked" + Naxcraft.MSG_COLOR + " and can only be unlocked by an admin."); 
				}

			} 
			else 
			{
				if(!group.isOwner(player.getName()))
				{
					message = Naxcraft.MSG_COLOR + "This area is owned by " + Naxcraft.DEFAULT_COLOR + group.getOwner() + Naxcraft.MSG_COLOR + ".";
					owner = "The owners";
					
					if(group.isFlag("creative")) 
					{
						extras.add("This is a " + Naxcraft.ADMIN_COLOR + "creative" + Naxcraft.MSG_COLOR + " area, the owner has infinite blocks.");
						
						if(plugin.superCommand.superPlayers.containsKey(player.getName().toLowerCase())) 
						{
							player.getInventory().addItem(new ItemStack(Material.SPONGE, 1));
						}
					}
					
				} 
				else 
				{
					message = Naxcraft.MSG_COLOR + "You own this area.";
					owner = "You";
					
					if(group.isFlag("creative")) 
					{
						extras.add("You get infinite stuff, this is a " + Naxcraft.ADMIN_COLOR + "creative" + Naxcraft.MSG_COLOR + " area!");
						player.getInventory().addItem(new ItemStack(Material.SPONGE, 1));
					}
					
					if(group.isLock())
					{
						extras.add("This area is " + Naxcraft.ADMIN_COLOR + "locked" + Naxcraft.MSG_COLOR + " and can only be unlocked by an admin."); 
					}
				}
			}
			
			boolean includeOwner = false;
			
			if(group.isFlag("pvp")) includeOwner = true;
			if(group.isFlag("nopvp")) includeOwner = false;
			if(group.isSafe()) includeOwner = true;
			if(group.isSanc()) includeOwner = false;
			
			if(includeOwner)
			{
				safety = String.format(safety, owner);
			}
			player.sendMessage(message);
			
			String extraMessage = "";
			
			if(safety != "") 
				extraMessage += safety + " ";
			
			for(String extra : extras)
			{
				extraMessage += extra + " ";
			}
			player.sendMessage(Naxcraft.MSG_COLOR + extraMessage);
			
		} 
		else 
		{
			player.sendMessage(Naxcraft.MSG_COLOR + "You are in the wilderness." );		
		}
	}

	public void handleEntityDamage(EntityDamageEvent event) {
		if(event.getEntity() instanceof Player) {
		
		Player player = (Player)event.getEntity();
		
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
		
	} else return;
		
	}
}
