package com.naxville.naxcraft.land;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.naxville.naxcraft.Naxcraft;

public class NaxcraftArea {

	public static Naxcraft plugin;
	
	private int[] pos1 = {0, 0, 0};
	private int[] pos2 = {0, 0, 0};
	private int name = 0;
	
	private List<Integer>posSave1 = new ArrayList<Integer>();
	private List<Integer>posSave2 = new ArrayList<Integer>();
	
	public NaxcraftArea(Naxcraft instance, int key){
		plugin = instance;
		this.name = key;
	}
	
	protected void setPos1(List<Integer> pos){
		pos1[0] = pos.get(0);
		pos1[1] = pos.get(1);
		pos1[2] = pos.get(2);
		
		posSave1 = pos;
	}
	
	protected void setPos2(List<Integer> pos){
		pos2[0] = pos.get(0);
		pos2[1] = pos.get(1);
		pos2[2] = pos.get(2);
		
		posSave2 = pos;
	}
	
	protected int getName(){
		return this.name;
	}
	
	protected int[] getPos1(){
		return this.pos1;
	}
	
	protected int[] getPos2(){
		return this.pos2;
	}
	
	protected int getX1(){
		return this.pos1[0];
	}
	protected int getY1(){
		return this.pos1[1];
	}
	protected int getZ1(){
		return this.pos1[2];
	}
	
	protected int getX2(){
		return this.pos2[0];
	}
	protected int getY2(){
		return this.pos2[1];
	}
	protected int getZ2(){
		return this.pos2[2];
	}
	
	protected boolean withinArea(World world, Location location){
		int x = (int) Math.round(location.getX());//.getBlockX();
		//int y = player.getLocation().getBlockY();
		int z = (int) Math.round(location.getZ());
		
		if(( this.pos2[0] <= x && x <= this.pos1[0] ) || ( this.pos1[0] <= x && x <= this.pos2[0])){
			if(( this.pos2[2] <= z && z <= this.pos1[2] ) || ( this.pos1[2] <= z && z <= this.pos2[2])){
				
				//INSIDE AN AREA's X and Z
				return true;
				
			}
		}
		
		return false;
	}
	
	protected boolean withinAreaY(World world, Location location){
		int y = (int) Math.round(location.getY());
		
		if(( this.pos2[1] <= y && y <= this.pos1[1] ) || ( this.pos1[1] <= y && y <= this.pos2[1])){
				
				//INSIDE AN AREA's Y
				return true;
				
		}
		
		return false;
	}
	
	protected List<Integer> savePos1 (){
		return this.posSave1;
	}
	
	protected List<Integer> savePos2 (){
		return this.posSave2;
	}

	public void getBlocks(List<Block> blocks, Boolean isHigh, int yBegin) {
		int width = (this.pos1[0] > this.pos2[0]) ? this.pos1[0] - this.pos2[0] : this.pos2[0] - this.pos1[0];
		int xstart = (this.pos1[0] > this.pos2[0]) ? this.pos2[0] : this.pos1[0];
		width = Math.abs(width);
		
		int height = 0;
		int ystart = yBegin;
		if(isHigh){
			height = 127;
			
		} else {
			height = (this.pos1[1] > this.pos2[1]) ? this.pos1[1] - this.pos2[1] : this.pos2[1] - this.pos1[1];
			ystart = (this.pos1[1] > this.pos2[1]) ? this.pos2[1] : this.pos1[1];
			if(ystart < yBegin) {
				//height = height - (yBegin-ystart);
				ystart = yBegin;
			}
			height = Math.abs(height);
		}
		
		int length = (this.pos1[2] > this.pos2[2]) ? this.pos1[2] - this.pos2[2] : this.pos2[2] - this.pos1[2];
		int zstart = (this.pos1[2] > this.pos2[2]) ? this.pos2[2] :this.pos1[2];
		length = Math.abs(length);
		
		for(int x = xstart; x < width; x++){
			for(int y = ystart; y < height; y++){
				if(y > 127) break;
				for(int z = zstart; z < length; z++){
					blocks.add(plugin.getServer().getWorld("world").getBlockAt(x, y, z));
				}
			}
		}	
	}
	
	public Point[] getCorners()
	{
		Point[] corners = new Point[4];
		
		corners[0] = new Point(pos1[0], pos1[1]);
		corners[1] = new Point(pos1[0] - pos2[0], pos2[1]);
		corners[2] = new Point(pos1[0], pos1[1] - pos2[1]);
		corners[3] = new Point(pos2[0], pos2[1]);
		
		return corners;
	}
}
