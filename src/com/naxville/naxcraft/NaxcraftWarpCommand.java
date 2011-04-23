package com.naxville.naxcraft;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;

import com.naxville.naxcraft.land.NaxcraftGroup;

public class NaxcraftWarpCommand {

	private static Naxcraft plugin;
	public Map<String, Location> warps = new HashMap<String, Location>();
	
	public NaxcraftWarpCommand(Naxcraft instance){
		plugin = instance;
		
		loadWarpFile();
			
	}
	
	public boolean runWarpCommand(CommandSender sender, String[] args){
		if(args.length == 1){
			if(!(sender instanceof Player)){
				sender.sendMessage(Naxcraft.NOT_A_PLAYER);
			}
			Player player = (Player) sender;
			
			if(!player.getWorld().getName().equals("world")){
				player.sendMessage(Naxcraft.MSG_COLOR + "You cannot warp out of here!");
				return true;
			}
			
		if(plugin.control.has(player, "warp")){
			
			String groupName = plugin.groupCommand.requestGroup(player.getWorld(), player.getLocation());
			if(groupName != ""){
				NaxcraftGroup group = plugin.groupCommand.getList(player).get(groupName);
				if(group.isFlag("jail")){
					if(!plugin.superCommand.isSuper(player.getName())){
						player.sendMessage(Naxcraft.MSG_COLOR + "You cannot warp out of a jail!");
						return true;
					}
				}
			}
			
			//TODO: MULTI WARPS?
			int totalWarps = 0;
			String warpName = args[0].toLowerCase();
			if(this.warps.containsKey(args[0].toLowerCase())){
				totalWarps = 1;
				
			}
				
			if(totalWarps > 0){							
				if(!plugin.warpingPlayers.containsKey(player.getName())){
					if(plugin.superCommand.isSuper(player.getName())){
						this.warpPlayer(player, warpName);
						
					} else {
						player.sendMessage(Naxcraft.COMMAND_COLOR + "You start to concentrate on " + args[0] + "!");
						plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new NaxcraftWarpRunnable(plugin, player, warpName), (5 * 20));
						
					}
				}
				
			} else {
				player.sendMessage(Naxcraft.ERROR_COLOR + "That warp point does not exist.");
			}
				
				return true;
			} else {
				sender.sendMessage(String.format(Naxcraft.PERMISSIONS_FAIL, "/warp"));
			}
		}
		return false;
	}
	
	public void warpPlayer(Player player, String warp){
		if(!this.warps.containsKey(warp)){
			if(warp.equalsIgnoreCase("home") || warp.equalsIgnoreCase("spawn")){
				Location loc = player.getWorld().getSpawnLocation();
				
				/*
				 * Fixes spawning underground, tries to find an area to spawn.
				 */
				
				boolean okayToSpawn = false;
				
				int i = 0;
				while(!okayToSpawn){
					if(loc.getBlock().getType() != Material.AIR || loc.getBlock().getRelative(0, 1, 0).getType() != Material.AIR){
						loc.setY(loc.getY()+1);
						
					} else {
						okayToSpawn = true;
					}
					
					i++;
					
					if(i > 130){
						player.sendMessage(Naxcraft.ERROR_COLOR + "There is an issue with warping home right now.");
						plugin.log.severe("WARNING! " + player.getName() + " attempted to go to /home, but I could not find a decent spawn point!");
						return;
					}
				}
				
				loc.setX(loc.getX()+.5);
				loc.setZ(loc.getZ()+.5);
				
				player.teleport(loc);
				
			} else {
				System.out.println("What the fuck " + player.getName() + " tried to warp to " + warp + " but that doesn't exist?!");
				return;
			}
		} else {
			player.teleport(this.warps.get(warp));
		}
		
		
	}
	private void loadWarpFile(){
		File file = new File(plugin.filePath);
		try {
			if (!file.exists()){
				file.mkdir();
			}
			file = new File(plugin.filePath + "Warps.txt");
			if (!file.exists()){
				file.createNewFile();
				initalizeFile();
			}
		} catch (IOException Ex) {
			System.out.println("Shit, problem creating new warps file");
			return;
		}
	}
	
	private void initalizeFile(){
		//File file = new File(plugin.filePath + "Warps.txt");
		/*
		try {
			//for(World world : plugin.getServer().getWorlds()){
				BufferedWriter output = new BufferedWriter(new FileWriter(file));
				output.write(world.getName() + ":");
				output.close();
			//}
		} catch (Exception Ex) {
			System.out.println("Error initalizing new warps file.");
		}*/
	}
	
	public boolean loadWarps(){
		
		File warpFile = new File(plugin.filePath + plugin.warpFile);
	    
		if(warpFile.exists())
		{
			try {
				BufferedReader input =  new BufferedReader(new FileReader(warpFile));
				String line = null; 
				
				try{
					while (( line = input.readLine()) != null){
						String[] split = line.split(":");
						
						double x = Double.parseDouble(split[1]);
						double y = Double.parseDouble(split[2]);
						double z = Double.parseDouble(split[3]);
						float yaw = Float.parseFloat(split[4]);
						float pitch = Float.parseFloat(split[5]);
						
						warps.put(split[0], new Location(plugin.getServer().getWorld("world"), x, y, z, yaw, pitch));
					}
				} catch (Exception e){
					e.printStackTrace();
				}
				input.close();
				
			} catch (IOException e){
			  e.printStackTrace();
			  return false;
			}
		}
		return true;
	}
	
	public boolean writeWarps(){
		File warpFile = new File(plugin.filePath + plugin.warpFile);
	    
		try {
			BufferedWriter output = new BufferedWriter(new FileWriter(warpFile));
			
			try{
				for(String warp : warps.keySet()){
					output.append(saveWarp(warp));
					output.append("\r\n");	
				}
			} catch (Exception e){
				e.printStackTrace();
			}
			output.close();
			
		} catch (IOException e){
		  e.printStackTrace();
		  return false;
		}
		
		return true;
	}

	private CharSequence saveWarp(String warp) {
		String save = "";
		
		Location warpLoc = this.warps.get(warp);
		
		save += warp + ":" + warpLoc.getX() + ":" + warpLoc.getY() + ":" + warpLoc.getZ() + ":" + warpLoc.getYaw() + ":" + warpLoc.getPitch();
		
		return save;
	}
}
