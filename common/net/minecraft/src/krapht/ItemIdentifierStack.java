/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.krapht;

import net.minecraft.src.ItemStack;

public final class ItemIdentifierStack {
	private final ItemIdentifier _item;
	public int stackSize;
	
	public static ItemIdentifierStack GetFromStack(ItemStack stack){
		return new ItemIdentifierStack(ItemIdentifier.get(stack), stack.stackSize);
	}
	
	public ItemIdentifierStack(ItemIdentifier item, int stackSize){
		_item = item;
		this.stackSize = stackSize;
	}
	
	public ItemIdentifier getItem(){
		return _item;
	}
}
