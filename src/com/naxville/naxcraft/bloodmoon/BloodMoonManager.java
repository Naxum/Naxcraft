package com.naxville.naxcraft.bloodmoon;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.entity.CraftCreeper;
import org.bukkit.craftbukkit.entity.CraftWolf;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import com.naxville.naxcraft.NaxColor;
import com.naxville.naxcraft.Naxcraft;

public class BloodMoonManager
{
	protected Naxcraft plugin;
	protected static final int NIGHT_START = 12000;
	protected static final int BLOODMOON_START = 12300;
	protected static final int BLOODMOON_END = 23450;
	protected static final int BLOODMOON_CHANCE = 8;
	protected static final int BLOODMOON_BOSS_PADDING = 5; // in seconds
	protected static final int NIGHT_END = 23850;
	
	protected Random random = new Random();
	
	protected List<World> bloodMoons = new ArrayList<World>();
	protected List<World> checkedWorlds = new ArrayList<World>();
	protected List<Integer> bloodEntities = new ArrayList<Integer>();
	protected Map<World, Long> lastBossSpawn = new HashMap<World, Long>();
	protected Map<World, Boolean> announcedWorlds = new HashMap<World, Boolean>();
	protected Map<Integer, BloodBossCreature> bossEntities = new HashMap<Integer, BloodBossCreature>();
	
	public BloodMoonManager(Naxcraft plugin)
	{
		this.plugin = plugin;
	}
	
	public boolean runBloodMoonCommand(Player player, String[] args)
	{
		
		if (player.getWorld().getName().endsWith("_nether"))
		{
			player.sendMessage(Naxcraft.MSG_COLOR + "Nope, not in the nether.");
			return true;
		}
		
		if (bloodMoons.contains(player.getWorld()))
		{
			player.sendMessage(NaxColor.MSG + "This world is already in a bloodmoon.");
			return true;
		}
		
		player.getWorld().setTime(NIGHT_START);
		bloodMoons.add(player.getWorld());
		
		plugin.announcer.announce(NaxColor.MSG + "The evil within " + plugin.getNickName(player) + " has manifested as a " + NaxColor.BLOODMOON + "Blood Moon" + NaxColor.MSG + "!", player.getWorld());
		
		return true;
	}
	
	public void handleCreatureSpawn(CreatureSpawnEvent event)
	{
		checkBloodMoon(event.getEntity().getWorld());
		
		long time = event.getEntity().getWorld().getTime();
		
		if (event.getSpawnReason() == SpawnReason.NATURAL && bloodMoons.contains(event.getEntity().getWorld()) && BLOODMOON_START < time && time < BLOODMOON_END)
		{
			spawnBloodMonsters(event.getLocation(), event.getEntityType());
			event.setCancelled(true);
		}
	}
	
	public boolean isBloodMoon(World world)
	{
		return bloodMoons.contains(world);
	}
	
	private void checkBloodMoon(World world)
	{
		long time = world.getTime();
		if (bloodMoons.contains(world))
		{
			if (NIGHT_START > time || time > NIGHT_END)
			{
				bloodMoons.remove(world);
				if (checkedWorlds.contains(world)) checkedWorlds.remove(world);
				if (announcedWorlds.containsKey(world)) announcedWorlds.remove(world);
				killAllMonsters(world);
				plugin.announcer.announce(NaxColor.MSG + "The " + NaxColor.BLOODMOON + "Blood Moon" + NaxColor.MSG + " has ended!");
			}
			else
			{
				if (announcedWorlds.containsKey(world))
				{
					if (!announcedWorlds.get(world))
					{
						plugin.announcer.announce(NaxColor.MSG + "The " + NaxColor.BLOODMOON + "Blood Moon" + NaxColor.MSG + " has arrived!");
						announcedWorlds.put(world, true);
					}
				}
				else
				{
					plugin.announcer.announce(NaxColor.MSG + "You feel a " + NaxColor.BLOODMOON + "dark prescence" + NaxColor.MSG + " in the moon...");
					announcedWorlds.put(world, false);
				}
			}
		}
		else
		{
			if (checkedWorlds.contains(world) && time > BLOODMOON_END)
			{
				checkedWorlds.remove(world);
			}
			
			if (!checkedWorlds.contains(world) && time < BLOODMOON_START && time > NIGHT_START)
			{
				int chance = random.nextInt(BLOODMOON_CHANCE);
				
				if (chance == 0)
				{
					bloodMoons.add(world);
				}
				
				checkedWorlds.add(world);
			}
		}
	}
	
	private void killAllMonsters(World world)
	{
		for (Entity e : world.getEntities())
		{
			int id = e.getEntityId();
			if (bloodEntities.contains((Integer) id))
			{
				bloodEntities.remove((Integer) id);
				e.remove();
			}
			
			if (bossEntities.containsKey((Integer) id))
			{
				bossEntities.remove((Integer) id);
				e.remove();
			}
		}
	}
	
	public void spawnBloodMonsters(Location l, EntityType c)
	{
		List<BloodCreature> mobs = new ArrayList<BloodCreature>();
		
		switch (c)
		{
			case SQUID:
			case CHICKEN:
			case COW:
			case PIG:
			case SHEEP:

				if (!lastBossSpawn.containsKey(l.getWorld()))
				{
					lastBossSpawn.put(l.getWorld(), new Date().getTime());
				}
				
				boolean okay = true;
				
				for (BloodBossCreature b : bossEntities.values())
				{
					if (!b.l.getWorld().equals(l.getWorld())) continue;
					
					if (b.l.distance(l) < 350)
					{
						okay = false;
						break;
					}
				}
				
				if (!okay || !(new Date().getTime() - lastBossSpawn.get(l.getWorld()) > BLOODMOON_BOSS_PADDING * 1000)) { return; }
				
				int boss = random.nextInt(3);
				
				if (boss == 0)
				{
					mobs.add(new BloodBossCreature(EntityType.GIANT, l));
				}
				else if (boss == 1)
				{
					mobs.add(new BloodBossCreature(EntityType.CREEPER, l));
				}
				else if (boss == 2)
				{
					mobs.add(new BloodBossCreature(EntityType.GHAST, l));
				}
				
				if (boss != 2) // if not ghast
				{
					for (int i = 0; i < random.nextInt(20) + 10; i++)
					{
						mobs.add(new BloodCreature(EntityType.WOLF, null, l));
					}
					
					for (int i = 0; i < random.nextInt(10) + 5; i++)
					{
						mobs.add(new BloodCreature(EntityType.SILVERFISH, null, l));
					}
					
					for (int i = 0; i < random.nextInt(10) + 5; i++)
					{
						mobs.add(new BloodCreature(EntityType.SKELETON, null, l));
					}
					
					for (int i = 0; i < random.nextInt(10) + 5; i++)
					{
						mobs.add(new BloodCreature(EntityType.PIG_ZOMBIE, null, l));
					}
				}
				break;
			
			case WOLF:
				for (int i = 0; i < random.nextInt(10) + 5; i++)
				{
					mobs.add(new BloodCreature(EntityType.WOLF, null, l));
				}
				
				for (int i = 0; i < random.nextInt(3); i++)
				{
					mobs.add(new BloodCreature(EntityType.WOLF, EntityType.SKELETON, l));
				}
				
				for (int i = 0; i < random.nextInt(6); i++)
				{
					mobs.add(new BloodCreature(EntityType.PIG_ZOMBIE, null, l));
				}
				break;
			
			case CAVE_SPIDER:
				for (int i = 0; i < random.nextInt(5); i++)
				{
					mobs.add(new BloodCreature(EntityType.SPIDER, null, l));
				}
				
				for (int i = 0; i < random.nextInt(6); i++)
				{
					mobs.add(new BloodCreature(EntityType.PIG_ZOMBIE, null, l));
				}
				
				mobs.add(new BloodCreature(EntityType.CAVE_SPIDER, null, l));
				break;
			
			case CREEPER:
				for (int i = 0; i < random.nextInt(10); i++)
				{
					mobs.add(new BloodCreature(EntityType.SPIDER, null, l));
				}
				
				for (int i = 0; i < random.nextInt(2); i++)
				{
					mobs.add(new BloodCreature(EntityType.CREEPER, null, l));
				}
				
				for (int i = 0; i < random.nextInt(2); i++)
				{
					mobs.add(new BloodCreature(EntityType.SPIDER, EntityType.CREEPER, l));
				}
				
				for (int i = 0; i < random.nextInt(2); i++)
				{
					mobs.add(new BloodCreature(EntityType.CAVE_SPIDER, null, l));
				}
				break;
			
			case ENDERMAN:
				// TODO: Add endermoons?!
				for (int i = 0; i < random.nextInt(20); i++)
				{
					mobs.add(new BloodCreature(EntityType.SILVERFISH, null, l));
				}
				
				for (int i = 0; i < random.nextInt(6); i++)
				{
					mobs.add(new BloodCreature(EntityType.PIG_ZOMBIE, null, l));
				}
				break;
			
			case SKELETON:
				for (int i = 0; i < random.nextInt(10); i++)
				{
					mobs.add(new BloodCreature(EntityType.SKELETON, null, l));
				}
				
				for (int i = 0; i < random.nextInt(3); i++)
				{
					mobs.add(new BloodCreature(EntityType.SPIDER, EntityType.SKELETON, l));
				}
				
				for (int i = 0; i < random.nextInt(6); i++)
				{
					mobs.add(new BloodCreature(EntityType.PIG_ZOMBIE, null, l));
				}
				break;
			
			case SPIDER:
				for (int i = 0; i < random.nextInt(10); i++)
				{
					mobs.add(new BloodCreature(EntityType.SPIDER, null, l));
				}
				
				for (int i = 0; i < random.nextInt(2); i++)
				{
					mobs.add(new BloodCreature(EntityType.CAVE_SPIDER, null, l));
				}
				
				for (int i = 0; i < random.nextInt(5); i++)
				{
					mobs.add(new BloodCreature(EntityType.SILVERFISH, null, l));
				}
				break;
			
			case ZOMBIE:
				for (int i = 0; i < random.nextInt(10); i++)
				{
					mobs.add(new BloodCreature(EntityType.ZOMBIE, null, l));
				}
				
				for (int i = 0; i < random.nextInt(10); i++)
				{
					mobs.add(new BloodCreature(EntityType.SILVERFISH, null, l));
				}
				
				for (int i = 0; i < random.nextInt(6); i++)
				{
					mobs.add(new BloodCreature(EntityType.SLIME, null, l));
				}
				break;
			
			default:
				// These shouldn't spawn naturally.
				break;
		}
		
		for (BloodCreature ct : mobs)
		{
			spawnNewBloodMonster(ct);
		}
	}
	
	public void spawnNewBloodMonster(BloodCreature c)
	{
		if (c instanceof BloodBossCreature)
		{
			if (c.c == EntityType.CREEPER)
			{
				LivingEntity e = c.l.getWorld().spawnCreature(c.l, c.c);
				
				doBossCreatureSpecialEffect(e);
				
				bossEntities.put(e.getEntityId(), (BloodBossCreature) c);
				
				for (int i = 0; i < 4; i++)
				{
					LivingEntity e2 = c.l.getWorld().spawnCreature(c.l, c.c);
					
					doBossCreatureSpecialEffect(e);
					
					e.setPassenger(e2);
					
					e = e2;
				}
			}
			else
			{
				LivingEntity e = c.l.getWorld().spawnCreature(c.l, c.c);
				bossEntities.put(e.getEntityId(), (BloodBossCreature) c);
			}
			return;
		}
		
		LivingEntity e = c.l.getWorld().spawnCreature(c.l, c.c);
		
		doCreatureSpecialEffect(e);
		
		if (c.riding != null)
		{
			LivingEntity e2 = c.l.getWorld().spawnCreature(c.l, c.riding);
			doCreatureSpecialEffect(e2);
			
			e.setPassenger(e2);
		}
	}
	
	public void doCreatureSpecialEffect(LivingEntity e)
	{
		Location l = e.getLocation();
		
		if (e instanceof CraftCreeper)
		{
			l.getWorld().strikeLightningEffect(l);
			((CraftCreeper) e).setPowered(true);
		}
		
		if (e instanceof CraftWolf)
		{
			((CraftWolf) e).setAngry(true);
		}
		
		bloodEntities.add(e.getEntityId());
	}
	
	public void doBossCreatureSpecialEffect(LivingEntity e)
	{
		Location l = e.getLocation();
		
		if (e instanceof CraftCreeper)
		{
			l.getWorld().strikeLightningEffect(l);
			((CraftCreeper) e).setPowered(true);
		}
		
		if (e instanceof CraftWolf)
		{
			((CraftWolf) e).setAngry(true);
		}
	}
	
	public void handleEntityDamage(EntityDamageEvent event)
	{
		if (bloodMoons.isEmpty() || !bloodMoons.contains(event.getEntity().getWorld())) return;
		
		if (event.getEntity() instanceof Monster)
		{
			int damage = (int) Math.ceil((double) event.getDamage() / 2);
			int id = event.getEntity().getEntityId();
			
			if (damage <= 0)
			{
				damage = 1;
			}
			event.setDamage(damage);
			
			if (bossEntities.containsKey(id) && event instanceof EntityDamageByEntityEvent)
			{
				EntityDamageByEntityEvent ev = (EntityDamageByEntityEvent) event;
				if (ev.getDamager() instanceof Player)
				{
					bossEntities.get(id).addSlayer((Player) ev.getDamager());
				}
			}
		}
		else if (event.getEntity() instanceof Player)
		{
			Player p = (Player) event.getEntity();
			
			boolean okay = true;
			
			for (ItemStack i : p.getInventory().getArmorContents())
			{
				if (i == null || !i.toString().startsWith("CHAINMAIL"))
				{
					okay = false;
				}
			}
			
			if (okay)
			{
				event.setDamage(event.getDamage() / 2);
			}
		}
	}
	
	public void handleEntityDeath(EntityDeathEvent event)
	{
		if (!(event.getEntity() instanceof Monster)) { return; }
		
		int id = event.getEntity().getEntityId();
		
		World world = event.getEntity().getWorld();
		Location location = event.getEntity().getLocation();
		boolean reward = true;
		
		if (event.getEntity().getLastDamageCause().getCause() != DamageCause.ENTITY_ATTACK)
		{
			reward = false;
		}
		
		if (bloodEntities.contains((Integer) id))
		{
			if (reward)
			{
				int i = random.nextInt(10);
				
				if (i == 0)
				{
					int r = random.nextInt(6);
					
					if (r == 0)
					{
						int x = random.nextInt(4);
						world.dropItem(location, new ItemStack(310 + x, 1));
					}
					else if (r == 1)
					{
						int x = random.nextInt(4);
						world.dropItem(location, new ItemStack(276 + x, 1));
					}
					else if (r == 2)
					{
						int x = random.nextInt(4);
						world.dropItem(location, new ItemStack(307 + x, 1));
					}
					else if (r == 3)
					{
						world.dropItem(location, new ItemStack(262, random.nextInt(64) + 32));
					}
					else if (r == 4)
					{
						world.dropItem(location, new ItemStack(314 + random.nextInt(4), 1));
					}
					else if (r == 5)
					{
						world.dropItem(location, new ItemStack(283 + random.nextInt(4), 1));
					}
					
					world.dropItem(location, new ItemStack(354, 1));
					
				}
				event.setDroppedExp(event.getDroppedExp() * 2);
			}
			
			bloodEntities.remove((Integer) id);
		}
		
		if (bossEntities.containsKey((Integer) id))
		{
			for (int j = 0; j < random.nextInt(5) + 1; j++)
			{
				world.dropItem(location, new ItemStack(Material.GOLDEN_APPLE, 1));
			}
			
			BloodBossCreature c = bossEntities.get((Integer) id);
			
			if (!c.slayers.isEmpty())
			{
				String str = "";
				for (Player player : c.slayers)
				{
					if (str != "") str += ", ";
					str += plugin.getNickName(player);
				}
				
				plugin.announcer.announce(Naxcraft.MSG_COLOR + "A " + c.getBossName() + " has been slain by " + str, event.getEntity().getWorld());
			}
			else
			{
				plugin.announcer.announce(Naxcraft.MSG_COLOR + "A " + c.getBossName() + " has been slain!", event.getEntity().getWorld());
			}
			event.getEntity().getWorld().dropItem(event.getEntity().getLocation(), new ItemStack(302 + random.nextInt(4), 1));
			event.setDroppedExp(event.getDroppedExp() * 5);
		}
		
	}
	
	public void handlePlayerJoin(Player player)
	{
		if (bloodMoons.contains(player.getWorld()))
		{
			player.sendMessage(NaxColor.MSG + "You have signed on during a " + NaxColor.BLOODMOON + "Blood Moon" + NaxColor.MSG + "!");
		}
	}
	
	public class BloodCreature
	{
		public EntityType c;
		public EntityType riding;
		public Location l;
		
		public BloodCreature(EntityType c, EntityType riding, Location l)
		{
			this.c = c;
			this.riding = riding;
			this.l = l;
		}
	}
	
	public class BloodBossCreature extends BloodCreature
	{
		public List<Player> slayers = new ArrayList<Player>();
		
		int id;
		
		public BloodBossCreature(EntityType c, Location l)
		{
			super(c, null, l);
		}
		
		public void addSlayer(Player p)
		{
			if (!slayers.contains(p))
			{
				slayers.add(p);
			}
		}
		
		public String getBossName()
		{
			String name = "";
			
			switch (c)
			{
				case CREEPER:
					name = "Creeper Stack";
					break;
				
				case GIANT:
					name = "Giant";
					break;
				
				case GHAST:
					name = "Ghast";
					break;
				
				default:
					name = "Boss";
					break;
			}
			
			return NaxColor.BLOODMOON + name + NaxColor.MSG;
		}
	}
}
