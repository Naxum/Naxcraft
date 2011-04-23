package com.naxville.naxcraft.land;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import com.naxville.naxcraft.Naxcraft;

/*
 * TODO: Saving, anyone?
 * 
 */

public class NaxcraftAutoarea {
	public Naxcraft plugin;
	
	private final Material FINAL_BLOCK = Material.REDSTONE_TORCH_ON;
	private final Material CENTER_BLOCK = Material.IRON_BLOCK;
	private final Material RING_BLOCK = Material.SANDSTONE;
	
	public List<Autoarea> autoareas = new ArrayList<Autoarea>();
	
	public NaxcraftAutoarea(Naxcraft instance){
		plugin = instance;
	}
	
	public void handleBlockPlace(BlockPlaceEvent event){
		
		if(event.getBlock().getType() == this.FINAL_BLOCK){
			
			Block centerBlock = event.getBlock().getRelative(0, -1, 0);
			
			if(centerBlock.getType() == this.CENTER_BLOCK){
				
				for(int x = -1; x < 2; x++){
					for(int z = -1; z < 2; z++){
						
						if(x == 0 && z == 0) continue;
						
						Block ringBlock = centerBlock.getRelative(x, 0, z);
						
						if(ringBlock.getType() != this.RING_BLOCK){
							return;
						}
					}
				}
				
				for(int x = -2; x < 3; x++){
					for(int z = -2; z < 3; z++){
						if(x == -1 && (z <= 1 && z >= -1)) continue;
						if(x ==  0 && (z <= 1 && z >= -1)) continue;
						if(x ==  1 && (z <= 1 && z >= -1)) continue;
						if((x == 2 && z == 2) || (x == -2 && z == 2) || (x == -2 && z == -2) || (x == 2 && z == -2)) continue;
						
						Block outsideBlock = centerBlock.getRelative(x, 0, z);

						if(outsideBlock.getType() != Material.AIR){
							event.getPlayer().sendMessage(Naxcraft.ERROR_COLOR + "Your property marker must be surrounded by air on all sides.");
							event.setCancelled(true);
							return;
						}
					}
				}
				
				event.getPlayer().sendMessage(Naxcraft.SUCCESS_COLOR + "You successfully planted an area marker!");
				this.autoareas.add(new Autoarea(plugin, event.getBlock()));
			}
		}
	}
	
	public void handleBlockBreak(BlockBreakEvent event){
		
		if(this.autoareas.size() > 0){
			for(Autoarea area : this.autoareas){
				if(area.withinMarker(event.getBlock())){
					event.setCancelled(true);
					event.getPlayer().sendMessage(Naxcraft.ERROR_COLOR + "You cannot destroy blocks on this marker. Remove the torch.");
				}
				
				if(event.getBlock().equals(area.getBlock())){
					area.removeArea();
					this.autoareas.remove(area);
				}
			}
		}
	}
	
	public void handleBlockInteract(PlayerInteractEvent event){
		if(event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		
		for(Autoarea area : this.autoareas){
			if(area.withinMarker(event.getClickedBlock())){
				if(area.clickedSign(event.getClickedBlock())){
					area.handleSignClick(event);
					
				} else {
					event.getPlayer().sendMessage(Naxcraft.MSG_COLOR + "This is an area marker. You can break it by destroying the redstone torch.");
				}
				
				event.setCancelled(true);
			}
		}
	}
	
	public class Autoarea {
		protected Naxcraft plugin;
		protected Block torchBlock;
		protected List<Block> blocks = new ArrayList<Block>();
		protected Date date;
		
		private List<Block> ringBlocks = new ArrayList<Block>();
		private List<Block> signs = new ArrayList<Block>();
		private List<BlockFace>faces = new ArrayList<BlockFace>();
		
		public Autoarea(Naxcraft instance, Block block){
			this.plugin = instance;
			this.torchBlock = block;
			this.date = new Date();
			
			this.createAutoarea();
		}
		
		private void createAutoarea(){				
			Block centerBlock = torchBlock.getRelative(0, -1, 0);
					
			for(int x = -1; x < 2; x++){
				for(int z = -1; z < 2; z++){
					
					if(x == 0 && z == 0) continue;
					
					Block ringBlock = centerBlock.getRelative(x, 0, z);
					
					this.ringBlocks.add(this.ringBlocks.size(), ringBlock);
					this.blocks.add(ringBlock);
				}
			}
					
			for(int x = -2; x < 3; x++){
				for(int z = -2; z < 3; z++){
					if(x == -1 && (z <= 1 && z >= -1)) continue;
					if(x ==  0 && (z <= 1 && z >= -1)) continue;
					if(x ==  1 && (z <= 1 && z >= -1)) continue;
					if((x == 2 && z == 2) || (x == -2 && z == 2) || (x == -2 && z == -2) || (x == 2 && z == -2)) continue;
					
					Block outsideBlock = centerBlock.getRelative(x, 0, z);
					
					for(Block block : this.ringBlocks){
						
						BlockFace face = block.getFace(outsideBlock);
						
						if(face != null){
							if(face == BlockFace.DOWN || face == BlockFace.NORTH_EAST || face == BlockFace.NORTH_WEST ||
									face == BlockFace.SOUTH_EAST || face == BlockFace.SOUTH_WEST || face == BlockFace.UP) continue;
							
							this.signs.add(outsideBlock);
							this.faces.add(face);
							this.blocks.add(outsideBlock);
							break;
						}
					}
					
				}
			}
			
			for(Block block : this.ringBlocks){
				block.setTypeIdAndData(Material.WOOL.getId(), (byte)13, false);
			}
			
			for(Block block : this.signs){
				
                block.setType(Material.WALL_SIGN);
                switch ( this.faces.get(this.signs.indexOf(block)) )
                {
                    case NORTH:
                        block.setData((byte)0x04);
                        break;
                    case SOUTH:
                    	block.setData((byte)0x05);
                        break;
                    case EAST:
                    	block.setData((byte)0x02);
                        break;
                    case WEST:
                    	block.setData((byte)0x03);
                        break;
                    default:
                        break;
                }
                
                block.getState().setData(new MaterialData(Material.WALL_SIGN));
                this.calcSign(this.signs.indexOf(block));
			}
			
		}
		
		public void calcSign(int num){
			//TODO: PURCHASED YET?
			
			/*
			 * Aw yeah weird numbers.
			 * 
			 *      11  10  9
			 *   8             7
			 *   
			 *   6    signs    5
			 *   
			 *   4             3
			 *      2   1   0
			 * 
			 */
			Sign sign = (Sign)this.signs.get(num).getState();
			
			//this will either be purchase, or extend.
			if(num == 1 || num == 5 || num == 6 || num == 10){
				this.writeSign(sign, AutoareaSignType.PURCHASE, num);
				
			} else {
				this.writeSign(sign, AutoareaSignType.HELP, num);
				
			}
		}
		
		public void writeSign(Sign sign, AutoareaSignType type, int num){
			/*
			 * TODO: Actualy do this, check and whatnot.
			 */
			switch(type){
			case PURCHASE:
				sign.setLine(0, ChatColor.BLUE + "[Purchase]");
				sign.setLine(1, "Right click to");
				sign.setLine(2, "buy property");
				sign.setLine(3, "for 32 Iron.");
				break;
				
			case HELP:
				sign.setLine(0, ChatColor.YELLOW + "[Help]");
				sign.setLine(1, "Right click");
				sign.setLine(2, "this sign for");
				sign.setLine(3, "more help.");
				break;
				
			default:
				sign.setLine(1, "Unused");
				break;
			}
			
		}
		
		public void removeArea(){
			for(Block block : this.signs){
				block.setType(Material.AIR);
			}
			for(Block block : this.ringBlocks){
				block.setType(Material.AIR);
				this.torchBlock.getWorld().dropItem(torchBlock.getLocation(), new ItemStack(RING_BLOCK, 1));
			}
		}
		
		public Block getBlock(){
			return this.torchBlock;
		}
		
		public List<Block> getBlocks(){
			return this.blocks;
		}
		
		public boolean withinMarker(Block blockTest){
			for(Block block : this.blocks){
				if(block.equals(blockTest))
					return true;
			}
			
			return false;
		}
		
		public boolean clickedSign(Block blockTest){
			for(Block block : this.signs){
				if(block.equals(blockTest))
					return true;
			}
			return false;
		}
		
		/*
		 * TODO: Write help, etc.
		 */
		public void handleSignClick(PlayerInteractEvent event){
			event.getPlayer().sendMessage(Naxcraft.MSG_COLOR + "Not now, maybe later.");
		}
	}

	public enum AutoareaSignType {
		PURCHASE,
		HELP,
		EXTEND,
		FLAG,
		SELL,
		EXCHANGE
	}
}
