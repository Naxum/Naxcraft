package com.naxville.naxcraft.admin;

import org.bukkit.entity.Player;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import com.naxville.naxcraft.Naxcraft;

public class NaxcraftTimeCommand {
	private static Naxcraft plugin;
	
	public NaxcraftTimeCommand(Naxcraft instance){
		plugin = instance;
	}
	
	public boolean runTimeCommand(CommandSender sender, String[] args){
		if(plugin.playerManager.getPlayer((Player)sender).rank.isDemiAdmin()){
			if(args.length != 1) return false;
			
			if(args.length == 1){
				if(args[0].equalsIgnoreCase("day")){
					
					if(sender instanceof Player){
						Player player = (Player) sender;
						plugin.announcer.announce(plugin.getNickName(player.getName()) + Naxcraft.MSG_COLOR + " has let there be light!", player.getWorld());
						player.getWorld().setTime(0);
					} else {
						plugin.getServer().broadcastMessage(Naxcraft.MSG_COLOR + "The almighty Super User has let there be light!");
						for(World w: (World[]) plugin.getServer().getWorlds().toArray()){
							w.setTime(0);
						}
					}
					
				} else if(args[0].equalsIgnoreCase("night")){
					
					if(sender instanceof Player){
						Player player = (Player) sender;
						plugin.announcer.announce(plugin.getNickName(player.getName()) + Naxcraft.MSG_COLOR + " has damned this world with darkness!", player.getWorld());
						player.getWorld().setTime(13000);
					} else {
						plugin.getServer().broadcastMessage(Naxcraft.MSG_COLOR + "The almighty Super User has damned the world with darkness!");
						for(World w: (World[]) plugin.getServer().getWorlds().toArray()){
							w.setTime(13000);
						}
					}
					
				}
				else if (args[0].equalsIgnoreCase("bloodmoon")) 
				{
					plugin.bloodMoonManager.runBloodMoonCommand((Player)sender, args);
				}
				else 
				{
					return false;
				}
			}
		} else {
			sender.sendMessage(String.format(Naxcraft.PERMISSIONS_FAIL, "/time"));
		}
			
		return true;
	}
}
