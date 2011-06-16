package com.naxville.naxcraft.player;

import org.bukkit.entity.Player;

import com.naxville.naxcraft.Naxcraft;
import com.naxville.naxcraft.player.PlayerManager.PlayerRank;

public class RankCommand 
{
	public Naxcraft plugin;
	
	public RankCommand(Naxcraft plugin)
	{
		this.plugin = plugin;
	}
	
	public boolean demote(NaxPlayer p, Player demoter)
	{
		NaxPlayer admin = plugin.playerManager.getPlayer(demoter);
		
		if(admin.rank.getId() < PlayerRank.MODERATOR.getId())
		{
			return false;
		}
		else
		{
			if(p.rank.getId() == PlayerRank.MODERATOR.getId())
			{
				if(admin.rank.getId() == PlayerRank.ADMIN.getId())
				{
					p.rank = PlayerRank.getRank(p.rank.getId() - 1);
					plugin.playerManager.savePlayer(p);
					return true;
				}
				else
				{
					return false;
				}
			}
			else if (p.rank.getId() == PlayerRank.ADMIN.getId())
			{
				if(admin.rank.getId() == PlayerRank.ADMIN.getId())
				{
					p.rank = PlayerRank.getRank(p.rank.getId() - 1);
					plugin.playerManager.savePlayer(p);
					return true;
				}
				else
				{
					return false;
				}
			}
			else
			{
				if(p.rank == PlayerRank.NOOB)
				{
					return false;
				}
				
				p.rank = PlayerRank.getRank(p.rank.getId() - 1);
				plugin.playerManager.savePlayer(p);
				return true;
			}
		}
	}
	
	public boolean promote(NaxPlayer p, Player promoter)
	{
		NaxPlayer admin = plugin.playerManager.getPlayer(promoter);
		
		if(admin.rank.getId() < PlayerRank.MODERATOR.getId())
		{
			return false;
		}
		else
		{
			if(p.rank.getId() == PlayerRank.MODERATOR.getId())
			{
				if(admin.rank.getId() == PlayerRank.ADMIN.getId())
				{
					p.rank = PlayerRank.getRank(p.rank.getId() + 1);
					plugin.playerManager.savePlayer(p);
					return true;
				}
				else
				{
					return false;
				}
			}
			else if (p.rank.getId() == PlayerRank.ADMIN.getId())
			{
				return false;
			}
			else
			{
				p.rank = PlayerRank.getRank(p.rank.getId() + 1);
				plugin.playerManager.savePlayer(p);
				return true;
			}
		}
	}

	public boolean runCommand(Player player, boolean promote, String[] args) 
	{
		if(args.length == 0)
		{
			player.sendMessage(Naxcraft.MSG_COLOR + "/promote|demote <name>");
			return true;
		}
		
		NaxPlayer p = plugin.playerManager.getPlayer(args[0]);
		
		if(p == null)
		{
			player.sendMessage(Naxcraft.MSG_COLOR + "That player doesn't exist.");
		}
		else
		{
			if(promote)
			{
				if(promote(p, player))
				{
					plugin.announcer.announce(plugin.playerManager.getDisplayName(player) + " promoted " + plugin.playerManager.getDisplayName(p) + " to " + p.rank.getName() + "!");
				}
				else
				{
					player.sendMessage(Naxcraft.MSG_COLOR + "You cannot promote that player.");
				}
			}
			else
			{
				if(demote(p, player))
				{
					plugin.announcer.announce(plugin.playerManager.getDisplayName(player) + " demoted " + plugin.playerManager.getDisplayName(p) + " to " + p.rank.getName() + ".");
				}
				else
				{
					player.sendMessage(Naxcraft.MSG_COLOR + "You cannot demote that player.");
				}
			}
		}
		
		return true;
	}
	
}
