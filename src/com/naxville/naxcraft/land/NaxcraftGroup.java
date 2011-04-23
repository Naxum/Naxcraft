package com.naxville.naxcraft.land;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;

import com.naxville.naxcraft.Naxcraft;

public class NaxcraftGroup {

	public static Naxcraft plugin;
	
	private String name;
	private String worldName;
	private String type;
	private String owner;
	private List<Integer> areas;
	private HashMap<String, String> flags;
	
	public NaxcraftGroup(Naxcraft instance, String gname){
		plugin = instance;
		this.name = gname;
	}
	
	public String getName(){
		return this.name;
	}
	
	public String getWorldName(){
		return this.worldName;
	}
	
	protected String getType(){
		return this.type;
	}
	
	public String getOwner(){
		return this.owner;
	}
	
	protected List<Integer> getAreas(){
		return this.areas;
	}
	
	protected HashMap<String, String> getFlags(){
		return this.flags;
	}
	
	public String getOwners(){
		String owners = "";
		if(this.type.equalsIgnoreCase("player")){
			for(String person : this.owner.split(",")){
				owners += plugin.getNickName(person);
			}
			
		} else if (this.type.equalsIgnoreCase("clan")){
			
			List<String>members = plugin.clanCommand.getClan(this.owner).getAllMembers();
			
			for(String member : members){
				if(owners != "") owners += Naxcraft.ERROR_COLOR + ", ";
				owners += plugin.getNickName(member);
			}
		}
		return owners;
	}
	
	public boolean isOwner(String player){
		player = player.toLowerCase();
		
		if(this.type.equalsIgnoreCase("player")){
			
			for(String person : this.owner.split(",")){
				if(player.equalsIgnoreCase(person)){
					return true;
				}
			}
			
		} else if (this.type.equalsIgnoreCase("clan")){
			return plugin.clanCommand.getClan(this.owner).isMember(player);
		}
		
		return false;
	}
	
	protected boolean addOwner(String owner){
		owner = owner.toLowerCase();
		
		String[] split = getOwner().split(",");
		for(String player : split){
			if(player.equals(owner)){
				return false;
			}
		}
		
		this.owner += ","+owner;
		return true;
	}
	
	protected boolean removeOwner(String owner){
		owner = owner.toLowerCase();
		String[] split = getOwner().split(",");
		
		String newOwners = "";
		boolean exists = false;
		
		for(String player : split){
			if(player.equals(owner)){
				exists = true;
			}
			if(!newOwners.equals("")) newOwners += ",";
			newOwners += player;
		}
		
		this.setOwner(newOwners);
		return exists;
	}
	
	protected boolean addArea(String area){
		if(!this.areas.contains(Integer.parseInt(area))){
			this.areas.add(Integer.parseInt(area));
			return true;
		}
		return false;
	}
	
	protected boolean removeArea(String area){
		int area2 = Integer.parseInt(area);
		if(this.areas.contains(area2)){
			this.areas.remove(this.areas.indexOf(area2));
			return true;
		}
		
		return false;
	}
 	
	public boolean isSafe(){
		return Boolean.parseBoolean(this.flags.get("safe"));
	}
	
	protected boolean isHigh(){
		return Boolean.parseBoolean(this.flags.get("high"));
	}
	
	public boolean isHurt(){
		return Boolean.parseBoolean(this.flags.get("hurt"));
	}
	
	public boolean isLock(){
		return Boolean.parseBoolean(this.flags.get("lock"));
	}
	
	public boolean isSanc(){
		return Boolean.parseBoolean(this.flags.get("sanc"));
	}
	
	public boolean isFlag(String flag){
		flag = flag.toLowerCase();
		if(this.flags.containsKey(flag)){
			return Boolean.parseBoolean(this.flags.get(flag));
		}
		
		return false;
	}
	
	protected String toggleFlag(String flag){
		String value = "";
		if(this.flags.containsKey(flag)){
			if(this.flags.get(flag).equalsIgnoreCase("true")){
				value = "false";
				this.flags.put(flag, value);
			} else {
				value = "true";
				this.flags.put(flag, value);
			}
		} else value = "error";
		return value;
	}
	
	protected void setName(String name){
		this.name = name;
	}
	
	protected void setWorldName(String name){
		this.worldName = name;
	}
	
	protected void setType(String type){
		this.type = type.toLowerCase();
	}
	
	protected void setOwner(String owner){
		this.owner = owner.toLowerCase();
	}
	
	protected void setAreas(List<Integer>areas){
		this.areas = areas;
	}
	
	protected void setFlags(HashMap<String, String>flags){
		this.flags = flags;
		
		for(String flag : plugin.groupCommand.allFlags){
			if(!this.flags.containsKey(flag)){
				this.flags.put(flag, "false");
			}
		}
	}
	
	protected String saveName(){
		return this.name;
	}
	
	protected String saveType(){
		return this.type;
	}
	
	protected String saveOwner(){
		return this.owner;
	}
	
	protected Object saveAreas(){
		if (this.areas == null || this.areas.isEmpty()) return "";
		return this.areas;
	}
	
	protected HashMap<String, String> saveFlags(){
		if(this.flags == null || this.flags.isEmpty()){
			for(String flag : plugin.groupCommand.allFlags){
				this.flags.put(flag, "false");	
			}
		}
		
		return this.flags;
	}

	public boolean withinGroup(World world, Location location) {
		for(int area : this.areas){
			//is the location within the areas' x and z values?
			if(plugin.areaCommand.withinArea(world, location, area)){
				
				//what about the y value?
				if(plugin.areaCommand.withinAreaY(world, location, area)){
					
					return true;
					
				//no? Well is it high?
				} else {
					
					return isHigh();
					
				}
			}
		}
		return false;
	}
}
