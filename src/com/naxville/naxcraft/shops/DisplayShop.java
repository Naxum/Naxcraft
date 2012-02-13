package com.naxville.naxcraft.shops;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.naxville.naxcraft.NaxColor;
import com.naxville.naxcraft.NaxUtil;
import com.naxville.naxcraft.Naxcraft;

public class DisplayShop
{
	public int id;
	public OfflinePlayer owner;
	public ItemStack good, cost;
	public Item goodItem, costItem;
	public Block sign;
	public World world;
	public Block[] chests, shelves;
	
	/**
	 * Called when a shop is created by placing a sign. Will spawn the Item entities here.
	 * 
	 * @param owner
	 * @param sign
	 * @param structure
	 * @param good
	 * @param cost
	 */
	
	public DisplayShop(int id, OfflinePlayer owner, Block sign, Block[] shelves, Block[] chests, ItemStack good, ItemStack cost)
	{
		this.id = id;
		this.owner = owner;
		this.sign = sign;
		this.world = sign.getWorld();
		this.shelves = shelves;
		this.chests = chests;
		this.good = good;
		this.cost = cost;
		
		ItemStack temp = good.clone();
		temp.setAmount(1);
		
		costItem = world.dropItem(shelves[1].getLocation().add(0.5, 0.8, 0.5), temp);
		costItem.setVelocity(new Vector(0, 0, 0));
		
		temp = cost.clone();
		temp.setAmount(1);
		
		goodItem = world.dropItem(shelves[0].getLocation().add(0.5, 0.8, 0.5), temp);
		goodItem.setVelocity(new Vector(0, 0, 0));
	}
	
	public DisplayShop(int id, OfflinePlayer owner, Block sign, Block[] shelves, Block[] chests, ItemStack good, ItemStack cost, Item goodItem2, Item costItem2)
	{
		this.id = id;
		this.owner = owner;
		this.sign = sign;
		this.world = sign.getWorld();
		this.shelves = shelves;
		this.chests = chests;
		this.good = good;
		this.cost = cost;
		this.goodItem = goodItem2;
		this.costItem = costItem2;
		
		if (costItem == null)
		{
			ItemStack temp = good.clone();
			temp.setAmount(1);
			
			costItem = world.dropItem(shelves[1].getLocation().add(0.5, 0.8, 0.5), temp);
			costItem.setVelocity(new Vector(0, 0, 0));
		}
		
		if (goodItem == null)
		{
			ItemStack temp = cost.clone();
			temp.setAmount(1);
			
			goodItem = world.dropItem(shelves[0].getLocation().add(0.5, 0.8, 0.5), temp);
			goodItem.setVelocity(new Vector(0, 0, 0));
		}
	}
	
	@SuppressWarnings("deprecation")
	public void buy(Player player)
	{
		if (!isSuperShop())
		{
			if (!haveEnoughGoods())
			{
				player.sendMessage(Naxcraft.MSG_COLOR + "Sorry, this shop is low on stock.");
				return;
			}
			else if (!haveRoomForMoney())
			{
				player.sendMessage(Naxcraft.MSG_COLOR + "Sorry, this shop has no room for your money.");
				return;
			}
		}
		
		if (NaxUtil.charge(player, cost))
		{
			if (!isSuperShop())
			{
				NaxUtil.removeFromInventory(((Chest) chests[1].getState()).getInventory(), good);
				((Chest) chests[0].getState()).getInventory().addItem(cost);
			}
			
			player.getInventory().addItem(good);
			player.updateInventory(); // they need to fix this.
			player.sendMessage(NaxColor.MSG + "You successfully bought " + NaxColor.WHITE + NaxUtil.materialName(good.getType()) + NaxColor.MSG + " from this shop.");
		}
		else
		{
			player.sendMessage(NaxColor.MSG + "Sorry, you don't have enough " + NaxColor.WHITE + NaxUtil.materialName(cost.getType()) + NaxColor.MSG + " to buy things from this shop.");
		}
	}
	
	private boolean isSuperShop()
	{
		return chests[0].getType() != Material.CHEST || chests[1].getType() != Material.CHEST;
	}
	
	private boolean haveEnoughGoods()
	{
		Chest chest = (Chest) chests[1].getState();
		
		int amountLeft = good.getAmount();
		boolean enoughMoney = false;
		
		for (int i = 0; i < chest.getInventory().getContents().length; i++)
		{
			ItemStack item = chest.getInventory().getContents()[i];
			
			if (item == null || item.getAmount() == 0 || item.getType() != good.getType() || item.getDurability() != good.getDurability()) continue;
			
			amountLeft -= item.getAmount();
			
			if (amountLeft <= 0)
			{
				enoughMoney = true;
				break;
			}
		}
		
		return enoughMoney;
		
	}
	
	private boolean haveRoomForMoney()
	{
		Chest chest = (Chest) chests[0].getState();
		
		HashMap<Integer, ItemStack> overflow = chest.getInventory().addItem(cost);
		
		chest.getInventory().removeItem(cost);
		
		if (overflow == null || overflow.isEmpty())
		{
			return true;
		}
		else
		{
			return false;
		}
		
	}
}
