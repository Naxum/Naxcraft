package com.naxville.naxcraft;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class NaxFile extends YamlConfiguration
{
	private File file = null;
	private Plugin plugin;
	
	/**
	 * Loads the file as a YamlConfiguration, creating it if it doesn't exist, and filling it if it's empty.
	 * The path to the file will be getDataFolder()'s results.
	 * 
	 * @param filename
	 *            The file's name WITHOUT the .yml ending.
	 */
	public NaxFile(Plugin plugin, String filename)
	{
		this.plugin = plugin;
		
		filename = filename.toLowerCase();
		
		File dir = plugin.getDataFolder();
		
		if (!dir.exists())
		{
			System.out.println("Creating directory " + dir.getPath());
			dir.mkdir();
		}
		
		filename.replace(".yml", "");
		
		File f = new File(dir.getPath() + "/" + filename + ".yml");
		
		if (!f.exists())
		{
			try
			{
				f.createNewFile();
				BufferedWriter w = new BufferedWriter(new FileWriter(f));
				w.write(filename + ": {}");
				
				w.close();
			}
			catch (IOException e)
			{
				System.out.println("Error creating new file for " + dir.getPath() + "/" + filename);
				e.printStackTrace();
			}
		}
		
		try
		{
			load(f);
		}
		catch (FileNotFoundException e) // Probably shouldn't happen
		{
			e.printStackTrace();
		}
		catch (IOException e) // Probably shouldn't happen
		{
			e.printStackTrace();
		}
		catch (InvalidConfigurationException e) // Not sure what this is.
		{
			e.printStackTrace();
		}
		
		this.file = f;
	}
	
	/**
	 * Rturns null if there is no configuration section to the path.
	 * 
	 * @param path
	 * @return
	 */
	public Set<String> getKeys(String path)
	{
		if (getConfigurationSection(path) == null) return null;
		
		return getConfigurationSection(path).getKeys(false);
	}
	
	public Location getLocation(String path)
	{
		World world = plugin.getServer().getWorld(this.getString(path + ".world"));
		
		double x = this.getDouble(path + ".x");
		double y = this.getDouble(path + ".y");
		double z = this.getDouble(path + ".z");
		
		return new Location(world, x, y, z);
	}
	
	public void setItemStack(String path, ItemStack item)
	{
		this.set(path + ".type", item.getTypeId());
		this.set(path + ".damage", item.getDurability());
		this.set(path + ".amount", item.getAmount());
	}
	
	@Override
	public ItemStack getItemStack(String path)
	{
		int type = this.getInt(path + ".type");
		int amount = this.getInt(path + ".amount");
		short damage = (short) this.getInt(path + ".damage");
		
		return new ItemStack(type, amount, damage);
	}
	
	public void setLocation(String path, Location loc)
	{
		this.set(path + ".world", loc.getWorld().getName());
		this.set(path + ".x", loc.getX());
		this.set(path + ".y", loc.getY());
		this.set(path + ".z", loc.getZ());
	}
	
	public void setProperty(String path, Object value)
	{
		super.set(path, value);
	}
	
	/**
	 * Removes everything at a path.
	 * 
	 * @param path
	 */
	public void removeProperty(String path)
	{
		super.set(path, "");
	}
	
	public void save()
	{
		try
		{
			save(file);
		}
		catch (IOException e)
		{
			System.out.println("Error saving NaxFile " + file.getName());
			e.printStackTrace();
		}
	}
}
