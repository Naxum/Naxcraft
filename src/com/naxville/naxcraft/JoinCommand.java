package com.naxville.naxcraft;

import java.util.ArrayList;
import java.util.HashMap;
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
		if(plugin.playerManager.getPlayer(player).rank == PlayerRank.getRank(0))
		{
			if(event.getItem().getItemStack().getType() == Material.IRON_INGOT && hasEnoughIron(event.getPlayer(), 16-1))
			{
				player.sendMessage(NaxColor.LOCAL + "You have enough iron to become a member!"); 
				player.sendMessage(NaxColor.MSG + "Use /join to pay 16 iron and become a member.");
			}
		}
	}
	
	private boolean hasEnoughIron(Player player, int i)
	{
		int amountLeft = i;
		
		HashMap<Integer, ? extends ItemStack> stacks = player.getInventory().all(Material.IRON_INGOT);
		
		for(ItemStack stack : stacks.values())
		{
			amountLeft -= stack.getAmount();
			
			if(amountLeft <= 0)
			{
				return true;
			}
		}
		
		return false;
	}

	public void runJoinCommand(Player player)
	{
		if(plugin.playerManager.getPlayer(player).rank == PlayerRank.getRank(0))
		{
			if(charge(player, Material.IRON_INGOT, 16))
			{
				NaxPlayer p = plugin.playerManager.getPlayer(player);
				p.rank = PlayerRank.MEMBER;
				plugin.playerManager.savePlayer(p);
				plugin.announcer.announce(plugin.getNickName(player.getName()) + Naxcraft.SUCCESS_COLOR + " is now a member!");
				player.sendMessage(Naxcraft.MSG_COLOR + "Check out the " + NaxColor.WHITE + "/color" + NaxColor.MSG + " command to change your name color.");
				player.sendMessage(Naxcraft.MSG_COLOR + "You've unlocked some " + NaxColor.WHITE + "/flags" + NaxColor.MSG + " for your bases!");
			}
			else
			{
				player.sendMessage(NaxColor.MSG + "It costs 16 iron ingots to become a member.");
			}
		}
		else
		{
			player.sendMessage(Naxcraft.MSG_COLOR + "You are already a member.");
		}
	}
	
	public void runPromoteCommand(Player player)
	{
		if(plugin.playerManager.getPlayer(player).rank == PlayerRank.MEMBER)
		{
			if(charge(player, Material.GOLD_INGOT, 32))
			{
				NaxPlayer p = plugin.playerManager.getPlayer(player);
				p.rank = PlayerRank.VETERAN;
				plugin.playerManager.savePlayer(p);
				plugin.announcer.announce(plugin.getNickName(player.getName()) + Naxcraft.SUCCESS_COLOR + " is now a veteran!");
				player.sendMessage(Naxcraft.MSG_COLOR + "You have unlocked more " + NaxColor.WHITE + "/flags" + NaxColor.MSG + "!");
			}
			else
			{
				player.sendMessage(NaxColor.MSG + "It cost 32 gold ingots to become a veteran.");
			}
		}
		else
		{
			player.sendMessage(Naxcraft.MSG_COLOR + "You must be a member to try and get promoted!");
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
