package com.naxville.naxcraft.admin;

import net.minecraft.server.EntityHuman;
import net.minecraft.server.ItemStack;
import net.minecraft.server.TileEntityChest;

public class VirtualChest extends TileEntityChest
{
	public VirtualChest()
	{
		super();
	}
	
	@Override
	public boolean a(EntityHuman entityhuman)
	{
		/*
		 * For this proof of concept, we ALWAYS validate the chest.
		 * This behavior has not been thoroughly tested, and may cause unexpected results depending on the state of the player.
		 * 
		 * Depending on your purposes, you might want to change this.
		 * It would likely be preferable to enforce your business logic outside of this file instead, however.
		 */
		return true;
	}
	
	public void addItem(int slot, org.bukkit.inventory.ItemStack stack)
	{
		if (stack == null) return;
		
		this.setItem(slot, new ItemStack(stack.getTypeId(), stack.getAmount(), stack.getDurability()));
	}
}
