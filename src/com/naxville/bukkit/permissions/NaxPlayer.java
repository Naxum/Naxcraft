package com.naxville.bukkit.permissions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NaxPlayer
{
  private Set<String> permissions = new HashSet<String>();
  private PermissionGroup group;

  public PermissionGroup getGroup()
  {
    return this.group;
  }

  public void setGroup(PermissionGroup group) {
    this.group = group;
  }

  public Set<String> getPermissions() {
    return this.permissions;
  }

  public boolean has(String permission) {
    if ((this.permissions.contains("*")) || (this.permissions.contains(permission.toLowerCase()))) {
      return true;
    }
    if (this.group != null) {
      return this.group.has(permission).booleanValue();
    }
    return false;
  }

  public void addPermission(String permission) {
    this.permissions.add(permission.toLowerCase());
  }

  public void removePermission(String permission) {
    if (this.permissions.contains(permission.toLowerCase()))
      this.permissions.remove(permission.toLowerCase());
  }

  public void setPermissions(Set<String> permissions)
  {
    this.permissions = permissions;
  }

  public Object save() {
    Map<String, Object> playerInfo = new HashMap<String, Object>();
    if (permissions != null) {
    	playerInfo.put("permissions", new ArrayList<String>(this.permissions));
    }
    if (this.group != null) {
    	playerInfo.put("group", this.group.getName());
    }

    return playerInfo;
  }
  
}