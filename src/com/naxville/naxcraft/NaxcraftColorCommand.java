package com.naxville.naxcraft;

import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;
import java.lang.Integer;

public class NaxcraftColorCommand {
	public static Naxcraft plugin;
	
	public NaxcraftColorCommand(Naxcraft instance){
		plugin = instance;
	}
	
	public boolean runColorCommand(CommandSender sender, String[] args){
		
		if (!(sender instanceof Player)) {
			sender.sendMessage(Naxcraft.NOT_A_PLAYER);
			return true;
		}
		
		Player player = (Player)sender;
		if (!plugin.control.has(player, "color")) {
			sender.sendMessage(String.format(Naxcraft.PERMISSIONS_FAIL, "/color"));
			return true;
		}
		
		if(args.length > 0){
			int color = 13;
			try {
				color = Integer.parseInt(args[0]);
			}
			catch (Exception e) {
				return false;
			}
		
			if(((color > 0)||((plugin.control.has(player, "admincolor"))&&(color==0)))&&((color<15)||((plugin.control.has(player, "admincolor"))&&(color<16)))) {
				player.setDisplayName(Naxcraft.COLORS[color] + player.getName() + Naxcraft.DEFAULT_COLOR);
				plugin.nickNames.put(player.getName().toLowerCase(), Naxcraft.COLORS[color] + player.getName() + Naxcraft.DEFAULT_COLOR);
				player.sendMessage((Naxcraft.COMMAND_COLOR + "Your display color is now " + Naxcraft.COLORS[color] + color));
				plugin.saveNickNames();
			}
			else {
				player.sendMessage((Naxcraft.COMMAND_COLOR + "Invalid color index: " + args[0]));
			}
			return true;
		}
		String colorCodes = Naxcraft.COMMAND_COLOR + "Color codes: ";
		for(int i=1; i<15; i++)
		{
			colorCodes += " " + Naxcraft.COLORS[i];
			colorCodes += i;
		}
		player.sendMessage(colorCodes);
		return true;
	}
}
