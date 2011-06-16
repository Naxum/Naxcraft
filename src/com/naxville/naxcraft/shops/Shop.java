package com.naxville.naxcraft.shops;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.naxville.naxcraft.Naxcraft;

public class Shop {
	protected int id;
	
	public World world;
	protected Location location;
	protected String line1;
	protected String line2;
	protected String line3;
	protected String line4;
	
	protected Material good;
	protected int goodAmount;
	
	protected Material price;
	protected int priceAmount;
	
	protected String owner;
	
	protected Map<String, Material> materials = new HashMap<String, Material>();
	
	public Shop (int id, Location location, Material good, int goodAmount, Material price, int priceAmount, String[] lines, String owner){
		this.id = id;
		this.owner = owner;
		
		this.location = location;
		this.world = location.getWorld();
		
		this.line1 = lines[0];
		this.line2 = lines[1];
		this.line3 = lines[2];
		this.line4 = lines[3];
		
		this.good = good;
		this.goodAmount = goodAmount;
		
		this.price = price;
		this.priceAmount = priceAmount;
	}
	
	public void buy(Player player){
		int priceleft = priceAmount;
		List<Integer> slotsToDestroy = new ArrayList<Integer>();
		
		boolean enoughMoney = false;
		
		for(int slot : player.getInventory().all(price).keySet()){
			priceleft -= player.getInventory().getItem(slot).getAmount();
			if(priceleft > 0) {
				slotsToDestroy.add(slot);
				
			} else if(priceleft == 0) {
				slotsToDestroy.add(slot);
				enoughMoney = true;
				break;
				
			} else if(priceleft < 0) {
				slotsToDestroy.add(slot);
				enoughMoney = true;
				break;
			}
		}
		
		if(enoughMoney){
			for(int slot : slotsToDestroy){
				player.getInventory().setItem(slot, null);
			}
			if(priceleft < 0){
				give(player, new ItemStack(price, priceleft*-1));
			}
			
			give(player, new ItemStack(good, goodAmount));
			player.sendMessage(Naxcraft.MSG_COLOR + "You " + Naxcraft.SUCCESS_COLOR + "successfully" + Naxcraft.MSG_COLOR + " bought " + Naxcraft.DEFAULT_COLOR + goodAmount + " " + good.toString().replace("_", " ").toLowerCase() + Naxcraft.MSG_COLOR + ".");
			
		} else {
			player.sendMessage(Naxcraft.MSG_COLOR + "You are short " + Naxcraft.DEFAULT_COLOR + priceAmount + " " + price.toString().replace("_", " ").toLowerCase() + Naxcraft.MSG_COLOR + " for this purchase.");
		}
	}
	
	@SuppressWarnings("deprecation")
	public void give(Player player, ItemStack item){
		
		int first = player.getInventory().firstEmpty();
		if(first < 0){
			player.getWorld().dropItem(player.getLocation(), item);
			
		} else{
			player.getInventory().addItem(item);
		}
		
		//TODO: THEY GUNNA UPDATE THIS OR WHAT?
		player.updateInventory();
		
	}
	public Location getLocation(){
		return this.location;
	}
	
	public String saveX(){
		return this.location.getX() + "";
	}
	
	public String saveY(){
		return this.location.getY() + "";
	}
	
	public String saveZ(){
		return this.location.getZ() + "";
	}
	
	public String[] getLines(){
		String[] lines ={line1, line2, line3, line4}; 
		return lines;
	}
	
	public String saveGood(){
		return this.good.toString();
	}
	
	public int saveGoodAmount(){
		return this.goodAmount;
	}
	
	public String savePrice(){
		return this.price.toString();
	}
	
	public int savePriceAmount(){
		return this.priceAmount;
	}
	
	public String saveOwner(){
		return this.owner;
	}

	public int getId() {
		return this.id;
	}
}
