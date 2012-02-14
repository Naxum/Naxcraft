package com.naxville.naxcraft.admin;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.naxville.naxcraft.NaxCommand;
import com.naxville.naxcraft.Naxcraft;
import com.naxville.naxcraft.player.PlayerManager.PlayerRank;

public class PotionCommand extends NaxCommand
{
	protected PotionCommand(Naxcraft plugin, PlayerRank minimum)
	{
		super(plugin, minimum);
	}

	public void runCommand(Player player, String[] args)
	{
		ItemStack i = new ItemStack(Material.POTION, 1);
	}
}
