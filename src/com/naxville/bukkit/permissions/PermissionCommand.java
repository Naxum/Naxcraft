package com.naxville.bukkit.permissions;

import com.naxville.naxcraft.Naxcraft;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PermissionCommand
{
  private Naxcraft plugin;
  private PermissionsControl control;

  public PermissionCommand(Naxcraft instance, PermissionsControl pControl)
  {
    this.plugin = instance;
    this.control = pControl;
  }

  public boolean runPermissionCommand(CommandSender sender, String commandLabel, String[] args)
  {
	  if (sender instanceof Player) {
		  if (!control.has((Player)sender, commandLabel)) {
			  sender.sendMessage(String.format(Naxcraft.PERMISSIONS_FAIL, commandLabel));
			  return true;
		  }
	  }
	  
    if (commandLabel.equalsIgnoreCase("savepermissions")) {
      return save(sender);
    }
    if (commandLabel.equalsIgnoreCase("promote")) {
      return promote(sender, args);
    }
    if (commandLabel.equalsIgnoreCase("demote")) {
      return demote(sender, args);
    }
    if (commandLabel.equalsIgnoreCase("bless")) {
      return bless(sender, args);
    }
    if (commandLabel.equalsIgnoreCase("grant")) {
      return grant(sender, args);
    }
    return false;
  }

  private boolean save(CommandSender sender) {
    this.control.save();
    sender.sendMessage(Naxcraft.SUCCESS_COLOR + "Permissions saved");
    return true;
  }

  private boolean promote(CommandSender sender, String[] args) {
    if (args.length < 1) {
      sender.sendMessage(Naxcraft.ERROR_COLOR + "You must specify a player to promote");
      return false;
    }
    Player player = this.plugin.getServer().getPlayer(args[0]);
    if (player != null) {
      if (this.control.promote(player))
        this.plugin.getServer().broadcastMessage(plugin.getNickName(player.getName().toLowerCase()) + Naxcraft.MSG_COLOR + " has been promoted to " + Naxcraft.DEFAULT_COLOR + this.control.getGroup(player).getName() + Naxcraft.MSG_COLOR + "!");
      else {
        sender.sendMessage(Naxcraft.ERROR_COLOR + "Unable to promote " + player.getDisplayName());
      }
      return true;
    }
    if (this.control.promote(args[0]))
      sender.sendMessage(args[0] + Naxcraft.MSG_COLOR + " has been promoted to " + Naxcraft.DEFAULT_COLOR + this.control.getGroup(args[0]).getName());
    else {
      sender.sendMessage(Naxcraft.ERROR_COLOR + "Unable to promote " + Naxcraft.DEFAULT_COLOR + args[0]);
    }
    return true;
  }

  private boolean demote(CommandSender sender, String[] args)
  {
    if (args.length < 1) {
      sender.sendMessage(Naxcraft.ERROR_COLOR + "You must specify player to promote");
      return false;
    }
    Player player = this.plugin.getServer().getPlayer(args[0]);
    if (player != null) {
      if (this.control.demote(player))
        this.plugin.getServer().broadcastMessage(player.getDisplayName() + Naxcraft.MSG_COLOR + " has been demoted to " + Naxcraft.DEFAULT_COLOR + this.control.getGroup(player).getName() + Naxcraft.MSG_COLOR+".");
      else {
        sender.sendMessage(Naxcraft.ERROR_COLOR + "Unable to demote " + player.getDisplayName());
      }
      return true;
    }
    if (this.control.demote(args[0]))
      sender.sendMessage(args[0] + Naxcraft.MSG_COLOR + " has been demoted to " + Naxcraft.DEFAULT_COLOR + this.control.getGroup(args[0]).getName());
    else {
      sender.sendMessage(Naxcraft.ERROR_COLOR + "Unable to demote " + Naxcraft.DEFAULT_COLOR + args[0]);
    }
    return true;
  }

  private boolean bless(CommandSender sender, String[] args)
  {
    if (args.length < 3) {
      sender.sendMessage(Naxcraft.ERROR_COLOR + "You must specify add/remove, player and command");
      return false;
    }
    String side = args[0].substring(0, 1).toLowerCase();
    Player player = this.plugin.getServer().getPlayer(args[1]);
    if (player != null) {
      if (side.equalsIgnoreCase("a")) {
        this.control.addPlayerPermission(player, args[2].toLowerCase());
        sender.sendMessage(player.getDisplayName() + Naxcraft.SUCCESS_COLOR + " has been blessed with the power of " + Naxcraft.DEFAULT_COLOR + args[2]);
        player.sendMessage(Naxcraft.ADMIN_COLOR + "You have been blessed with the power of " + Naxcraft.DEFAULT_COLOR + args[2]);
      } else {
        this.control.removePlayerPermission(player, args[2].toLowerCase());
        sender.sendMessage(player.getDisplayName() + Naxcraft.SUCCESS_COLOR + " has lost the power of " + Naxcraft.DEFAULT_COLOR + args[2]);
        player.sendMessage(Naxcraft.ADMIN_COLOR + "You have lost the power of " + Naxcraft.DEFAULT_COLOR + args[2]);
      }
      return true;
    }
    if (side.equalsIgnoreCase("a")) {
      this.control.addPlayerPermission(args[0], args[2].toLowerCase());
      sender.sendMessage(args[0] + Naxcraft.SUCCESS_COLOR + " has been blessed with the power of " + Naxcraft.DEFAULT_COLOR + args[2]);
    } else {
      this.control.removePlayerPermission(args[0], args[2].toLowerCase());
      sender.sendMessage(args[0] + Naxcraft.SUCCESS_COLOR + " has lost the power of " + Naxcraft.DEFAULT_COLOR + args[2]);
    }
    return true;
  }

  private boolean grant(CommandSender sender, String[] args)
  {
    if (args.length < 3) {
      sender.sendMessage(Naxcraft.ERROR_COLOR + "You must specify add/remove, group and command");
      return false;
    }
    String side = args[0].substring(0, 1).toLowerCase();
    String group = args[1].toLowerCase();

    if (side.equalsIgnoreCase("a")) {
      this.control.addGroupPermission(group, args[2]);
      sender.sendMessage(Naxcraft.SUCCESS_COLOR + "Group " + Naxcraft.DEFAULT_COLOR + group + Naxcraft.SUCCESS_COLOR + " has been granted the power of " + args[2]);
    } else {
      this.control.removeGroupPermission(group, args[2]);
      sender.sendMessage(Naxcraft.SUCCESS_COLOR + "Group " + Naxcraft.DEFAULT_COLOR + group + Naxcraft.SUCCESS_COLOR + " has lost the power of " + args[2]);
    }
    return true;
  }
}