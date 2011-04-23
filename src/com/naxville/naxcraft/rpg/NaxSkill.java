package com.naxville.naxcraft.rpg;

import java.util.List;

public class NaxSkill {
	protected int id;
	protected int maxLevel;
	protected List<Effect> effects;
	
	
	public NaxSkill(List<Effect> effects){
	}
	
	public enum Effect {
		BLOCK_BREAK,
		BLOCK_DAMAGE,
		BLOCK_PLACE,
		BLOCK_DROP,
		
		MOB_DAMAGE,
		MOB_DROP,
		
		CREATURE_DAMAGE,
		CREATURE_DROP,
		
		PLAYER_DAMAGE,
		PLAYER_DROP,
		PLAYER_FALL_DAMAGE,
		PLAYER_HEALTH_REGEN,
		PLAYER_HUNGER,
		PLAYER_THIRST,
		PLAYER_MOVE,
		PLAYER_BREATH,
		PLAYER_ARMOR,
		
		SHOP_INTERACT,
		SHOP_EDIT,
		SHOP_BUY_VALUE,
		SHOP_SELL_VALUE,
		
		CRAFT_DISALLOWED,
		CRAFT_SUCCESS_RATE,
		CRAFT_AMOUNT,
		
		TOOL_DURABILITY,
		TOOL_INSTANT_BREAK,
		
	}
}
