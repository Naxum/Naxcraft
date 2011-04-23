package com.naxville.naxcraft.land;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.naxville.naxcraft.Naxcraft;
import com.naxville.naxcraft.admin.NaxcraftConfiguration;

public class NaxcraftAreaCommand {
	public static Naxcraft plugin;
	private NaxcraftConfiguration config;
	protected HashMap<Integer, NaxcraftArea>areas;
	
	public NaxcraftAreaCommand(Naxcraft instance){
		plugin = instance;
		
		this.areas = new HashMap<Integer, NaxcraftArea>();
		
		loadAreaFile();
	}
	
	public boolean runAreaCommand(CommandSender sender, String[] args){
		if (!(sender instanceof Player)) {
			sender.sendMessage("Only players can create areas");
			return true;
		}
		Player player = (Player)sender;
		if (!plugin.control.has(player, "area")){
			player.sendMessage(String.format(Naxcraft.PERMISSIONS_FAIL, "/area"));
			return true;
		}
		
		if(args.length == 0) return false;
		
		/*
		if(args.length == 1 && args[0].equalsIgnoreCase("update")){
			updateOldAreas();
			return true;
		}*/
		
		if(args[0].equalsIgnoreCase("list")){
			if(args.length == 1){
				getOwnedAreas(player.getName().toLowerCase());
				
			} else if (args.length == 2){
				
				getOwnedAreas(args[1].toLowerCase());
				
			} else return false;
			
			return true;
		}
		
		if(args[0].equalsIgnoreCase("calc") || args[0].equalsIgnoreCase("calculate")){
			if(args.length == 1){
				if(plugin.position1.containsKey(player) && plugin.position2.containsKey(player)){
					
					int width = (plugin.position1.get(player).getBlockX() > plugin.position2.get(player).getBlockX()) ? plugin.position1.get(player).getBlockX() - plugin.position2.get(player).getBlockX() : plugin.position2.get(player).getBlockX() - plugin.position1.get(player).getBlockX();
					width = Math.abs(width);
					
					int length = (plugin.position1.get(player).getBlockZ() > plugin.position2.get(player).getBlockZ()) ? plugin.position1.get(player).getBlockZ() - plugin.position2.get(player).getBlockZ() : plugin.position2.get(player).getBlockZ() - plugin.position1.get(player).getBlockZ();
					length = Math.abs(length);
					
					int lots1 = (int)Math.ceil(width/10);
					lots1 += (width%10)>0 ? 1 : 0;
					
					int lots2 = (int)Math.ceil(length/10);
					lots2 += (length%10)>0 ? 1 : 0;
					
					int lots = lots1 * lots2;
					
					int diamonds = (int)Math.floor(lots/2);
					int iron = (lots%2)*16;
					
					player.sendMessage(Naxcraft.MSG_COLOR + "Area Calc: " + Naxcraft.DEFAULT_COLOR + width + Naxcraft.MSG_COLOR + "x" + Naxcraft.DEFAULT_COLOR + length + Naxcraft.MSG_COLOR +
										": " +Naxcraft.DEFAULT_COLOR + lots + " lots" + Naxcraft.MSG_COLOR + " for " + Naxcraft.DEFAULT_COLOR + diamonds + " diamonds" + Naxcraft.MSG_COLOR + ", and " + Naxcraft.DEFAULT_COLOR + iron + " iron" + Naxcraft.MSG_COLOR +
										".");
					
					
				} else {
					player.sendMessage(Naxcraft.ERROR_COLOR + "Both positions not set.");
				}
			}
		}
		
		if(args[0].equalsIgnoreCase("tp")){
			if(args.length != 2) return false;
			
			NaxcraftArea area = areas.get(Integer.parseInt(args[1])); //plugin.areas.get(Integer.parseInt(args[1])).split(";");
			
			double x = area.getX1();
			double y = 127; //area[1];
			double z = area.getZ1();
			
			if(plugin.tpCommand.teleport(player, x, y, z)){
				player.sendMessage(Naxcraft.SUCCESS_COLOR + "Successfully teleported to area " + args[1] + "!");
			} else {
				player.sendMessage(Naxcraft.ERROR_COLOR + "Error teleporting to area " + args[1] + ".");
			}
			
			return true;
		}
		
		if(plugin.position1.containsKey(player) && plugin.position2.containsKey(player)){
			
			if(args.length >= 1){
				if(args[0].equalsIgnoreCase("create")){					
					
					NaxcraftArea area = new NaxcraftArea(plugin, getNextPossibleName());
					
					List<Integer> pos1 = new ArrayList<Integer>();
						pos1.add(plugin.position1.get(player).getBlockX());
						pos1.add(plugin.position1.get(player).getBlockY());
						pos1.add(plugin.position1.get(player).getBlockZ());
						
					List<Integer> pos2 = new ArrayList<Integer>();
						pos2.add(plugin.position2.get(player).getBlockX());
						pos2.add(plugin.position2.get(player).getBlockY());
						pos2.add(plugin.position2.get(player).getBlockZ());
					
					area.setPos1(pos1);
					area.setPos2(pos2);
					
					areas.put(area.getName(), area);
					
					saveArea(area);
					
					player.sendMessage(Naxcraft.SUCCESS_COLOR + "Area " + Naxcraft.DEFAULT_COLOR + area.getName() + Naxcraft.SUCCESS_COLOR + " created!");
					
					return true;
				}
			}
			
		} else {
			player.sendMessage(Naxcraft.ERROR_COLOR + "You do not have both positions set!");
		}
		
		return true;
	}
	
	private void loadAreaFile(){
		File file = new File(plugin.filePath);
		try {
			if (!file.exists()) {
				file.mkdir();
			}
			file = new File(plugin.filePath + "Areas.yml");
			if (!file.exists()) {
				file.createNewFile();
				initalizeFile();
			}
		} catch (IOException Ex) {
			System.out.println("Shit, problem creating new areas file");
			return;
		}
		
		config = new NaxcraftConfiguration(file);
		config.load();
	}
	
	private void initalizeFile(){
		File file = new File(plugin.filePath + "Areas.yml");
		try {
			
			//System.out.println("I'm an idiot and rewriting a file I shouldn't.");
			
			BufferedWriter output = new BufferedWriter(new FileWriter(file));
			output.write("areas:");
			output.close();
		} catch (Exception Ex) {
			System.out.println("Error initalizing areas file.  Areas cant save >:(!");
		}
	}
	
	public boolean loadAreas(){
		try {
			List<String>areaList = config.getKeys("areas");
			NaxcraftArea area;
			//int i = 0;
			
			if(areaList == null) return true;
			
			for (String key : areaList){
				int new_key = Integer.parseInt(key);
				area = new NaxcraftArea(plugin, new_key);
	
				List<Integer> pos1 = config.getIntList("areas." + key + ".pos1", null);
				List<Integer> pos2 = config.getIntList("areas." + key + ".pos2", null);
				
				//System.out.println("Position 1 size: " + pos1.size());
				
				area.setPos1(pos1);
				area.setPos2(pos2);
				
				this.areas.put(area.getName(), area);
				//i++;
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
			//initalizeFile();
			return false;
		} 
		return true;
	}
	
	protected void saveArea(NaxcraftArea area){
		String key = String.valueOf(area.getName());
		//config.setProperty("areas." + key + ".pos1", area.savePos1());
		//config.setProperty("areas." + key + ".pos2", area.savePos2());
		
		Map<String, Object> areaInfo = new HashMap<String, Object>();
		areaInfo.put("pos1", area.savePos1());
		areaInfo.put("pos2", area.savePos2());
		config.setProperty("areas." + key, areaInfo);
		
		config.save();
		return;
	}
	
	protected void removeArea(NaxcraftArea area){
		config.removeProperty("area." + area.getName());
		config.save();
	}
	
	protected int getNextPossibleName(){
		int last = -1;
		
		if(!areas.isEmpty() && areas.size() >= 0){
			last = areas.get(areas.size()-1).getName();
		}
		last++;
		
		return last;
	}
	
	public int withinOldSchoolArea(Location location){
		int area = -1;
		int x = (int) Math.round(location.getX());
		int z = (int) Math.round(location.getZ());
		
		for(String derp : plugin.areas){
			String[] split = derp.split(";");
			
			if(( Integer.parseInt(split[0]) <= x && x <= Integer.parseInt(split[3]) ) || ( Integer.parseInt(split[3]) <= x && x <= Integer.parseInt(split[0]))){
				if(( Integer.parseInt(split[2]) <= z && z <= Integer.parseInt(split[5]) ) || ( Integer.parseInt(split[5]) <= z && z <= Integer.parseInt(split[2]))){
					return plugin.areas.indexOf(derp);
				}
			}
		}
		
		return area;
	}
	
	public boolean isOldSchoolOwner(String player, int area){
		String[] split = plugin.areas.get(area).split(";");
		String[] people = split[6].split(",");
		
		for(String owner : people){
			if(owner.equalsIgnoreCase(player)){
				return true;
			}
		}
		
		return false;
	}
	
	public String getOldSchoolOwners(int area){
		String owners = "";
		
		String[] split = plugin.areas.get(area).split(";");
		String[] people = split[6].split(",");
		
		for(String owner : people){
			owners += Naxcraft.COMMAND_COLOR + ", ";
			owners += plugin.getNickName(owner);
		}
		
		return owners;
	}
	
	public int withinArea(Location location){
		
		for(int key : areas.keySet()){
			NaxcraftArea area = areas.get(key);
			if(area.withinArea(null, location)){
				//System.out.print("Within a looped up area");
				return key;
			}
		}
		return -1;
	}
	
	public int getArea(Player player){
		
		for(int key : areas.keySet()){
			NaxcraftArea area = areas.get(key);
			if(area.withinArea(player.getWorld(), player.getLocation())){
				//System.out.print("Within a looped up area");
				return key;
			}
		}
		return -1;
	}
	
	public boolean withinArea(World world, Location location, int areaid){
		NaxcraftArea area = areas.get(areaid);
		if(area.withinArea(world, location)){
			//System.out.print("Within a specific group area");
			return true;
		}
		return false;
	}
	
	public boolean withinAreaY(World world, Location location, int areaID){
		NaxcraftArea area = areas.get(areaID);
		if(area.withinAreaY(world, location)){
			//System.out.print("Within a specific group area");
			return true;
		}
		return false;
	}
	
	public void getOwnedAreas(String name){
		name = name.toLowerCase();
		String message = "";
		int num = 0;
		
		for(String area : plugin.areas){
			if(isOldSchoolOwner(name.toLowerCase(), plugin.areas.indexOf(area))){
				if(message != "") message += Naxcraft.COMMAND_COLOR + ", ";
				message += Naxcraft.DEFAULT_COLOR + "" + plugin.areas.indexOf(area);
				num++;
			}
		}
		
		if(message != ""){
			plugin.getServer().getPlayer(name).sendMessage(plugin.nickNames.get(name) + Naxcraft.COMMAND_COLOR + " owns " + num + " areas: " + message + Naxcraft.COMMAND_COLOR + ".");
		} else {
			plugin.getServer().getPlayer(name).sendMessage(plugin.nickNames.get(name) + Naxcraft.COMMAND_COLOR + " owns no areas.");
		}
	}
	
	//SUPER OVERWRITES ANY PREXISTING INCREMENTING AREA IDS, REPLACED WITH OLD TEXT FILE'S DATA
	/*
	private void updateOldAreas(){
		int i = 0;
		for(String unsplit : plugin.areas){
			
			if(this.areas.containsKey(i)){
				this.removeArea(this.areas.get(i));
				this.areas.remove(i);
			}
			
			String[] split = unsplit.split(";");
			int x1 = Integer.parseInt(split[0]);
			int y1 = Integer.parseInt(split[1]);
			int z1 = Integer.parseInt(split[2]);
			
			int x2 = Integer.parseInt(split[3]);
			int y2 = Integer.parseInt(split[4]);
			int z2 = Integer.parseInt(split[5]);
			
			List<Integer> pos1 = new ArrayList<Integer>();
				pos1.add(x1);
				pos1.add(y1);
				pos1.add(z1);
				
			List<Integer> pos2 = new ArrayList<Integer>();
				pos2.add(x2);
				pos2.add(y2);
				pos2.add(z2);
				
			NaxcraftArea area = new NaxcraftArea(plugin, i);
				area.setPos1(pos1);
				area.setPos2(pos2);
			
			this.areas.put(i, area);
			this.saveArea(area);
			i++;
		}
		
		plugin.getServer().broadcastMessage(i + "" + Naxcraft.MSG_COLOR + " areas have been converted.");
	}*/
	
}
