package com.naxville.naxcraft.listeners;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.PoweredMinecart;
import org.bukkit.entity.StorageMinecart;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;

import com.naxville.naxcraft.NaxColor;
import com.naxville.naxcraft.Naxcraft;
import com.naxville.naxcraft.autoareas.AutoAreaManager.Flag;
import com.naxville.naxcraft.autoareas.AutoBase;
import com.naxville.naxcraft.autoareas.WorldBoundary;
import com.naxville.naxcraft.player.PlayerManager.PlayerRank;

public class NaxcraftBlockListener extends BlockListener
{
	public static Naxcraft plugin;
	
	public NaxcraftBlockListener(Naxcraft instance)
	{
		plugin = instance;
	}
	
	public void onBlockPhysics(BlockPhysicsEvent event)
	{
		if (event.isCancelled()) return;
		
		plugin.warpgateCommand.handlePhysics(event);
		
		if (event.getBlock().getType() == Material.TNT)
		{
			event.setCancelled(true);
		}
	}
	
	public void onBlockDispense(BlockDispenseEvent event)
	{
		AutoBase base = plugin.autoAreaManager.getBase(event.getBlock());
		
		if (base != null && base.hasFlag(Flag.RAILWAY))
		{
			if (event.getItem().getType() == Material.MINECART || event.getItem().getType() == Material.POWERED_MINECART || event.getItem().getType() == Material.STORAGE_MINECART)
			{
				event.setCancelled(true);
				
				int data = event.getBlock().getData();
				
				Material m = event.getItem().getType();
				
				BlockFace face = null;
				
				switch (data)
				{
					case 2:
						face = BlockFace.EAST;
						break;
					case 3:
						face = BlockFace.WEST;
						break;
					case 4:
						face = BlockFace.NORTH;
						break;
					case 5:
					default:
						face = BlockFace.SOUTH;
				}
				
				Block target = event.getBlock().getRelative(face);
				
				if (target.getType() == Material.RAILS || target.getType() == Material.POWERED_RAIL)
				{
					if (m == Material.MINECART)
					{
						target.getWorld().spawn(target.getLocation(), Minecart.class);
					}
					else if (m == Material.POWERED_MINECART)
					{
						target.getWorld().spawn(target.getLocation(), PoweredMinecart.class);
					}
					else if (m == Material.STORAGE_MINECART)
					{
						target.getWorld().spawn(target.getLocation(), StorageMinecart.class);
					}
				}
			}
		}
	}
	
	public void onBlockForm(BlockFormEvent event)
	{
		AutoBase base = plugin.autoAreaManager.getBase(event.getBlock());
		
		if (base != null && base.hasFlag(Flag.WEATHER))
		{
			event.setCancelled(true);
		}
	}
	
	public void onBlockFromTo(BlockFromToEvent event)
	{
		AutoBase from = plugin.autoAreaManager.getBase(event.getBlock());
		AutoBase to = plugin.autoAreaManager.getBase(event.getToBlock());
		
		if (from != to)
		{
			event.setCancelled(true);
		}
	}
	
	public void onBlockIgnite(BlockIgniteEvent event)
	{
		if (event.getCause() != IgniteCause.FLINT_AND_STEEL)
		{
			event.setCancelled(true);
			return;
		}
		event.setCancelled(!plugin.autoAreaManager.canInteract(event.getPlayer(), event.getBlock()));
	}
	
	public void onSignChange(SignChangeEvent event)
	{
		plugin.shopManager.handleSign(event);
		plugin.displayShopManager.handleSignChange(event);
		
		if (event.isCancelled()) return;
		
		if (plugin.playerManager.getPlayer(event.getPlayer()).rank.getId() < PlayerRank.VETERAN.getId()) { return; }
		
		int index = 0;
		for (String line : event.getLines())
		{
			
			Pattern p = Pattern.compile("^.*\\^.*$");
			Matcher m = p.matcher(line);
			
			if (m.matches())
			{
				String newLine = "";
				
				for (String s : line.split("\\^"))
				{
					// System.out.println("' " + s + " '");
					
					if (s == null || s.isEmpty() || s == "") continue;
					
					char c = s.charAt(0);
					
					int i = -1;
					
					try
					{
						i = Integer.parseInt(c + "");
					}
					catch (Exception e)
					{
					}
					
					if (i == -1)
					{
						newLine += "^" + s;
						continue;
					}
					else if (i == 1)
					{
						int i2 = 0;
						
						try
						{
							i2 = Integer.parseInt(s.charAt(1) + "");
						}
						catch (Exception e)
						{
							i2 = 0;
						}
						
						if (i2 > 0)
						{
							i = 10;
							i += i2;
						}
					}
					
					if (i == 0 || i == Naxcraft.COLORS.length - 1)
					{
						if (!plugin.playerManager.getPlayer(event.getPlayer()).rank.isAdmin())
						{
							newLine += "^" + s;
						}
					}
					else if (i >= Naxcraft.COLORS.length)
					{
						newLine += "^" + s;
					}
					else
					{
						int start = i >= 10 ? 2 : 1;
						s = s.substring(start);
						
						newLine += Naxcraft.COLORS[i] + s;
					}
				}
				
				event.setLine(index, newLine);
			}
			
			index++;
		}
	}
	
	public void onBlockDamage(BlockDamageEvent event)
	{
		if (event.isCancelled()) return;
		
		plugin.autoAreaManager.handleBlockDamage(event);
		
		if (event.isCancelled()) return;
		
		plugin.playerManager.handleBlockDamage(event);
	}
	
	public void onBlockBreak(BlockBreakEvent event)
	{
		if (event.isCancelled()) return;
		
		plugin.autoAreaManager.handleBlockBreak(event);
		
		if (event.isCancelled()) return;
		plugin.atmManager.handleBlockBreak(event);
		plugin.warpgateCommand.handleBreak(event);
		plugin.shopManager.handleBlockBreak(event);
		plugin.playerManager.handleBlockBreak(event);
		plugin.displayShopManager.handleBlockBreak(event);
		
		if (event.isCancelled()) return;
		
		if (event.getBlock().getType() == Material.GRAVEL)
		{
			event.setCancelled(true);
			
			event.getBlock().setType(Material.AIR);
			
			Random r = new Random();
			
			if (r.nextInt(5) == 0)
			{
				event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.FLINT, 1));
			}
			else
			{
				event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.GRAVEL, 1));
			}
		}
	}
	
	public void onBlockPlace(BlockPlaceEvent event)
	{
		if (event.isCancelled()) return;
		
		plugin.autoAreaManager.handleBlockPlace(event);
		plugin.playerManager.cheatingManager.handleBlockPlace(event);
		plugin.mobCommand.handleBlockPlace(event);
		plugin.atmManager.handleBlockPlace(event);
		
		WorldBoundary.handleBlockPlace(event);
		
		if (event.isCancelled()) return;
		
		if (event.getBlock().getWorld().getName().startsWith("old_naxville"))
		{
			event.getPlayer().sendMessage(Naxcraft.MSG_COLOR + "Editing and deleting is not allowed in Old Naxville.");
			event.setCancelled(true);
		}
		
		if (event.getBlock().getType() == Material.PUMPKIN && event.getBlock().getRelative(BlockFace.DOWN).getType() == Material.SNOW_BLOCK)
		{
			AutoBase base = plugin.autoAreaManager.getBase(event.getBlock());
			if (base == null || !plugin.autoAreaManager.isOwner(event.getPlayer(), base))
			{
				event.setCancelled(true);
				event.getPlayer().sendMessage(NaxColor.MSG + "You can't try to make snow golems outside areas you own.");
			}
		}
	}
}
