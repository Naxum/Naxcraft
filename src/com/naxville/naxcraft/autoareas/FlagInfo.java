package com.naxville.naxcraft.autoareas;

public class FlagInfo 
{
	public String name;
	public String description;
	public boolean adminOnly;
	public int cost;
	public String required;
	
	public FlagInfo(String name, String description, int cost, String required)
	{
		this.name = name;
		this.description = description;
		this.adminOnly = false;
		this.cost = cost;
		this.required = required;
	}
	
	public FlagInfo(String name, String description)
	{
		this.name = name;
		this.description = description;
		this.adminOnly = true;
	}
}
