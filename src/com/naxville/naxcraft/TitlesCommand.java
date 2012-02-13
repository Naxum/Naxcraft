package com.naxville.naxcraft;

import org.bukkit.entity.Player;

import com.naxville.naxcraft.player.NaxPlayer;
import com.naxville.naxcraft.player.PlayerManager.PlayerRank;
import com.naxville.naxcraft.player.PlayerManager.Title;

public class TitlesCommand extends NaxCommand
{
	
	protected TitlesCommand(Naxcraft plugin)
	{
		super(plugin, PlayerRank.NOOB);
	}
	
	public void runCommand(Player player, String[] args)
	{
		if (args.length == 0)
		{
			printHelp(player);
			return;
		}
		
		if (args.length == 1 && args[0].equalsIgnoreCase("listall"))
		{
			player.sendMessage(NaxColor.MSG + "----");
			for (Title t : Title.values())
			{
				player.sendMessage(NaxColor.MSG + "[" + t.getSymbol() + "] " + t.getName() + ": " + t.getDescription());
			}
		}
		else if (args.length == 0)
		{
			printHelp(player);
			return;
		}
		
		if (args[0].equalsIgnoreCase("list"))
		{
			NaxPlayer target = plugin.playerManager.getPlayer(player);
			
			if (args.length == 2)
			{
				target = plugin.playerManager.getPlayer(args[1]);
				if (target == null)
				{
					notAPlayer(player, args[1]);
					return;
				}
			}
			
			player.sendMessage(NaxColor.MSG + "----");
			
			if (target.titles.isEmpty())
			{
				player.sendMessage(target.getChatName() + " has no active titles.");
			}
			else
			{
				String titles = "";
				for (Title t : target.titles)
				{
					if (titles != "") titles += NaxColor.MSG + ", ";
					titles += t.getName();
				}
				player.sendMessage(target.getChatName() + "'s titles: " + titles);
			}
			
			if (target.hiddenTitles.isEmpty())
			{
				player.sendMessage(target.getChatName() + " has no hidden titles.");
			}
			else
			{
				String titles = "";
				for (Title t : target.hiddenTitles)
				{
					if (titles != "") titles += NaxColor.MSG + ", ";
					titles += t.getName();
				}
				
				player.sendMessage(target.getChatName() + "'s hidden titles: " + titles);
			}
		}
		
		if (args.length == 2 && args[0].equalsIgnoreCase("info"))
		{
			Title target = null;
			
			for (Title t : Title.values())
			{
				if (args[1].equalsIgnoreCase(t.getSymbol()))
				{
					target = t;
					break;
				}
			}
			
			if (target == null)
			{
				for (Title t : Title.values())
				{
					if (t.getCleanName().toLowerCase().startsWith(args[1].toLowerCase()))
					{
						target = t;
						break;
					}
				}
			}
			
			if (target == null)
			{
				player.sendMessage(NaxColor.MSG + args[1] + " is not a symbol or beginning of a name of a title.");
				return;
			}
			
			player.sendMessage(NaxColor.MSG + "[" + target.getSymbol() + "] " + target.getName() + ": " + target.getDescription());
		}
		
		if (args.length == 2 && (args[0].equalsIgnoreCase("show") || args[0].equalsIgnoreCase("hide")))
		{
			NaxPlayer np = plugin.playerManager.getPlayer(player);
			Title target = null;
			
			for (Title t : Title.values())
			{
				if (args[1].equalsIgnoreCase(t.getCleanSymbol()))
				{
					target = t;
					break;
				}
			}
			
			if (target == null)
			{
				for (Title t : Title.values())
				{
					if (t.getCleanName().toLowerCase().startsWith(args[0].toLowerCase()))
					{
						target = t;
						break;
					}
				}
			}
			
			if (target == null)
			{
				player.sendMessage(NaxColor.MSG + args[1] + " is not a symbol or beginning of a name of a title.");
				return;
			}
			else if (!np.hasTitle(target))
			{
				player.sendMessage(NaxColor.MSG + "You do not have the " + target.getName() + " title. You can be more specific by typing in the first word in the name of the title you wish to show or hide.");
				return;
			}
			
			String hide = "hidden";
			if (args[0].equalsIgnoreCase("hide"))
			{
				np.titles.remove(target);
				np.hiddenTitles.remove(target);
				np.hiddenTitles.add(target);
			}
			else
			{
				hide = "shown";
				np.titles.remove(target);
				np.hiddenTitles.remove(target);
				np.titles.add(target);
			}
			
			player.sendMessage(target.getName() + " is now " + hide);
		}
	}
	
	public void printHelp(Player player)
	{
		player.sendMessage(NaxColor.MSG + "----");
		player.sendMessage(NaxColor.COMMAND + "/titles command:");
		player.sendMessage(NaxColor.MSG + "/titles " + NaxColor.WHITE + "listall" + NaxColor.MSG + ": " + "Lists all titles.");
		player.sendMessage(NaxColor.MSG + "/titles " + NaxColor.WHITE + "list [name]" + NaxColor.MSG + ": " + "L yours or others' titles.");
		player.sendMessage(NaxColor.MSG + "/titles " + NaxColor.WHITE + "hide [name]" + NaxColor.MSG + ": " + "Hides owned title.");
		player.sendMessage(NaxColor.MSG + "/titles " + NaxColor.WHITE + "show [name]" + NaxColor.MSG + ": " + "Shows owned title.");
		player.sendMessage(NaxColor.MSG + "/titles " + NaxColor.WHITE + "info [symbol/name]" + NaxColor.MSG + ": " + "Gives info on title.");
		player.sendMessage(NaxColor.MSG + "When typing in title names, " + NaxColor.COMMAND + "only use the first word, " + NaxColor.MSG + "and make sure you use" + NaxColor.COMMAND + " no spaces" + NaxColor.MSG + ".");
	}
	
}
