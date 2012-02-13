package com.naxville.naxcraft.admin;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.naxville.naxcraft.Naxcraft;
import com.naxville.naxcraft.player.PlayerManager.PlayerRank;

public class NaxcraftGiveCommand 
{
	private static Naxcraft plugin;
	
	List<String> armors = new ArrayList<String>();
	List<String> tools = new ArrayList<String>();
	List<String> toolMaterials = new ArrayList<String>();
	
	public NaxcraftGiveCommand(Naxcraft instance)
	{
		plugin = instance;
		
		armors.add("leather");
		armors.add("iron");
		armors.add("gold");
		armors.add("diamond");
		armors.add("chainmail");
		
		toolMaterials.add("wood");
		toolMaterials.add("stone");
		toolMaterials.add("iron");
		toolMaterials.add("diamond");
		toolMaterials.add("gold");
		
		tools.add("sword");
		tools.add("pickaxe");
		tools.add("axe");
		tools.add("spade");
		tools.add("hoe");
	}
	
	public boolean runGiveCommand(CommandSender sender, String[] args)
	{		
        if ((args.length > 3) || (args.length == 0)) 
        {
            return false;
        }
        else if ((sender instanceof Player) && plugin.playerManager.getPlayer((Player)sender).rank != PlayerRank.ADMIN) 
        {
            sender.sendMessage(String.format(Naxcraft.PERMISSIONS_FAIL, "/give"));
            return true;
        }
        
        if(args.length >= 2)
        {
	        if(args[1].equalsIgnoreCase("gear"))
	        {
	        	if(this.armors.contains(args[0].toLowerCase()))
	        	{
	        		Player player = (Player)sender;
	        		
	        		if(args.length == 3)
	        		{
	        			player = plugin.getServer().getPlayer(args[2].toLowerCase());
	        			if(player == null)
	        			{
	        				sender.sendMessage(Naxcraft.ERROR_COLOR + args[3] + " is not online, or just not a player.");
	        				return true;
	        			} 
	        			else 
	        			{
	        				player.sendMessage(Naxcraft.ADMIN_COLOR + "You have been bestowed " + args[0].toLowerCase() + " gear!");
	        			}
	        		}
	        		
	        		String base = args[0].toUpperCase();
	        		String chest = base +"_CHESTPLATE";
	        		String boots = base +"_BOOTS";
	        		String leggings = base +"_LEGGINGS";
	        		String helm = base +"_HELMET";
	        		
	        		player.getInventory().setChestplate(new ItemStack(Material.getMaterial(chest), 1));
	        		player.getInventory().setBoots(new ItemStack(Material.getMaterial(boots), 1));
	        		player.getInventory().setLeggings(new ItemStack(Material.getMaterial(leggings), 1));
	        		player.getInventory().setHelmet(new ItemStack(Material.getMaterial(helm), 1));
	        		
	        		sender.sendMessage(Naxcraft.SUCCESS_COLOR + player.getName() + "'s " + args[0] + Naxcraft.MSG_COLOR + " gear given!");
	        		return true;
	        	} 
	        	else 
	        	{
	        		sender.sendMessage(Naxcraft.ERROR_COLOR + args[1] + " is not a gear type.");
	        	}
	        	
	        	return true;
	        }
	        else if(args[1].equalsIgnoreCase("tools"))
	        {
	        	Player player = (Player)sender;
	        	
	        	if(args.length == 3)
	        	{
	        		player = plugin.getServer().getPlayer(args[2].toLowerCase());
	        		if(player == null)
	        		{
	        			sender.sendMessage(Naxcraft.ERROR_COLOR + args[2] + " either isn't online or isn't a player.");
	        			return true;
	        		}
	        	}
	        	
	        	if(!toolMaterials.contains(args[0].toLowerCase()))
	        	{
	        		player.sendMessage(Naxcraft.ERROR_COLOR + "Hey, stupid face, " + args[0] + " is not a tool material.");
	        		return true;
	        	}
	        	
	        	for(String tool : tools)
        		{
        			Material type = Material.getMaterial(args[0].toUpperCase() + "_" + tool.toUpperCase());
        			player.getInventory().addItem(new ItemStack(type, 1));
        		}
	        	
	        	if(player != (Player)sender)
	        	{
	        		player.sendMessage(plugin.getNickName((Player)sender) + " has sent you " + Naxcraft.SUCCESS_COLOR + args[0].toUpperCase() + Naxcraft.MSG_COLOR + " tools!");
	        		sender.sendMessage(Naxcraft.MSG_COLOR + "You have sent " + plugin.getNickName(player) + Naxcraft.SUCCESS_COLOR + args[0].toUpperCase() + Naxcraft.MSG_COLOR + " tools!");
	        	}
	        	else
	        	{
	        		player.sendMessage(Naxcraft.MSG_COLOR + "Here are your tools, my lord!");
	        	}
	        	
	        	return true;
	        }
        }
        
        Material material = null;
        int count = 1;
        String[] gData = null;
        Byte bytedata = null;
        
        if (args.length >= 1) 
        {
            gData = args[0].split(":");
            material = Material.matchMaterial(gData[0]);
            if (gData.length == 2) 
            {
                bytedata = Byte.valueOf(gData[1]);
            }
        }
        if (args.length >= 2) 
        {
            try 
            {
                count = Integer.parseInt(args[1]);
            }
            catch (NumberFormatException ex) 
            {
                sender.sendMessage(Naxcraft.ERROR_COLOR + "'" + args[1] + "' is not a number!");
                return false;
            }
        }
        
        Player target = null;
        
        if (args.length == 3) 
        {
            target = plugin.getServer().getPlayer(args[2]);
            if (target == null) 
            {
                sender.sendMessage(Naxcraft.ERROR_COLOR + "'" + args[2] + "' is not a valid player!");
                return false;
            }
        } 
        else 
        {
            if (!(sender instanceof Player)) 
            {
            	sender.sendMessage(Naxcraft.NOT_A_PLAYER);
                return false;
            } 
            else 
            {
                target = (Player)sender;
            }
        }
        
        if (material == null) 
        {
            sender.sendMessage(Naxcraft.ERROR_COLOR + "Unknown item");
            return false;
        }
        
        if (bytedata != null) 
        {
            target.getInventory().addItem(new ItemStack(material, count, (short) 0, bytedata));
        } 
        else 
        {
            target.getInventory().addItem(new ItemStack(material, count));
        }
        
        if(target.getName().equalsIgnoreCase(((Player)sender).getName()))
        {
        	sender.sendMessage(Naxcraft.SUCCESS_COLOR + "You gave yourself " + count + " " + material.toString().toLowerCase() + ".");
        } 
        else 
        {
        	sender.sendMessage(Naxcraft.SUCCESS_COLOR + "You gave " + plugin.getNickName(target.getName()) + Naxcraft.SUCCESS_COLOR + " " + count + " " + material.toString().toLowerCase() + ".");
        	target.sendMessage(plugin.getNickName(((Player)sender).getName().toLowerCase()) + Naxcraft.COMMAND_COLOR + " sent you " + count + " " + material.toString().toLowerCase() + ".");
        }
        
        return true;
	}
}
