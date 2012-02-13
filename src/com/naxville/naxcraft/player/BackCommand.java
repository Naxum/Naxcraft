package com.naxville.naxcraft.player;

import org.bukkit.entity.Player;

import com.naxville.naxcraft.Naxcraft;
import com.naxville.naxcraft.player.PlayerManager.PlayerRank;

public class BackCommand
{
	public PlayerManager pm;
	
	public BackCommand(PlayerManager pm)
	{
		this.pm = pm;
	}
	
	public boolean runCommand(Player player, String[] args)
	{
		NaxPlayer p = pm.getPlayer(player);
		if(p.rank.getId() < PlayerRank.VETERAN.getId())
		{
			player.sendMessage(Naxcraft.MSG_COLOR + "You can't use the /back command unless you're Veteran+!");
			return true;
		}
		
		Player target = player;
		
		if(args.length > 0)
		{
			if(!p.rank.isAdmin())
			{
				player.sendMessage(Naxcraft.MSG_COLOR + "You must use just the /back command, you may not teleport anyone else back to where they were.");
				return true;
			}
			else
			{
				target = pm.plugin.getServer().getPlayer(args[0]);
				
				if(target == null)
				{
					player.sendMessage(Naxcraft.ERROR_COLOR + "There is no one online with the name " + args[0] + ".");
					return true;
				}
			}
		}
		
		if(pm.backs.containsKey(target))
		{
			target.teleport(pm.backs.get(target));
			pm.backs.remove(target);
			
			if(target != player)
			{
				player.sendMessage(pm.getDisplayName(target) + " was sent back successfully!");
				target.sendMessage(pm.getDisplayName(player) + " send you back to before you teleported!");
			}
			else
			{
				player.sendMessage(Naxcraft.MSG_COLOR + "Whoosh!");
			}
		}
		else
		{
			player.sendMessage(Naxcraft.MSG_COLOR + "You can't /back a /back!");
		}
		
		return true;
	}
}
