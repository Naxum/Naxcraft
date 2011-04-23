package com.naxville.naxcraft.npcs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.naxville.naxcraft.npcs.NaxcraftNpc.NpcInput;

public class NpcPersonality {
	protected Map<String, String> conversation;
	protected Map<String, List<String>> chatMessages;
	protected String type;
	
	public NpcPersonality(String type){
		conversation = new HashMap<String, String>();
		chatMessages = new HashMap<String, List<String>>();
		this.type = type;
		
		//TODO: LOAD FROM YML ALL POSSIBLE WORDS, ETC, SHOP ETC ETC
		
		if(type.equalsIgnoreCase("jim")){
			List<String> current = new ArrayList<String>();
				current.add("I can't believe this is my new job.");
				current.add("There's a sand-castle competition coming up. You should go.");
				current.add("Trying to start a conversation without saying hi?");
				current.add("Oh great, someone who wants to talk to me.");
			chatMessages.put("unmet", current);
				
			current = new ArrayList<String>();
				current.add("Do I know you?");
				current.add("I don't talk to anyone who doesn't know my name.");
			chatMessages.put("greeting", current);
			
			current = new ArrayList<String>();
				current.add("Oh, hey.");
				current.add("I remember you now.");
				current.add("Glad to see at least one person knows my name.");
			chatMessages.put("met", current);
			
			current = new ArrayList<String>();
				current.add("Hey, there's a sand-castle competition coming up, I'm going, are you?");
				current.add("How annoying do you find Narin?");
				current.add("What about AdjectivalNoun?");
			chatMessages.put("friends", current);
			
			current = new ArrayList<String>();
				current.add("Ow.");
				current.add("Knock it off.");
				current.add("I know people in high places, man.");
				current.add("Seriously, I'll get Naxum to come on and... ban you. Or something.");
				current.add(":(");
			chatMessages.put("hurt", current);
			
			current = new ArrayList<String>();
				current.add("Pfft, like I actually do anything right now.");
				current.add("Seriously, what do you want me to do?");
				current.add("Did you think I was a player at first?");
			chatMessages.put("click", current);
			
			current = new ArrayList<String>();
				current.add("Kinda close there, buddy.");
				current.add("Woah, stay back.");
				current.add("Step off my grill, dawg.");
			chatMessages.put("close", current);
			
		} else {
			List<String> current = new ArrayList<String>();
				current.add("");
			chatMessages.put("unmet", current);
			chatMessages.put("greeting", current);
			chatMessages.put("met", current);
			chatMessages.put("friends", current);
			chatMessages.put("hurt", current);
			chatMessages.put("close", current);
			chatMessages.put("click", current);
		}
	}
	
	public String getConversation(String name){
		name = name.toLowerCase();
		
		if(!this.conversation.containsKey(name)){
			this.conversation.put(name, "unmet");
		}
		return this.conversation.get(name);
	}
	
	private void setConversation(String name, String i){
		name = name.toLowerCase();
		
		if(this.conversation.containsKey(name)){
			this.conversation.remove(name);
			this.conversation.put(name, i);
			
		} else {
			this.conversation.put(name, i);
		}
	}
	
	private String getMessage(NpcInput type, String i){		
		Random random = new Random();
		
		int thisone = random.nextInt(this.chatMessages.get(i).size());
		return this.chatMessages.get(i).get(thisone);
	}
	
	public String getType(){
		return this.type;
	}
	
	public String getChatResponse(String name, String message){
		name = name.toLowerCase();
		message = message.toLowerCase();
		//TODO: TYPE SPECIFIC RESPONSES/CONVERSATIONS
		
		//haven't spoken yet.
		String convo = getConversation(name);
		String derp = "";
		
		if(convo.equals("unmet") || convo.equals("greeting")){
			if(message.contains("hey") || message.contains("hi") || message.contains("hello") || message.contains("yo")){
				this.setConversation(name, "greeting");
			}
			if(message.contains("jim")){
				this.setConversation(name, "met");
				derp = this.getMessage(NpcInput.CHAT, this.getConversation(name));
				this.setConversation(name, "friends");
			}
		}
		if(derp == "") return this.getMessage(NpcInput.CHAT, this.getConversation(name));
		return derp;
	}
	
	public String getHurtResponse(String name){
		name = name.toLowerCase();
		return this.getMessage(NpcInput.HURT, "hurt");
	}
	
	public String getProximityResponse(String name){
		name = name.toLowerCase();
		return this.getMessage(NpcInput.PROXIMITY, "close");
	}
	
	public String getRightClickResponse(String name){
		name = name.toLowerCase();
		return this.getMessage(NpcInput.RIGHT_CLICK, "click");
	}
	
	public String getBounceResponse(String name){
		name = name.toLowerCase();
		return this.getMessage(NpcInput.BOUNCE, "close");
	}
	
	public String getName(){
		return this.type;
	}
}
