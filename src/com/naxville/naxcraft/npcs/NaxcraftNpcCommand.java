package com.naxville.naxcraft.npcs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
//import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
//import org.bukkit.Material;
//import org.bukkit.World;
//import org.bukkit.entity.Player;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.martin.bukkit.npclib.NPCEntity;
import org.martin.bukkit.npclib.NPCManager;

import com.naxville.naxcraft.Naxcraft;
import com.naxville.naxcraft.admin.NaxcraftConfiguration;
//import com.naxville.naxcraft.npcs.NaxcraftNpc.NpcInput;

public class NaxcraftNpcCommand {
	public Naxcraft plugin;
    public NpcList npcs;
    public int id_increment;
    
    private NaxcraftConfiguration config;
	
    public String personalities[] = {"jim", "silent"};
    
	public NaxcraftNpcCommand(Naxcraft instance){
		plugin = instance;
		npcs = new NpcList();
		id_increment = 0;
		//loadNpcFile();
	}
	
	@SuppressWarnings("unused")
	private void loadNpcFile(){
		
		File file = new File(plugin.filePath);
		try {
			if (!file.exists()){
				file.mkdir();
			}
			file = new File(plugin.filePath + "Npcs.yml");
			if (!file.exists()){
				file.createNewFile();
				initalizeFile();
			}
		} catch (IOException Ex) {
			System.out.println("Shit, problem creating new NPCs file");
			return;
		}
		
		config = new NaxcraftConfiguration(file);
		config.load();
		
	}
	
	private void initalizeFile(){
		
		File file = new File(plugin.filePath + "Npcs.yml");
		try {			
			BufferedWriter output = new BufferedWriter(new FileWriter(file));
			//TODO: HEY, NO WORLDS HUH? COOL, BUKKIT.
			output.write("world: {}\r\n");
			output.write("rpg: {}");
			output.close();
		} catch (Exception e) {
			System.out.println("Error NPC file, AAGGGHHH");
			e.printStackTrace();
		}
	}
	
	public boolean loadNpcs(){	
		return false;
		/*
		try {
			for(World world : plugin.getServer().getWorlds()){
				List<String>npcList = config.getKeys(world.getName());
				if(npcList == null) return true;
				
				for (String id : npcList){
				
					String name = config.getString(world.getName() + "." + id + ".name");
					
					double x = Double.parseDouble(config.getString(world.getName() + "." + id + ".x"));
					double y = Double.parseDouble(config.getString(world.getName() + "." + id + ".y"));
					double z = Double.parseDouble(config.getString(world.getName() + "." + id + ".z"));
					float yaw = Float.parseFloat(config.getString(world.getName() + "." + id + ".yaw"));
					float pitch = Float.parseFloat(config.getString(world.getName() + "." + id + ".pitch"));
					Location location = new Location(world, x, y, z, yaw, pitch);
					
					boolean invincible = Boolean.parseBoolean(config.getString(world.getName() + "."+id+".invincible"));
					
					NpcPersonality personality = new NpcPersonality(config.getString(world.getName() + "."+id+".personality"));
					
					Material hand = Material.getMaterial(config.getString(world.getName() + "." + id + ".hand"));
					
					String rightClick = config.getString(world.getName() + "." + id + ".rightclick");
					String proximity = config.getString(world.getName() + "." + id + ".proximity");
					String hurt = config.getString(world.getName() + "." + id + ".hurt");
					String chat = config.getString(world.getName() + "." + id + ".chat");
					
					this.npcs.put(id, new NaxcraftNpc(id, name, location, invincible, personality, hand, rightClick, proximity, hurt, chat));
					id_increment++;
				}
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
			//initalizeFile();
			return false;
		} 
		return true;
		*/
	}
	/*
	protected void saveNpc(NaxcraftNpc npc){	
		String prefix = npc.getLocation().getWorld().getName() + "." + npc.getUniqueId() + ".";
		
		config.setProperty(prefix + "name", npc.getName());
		config.setProperty(prefix + "x", ""+npc.getLocation().getX());
		config.setProperty(prefix + "y", ""+npc.getLocation().getY());
		config.setProperty(prefix + "z", ""+npc.getLocation().getZ());
		config.setProperty(prefix + "yaw", ""+npc.getLocation().getYaw());
		config.setProperty(prefix + "pitch", ""+npc.getLocation().getPitch());
		config.setProperty(prefix + "invincible", ""+npc.isInvincible());
		config.setProperty(prefix + "personality", npc.getPersonality().getName());
		config.setProperty(prefix + "hand", npc.saveHand());
		config.setProperty(prefix + "rightclick", npc.saveMsg("click"));
		config.setProperty(prefix + "proximity", npc.saveMsg("proximity"));
		config.setProperty(prefix + "hurt", npc.saveMsg("hurt"));
		config.setProperty(prefix + "chat", npc.saveMsg("chat"));
		
		config.save();
		return;
	}
	
	protected void removeNpc(NaxcraftNpc npc){
		config.removeProperty(npc.getLocation().getWorld().getName() + "." + npc.getUniqueId());
		this.npcs.remove(npc.getUniqueId());
		npc.delete();
		config.save();
	}
	*/
	public Location location;
	public NPCManager m;
	
	public boolean runNpcCommand(CommandSender sender, String[] args){
		
		if(sender instanceof Player){
			
			if(location == null)
				 m = new NPCManager(plugin);
			
            Player player = (Player) sender;
            Location l = player.getLocation();

            if(!plugin.control.has(player, "npc")){
            	sender.sendMessage(String.format(Naxcraft.PERMISSIONS_FAIL, "/npc"));
            	return true;
			}
            
            if(args.length == 0) return false;
            
            if(m.getNPC("test") == null){
            	NPCEntity npc = m.spawnNPC(ChatColor.RED + "Naxville_NPC", player.getLocation(), "test");
            }
            
            location = player.getLocation();
            
            //m.moveNPC("test", location);
            m.pathFindNPC("test", location);
            
            return true;
		}
		
		return false;
	}
            /*
            // create npc-id npc-name
            if (args[0].equalsIgnoreCase("create") || args[0].equalsIgnoreCase("spawn") || args[0].equalsIgnoreCase("add")) {
            	String name = "Naxville_NPC";
                String id = "id"+id_increment;
                
                if(args.length == 2){
                	name = args[1];
                	 
                } else if(args.length == 3) {
                	if (this.npcs.get(args[2]) != null) {
                        player.sendMessage("This npc-id is already in use.");
                        return true;
                    }
                	id = args[2];
                }

                NaxcraftNpc npc = new NaxcraftNpc(id, name, player.getLocation());
                
                this.npcs.put(id, npc);
                this.saveNpc(npc);
                id_increment++;
                player.sendMessage(Naxcraft.SUCCESS_COLOR + "NPC named " + Naxcraft.DEFAULT_COLOR + name + Naxcraft.SUCCESS_COLOR + " created! His ID is " + Naxcraft.DEFAULT_COLOR + id + Naxcraft.SUCCESS_COLOR + ".");

                return true;

            } else if (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("del")){
            	if(args.length != 2){
            		return false;
            	}
            	
            	NaxcraftNpc npc = this.npcs.get(args[1]);
                if (npc != null) {
                	
                	npc.delete();
	                npcs.remove(npc.getUniqueId());
	                config.removeProperty(npc.getLocation().getWorld().getName() + "." + npc.getUniqueId());
	        		config.save();
	                player.sendMessage(Naxcraft.MSG_COLOR + "NPC id " + Naxcraft.DEFAULT_COLOR + npc.getUniqueId() + Naxcraft.MSG_COLOR + " deleted " + Naxcraft.SUCCESS_COLOR + "successfully" + Naxcraft.MSG_COLOR + ".");
	                return true;
                } else {
                	player.sendMessage(Naxcraft.MSG_COLOR + "NPC does " + Naxcraft.ERROR_COLOR + "not" + Naxcraft.MSG_COLOR + " exist.");
                	return true;
                }
                
            } else if (args[0].equalsIgnoreCase("identify") || args[0].equalsIgnoreCase("id")){
            	for(String id : this.npcs.keySet()){
            		if(getDistance(this.npcs.get(id).getLocation(), l) < 15){
            			this.npcs.get(id).say(this.npcs.get(id).getUniqueId() + ", sir. I am " + getDistance(this.npcs.get(id).getLocation(), l) + " blocks away from you.", player);
            			this.npcs.get(id).animateArmSwing();
            		}
            	}
            	return true;
            }
            
            NaxcraftNpc npc = this.npcs.get(args[0]);
            if (npc == null) {
            	player.sendMessage(Naxcraft.MSG_COLOR + "NPC does " + Naxcraft.ERROR_COLOR + "not" + Naxcraft.MSG_COLOR + " exist.");
                return true;
            }
            
            if (args[1].equalsIgnoreCase("move")) {
                if (args.length != 2) return false;
                npc.moveTo(l);
                
            	player.sendMessage(Naxcraft.MSG_COLOR + "NPC moved " + Naxcraft.SUCCESS_COLOR + "successfully" + Naxcraft.MSG_COLOR + ".");
            	return true;

            } else if(args[1].equalsIgnoreCase("item")){
            	if(args.length != 3) return false;
            	
            	Material item;
            	if(args[2].equalsIgnoreCase("0") || args[2].equalsIgnoreCase("air")){
            		//GIVE SOMEONE AIR? WELL LET'S JUST FUCKING CRASH EVERYTHING WHY DON'T WE.
	            	npc.putHand(null);
            		
            	} else {
            		try {
	            		int num = Integer.parseInt(args[2]);
	            		item = Material.getMaterial(num);
	            		
	            	} catch (NumberFormatException e){
	            		item = Material.getMaterial(args[2].toUpperCase());
	            	}
	            	
	            	if(item != null){
	            		npc.putHand(item);
	            		
	            	} else {
	            		player.sendMessage(args[2] + Naxcraft.MSG_COLOR + " is " + Naxcraft.ERROR_COLOR + "not" + Naxcraft.MSG_COLOR + " a proper material.");
	            	}
            	}
            	           	
            	
                return true;
                
            } else if(args[1].equalsIgnoreCase("list")){
            	//TODO: LIST INFO
            	return true;
            	
            } else if(args[1].equalsIgnoreCase("tp")){
            	player.teleport(npc.getLocation());
            	return true;
            	
            } else if(args[1].contains("person")){
            	if(args.length != 3) {
            		player.sendMessage(Naxcraft.ERROR_COLOR + "/npc <id> person[ality] <type>");
            		return true;
            	}
            	boolean exists = false;
            	for(String personality : this.personalities){
            		if(personality.equalsIgnoreCase(args[2])){
            			exists = true;
            		}
            	}
            	
            	if(exists){
            		npc.setPersonality(new NpcPersonality(args[2].toLowerCase()));
            		player.sendMessage(Naxcraft.MSG_COLOR + " Npc personality set " + Naxcraft.SUCCESS_COLOR + "successfully" + Naxcraft.MSG_COLOR + ".");
            		
            	} else {
            		player.sendMessage(args[2] + Naxcraft.MSG_COLOR + " is " + Naxcraft.ERROR_COLOR + "not" + Naxcraft.MSG_COLOR + " a personality type.");
            		player.sendMessage(Naxcraft.DEFAULT_COLOR + plugin.arrayToString(this.personalities, Naxcraft.MSG_COLOR + ", "));
            	}
            	return true;
            	
            } else if(args[1].contains("message") || args[1].contains("mes") || args[1].contains("msg")){
            	if(args.length < 4){
            		player.sendMessage(Naxcraft.ERROR_COLOR + "/npc <id> msg <hurt/click/near> <message>");
            		return true;
            	}
            	NpcInput type = NpcInput.UNSET;
            	
            	if(args[2].contains("hurt")){
            		type = NpcInput.HURT;
            	} if (args[2].contains("click")){
            		type = NpcInput.RIGHT_CLICK;
            	} if(args[2].contains("near") || args[2].contains("prox")){
            		type = NpcInput.PROXIMITY;
            	} if(args[2].contains("chat")){
            		type = NpcInput.CHAT;
            	}
            	
            	if(type != NpcInput.UNSET){
            		String mes[] = args;
            		mes[0] = "";
            		mes[1] = "";
            		mes[2] = "";
            		npc.setMsg(type, plugin.arrayToString(mes) );
            		player.sendMessage("Done");
            		
            	} else {
            		player.sendMessage(args[2] + Naxcraft.MSG_COLOR + " is " + Naxcraft.ERROR_COLOR + "not" + Naxcraft.MSG_COLOR + " a proper input type.");
            		String total = "";
            		for(NpcInput input : NpcInput.values()){
            			if(total != "") total += Naxcraft.MSG_COLOR + ", ";
            			total += Naxcraft.DEFAULT_COLOR + input.toString().toLowerCase();
            		}
            		player.sendMessage(Naxcraft.MSG_COLOR + "Possible input types: " + total);
            	}
            	return true;
            }
			
            return false;
            
		} else {
			System.out.println("Knock it off, you need to be a player to make NPCs.");
		}
		
		return false;
		*/
	//}
	
	public int getDistance (Location loc1, Location loc2){
		return (int) Math.ceil(Math.sqrt(Math.pow(loc1.getX() - loc2.getX(), 2) + Math.pow(loc1.getY() - loc2.getY(), 2) + Math.pow(loc1.getZ() - loc2.getZ(), 2)));
	}

}
