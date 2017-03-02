package com.blocktyper.magicdoors;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bukkit.inventory.ItemStack;

import com.blocktyper.v1_2_3.BlockTyperListener;
import com.blocktyper.v1_2_3.IBlockTyperPlugin;
import com.blocktyper.v1_2_3.helpers.InvisHelper;
import com.blocktyper.v1_2_3.nbt.NBTItem;

public abstract class ListenerBase extends BlockTyperListener {

	public static String DOOR_NAME_PREFIXES = "magic.doors.door.name.prefixes";
	public static String DOOR_NAME_SUFFIXES = "magic.doors.door.name.suffixes";

	public static final String DATA_KEY_MAGIC_DOOR_DIMENTION_MAP = "root-doors-dimention";
	public static final String DATA_KEY_MAGIC_DOORS = "magic-doors";

	public static final String PARENT_ID_HIDDEN_LORE_PREFIX = "#MAGIC_DOOR_PARENT";

	public ListenerBase(IBlockTyperPlugin plugin) {
		init(plugin);
	}

	public ListenerBase() {
		init(MagicDoorsPlugin.getPlugin());
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

}
