package com.naxville.bukkit.permissions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

public class PermissionsControl
{
  private String filePath = "plugins/Naxcraft/";

  public Map<String, NaxPlayer> Players = new HashMap<String, NaxPlayer>();
  public Map<String, PermissionGroup> Groups = new HashMap<String, PermissionGroup>();
  public PermissionGroup defaultGroup = null;
  private Map<String, Boolean> PlayerCache = new HashMap<String, Boolean>();
  private Configuration config;

  public PermissionsControl(Configuration config)
  {
    this.config = config;
  }

  public PermissionsControl()
  {
    loadFile();
    load();
  }

  private void loadFile() {
    File file = new File(this.filePath);
    try {
      if (!file.exists()) {
        file.mkdir();
      }
      file = new File(this.filePath + "permissions.yml");
      if (!file.exists()) {
        file.createNewFile();
        initalizeFile();
      }
    } catch (IOException Ex) {
      System.out.println("Shit, problem creating new permissions file");
      return;
    }

    this.config = new Configuration(file);
    this.config.load();
  }

  private void initalizeFile() {
    File file = new File(this.filePath + "permissions.yml");
    try {
      BufferedWriter output = new BufferedWriter(new FileWriter(file));
      output.write("world:");
      output.close();
    } catch (Exception Ex) {
      System.out.println("Error initalizing permissions file.  Permissions cant save :(");
    }
  }

  public boolean has(Player player, String permission) {
    return has(player.getName(), permission);
  }

  public boolean has(String player, String permission) {
    String name = player.toLowerCase();
    String perm = permission.toLowerCase();

    if (this.PlayerCache.containsKey(name + ":" + perm)) {
      return ((Boolean)this.PlayerCache.get(name + ":" + perm)).booleanValue();
    }

    if (this.Players.containsKey(name)) {
      return ((NaxPlayer)this.Players.get(name)).has(perm);
    } else {
    	return defaultGroup.has(perm);
    }
  }

  public boolean groupHas(String group, String permission) {
    if (this.Groups.containsKey(group.toLowerCase())) {
      return ((PermissionGroup)this.Groups.get(group.toLowerCase())).has(permission.toLowerCase()).booleanValue();
    }
    return false;
  }

  private void clearPlayerCache(String name) {
    Set<String> index = new HashSet<String>();
    String entry;
    for (Iterator<String> it = this.PlayerCache.keySet().iterator(); it.hasNext(); ) {
      entry = it.next();
      if (entry.contains(name)) {
        index.add(entry);
      }
    }
    for (String e : index)
      this.PlayerCache.remove(e);
  }

  protected boolean promote(Player player)
  {
    return promote(player.getName());
  }

  protected boolean promote(String player)
  {
    NaxPlayer name;
    if (this.Players.containsKey(player.toLowerCase())) {
      name = (NaxPlayer)this.Players.get(player.toLowerCase());
    } else {
      name = new NaxPlayer();
      this.Players.put(player.toLowerCase(), name);
      name.setGroup(this.defaultGroup);
    }
    if (name.getGroup() != null) {
      name.setGroup(getGroupParent(name.getGroup()));
      clearPlayerCache(player.toLowerCase());
      save();
      return true;
    }
    return false;
  }

  protected boolean demote(Player player) {
    return demote(player.getName());
  }

  protected boolean demote(String player)
  {
    NaxPlayer name;
    if (this.Players.containsKey(player.toLowerCase())) {
      name = (NaxPlayer)this.Players.get(player.toLowerCase());
    } else {
      name = new NaxPlayer();
      this.Players.put(player.toLowerCase(), name);
      name.setGroup(this.defaultGroup);
    }
    if (name.getGroup() != null) {
      PermissionGroup group = name.getGroup();
      if (group.getInheritance() != null) {
        name.setGroup(group.getInheritance());
        clearPlayerCache(player.toLowerCase());
        save();
        return true;
      }
    }
    return false;
  }

  protected void addPlayerPermission(Player player, String permission) {
    addPlayerPermission(player.getName(), permission);
  }

  protected void addPlayerPermission(String player, String permission)
  {
    NaxPlayer name;
    if (this.Players.containsKey(player.toLowerCase())) {
      name = (NaxPlayer)this.Players.get(player.toLowerCase());
    } else {
      name = new NaxPlayer();
      this.Players.put(player.toLowerCase(), name);
      name.setGroup(this.defaultGroup);
    }
    name.addPermission(permission.toLowerCase());
    this.PlayerCache.put(player.toLowerCase() + ":" + permission.toLowerCase(), Boolean.valueOf(true));
    save();
  }

  protected void removePlayerPermission(Player player, String permission) {
    removePlayerPermission(player.getName(), permission);
  }

  protected void removePlayerPermission(String player, String permission)
  {
    NaxPlayer name;
    if (this.Players.containsKey(player.toLowerCase())) {
      name = (NaxPlayer)this.Players.get(player.toLowerCase());
    } else {
      name = new NaxPlayer();
      this.Players.put(player.toLowerCase(), name);
      name.setGroup(this.defaultGroup);
    }
    name.removePermission(permission.toLowerCase());
    this.PlayerCache.put(player.toLowerCase() + ":" + permission.toLowerCase(), Boolean.valueOf(false));
    save();
  }

  protected void addGroupPermission(String group, String permission) {
    String gname = group.toLowerCase();

    if (this.Groups.containsKey(gname)) {
      PermissionGroup g = (PermissionGroup)this.Groups.get(gname);
      g.addPermission(permission.toLowerCase());
      clearPlayerCache(permission.toLowerCase());
      save();
    }
  }

  protected Boolean removeGroupPermission(String group, String permission) {
    String gname = group.toLowerCase();

    if (this.Groups.containsKey(gname)) {
      PermissionGroup g = (PermissionGroup)this.Groups.get(gname);
      if (g.removePermission(permission.toLowerCase()).booleanValue()) {
        clearPlayerCache(permission.toLowerCase());
        save();
        return Boolean.valueOf(true);
      }
    }
    return Boolean.valueOf(false);
  }

  private PermissionGroup getGroupParent(PermissionGroup group) {
    for (PermissionGroup g : this.Groups.values()) {
      if ((g.getInheritance() != null) && 
        (g.getInheritance().equals(group))) {
        return g;
      }
    }

    return null;
  }

  public PermissionGroup getGroup(Player player) {
    return getGroup(player.getName());
  }

  public PermissionGroup getGroup(String player) {
    String name = player.toLowerCase();
    NaxPlayer pname = (NaxPlayer)this.Players.get(name);
    if ((pname != null) && 
      (pname.getGroup() != null)) {
      return pname.getGroup();
    }

    return null;
  }
  
  public PermissionGroup getGroup(String player, Boolean dick) {
	    String name = player.toLowerCase();
	    NaxPlayer pname = (NaxPlayer)this.Players.get(name);
	    if ((pname != null) && 
	      (pname.getGroup() != null)) {
	      return pname.getGroup();
	    }
	    return defaultGroup;
	  }

  public NaxPlayer getPlayer(Player player) {
    return getPlayer(player.getName());
  }

  public NaxPlayer getPlayer(String player) {
    String name = player.toLowerCase();
    if (this.Players.containsKey(name)) {
      return (NaxPlayer)this.Players.get(name);
    }
    return null;
  }

  private void load() {
    List<String> groups = this.config.getKeys("world.groups");

    Map<String, String> tempInheritance = new HashMap<String, String>();
    String name;
    if (groups != null)
    {
      String groupInheritance = "";
      String def = "";
      String admin = "";
      name = "";

      for (String group : groups) {
        List<String> groupPermissions = this.config.getStringList("world.groups." + group + ".permissions", null);
        groupInheritance = this.config.getString("world.groups." + group + ".inheritance", "");
        def = this.config.getString("world.groups." + group + ".default");
        admin = this.config.getString("world.groups." + group + ".admin");
        name = this.config.getString("world.groups." + group + ".name", "");

        PermissionGroup pGroup = new PermissionGroup();
        if (name.equals(""))
          pGroup.setName(group);
        else {
          pGroup.setName(name);
        }
        if (groupPermissions != null) {
          pGroup.setPermissions(new HashSet<String>(groupPermissions));
        }
        if (def.equals("true")) {
          pGroup.setDefault(true);
          this.defaultGroup = pGroup;
        }
        if (admin.equals("true")) {
          pGroup.setAdmin(true);
        }
        if (!groupInheritance.equals("")) {
          tempInheritance.put(group.toLowerCase(), groupInheritance.toLowerCase());
        }
        this.Groups.put(group.toLowerCase(), pGroup);
      }

      for (String group : tempInheritance.keySet()) {
        if ((this.Groups.containsKey(group)) && (this.Groups.containsKey(tempInheritance.get(group)))) {
          ((PermissionGroup)this.Groups.get(group)).setInheritance((PermissionGroup)this.Groups.get(tempInheritance.get(group)));
        }
      }
    }
    List<String> players = this.config.getKeys("world.players");

    String PlayerGroup = "";
    if (players != null)
      for (String player : players) {
        List<String> playerPermissions = this.config.getStringList("world.players." + player + ".permissions", null);
        PlayerGroup = this.config.getString("world.players." + player + ".group", "");

        NaxPlayer np = new NaxPlayer();
        if (playerPermissions != null) {
          np.setPermissions(new HashSet<String>(playerPermissions));
        }
        if (PlayerGroup != "") {
          if (this.Groups.containsKey(PlayerGroup.toLowerCase()))
            np.setGroup((PermissionGroup)this.Groups.get(PlayerGroup.toLowerCase()));
          else
            np.setGroup(this.defaultGroup);
        }
        else {
          np.setGroup(this.defaultGroup);
        }
        this.Players.put(player.toLowerCase(), np);
      }
  }

  protected void save()
  {
    savePlayers();
    saveGroups();
    this.config.save();
  }

  protected void reload() {
    this.Players = new HashMap<String, NaxPlayer>();
    this.Groups = new HashMap<String, PermissionGroup>();

    this.defaultGroup = null;

    load();
  }

  protected void savePlayers() {
    for (String player : this.Players.keySet())
      this.config.setProperty("world.players." + player, ((NaxPlayer)this.Players.get(player)).save());
  }

  protected void saveGroups()
  {
    for (String group : this.Groups.keySet())
      this.config.setProperty("world.groups." + group, ((PermissionGroup)this.Groups.get(group)).save());
  }

}