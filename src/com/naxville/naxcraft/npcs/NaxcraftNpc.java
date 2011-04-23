package com.naxville.naxcraft.npcs;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.naxville.naxcraft.Naxcraft;

import redecouverte.npcspawner.BasicHumanNpc;
import redecouverte.npcspawner.NpcSpawner;

public class NaxcraftNpc {
	
	protected Material hand;
	protected NpcPersonality personalityName;
	protected BasicHumanNpc human;
	protected Location location;
	protected boolean invincible;
	protected String uniqueId = "";
	protected String proximityMsg = "";
	protected String hurtMsg = "";
	protected String clickMsg = "";
	protected String chatMsg = "";
	
	protected NpcPersonality personality;
	
	//actually creating one
	public NaxcraftNpc(String uniqueId, String name, Location location) {
		human = NpcSpawner.SpawnBasicHumanNpc(uniqueId, name, location.getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
		
		this.uniqueId = uniqueId;
		this.invincible = true;
		this.location = location;
		
		if(name.equalsIgnoreCase("Naxville_NPC")){
			setPersonality(new NpcPersonality("jim"));
		} else {
			setPersonality(new NpcPersonality("silent"));
		}
	}
	
	//loading one
	public NaxcraftNpc(String uniqueId, String name, Location location, Boolean invincible, NpcPersonality personality, Material handItem, String rightClick, String proximity, String hurt, String chat) {
		this.uniqueId = uniqueId;
		
		human = NpcSpawner.SpawnBasicHumanNpc(uniqueId, name, location.getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
		
		setPersonality(personality);
		this.invincible = invincible;

		this.location = location;
		
		putHand(handItem);
		
		setMsg(NpcInput.RIGHT_CLICK, rightClick);
		setMsg(NpcInput.PROXIMITY, proximity);
		setMsg(NpcInput.HURT, hurt);
		setMsg(NpcInput.CHAT, chat);
	}

	public Material getHand() {
		return hand;
	}
	
	public void getMsg(String msg, Player player){
		String message = "";
		if(msg.equalsIgnoreCase("proximity"))
			message = this.proximityMsg;
		
		if(msg.equalsIgnoreCase("hurt"))
			message = this.hurtMsg;
		
		if(msg.equalsIgnoreCase("click"))
			message = this.clickMsg;
		
		this.say(message, player);
	}
	
	public void respond(NpcInput input, Player player, String message){
		if(input == NpcInput.UNSET) return;
		
		//look!
		float yaw = player.getLocation().getYaw() % 360 - 180;
		this.human.moveTo(this.location.getX(), this.location.getY(), this.location.getZ(), yaw, 0); //loc.getPitch()
		
		String override = "";
		if(input == NpcInput.PROXIMITY)
			override = this.proximityMsg;
		
		if(input == NpcInput.HURT)
			override = this.hurtMsg;
		
		if(input == NpcInput.RIGHT_CLICK)
			override = this.clickMsg;
		
		if(input == NpcInput.CHAT)
			override = this.chatMsg;
		
		String msg = "";
		if(override.equals("") || override.equals("none") || override.isEmpty()){
			
			if(input == NpcInput.CHAT){
				msg = this.personality.getChatResponse(player.getName(), message);
				
			} else if (input == NpcInput.HURT) {
				msg = this.personality.getHurtResponse(player.getName());
				
			} else if (input == NpcInput.RIGHT_CLICK) {
				msg = this.personality.getRightClickResponse(player.getName());
				
			} else if (input == NpcInput.PROXIMITY) {
				msg = this.personality.getProximityResponse(player.getName());
				
			} else if (input == NpcInput.BOUNCE) {
				msg = this.personality.getBounceResponse(player.getName());
				
			}
			
		} else {
			msg = override;
		}
		
		if(!msg.equals("") || !msg.equals("none") || !override.isEmpty()){
			this.say(msg, player);
		}
	}
	
	public void setMsg(NpcInput type, String message){
		if(type == NpcInput.PROXIMITY)
			this.proximityMsg = message;
		
		if(type == NpcInput.HURT)
			this.hurtMsg = message;
		
		if(type == NpcInput.RIGHT_CLICK)
			this.clickMsg = message;
		
		if(type == NpcInput.CHAT)
			this.chatMsg = message;
	}
	
	public void putHand(Material hand) {
		this.hand = hand;
		if(hand != null){
			this.human.getBukkitEntity().setItemInHand(new ItemStack(hand, 1));
		} else {
			this.human.getBukkitEntity().setItemInHand(null);
		}
	}
	
	public void setPersonality(NpcPersonality npcPersonality){
		this.personality = npcPersonality;
	}
	
	public NpcPersonality getPersonality(){
		return this.personality;
	}
	
	public String saveMsg(String msg){
		String message = "";
		if(msg.equalsIgnoreCase("proximity"))
			message = this.proximityMsg;
		
		if(msg.equalsIgnoreCase("hurt"))
			message = this.hurtMsg;
		
		if(msg.equalsIgnoreCase("click"))
			message = this.clickMsg;
		
		if(msg.equalsIgnoreCase("chat"))
			message = this.chatMsg;
		
		return message;
	}
	
	public String savePersonality(){
		return this.personalityName.getName();
	}
	
	public String saveHand() {
		if(hand == null) return "null";
		return hand.name();
	}

	public void say(String message, Player player){
    	if(message != "") player.sendMessage(Naxcraft.MSG_COLOR + "[NPC] <" + Naxcraft.NPC_COLOR + this.human.getName() + Naxcraft.MSG_COLOR + "> " + Naxcraft.DEFAULT_COLOR + message);
    }
	
	public String getUniqueId(){
		return this.uniqueId;
	}
	
	public BasicHumanNpc getHuman(){
		return this.human;
	}
	
	public void animateArmSwing(){
		this.human.animateArmSwing();
	}
	
	public Location getLocation(){
		return this.location;
	}
	
	public void moveTo(Location location){
		this.location = location;
		this.human.moveTo(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
	}
	
	public enum NpcInput {
		PROXIMITY,
		HURT,
		RIGHT_CLICK,
		CHAT,
		BOUNCE,
		UNSET
	}

	public boolean isInvincible() {
		return this.invincible;
	}

	public String getName() {
		return this.human.getName();
	}

	public void delete() {
		NpcSpawner.RemoveBasicHumanNpc(this.human);
	}

	public void reload() {
		this.human = NpcSpawner.SpawnBasicHumanNpc(this.uniqueId, this.getName(), location.getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
		
	}
}
