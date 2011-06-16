package com.naxville.naxcraft;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import com.naxville.naxcraft.player.NaxPlayer;
import com.naxville.naxcraft.player.PlayerManager.PlayerRank;

public class JoinCommand
{
	private Naxcraft plugin;
	
	public JoinCommand(Naxcraft plugin)
	{
		this.plugin = plugin;
	}
	
	public void handleItemPickup(PlayerPickupItemEvent event)
	{
		Player player = event.getPlayer();
		if(plugin.playerManager.getPlayer(player).rank == PlayerRank.NOOB)
		{
			if(event.getItem().getItemStack().getType() == Material.IRON_INGOT && event.getItem().getItemStack().getAmount() > 16)
			{
				player.sendMessage(Naxcraft.MSG_COLOR + "You have enough iron to become a member! Use /join to pay 16 iron and become a member.");
			}
		}
	}
	
	public void runJoinCommand(Player player)
	{
		if(plugin.playerManager.getPlayer(player).rank == PlayerRank.NOOB)
		{
			if(charge(player, Material.IRON_INGOT, 16))
			{
				NaxPlayer p = plugin.playerManager.getPlayer(player);
				p.rank = PlayerRank.MEMBER;
				plugin.playerManager.savePlayer(p);
				player.sendMessage(Naxcraft.MSG_COLOR + "Congratulations you are now a member!");
				plugin.announcer.announce(plugin.getNickName(player.getName()) + " is now a member!");
			}
		}
		else
		{
			player.sendMessage(Naxcraft.MSG_COLOR + "You are already a member.");
		}
	}
	
	public boolean charge(Player player, Material material, int cost)
	{		
		int priceleft = cost;
		List<Integer> slotsToDestroy = new ArrayList<Integer>();
		
		boolean enoughMoney = false;
		
		for(int slot : player.getInventory().all(material).keySet())
		{
			priceleft -= player.getInventory().getItem(slot).getAmount();
			if(priceleft > 0) 
			{
				slotsToDestroy.add(slot);
			} 
			else if(priceleft == 0) 
			{
				slotsToDestroy.add(slot);
				enoughMoney = true;
				break;
			} 
			else if(priceleft < 0) 
			{
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
				player.getInventory().addItem(new ItemStack(material, priceleft*-1));
			}		
		}
		
		return enoughMoney;
	}
}
