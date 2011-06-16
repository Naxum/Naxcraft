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
		if (!plugin.playerManager.getPlayer(player).rank.isAdmin()){
			player.sendMessage(String.format(Naxcraft.PERMISSIONS_FAIL, "/area"));
			return true;
		}
		
		if(args.length == 0) return false;
		
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
	
	public NaxcraftArea getArea(int x)
	{
		if(areas.containsKey(x))
		{
			return areas.get(x);
		}
		else
		{
			return null;
		}
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
}
