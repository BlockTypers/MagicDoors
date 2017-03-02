package com.blocktyper.magicdoors;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;

import com.blocktyper.v1_2_3.nbt.NBTItem;

public class KeyChainListener extends ListenerBase {

	public KeyChainListener() {
		super();
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
	public void onInventoryClick(InventoryClickEvent event) {
		debugInfo("KeyChain");
		if (event.getAction() != InventoryAction.SWAP_WITH_CURSOR) {
			debugInfo("Not InventoryAction.SWAP_WITH_CURSOR");
			return;
		}

		if (event.getCurrentItem() == null || event.getCursor() == null) {
			return;
		}

		if (!itemHasExpectedNbtKey(event.getCurrentItem(), MagicDoorsPlugin.keyChainRecipe())) {
			debugInfo("Current Item is not key chain.");
			return;
		}

		if (!itemHasExpectedNbtKey(event.getCursor(), MagicDoorsPlugin.doorKeyRecipe())) {
			debugInfo("Cursor Item is not a key.");
			return;
		}

		String parentId = getParentIdFromLore(
				event.getCursor().getItemMeta() != null ? event.getCursor().getItemMeta().getLore() : null);

		if (parentId == null) {
			debugInfo("Cursor Item is not a named key.");
			return;
		}

		NBTItem keyChainItem = new NBTItem(event.getCurrentItem());

		List<String> parentIds = getParentIdsOnKeyChain(event.getCurrentItem());

		if (parentIds == null) {
			parentIds = new ArrayList<>();
		}

		if (parentIds.contains(parentId)) {
			event.getWhoClicked().sendMessage("The key chain already has this key.");
			return;
		}

		parentIds.add(parentId);

		keyChainItem.setObject(MagicDoorsPlugin.keyChainRecipe(), new KeyChain(parentIds));

		event.setCurrentItem(keyChainItem.getItem());

		event.getWhoClicked().setItemOnCursor(null);

	}

}
