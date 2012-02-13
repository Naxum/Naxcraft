package com.naxville.naxcraft.atms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.server.EntityPlayer;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.naxville.naxcraft.NaxColor;
import com.naxville.naxcraft.NaxFile;
import com.naxville.naxcraft.NaxUtil;
import com.naxville.naxcraft.Naxcraft;
import com.naxville.naxcraft.admin.SuperManager;
import com.naxville.naxcraft.admin.VirtualChest;

public class AtmManager
{
	public Naxcraft plugin;
	public List<Atm> atms = new ArrayList<Atm>();
	public List<String> ids = new ArrayList<String>();
	
	public Map<Player, VirtualChest> openAtms = new HashMap<Player, VirtualChest>();
	
	public NaxFile config;
	
	public static final int ATM_COST = 32;
	public static final Material ATM_MATERIAL_COST = Material.GOLD_INGOT;
	public static final String FILE_NAME = "atms";
	
	public AtmManager(Naxcraft plugin)
	{
		this.plugin = plugin;
	}
	
	public void handleBlockBreak(BlockBreakEvent event)
	{
		if (event.isCancelled()) return;
		
		if (event.getBlock().getType() != Material.CHEST && event.getBlock().getType() != Material.WALL_SIGN) return;
		
		Block block = event.getBlock();
		
		if (block.getType() == Material.WALL_SIGN)
		{
			Atm atm = getAtmFromChest(block.getRelative(BlockFace.DOWN));
			
			if (atm != null)
			{
				event.setCancelled(true);
				event.getPlayer().sendMessage(Naxcraft.MSG_COLOR + "You cannot remove this ATM sign unless the ATM is removed.");
			}
		}
		else
		{
			Atm atm = getAtmFromChest(block);
			
			if (atm != null)
			{
				removeAtm(atm);
				event.getPlayer().sendMessage(Naxcraft.MSG_COLOR + "You have successfully removed this ATM.");
			}
		}
	}
	
	public void handleBlockPlace(BlockPlaceEvent event)
	{
		if (event.isCancelled()) return;
		
		if (event.getBlock().getType() == Material.CHEST)
		{
			double x = event.getBlock().getLocation().getX();
			double z = event.getBlock().getLocation().getZ();
			
			for (Atm atm : atms)
			{
				double xa = atm.location.getX();
				double za = atm.location.getZ();
				
				if (x <= xa + 1 && x >= xa - 1)
				{
					if (z <= za + 1 && z >= za - 1)
					{
						event.getPlayer().sendMessage(NaxColor.MSG + "ATMs can only be a single chest.");
						event.setCancelled(true);
					}
				}
			}
		}
	}
	
	public void handlePlayerInteract(PlayerInteractEvent event)
	{
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.CHEST)
		{
			Block block = event.getClickedBlock();
			Atm atm = getAtmFromChest(block);
			
			if (atm == null)
			{
				if (block.getRelative(BlockFace.UP).getType() == Material.WALL_SIGN)
				{
					Sign sign = (Sign) block.getRelative(BlockFace.UP).getState();
					
					if (sign.getLine(1).equalsIgnoreCase("[ATM]"))
					{
						boolean singleChest = true;
						event.setCancelled(true);
						
						for (int x = -1; x < 2; x++)
						{
							for (int z = -1; z < 2; z++)
							{
								if (x == 0 && z == 0) continue;
								
								if (block.getRelative(x, 0, z).getType() == Material.CHEST)
								{
									singleChest = false;
								}
							}
						}
						
						if (singleChest)
						{
							if (NaxUtil.charge(event.getPlayer(), ATM_MATERIAL_COST, ATM_COST))
							{
								addAtm(event.getPlayer(), block);
								sign.setLine(1, ChatColor.DARK_PURPLE + "[ATM]");
								sign.update();
								
								Chest chest = (Chest) event.getClickedBlock().getState();
								
								for (ItemStack stack : chest.getInventory().getContents())
								{
									if (stack == null || stack.getAmount() == 0) continue;
									
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
				
				if (event.getPlayer().getGameMode() == GameMode.CREATIVE && !SuperManager.isSuper(event.getPlayer()))
				{
					event.getPlayer().sendMessage(NaxColor.MSG + "You cannot use ATMs in creative mode.");
				}
				else
				{
					accessAtm(event.getPlayer(), atm);
				}
			}
		}
	}
	
	private String getMaterialName(Material mat)
	{
		return mat.toString().toLowerCase().replace("_", " ");
	}
	
	public Atm getAtmFromChest(Block block)
	{
		if (block.getType() != Material.CHEST) return null;
		
		for (Atm atm : atms)
		{
			if (block.getLocation().equals(atm.location)) return atm;
		}
		
		return null;
	}
	
	public void addAtm(Player player, Block block)
	{
		Atm atm = new Atm(getNewID(player.getName()), player.getName(), block.getLocation());
		saveAtm(atm);
		
		atms.add(atm);
	}
	
	public void accessAtm(Player player, Atm atm)
	{
		VirtualChest chest = accessAccount(player);
		EntityPlayer eh = ((CraftPlayer) player).getHandle();
		
		openAtms.put(player, chest);
		plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new AtmChecker(player, plugin, this, atm.owner), 5);
		
		eh.a(chest);
	}
	
	public VirtualChest accessAccount(Player player)
	{
		Set<String> slots = config.getKeys("accounts." + player.getName() + "." + player.getWorld().getName());
		
		if (slots == null)
		{
			return createAccount(player);
		}
		else
		{
			return loadAccount(player, slots);
		}
	}
	
	public VirtualChest loadAccount(Player player, Set<String> slots)
	{
		VirtualChest chest = new VirtualChest();
		
		for (String slot : slots)
		{
			String prefix = "accounts." + player.getName() + "." + player.getWorld().getName() + "." + slot;
			
			ItemStack stack = config.getItemStack(prefix);
			
			chest.addItem(Integer.parseInt(slot), stack);
		}
		
		return chest;
	}
	
	public VirtualChest createAccount(Player player)
	{
		VirtualChest chest = new VirtualChest();
		chest.addItem(0, new ItemStack(Material.COOKIE.getId(), 8));
		
		saveAccount(player, chest);
		
		return chest;
	}
	
	public String getNewID(String owner)
	{
		for (int i = 0; i < 1000; i++)
		{
			if (!ids.contains(owner + "_" + i))
			{
				ids.add(owner + "_" + i);
				return owner + "_" + i;
			}
		}
		
		return "IMPOSSIBLE_ID";
	}
	
	public void loadFile()
	{
		config = new NaxFile(plugin, "atms");
		
		config.addDefault("acounts", "");
	}
	
	public void loadAtms()
	{
		loadFile();
		
		try
		{
			Set<String> keys = config.getKeys("atms");
			
			for (String key : keys)
			{
				String prefix = "atms." + key;
				
				if (!config.contains(prefix + ".world")) continue;
				
				World world = plugin.getServer().getWorld(config.getString(prefix + ".world"));
				
				if (world == null) continue;
				
				Location loc = config.getLocation(prefix);
				
				String owner = config.getString(prefix + ".owner");
				
				ids.add(key);
				Atm atm = new Atm(key, owner, loc);
				atms.add(atm);
			}
			
			// /updateAccounts();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void saveAccount(Player player)
	{
		saveAccount(player, openAtms.get(player));
		openAtms.remove(player);
	}
	
	public void saveAccount(Player player, VirtualChest chest)
	{
		config.set("accounts." + player.getName() + "." + player.getWorld().getName(), "");
		
		if (chest != null)
		{
			for (int i = 0; i < chest.getContents().length; i++)
			{
				net.minecraft.server.ItemStack mcItem = chest.getContents()[i];
				
				if (mcItem == null) continue;
				
				ItemStack item = new ItemStack(mcItem.id, mcItem.count, (short) mcItem.getData());
				
				String prefix = "accounts." + player.getName() + "." + player.getWorld().getName() + "." + i;
				config.setItemStack(prefix, item);
			}
		}
		
		config.save();
	}
	
	public void saveAtm(Atm atm)
	{
		String prefix = "atms." + atm.id;
		config.setLocation(prefix, atm.location);
		config.set(prefix + ".owner", atm.owner);
		
		config.save();
	}
	
	public void removeAtm(Atm atm)
	{
		atms.remove(atm);
		
		Sign sign = (Sign) atm.world.getBlockAt(atm.location.getBlockX(), atm.location.getBlockY() + 1, atm.location.getBlockZ()).getState();
		sign.setLine(1, "[Deleted ATM]");
		sign.update();
		
		config.removeProperty("atms." + atm.id);
		config.save();
	}
}
