package net.minecraft.src.buildcraft.logisticspipes.modules;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.buildcraft.krapht.GuiIDs;
import net.minecraft.src.buildcraft.krapht.SimpleServiceLocator;
import net.minecraft.src.buildcraft.logisticspipes.modules.SinkReply.FixedPriority;
import net.minecraft.src.krapht.InventoryUtil;
import net.minecraft.src.krapht.ItemIdentifier;
import net.minecraft.src.krapht.SimpleInventory;

public class ModuleItemSink implements ILogisticsModule{
	
	private final SimpleInventory _filterInventory = new SimpleInventory(9, "Requested items", 1);
	private boolean _isDefaultRoute;
	
	public IInventory getFilterInventory(){
		return _filterInventory;
	}
	
	public boolean isDefaultRoute(){
		return _isDefaultRoute;
	}
	public void setDefaultRoute(boolean isDefaultRoute){
		_isDefaultRoute = isDefaultRoute;
	}
	

	@Override
	public SinkReply sinksItem(ItemStack item) {
		InventoryUtil invUtil = SimpleServiceLocator.inventoryUtilFactory.getInventoryUtil(_filterInventory);
		if (invUtil.containsItem(ItemIdentifier.get(item))){
			SinkReply reply = new SinkReply();
			reply.fixedPriority = FixedPriority.ItemSink;
			reply.isPassive = true;
			return reply;
		}
		
		if (_isDefaultRoute){
			SinkReply reply = new SinkReply();
			reply.fixedPriority = FixedPriority.DefaultRoute;
			reply.isPassive = true;
			reply.isDefault = true;
			return reply;
		}

		return null;
	}

	@Override
	public int getGuiHandlerID() {
		return GuiIDs.GUI_Module_ItemSink_ID;
	}
	
	@Override
	public ILogisticsModule getSubModule(int slot) {return null;}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound, String prefix) {
		_filterInventory.readFromNBT(nbttagcompound, "");
		setDefaultRoute(nbttagcompound.getBoolean("defaultdestination"));
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound, String prefix) {
    	_filterInventory.writeToNBT(nbttagcompound, "");
    	nbttagcompound.setBoolean("defaultdestination", isDefaultRoute());
	}

	@Override
	public void tick() {}
}
