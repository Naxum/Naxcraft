package com.naxville.naxcraft.shops;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.naxville.naxcraft.NaxFile;
import com.naxville.naxcraft.Naxcraft;
import com.naxville.naxcraft.autoareas.AutoAreaManager.Flag;
import com.naxville.naxcraft.autoareas.AutoBase;

public class ShopManager
{
	public Naxcraft plugin;
	protected Map<String, Material> materials;
	protected Map<Location, Shop> shops;
	protected NaxFile config;
	protected int id;
	
	public ShopManager(Naxcraft plugin)
	{
		this.plugin = plugin;
		
		shops = new HashMap<Location, Shop>();
		
		materials = new HashMap<String, Material>();
		materials.put("B Mushroom", Material.BROWN_MUSHROOM);
		materials.put("Cobble Stairs", Material.COBBLESTONE_STAIRS);
		materials.put("Lighter", Material.FLINT_AND_STEEL);
		materials.put("Milk", Material.MILK_BUCKET);
		materials.put("Water", Material.WATER_BUCKET);
		materials.put("Moss Stone", Material.MOSSY_COBBLESTONE);
		
		materials.put("Leather Boots", Material.LEATHER_BOOTS);
		materials.put("Leather Helm", Material.LEATHER_HELMET);
		materials.put("Leather Chest", Material.LEATHER_CHESTPLATE);
		materials.put("Leather Legs", Material.LEATHER_LEGGINGS);
		
		materials.put("Iron Boots", Material.IRON_BOOTS);
		materials.put("Iron Helm", Material.IRON_HELMET);
		materials.put("Iron Chest", Material.IRON_CHESTPLATE);
		materials.put("Iron Legs", Material.IRON_LEGGINGS);
		
		materials.put("Chain Boots", Material.CHAINMAIL_BOOTS);
		materials.put("Chain Chest", Material.CHAINMAIL_CHESTPLATE);
		materials.put("Chain Helm", Material.CHAINMAIL_HELMET);
		materials.put("Chain Legs", Material.CHAINMAIL_LEGGINGS);
		
		materials.put("Diamond Chest", Material.DIAMOND_CHESTPLATE);
		materials.put("Diamond Helm", Material.DIAMOND_HELMET);
		materials.put("Diamond Boots", Material.DIAMOND_BOOTS);
		materials.put("Diamond Legs", Material.DIAMOND_LEGGINGS);
		
		materials.put("Gold Chest", Material.GOLD_CHESTPLATE);
		materials.put("Gold Helm", Material.GOLD_HELMET);
		materials.put("Gold Boots", Material.GOLD_BOOTS);
		materials.put("Gold Legs", Material.GOLD_LEGGINGS);
		
		id = 0;
	}
	
	/**
	 * Checks the player's permission for "super," and creates a shop if correctly syntaxed.
	 * 
	 * @param player
	 *            Player that placed the sign
	 * @param event
	 *            The sign event because the sign isn't udpated until after the event.
	 */
	public void handleSign(SignChangeEvent event)
	{
		Player player = event.getPlayer();
		
		AutoBase base = plugin.autoAreaManager.getBase(event.getBlock());
		
		if (base == null || (!base.hasFlag(Flag.MERCHANT) || !plugin.autoAreaManager.isOwner(player, base)))
		{
			if (!plugin.playerManager.getPlayer(player).rank.isAdmin()) { return; }
		}
		
		boolean correct = true;
		
		Material price = Material.AIR;
		int priceAmount = 0;
		
		Material good = Material.AIR;
		int goodAmount = 0;
		
		if (!event.getLine(0).equalsIgnoreCase("buy"))
		{
			correct = false;
			return;
		}
		
		Pattern p = Pattern.compile("\\d*[a-zA-Z\\s]*");
		Matcher m = p.matcher(event.getLine(1));
		
		if (!m.matches())
		{
			correct = false;
			
		}
		else
		{
			String[] split = event.getLine(1).split(" ");
			
			try
			{
				goodAmount = Integer.parseInt(split[0]);
			}
			catch (NumberFormatException e)
			{
				correct = false;
				return;
			}
			
			split[0] = "";
			String mat = arrayToString(split);
			if (this.materials.containsKey(mat))
			{
				good = this.materials.get(mat);
				
			}
			else
			{
				String fixedup = mat.replace(" ", "_").toUpperCase();
				if (Material.getMaterial(fixedup) != null)
				{
					good = Material.getMaterial(fixedup);
				}
				else
				{
					player.sendMessage(Naxcraft.ERROR_COLOR + "Error: " + Naxcraft.DEFAULT_COLOR + mat + Naxcraft.ERROR_COLOR + " is not a material.");
					correct = false;
				}
			}
		}
		
		if (!event.getLine(2).equalsIgnoreCase("for"))
		{
			correct = false;
		}
		
		m = p.matcher(event.getLine(3));
		
		if (!m.matches())
		{
			correct = false;
		}
		else
		{
			String[] split = event.getLine(3).split(" ");
			
			try
			{
				priceAmount = Integer.parseInt(split[0]);
			}
			catch (NumberFormatException e)
			{
				correct = false;
				return;
			}
			
			split[0] = "";
			String mat = arrayToString(split);
			if (this.materials.containsKey(mat))
			{
				price = this.materials.get(mat);
				
			}
			else
			{
				String fixedup = mat.replace(" ", "_").toUpperCase();
				if (Material.getMaterial(fixedup) != null)
				{
					price = Material.getMaterial(fixedup);
				}
				else
				{
					player.sendMessage(Naxcraft.ERROR_COLOR + "Error: " + Naxcraft.DEFAULT_COLOR + mat + Naxcraft.ERROR_COLOR + " is not a material.");
					correct = false;
				}
			}
		}
		
		if (correct)
		{
			event.setLine(0, "" + ChatColor.RED + event.getLine(0));
			Shop shop = new Shop(this, id, event.getBlock().getLocation(), good, goodAmount, price, priceAmount, event.getLines(), event.getPlayer().getName().toLowerCase());
			this.shops.put(event.getBlock().getLocation(), shop);
			this.save(shop);
			
			id++;
			player.sendMessage(Naxcraft.MSG_COLOR + "You " + Naxcraft.SUCCESS_COLOR + "successfully" + Naxcraft.MSG_COLOR + " created a shop.");
			
		}
		else
		{
			// player.sendMessage("Error for some reason. :(");
		}
	}
	
	/**
	 * Handles a player attempting to buy from a shop, on right click of a sign.
	 */
	public void handleShopClick(PlayerInteractEvent event)
	{
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.isCancelled()) { return; }
		
		if (this.shops.containsKey(event.getClickedBlock().getLocation()))
		{
			this.shops.get(event.getClickedBlock().getLocation()).buy(event.getPlayer());
			event.setCancelled(true);
		}
	}
	
	public boolean loadShops()
	{
		config = new NaxFile(plugin, "shops");
		
		Set<String> shopList = config.getConfigurationSection("shops").getKeys(false);
		
		if (shopList == null || shopList.isEmpty()) return true;
		
		for (String id : shopList)
		{
			String prefix = "shops." + id + ".";
			
			if (!config.contains(prefix + "world")) continue;
			
			World world = plugin.getServer().getWorld(config.getString(prefix + "world"));
			
			if (world == null)
			{
				System.out.println("null world");
				continue;
			}
			
			Location location = new Location(world,
													Double.parseDouble(config.getString(prefix + "location.x")),
													Double.parseDouble(config.getString(prefix + "location.y")),
													Double.parseDouble(config.getString(prefix + "location.z")));
			
			String owner = config.getString(prefix + "owner");
			
			String[] lines = { config.getString(prefix + "line1"), config.getString(prefix + "line2"), config.getString(prefix + "line3"), config.getString(prefix + "line4") };
			
			Material good = Material.getMaterial(config.getString(prefix + "good"));
			int goodAmount = config.getInt(prefix + "goodAmount");
			
			Material price = Material.getMaterial(config.getString(prefix + "price"));
			int priceAmount = config.getInt(prefix + "priceAmount");
			
			this.shops.put(location, new Shop(this, Integer.parseInt(id), location, good, goodAmount, price, priceAmount, lines, owner));
			if (this.id < Integer.parseInt(id) + 1)
			{
				// System.out.println("New shop with: " + Integer.parseInt(id) + ", " + location.toString() + ", " + good.toString() + ", " + goodAmount + ", " + price.toString() + ", " + priceAmount + ", " + lines.toString() + ", " + owner);
				this.id = Integer.parseInt(id) + 1;
			}
		}
		return true;
	}
	
	protected void save(Shop shop)
	{
		String prefix = "shops." + shop.getId() + ".";
		
		config.set(prefix + ".world", shop.world.getName());
		
		config.set(prefix + "location.x", shop.saveX());
		config.set(prefix + "location.y", shop.saveY());
		config.set(prefix + "location.z", shop.saveZ());
		
		config.set(prefix + "owner", shop.saveOwner());
		
		for (int i = 1; i < 5; i++)
		{
			config.set(prefix + "line" + i, shop.getLines()[i - 1]);
		}
		
		config.set(prefix + "good", shop.saveGood());
		config.set(prefix + "goodAmount", shop.saveGoodAmount());
		
		config.set(prefix + "price", shop.savePrice());
		config.set(prefix + "priceAmount", shop.savePriceAmount());
		
		config.save();
		return;
	}
	
	protected void remove(Shop shop)
	{
		this.shops.remove(shop.getLocation());
		config.removeProperty("shops." + shop.getId());
		config.save();
	}
	
	public void handleBlockBreak(BlockBreakEvent event)
	{
		if (this.shops.containsKey(event.getBlock().getLocation()))
		{
			this.remove(this.shops.get(event.getBlock().getLocation()));
			event.getPlayer().sendMessage(Naxcraft.MSG_COLOR + "Shop deleted.");
		}
	}
	
	private String arrayToString(String[] array)
	{
		StringBuffer buffer = new StringBuffer();
		String seperator = " ";
		int newWords = 0;
		if (array.length > 0)
		{
			for (int i = 0; i < array.length; i++)
			{
				if (!array[i].equals(""))
				{
					if (newWords != 0) buffer.append(seperator);
					buffer.append(array[i]);
					newWords++;
				}
			}
		}
		
		return buffer.toString();
	}
}
