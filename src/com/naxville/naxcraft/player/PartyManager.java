package com.naxville.naxcraft.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.naxville.naxcraft.NaxColor;

public class PartyManager
{
	protected PlayerManager pm;
	protected List<Party> parties = new ArrayList<Party>();
	protected Map<Player, ArrayList<PartyQuestion>> questions = new HashMap<Player, ArrayList<PartyQuestion>>();
	protected Map<Player, Party> invites = new HashMap<Player, Party>();
	
	public PartyManager(PlayerManager pm)
	{
		this.pm = pm;
	}
	
	public boolean isPlayerInParty(Player player)
	{
		for (Party p : parties)
		{
			if (p.players.contains(player)) { return true; }
		}
		
		return false;
	}
	
	public Party getParty(Player player)
	{
		for (Party p : parties)
		{
			if (p.players.contains(player)) { return p; }
		}
		
		return null;
	}
	
	public void addRequest(Player player, PartyQuestion question)
	{
		if (!questions.containsKey(player))
		{
			questions.put(player, new ArrayList<PartyQuestion>());
		}
		
		ArrayList<PartyQuestion> tempList = questions.get(player);
		tempList.add(question);
		
		questions.put(player, tempList);
		
		player.sendMessage(getRequestMessage(player, question));
		
	}
	
	public void addInvite(Player player, Player target, Party party)
	{
		target.sendMessage(NaxColor.PARTY + "[Party] " + pm.getDisplayName(player) + " has invited you to join their party. (" + NaxColor.WHITE + "/party yes" + NaxColor.MSG + " or " + NaxColor.WHITE + "/party no" + NaxColor.MSG + ")");
		invites.put(target, party);
	}
	
	public void checkRequests(Player player)
	{
		if (!questions.containsKey(player) || questions.get(player).size() == 0)
		{
			player.sendMessage(NaxColor.MSG + "You do not have any requests.");
			return;
		}
		
		ArrayList<PartyQuestion> requests = questions.get(player);
		
		player.sendMessage(NaxColor.MSG + "You have " + NaxColor.PARTY + requests.size() + " requests " + NaxColor.MSG + ".");
		player.sendMessage(getRequestMessage(player, requests.get(requests.size() - 1)));
	}
	
	public String getRequestMessage(Player player, PartyQuestion question)
	{
		String message = NaxColor.PARTY + "[Party Request]: " + NaxColor.MSG;
		String playerName = pm.getDisplayName(question.player);
		String targetName = pm.getDisplayName(question.target);
		
		switch (question.type)
		{
			case JOIN:
				message += playerName + " would like to join.";
				break;
			
			case INVITE:
				message += playerName + " would like to invite " + targetName;
				break;
			
			case KICK:
				message += playerName + " would like to kick " + targetName;
				break;
		}
		
		return message;
	}
	
	public void handleEntityDamageByEntity(EntityDamageByEntityEvent event)
	{
		if ((!(event.getDamager() instanceof Player) && !(event.getEntity() instanceof Player)) || event.isCancelled()) { return; }
		
		Player player = (Player) event.getEntity();
		Player target = (Player) event.getDamager();
		
		Party party = getParty(player);
		Party party2 = getParty(target);
		
		if (party != null && party2 != null && party2 == party)
		{
			event.setCancelled(true);
		}
	}
	
	public void handlePlayerPickupItem(PlayerPickupItemEvent event)
	{
		Player player = event.getPlayer();
		Party party = getParty(player);
		// ItemStack item = event.getItem().getItemStack().clone();
		
		if (party == null) return;
		else return;
		/*
		List<Player> looters = party.getLooters();
		
		if(looters.isEmpty() || looters.size() == 0) return;
		
		event.getItem().remove();
		event.setCancelled(true);
		
		int original = item.getAmount();
		int division = (int) item.getAmount() / looters.size();
		if(division <= 0) division = 1;
		int leftOver = item.getAmount() % looters.size();
		
		
		if(original < looters.size())
		{
			int i = 0;
			for(Player p : looters)
			{
				if(!(p.getWorld() == player.getWorld() && p.getLocation().distance(player.getLocation()) < 200))
				{
					continue;
				}
				
				int amount = division;
				if(i >= original)
				{
					return;
				}
				else if(i + amount > original)
				{
					amount = leftOver;
				}
				
				p.getInventory().addItem(new ItemStack(item.getType(), amount, item.getDurability()));
				
				i += amount;
			}
			
			if(i < original)
			{
				player.getInventory().addItem(new ItemStack(item.getType(), leftOver, item.getDurability()));
			}
		}
		else
		{
			for(int i = 0; i < division; i++)
			{
				looters.get(new Random().nextInt(looters.size())-1).getInventory().addItem(new ItemStack(item.getType(), division, item.getDurability()));
			}
		}
		*/

	}
	
	public void handlePlayerQuit(PlayerQuitEvent event)
	{
		Party party = getParty(event.getPlayer());
		
		if (party != null)
		{
			pm.plugin.getServer().getScheduler().scheduleSyncDelayedTask(pm.plugin, new PartyWait(event.getPlayer(), party), 20 * 10);
		}
	}
	
	public boolean runPartyCommand(Player player, String[] args)
	{
		if (args.length == 0)
		{
			if (getParty(player) == null)
			{
				printHelp(player);
			}
			else
			{
				printInfo(player);
			}
			return true;
		}
		
		if (args.length == 1)
		{
			String label = args[0];
			Party party = getParty(player);
			
			if (label.equalsIgnoreCase("help"))
			{
				printHelp(player);
			}
			else if (label.equalsIgnoreCase("create"))
			{
				if (party != null)
				{
					player.sendMessage(NaxColor.MSG + "You are already in a party. Leave or disband your current party to create a new one.");
					return true;
				}
				
				party = new Party(player);
				parties.add(party);
				
				party.notice("Party has been created!");
			}
			else if (label.equalsIgnoreCase("private"))
			{
				if (party == null)
				{
					notInParty(player);
					return true;
				}
				
				if (party.leader == player)
				{
					boolean publicParty = party.togglePublic();
					
					String now = publicParty ? "public" : "private";
					
					party.notice("The party is now " + now + ".");
				}
				else
				{
					player.sendMessage(NaxColor.MSG + "You are not the leader of the party, you cannot set the public option.");
				}
			}
			else if (label.equalsIgnoreCase("pass"))
			{
				if (party == null)
				{
					notInParty(player);
					return true;
				}
				boolean passing = party.togglePassing(player);
				
				String now = passing ? "now" : "no longer";
				
				player.sendMessage(NaxColor.PARTY + "[Party] " + NaxColor.MSG + "You are " + now + " passing on all loot.");
				
			}
			else if (label.equalsIgnoreCase("leave"))
			{
				if (party == null)
				{
					notInParty(player);
					return true;
				}
				
				if (party.leader == player)
				{
					if (party.players.size() == 1)
					{
						party.disband();
					}
					else
					{
						party.leave(player);
						party.setLeader();
					}
				}
				else
				{
					party.leave(player);
				}
			}
			else if (label.equalsIgnoreCase("disband"))
			{
				if (party == null)
				{
					notInParty(player);
					return true;
				}
				
				if (party.leader == player)
				{
					party.disband();
				}
				else
				{
					player.sendMessage(NaxColor.MSG + "You are not the party leader, you may not use disband the party.");
				}
			}
			else if (label.equalsIgnoreCase("yes") || label.equalsIgnoreCase("no"))
			{
				if (party == null)
				{
					if (invites.containsKey(player))
					{
						if (label.equalsIgnoreCase("yes"))
						{
							invites.get(player).join(player);
						}
						else if (label.equalsIgnoreCase("no"))
						{
							invites.get(player).notice(pm.getDisplayName(player) + " has refused the invitation to join.");
						}
						invites.remove(player);
					}
					else
					{
						player.sendMessage(NaxColor.MSG + "You are not in a party and were not invited to any.");
					}
				}
				else
				{
					if (questions.containsKey(player) && questions.get(player).size() > 0)
					{
						PartyQuestion question = questions.get(player).get(0);
						
						if (label.equalsIgnoreCase("yes"))
						{
							switch (question.type)
							{
								case INVITE:
									party.invite(question.player, question.target);
									break;
								
								case JOIN:
									party.join(question.player);
									break;
								
								case KICK:
									party.kickPlayer(question.player, question.target);
									break;
							}
						}
						else
						{
							player.sendMessage(NaxColor.MSG + "You have refused " + pm.getDisplayName(question.player) + "'s request.");
							question.player.sendMessage(pm.getDisplayName(player) + " has refused your request.");
						}
						
						questions.get(player).remove(question);
					}
					else
					{
						player.sendMessage(NaxColor.MSG + "You have no requests.");
					}
				}
			}
			else if (label.equalsIgnoreCase("requests"))
			{
				if (party == null)
				{
					notInParty(player);
					return true;
				}
				
				checkRequests(player);
			}
		}
		else if (args.length == 2)
		{
			String label = args[0];
			Party party = getParty(player);
			
			if (label.equalsIgnoreCase("join"))
			{
				if (party != null)
				{
					player.sendMessage(NaxColor.MSG + "You are already in a party. To join another, leave this one first.");
					return true;
				}
				
				Player target = pm.plugin.getServer().getPlayer(args[1]);
				
				if (target == null)
				{
					player.sendMessage(NaxColor.MSG + "The player " + args[1] + " is either offline or doesn't exist.");
					return true;
				}
				
				party = getParty(target);
				
				if (party == null)
				{
					player.sendMessage(pm.getDisplayName(target) + " is not in a party.");
					return true;
				}
				else
				{
					party.requestJoin(player);
				}
			}
			else if (label.equalsIgnoreCase("invite"))
			{
				if (party == null)
				{
					notInParty(player);
					return true;
				}
				
				Player target = pm.plugin.getServer().getPlayer(args[1]);
				
				if (target == null)
				{
					player.sendMessage(NaxColor.MSG + "The player " + args[1] + " is either offline or doesn't exist.");
					return true;
				}
				
				Party p2 = getParty(target);
				
				if (p2 != null)
				{
					player.sendMessage(pm.getDisplayName(target) + " is already in another party.");
					return true;
				}
				
				party.invite(player, target);
				
			}
			else if (label.equalsIgnoreCase("kick"))
			{
				if (party == null)
				{
					notInParty(player);
					return true;
				}
				
				Player target = pm.plugin.getServer().getPlayer(args[1]);
				
				if (target == null)
				{
					player.sendMessage(NaxColor.MSG + "The player " + args[1] + " is either offline or doesn't exist.");
					return true;
				}
				
				Party p2 = getParty(target);
				
				if (p2 != party)
				{
					player.sendMessage(pm.getDisplayName(target) + " is not in your party.");
					return true;
				}
				else if (target == player)
				{
					player.sendMessage(NaxColor.MSG + "Knock if off.");
					return true;
				}
				else if (target == party.leader)
				{
					player.sendMessage(NaxColor.MSG + "You're really going to ask the leader to kick himself?");
					return true;
				}
				
				party.kick(player, target);
			}
		}
		
		return true;
	}
	
	private void printInfo(Player player)
	{
		Party party = getParty(player);
		List<String> messages = new ArrayList<String>();
		
		messages.add("----");
		messages.add(NaxColor.PARTY + "Party Information: ");
		
		messages.add("Players (" + party.players.size() + "): " + party.getPlayers());
		
		String publicParty = party.publicParty ? "public" : "private";
		
		messages.add("This is a " + publicParty + " party. The leader is " + pm.getDisplayName(party.leader) + ".");
		
		for (String message : messages)
		{
			player.sendMessage(NaxColor.MSG + message);
		}
		
	}
	
	public void notInParty(Player player)
	{
		player.sendMessage(NaxColor.MSG + "You are not in a party, so you cannot use this command.");
	}
	
	public void printHelp(Player player)
	{
		List<String> help = new ArrayList<String>();
		help.add(NaxColor.MSG + "----");
		help.add(NaxColor.COMMAND + "Party Command:");
		help.add(NaxColor.MSG + "/party " + NaxColor.WHITE + "help" + NaxColor.MSG + ": Shows this message.");
		help.add(NaxColor.MSG + "/party " + NaxColor.WHITE + "create" + NaxColor.MSG + ": Creates a new party.");
		help.add(NaxColor.MSG + "/party " + NaxColor.WHITE + "join <player>" + NaxColor.MSG + ": Asks the leader of that party to join.");
		help.add(NaxColor.MSG + "/party " + NaxColor.WHITE + "invite <player>" + NaxColor.MSG + ": Invites if public, asks if private.");
		help.add(NaxColor.MSG + "/party " + NaxColor.WHITE + "kick <player>" + NaxColor.MSG + ": Asks leader to kick member.");
		help.add(NaxColor.MSG + "/party " + NaxColor.WHITE + "requests" + NaxColor.MSG + ": Lists party requests.");
		help.add(NaxColor.MSG + "/party " + NaxColor.WHITE + "yes/no" + NaxColor.MSG + ": Responds to a party request.");
		help.add(NaxColor.MSG + "/party " + NaxColor.WHITE + "pass" + NaxColor.MSG + ": Pass on all drops, use again to get drops.");
		help.add(NaxColor.MSG + "/party " + NaxColor.WHITE + "leave" + NaxColor.MSG + ": Leaves a party.");
		
		help.add(NaxColor.PARTY + "Party Leaders Only:");
		help.add(NaxColor.PARTY + "/party " + NaxColor.WHITE + "setleader <player>" + NaxColor.MSG + ": Sets a new leader.");
		help.add(NaxColor.PARTY + "/party " + NaxColor.WHITE + "private" + NaxColor.MSG + ": Toggles public invitations or leader only.");
		help.add(NaxColor.PARTY + "/party " + NaxColor.WHITE + "disband" + NaxColor.MSG + ": Disbands a party.");
		
		for (String s : help)
		{
			player.sendMessage(s);
		}
	}
	
	public class Party
	{
		public List<Player> players = new ArrayList<Player>();
		public List<Player> passing = new ArrayList<Player>();
		public Player leader = null;
		public boolean publicParty = true;
		public int lootIndex = 0;
		
		public Party(Player player)
		{
			leader = player;
			players.add(player);
		}
		
		public List<Player> getLooters()
		{
			List<Player> result = new ArrayList<Player>();
			for (Player player : players)
			{
				if (!isPassing(player))
				{
					result.add(player);
				}
			}
			
			return result;
		}
		
		public void chat(Player p, String message)
		{
			for (Player player : players)
			{
				player.sendMessage(NaxColor.PARTY + "[Party] <" + pm.getDisplayName(p) + NaxColor.PARTY + "> " + NaxColor.WHITE + message);
			}
			pm.plugin.log.info("[Party] " + p.getName() + ": " + message);
		}
		
		public void notice(String message)
		{
			for (Player player : players)
			{
				player.sendMessage(NaxColor.PARTY + "[Party] " + NaxColor.MSG + message);
			}
		}
		
		public void requestJoin(Player player)
		{
			PartyQuestion question = new PartyQuestion(player, player, QuestionType.JOIN);
			addRequest(leader, question);
			player.sendMessage(NaxColor.MSG + "You have asked " + pm.getDisplayName(leader) + " to let you join their party.");
		}
		
		public void join(Player player)
		{
			players.add(player);
			notice(pm.getDisplayName(player) + " has joined the party.");
		}
		
		public void invite(Player player, Player target)
		{
			if (publicParty || player == leader)
			{
				invitePlayer(player, target);
				
				player.sendMessage(NaxColor.MSG + "You have asked " + pm.getDisplayName(target) + " to join your party.");
			}
			else
			{
				PartyQuestion question = new PartyQuestion(player, target, QuestionType.INVITE);
				addRequest(leader, question);
				
				player.sendMessage(NaxColor.MSG + "You have asked " + pm.getDisplayName(leader) + " to invite " + pm.getDisplayName(target) + ".");
			}
		}
		
		public void invitePlayer(Player player, Player target)
		{
			addInvite(player, target, this);
		}
		
		public void kick(Player player, Player target)
		{
			if (player == leader)
			{
				kickPlayer(player, target);
			}
			else
			{
				PartyQuestion question = new PartyQuestion(player, target, QuestionType.KICK);
				addRequest(leader, question);
				
				player.sendMessage(NaxColor.MSG + "You have asked " + pm.getDisplayName(leader) + " to kick " + pm.getDisplayName(target) + ".");
			}
		}
		
		public void kickPlayer(Player player, Player target)
		{
			String message = pm.getDisplayName(player) + " has kicked " + pm.getDisplayName(target);
			notice(message);
			players.remove(target);
		}
		
		public boolean togglePublic()
		{
			publicParty = !publicParty;
			
			return publicParty;
		}
		
		public boolean isPassing(Player player)
		{
			return passing.contains(player);
		}
		
		public boolean togglePassing(Player player)
		{
			if (isPassing(player))
			{
				passing.remove(player);
				return false;
			}
			else
			{
				passing.add(player);
				return true;
			}
		}
		
		public void disband()
		{
			notice("The party has been disbanded.");
			
			for (Player player : players)
			{
				if (questions.containsKey(player))
				{
					questions.remove(player);
				}
			}
			
			parties.remove(this);
		}
		
		public void leave(Player player)
		{
			notice(pm.getDisplayName(player) + " has left the party.");
			
			if (questions.containsKey(player))
			{
				questions.remove(player);
			}
			
			players.remove(player);
			
			if (leader == player)
			{
				if (players.size() == 0)
				{
					disband();
				}
				else
				{
					setLeader();
				}
			}
		}
		
		public void setLeader()
		{
			leader = players.get(0);
			notice(pm.getDisplayName(leader) + " is the new party leader.");
		}
		
		public void setLeader(Player player, Player target)
		{
			leader = target;
			notice(pm.getDisplayName(player) + " has made " + pm.getDisplayName(target) + " the new party leader.");
		}
		
		public String getPlayers()
		{
			String result = "";
			
			int i = 0;
			for (Player player : players)
			{
				if (i != 0) result += ", ";
				
				result += pm.getDisplayName(player);
				
				i++;
			}
			
			return result;
		}
	}
	
	public enum QuestionType
	{
		JOIN,
		INVITE,
		KICK
	}
	
	public class PartyQuestion
	{
		public Player player;
		public Player target;
		public QuestionType type;
		
		public PartyQuestion(Player player, Player target, QuestionType type)
		{
			this.player = player;
			this.target = target;
			this.type = type;
		}
	}
	
	public class PartyWait implements Runnable
	{
		private Player player;
		private Party party;
		
		public PartyWait(Player player, Party party)
		{
			this.player = player;
			this.party = party;
		}
		
		public void run()
		{
			if (!player.isOnline())
			{
				party.leave(player);
			}
		}
		
	}
}
