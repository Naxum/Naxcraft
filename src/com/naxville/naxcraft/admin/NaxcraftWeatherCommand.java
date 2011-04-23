package com.naxville.naxcraft.admin;

import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.naxville.naxcraft.Naxcraft;

public class NaxcraftWeatherCommand {
	public Naxcraft plugin;
	
	public NaxcraftWeatherCommand(Naxcraft plugin){
		this.plugin = plugin;
	}
	
	public boolean runCommand(CommandSender sender, String[] args){
		if(!(sender instanceof Player)){
			sender.sendMessage("Knock it off");
			return true;
		}
		
		Player player = (Player)sender;
		
		if(args.length == 0){
			return false;
		}
		
		World target;
		String targetStr;
		
		if(args.length == 1){
			target = player.getWorld();
			targetStr = Naxcraft.WORLD_COLOR + plugin.getWorldName(player.getWorld());
			
		} else {
			if(args.length == 2){
				Player targetPlayer = player.getServer().getPlayer(args[1]);
				if(targetPlayer != null){
					
					target = targetPlayer.getWorld();
					targetStr = Naxcraft.WORLD_COLOR + plugin.getWorldName(targetPlayer.getWorld());
					
				} else {
					player.sendMessage(Naxcraft.MSG_COLOR + "No one with the name " + args[1] + " is online.");
					return true;
				}
				
			} else return false;
		}
		
		String weather = "";
		
		if(args[0].equalsIgnoreCase("thunder")){
			target.setStorm(true);
			target.setThundering(true);
			weather = "The rage of %s has engulfed " + targetStr + Naxcraft.MSG_COLOR + "!";
			
		} else if (args[0].equalsIgnoreCase("storm")){
			target.setStorm(true);
			target.setThundering(false);
			weather = "%s has started a storm in " + targetStr + Naxcraft.MSG_COLOR + "!";
			
		} else if (args[0].equalsIgnoreCase("clear")){
			target.setStorm(false);
			target.setThundering(false);
			weather = "%s has cleared the sky of " + targetStr + Naxcraft.MSG_COLOR + "!";
			
		} else {
			return false;
		}
		
		player.getServer().broadcastMessage(String.format(Naxcraft.MSG_COLOR + weather, plugin.getNickName(player.getName()) + Naxcraft.MSG_COLOR));
		
		return true;
	}
}
