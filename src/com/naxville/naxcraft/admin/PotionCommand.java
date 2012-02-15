package com.naxville.naxcraft.admin;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import com.naxville.naxcraft.NaxColor;
import com.naxville.naxcraft.NaxCommand;
import com.naxville.naxcraft.Naxcraft;
import com.naxville.naxcraft.player.PlayerManager.PlayerRank;

public class PotionCommand extends NaxCommand
{
	public PotionCommand(Naxcraft plugin)
	{
		super(plugin, PlayerRank.MODERATOR);
	}

	public void runCommand(Player player, String[] args)
	{
		if(args.length == 1 && args[0].equalsIgnoreCase("list"))
		{
			String str = "";
			
			for(PotionEffectType type : PotionEffectType.values())
			{
				if(type == null || type.getName() == null) continue;
				
				if(str != "") str += NaxColor.MSG + ", ";
				str += NaxColor.WHITE + type.getName();
			}
			
			player.sendMessage(str);
		}
		else if (args.length >= 2 && args[0].equalsIgnoreCase("effect"))
		{
			PotionEffectType type = null;
			Player target = player;
			
			int amount = 0;
			int duration = 0;
			
			int i = 0;
			
			if(args.length >= 3)
			{
				Player p = plugin.getServer().getPlayer(args[1]);
				
				if(p != null)
				{
					target = p;
					i = 1;
				}
			}
			
			for(PotionEffectType t : PotionEffectType.values())
			{
				if(t == null || t.getName() == null) continue;
				
				if(t.getName().toLowerCase().startsWith(args[1+i].toLowerCase()))
				{
					type = t;
				}
			}
			
			if(type == null)
			{
				player.sendMessage(NaxColor.MSG + args[1+i] + " is not a potion type.");
				return;
			}
			
			if(args.length >= 3+i)
			{
				int mod = 1;
				
				try
				{
					mod = Integer.parseInt(args[2+i]);
				}
				catch(NumberFormatException e) {}
				
				amount = mod;
			}
			
			if(args.length >= 4+i)
			{
				int mod = 1000;
				
				try
				{
					mod = Integer.parseInt(args[3+i]);
				}
				catch(NumberFormatException e) {}
				
				duration = mod;
			}
			
			if(target != player)
			{
				player.sendMessage(plugin.getNickName(target) + " has received their " + NaxColor.WHITE + type.getName() + NaxColor.MSG + " potion effect!");
				target.sendMessage(plugin.getNickName(player) + " has given you a " + NaxColor.WHITE + type.getName() + NaxColor.MSG + " potion effect!");
			}
			else
			{
				player.sendMessage("You give yourself a " + NaxColor.WHITE + type.getName() + NaxColor.MSG + " potion effect!");
			}
			
			target.addPotionEffect(type.createEffect(duration, amount));
		}
		else
		{
			printHelp(player);
		}
		
		//player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 3000, 30));
		//player.sendMessage(NaxColor.MSG + "Bing bomb BOOP!");
	}
	
	private void printHelp(Player player)
	{
		player.sendMessage(NaxColor.MSG + "----");
		player.sendMessage(NaxColor.COMMAND + "/potion command:");
		player.sendMessage(NaxColor.MSG + "/potion " + NaxColor.WHITE + "list" + NaxColor.MSG + ": " + "Lists all potion types");
		player.sendMessage(NaxColor.MSG + "/potion " + NaxColor.WHITE + "effect <type> [amount] [duration]" + NaxColor.MSG + ": " + "Gives you the desired effect.");
		player.sendMessage(NaxColor.MSG + "/potion " + NaxColor.WHITE + "effect <player> <type> [amount] [duration]" + NaxColor.MSG + ": " + "Gives the player the desired effect.");
	}
}
