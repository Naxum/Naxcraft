package com.naxville.naxcraft.admin;

import org.bukkit.entity.Player;

import com.naxville.naxcraft.NaxColor;
import com.naxville.naxcraft.NaxCommand;
import com.naxville.naxcraft.Naxcraft;
import com.naxville.naxcraft.player.NaxPlayer;
import com.naxville.naxcraft.player.PlayerManager.PlayerRank;
import com.naxville.naxcraft.player.PlayerManager.Title;

public class TitleCommand extends NaxCommand
{
	
	public TitleCommand(Naxcraft plugin)
	{
		super(plugin, PlayerRank.MODERATOR);
	}
	
	public void runCommand(Player player, String[] args)
	{
		if (args.length == 0)
		{
			printHelp(player);
			return;
		}
		
		if (args.length == 1 && args[0].equalsIgnoreCase("list")) // title [list]
		{
			player.sendMessage(NaxColor.MSG + "----");
			for (Title t : Title.values())
			{
				player.sendMessage(NaxColor.MSG + "[" + t.getSymbol() + ": " + NaxColor.WHITE + t.name() + NaxColor.MSG + "] " + t.getName() + ": " + t.getDescription());
			}
			return;
		}
		else if (args.length == 1)
		{
			printHelp(player);
			return;
		}
		
		NaxPlayer np = plugin.playerManager.getPlayer(args[1]);
		if (np == null)
		{
			notAPlayer(player, args[1]);
			return;
		}
		
		if (args.length == 2 && args[0].equalsIgnoreCase("strip")) // title [strip, name]
		{
			np.titles.clear();
			np.hiddenTitles.clear();
			
			plugin.playerManager.savePlayer(np);
			
			plugin.announcer.announce(np.getChatName() + " has had all of their titles stripped.");
		}
		else if (args.length == 3 && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("add"))) // title [give, name, title]
		{
			Title t = null;
			
			for (Title tx : Title.values())
			{
				if (tx.name().equalsIgnoreCase(args[2]))
				{
					t = tx;
					break;
				}
			}
			
			if (t == null)
			{
				notATitle(player, args[2]);
				return;
			}
			
			if (np.titles.contains(t) || np.hiddenTitles.contains(t))
			{
				player.sendMessage(NaxColor.MSG + "Player already has that title, they may have chosen to hide it.");
				return;
			}
			
			np.titles.add(t);
			plugin.playerManager.savePlayer(np);
			
			plugin.announcer.announce(np.getChatName() + " has been awarded the " + t.getName() + " title!");
		}
		else if (args.length == 3 && args[0].equalsIgnoreCase("remove"))
		{
			Title t = null;
			
			for (Title tx : Title.values())
			{
				if (tx.name().equalsIgnoreCase(args[2]))
				{
					t = tx;
					break;
				}
			}
			
			if (t == null)
			{
				notATitle(player, args[2]);
				return;
			}
			
			if (!np.titles.contains(t) && !np.hiddenTitles.contains(t))
			{
				player.sendMessage(NaxColor.MSG + "Player does not have that title.");
				return;
			}
			
			np.titles.remove(t);
			np.hiddenTitles.remove(t);
			plugin.playerManager.savePlayer(np);
			
			plugin.announcer.announce(np.getChatName() + " has had their " + t.getName() + " title removed.");
		}
		else
		{
			printHelp(player);
		}
		
	}
	
	public void notATitle(Player player, String name)
	{
		player.sendMessage(name + NaxColor.MSG + " is not a title.");
	}
	
	public void printHelp(Player player)
	{
		player.sendMessage(NaxColor.MSG + "----");
		player.sendMessage(NaxColor.COMMAND + "/title command:");
		player.sendMessage(NaxColor.MSG + "/title" + NaxColor.WHITE + "list" + NaxColor.MSG + ": " + "Lists all titles.");
		player.sendMessage(NaxColor.MSG + "/title" + NaxColor.WHITE + "give [name] [title]" + NaxColor.MSG + ": " + "Awards someone a title.");
		player.sendMessage(NaxColor.MSG + "/title" + NaxColor.WHITE + "remove [name] [title]" + NaxColor.MSG + ": " + "Removes someone's title.");
		player.sendMessage(NaxColor.MSG + "/title" + NaxColor.WHITE + "strip [name]" + NaxColor.MSG + ": " + "Strips someone of all titles.");
	}
}
