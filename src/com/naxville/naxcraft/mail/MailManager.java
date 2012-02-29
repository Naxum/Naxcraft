package com.naxville.naxcraft.mail;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.TrapDoor;

import com.naxville.naxcraft.NaxColor;
import com.naxville.naxcraft.NaxFile;
import com.naxville.naxcraft.NaxUtil;
import com.naxville.naxcraft.Naxcraft;
import com.naxville.naxcraft.admin.SuperManager;
import com.naxville.naxcraft.autoareas.AutoBase;
import com.naxville.naxcraft.player.NaxPlayer;

public class MailManager
{
	public Naxcraft plugin;
	public NaxFile config;
	public static final String FILE_NAME = "mail.yml";
	
	public Map<String, MailAccount> loadedAccounts = new HashMap<String, MailAccount>();
	public Map<Player, MailMessage> drafts = new HashMap<Player, MailMessage>();
	public Map<Player, Location> reading = new HashMap<Player, Location>();
	
	public int INBOX_MAX = 8;
	public int MAIL_CHAR_LIMIT = 400;
	public int MAIL_TAX_BASE = 4;
	public Material MAIL_TAX = Material.IRON_INGOT;
	public int MAIL_TAX_RATE = 10; // Amount / rate = additional tax
	
	public MailManager(Naxcraft plugin)
	{
		this.plugin = plugin;
	}
	
	public void runMailCommand(Player p, String[] args)
	{
		if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("help")))
		{
			printHelp(p);
			return;
		}
		
		if (p.getGameMode() == GameMode.CREATIVE && !SuperManager.isSuper(p))
		{
			p.sendMessage(NaxColor.MSG + "You cannot use mail while in creative mode.");
			return;
		}
		
		String cmd = args[0];
		
		p.sendMessage("");
		
		if (cmd.equalsIgnoreCase("compose"))
		{
			if (!canWriteMessage(p)) { return; }
			if (args.length != 2)
			{
				printHelp(p);
				return;
			}
			
			drafts.put(p, new MailMessage(args[1], null, p));
			p.sendMessage(NaxColor.MSG + "Your " + getDraftName(p) + " draft has been started!");
		}
		
		if (cmd.equalsIgnoreCase("target"))
		{
			if (!hasDraft(p)) return;
			if (args.length != 2)
			{
				printHelp(p);
				return;
			}
			
			NaxPlayer np = plugin.playerManager.getPlayer(args[1]);
			NaxPlayer npp = plugin.playerManager.getPlayer(p);
			
			if ((npp.ignored != null && npp.ignored.contains(np.name)) || (np.ignored != null && np.ignored.contains(npp.name)))
			{
				p.sendMessage(NaxColor.ERR + "You cannot send mail to someone that has ignored you.");
				return;
			}
			
			String target = null;
			if (np != null) target = np.name;
			
			if (target == null)
			{
				p.sendMessage(NaxColor.MSG + "The player " + args[1] + " does not exist.");
			}
			else
			{
				drafts.get(p).target = target;
				p.sendMessage(NaxColor.MSG + "Your draft " + getDraftName(p) + " will now be sent to " + plugin.getNickName(target));
			}
		}
		
		if (cmd.equalsIgnoreCase("add"))
		{
			if (!hasDraft(p)) return;
			if (args.length == 1)
			{
				printHelp(p);
				return;
			}
			
			args[0] = "";
			
			String text = NaxUtil.arrayToString(args);
			String message = drafts.get(p).text;
			
			if (message == null) message = "";
			
			if (message != "") message += " ";
			
			if ((message + text).length() > MAIL_CHAR_LIMIT)
			{
				p.sendMessage(NaxColor.MSG + "Your message can only be " + MAIL_CHAR_LIMIT + " characters long");
				return;
			}
			
			message += text;
			
			drafts.get(p).text = message;
			
			p.sendMessage(NaxColor.MSG + "Your draft " + getDraftName(p) + " now contains the text: " + NaxColor.WHITE + message + NaxColor.MSG + " : And has " + NaxColor.WHITE +
					(MAIL_CHAR_LIMIT - message.length()) + NaxColor.MSG + " characters left");
		}
		
		if (cmd.equalsIgnoreCase("attach"))
		{
			if (!hasDraft(p)) return;
			
			MailMessage m = drafts.get(p);
			
			if (m.item != null)
			{
				p.sendMessage(NaxColor.MSG + "You must first detach the current attachment before adding a new item to the draft.");
				return;
			}
			
			ItemStack i = p.getItemInHand();
			if (i == null || i.getAmount() == 0)
			{
				p.sendMessage(NaxColor.MSG + "When you use this command you use the item in your hand. There is nothing in your hand.");
				return;
			}
			else
			{
				m.item = i.clone();
				p.getInventory().removeItem(i);
				p.sendMessage(NaxColor.MSG + "Your draft " + getDraftName(p) + " now contains " + NaxColor.WHITE + m.item.getAmount() + " " + m.item.getType().toString() + NaxColor.MSG + " which will cost you " + NaxColor.WHITE + getTaxPrice(i) + " " + MAIL_TAX
						.toString() + NaxColor.MSG + " to ship.");
			}
		}
		
		if (cmd.equalsIgnoreCase("detach"))
		{
			if (!hasDraft(p)) return;
			
			MailMessage m = drafts.get(p);
			
			if (m.item == null)
			{
				p.sendMessage(NaxColor.MSG + "Your draft does not have an attachment to remove.");
				return;
			}
			
			HashMap<Integer, ItemStack> overflow = p.getInventory().addItem(m.item.clone());
			
			if (!overflow.isEmpty())
			{
				for (ItemStack i : overflow.values())
				{
					p.getWorld().dropItemNaturally(p.getLocation(), i);
				}
			}
			
			m.item = null;
			
			p.sendMessage(NaxColor.MSG + "You removed the attachment from your " + getDraftName(p) + " draft");
		}
		
		if (cmd.equalsIgnoreCase("check"))
		{
			if (!hasDraft(p)) return;
			p.sendMessage(NaxColor.MSG + "----");
			p.sendMessage(NaxColor.MSG + "Details for your " + getDraftName(p) + " draft:");
			MailMessage m = drafts.get(p);
			
			String target = m.target;
			if (target == null || target == "") target = NaxColor.ERR + "[NEEDS TARGET]";
			else target = "[" + plugin.getNickName(target) + "]";
			
			String text = m.text;
			if (text == null || text == "") text = NaxColor.ERR + "[NO TEXT]";
			
			String attachment = NaxColor.MSG + "[NO ATTACHMENT]";
			if (m.item != null) attachment = "[" + m.item.getAmount() + " " + m.item.getType().toString() + "]";
			
			p.sendMessage(NaxColor.MSG + "Text: " + NaxColor.WHITE + text + NaxColor.MSG + " : Target: " + target + NaxColor.MSG + " : Attachment: " + NaxColor.WHITE + attachment);
		}
		
		if (cmd.equalsIgnoreCase("finalize"))
		{
			if (!hasDraft(p)) return;
			MailMessage m = drafts.get(p);
			
			if (m.target == null || m.target == "")
			{
				p.sendMessage(NaxColor.MSG + "You must set a target for this draft.");
				return;
			}
			else if (m.text == null || m.text == "")
			{
				p.sendMessage(NaxColor.MSG + "You must add text to this draft.");
				return;
			}
			
			m.finished = true;
			p.sendMessage(NaxColor.MSG + "Your draft " + getDraftName(p) + " is now finalized and ready for mail!");
		}
		
		if (cmd.equalsIgnoreCase("destroy"))
		{
			if (drafts.containsKey(p))
			{
				drafts.remove(p);
				p.sendMessage(NaxColor.MSG + "Your draft has been deleted.");
			}
			else
			{
				p.sendMessage(NaxColor.MSG + "You don't have a draft!");
			}
		}
		
		if (cmd.equalsIgnoreCase("read") || cmd.equalsIgnoreCase("take") || cmd.equalsIgnoreCase("delete"))
		{
			if (args.length != 2)
			{
				p.sendMessage(NaxColor.MSG + "You have to specify which mail you want to read/take/delete.");
				return;
			}
		}
		
		if (cmd.equalsIgnoreCase("read"))
		{
			if (!canDoMail(p)) return;
			
			MailAccount a = getMailAccount(p);
			MailMessage m = a.getMail(args[1]);
			
			if (m == null)
			{
				p.sendMessage(NaxColor.MSG + "You don't have any mail titled " + args[1]);
			}
			else
			{
				p.sendMessage(NaxColor.MAIL + m.title + NaxColor.MSG + " by " + plugin.getNickName(m.sender) + " sent " + getTimeAgo(m.sentTime));
				p.sendMessage(NaxColor.MSG + "Body: " + NaxColor.WHITE + m.text);
				if (m.item != null)
				{
					p.sendMessage(NaxColor.MSG + "Attachment: " + NaxColor.MAIL + m.item.getAmount() + " " + m.item.getType().toString() + NaxColor.MSG + ". Retrievable in " +
							plugin.getWorldName(m.sentWorld, true));
				}
				a.readMail(m);
			}
		}
		
		if (cmd.equalsIgnoreCase("take"))
		{
			if (!canDoMail(p)) return;
			
			MailAccount a = getMailAccount(p);
			MailMessage m = a.getMail(args[1]);
			
			if (m == null)
			{
				p.sendMessage(NaxColor.MSG + "You don't have any mail titled " + args[1]);
			}
			else
			{
				if (m.item == null)
				{
					p.sendMessage(NaxColor.MSG + "That mail doesn't have an attachment.");
					return;
				}
				else if (m.sentWorld != p.getWorld())
				{
					p.sendMessage(NaxColor.MSG + "You cannot retrieve that item in this world.");
					return;
				}
				else
				{
					HashMap<Integer, ItemStack> overflow = p.getInventory().addItem(m.item.clone());
					if (!overflow.isEmpty())
					{
						for (ItemStack i : overflow.values())
						{
							p.getWorld().dropItemNaturally(p.getLocation(), i);
						}
					}
					
					a.removeAttachment(m);
					p.sendMessage(NaxColor.MSG + "You successfully took the attachment!");
				}
			}
		}
		
		if (cmd.equalsIgnoreCase("delete"))
		{
			if (!canDoMail(p)) return;
			
			MailMessage m = getMailAccount(p).getMail(args[1]);
			
			if (m == null)
			{
				p.sendMessage(NaxColor.MSG + "You don't have any mail titled " + args[1]);
			}
			else
			{
				p.sendMessage(m.title + NaxColor.MSG + " successfully deleted.");
				getMailAccount(p).deleteMail(m);
			}
		}
	}
	
	public boolean canDoMail(Player p)
	{
		if (reading.containsKey(p))
		{
			if (p.getWorld() != reading.get(p).getWorld())
			{
				p.sendMessage(NaxColor.MSG + "You have to open your mailbox before you read mail!");
				return false;
			}
			else if (p.getLocation().distance(reading.get(p)) > 10)
			{
				p.sendMessage(NaxColor.MSG + "You have to open your mailbox before you read mail!");
				return false;
			}
		}
		else
		{
			p.sendMessage(NaxColor.MSG + "You have to open your mailbox before you read mail!");
			return false;
		}
		
		return true;
	}
	
	private boolean hasDraft(Player p)
	{
		if (drafts.containsKey(p))
		{
			if (drafts.get(p).finished)
			{
				p.sendMessage(NaxColor.MSG + "You cannot edit this finalized draft. Try removing it if you want to make a new one.");
				return false;
			}
			return true;
		}
		
		p.sendMessage(NaxColor.MSG + "You may only use this command if you've composed a new draft.");
		return false;
	}
	
	private String getDraftName(Player p)
	{
		return NaxColor.MAIL + "[" + drafts.get(p).title + "]" + NaxColor.MSG;
	}
	
	private boolean canWriteMessage(Player p)
	{
		if (drafts.containsKey(p))
		{
			p.sendMessage(NaxColor.MSG + "You can only have one unsent draft at a time. Please send your draft in a mailbox first.");
			return false;
		}
		
		return true;
	}
	
	public void printHelp(Player p)
	{
		p.sendMessage(NaxColor.MSG + "----");
		p.sendMessage(NaxColor.COMMAND + "Mail Command Help:");
		
		p.sendMessage("");
		p.sendMessage(NaxColor.MAIL + "Writing a draft:");
		p.sendMessage(NaxColor.MSG + "/mail " + NaxColor.WHITE + "help" + NaxColor.MSG + ": " + "Shows this information.");
		p.sendMessage(NaxColor.MSG + "/mail " + NaxColor.WHITE + "compose [title]" + NaxColor.MSG + ": " + "Creates a new letter named [title].");
		p.sendMessage(NaxColor.MSG + "/mail " + NaxColor.WHITE + "target [player]" + NaxColor.MSG + ": " + "Changes destination of letter.");
		p.sendMessage(NaxColor.MSG + "/mail " + NaxColor.WHITE + "add [text to add]" + NaxColor.MSG + ": " + "Can add text up to " + MAIL_CHAR_LIMIT + " chars.");
		p.sendMessage(NaxColor.MSG + "/mail " + NaxColor.WHITE + "attach" + NaxColor.MSG + ": " + "Attaches item in your hand to message.");
		p.sendMessage(NaxColor.MSG + "/mail " + NaxColor.WHITE + "detach" + NaxColor.MSG + ": " + "Removes item from mail.");
		p.sendMessage(NaxColor.MSG + "/mail " + NaxColor.WHITE + "check" + NaxColor.MSG + ": " + "Read your draft to send.");
		p.sendMessage(NaxColor.MSG + "/mail " + NaxColor.WHITE + "finalize" + NaxColor.MSG + ": " + "Locks your message, deliver to mailbox.");
		p.sendMessage(NaxColor.MSG + "/mail " + NaxColor.WHITE + "destroy" + NaxColor.MSG + ": " + "Deletes draft, returns attachment.");
		
		p.sendMessage("");
		p.sendMessage(NaxColor.MAIL + "Reading/checking your mail:");
		p.sendMessage(NaxColor.MSG + "/mail " + NaxColor.WHITE + "read [title]" + NaxColor.MSG + ": " + "Read a message.");
		p.sendMessage(NaxColor.MSG + "/mail " + NaxColor.WHITE + "take [title]" + NaxColor.MSG + ": " + "Takes an attachment.");
		p.sendMessage(NaxColor.MSG + "/mail " + NaxColor.WHITE + "delete [title]" + NaxColor.MSG + ": " + "Deletes a message (you can store " + INBOX_MAX + ").");
	}
	
	public void handlePlayerInteract(PlayerInteractEvent event)
	{
		if (event.isCancelled() && event.getClickedBlock() != null) return;
		
		if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK)
		{
			if (event.getClickedBlock().getType() == Material.TRAP_DOOR)
			{
				handleTrapDoorClick(event.getClickedBlock(), event.getPlayer());
			}
		}
	}
	
	private void handleTrapDoorClick(Block b, Player player)
	{
		AutoBase base = plugin.autoAreaManager.getBase(b);
		TrapDoor t = (TrapDoor) b.getState().getData();
		BlockFace f = t.getAttachedFace();
		Block wool = b.getRelative(f);
		
		if (wool.getType() != Material.WOOL || !t.isOpen() || base == null) return;
		
		player.sendMessage("");
		openMailbox(player);
	}
	
	public void handlePlayerJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();
		
		MailAccount m = getMailAccount(player);
		
		if (m.hasUnreadMail())
		{
			player.sendMessage(NaxColor.MSG + "You have " + NaxColor.MAIL + "[" + m.getUnreadMailNumber() + " Unread Mail] " + NaxColor.MSG + "waiting at a mailbox!");
		}
	}
	
	public void handlePlayerQuit(PlayerQuitEvent event)
	{
		if (loadedAccounts.containsKey(event.getPlayer()))
		{
			loadedAccounts.remove(event.getPlayer());
		}
	}
	
	public void openMailbox(Player player)
	{
		reading.put(player, player.getLocation());
		
		if (drafts.containsKey(player))
		{
			MailMessage draft = drafts.get(player);
			
			if (!draft.finished)
			{
				player.sendMessage(NaxColor.ERR + "Your draft is not ready to be mailed until you do " + NaxColor.MAIL + "/mail finalize");
			}
			else
			{
				sendMail(player, draft);
				return;
			}
		}
		
		MailAccount a = getMailAccount(player);
		
		int unread = a.getUnreadMailNumber();
		int read = a.getReadMailNumber();
		
		player.sendMessage(NaxColor.MSG + "==================== " + NaxColor.MAIL + "[Mail Account]" + NaxColor.MSG + " ===================");
		player.sendMessage(unread + " Unread Mail:");
		
		if (unread > 0)
		{
			for (MailMessage m : a.getUnreadMail())
			{
				String msg = NaxColor.MAIL + "[" + m.title + "] " + NaxColor.MSG + "by " + plugin.getNickName(m.sender) + ", sent " + getTimeAgo(m.sentTime) + NaxColor.MSG;
				if (m.item != null)
				{
					msg += NaxColor.WHITE + " " + m.item.getAmount() + " " + m.item.getType().toString();
				}
				else
				{
					msg += " No Attachment";
				}
				player.sendMessage(msg);
			}
		}
		else
		{
			player.sendMessage(NaxColor.MSG + "EMPTY");
		}
		
		player.sendMessage("");
		player.sendMessage(read + " Read Mail:");
		
		if (read > 0)
		{
			String message = "";
			for (MailMessage m : a.getReadMail())
			{
				if (message != "") message += ", ";
				message += NaxColor.MAIL + "[" + m.title + "] " + NaxColor.MSG + "(" + getTimeAgo(m.sentTime) + NaxColor.MSG + ")";
			}
			
			player.sendMessage(message);
		}
		else
		{
			player.sendMessage(NaxColor.MSG + "EMPTY");
		}
	}
	
	public String getTimeAgo(long time)
	{
		String unit = "second";
		long amount = (new Date().getTime() - time) / 1000;
		
		if (amount > 60)
		{
			amount /= 60;
			unit = "minute";
		}
		
		if (amount > 60)
		{
			amount /= 60;
			unit = "hour";
		}
		
		if (amount > 24)
		{
			amount /= 24;
			unit = "day";
		}
		
		return amount + " " + unit + (amount == 1 ? "" : "s") + " ago";
	}
	
	public void sendMail(Player player, MailMessage m)
	{
		if (m.finished)
		{
			String target = m.target;
			MailAccount a = getMailAccount(target);
			
			if (a.isFull())
			{
				player.sendMessage(NaxColor.MSG + "Messages to " + plugin.getNickName(target) + " could not be sent, his/her inbox is full.");
				return;
			}
			
			if (m.item != null)
			{
				int tax = getTaxPrice(m.item);
				
				if (NaxUtil.charge(player, MAIL_TAX, tax))
				{
					player.sendMessage(NaxColor.MSG + "You were successfully charged " + NaxColor.WHITE + tax + " " + MAIL_TAX.toString() + NaxColor.MSG + " for attachment on " + NaxColor.MAIL + "[" + m.title + "]");
				}
				else
				{
					player.sendMessage(NaxColor.MSG + "You could not afford to send the attachment for " + NaxColor.MAIL + "[" + m.title + "]" + NaxColor.MSG + " which totals to " + NaxColor.WHITE + tax + " " + MAIL_TAX
							.toString());
					return;
				}
			}
			
			player.sendMessage(NaxColor.MSG + "Your draft has been sent successfully!");
			m.sentTime = new Date().getTime();
			a.sendMail(m);
			drafts.remove(player);
		}
	}
	
	private int getTaxPrice(ItemStack i)
	{
		return (int) (MAIL_TAX_BASE + Math.floor(i.getAmount() / MAIL_TAX_RATE));
	}
	
	public MailAccount getMailAccount(Player player)
	{
		return getMailAccount(player.getName());
	}
	
	public MailAccount getMailAccount(String name)
	{
		if (loadedAccounts.containsKey(name)) { return loadedAccounts.get(name); }
		
		String prefix = "mail." + name;
		Set<String> messageIDs = config.getKeys(prefix);
		List<MailMessage> messages = new ArrayList<MailMessage>();
		
		if (messageIDs != null)
		{
			System.out.println(messageIDs);
			
			for (String id : messageIDs)
			{
				String title = id;
				String mPrefix = prefix + "." + id;
				
				if(!config.isConfigurationSection(prefix + ".sentWorld")) continue;
				
				boolean read = config.getBoolean(mPrefix + ".read");
				String text = config.getString(mPrefix + ".text");
				String sender = config.getString(mPrefix + ".sender");
				String target = config.getString(mPrefix + ".target");
				long sent = config.getLong(mPrefix + ".sentTime");
				
				ItemStack item = null;
				if (config.getInt(mPrefix + ".item.amount") != 0)
				{
					item = new ItemStack(
							config.getInt(mPrefix + ".item.id"),
							config.getInt(mPrefix + ".item.amount"),
							(short) config.getInt(mPrefix + ".item.damage")
							);
				}
				
				World world = plugin.getServer().getWorld(config.getString(mPrefix + ".sentWorld"));
				
				if (world != null)
				{
					MailMessage m = new MailMessage(read, title, text, sender, target, sent, item, world);
					messages.add(m);
				}
				else
				{
					config.removeProperty(mPrefix);
					config.save();
				}
			}
		}
		
		MailAccount a = new MailAccount(name, messages);
		
		loadedAccounts.put(name, a);
		
		return a;
	}
	
	public void loadMessages()
	{
		config = new NaxFile(plugin, "mail");
	}
	
	public class MailAccount
	{
		String name;
		public List<MailMessage> messages = new ArrayList<MailMessage>();
		
		public MailAccount(String name, List<MailMessage> messages)
		{
			this.name = name;
			this.messages = messages;
		}
		
		public void readMail(MailMessage m)
		{
			m.read = true;
			save();
		}
		
		public void deleteMail(MailMessage m)
		{
			if (messages.contains(m))
			{
				messages.remove(m);
				save();
			}
		}
		
		public void removeAttachment(MailMessage m)
		{
			m.item = null;
			save();
		}
		
		private void save()
		{
			String prefix = "mail." + name;
			
			config.setProperty(prefix, "");
			
			for (MailMessage m : messages)
			{
				String mPrefix = prefix + "." + m.title;
				config.setProperty(mPrefix + ".item.amount", (m.item == null ? "0" : m.item.getAmount()));
				config.setProperty(mPrefix + ".item.id", (m.item == null ? "0" : m.item.getTypeId()));
				config.setProperty(mPrefix + ".item.damage", (m.item == null ? "0" : m.item.getDurability()));
				
				config.setProperty(mPrefix + ".read", m.read);
				config.setProperty(mPrefix + ".sender", m.sender);
				config.setProperty(mPrefix + ".target", m.target);
				config.setProperty(mPrefix + ".sentTime", m.sentTime);
				config.setProperty(mPrefix + ".sentWorld", m.sentWorld.getName());
				config.setProperty(mPrefix + ".text", m.text);
			}
			
			config.save();
		}
		
		public MailMessage getMail(String title)
		{
			for (MailMessage m : messages)
			{
				if (m.title.equalsIgnoreCase(title)) return m;
			}
			return null;
		}
		
		public boolean hasUnreadMail()
		{
			for (MailMessage m : messages)
			{
				if (!m.read) { return true; }
			}
			
			return false;
		}
		
		public boolean isFull()
		{
			return (messages.size() >= INBOX_MAX);
		}
		
		public int getUnreadMailNumber()
		{
			int result = 0;
			
			for (MailMessage m : messages)
			{
				if (!m.read) result++;
			}
			
			return result;
		}
		
		public int getReadMailNumber()
		{
			int result = 0;
			
			for (MailMessage m : messages)
			{
				if (m.read) result++;
			}
			
			return result;
		}
		
		public List<MailMessage> getUnreadMail()
		{
			List<MailMessage> result = new ArrayList<MailMessage>();
			
			for (MailMessage m : messages)
			{
				if (!m.read)
				{
					result.add(m);
				}
			}
			return result;
		}
		
		public List<MailMessage> getReadMail()
		{
			List<MailMessage> result = new ArrayList<MailMessage>();
			
			for (MailMessage m : messages)
			{
				if (m.read)
				{
					result.add(m);
				}
			}
			return result;
		}
		
		public void sendMail(MailMessage m)
		{
			if (!m.finished) return;
			if (isFull()) return;
			
			Player p = plugin.getServer().getPlayer(m.target);
			if (p != null) p.sendMessage(NaxColor.MSG + "You have received " + NaxColor.MAIL + "[New Mail]" + NaxColor.MSG + "! Check a mailbox to read it.");
			
			messages.add(m);
			m.sent = true;
			
			save();
		}
	}
	
	public class MailMessage
	{
		public boolean read, finished, sent;
		public String title, text, sender, target;
		public long sentTime;
		public ItemStack item;
		public World sentWorld;
		
		// new mail
		public MailMessage(String title, String text, Player sender)
		{
			init(false, title, text, sender.getName(), null, 0, null, sender.getWorld());
		}
		
		// loaded mail
		public MailMessage(boolean read, String title, String text, String sender, String target, long sent, ItemStack item, World sentWorld)
		{
			init(read, title, text, sender, target, sent, item, sentWorld);
		}
		
		public void init(boolean read, String title, String text, String sender, String target, long sent, ItemStack item, World sentWorld)
		{
			this.read = read;
			this.title = title;
			this.text = text;
			this.sender = sender;
			this.target = target;
			this.sentTime = sent;
			this.item = item;
			this.sentWorld = sentWorld;
		}
	}
	
	public class MailAccountUnloader implements Runnable
	{
		public Player player;
		
		public MailAccountUnloader(Player player)
		{
			this.player = player;
		}
		
		public void run()
		{
			if (!player.isOnline() && loadedAccounts.containsKey(player))
			{
				loadedAccounts.remove(player);
			}
		}
	}
}
