package com.naxville.naxcraft.shops;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.block.CraftChest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.naxville.naxcraft.NaxUtil;
import com.naxville.naxcraft.Naxcraft;

public class Shop
{
	protected int id;
	
	protected ShopManager sm;
	
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
	
	public Shop(ShopManager sm, int id, Location location, Material good, int goodAmount, Material price, int priceAmount, String[] lines, String owner)
	{
		this.sm = sm;
		this.id = id;
		this.owner = owner;
		
		this.location = location;
		this.world = location.getWorld();
		
		this.line1 = "Buy";
		this.line2 = lines[1];
		this.line3 = lines[2];
		this.line4 = lines[3];
		
		this.good = good;
		this.goodAmount = goodAmount;
		
		this.price = price;
		this.priceAmount = priceAmount;
	}
	
	public void buy(Player player)
	{
		if (!haveEnoughMaterial())
		{
			player.sendMessage(Naxcraft.MSG_COLOR + "Sorry, this shop is low on stock.");
			return;
		}
		else if (!haveRoomForMoney())
		{
			player.sendMessage(Naxcraft.MSG_COLOR + "Sorry, this shop has no room for your money.");
			return;
		}
		
		if (NaxUtil.charge(player, price, priceAmount))
		{
			give(player, new ItemStack(good, goodAmount));
			player.sendMessage(Naxcraft.MSG_COLOR + "You " + Naxcraft.SUCCESS_COLOR + "successfully" + Naxcraft.MSG_COLOR + " bought " + Naxcraft.DEFAULT_COLOR + goodAmount + " " + good.toString().replace("_", " ").toLowerCase() + Naxcraft.MSG_COLOR + ".");
			
			Block block = world.getBlockAt(location.clone().add(0, -1, 0));
			if (block.getType() == Material.CHEST)
			{
				CraftChest chest = (CraftChest) block.getState();
				chest.getInventory().removeItem(new ItemStack(good, goodAmount));
				
				chest.getInventory().addItem(new ItemStack(price, priceAmount));
			}
			
		}
		else if (player.getGameMode() != GameMode.CREATIVE) // creative is handled in the charge.
		{
			player.sendMessage(Naxcraft.MSG_COLOR + "You are short " + Naxcraft.DEFAULT_COLOR + priceAmount + " " + price.toString().replace("_", " ").toLowerCase() + Naxcraft.MSG_COLOR + " for this purchase.");
		}
	}
	
	private boolean haveEnoughMaterial()
	{
		Block block = world.getBlockAt(location.clone().add(0, -1, 0));
		if (block.getType() == Material.CHEST)
		{
			CraftChest chest = (CraftChest) block.getState();
			
			int goodAmountLeft = goodAmount;
			
			for (int slot : chest.getInventory().all(good).keySet())
			{
				goodAmountLeft -= chest.getInventory().getItem(slot).getAmount();
				
				if (goodAmountLeft <= 0) { return true; }
			}
			
			return false;
		}
		else
		{
			if (sm.plugin.playerManager.getPlayer(owner).rank.isAdmin()) { return true; }
			
			return false;
		}
	}
	
	private boolean haveRoomForMoney()
	{
		Block block = world.getBlockAt(location.clone().add(0, -1, 0));
		if (block.getType() == Material.CHEST)
		{
			CraftChest chest = (CraftChest) block.getState();
			
			HashMap<Integer, ItemStack> overflow = chest.getInventory().addItem(new ItemStack(price, priceAmount));
			
			chest.getInventory().removeItem(new ItemStack(price, priceAmount));
			
			if (overflow == null || overflow.isEmpty())
			{
				return true;
			}
			else
			{
				System.out.println(overflow.size() + " size");
				System.out.println(overflow.toString());
				
				return false;
			}
			
		}
		else
		{
			if (sm.plugin.playerManager.getPlayer(owner).rank.isAdmin()) { return true; }
			
			return false;
		}
	}
	
	@SuppressWarnings("deprecation")
	public void give(Player player, ItemStack item)
	{
		
		int first = player.getInventory().firstEmpty();
		if (first < 0)
		{
			player.getWorld().dropItem(player.getLocation(), item);
			
		}
		else
		{
			player.getInventory().addItem(item);
		}
		
		// TODO: THEY GUNNA UPDATE THIS OR WHAT?
		player.updateInventory();
	}
	
	public Location getLocation()
	{
		return this.location;
	}
	
	public String saveX()
	{
		return this.location.getX() + "";
	}
	
	public String saveY()
	{
		return this.location.getY() + "";
	}
	
	public String saveZ()
	{
		return this.location.getZ() + "";
	}
	
	public String[] getLines()
	{
		String[] lines = { line1, line2, line3, line4 };
		return lines;
	}
	
	public String saveGood()
	{
		return this.good.toString();
	}
	
	public int saveGoodAmount()
	{
		return this.goodAmount;
	}
	
	public String savePrice()
	{
		return this.price.toString();
	}
	
	public int savePriceAmount()
	{
		return this.priceAmount;
	}
	
	public String saveOwner()
	{
		return this.owner;
	}
	
	public int getId()
	{
		return this.id;
	}
}
