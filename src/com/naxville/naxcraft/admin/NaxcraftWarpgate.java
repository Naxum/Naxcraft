package com.naxville.naxcraft.admin;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import com.naxville.naxcraft.NaxFile;
import com.naxville.naxcraft.Naxcraft;

public class NaxcraftWarpgate
{
	public Naxcraft plugin;
	public NaxFile config;
	
	public List<NaxGate> gates = new ArrayList<NaxGate>();
	public HashMap<Player, HashMap<NaxGate, Date>> messages = new HashMap<Player, HashMap<NaxGate, Date>>();
	public HashMap<Player, HashMap<NaxGate, Date>> attemptMessages = new HashMap<Player, HashMap<NaxGate, Date>>();
	public HashMap<Player, Date> teleportCooldown = new HashMap<Player, Date>();
	public HashMap<Player, NaxGate> gateWait = new HashMap<Player, NaxGate>();
	
	public int portalMaterial = Material.SUGAR_CANE_BLOCK.getId();
	
	/**
	 * TODO:
	 * - Add functionality for Quest requirements or completions.
	 * - Add Cost functionality.
	 */
	
	protected int timeout = 20; // seconds before next message from a gate is sent.
	protected int cooldown = 5; // seconds after leaving a warpgate before you can warp again
	
	public NaxcraftWarpgate(Naxcraft instance)
	{
		plugin = instance;
	}
	
	public boolean loadWarpGates()
	{
		config = new NaxFile(plugin, "warpgates");
		
		Set<String> list = config.getKeys("warpgates");
		
		for (String id : list)
		{
			String realId = id;
			id = "warpgates." + id;
			
			String name = config.getString(id + ".name");
			String hereName = config.getString(id + ".signText");
			boolean on = config.getBoolean(id + ".on");
			
			if(!config.isConfigurationSection(id + ".world")) continue;
			
			World world = plugin.getServer().getWorld(config.getString(id + ".world"));
			
			if (world == null) continue;
			
			Block signBlock = world.getBlockAt(config.getInt(id + ".sign.x", 0), config.getInt(id + ".sign.y", 0), config.getInt(id + ".sign.z", 0));
			
			List<Block> frame = new ArrayList<Block>();
			for (String block : config.getKeys(id + ".frame"))
			{
				Block frameBlock = world.getBlockAt(config.getInt(id + ".frame." + block + ".x", 0), config.getInt(id + ".frame." + block + ".y", 0), config.getInt(id + ".frame." + block + ".z", 0));
				frame.add(frameBlock);
			}
			
			List<Block> portals = new ArrayList<Block>();
			for (String block : config.getKeys(id + ".portals"))
			{
				Block portalBlock = world.getBlockAt(config.getInt(id + ".portals." + block + ".x", 0), config.getInt(id + ".portals." + block + ".y", 0), config.getInt(id + ".portals." + block + ".z", 0));
				portals.add(portalBlock);
			}
			
			List<String> destinations = config.getStringList(id + ".destinations");
			List<String> whitelist = config.getStringList(id + ".whitelist");
			
			List<ItemStack> costs = new ArrayList<ItemStack>();
			for (String item : config.getKeys(id + ".costs"))
			{
				ItemStack cost = new ItemStack(Material.getMaterial(config.getString(id + ".costs." + item + ".material")), config.getInt(id + ".costs." + item + ".amount", 0));
				
				if (cost.getAmount() == 0) continue;
				costs.add(cost);
				
			}
			
			this.gates.add(new NaxGate(realId, name, hereName, on, signBlock, frame, portals, destinations, costs, whitelist));
			
			// plugin.log.info("Size: " +this.gates.size());
		}
		
		return true;
	}
	
	/**
	 * Returns a name of a gate using its ID.
	 * 
	 * @param gateId
	 *            Gate Id.
	 * @return Gate name.
	 */
	public String getGateName(String gateId)
	{
		NaxGate gate = this.getWarpGate(gateId);
		if (gate != null) { return gate.getName(); }
		return null;
	}
	
	/**
	 * Returns a gate using its ID.
	 * 
	 * @param gateId
	 *            Gate Id.
	 * @return Gate.
	 */
	public NaxGate getWarpGate(String gateId)
	{
		for (NaxGate gate : this.gates)
		{
			if (gate.getId().equalsIgnoreCase(gateId)) return gate;
		}
		return null;
	}
	
	public boolean runCommand(CommandSender sender, String[] args)
	{
		
		if (!(sender instanceof Player)) { return true; }
		
		Player player = (Player) sender;
		
		if (!plugin.playerManager.getPlayer(player).rank.isAdmin()) { return true; }
		
		if (args.length == 0) return false;
		
		if (args[0].equalsIgnoreCase("create") || args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("c"))
		{
			if (args.length == 1)
			{
				player.sendMessage(Naxcraft.COMMAND_COLOR + "/wg c [id] [name] [signText]");
				return true;
			}
			
			if (this.getGateName(args[1].toLowerCase()) != null)
			{
				player.sendMessage(Naxcraft.MSG_COLOR + "A warpgate with that id already exists!");
				return true;
			}
			
			String name = args[1];
			String signText = args[1];
			String id = args[1].toLowerCase();
			
			if (args.length >= 3)
			{
				name = args[2];
				signText = args[2];
			}
			
			if (args.length == 4)
			{
				signText = args[3];
			}
			
			Block block = player.getTargetBlock(null, 150);
			
			if (block.getType() != Material.WALL_SIGN)
			{
				player.sendMessage(Naxcraft.MSG_COLOR + "To define a warpgate, you must be looking at a sign block.");
				return true;
			}
			
			for (NaxGate gate : this.gates)
			{
				if (block.getLocation().getBlockX() != gate.getSign().getLocation().getBlockX()) continue;
				
				if (block.getLocation().getBlockY() != gate.getSign().getLocation().getBlockY()) continue;
				
				if (block.getLocation().getBlockZ() != gate.getSign().getLocation().getBlockZ()) continue;
				
				player.sendMessage(Naxcraft.ERROR_COLOR + "The warp " + gate.getId() + " already is here!");
				return true;
			}
			
			int rotated = 0;
			Block startBlock;
			
			if (block.getRelative(BlockFace.NORTH).getType() == Material.OBSIDIAN)
			{
				startBlock = block.getRelative(BlockFace.NORTH);
				rotated = 0;
				
			}
			else if (block.getRelative(BlockFace.SOUTH).getType() == Material.OBSIDIAN)
			{
				startBlock = block.getRelative(BlockFace.SOUTH);
				rotated = 0;
				
			}
			else if (block.getRelative(BlockFace.EAST).getType() == Material.OBSIDIAN)
			{
				startBlock = block.getRelative(BlockFace.EAST);
				rotated = 1;
				
			}
			else if (block.getRelative(BlockFace.WEST).getType() == Material.OBSIDIAN)
			{
				startBlock = block.getRelative(BlockFace.WEST);
				rotated = 1;
				
			}
			else
			{
				player.sendMessage(Naxcraft.MSG_COLOR + "There is no obsidian blocks here for a warp gate.");
				return true;
			}
			
			List<Block> frame = new ArrayList<Block>();
			List<Block> portals = new ArrayList<Block>();
			
			for (int i = -1; i < 2; i++)
			{
				for (int y = 0; y < 3; y++)
				{
					if (y == 1 && i == 0) continue;
					if (y == 2 && i == 0) continue;
					
					int x = i;
					int z = i;
					if (rotated == 0) x = 0;
					if (rotated == 1) z = 0;
					
					if (startBlock.getRelative(x, -y, z).getType() != Material.OBSIDIAN)
					{
						player.sendMessage(Naxcraft.ERROR_COLOR + "That is not a correct gate design. (" + x + ", " + y + ", " + z + ") " + startBlock.getRelative(x, -y, z).getType().toString());
						return true;
					}
					
					frame.add(startBlock.getRelative(x, -y, z));
				}
			}
			
			startBlock.getRelative(0, -1, 0).setTypeIdAndData(Material.SUGAR_CANE_BLOCK.getId(), (byte) 0, false);
			startBlock.getRelative(0, -2, 0).setTypeIdAndData(Material.SUGAR_CANE_BLOCK.getId(), (byte) 0, false);
			
			portals.add(startBlock.getRelative(0, -1, 0));
			portals.add(startBlock.getRelative(0, -2, 0));
			
			Sign sign = (Sign) block.getState();
			sign.setLine(1, ChatColor.AQUA + "[WarpGate]");
			sign.setLine(2, signText);
			sign.update();
			
			this.gates.add(new NaxGate(id, name, signText, block, frame, portals));
			player.sendMessage(Naxcraft.MSG_COLOR + "Warp " + name + Naxcraft.SUCCESS_COLOR + " successfully " + Naxcraft.MSG_COLOR + " created!");
			
		}
		else if (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("d") || args[0].equalsIgnoreCase("remove"))
		{
			if (args.length < 2) return false;
			
			NaxGate gate = this.getWarpGate(args[1].toLowerCase());
			
			if (gate != null)
			{
				
				gate.delete();
				this.gates.remove(gate);
				
				player.sendMessage(Naxcraft.MSG_COLOR + "Warpgate " + Naxcraft.SUCCESS_COLOR + " successfully " + Naxcraft.MSG_COLOR + "deleted.");
				
			}
			else
			{
				player.sendMessage(Naxcraft.MSG_COLOR + args[1] + " does " + Naxcraft.ERROR_COLOR + "not" + Naxcraft.MSG_COLOR + " exist.");
				return true;
			}
			
		}
		else if (args[0].equalsIgnoreCase("multilink") || args[0].equalsIgnoreCase("ml") || args[0].equalsIgnoreCase("duallink") || args[0].equalsIgnoreCase("dl"))
		{
			if (args.length < 3)
			{
				player.sendMessage(Naxcraft.COMMAND_COLOR + "/wg ml [id] [id2] [id3...]");
				return true;
			}
			
			args[0] = "";
			
			for (String id : args)
			{
				if (id == "") continue;
				
				if (this.getWarpGate(id) == null)
				{
					player.sendMessage(Naxcraft.MSG_COLOR + id + " is " + Naxcraft.ERROR_COLOR + "not" + Naxcraft.MSG_COLOR + " a warpgate ID.");
					continue;
				}
				
				for (String id2 : args)
				{
					if (id == id2) continue;
					if (id2 == "") continue;
					
					if (this.getWarpGate(id2) == null) continue;
					
					this.getWarpGate(id).addDestination(id2);
				}
			}
			player.sendMessage(Naxcraft.MSG_COLOR + "Warps should be linked.");
			
		}
		else if (args[0].equalsIgnoreCase("link") || args[0].equalsIgnoreCase("l"))
		{
			if (args.length != 3)
			{
				player.sendMessage(Naxcraft.COMMAND_COLOR + "/wg link [first gate] [second gate]");
				player.sendMessage(Naxcraft.COMMAND_COLOR + "Makes [first gate] go to [second gate].");
				return true;
			}
			
			NaxGate gate1 = this.getWarpGate(args[1].toLowerCase());
			NaxGate gate2 = this.getWarpGate(args[2].toLowerCase());
			
			if (gate1 == null)
			{
				player.sendMessage(Naxcraft.MSG_COLOR + args[1] + " is " + Naxcraft.ERROR_COLOR + "not" + Naxcraft.MSG_COLOR + " a warpgate ID.");
				return true;
			}
			
			if (gate2 == null)
			{
				player.sendMessage(Naxcraft.MSG_COLOR + args[2] + " is " + Naxcraft.ERROR_COLOR + "not" + Naxcraft.MSG_COLOR + " a warpgate ID.");
				return true;
			}
			
			gate1.addDestination(gate2.getId());
			player.sendMessage(Naxcraft.SUCCESS_COLOR + "Gate " + gate1.getName() + " successfully linked to " + gate2.getName() + ".");
			
		}
		else if (args[0].equalsIgnoreCase("clear"))
		{
			if (args.length != 2)
			{
				player.sendMessage(Naxcraft.COMMAND_COLOR + "/wg clear [id] - Clears all destinations of that gate.");
				return true;
			}
			
			NaxGate gate = this.getWarpGate(args[1].toLowerCase());
			
			if (gate == null)
			{
				player.sendMessage(Naxcraft.ERROR_COLOR + args[1] + " is not a warpgate ID.");
				return true;
			}
			
			gate.clear();
			player.sendMessage(Naxcraft.SUCCESS_COLOR + "Gate cleared of all destinations.");
		}
		else if (args[0].equalsIgnoreCase("unlink") || args[0].equalsIgnoreCase("ul"))
		{
			if (args.length < 3)
			{
				player.sendMessage(Naxcraft.COMMAND_COLOR + "/wg ul [gate] [destination 1] [dest 2...]");
				return true;
			}
			
			String id = args[1].toLowerCase();
			NaxGate gate = this.getWarpGate(id);
			
			args[0] = "";
			args[1] = "";
			
			if (gate == null)
			{
				player.sendMessage(Naxcraft.ERROR_COLOR + id + " is not a gate ID!");
			}
			
			for (String id2 : args)
			{
				if (id2 == "") continue;
				
				NaxGate gate2 = this.getWarpGate(id2);
				
				if (gate2 == null)
				{
					player.sendMessage(Naxcraft.ERROR_COLOR + id + " is not a gate ID!");
				}
				
				gate.removeDestination(id2);
			}
			
			player.sendMessage(Naxcraft.SUCCESS_COLOR + "Gates unlinked from " + id + "!");
			
		}
		else if (args[0].equalsIgnoreCase("id") || args[0].equalsIgnoreCase("identify"))
		{
			NaxGate gate = null;
			
			if (args.length == 1)
			{
				Block block = player.getTargetBlock(null, 150);
				
				if (!(block.getState() instanceof Sign))
				{
					player.sendMessage(Naxcraft.COMMAND_COLOR + "/wg id [look at sign] (or) [id]");
					return true;
				}
				
				for (NaxGate gate2 : this.gates)
				{
					if (block.getLocation().getBlockX() != gate2.getSign().getLocation().getBlockX()) continue;
					
					if (block.getLocation().getBlockY() != gate2.getSign().getLocation().getBlockY()) continue;
					
					if (block.getLocation().getBlockZ() != gate2.getSign().getLocation().getBlockZ()) continue;
					
					gate = gate2;
				}
				
				if (gate == null)
				{
					player.sendMessage(Naxcraft.MSG_COLOR + "That is not a warpgate.");
					return true;
				}
			}
			else if (args.length == 2)
			{
				
				NaxGate gate2 = this.getWarpGate(args[1].toLowerCase());
				
				if (gate2 == null)
				{
					player.sendMessage(Naxcraft.MSG_COLOR + args[1].toLowerCase() + " is not a warpgate ID.");
					return true;
					
				}
				else
				{
					gate = gate2;
				}
				
			}
			else
			{
				player.sendMessage(Naxcraft.COMMAND_COLOR + "/wg id [look at sign] (or) [id]");
				return true;
			}
			
			String msg = Naxcraft.MSG_COLOR + "WarpGate Information: ";
			msg += "id: " + gate.getId() + ", name: " + gate.getName() + ". ";
			
			if (gate.on) msg += Naxcraft.SUCCESS_COLOR + "Is on. ";
			else msg += Naxcraft.ERROR_COLOR + "Is off. ";
			
			msg += Naxcraft.MSG_COLOR + "Destinations: ";
			
			int destCount = 0;
			for (String dest : gate.getDestinations())
			{
				if (destCount != 0) msg += ", ";
				msg += dest;
				
				destCount++;
			}
			
			if (destCount == 0) msg += "None.";
			
			msg += " Costs: ";
			
			int costCount = 0;
			for (ItemStack item : gate.getCosts())
			{
				if (costCount != 0) msg += ", ";
				msg += item.getAmount() + " " + item.getType().toString().toLowerCase().replace("_", " ");
				
				costCount++;
			}
			if (costCount == 0) msg += "None.";
			
			msg += "WhiteList: " + Naxcraft.DEFAULT_COLOR;
			int whiteListCount = 0;
			for (String name : gate.getWhiteList())
			{
				if (whiteListCount != 0) msg += Naxcraft.MSG_COLOR + ", " + Naxcraft.DEFAULT_COLOR;
				msg += name;
				
				whiteListCount++;
			}
			
			player.sendMessage("");
			player.sendMessage(msg);
			
		}
		else if (args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("off"))
		{
			if (args.length != 2)
			{
				player.sendMessage(Naxcraft.COMMAND_COLOR + "/wg [on/off] [id]");
				return true;
			}
			
			NaxGate gate = this.getWarpGate(args[1].toLowerCase());
			if (gate == null)
			{
				player.sendMessage(Naxcraft.ERROR_COLOR + args[1] + " is not a warpgate ID.");
				return true;
			}
			
			boolean onOff = false;
			if (args[0].equals("on")) onOff = true;
			
			gate.setOnOff(onOff);
			player.sendMessage(Naxcraft.SUCCESS_COLOR + "Gate turned " + args[0] + "!");
			
		}
		else if (args[0].equalsIgnoreCase("wl") || args[0].equalsIgnoreCase("whitelist"))
		{
			if (args.length < 4)
			{
				player.sendMessage(Naxcraft.COMMAND_COLOR + "/wg wl <gate id> <add/remove> <name1> [name2...]");
				return true;
			}
			
			String id = args[1].toLowerCase();
			String addRemove = args[2];
			NaxGate gate = this.getWarpGate(id);
			
			args[0] = "";
			args[1] = "";
			args[2] = "";
			
			if (addRemove.equalsIgnoreCase("add") || addRemove.equalsIgnoreCase("a") || addRemove.equalsIgnoreCase("r") || addRemove.equalsIgnoreCase("remove") || addRemove.equalsIgnoreCase("d") || addRemove.equalsIgnoreCase("del") || addRemove.equalsIgnoreCase("delete"))
			{
				if (addRemove.equalsIgnoreCase("add") || addRemove.equalsIgnoreCase("a"))
				{
					addRemove = "add";
				}
				else
				{
					addRemove = "remove";
				}
			}
			else
			{
				player.sendMessage(Naxcraft.COMMAND_COLOR + "/wg <gate id> wl <add/remove> <name1> [name2...]");
				return true;
			}
			
			if (gate == null)
			{
				player.sendMessage(Naxcraft.ERROR_COLOR + id + " is not a gate ID!");
			}
			
			for (String name : args)
			{
				if (name == "") continue;
				
				if (addRemove.equals("add"))
				{
					gate.addWhiteListMember(name);
				}
				else
				{
					gate.removeWhiteListMember(name);
				}
			}
			
			player.sendMessage(Naxcraft.SUCCESS_COLOR + "Gate " + id + "'s whitelist has been altered!");
			
		}
		else if (args[0].equalsIgnoreCase("rename"))
		{
			if (args.length != 3)
			{
				player.sendMessage(Naxcraft.COMMAND_COLOR + "/wg rename [id] [new name]");
				return true;
			}
			
			NaxGate gate = this.getWarpGate(args[1].toLowerCase());
			
			if (gate == null)
			{
				player.sendMessage(Naxcraft.ERROR_COLOR + args[1] + " is not a warpgate ID.");
				return true;
			}
			
			gate.rename(args[2]);
			player.sendMessage(Naxcraft.SUCCESS_COLOR + "Warp renamed!");
			
		}
		else if (args[0].equalsIgnoreCase("sign"))
		{
			if (args.length != 3)
			{
				player.sendMessage(Naxcraft.COMMAND_COLOR + "/wg sign [id] [text]");
				return true;
			}
			
			NaxGate gate = this.getWarpGate(args[1].toLowerCase());
			
			if (gate == null)
			{
				player.sendMessage(Naxcraft.ERROR_COLOR + args[1] + " is not a warpgate ID.");
				return true;
			}
			
			gate.setSignText(args[2]);
			player.sendMessage(Naxcraft.SUCCESS_COLOR + "Warp sign changed!");
			
		}
		else
		{
			return false;
		}
		
		return true;
	}
	
	public void handleMove(PlayerMoveEvent event)
	{
		if (this.gateWait.containsKey(event.getPlayer()))
		{
			
			boolean outside = true;
			for (Block block : this.gateWait.get(event.getPlayer()).getPortals())
			{
				if ((block.getLocation().getBlockX() == event.getPlayer().getLocation().getBlockX()) &&
						(block.getLocation().getBlockY() == event.getPlayer().getLocation().getBlockY()) &&
						(block.getLocation().getBlockZ() == event.getPlayer().getLocation().getBlockZ()))
				{
					outside = false;
				}
			}
			
			if (outside)
			{
				this.gateWait.remove(event.getPlayer());
				this.teleportCooldown.put(event.getPlayer(), new Date());
				
			}
			else
			{
				return;
			}
		}
		
		if (this.teleportCooldown.containsKey(event.getPlayer()))
		{
			
			if (new Date().getTime() - this.teleportCooldown.get(event.getPlayer()).getTime() >= this.cooldown * 1000)
			{
				this.teleportCooldown.remove(event.getPlayer());
				
			}
			else
			{
				return;
			}
		}
		
		for (NaxGate gate : this.gates)
		{
			gate.handleTeleport(event);
		}
	}
	
	public void handleBreak(BlockBreakEvent event)
	{
		if (this.handleChange(event.getBlock()))
		{
			event.getPlayer().sendMessage(Naxcraft.ERROR_COLOR + "Stop trying to break the warpgate.");
			event.setCancelled(true);
		}
	}
	
	public void handlePhysics(BlockPhysicsEvent event)
	{
		if (this.handleChange(event.getBlock())) event.setCancelled(true);
	}
	
	public boolean handleChange(Block eventBlock)
	{
		for (NaxGate gate : this.gates)
		{
			for (Block block : gate.getPortals())
			{
				if (eventBlock.getWorld().getName() != block.getLocation().getWorld().getName()) continue;
				
				if (eventBlock.getLocation().getX() != block.getLocation().getX()) continue;
				
				if (eventBlock.getLocation().getY() != block.getLocation().getY()) continue;
				
				if (eventBlock.getLocation().getZ() != block.getLocation().getZ()) continue;
				
				return true;
			}
			
			for (Block block : gate.getFrame())
			{
				if (eventBlock.getWorld().getName() != block.getLocation().getWorld().getName()) continue;
				
				if (eventBlock.getLocation().getX() != block.getLocation().getX()) continue;
				
				if (eventBlock.getLocation().getY() != block.getLocation().getY()) continue;
				
				if (eventBlock.getLocation().getZ() != block.getLocation().getZ()) continue;
				
				return true;
			}
			
			if (eventBlock.getWorld().getName() != gate.getSign().getLocation().getWorld().getName()) continue;
			
			if (eventBlock.getLocation().getX() != gate.getSign().getLocation().getX()) continue;
			
			if (eventBlock.getLocation().getY() != gate.getSign().getLocation().getY()) continue;
			
			if (eventBlock.getLocation().getZ() != gate.getSign().getLocation().getZ()) continue;
			
			return true;
		}
		
		return false;
	}
	
	public class NaxGate
	{
		/**
		 * Creates a Naxcraft Warp Gate
		 * 
		 * @param name
		 *            The name of the gate.
		 * @param sign
		 *            The block of the sign used to create it.
		 * @param frame
		 *            List of frame blocks
		 * @param portals
		 *            List of portal blocks
		 */
		protected Block signBlock;
		protected List<Block> frameBlocks;
		protected List<Block> portalBlocks;
		protected Location landingLocation;
		
		protected String name;
		protected String signText;
		protected String id;
		protected boolean on = true;
		protected List<String> whiteList = new ArrayList<String>();
		protected List<String> destinations = new ArrayList<String>();
		protected List<ItemStack> cost = new ArrayList<ItemStack>();
		
		// TODO: NAXQUESTS, AND STEPS?
		
		public NaxGate(String id, String name, String hereName, Block sign, List<Block> frame, List<Block> portals)
		{
			this.name = name;
			this.signText = hereName;
			this.id = id;
			this.signBlock = sign;
			this.frameBlocks = frame;
			this.portalBlocks = portals;
			this.landingLocation = getBottomMostPortalBlock();
			
			this.save();
		}
		
		// for loading
		public NaxGate(String id, String name, String hereName, boolean on, Block signBlock,
				List<Block> frame, List<Block> portals,
				List<String> destinations, List<ItemStack> costs, List<String> whitelist)
		{
			this.name = name;
			this.signText = hereName;
			this.id = id;
			this.on = on;
			this.signBlock = signBlock;
			this.frameBlocks = frame;
			this.portalBlocks = portals;
			this.destinations = destinations;
			this.landingLocation = getBottomMostPortalBlock();
			this.whiteList = whitelist;
			this.cost = costs;
			
			checkPortalBlocks();
		}
		
		private void checkPortalBlocks()
		{
			for (Block e : portalBlocks)
			{
				if (on && e.getType().getId() != portalMaterial)
				{
					e.setTypeIdAndData(portalMaterial, (byte) 0, false);
				}
				else if (!on && e.getType().getId() != 0)
				{
					e.setType(Material.AIR);
				}
			}
		}
		
		private Location getBottomMostPortalBlock()
		{
			Block lowestY = null;
			for (Block portal : this.portalBlocks)
			{
				if (lowestY == null) lowestY = portal;
				
				if (portal.getLocation().getBlockY() < lowestY.getLocation().getBlockY()) lowestY = portal;
			}
			
			int xDir = this.signBlock.getLocation().getBlockX() - lowestY.getLocation().getBlockX();
			int zDir = this.signBlock.getLocation().getBlockZ() - lowestY.getLocation().getBlockZ();
			
			float direction = 0;
			
			if (xDir > 0)
			{
				// north
				direction = 270;
				
			}
			else if (xDir == 0)
			{
				
				if (zDir > 0)
				{
					// west
					direction = 0;
					
				}
				else
				{
					// east
					direction = 180;
				}
			}
			else
			{
				// south
				direction = 90;
			}
			
			return new Location(lowestY.getWorld(), lowestY.getLocation().getBlockX() + 0.5, lowestY.getLocation().getBlockY(), lowestY.getLocation().getBlockZ() + 0.5, direction, (float) 0);
		}
		
		public Block getSign()
		{
			return this.signBlock;
		}
		
		public List<Block> getFrame()
		{
			return this.frameBlocks;
		}
		
		public List<Block> getPortals()
		{
			return this.portalBlocks;
		}
		
		public String getName()
		{
			return this.name;
		}
		
		public String getId()
		{
			return this.id;
		}
		
		public Location getLandingLocation()
		{
			return this.landingLocation;
		}
		
		public List<String> getDestinations()
		{
			return this.destinations;
		}
		
		public List<ItemStack> getCosts()
		{
			return this.cost;
		}
		
		public List<String> getWhiteList()
		{
			return this.whiteList;
		}
		
		public void addWhiteListMember(String name)
		{
			name = name.toLowerCase();
			
			if (!this.whiteList.contains(name)) this.whiteList.add(name);
			
			this.save();
		}
		
		public void removeWhiteListMember(String name)
		{
			name = name.toLowerCase();
			
			if (this.whiteList.contains(name)) this.whiteList.remove(name);
			
			this.save();
		}
		
		public void addDestination(String id)
		{
			this.destinations.add(id);
			this.save();
		}
		
		public void removeDestination(String id)
		{
			if (this.destinations.contains(id)) this.destinations.remove(id);
			
			this.save();
		}
		
		public void rename(String name)
		{
			this.name = name;
			this.save();
		}
		
		public void setSignText(String text)
		{
			this.signText = text;
			Sign sign = (Sign) this.signBlock.getState();
			sign.setLine(1, ChatColor.AQUA + "[WarpGate]");
			sign.setLine(2, text);
			sign.update();
			
			this.save();
		}
		
		public void clear()
		{
			this.destinations.clear();
			this.save();
		}
		
		public void setOnOff(boolean onOff)
		{
			if (onOff)
			{
				if (this.on)
				{
					this.on = true;
					for (Block block : this.portalBlocks)
					{
						// TODO: SET DIRECTION
						block.setTypeIdAndData(portalMaterial, (byte) 0, false);
					}
				}
				else
				{
					this.on = true;
					for (Block block : this.portalBlocks)
					{
						// TODO: SET DIRECTION
						block.setTypeIdAndData(portalMaterial, (byte) 0, false);
					}
					this.save();
				}
			}
			else
			{
				if (!this.on)
				{
					this.on = false;
					
					for (Block block : this.portalBlocks)
					{
						block.setType(Material.AIR);
					}
				}
				else
				{
					this.on = false;
					
					for (Block block : this.portalBlocks)
					{
						block.setType(Material.AIR);
					}
					this.save();
				}
			}
		}
		
		public boolean withinDistance(Player player, int distance)
		{
			if (player.getWorld().equals(this.getSign().getWorld()))
			{
				if (player.getLocation().distanceSquared(this.getSign().getLocation()) <= distance) return true;
			}
			return false;
		}
		
		public void handleMsg(Player player)
		{
			if (gateWait.containsKey(player) || teleportCooldown.containsKey(player)) return;
			
			if (messages.containsKey(player))
			{
				if (messages.get(player).containsKey(this))
				{
					if (new Date().getTime() - messages.get(player).get(this).getTime() > timeout * 1000)
					{
						messages.get(player).remove(this);
						messages.get(player).put(this, new Date());
						
						player.sendMessage(getMsg());
					}
				}
				else
				{
					messages.get(player).put(this, new Date());
					player.sendMessage(getMsg());
				}
			}
			else
			{
				HashMap<NaxGate, Date> temp = new HashMap<NaxGate, Date>();
				temp.put(this, new Date());
				messages.put(player, temp);
				
				player.sendMessage(getMsg());
			}
		}
		
		public void handleAttemptMsg(Player player, String msg)
		{
			if (attemptMessages.containsKey(player))
			{
				if (attemptMessages.get(player).containsKey(this))
				{
					if (new Date().getTime() - attemptMessages.get(player).get(this).getTime() > timeout * 1000)
					{
						attemptMessages.get(player).remove(this);
						attemptMessages.get(player).put(this, new Date());
						
						player.sendMessage(msg);
					}
				}
				else
				{
					attemptMessages.get(player).put(this, new Date());
					player.sendMessage(msg);
				}
			}
			else
			{
				HashMap<NaxGate, Date> temp = new HashMap<NaxGate, Date>();
				temp.put(this, new Date());
				attemptMessages.put(player, temp);
				
				player.sendMessage(msg);
			}
		}
		
		public String getMsg()
		{
			String msg = "This warp gate";
			
			if (this.destinations.size() == 0) msg += " has no destination";
			else
			{
				msg += " goes to";
				int i = 0;
				for (String gate : this.destinations)
				{
					if (getWarpGate(gate) == null)
					{
						this.removeDestination(gate);
						continue;
					}
					
					if (i != 0) msg += ",";
					msg += " " + getGateName(gate);
					i++;
				}
			}
			
			if (this.cost.size() > 0)
			{
				msg += "and costs ";
				int i = 0;
				for (ItemStack price : this.cost)
				{
					if (i != 0) msg += ",";
					msg += price.getAmount() + " " + price.getType().toString().toLowerCase().replace("_", " ") + " ";
					i++;
				}
				msg += "to use";
			}
			msg += ".";
			
			if (!this.whiteList.isEmpty())
			{
				msg += " Authorized users: " + Naxcraft.DEFAULT_COLOR;
				
				int whiteListCount = 0;
				for (String name : this.whiteList)
				{
					if (whiteListCount != 0) msg += Naxcraft.MSG_COLOR + ", " + Naxcraft.DEFAULT_COLOR;
					msg += name;
					
					whiteListCount++;
				}
				msg += Naxcraft.MSG_COLOR + ".";
			}
			
			if (!this.on) msg = "This warp gate is inactive.";
			return Naxcraft.MSG_COLOR + msg;
		}
		
		public void handleTeleport(PlayerMoveEvent event)
		{
			if (withinDistance(event.getPlayer(), 3 * 3))
			{
				handleMsg(event.getPlayer());
				
				if (gateWait.containsKey(event.getPlayer()) || teleportCooldown.containsKey(event.getPlayer())) return;
				
				for (Block portal : this.portalBlocks)
				{
					if (!(event.getPlayer().getLocation().getWorld().getName().equals(portal.getLocation().getWorld().getName()))) continue;
					
					if (event.getPlayer().getLocation().getBlockX() - portal.getLocation().getBlockX() != 0) continue;
					
					if (event.getPlayer().getLocation().getBlockY() - portal.getLocation().getBlockY() != 0) continue;
					
					if (event.getPlayer().getLocation().getBlockZ() - portal.getLocation().getBlockZ() != 0) continue;
					
					this.teleport(event);
				}
			}
		}
		
		protected void teleport(PlayerMoveEvent event)
		{
			if (!this.on) return;
			
			if (this.destinations.isEmpty())
			{
				this.handleAttemptMsg(event.getPlayer(), Naxcraft.MSG_COLOR + "This warp won't take you anywhere.");
				return;
			}
			
			if (!this.whiteList.isEmpty())
			{
				if (!this.withinWhiteList(event.getPlayer()))
				{
					if (!SuperManager.isSuper(event.getPlayer()))
					{
						this.handleAttemptMsg(event.getPlayer(), Naxcraft.MSG_COLOR + "You are not authorized to use this gate.");
						return;
					}
				}
			}
			
			String successMessage = "";
			
			if (!this.cost.isEmpty())
			{
				boolean enoughMoney = false;
				List<Integer> slotsToDestroy = new ArrayList<Integer>();
				
				String errorMessage = "";
				
				for (ItemStack price : this.cost)
				{
					Material type = price.getType();
					int priceleft = price.getAmount();
					
					for (int slot : event.getPlayer().getInventory().all(type.getId()).keySet())
					{
						priceleft -= event.getPlayer().getInventory().getItem(slot).getAmount();
						if (priceleft > 0)
						{
							slotsToDestroy.add(slot);
							
						}
						else if (priceleft == 0)
						{
							slotsToDestroy.add(slot);
							enoughMoney = true;
							break;
							
						}
						else if (priceleft < 0)
						{
							slotsToDestroy.add(slot);
							enoughMoney = true;
							break;
						}
					}
					
					if (enoughMoney)
					{
						for (int slot : slotsToDestroy)
						{
							event.getPlayer().getInventory().setItem(slot, null);
						}
						if (priceleft < 0)
						{
							event.getPlayer().getInventory().addItem(new ItemStack(type, priceleft * -1));
						}
						
						if (!successMessage.equals("")) successMessage += ", ";
						successMessage += price.getAmount() + " " + price.getType().toString().toLowerCase().replace("_", " ");
						
					}
					else
					{
						if (!errorMessage.equals("")) successMessage += ", ";
						errorMessage += price.getAmount() + " " + price.getType().toString().toLowerCase().replace("_", " ");
					}
				}
				
				if (!errorMessage.equals(""))
				{
					this.handleAttemptMsg(event.getPlayer(), Naxcraft.MSG_COLOR + "You may not use this warp, you are short " + errorMessage);
					return;
				}
			}
			
			Random rand = new Random();
			int dest = rand.nextInt(this.destinations.size());
			
			gateWait.put(event.getPlayer(), getWarpGate(this.destinations.get(dest)));
			
			// event.setFrom(getWarpGate(this.destinations.get(dest)).getLandingLocation());
			// event.setTo(getWarpGate(this.destinations.get(dest)).getLandingLocation());
			
			event.setTo(getWarpGate(this.destinations.get(dest)).getLandingLocation());
			
			// event.getPlayer().sendMessage(Naxcraft.SUCCESS_COLOR + "You have successfully warped! " + successMessage);
		}
		
		private boolean withinWhiteList(Player player)
		{
			for (String name : this.whiteList)
			{
				if (player.getName().equalsIgnoreCase(name)) return true;
			}
			
			return false;
		}
		
		public void save()
		{
			String id = "warpgates." + this.id;
			
			config.set(id + ".name", this.name);
			config.set(id + ".signText", this.signText);
			config.set(id + ".world", this.signBlock.getWorld().getName());
			config.set(id + ".on", this.on);
			
			config.set(id + ".sign.x", this.signBlock.getLocation().getBlockX());
			config.set(id + ".sign.y", this.signBlock.getLocation().getBlockY());
			config.set(id + ".sign.z", this.signBlock.getLocation().getBlockZ());
			
			int i = 0;
			for (Block frame : this.frameBlocks)
			{
				config.set(id + ".frame." + i + ".x", frame.getLocation().getBlockX());
				config.set(id + ".frame." + i + ".y", frame.getLocation().getBlockY());
				config.set(id + ".frame." + i + ".z", frame.getLocation().getBlockZ());
				i++;
			}
			
			i = 0;
			for (Block portal : this.portalBlocks)
			{
				config.set(id + ".portals." + i + ".x", portal.getLocation().getBlockX());
				config.set(id + ".portals." + i + ".y", portal.getLocation().getBlockY());
				config.set(id + ".portals." + i + ".z", portal.getLocation().getBlockZ());
				i++;
			}
			
			config.set(id + ".destinations", this.destinations);
			config.set(id + ".whitelist", this.whiteList);
			
			i = 0;
			if (this.cost.isEmpty())
			{
				List<ItemStack> temp = new ArrayList<ItemStack>();
				temp.add(new ItemStack(Material.AIR, 0));
				config.set(id + ".costs.0.material", temp.get(0).getType().name());
				config.set(id + ".costs.0.amount", temp.get(0).getAmount());
				
			}
			else
			{
				for (ItemStack item : this.cost)
				{
					config.set(id + ".costs." + i + ".material", item.getType().name());
					config.set(id + ".costs." + i + ".amount", item.getAmount());
					i++;
				}
			}
			
			config.save();
		}
		
		public void delete()
		{
			config.removeProperty("warpgates." + this.id);
			config.save();
		}
	}
}
