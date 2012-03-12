package com.naxville.naxcraft.admin;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
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
				
				if(amount > 30){
					player.sendMessage(Naxcraft.ERROR_COLOR + "That number was crazy high, defaulting to 30.");
					amount = 30;
				}
			}
			
			Location location = player.getTargetBlock(null, 120).getLocation();
			location.setY(location.getY()+1);
			
			String cap = args[0].substring(0, 1).toUpperCase();
			String rest = args[0].substring(1);
			
			String name = cap + rest;
			name = name.replace("zombie", "Zombie"); //PigZombie
			
			if(name.equalsIgnoreCase("angrywolf")){
				
				EntityType creature = EntityType.WOLF;
				
				for(int i = 0; i < amount; i++){
					LivingEntity le = player.getLocation().getWorld().spawnCreature(location, creature);
					Wolf wolf = (Wolf)le;
					wolf.setAngry(true);
				}
				
			} else if (name.equalsIgnoreCase("jockey") || name.equalsIgnoreCase("spiderjockey")){
				for(int i = 0; i < amount; i++){
					LivingEntity le = player.getLocation().getWorld().spawnCreature(location, EntityType.SPIDER);
					LivingEntity le2 = player.getLocation().getWorld().spawnCreature(le.getLocation(), EntityType.SKELETON);
					
					le.setPassenger(le2);
				}
				
			} else if (name.contains(":")) {				
				String cap1 = name.split(":")[0].substring(0, 1).toUpperCase();
				String rest1 = name.split(":")[0].substring(1);
				
				String name1 = cap1 + rest1;
				
				String cap2 = name.split(":")[1].substring(0, 1).toUpperCase();
				String rest2 = name.split(":")[1].substring(1);
				
				String name2 = cap2 + rest2;
				
				if(EntityType.fromName(name1) == null || EntityType.fromName(name2) == null){
					player.sendMessage(Naxcraft.ERROR_COLOR + name1 + " is not a creature or " + name2 + " is not a creature!");
					return true;
				}
				
				for(int i = 0; i < amount; i++){
					//LivingEntity le = player.getLocation().getWorld().spawnCreature(location, CreatureType.fromName(name1));
					LivingEntity le = player.getLocation().getWorld().spawnCreature(location, EntityType.fromName(name1));
					//LivingEntity le2 = player.getLocation().getWorld().spawnCreature(le.getLocation(), CreatureType.fromName(name2));
					LivingEntity le2 = player.getLocation().getWorld().spawnCreature(location, EntityType.fromName(name2));
					
					le.setPassenger(le2);
				}
				
			} else {
				//CreatureType creature = CreatureType.fromName(name);
				EntityType type = EntityType.fromName(name);
				if(type == null){
					player.sendMessage(Naxcraft.ERROR_COLOR + name + " is not a creature!");
					return true;
				}
				
				for(int i = 0; i < amount; i++){
					//player.getLocation().getWorld().spawnCreature(location, creature);
					player.getLocation().getWorld().spawnCreature(location, type);
				}
			}
			
			
		} else {
			player.sendMessage(String.format(Naxcraft.PERMISSIONS_FAIL, "/spawn"));
			return true;
		}
		
		return true;
	}
}
