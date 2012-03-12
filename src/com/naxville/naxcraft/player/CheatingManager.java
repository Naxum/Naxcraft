package com.naxville.naxcraft.player;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class CheatingManager
{
	public PlayerManager pm;
	public List<Material> materials = new ArrayList<Material>();
	public SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd hh:mm a zzz");
	
	public HashMap<Player, MiningManager> miningManagers = new HashMap<Player, MiningManager>();
	
	public CheatingManager(PlayerManager pm)
	{
		this.pm = pm;
		
		materials.add(Material.GOLD_ORE);
		materials.add(Material.DIAMOND_ORE);
		materials.add(Material.IRON_ORE);
		materials.add(Material.STONE);
		materials.add(Material.LAPIS_ORE);
		materials.add(Material.REDSTONE_ORE);
		
		dateFormat.setTimeZone(TimeZone.getTimeZone("EST"));
	}
	
	public void loadCheatingManager()
	{
		File f = pm.plugin.getDataFolder();
		
		if(!f.exists()) f.mkdir();
		
		f = new File(f.getPath() + "/cheats");
		
		if(!f.exists()) f.mkdir();
		
		f = new File(f.getPath() + "/mining.txt");
		
		if(!f.exists())
		{
			try
			{
				System.out.println("New File");
				f.createNewFile();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public void handleBlockPlace(BlockPlaceEvent event)
	{
		if (event.isCancelled()) return;
		
		Player player = event.getPlayer();
		Block block = event.getBlock();
		
		if (block.getType() != Material.TORCH) { return; }
		
		MiningManager mm = getMiningManager(player);
		
		if (mm.hasTimer)
		{
			mm.addTorch();
		}
	}
	
	public void handleBlockBreak(BlockBreakEvent event)
	{
		if (event.isCancelled()) return;
		
		Player player = event.getPlayer();
		Block block = event.getBlock();
		
		if (!materials.contains(block.getType())) { return; }
		
		getMiningManager(player).add(block.getType());
	}
	
	public MiningManager getMiningManager(Player player)
	{
		if (!miningManagers.containsKey(player))
		{
			miningManagers.put(player, new MiningManager(player, new Date()));
		}
		return miningManagers.get(player);
	}
	
	public class MiningManager
	{
		public Player player;
		public Date start;
		public int torchCount = 0;
		public boolean hasTimer = false;
		public HashMap<Material, Integer> mined = new HashMap<Material, Integer>();
		
		public MiningManager(Player player, Date date)
		{
			this.player = player;
			this.start = date;
		}
		
		public void add(Material mat)
		{
			if (mined.containsKey(mat))
			{
				mined.put(mat, mined.get(mat) + 1);
			}
			else
			{
				mined.put(mat, 1);
			}
			
			startTimer();
		}
		
		public void addTorch()
		{
			torchCount++;
		}
		
		public void startTimer()
		{
			if (!hasTimer)
			{
				System.out.println("Starting mining counter for " + player.getName());
				
				start = new Date();
				hasTimer = true;
				
				pm.plugin.getServer().getScheduler().scheduleSyncDelayedTask(pm.plugin, new MiningManagerTimer(this), 20 * 10);
			}
		}
		
		public void print(String reason)
		{
			BufferedWriter out = null;
			try
			{
				out = new BufferedWriter(new FileWriter(pm.plugin.getDataFolder().getPath() + "/cheats/mining.txt", true));
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			
			if(out == null)
			{
				boolean okay = false;
				
				for (Material mat : mined.keySet())
				{
					if (mat != Material.STONE) okay = true;
				}
				
				if (okay)
				{
					System.out.println("----------------------");
					System.out.println("Player " + player.getName() + " Mining Details : Reason: " + reason + " : Elapsed Time: " + ((new Date().getTime() - start.getTime()) / 1000 / 60) + " minutes");
					
					System.out.println(" ");
					
					for (Material mat : mined.keySet())
					{
						String message = "";
						int amount = mined.get(mat);
						if (mat != Material.STONE && amount > 30)
						{
							message += "WARNING: ";
						}
						
						message += mat.toString() + ": " + amount;
						
						System.out.println(message);
					}
					
					System.out.println("Torches placed: " + torchCount);
					System.out.println("World: " + player.getWorld().getName());
					System.out.println("----------------------");
				}
				else
				{
					System.out.println(player.getName() + " only mined stone.");
				}
			}
			else
			{
				try
				{
					boolean okay = false;
					
					for (Material mat : mined.keySet())
					{
						if (mat != Material.STONE) okay = true;
					}
					
					if (okay)
					{
						out.newLine();
						out.write("----------------------");
						out.newLine();
						
						out.write("Mining Details: (Reason: " + reason + ")");
						out.newLine();
						
						out.write("Player: " + player.getName());
						out.newLine();
						
						out.newLine();
						out.write("Date: " + dateFormat.format(new Date()));
						
						out.newLine();
						out.write("Elapsed Time: " + ((new Date().getTime() - start.getTime()) / 1000 / 60) + " minutes");
						out.newLine();
						
						for (Material mat : mined.keySet())
						{
							String message = "";
							int amount = mined.get(mat);
							if (mat != Material.STONE && amount > 30)
							{
								message += "WARNING: ";
							}
							
							message += mat.toString() + ": " + amount;
							
							out.newLine();
							out.write(message);
						}
						
						out.newLine();
						out.write("Torches placed: " + torchCount);
						
						out.newLine();
						out.newLine();
						out.write("World: " + player.getWorld().getName());
						out.newLine();
						out.write("----------------------");
					}
					else
					{
						out.newLine();
						out.write(player.getName() + " only mined stone.");
					}
					
					out.close();
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
			}			
			
			mined.clear();
			torchCount = 0;
			hasTimer = false;
		}
	}
	
	public class MiningManagerTimer implements Runnable
	{
		public Date end;
		public MiningManager mm;
		
		public MiningManagerTimer(MiningManager mm)
		{
			this.mm = mm;
			this.end = new Date(mm.start.getTime() + (1000 * 60 * 30)); // 30 minutes
		}
		
		public void run()
		{
			if (end.getTime() - new Date().getTime() <= 0)
			{
				mm.print("Time Up");
			}
			else if (!mm.player.isOnline())
			{
				mm.print("Sign Out");
			}
			else
			{
				pm.plugin.getServer().getScheduler().scheduleSyncDelayedTask(pm.plugin, new MiningManagerTimer(mm), 20 * 10);
			}
		}
	}
}
