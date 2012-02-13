package com.naxville.naxcraft;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.naxville.naxcraft.player.NaxPlayer;
import com.naxville.naxcraft.player.PlayerManager.PlayerRank;

public abstract class NaxCommand implements CommandExecutor
{
	public Naxcraft plugin;
	public PlayerRank minimum;
	
	protected NaxCommand(Naxcraft plugin, PlayerRank minimum)
	{
		this.plugin = plugin;
		this.minimum = minimum;
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		if (sender instanceof Player)
		{
			NaxPlayer np = plugin.playerManager.getPlayer((Player) sender);
			
			if (np.rank.getId() < minimum.getId())
			{
				lowRank((Player) sender);
			}
			else
			{
				runCommand((Player) sender, args);
			}
		}
		return true;
	}
	
	public void lowRank(Player player)
	{
		player.sendMessage(NaxColor.MSG + "You must hold the rank of " + minimum.getPrefix() + minimum.getName() + " to use this command.");
	}
	
	public abstract void runCommand(Player player, String[] args);
	
	public void notAPlayer(Player player, String name)
	{
		player.sendMessage(name + NaxColor.MSG + " is not a player.");
	}
}
