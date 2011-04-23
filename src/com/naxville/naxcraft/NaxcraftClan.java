package com.naxville.naxcraft;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class NaxcraftClan {

	public static Naxcraft plugin;
	
	private Object members = new ArrayList<String>();
	
	private String name;
	private String cMOTD = "";
	private String leader = "";
	
	public NaxcraftClan(Naxcraft instance, String cname){
		plugin = instance;
		this.name = cname;
	}
	
	@SuppressWarnings("unchecked")
	protected boolean disband(){
		Player player;
		for (String member : ((ArrayList<String>)members)) {
			player = plugin.getServer().getPlayer(member);
			if (player != null)
				player.sendMessage(Naxcraft.COMMAND_COLOR + this.name + " has been disbanned");
		}
		return true;
	}
	
	protected void setMOTD(String message){
		this.cMOTD = message;
		sendGuildMessage(Naxcraft.COMMAND_COLOR + "Clan MOTD: " + Naxcraft.COMMAND_COLOR + this.cMOTD);
	}
	
	public String getMOTD(){
		return ChatColor.GOLD + "Clan MOTD: " + Naxcraft.COMMAND_COLOR + cMOTD;
	}
	
	protected String getName(){
		return this.name;
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getAllMembers(){
		return (ArrayList<String>)members;
	}
	
	@SuppressWarnings("unchecked")
	protected List<Player> getOnlineMembers(){
		List<Player> players = new ArrayList<Player>();
		for (String member : ((ArrayList<String>)members)){
			Player player = plugin.getServer().getPlayer(member);
			if (player != null){
				players.add(player);
			}
		}
		return players;
	}

	@SuppressWarnings("unchecked")
	protected boolean addMember(Player player){
		if (!((ArrayList<String>)members).contains(player.getName())){
			((ArrayList<String>)members).add(player.getName());
			sendGuildMessage(Naxcraft.COMMAND_COLOR + player.getName() + " has joined the clan");
			player.sendMessage(Naxcraft.COMMAND_COLOR + "Welcome to " + this.name);
			player.sendMessage(this.getMOTD());
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	protected boolean removeMember(Player player){
		if (((ArrayList<String>)members).contains(player.getName())){
			((ArrayList<String>)members).remove(player.getName());
			player.sendMessage(Naxcraft.COMMAND_COLOR + "You have been removed from " + this.name);
			sendGuildMessage(Naxcraft.COMMAND_COLOR + player.getName() + " has left the clan");
			return true;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	protected boolean removeMember(String player){
		Player p = plugin.getServer().getPlayer(player);
		if (p != null){
			return removeMember(p);
		} else {
			if (((ArrayList<String>)members).contains(player)){
				((ArrayList<String>)members).remove(player);
				sendGuildMessage(Naxcraft.COMMAND_COLOR + player + " has left the clan");
				return true;
			}
		}
		return false;
	}
	
	protected boolean isPlayerLeader(Player player){
		return (player.getName().equalsIgnoreCase(this.leader));
	}
	
	protected boolean isPlayerLeader(String player){
		return (player.equalsIgnoreCase(this.leader));
	}
	
	protected String getLeader(){
		return leader;
	}
	
	@SuppressWarnings("unchecked")
	protected boolean setLeader(Player player){
		if (((ArrayList<String>)members).contains(player.getName())){
			if (player.getName() != leader && leader != "") {
				Player pleader = plugin.getServer().getPlayer((leader));
				if (pleader != null) {
					pleader.sendMessage(Naxcraft.COMMAND_COLOR + "You are no longer leader of " + this.name);
				}
			}
			this.leader = player.getName();
			player.sendMessage(Naxcraft.COMMAND_COLOR + "You are now the leader of " + this.name);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	protected boolean isMember(Player player){
		return ((ArrayList<String>)members).contains(player.getName());
	}
	
	@SuppressWarnings("unchecked")
	public boolean isMember(String player){
		Player p = plugin.getServer().getPlayer(player);
		if (p != null){
			return isMember(p);
		} else if (((ArrayList<String>)members).contains(player)){
			return true;
		}
		return false;
	}
	
 	protected void sendGuildMessage(String message){
		for (Player player : this.getOnlineMembers()){
			player.sendMessage(message);
		}
	}
 	
 	protected void loadMembers(List<String> memberList){
 		this.members = memberList;
 	}
 	
 	protected void loadLeader(String player){
 		this.leader = player;
 	}
 	
 	protected void loadMOTD(String motd){
 		this.cMOTD = motd;
 	}
 	
 	@SuppressWarnings("unchecked")
	protected Object saveMembers(){
 		if (((ArrayList<String>)members).isEmpty()){
 			return "";
 		}
 		return this.members;
 	}
 	
 	protected String saveLeader(){
 		return this.leader;
 	}
 	
 	protected String saveMOTD(){
 		return this.cMOTD;
 	}
}
