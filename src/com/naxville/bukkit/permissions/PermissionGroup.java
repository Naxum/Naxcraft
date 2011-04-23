package com.naxville.bukkit.permissions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PermissionGroup
{
  private String name = "";
  private PermissionGroup inheritance = null;
  private Boolean admin = Boolean.valueOf(false);
  private Boolean def = Boolean.valueOf(false);

  private Set<String> permissions = new HashSet<String>();

  protected Object save() {
    Map<String, Object> groupInfo = new HashMap<String, Object>();
    if (!this.name.equalsIgnoreCase("")) {
      groupInfo.put("name", this.name);
    }
    groupInfo.put("permissions", new ArrayList<String>(this.permissions));
    groupInfo.put("default", this.def);
    groupInfo.put("admin", this.admin);
    if (this.inheritance != null) {
      groupInfo.put("inheritance", this.inheritance.getName());
    }
    return groupInfo;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  protected Set<String> getPermissions() {
    return this.permissions;
  }

  protected void addPermission(String permission) {
    this.permissions.add(permission.toLowerCase());
  }

  protected Boolean removePermission(String permission) {
    if (this.permissions.contains(permission.toLowerCase())) {
      this.permissions.remove(permission.toLowerCase());
      return Boolean.valueOf(true);
    }
    return Boolean.valueOf(false);
  }

  protected void setPermissions(Set<String> permissions) {
    this.permissions = permissions;
  }

  protected Boolean isAdmin() {
    return this.admin;
  }

  protected void setAdmin(Boolean admin) {
    this.admin = admin;
  }

  protected Boolean isDefault() {
    return this.def;
  }

  protected void setDefault(Boolean def) {
    this.def = def;
  }

  protected PermissionGroup getInheritance() {
    return this.inheritance;
  }

  protected void setInheritance(PermissionGroup group) {
    this.inheritance = group;
  }

  protected Boolean has(String permission) {
    if ((this.permissions.contains("*")) || (this.permissions.contains(permission.toLowerCase()))) {
      return Boolean.valueOf(true);
    }
    if (this.inheritance != null) {
      return this.inheritance.has(permission);
    }
    return Boolean.valueOf(false);
  }
}