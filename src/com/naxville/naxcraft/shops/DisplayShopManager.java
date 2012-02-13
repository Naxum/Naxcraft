package com.naxville.naxcraft.shops;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import com.naxville.naxcraft.NaxColor;
import com.naxville.naxcraft.NaxFile;
import com.naxville.naxcraft.NaxUtil;
import com.naxville.naxcraft.Naxcraft;
import com.naxville.naxcraft.admin.SuperManager;

public class DisplayShopManager
{
	public Naxcraft plugin;
	
	private List<DisplayShop> shops = new ArrayList<DisplayShop>();
	private NaxFile config;
	private int idCount;
	
	public DisplayShopManager(Naxcraft plugin)
	{
		this.plugin = plugin;
		
	}
	
	public void handleSignChange(SignChangeEvent event)
	{
		Material mat = event.getBlock().getType();
		
		if (event.isCancelled() || (mat != Material.WALL_SIGN && mat != Material.CHEST)) return;
		
		Player player = event.getPlayer();
		
		if (mat == Material.WALL_SIGN)
		{
			Block b = event.getBlock();
			BlockFace direction = null;
			
			for (BlockFace f : NaxUtil.cardinals())
			{
				if (b.getRelative(f).getType() == Material.STEP)
				{
					b = b.getRelative(f);
					direction = f;
					break;
				}
			}
			
			if (direction == null) return;
			
			if (b.getType() != Material.STEP) return;
			
			if (b.getRelative(BlockFace.UP).getType() != Material.STEP) return;
			
			Block chestBlock = b.getRelative(direction).getRelative(direction);
			Block chestBlock2 = chestBlock.getRelative(BlockFace.UP);
			
			if (chestBlock.getType() != Material.CHEST && chestBlock2.getType() != Material.CHEST) return;
			
			// ItemStack test = new ItemStack(Material.APPLE, 1);
			
			Chest chest = (Chest) chestBlock.getState();
			Chest chest2 = (Chest) chestBlock2.getState();
			
			if (chest.getInventory() == null || chest2.getInventory() == null)
			{
				player.sendMessage(NaxColor.MSG + "You must have your good and cost items in the first slot of either chest.");
				return;
			}
			
			ItemStack good = chest2.getInventory().getItem(0);
			ItemStack cost = chest.getInventory().getItem(0);
			
			if (good == null || good.getAmount() == 0 || cost == null || cost.getAmount() == 0)
			{
				player.sendMessage(NaxColor.MSG + "You must have your good and cost items in the first slot of either chest.");
				return;
			}
			
			good = good.clone();
			cost = cost.clone();
			
			Block[] shelves = new Block[] { b, b.getRelative(BlockFace.UP) };
			Block[] chests = new Block[] { chestBlock, chestBlock2 };
			
			// event.getBlock().setType(Material.WALL_SIGN);
			// event.getBlock().setData(NaxUtil.getDirectionFromBlockFace(direction));
			
			DisplayShop shop = new DisplayShop(idCount++, player, event.getBlock(), shelves, chests, good, cost);
			
			shops.add(shop);
			
			save(shop);
			
			player.sendMessage(NaxColor.MSG + "Shop " + NaxColor.LOCAL + "successfully" + NaxColor.MSG + " created!");
			// System.out.println("New shop added!");
			
			event.setLine(0, NaxColor.SHOP + "Purchase");
			event.setLine(1, good.getAmount() + " " + NaxUtil.materialName(good.getType()));
			event.setLine(2, NaxColor.SHOP + "for");
			event.setLine(3, cost.getAmount() + " " + NaxUtil.materialName(cost.getType()));
		}
	}
	
	public void handleBlockBreak(BlockBreakEvent event)
	{
		if (event.isCancelled()) return;
		
		Block b = event.getBlock();
		Player player = event.getPlayer();
		
		for (DisplayShop s : shops)
		{
			if (!event.getBlock().getWorld().getName().equalsIgnoreCase(s.world.getName())) continue;
			
			for (Block c : s.chests)
			{
				if (c.getLocation().equals(b.getLocation()))
				{
					if (c.getType() != Material.CHEST) return;
					
					if (SuperManager.isSuper(player))
					{
						player.sendMessage(NaxColor.MSG + "This shop is now a super shop until the chests are replaced.");
					}
					else
					{
						event.setCancelled(true);
						player.sendMessage(NaxColor.MSG + "To remove the shop, break the sign first.");
					}
					return;
				}
			}
			
			for (Block c : s.shelves)
			{
				if (c.getLocation().equals(b.getLocation()))
				{
					event.setCancelled(true);
					event.getPlayer().sendMessage(NaxColor.MSG + "To remove the shop, break the sign first.");
					return;
				}
			}
			
			if (s.sign.getLocation().equals(b.getLocation()))
			{
				destroy(s);
				
				event.getPlayer().sendMessage(NaxColor.MSG + "Shop successfully removed!");
				return;
			}
		}
	}
	
	private void destroy(DisplayShop s)
	{
		s.goodItem.remove();
		s.costItem.remove();
		
		shops.remove(s);
		
		config.set("displayshops." + s.id, "");
		config.save();
	}
	
	public void handlePlayerPickupItem(PlayerPickupItemEvent event)
	{
		if (event.isCancelled()) return;
		
		for (DisplayShop s : shops)
		{
			if (!event.getItem().getWorld().getName().equalsIgnoreCase(s.world.getName())) continue;
			
			if (event.getItem().getLocation().equals(s.costItem.getLocation()) || event.getItem().getLocation().equals(s.goodItem.getLocation()))
			{
				event.setCancelled(true);
			}
		}
	}
	
	public void handleItemDespawn(ItemDespawnEvent event)
	{
		if (event.isCancelled()) return;
		
		for (DisplayShop s : shops)
		{
			if (!event.getEntity().getWorld().getName().equalsIgnoreCase(s.world.getName())) continue;
			
			if (event.getEntity().getLocation().equals(s.costItem.getLocation()) || event.getEntity().getLocation().equals(s.goodItem.getLocation()))
			{
				event.setCancelled(true);
			}
		}
	}
	
	public void handleExplosion(EntityExplodeEvent event)
	{
		for (DisplayShop s : shops)
		{
			if (!s.world.getName().equalsIgnoreCase(event.getLocation().getWorld().getName())) continue;
			
			for (Block c : s.chests)
			{
				if (event.blockList().contains(c))
				{
					event.setCancelled(true);
					return;
				}
			}
			
			for (Block c : s.shelves)
			{
				if (event.blockList().contains(c))
				{
					event.setCancelled(true);
					return;
				}
			}
			
			if (event.blockList().contains(s.sign))
			{
				event.setCancelled(true);
				return;
			}
		}
	}
	
	public void handlePlayerInteract(PlayerInteractEvent event)
	{
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock().getType() != Material.WALL_SIGN) return;
		
		Block sign = event.getClickedBlock();
		DisplayShop shop = null;
		Player player = event.getPlayer();
		
		for (DisplayShop s : shops)
		{
			if (sign.getLocation().equals(s.sign.getLocation()))
			{
				shop = s;
			}
		}
		
		if (shop == null) return;
		
		shop.buy(player);
	}
	
	public void loadShops()
	{
		config = new NaxFile(plugin, "displayshops");
		
		Set<String> keys = config.getKeys("displayshops");
		
		if (keys == null) return;
		
		for (String key : keys)
		{
			String prefix = "displayshops." + key;
			
			if (!config.contains(prefix + ".world")) continue;
			
			World world = plugin.getServer().getWorld(config.getString(prefix + ".world"));
			
			if (world == null) continue;
			
			OfflinePlayer owner = plugin.getServer().getOfflinePlayer(config.getString(prefix + ".owner"));
			
			Block sign = world.getBlockAt(config.getLocation(prefix + ".sign"));
			
			Block[] shelves = { world.getBlockAt(config.getLocation(prefix + ".shelf0")), world.getBlockAt(config.getLocation(prefix + ".shelf1")) };
			Block[] chests = { world.getBlockAt(config.getLocation(prefix + ".chest0")), world.getBlockAt(config.getLocation(prefix + ".chest1")) };
			
			ItemStack good = config.getItemStack(prefix + ".good");
			ItemStack cost = config.getItemStack(prefix + ".cost");
			
			Location goodItemLocation = config.getLocation(prefix + ".goodItem");
			Location costItemLocation = config.getLocation(prefix + ".costItem");
			
			Item goodItem = null;
			Item costItem = null;
			
			for (Entity e : world.getEntities())
			{
				if (e instanceof Item && e.getLocation().equals(goodItemLocation))
				{
					goodItem = (Item) e;
				}
				
				if (e instanceof Item && e.getLocation().equals(costItemLocation))
				{
					costItem = (Item) e;
				}
			}
			
			DisplayShop shop = new DisplayShop(Integer.parseInt(key), owner, sign, shelves, chests, good, cost, goodItem, costItem);
			
			shops.add(shop);
			if (idCount < shop.id + 1) idCount = shop.id + 1;
		}
	}
	
	public void save(DisplayShop shop)
	{
		String pr = "displayshops." + shop.id;
		
		config.set(pr, "");
		
		config.setItemStack(pr + ".good", shop.good);
		config.setItemStack(pr + ".cost", shop.cost);
		config.set(pr + ".owner", shop.owner.getName());
		config.set(pr + ".world", shop.world.getName());
		
		config.setLocation(pr + ".goodItem", shop.goodItem.getLocation());
		config.setLocation(pr + ".costItem", shop.costItem.getLocation());
		config.setLocation(pr + ".sign", shop.sign.getLocation());
		config.setLocation(pr + ".chest0", shop.chests[0].getLocation());
		config.setLocation(pr + ".chest1", shop.chests[1].getLocation());
		config.setLocation(pr + ".shelf0", shop.shelves[0].getLocation());
		config.setLocation(pr + ".shelf1", shop.shelves[1].getLocation());
		
		config.save();
		
	}
}
