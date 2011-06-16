package com.naxville.naxcraft.admin;

import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;

import com.naxville.naxcraft.Naxcraft;

public class NaxcraftKillCommand {
	public static Naxcraft plugin;
	
	public NaxcraftKillCommand(Naxcraft instance){
		plugin = instance;
	}
	
	public boolean runKillCommand(CommandSender sender, String[] args){
		if(plugin.playerManager.getPlayer((Player)sender).rank.isAdmin()){
			if(args.length > 0){
				Player target = plugin.getServer().getPlayer(args[0]);
				if(target != null){
					target.sendMessage(Naxcraft.COMMAND_COLOR + "The gods have marked you for death.");
					sender.sendMessage(args[0] + Naxcraft.COMMAND_COLOR + "'s life has been snuffed out by a keystroke.");
					target.setHealth(0);
				} else {
					sender.sendMessage(args[0] + Naxcraft.ERROR_COLOR + " is not on the server.");
				}
			} else if(sender instanceof Player) {
				Player player = (Player) sender;
				player.sendMessage(Naxcraft.COMMAND_COLOR + "A worthy sacrifice!");
				player.setHealth(0);
			} else {
				sender.sendMessage(Naxcraft.NOT_A_PLAYER);
			}
		} else {
			sender.sendMessage(String.format(Naxcraft.PERMISSIONS_FAIL, "/kill"));
		}
		
		return true;
	}
}
