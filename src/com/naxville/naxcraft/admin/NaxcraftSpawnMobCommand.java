package com.naxville.naxcraft.admin;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;

import com.naxville.naxcraft.Naxcraft;

public class NaxcraftSpawnMobCommand {
	public Naxcraft plugin;
	
	public NaxcraftSpawnMobCommand(Naxcraft instance){
		plugin = instance;
	}
	
	public boolean runSpawnMobCommand(CommandSender sender, String[] args){
		if(!(sender instanceof Player)){
			sender.sendMessage(Naxcraft.NOT_A_PLAYER);
			return true;
		}
		
		Player player = (Player)sender;
		
		if(plugin.playerManager.getPlayer(player).rank.isAdmin()){
			
			if(args.length == 0) return false;
			
			int amount = 0;
			
			if(args.length == 1) {
				amount = 1;
			}
			
			if(args.length ==2){
				try {
					amount = Integer.parseInt(args[1]);
				} catch(NumberFormatException e){
					player.sendMessage(Naxcraft.ERROR_COLOR + args[1] + " is not a number.");
					return true;
				}
				
				if(amount > 15){
					player.sendMessage(Naxcraft.ERROR_COLOR + "That number was crazy high, defaulting to 15.");
					amount = 15;
				}
			}
			
			Location location = player.getTargetBlock(null, 120).getLocation();
			location.setY(location.getY()+1);
			
			String cap = args[0].substring(0, 1).toUpperCase();
			String rest = args[0].substring(1);
			
			String name = cap + rest;
			name = name.replace("zombie", "Zombie"); //PigZombie
			
			if(name.equalsIgnoreCase("angrywolf")){
				
				CreatureType creature = CreatureType.WOLF;
				
				for(int i = 0; i < amount; i++){
					LivingEntity le = player.getLocation().getWorld().spawnCreature(location, creature);
					Wolf wolf = (Wolf)le;
					wolf.setAngry(true);
				}
				
			} else if (name.equalsIgnoreCase("jockey") || name.equalsIgnoreCase("spiderjockey")){
				for(int i = 0; i < amount; i++){
					LivingEntity le = player.getLocation().getWorld().spawnCreature(location, CreatureType.SPIDER);
					LivingEntity le2 = player.getLocation().getWorld().spawnCreature(le.getLocation(), CreatureType.SKELETON);
					
					le.setPassenger(le2);
				}
				
			} else if (name.contains(":")) {				
				String cap1 = name.split(":")[0].substring(0, 1).toUpperCase();
				String rest1 = name.split(":")[0].substring(1);
				
				String name1 = cap1 + rest1;
				
				String cap2 = name.split(":")[1].substring(0, 1).toUpperCase();
				String rest2 = name.split(":")[1].substring(1);
				
				String name2 = cap2 + rest2;
				
				CreatureType creature1 = CreatureType.fromName(name1);
				CreatureType creature2 = CreatureType.fromName(name2);
				
				if(creature1 == null || creature2 == null){
					player.sendMessage(Naxcraft.ERROR_COLOR + name + " is not a creature or " + creature2.getName() + " is not a creature!");
					return true;
				}
				
				for(int i = 0; i < amount; i++){
					LivingEntity le = player.getLocation().getWorld().spawnCreature(location, CreatureType.fromName(name1));
					LivingEntity le2 = player.getLocation().getWorld().spawnCreature(le.getLocation(), CreatureType.fromName(name2));
					
					le.setPassenger(le2);
				}
				
			} else {
				CreatureType creature = CreatureType.fromName(name);
				if(creature == null){
					player.sendMessage(Naxcraft.ERROR_COLOR + name + " is not a creature!");
					return true;
				}
				
				for(int i = 0; i < amount; i++){
					player.getLocation().getWorld().spawnCreature(location, creature);
				}
			}
			
			
		} else {
			player.sendMessage(String.format(Naxcraft.PERMISSIONS_FAIL, "/spawn"));
			return true;
		}
		
		return true;
	}
}
