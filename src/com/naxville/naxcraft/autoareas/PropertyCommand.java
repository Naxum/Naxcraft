package com.naxville.naxcraft.autoareas;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.naxville.naxcraft.Naxcraft;

public class PropertyCommand
{
	public Naxcraft plugin;
	public AutoAreaManager autoAreaManager;
	
	public static final Material CHUNK_MATERIAL = Material.IRON_INGOT;
	public static final int CHUNK_COST = 32;
	
	public PropertyCommand(Naxcraft plugin)
	{
		this.plugin = plugin;
		this.autoAreaManager = plugin.autoAreaManager;
	}
	
	public boolean runCommand(Player player, String args[])
	{
		player.sendMessage(Naxcraft.MSG_COLOR + "----");
		
		if(args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("help")))
		{
			printHelp(player);
		}
		else if (args.length == 1)
		{
			if(args[0].equalsIgnoreCase("shutup"))
			{
				autoAreaManager.shutup(player);
				player.sendMessage(Naxcraft.MSG_COLOR + "You won't get any more unowned property messages until you relog.");
			}
			else if (args[0].equalsIgnoreCase("info"))
			{
				printInformation(player);
			}
			else if (args[0].equalsIgnoreCase("borders"))
			{
				autoAreaManager.showBorders(player);
				showChunkBorders(player);
			}
			else if (args[0].equalsIgnoreCase("noborders"))
			{
				autoAreaManager.hideBorders(player);
				hideChunkBorders(player);
				
				player.sendMessage(Naxcraft.MSG_COLOR + "Your borders should now be gone, if not, try this command again or relog.");
			}
			else
			{
				printHelp(player);
			}
		}
		else if (args.length == 2)
		{			
			//buying chunk
			AutoBase here = autoAreaManager.getBase(player);
			if(here != null)
			{
				player.sendMessage(Naxcraft.MSG_COLOR + "You already own this chunk!");
				return true;
			}
			
			AutoBase buffer = autoAreaManager.getNearestBaseWithin(player, AutoAreaManager.BASE_BUFFER);
			
			if(buffer != null && !autoAreaManager.isOwner(player, buffer))
			{
				player.sendMessage(Naxcraft.MSG_COLOR + "You are " + Naxcraft.ERROR_COLOR + "too close" + Naxcraft.MSG_COLOR + " to " + Naxcraft.DEFAULT_COLOR + buffer.getFounderName() + Naxcraft.MSG_COLOR + "'s area to buy this chunk.");
			}
			else if (!checkSurroundingChunks(player))
			{
				if(newBase(player))
				{
					purchaseChunk(player, createNewBase(player));
				}
				else
				{
					player.sendMessage(Naxcraft.MSG_COLOR + "You must buy chunks that are touching your base. If you want to make a new base, you must move " + Naxcraft.DEFAULT_COLOR + AutoAreaManager.SAME_OWNER_BUFFER + " chunks" + Naxcraft.MSG_COLOR + " away from your old base.");
				}
			}
			else
			{
				purchaseChunk(player, getTouchingBase(player));
			}
			
		}
		else if (args.length == 3)
		{
			//buying a flag
			if(args[0].equalsIgnoreCase("buy") && args[1].equalsIgnoreCase("flag"))
			{
				autoAreaManager.buyFlag(player, args);
			}
			else if ((args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove")) && args[1].equalsIgnoreCase("builder"))
			{
				autoAreaManager.addRemoveBuilder(player, args);
			}
		}
		else
		{
			printHelp(player);
		}
		
		return true;
	}
	
	private void showChunkBorders(Player player) 
	{
		Chunk c = player.getLocation().getBlock().getChunk();
		
		for(int x = 0; x < 2; x++)
		{
			for(int y = 0; y < 128; y += 3)
			{
				for(int z = 0; z < 2; z++)
				{
					Block b = c.getBlock(x * 15, y, z * 15);
					
					if(b.getType() == Material.AIR)
					{
						player.sendBlockChange(b.getLocation(), Material.WOOL, (byte)5);
					}
				}
			}
		}	
	}
	
	private void hideChunkBorders(Player player) 
	{
		for(int cx = -3; cx < 4; cx++)
		{
			for(int cz = -3; cz < 4; cz++)
			{
				Chunk c = player.getWorld().getChunkAt(player.getLocation().getBlock().getChunk().getX() + cx, player.getLocation().getBlock().getChunk().getZ() + cz);
				
				for(int x = 0; x < 2; x++)
				{
					for(int y = 0; y < 128; y += 3)
					{
						for(int z = 0; z < 2; z++)
						{
							Block b = c.getBlock(x * 15, y, z * 15);
							
							if(b.getType() == Material.AIR)
							{
								player.sendBlockChange(b.getLocation(), b.getType(), b.getData());
							}
						}
					}
				}	
			}
		}
	}

	public boolean newBase(Player player)
	{
		return autoAreaManager.newBase(player);
	}
	
	public boolean checkSurroundingChunks(Player player)
	{
		return autoAreaManager.checkSurroundingChunks(player);
	}
	
	public AutoBase createNewBase(Player player)
	{
		return autoAreaManager.createNewBase(player);
	}
	
	public AutoBase getTouchingBase(Player player)
	{
		return autoAreaManager.getTouchingBase(player);
	}
	
	public void purchaseChunk(Player player, AutoBase base)
	{		
		if(plugin.superCommand.isSuper(player.getName()) || charge(player, CHUNK_MATERIAL, CHUNK_COST * base.getChunkMultiplier()))
		{
			int x = player.getLocation().getBlock().getChunk().getX();
			int z = player.getLocation().getBlock().getChunk().getZ();
			
			autoAreaManager.updateCaches(new Point(x, z));
			
			base.chunks.add(new Point(x, z));
			base.showBorders(player);
			base.save();
			
			player.sendMessage(Naxcraft.MSG_COLOR + "You have " + Naxcraft.SUCCESS_COLOR + "successfully" + Naxcraft.MSG_COLOR + " bought this chunk!");
		}
		else
		{
			player.sendMessage(Naxcraft.MSG_COLOR + "It costs " + Naxcraft.DEFAULT_COLOR + (CHUNK_COST * base.getChunkMultiplier()) + " " + CHUNK_MATERIAL.toString().toLowerCase().replace("_", " ") + "s" + Naxcraft.MSG_COLOR + " to buy this chunk. You don't have enough.");
		}
	}
	
	public void printInformation(Player player)
	{
		AutoBase base = autoAreaManager.getBase(player);
				
		if(base == null)
		{
			player.sendMessage(Naxcraft.MSG_COLOR + "You are not in an owned area.");
			AutoBase buffer = autoAreaManager.getNearestBaseWithinBuffer(player);
			
			if(buffer != null && !autoAreaManager.isOwner(player, buffer))
			{
				player.sendMessage(Naxcraft.MSG_COLOR + "You are " + Naxcraft.ERROR_COLOR + "too close" + Naxcraft.MSG_COLOR + " to " + buffer.getFounderName() + Naxcraft.MSG_COLOR + "'s area to buy this chunk.");
			}
			else
			{
				player.sendMessage(Naxcraft.MSG_COLOR + "You are " + Naxcraft.SUCCESS_COLOR + "far enough" + Naxcraft.MSG_COLOR + " from any areas to buy this chunk.");
			}
		}
		else
		{
			String info = "You are in an area founded by " + base.getFounderName() + Naxcraft.MSG_COLOR + ". Its builders include " + base.getBuilderNames() + Naxcraft.MSG_COLOR + ". This area encompasses " + Naxcraft.DEFAULT_COLOR +
			base.chunks.size() +  " chunks" + Naxcraft.MSG_COLOR + " and has the flags ";
			
			int i = 0;
			for(String flag : base.flags.keySet())
			{
				if(base.flags.get(flag))
				{
					if(i > 0) info += Naxcraft.MSG_COLOR + ", ";
					info += Naxcraft.SUCCESS_COLOR + flag;
					i++;
				}
			}
			
			if(i > 0)
			{
				info += Naxcraft.MSG_COLOR + ".";	
			}
			else
			{
				info += Naxcraft.DEFAULT_COLOR + "no flags" + Naxcraft.MSG_COLOR + ".";
			}
			
			player.sendMessage(Naxcraft.MSG_COLOR + info);
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
	
	public void printHelp(Player player)
	{
		player.sendMessage(Naxcraft.COMMAND_COLOR + "Property Command Usage:");
		player.sendMessage(Naxcraft.MSG_COLOR + "/property " + Naxcraft.DEFAULT_COLOR + "shutup" + Naxcraft.MSG_COLOR + " - Stops property msgs in unowned areas.");
		player.sendMessage(Naxcraft.MSG_COLOR + "/property " + Naxcraft.DEFAULT_COLOR + "borders" + Naxcraft.MSG_COLOR + " - Shows your owned area borders.");
		player.sendMessage(Naxcraft.MSG_COLOR + "/property " + Naxcraft.DEFAULT_COLOR + "info" + Naxcraft.MSG_COLOR + " - Lists current area's information.");
		player.sendMessage(Naxcraft.MSG_COLOR + "/property " + Naxcraft.DEFAULT_COLOR + "buy chunk" + Naxcraft.MSG_COLOR + " - Buys the current chunk.");
		player.sendMessage(Naxcraft.MSG_COLOR + "/property " + Naxcraft.DEFAULT_COLOR + "add builder <name>" + Naxcraft.MSG_COLOR + " - Adds an owner.");
		player.sendMessage(Naxcraft.MSG_COLOR + "/property " + Naxcraft.DEFAULT_COLOR + "remove builder <name>" + Naxcraft.MSG_COLOR + " - removes an owner.");
		player.sendMessage(Naxcraft.MSG_COLOR + "/property " + Naxcraft.DEFAULT_COLOR + "buy flag <name>" + Naxcraft.MSG_COLOR + " - Buys an area flag. (/flags)");
		player.sendMessage(Naxcraft.MSG_COLOR + "Also: " + Naxcraft.DEFAULT_COLOR + "/bc" + Naxcraft.MSG_COLOR + " works for buying chunks, and " + Naxcraft.DEFAULT_COLOR + "/bf" + Naxcraft.MSG_COLOR  + " for buying flags.");
	}
}
