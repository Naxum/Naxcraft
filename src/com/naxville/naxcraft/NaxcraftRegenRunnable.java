package com.naxville.naxcraft;


public class NaxcraftRegenRunnable implements Runnable
{
	public static Naxcraft plugin;
	
	public NaxcraftRegenRunnable(Naxcraft instance)
	{
		plugin = instance;
	}
	
	public void run()
	{
		/*
		for (Player player : plugin.getServer().getOnlinePlayers())
		{
			NaxPlayer p = plugin.playerManager.getPlayer(player);
			
			if (p == null || p.rank == PlayerRank.getRank(0))
			{
				continue;
			}
			
			int x = p.rank.getId() + 1;
			
			if (p.rank == PlayerRank.SUPER_PATRON)
			{
				x = PlayerRank.ADMIN.getId() + 1;
			}
			
			if (plugin.regenValues.containsKey(player.getName()))
			{
				if (plugin.regenValues.get(player.getName()) <= 1)
				{
					if (!player.getWorld().getName().startsWith("aether"))
					{
						if ((player.getHealth() < 20) && (player.getHealth() > 0))
						{
							player.setHealth(player.getHealth() + ((int) Math.ceil(1 * (x / 2))));
						}
						plugin.regenValues.put(player.getName(), (int) Math.ceil(10 / x));
					}
				}
				else
				{
					plugin.regenValues.put(player.getName(), (plugin.regenValues.get(player.getName()) - 1));
				}
			}
			else
			{
				plugin.regenValues.put(player.getName(), (int) Math.ceil(10 / x));
			}
		}*/

		plugin.stealthCommand.updateInvisibles();
	}
}
