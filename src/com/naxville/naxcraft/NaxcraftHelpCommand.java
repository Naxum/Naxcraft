package com.naxville.naxcraft;

import org.bukkit.entity.Player;
import org.bukkit.plugin.*;
import org.bukkit.command.*;

import com.naxville.naxcraft.player.PlayerManager.PlayerRank;

import java.lang.Integer;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class NaxcraftHelpCommand {
	public static Naxcraft plugin;
	private List<Command> commands = new ArrayList<Command>();
	private int init = 0;
	
	public NaxcraftHelpCommand(Naxcraft instance){
		plugin = instance;
	}
	
	public boolean runHelpCommand(CommandSender sender, String[] args){
		/*
		 * args = null : prints first page
		 * args[0] is int: prints page(args[0])
		 * args[0] is string: prints specific command help.
		 */
		if(this.init == 0){
			PluginManager manager = plugin.getServer().getPluginManager();
			
			/*
			for(int i=0; i<manager.getPlugins().length; i++){
				commands.addAll(PluginCommandYamlParser.parse(manager.getPlugins()[i]));
			}*/
			
			commands.addAll(PluginCommandYamlParser.parse(manager.getPlugin("Naxcraft")));
			this.init = 1;
		}
		
		List<Command> visibleCommands = new ArrayList<Command>();
		Iterator<Command> iter = commands.iterator();
		Command curr = null;
		while(iter.hasNext())
		{
			curr = iter.next();
			
			if(curr.getName().equalsIgnoreCase("tp"))
			{
				if(plugin.playerManager.getPlayer((Player)sender).rank.getId() >= PlayerRank.PATRON.getId())
				{
					visibleCommands.add(curr);
				}
			}
			else if (curr.getName().equalsIgnoreCase("drop") || curr.getName().equalsIgnoreCase("freeze") || curr.getName().equalsIgnoreCase("give") || curr.getName().equalsIgnoreCase("god")
					|| curr.getName().equalsIgnoreCase("kill") || curr.getName().equalsIgnoreCase("modkit") || curr.getName().equalsIgnoreCase("motd") || curr.getName().equalsIgnoreCase("spawnmob")
					|| curr.getName().equalsIgnoreCase("stealth") || curr.getName().equalsIgnoreCase("strike") || curr.getName().equalsIgnoreCase("super") || curr.getName().equalsIgnoreCase("time")
					|| curr.getName().equalsIgnoreCase("wg") || curr.getName().equalsIgnoreCase("weather") || curr.getName().equalsIgnoreCase("drop") || curr.getName().equalsIgnoreCase("promote")
					|| curr.getName().equalsIgnoreCase("demote") || curr.getName().equalsIgnoreCase("tphere") || curr.getName().equalsIgnoreCase("tpthere") || curr.getName().equalsIgnoreCase("demote")
					|| curr.getName().equalsIgnoreCase("transcend") || curr.getName().equalsIgnoreCase("demote"))
			{
				if(plugin.playerManager.getPlayer((Player)sender).rank.isAdmin())
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
		if(args.length == 0){
			int pageCap = visibleCommands.size()/7;
			if((visibleCommands.size()%7)!=0) pageCap += 1;
			
			sender.sendMessage(Naxcraft.COMMAND_COLOR + "Type /help <page number> to list commands. You have " + Naxcraft.DEFAULT_COLOR + pageCap + Naxcraft.COMMAND_COLOR + " pages.");
			return true;
		}
		
		if(args.length > 0){
			try {
				page = Integer.parseInt(args[0]);
			}
			catch (Exception e) {
				iter = visibleCommands.listIterator();
				curr = null;
				while(iter.hasNext()){
					curr = iter.next();
					if(curr.getName().equals(args[0])){
						sender.sendMessage(Naxcraft.DEFAULT_COLOR + "/" + curr.getName() + ":" + Naxcraft.COMMAND_COLOR + curr.getDescription());
						sender.sendMessage(Naxcraft.COMMAND_COLOR + curr.getUsage());
						return true;
					}
				}
				sender.sendMessage(Naxcraft.ERROR_COLOR + "No command named /" + args[0]);
				return true;
			}
		}
		if(page >= 1){
			if(((page-1)*7) < (visibleCommands.size())) {
				iter = visibleCommands.listIterator(((page-1)*7));
				int pageCap = visibleCommands.size()/7;
				if((visibleCommands.size()%7)!=0) pageCap += 1;
				
				sender.sendMessage("");
				sender.sendMessage(Naxcraft.DEFAULT_COLOR + "Page " + page + " of " + pageCap + ":");
				
				for(int i=0; (iter.hasNext()&&(i < 7)); i++) {
					curr = iter.next();
					sender.sendMessage(Naxcraft.DEFAULT_COLOR + "/" + curr.getName() + ": " + Naxcraft.COMMAND_COLOR + curr.getDescription());
				}
			}
		}
		return true;
	}
}
