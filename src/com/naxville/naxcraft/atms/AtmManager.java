package com.naxville.naxcraft.atms;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.server.EntityPlayer;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.block.CraftChest;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.naxville.naxcraft.Naxcraft;
import com.naxville.naxcraft.admin.NaxcraftConfiguration;
import com.naxville.naxcraft.admin.TileEntityVirtualChest;

public class AtmManager 
{
	public Naxcraft plugin;
	public List<Atm> atms = new ArrayList<Atm>();
	public List<String> ids = new ArrayList<String>();
	
	public Map<Player, TileEntityVirtualChest> openAtms = new HashMap<Player, TileEntityVirtualChest>();
	
	public NaxcraftConfiguration config;
	
	public static final int ATM_COST = 64;
	public static final Material ATM_MATERIAL_COST = Material.GOLD_INGOT;
	public static final String FILE_NAME = "ATM_Data";
	
	public AtmManager(Naxcraft plugin)
	{
		this.plugin = plugin;
	}
	
	public void handleBlockBreak(BlockBreakEvent event)
	{
		if(event.isCancelled()) return;
		
		if(event.getBlock().getType() != Material.CHEST && event.getBlock().getType() != Material.WALL_SIGN)
			return;
		
		Block block = event.getBlock();
		
		if(block.getType() == Material.WALL_SIGN)
		{
			Atm atm = getAtmFromChest(block.getRelative(BlockFace.DOWN));
			
			if(atm != null)
			{
				event.setCancelled(true);
				event.getPlayer().sendMessage(Naxcraft.MSG_COLOR + "You cannot remove this ATM sign unless the ATM is removed.");
			}
		}
		else
		{
			Atm atm = getAtmFromChest(block);
			
			if(atm != null)
			{
				removeAtm(atm);
				event.getPlayer().sendMessage(Naxcraft.MSG_COLOR + "You have successfully removed this ATM.");
			}
		}
	}
	
	public void handleBlockPlace(BlockPlaceEvent event)
	{
		if(event.isCancelled()) return;
		
		if(event.getBlock().getType() == Material.CHEST)
		{
			double x = event.getBlockAgainst().getLocation().getX();
			double z = event.getBlockAgainst().getLocation().getZ();
			
			for(Atm atm : atms)
			{
				double xa = atm.location.getX();
				double za = atm.location.getZ();
				
				if(x <= xa + 1 && x >= xa - 1)
				{
					if(z <= za + 1 && z >= za - 1)
					{
						event.setCancelled(true);
					}
				}
			}
		}
	}
	
	public void handlePlayerInteract(PlayerInteractEvent event)
	{
		if(event.isCancelled()) return;
		
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK)
		{
			if(event.getClickedBlock() != null)
			{
				Block block = event.getClickedBlock();
				
				if(block.getType() == Material.CHEST)
				{
					Atm atm = getAtmFromChest(block);
					
					if(atm == null)
					{
						if(block.getRelative(BlockFace.UP).getType() == Material.WALL_SIGN)
						{
							Sign sign = (Sign) block.getRelative(BlockFace.UP).getState();
							
							if(sign.getLine(1).equalsIgnoreCase("[ATM]"))
							{
								boolean singleChest = true;
								event.setCancelled(true);
								
								for(int x = -1; x < 2; x++)
								{
									for(int z = -1; z < 2; z++)
									{
										if(x == 0 && z == 0) continue;
										
										if(block.getRelative(x, 0, z).getType() == Material.CHEST)
										{
											singleChest = false;
										}
									}
								}
								
								if(singleChest)
								{									
									if(charge(event.getPlayer(), ATM_MATERIAL_COST, ATM_COST))
									{
										addAtm(event.getPlayer(), block);
										sign.setLine(1, ChatColor.DARK_PURPLE + "[ATM]");
										sign.update();
										
										CraftChest chest = (CraftChest)block.getState();
										
										for(ItemStack stack : chest.getInventory().getContents())
										{
											chest.getWorld().dropItemNaturally(event.getPlayer().getLocation(), stack);
										}
										
										event.getPlayer().sendMessage(Naxcraft.SUCCESS_COLOR + "You successfully activated this ATM for " + Naxcraft.DEFAULT_COLOR + ATM_COST + " " + getMaterialName(ATM_MATERIAL_COST) + "s" + Naxcraft.SUCCESS_COLOR + ".");
									}
									else
									{
										event.getPlayer().sendMessage(Naxcraft.MSG_COLOR + "You can activate this ATM for " + Naxcraft.DEFAULT_COLOR + ATM_COST + " " + getMaterialName(ATM_MATERIAL_COST) + "s" + Naxcraft.MSG_COLOR + ".");
									}
								}
								else
								{
									event.getPlayer().sendMessage(Naxcraft.MSG_COLOR + "You cannot have a large chest for an ATM.");
								}
							}
						}
					}
					else
					{
						event.setCancelled(true);
						accessAtm(event.getPlayer(), atm);
					}
				}
			}
		}
	}
	
	public String getMaterialName(Material mat)
	{
		return mat.toString().toLowerCase().replace("_", " ");
	}
	
	public Atm getAtmFromChest(Block block)
	{
		if(block.getType() != Material.CHEST)
			return null;
		
		for(Atm atm : atms)
		{
			if(block.getLocation().getWorld().getName() == atm.location.getWorld().getName() &&
					block.getLocation().getX() == atm.location.getX() &&
					block.getLocation().getY() == atm.location.getY() &&
					block.getLocation().getZ() == atm.location.getZ())
			{
				return atm;
			}
		}
		
		return null;
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
	
	public void addAtm(Player player, Block block)
	{
		Atm atm = new Atm(getNewID(player.getName()), player.getName(), block.getLocation());
		saveAtm(atm);
		
		atms.add(atm);
	}
	
	public void accessAtm(Player player, Atm atm)
	{
		TileEntityVirtualChest chest = accessAccount(player);
		EntityPlayer eh = ((CraftPlayer)player).getHandle();
		
		openAtms.put(player, chest);
		plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new AtmChecker(player, plugin, this, atm.owner), 5);
		
		eh.a(chest);
	}
	
	public TileEntityVirtualChest accessAccount(Player player)
	{
		List<String> slots = config.getKeys("accounts." + player.getName());
		
		if(slots == null || slots.isEmpty())
		{
			return createAccount(player);
		}
		else
		{
			return loadAccount(player, slots);
		}
	}
	
	public TileEntityVirtualChest loadAccount(Player player, List<String> slots)
	{
		TileEntityVirtualChest chest = new TileEntityVirtualChest();
		
		for(String slot : slots)
		{
			String prefix = "accounts." + player.getName() + "." + slot;
			
			if(config.getString(prefix + ".type").equals("0"))
			{
				continue;
			}
			
			ItemStack stack = new ItemStack
			(
				Integer.parseInt(config.getString(prefix + ".type")),
				Integer.parseInt(config.getString(prefix + ".amount")),
				Short.parseShort(config.getString(prefix + ".damage"))
			);
			
			chest.addItem(Integer.parseInt(slot), stack);
		}
		
		return chest;
	}
	
	public TileEntityVirtualChest createAccount(Player player)
	{			
		TileEntityVirtualChest chest = new TileEntityVirtualChest();
		chest.addItem(0, new ItemStack(Material.COOKIE.getId(), 8));
		
		saveAccount(player, chest);
		
		return chest;
	}
	
	public String getNewID(String owner)
	{
		for(int i = 0; i < 1000; i++)
		{
			if(!ids.contains(owner + "_" + i))
			{
				ids.add(owner + "_" + i);
				return owner + "_" + i;
			}
		}
		
		return "IMPOSSIBLE_ID";
	}
	
	public void loadFile()
	{
		File file = new File(plugin.filePath + FILE_NAME + ".yml");
		try
		{
			if(!file.exists())
			{
				file.createNewFile();
				initializeFile(file);
			}
		}
		catch(IOException e)
		{
			System.out.println("Error creating new " + FILE_NAME + " file.");
			e.printStackTrace();
		}
		
		config = new NaxcraftConfiguration(file);
		config.load();
	}
	
	public void initializeFile(File file)
	{
		try
		{
			BufferedWriter output = new BufferedWriter(new FileWriter(file));
			output.write("atms: {}\n");
			output.write("accounts: {}");
			output.close();
		}
		catch(IOException e)
		{
			System.out.println("Error initializing " + FILE_NAME + ".");
			e.printStackTrace();
		}
	}
	
	public void loadAtms()
	{
		loadFile();
		
		try
		{
			List<String> keys = config.getKeys("atms");
			
			for(String key : keys)
			{
				String prefix = "atms." + key;
				Location loc = new Location
				(
					plugin.getServer().getWorld(config.getString(prefix + ".world")),
					Double.parseDouble(config.getString(prefix + ".x")),
					Double.parseDouble(config.getString(prefix + ".y")),
					Double.parseDouble(config.getString(prefix + ".z"))
				);
				
				String owner = config.getString(prefix + ".owner");
				
				ids.add(key);
				Atm atm = new Atm(key, owner, loc);
				atms.add(atm);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void saveAccount(Player player)
	{
		saveAccount(player, openAtms.get(player));
		openAtms.remove(player);
	}
	
	public void saveAccount(Player player, TileEntityVirtualChest chest)
	{
		int i = 0;
		
		for(net.minecraft.server.ItemStack item : chest.getContents())
		{			
			String prefix = "accounts." + player.getName() + "." + i;
			
			if(item == null)
			{
				config.setProperty(prefix + ".type", 0);
				config.setProperty(prefix + ".amount", 0);
				config.setProperty(prefix + ".damage", 0);
			}
			else
			{
				config.setProperty(prefix + ".type", item.id);
				config.setProperty(prefix + ".amount", item.count);
				config.setProperty(prefix + ".damage", item.damage);
			}
			
			i++;
		}
		
		config.save();
	}
	
	public void saveAtm(Atm atm)
	{		
		String prefix = "atms." + atm.id;
		
		config.setProperty(prefix + ".world", atm.world.getName());
		config.setProperty(prefix + ".x", atm.location.getX());
		config.setProperty(prefix + ".y", atm.location.getY());
		config.setProperty(prefix + ".z", atm.location.getZ());
		config.setProperty(prefix + ".owner", atm.owner);
		
		config.save();
	}
	
	public void removeAtm(Atm atm)
	{
		atms.remove(atm);
		
		Sign sign = (Sign)atm.world.getBlockAt(atm.location.getBlockX(), atm.location.getBlockY() + 1, atm.location.getBlockZ());
		sign.setLine(1, "[Deleted ATM]");
		sign.update();
		
		config.removeProperty("atms." + atm.id);
		config.save();
	}
}
