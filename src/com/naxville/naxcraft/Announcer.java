package com.naxville.naxcraft;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.CaveSpider;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Giant;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Silverfish;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public class Announcer
{
	Naxcraft plugin;
	private Map<Player, String> killMessages = new HashMap<Player, String>();
	
	public Announcer(Naxcraft plugin)
	{
		this.plugin = plugin;
	}
	
	public void announce(String str)
	{
		plugin.getServer().broadcastMessage(str);
	}
	
	public void announce(String str, World world)
	{
		List<Player> players = world.getPlayers();
		
		for (Player player : players)
		{
			if (player == null || !player.isOnline()) continue;
			player.sendMessage(str);
		}
		
		System.out.println(str);
	}
	
	public void announceToOtherWorlds(String str, World world)
	{
		for (World w : plugin.getServer().getWorlds())
		{
			if (w.equals(world)) continue;
			
			List<Player> players = w.getPlayers();
			
			for (Player player : players)
			{
				if (player == null || !player.isOnline()) continue;
				player.sendMessage(str);
			}
		}
		
		System.out.println(str);
	}
	
	public void handleEntityDeath(EntityDeathEvent event)
	{
		if (!(event.getEntity() instanceof Player)) return;
		
		Player player = (Player) event.getEntity();
		
		if (killMessages.containsKey(player) && killMessages.get(player) != null)
		{
			if (killMessages.get(player) == "")
			{
				announce(plugin.playerManager.getDisplayName(player) + Naxcraft.MSG_COLOR + " has died!", player.getWorld());
			}
			else
			{
				announce(plugin.playerManager.getDisplayName(player) + Naxcraft.MSG_COLOR + " was killed by " + killMessages.get(player) + Naxcraft.MSG_COLOR + "!", player.getWorld());
			}
			killMessages.put(player, null);
		}
	}
	
	public void announceDeath(Player player)
	{
		EntityDamageEvent event = player.getLastDamageCause();
		
		String middleText = "", cause = "";
		
		if(event == null || event.getCause() == null)
		{
			middleText = "was";
			cause = "sacrificed to the gods!";
		}
		else if (event instanceof EntityDamageByEntityEvent)
		{
			EntityDamageByEntityEvent ev = (EntityDamageByEntityEvent) event;
			Entity e = ev.getDamager();
			
			if (e instanceof Player)
			{
				String item = "";
				if (((Player) e).getItemInHand().getType() != Material.AIR)
				{
					item += "a " + ((Player) e).getItemInHand().getType().toString().replace("_", " ").toLowerCase().replace("spade", "shovel");
				}
				else
				{
					item += "their bare hands";
				}
				cause = plugin.getNickName(((Player) e)) + Naxcraft.MSG_COLOR + " with " + item;
				
			}
			else
			{
				if (e instanceof Creature)
				{
					Random r = new Random();
					Creature c = (Creature) e;
					String[] messages;
					if (c instanceof Skeleton)
					{
						messages = new String[]
						{
								"trying to steal bones",
								"getting boned",
								"stealing from Uncle Muscles' charity",
								"Muscles for Bones charity",
								"picking his teeth with a skeleton"
						};
						cause = messages[r.nextInt(messages.length)];
						
					}
					else if (c instanceof Giant)
					{
						messages = new String[]
						{
								"getting squashed",
								"failing to beat Goliath",
								"being a bad David",
								"not using a slingshot",
								"not being a good David"
						};
						cause = messages[r.nextInt(messages.length)];
					}
					else if (c instanceof Enderman)
					{
						messages = new String[]
						{
								"getting spooked",
								"getting picked up by an Enderman",
								"noticing things they shouldn't",
								"observing things they shouldn't",
								"looking at an Enderman the wrong way"
						};
						cause = messages[r.nextInt(messages.length)];
					}
					else if (c instanceof Silverfish)
					{
						messages = new String[]
						{
								"getting nibbled on",
								"so many bugs",
								"oh god they're everywhere",
								"bugs in their eyes",
								"bugs everywhere"
						};
						cause = messages[r.nextInt(messages.length)];
					}
					else if (c instanceof CaveSpider)
					{
						messages = new String[]
						{
								"poisoned silk",
								"poisoned farts",
								"getting even with Romeo",
								"turing into Venom",
								"wishing there was antivenom"
						};
						cause = messages[r.nextInt(messages.length)];
					}
					else if (c instanceof Spider)
					{
						messages = new String[]
						{
								"getting all silky",
								"spinning web",
								"Charlotte's Web",
								"spindles, so many spindles",
								"picking his teeth with a skeleton"
						};
						cause = messages[r.nextInt(messages.length)];
					}
					else if (c instanceof Ghast)
					{
						messages = new String[]
						{
								"ghastsmax",
								"ghastsmacked",
								"ghastthrashed",
								"fire ball'd"
						};
						cause = messages[r.nextInt(messages.length)];
					}
					else if (c instanceof PigZombie)
					{
						messages = new String[]
						{
								"zombie pork",
								"nibbling on zombie pork",
								"stealing zombie pork",
								"asking a zombie pork where it got its gold sword"
						};
						cause = messages[r.nextInt(messages.length)];
					}
					else if (c instanceof Zombie)
					{
						messages = new String[]
						{
								"donating their brain",
								"trying to lick some rotten flesh",
								"living rotten flesh",
								"wondering why zombies don't drop feathers anymore"
						};
						cause = messages[r.nextInt(messages.length)];
					}
					else if (c instanceof Slime)
					{
						messages = new String[]
						{
								"getting all slimey",
								"getting slimed",
								"oh god there's so many slimes",
								"what do you mean if you kill a big slime more pop out?"
						};
						cause = messages[r.nextInt(messages.length)];
					}
					else if (c instanceof Wolf)
					{
						messages = new String[]
						{
								"puppies!",
								"getting eaten alive by puppies",
								"licked too hard by puppies",
								"so many puppies, so little flesh"
						};
						cause = messages[r.nextInt(messages.length)];
					}
					else
					{
						cause = "something or other";
					}
				}
			}
		}
		else
		{
			Random r = new Random();
			String[] messages;
			
			switch (event.getCause())
			{
				case BLOCK_EXPLOSION:
					messages = new String[]
					{
							"cat smack",
							"playing with dynamite"

					};
					cause = messages[r.nextInt(messages.length)];
					break;
				
				case CONTACT:
					messages = new String[]
					{
							"snuggling with desert plants",
							"feeling up the scenery",
							"touching things they shouldn't",
							"getting pricked"

					};
					cause = messages[r.nextInt(messages.length)];
					break;
				
				case FIRE_TICK:
					messages = new String[]
					{
							"roastin'",
							"toastin'",
							"making smores",
							"eating fire"

					};
					cause = messages[r.nextInt(messages.length)];
					break;
				
				case SUFFOCATION:
					messages = new String[]
					{
							"attempting a block transfusion",
							"checking out a block texture up close",
							"huffing blocks",
							"snorting cubes"

					};
					cause = messages[r.nextInt(messages.length)];
					break;
				
				case DROWNING:
					messages = new String[]
					{
							"failing at holding spacebar",
							"not knowing they can't breathe that water",
							"drinking too much",
							"being an h2o-aholic",
							"snuffin' water"

					};
					cause = messages[r.nextInt(messages.length)];
					break;
				
				case FALL:
					messages = new String[]
					{
							"challenging virtual gravity",
							"learning how to fly the hard way",
							"splatting",
							"becoming two dimensional",
							"wut gravity u guise",
							"oh there's falling damage in this game?",
							"trying to become puddin'"

					};
					cause = messages[r.nextInt(messages.length)];
					break;
				
				case LAVA:
					messages = new String[]
					{
							"having skeleton legs",
							"being about to ragequit at lava",
							"slippin' and roastin'",
							"drinking hot liquids",
							"exposing their skeleton to magma"

					};
					cause = messages[r.nextInt(messages.length)];
					break;
				
				case FIRE:
					messages = new String[]
					{
							"not walking into water",
							"getting crispy",
							"roastin' marshmallows"

					};
					cause = messages[r.nextInt(messages.length)];
					break;
				
				case ENTITY_EXPLOSION:
					messages = new String[]
					{
							"getting blown",
							"getting knocked around",
							"getting blasted"

					};
					cause = messages[r.nextInt(messages.length)];
					break;
				
				case LIGHTNING:
					messages = new String[]
					{
							"having a shocking old time",
							"getting zapped",
							"zap'd"

					};
					cause = messages[r.nextInt(messages.length)];
					break;
				
				case ENTITY_ATTACK:
					messages = new String[]
					{
							"getting beaten up",
							"pow punched",
							"no one ever dies from this"

					};
					cause = messages[r.nextInt(messages.length)];
					break;
				
				case SUICIDE:
					messages = new String[]
					{
							"committing suicide?!"
					};
					
				case MAGIC:
					messages = new String[]
											{
							"magic"
											};
					
				case POISON:
					messages = new String[]
											{
							"poison"
											};
					
				case STARVATION:
					messages = new String[]
											{
							"the land of thirst!"
											};
					
				case VOID:
					cause = "attempting to explore the great unknown";
					break;
				
				default:
					cause = event.getCause().toString().toLowerCase().replace("_", " ");
					break;
			}
		}
		
		if (cause.equals("")) cause = "something or other";
		if (middleText.equals("")) middleText = "was killed by";
		
		announce(plugin.getNickName(player) + " " + middleText + " " + cause + "!", player.getWorld());
	}
}
