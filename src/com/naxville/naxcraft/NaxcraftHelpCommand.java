package com.naxville.naxcraft;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommandYamlParser;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

import com.naxville.naxcraft.player.NaxPlayer;
import com.naxville.naxcraft.player.PlayerManager.PlayerRank;

public class NaxcraftHelpCommand
{
	public static Naxcraft plugin;
	private List<Command> commands = new ArrayList<Command>();
	private int init = 0;
	
	public NaxcraftHelpCommand(Naxcraft instance)
	{
		plugin = instance;
	}
	
	public boolean runHelpCommand(CommandSender sender, String[] args)
	{
		/*
		 * args = null : prints first page
		 * args[0] is int: prints page(args[0])
		 * args[0] is string: prints specific command help.
		 */
		if (this.init == 0)
		{
			PluginManager manager = plugin.getServer().getPluginManager();
			
			if (plugin.playerManager.getPlayer((Player) sender).rank.isAdmin())
			{
				for (int i = 0; i < manager.getPlugins().length; i++)
				{
					commands.addAll(PluginCommandYamlParser.parse(manager.getPlugins()[i]));
				}
			}
			else
			{
				commands.addAll(PluginCommandYamlParser.parse(manager.getPlugin("Naxcraft")));
			}
			
			this.init = 1;
		}
		
		List<Command> visibleCommands = new ArrayList<Command>();
		Iterator<Command> iter = commands.iterator();
		Command curr = null;
		while (iter.hasNext())
		{
			curr = iter.next();
			
			if (curr.getName().equalsIgnoreCase("join"))
			{
				NaxPlayer p = plugin.playerManager.getPlayer((Player) sender);
				
				if (!p.rank.isAdmin())
				{
					if (p.rank == PlayerRank.NOOB)
					{
						visibleCommands.add(curr);
					}
				}
			}
			
			if (curr.getName().equalsIgnoreCase("tp") || curr.getName().equalsIgnoreCase("back") || curr.getName().equalsIgnoreCase("fire") || curr.getName().equalsIgnoreCase("hat"))
			{
				if (plugin.playerManager.getPlayer((Player) sender).isDemiAdminOrPatron())
				{
					visibleCommands.add(curr);
				}
			}
			else if (curr.getName().equalsIgnoreCase("stealth") || curr.getName().equalsIgnoreCase("drop") || curr.getName().equalsIgnoreCase("freeze") || curr.getName().equalsIgnoreCase("time")
					|| curr.getName().equalsIgnoreCase("weather") || curr.getName().equalsIgnoreCase("tphere") || curr.getName().equalsIgnoreCase("god"))
			{
				if (plugin.playerManager.getPlayer((Player) sender).rank.isDemiAdmin())
				{
					visibleCommands.add(curr);
				}
			}
			else if (curr.getName().equalsIgnoreCase("give")
					|| curr.getName().equalsIgnoreCase("kill") || curr.getName().equalsIgnoreCase("modkit") || curr.getName().equalsIgnoreCase("motd") || curr.getName().equalsIgnoreCase("spawnmob")
					|| curr.getName().equalsIgnoreCase("strike") || curr.getName().equalsIgnoreCase("super")
					|| curr.getName().equalsIgnoreCase("wg") || curr.getName().equalsIgnoreCase("drop") || curr.getName().equalsIgnoreCase("promote")
					|| curr.getName().equalsIgnoreCase("demote") || curr.getName().equalsIgnoreCase("tpthere") || curr.getName().equalsIgnoreCase("save-all")
					|| curr.getName().equalsIgnoreCase("transcend") || curr.getName().equalsIgnoreCase("setspawn") || curr.getName().equalsIgnoreCase("boom"))
			{
				if (plugin.playerManager.getPlayer((Player) sender).rank.isAdmin())
				{
					visibleCommands.add(curr);
				}
			}
			else
			{
				visibleCommands.add(curr);
			}
		}
		
		int page = 1;
		if (args.length == 0)
		{
			int pageCap = visibleCommands.size() / 7;
			if ((visibleCommands.size() % 7) != 0) pageCap += 1;
			
			sender.sendMessage(Naxcraft.COMMAND_COLOR + "Type /help <page number> to list commands. You have " + Naxcraft.DEFAULT_COLOR + pageCap + Naxcraft.COMMAND_COLOR + " pages.");
			return true;
		}
		
		if (args.length > 0)
		{
			try
			{
				page = Integer.parseInt(args[0]);
			}
			catch (Exception e)
			{
				iter = visibleCommands.listIterator();
				curr = null;
				while (iter.hasNext())
				{
					curr = iter.next();
					if (curr.getName().equals(args[0]))
					{
						sender.sendMessage(Naxcraft.DEFAULT_COLOR + "/" + curr.getName() + ":" + Naxcraft.COMMAND_COLOR + curr.getDescription());
						sender.sendMessage(Naxcraft.COMMAND_COLOR + curr.getUsage());
						return true;
					}
				}
				sender.sendMessage(Naxcraft.ERROR_COLOR + "No command named /" + args[0]);
				return true;
			}
		}
		if (page >= 1)
		{
			if (((page - 1) * 7) < (visibleCommands.size()))
			{
				iter = visibleCommands.listIterator(((page - 1) * 7));
				int pageCap = visibleCommands.size() / 7;
				if ((visibleCommands.size() % 7) != 0) pageCap += 1;
				
				sender.sendMessage("");
				sender.sendMessage(Naxcraft.DEFAULT_COLOR + "Page " + page + " of " + pageCap + ":");
				
				for (int i = 0; (iter.hasNext() && (i < 7)); i++)
				{
					curr = iter.next();
					sender.sendMessage(Naxcraft.DEFAULT_COLOR + "/" + curr.getName() + ": " + Naxcraft.COMMAND_COLOR + curr.getDescription());
				}
			}
		}
		return true;
	}
}
