package com.naxville.naxcraft.skills;

import org.bukkit.entity.Player;

import com.naxville.naxcraft.NaxColor;
import com.naxville.naxcraft.Naxcraft;
import com.naxville.naxcraft.player.NaxPlayer;

public class LevelCommand
{
	public Naxcraft plugin;
	
	public LevelCommand(Naxcraft plugin)
	{
		this.plugin = plugin;
	}
	
	public void runCommand(Player player, String[] args)
	{
		NaxPlayer np = plugin.playerManager.getPlayer(player);
		
		if ((args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("list"))) && !np.rank.isAdmin())
		{
			player.sendMessage(NaxColor.MSG + "----");
			player.sendMessage(NaxColor.MSG + "Levels or players online: ");
			String result = "";
			
			for (Player p : player.getWorld().getPlayers())
			{
				if (result != "") result += NaxColor.MSG + ", ";
				result += plugin.getNickName(p) + " is level " + NaxColor.LEVEL + p.getLevel();
			}
			
			player.sendMessage(result);
			return;
		}
		
		if (!np.rank.isAdmin()) return;
		
		if (args.length == 0)
		{
			player.sendMessage(NaxColor.MSG + "----");
			player.sendMessage(NaxColor.COMMAND + "/level command:");
			player.sendMessage(NaxColor.MSG + "/level " + NaxColor.WHITE + "list" + NaxColor.MSG + " - " + "Shows all online players' levels.");
			player.sendMessage(NaxColor.MSG + "/level " + NaxColor.WHITE + "reset" + NaxColor.MSG + " - " + "Resets all online players' levels to 0.");
			player.sendMessage(NaxColor.MSG + "/level " + NaxColor.WHITE + "[player] reset" + NaxColor.MSG + " - " + "Resets that player's level to 0.");
			player.sendMessage(NaxColor.MSG + "/level " + NaxColor.WHITE + "set [player] [level]" + NaxColor.MSG + " - " + "Sets that player's level.");
		}
		else if (args.length == 1 && args[0].equalsIgnoreCase("list"))
		{
			String result = "";
			
			for (Player p : player.getWorld().getPlayers())
			{
				if (result != "") result += NaxColor.MSG + ", ";
				result += plugin.getNickName(p) + " is level " + NaxColor.LEVEL + p.getLevel();
			}
			
			player.sendMessage(result);
			return;
		}
		else if (args.length == 1 && args[0].equalsIgnoreCase("reset"))
		{
			if (!plugin.playerManager.getPlayer(player).rank.isAdmin()) return;
			
			for (Player p : player.getWorld().getPlayers())
			{
				p.setLevel(0);
				p.setTotalExperience(0);
			}
			
			player.sendMessage(NaxColor.MSG + "Done.");
		}
		else if (args.length == 2 && args[0].equalsIgnoreCase("reset"))
		{
			if (!plugin.playerManager.getPlayer(player).rank.isAdmin()) return;
			
			Player p = plugin.getServer().getPlayer(args[1]);
			
			if (p != null)
			{
				p.setLevel(0);
				p.setTotalExperience(0);
				
				player.sendMessage(NaxColor.MSG + "Done.");
			}
			else
			{
				player.sendMessage(NaxColor.MSG + args[1] + " is not a player.");
			}
		}
		else if (args.length == 3 && args[0].equalsIgnoreCase("set"))
		{
			Player p = plugin.getServer().getPlayer(args[1]);
			int level = -1;
			
			if (p == null)
			{
				player.sendMessage(NaxColor.MSG + "The name " + args[1] + " is not a player.");
				return;
			}
			
			try
			{
				level = Integer.parseInt(args[2]);
			}
			catch (NumberFormatException e)
			{
				player.sendMessage(NaxColor.MSG + "/level set [name] [level]");
				return;
			}
			
			if (level < 0)
			{
				level = 0;
				player.sendMessage(NaxColor.MSG + "Level has to 0 or higher.");
			}
			
			p.setLevel(level);
			
			p.sendMessage(plugin.getNickName(player) + " has set your level to " + NaxColor.WHITE + level + NaxColor.MSG + ".");
			player.sendMessage(plugin.getNickName(p) + " is now level " + NaxColor.WHITE + level + NaxColor.MSG + ".");
			
		}
	}
}
