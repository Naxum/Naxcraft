package com.naxville.naxcraft.rpg;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.naxville.naxcraft.Naxcraft;
import com.naxville.naxcraft.admin.NaxcraftConfiguration;

public class NaxInventory {
	public Naxcraft plugin;
	
	public NaxcraftConfiguration config;
	protected Map<World, HashMap<String, ItemStack[]>> inventories;
	protected Map<String, String> inWorld;
	
	public NaxInventory(Naxcraft instance){
		plugin = instance;
		
		inventories = new HashMap<World, HashMap<String, ItemStack[]>>();
		inWorld = new HashMap<String, String>();
		loadFile();
	}
	
	public void saveInventory(Player player, String world){		
		ItemStack[] inv = new ItemStack[40];
		
		for(int i = 0; i < 36; i++){
			inv[i] = player.getInventory().getItem(i);
		}
		
		for(int i = 0; i < 4; i++){
			if(i == 0){
				inv[(36+i)] = player.getInventory().getBoots();
			} else if (i == 1){
				inv[(36+i)] = player.getInventory().getLeggings();
			} else if (i == 2){
				inv[(36+i)] = player.getInventory().getChestplate();
			} else if (i == 3){
				inv[(36+i)] = player.getInventory().getHelmet();
			}
			
		}
		
		this.save(player, inv, world);
	}
	
	public void saveInventory(Player player){		
		ItemStack[] inv = new ItemStack[40];
		
		for(int i = 0; i < 36; i++){
			inv[i] = player.getInventory().getItem(i);
		}
		
		for(int i = 0; i < 4; i++){
			if(i == 0){
				inv[(36+i)] = player.getInventory().getBoots();
			} else if (i == 1){
				inv[(36+i)] = player.getInventory().getLeggings();
			} else if (i == 2){
				inv[(36+i)] = player.getInventory().getChestplate();
			} else if (i == 3){
				inv[(36+i)] = player.getInventory().getHelmet();
			}
			
		}
		
		this.save(player, inv);
	}
	
	public void handleInventory(Player player, String worldto){
		if(this.shouldLoadInventory(player, worldto)){
			this.saveInventory(player);
			this.wipe(player);
			this.saveWorld(player);
			this.load(player, worldto);
		}
	}
	
	public void handleInventoryJoin(Player player){
		/*
		 * Fix inventory because bukkit can't stand people logging into their correct worlds.
		 */
		
		String prefix = player.getWorld().getName() + "." + player.getName().toLowerCase() + ".";
		if(this.config.getProperty(prefix + "isworld") != null){
			if(this.config.getString(prefix + "set").equals("false"))
				return;
			
			if(this.config.getString(prefix + "isworld").equals("false")){
				
				//this.load(player, "world");
				//this.saveInventory(player, plugin.rpgWorld);
				//this.wipe(player);
				this.setWorld(player);
				//player.teleport(new Location(player.getWorld(), player.getWorld().getSpawnLocation().getX(), player.getWorld().getSpawnLocation().getY(), player.getWorld().getSpawnLocation().getZ()));
				//player.sendMessage(Naxcraft.MSG_COLOR + "You have been returned to the real world. Your inventory will be returned to you.");
			} 
		} else {
			this.setWorld(player);
		}
	}
	
	public boolean shouldLoadInventory(Player player, String worldto){
		if(!this.inWorld.containsKey(player.getName().toLowerCase())){
			this.saveWorld(player);
		}
		
		//plugin.log.info(player.getName() + " is going to " + worldto);
		
		if(!this.inWorld.get(player.getName().toLowerCase()).equalsIgnoreCase(worldto)){
			return true;
		}
		return false;
	}
	
	private void loadFile(){
		File file = new File(plugin.filePath);
		try {
			if (!file.exists()){
				file.mkdir();
			}
			file = new File(plugin.filePath + "Inventories.yml");
			if (!file.exists()){
				file.createNewFile();
				initalizeFile();
			}
		} catch (IOException Ex) {
			System.out.println("Error creating new Inventories file");
			return;
		}
		
		config = new NaxcraftConfiguration(file);
		config.load();
	}
	
	private void initalizeFile(){
		File file = new File(plugin.filePath + "Inventories.yml");
		try {			
			BufferedWriter output = new BufferedWriter(new FileWriter(file));
			//TODO: HEY, NO WORLDS HUH? COOL, BUKKIT.
			output.write("world:{}\r\n");
			output.write("rpg:{}");
			output.close();
		} catch (Exception e) {
			System.out.println("Problem initializing new Inventories file.");
			e.printStackTrace();
		}
	}
	
	public void wipe(Player player){
		player.getInventory().clear();
		player.getInventory().setChestplate(null);
		player.getInventory().setLeggings(null);
		player.getInventory().setHelmet(null);
		player.getInventory().setBoots(null);
	}
	
	public void load(Player player, String worldto){		
		String prefix = worldto.toLowerCase() + "." + player.getName().toLowerCase() + ".";
		if(config.getProperty(worldto.toLowerCase() + "." + player.getName().toLowerCase()+".set") == null || config.getString(worldto.toLowerCase() + "." + player.getName().toLowerCase()+".set").equals("false")){
			//config.save();
			return;
		}
		for(int i = 0; i < 40; i++){
			
			if(!config.getString(prefix + i + ".id").equals("0")){
				int id = Integer.parseInt(config.getString(prefix + i + ".id"));
				int amount = Integer.parseInt(config.getString(prefix + i + ".amount"));
				short damage = Short.parseShort(config.getString(prefix + i + ".damage"));
				if(i == 36) {
					player.getInventory().setBoots(new ItemStack(id, amount, damage));
				} else if (i == 37){
					player.getInventory().setLeggings(new ItemStack(id, amount, damage));
				} else if (i == 38){
					player.getInventory().setChestplate(new ItemStack(id, amount, damage));
				} else if (i == 39){
					player.getInventory().setHelmet(new ItemStack(id, amount, damage));
				} else {
					player.getInventory().setItem(i, new ItemStack(id, amount, damage));
				}
				
			}
			
		}
	}
	
	protected void save(Player player, ItemStack[] inv){	
		String prefix = player.getWorld().getName().toLowerCase() + "." + player.getName().toLowerCase() + ".";
		
		for(int i = 0; i < inv.length; i++){
			if(inv[i] == null){
				config.setProperty(prefix + i + ".id", "0");
				config.setProperty(prefix + i + ".amount", "0");
				config.setProperty(prefix + i + ".damage", "-1");
				continue;
			}
			int id = inv[i].getTypeId();
			int amount = inv[i].getAmount();
			short damage = inv[i].getDurability();
			
			config.setProperty(prefix + i + ".id", "" + id);
			config.setProperty(prefix + i + ".amount", "" + amount);
			config.setProperty(prefix + i + ".damage", "" + damage);
		}
		
		config.setProperty(prefix + "set", "true");
		config.save();
		return;
	}
	
	protected void save(Player player, ItemStack[] inv, String world){	
		String prefix = world.toLowerCase() + "." + player.getName().toLowerCase() + ".";
		
		for(int i = 0; i < inv.length; i++){
			if(inv[i] == null){
				config.setProperty(prefix + i + ".id", "0");
				config.setProperty(prefix + i + ".amount", "0");
				config.setProperty(prefix + i + ".damage", "-1");
				continue;
			}
			int id = inv[i].getTypeId();
			int amount = inv[i].getAmount();
			short damage = inv[i].getDurability();
			
			config.setProperty(prefix + i + ".id", "" + id);
			config.setProperty(prefix + i + ".amount", "" + amount);
			config.setProperty(prefix + i + ".damage", "" + damage);
		}
		
		config.setProperty(prefix + "set", "true");
		config.save();
		return;
	}
	
	protected void saveWorld(Player player){
		if(player.getWorld().getName().equals("world")){
			config.setProperty("world."+player.getName().toLowerCase()+".isworld", "false");
			config.setProperty("rpg."+player.getName().toLowerCase()+".isworld", "true");
			this.inWorld.put(player.getName().toLowerCase(), "rpg");
			
		} else {
			config.setProperty("world."+player.getName().toLowerCase()+".isworld", "true");
			config.setProperty("rpg."+player.getName().toLowerCase()+".isworld", "false");
			this.inWorld.put(player.getName().toLowerCase(), "world");
		}
		
		if(config.getProperty("world."+player.getName().toLowerCase()+".set") == null){
			config.setProperty("world."+player.getName().toLowerCase()+".set", "true");
			String prefix = "world."+player.getName().toLowerCase()+".";
			for(int i = 0; i < 40; i++){
				config.setProperty(prefix + i + ".id", "0");
				config.setProperty(prefix + i + ".amount", "0");
				config.setProperty(prefix + i + ".damage", "-1");
			}
		}
		
		if(config.getProperty("rpg."+player.getName().toLowerCase()+".set") == null){
			config.setProperty("rpg."+player.getName().toLowerCase()+".set", "true");
			String prefix = "rpg."+player.getName().toLowerCase()+".";
			for(int i = 0; i < 40; i++){
				config.setProperty(prefix + i + ".id", "0");
				config.setProperty(prefix + i + ".amount", "0");
				config.setProperty(prefix + i + ".damage", "-1");
			}
		}
		
		config.save();
	}
	
	protected void setWorld(Player player){
		if(player.getWorld().getName().equals("world")){
			config.setProperty("world."+player.getName().toLowerCase()+".isworld", "true");
			config.setProperty("rpg."+player.getName().toLowerCase()+".isworld", "false");
			this.inWorld.put(player.getName().toLowerCase(), "world");
			
		} else {
			config.setProperty("world."+player.getName().toLowerCase()+".isworld", "false");
			config.setProperty("rpg."+player.getName().toLowerCase()+".isworld", "true");
			this.inWorld.put(player.getName().toLowerCase(), "rpg");
		}
		
		if(config.getProperty("world."+player.getName().toLowerCase()+".set") == null){
			config.setProperty("world."+player.getName().toLowerCase()+".set", "true");
			String prefix = "world."+player.getName().toLowerCase()+".";
			for(int i = 0; i < 40; i++){
				config.setProperty(prefix + i + ".id", "0");
				config.setProperty(prefix + i + ".amount", "0");
				config.setProperty(prefix + i + ".damage", "-1");
			}
		}
		
		if(config.getProperty("rpg."+player.getName().toLowerCase()+".set") == null){
			config.setProperty("rpg."+player.getName().toLowerCase()+".set", "true");
			String prefix = "rpg."+player.getName().toLowerCase()+".";
			for(int i = 0; i < 40; i++){
				config.setProperty(prefix + i + ".id", "0");
				config.setProperty(prefix + i + ".amount", "0");
				config.setProperty(prefix + i + ".damage", "-1");
			}
		}
		
		config.save();
	}
	
	@Deprecated
	protected void remove(Player player){
		/*
		 * Why would I need this?
		 */
		config.removeProperty(player.getWorld().getName().toLowerCase() + "." + player.getName().toLowerCase());
		this.inventories.remove(player.getName().toLowerCase());
		//this.npcs.remove(npc.getUniqueId());
		//npc.delete();
		config.save();
	}
	
}
