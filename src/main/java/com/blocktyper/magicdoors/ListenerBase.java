package com.blocktyper.magicdoors;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;

import com.blocktyper.magicdoors.data.DimentionItemCount;
import com.blocktyper.magicdoors.data.MagicDoor;
import com.blocktyper.magicdoors.data.MagicDoorRepo;
import com.blocktyper.v1_2_6.BlockTyperListener;
import com.blocktyper.v1_2_6.helpers.InvisHelper;
import com.blocktyper.v1_2_6.nbt.NBTItem;

public abstract class ListenerBase extends BlockTyperListener {

	public static String DOOR_NAME_PREFIXES = "magic.doors.door.name.prefixes";
	public static String DOOR_NAME_SUFFIXES = "magic.doors.door.name.suffixes";

	public static final String DATA_KEY_MAGIC_DOOR_DIMENTION_MAP = "root-doors-dimention";
	public static final String DATA_KEY_MAGIC_DOORS = "magic-doors";

	public static final String PARENT_ID_HIDDEN_LORE_PREFIX = "#MAGIC_DOOR_PARENT";

	public static MagicDoorRepo magicDoorRepo;
	public static DimentionItemCount dimentionItemCount;


	public ListenerBase() {
		init(MagicDoorsPlugin.getPlugin());

		initMagicDoorRepo();
		initDimentionItemCount();
	}

	protected String getParentIdFromLore(List<String> lore) {

		String parentId = null;
		if (lore != null && !lore.isEmpty()) {
			Optional<String> loreLineWithParentId = lore.stream().filter(
					l -> l != null && InvisHelper.convertToVisibleString(l).contains(PARENT_ID_HIDDEN_LORE_PREFIX))
					.findFirst();
			if (loreLineWithParentId != null && loreLineWithParentId.isPresent()
					&& loreLineWithParentId.get() != null) {
				String loreLine = InvisHelper.convertToVisibleString(loreLineWithParentId.get());
				parentId = loreLine.substring(
						loreLine.indexOf(PARENT_ID_HIDDEN_LORE_PREFIX) + PARENT_ID_HIDDEN_LORE_PREFIX.length());
			}
		}

		return parentId;
	}
	
	protected static class KeyChain {
		
		
		public KeyChain() {
			super();
		}

		public KeyChain(List<String> parentIds) {
			super();
			this.parentIds = parentIds;
		}

		private List<String> parentIds = new ArrayList<>();

		public List<String> getParentIds() {
			return parentIds;
		}

		public void setParentIds(List<String> parentIds) {
			this.parentIds = parentIds;
		}

	}
	
	protected List<String> getParentIdsOnKeyChain(ItemStack item){
		NBTItem keyChainItem = new NBTItem(item);

		KeyChain keyChain = keyChainItem.getObject(MagicDoorsPlugin.keyChainRecipe(), KeyChain.class);

		if (keyChain == null) {
			return null;
		}

		return keyChain.getParentIds();
	}
	
	protected void initMagicDoorRepo() {
		if (magicDoorRepo == null) {
			magicDoorRepo = MagicDoorsPlugin.getPlugin().getTypeData(DATA_KEY_MAGIC_DOORS, MagicDoorRepo.class);

			if (magicDoorRepo == null || magicDoorRepo.getMap() == null) {
				magicDoorRepo = new MagicDoorRepo();
				magicDoorRepo.setMap(new HashMap<String, MagicDoor>());
			}
			updateMagicDoorRepo();
		}
	}

	protected void updateMagicDoorRepo() {
		MagicDoorsPlugin.getPlugin().setData(DATA_KEY_MAGIC_DOORS, magicDoorRepo, true);
	}

	protected void initDimentionItemCount() {
		if (dimentionItemCount == null) {
			dimentionItemCount = MagicDoorsPlugin.getPlugin().getTypeData(DATA_KEY_MAGIC_DOOR_DIMENTION_MAP,
					DimentionItemCount.class);
			if (dimentionItemCount == null || dimentionItemCount.getItemsInDimentionAtValue() == null) {
				dimentionItemCount = new DimentionItemCount();
				dimentionItemCount.setItemsInDimentionAtValue(new HashMap<String, Map<Integer, Set<String>>>());
			}
			updateDimentionItemCount();
		}
	}
	
	protected void updateDimentionItemCount() {
		MagicDoorsPlugin.getPlugin().setData(DATA_KEY_MAGIC_DOOR_DIMENTION_MAP, dimentionItemCount, true);
	}
	
	
	protected void teleportToChildDoor(HumanEntity player, String parentId, int childDoorNumber) {

		MagicDoor magicDoor = magicDoorRepo.getMap().get(parentId);
		if (magicDoor == null) {
			MagicDoorsPlugin.getPlugin().debugWarning("Failed to load parent door from magic-doors repo.");
			return;
		}

		String childId = magicDoor.getChildren().get(childDoorNumber);
		MagicDoor childDoor = magicDoorRepo.getMap().get(childId);

		if (childDoor == null) {
			player.sendMessage(ChatColor.RED + new MessageFormat(MagicDoorsPlugin.getPlugin()
					.getLocalizedMessage("magic-doors-failed-to-find-child-door-number", player))
							.format(new Object[] { (childDoorNumber + 1) + "", magicDoor.getChildren().size() + "" }));
			return;
		}

		if(teleportToDoor(player, childDoor)){
			player.sendMessage(new MessageFormat(MagicDoorsPlugin.getPlugin()
					.getLocalizedMessage("magic-doors-you-have-been-teleported-to-child", player))
							.format(new Object[] { (childDoorNumber + 1) + "", magicDoor.getId() }));
		}
	}
	
	
	protected boolean teleportToDoor(HumanEntity player, MagicDoor door) {

		World world = MagicDoorsPlugin.getPlugin().getServer().getWorld(door.getWorld());

		if (world == null) {
			sendMissingWorldMessage(player, door.getWorld());
			return false;
		}

		Location destination = new Location(world, door.getPlayerX() + 0.0, door.getPlayerY() + 0.0,
				door.getPlayerZ() + 0.0);
		player.teleport(destination);
		
		return true;
	}
	
	protected void sendMissingWorldMessage(HumanEntity player, String world) {
		player.sendMessage(ChatColor.RED + new MessageFormat(
				MagicDoorsPlugin.getPlugin().getLocalizedMessage("magic-doors-failed-to-find-world", player))
						.format(new Object[] { world }));
	}

}
