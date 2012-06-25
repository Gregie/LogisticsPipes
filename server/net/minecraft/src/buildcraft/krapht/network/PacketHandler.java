package net.minecraft.src.buildcraft.krapht.network;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.NetServerHandler;
import net.minecraft.src.NetworkManager;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.mod_LogisticsPipes;
import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.krapht.CoreRoutedPipe;
import net.minecraft.src.buildcraft.krapht.GuiIDs;
import net.minecraft.src.buildcraft.krapht.logic.LogicCrafting;
import net.minecraft.src.buildcraft.krapht.logic.LogicProvider;
import net.minecraft.src.buildcraft.krapht.logic.LogicSatellite;
import net.minecraft.src.buildcraft.krapht.logic.LogicSupplier;
import net.minecraft.src.buildcraft.krapht.pipes.PipeItemsProviderLogistics;
import net.minecraft.src.buildcraft.krapht.pipes.PipeItemsRequestLogistics;
import net.minecraft.src.buildcraft.krapht.pipes.PipeLogisticsChassi;
import net.minecraft.src.buildcraft.krapht.routing.OrdererRequests;
import net.minecraft.src.buildcraft.logisticspipes.modules.ISneakyOrientationreceiver;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModuleAdvancedExtractor;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModuleExtractor;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModuleItemSink;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModuleProvider;
import net.minecraft.src.buildcraft.logisticspipes.modules.SneakyOrientation;
import net.minecraft.src.buildcraft.transport.TileGenericPipe;
import net.minecraft.src.forge.IPacketHandler;

public class PacketHandler implements IPacketHandler {

	@Override
	public void onPacketData(NetworkManager network, String channel, byte[] bytes) {
		final DataInputStream data = new DataInputStream(new ByteArrayInputStream(bytes));
		try {
			final NetServerHandler net = (NetServerHandler) network.getNetHandler();
			final int packetID = data.read();
			switch (packetID) {

				case NetworkConstants.CRAFTING_PIPE_NEXT_SATELLITE:
					final PacketCoordinates packet = new PacketCoordinates();
					packet.readData(data);
					onCraftingPipeNextSatellite(net.getPlayerEntity(), packet);
					break;

				case NetworkConstants.CRAFTING_PIPE_PREV_SATELLITE:
					final PacketCoordinates packetA = new PacketCoordinates();
					packetA.readData(data);
					onCraftingPipePrevSatellite(net.getPlayerEntity(), packetA);
					break;
				case NetworkConstants.CRAFTING_PIPE_IMPORT:
					final PacketCoordinates packetB = new PacketCoordinates();
					packetB.readData(data);
					onCraftingPipeImport(net.getPlayerEntity(), packetB);
					break;
				case NetworkConstants.SATELLITE_PIPE_NEXT:
					final PacketCoordinates packetC = new PacketCoordinates();
					packetC.readData(data);
					onSatellitePipeNext(net.getPlayerEntity(), packetC);
					break;
				case NetworkConstants.SATELLITE_PIPE_PREV:
					final PacketCoordinates packetD = new PacketCoordinates();
					packetD.readData(data);
					onSatellitePipePrev(net.getPlayerEntity(), packetD);
					break;
				case NetworkConstants.CHASSI_GUI_PACKET_ID:
					final PacketPipeInteger packetE = new PacketPipeInteger();
					packetE.readData(data);
					onModuleGuiOpen(net.getPlayerEntity(), packetE);
					break;
				case NetworkConstants.GUI_BACK_PACKET:
					final PacketPipeInteger packetF = new PacketPipeInteger();
					packetF.readData(data);
					onGuiBackOpen(net.getPlayerEntity(), packetF);
					break;
				case NetworkConstants.REQUEST_SUBMIT:
					final PacketRequestSubmit packetG = new PacketRequestSubmit();
					packetG.readData(data);
					onRequestSubmit(net.getPlayerEntity(), packetG);
					break;
				case NetworkConstants.ORDERER_REFRESH_REQUEST:
					final PacketPipeInteger packetH = new PacketPipeInteger();
					packetH.readData(data);
					onRefreshRequest(net.getPlayerEntity(), packetH);
					break;
				case NetworkConstants.ITEM_SINK_DEFAULT:
					final PacketPipeInteger packetI = new PacketPipeInteger();
					packetI.readData(data);
					onItemSinkDefault(net.getPlayerEntity(), packetI);
					break;
				case NetworkConstants.PROVIDER_PIPE_NEXT_MODE:
					final PacketCoordinates packetJ = new PacketCoordinates();
					packetJ.readData(data);
					onProviderModeChange(net.getPlayerEntity(), packetJ);
					break;
				case NetworkConstants.PROVIDER_PIPE_CHANGE_INCLUDE:
					final PacketCoordinates packetK = new PacketCoordinates();
					packetK.readData(data);
					onProviderIncludeChange(net.getPlayerEntity(), packetK);
					break;
				case NetworkConstants.SUPPLIER_PIPE_MODE_CHANGE:
					final PacketCoordinates packetL = new PacketCoordinates();
					packetL.readData(data);
					onSupplierModeChange(net.getPlayerEntity(), packetL);
					break;
				case NetworkConstants.EXTRACTOR_MODULE_DIRECTION_SET:
					final PacketPipeInteger packetM = new PacketPipeInteger();
					packetM.readData(data);
					onExtractorModeChange(net.getPlayerEntity(), packetM);
					break;
				case NetworkConstants.PROVIDER_MODULE_NEXT_MODE:
					final PacketPipeInteger packetN = new PacketPipeInteger();
					packetN.readData(data);
					onProviderModuleModeChange(net.getPlayerEntity(), packetN);
					break;
				case NetworkConstants.PROVIDER_MODULE_CHANGE_INCLUDE:
					final PacketPipeInteger packetO = new PacketPipeInteger();
					packetO.readData(data);
					onProviderModuleIncludeChange(net.getPlayerEntity(), packetO);
					break;
				case NetworkConstants.ADVANCED_EXTRACTOR_MODULE_INCLUDED_SET:
					final PacketPipeInteger packetP = new PacketPipeInteger();
					packetP.readData(data);
					onAdvancedExtractorModuleIncludeChange(net.getPlayerEntity(), packetP);
					break;
				case NetworkConstants.ADVANCED_EXTRACTOR_MODULE_SNEAKY_GUI:
					final PacketPipeInteger packetQ = new PacketPipeInteger();
					packetQ.readData(data);
					onAdvancedExtractorModuleGuiSneaky(net.getPlayerEntity(), packetQ);
					break;
			}
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void onCraftingPipeNextSatellite(EntityPlayerMP player, PacketCoordinates packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe.logic instanceof LogicCrafting)) {
			return;
		}

		((LogicCrafting) pipe.pipe.logic).setNextSatellite(player);
	}

	private void onCraftingPipePrevSatellite(EntityPlayerMP player, PacketCoordinates packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe.logic instanceof LogicCrafting)) {
			return;
		}

		((LogicCrafting) pipe.pipe.logic).setPrevSatellite(player);
	}

	private void onCraftingPipeImport(EntityPlayerMP player, PacketCoordinates packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe.logic instanceof LogicCrafting)) {
			return;
		}

		((LogicCrafting) pipe.pipe.logic).importFromCraftingTable(player);
	}

	private void onSatellitePipeNext(EntityPlayerMP player, PacketCoordinates packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe.logic instanceof LogicSatellite)) {
			return;
		}

		((LogicSatellite) pipe.pipe.logic).setNextId(player);
	}

	private void onSatellitePipePrev(EntityPlayerMP player, PacketCoordinates packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe.logic instanceof LogicSatellite)) {
			return;
		}

		((LogicSatellite) pipe.pipe.logic).setPrevId(player);
	}

	private void onModuleGuiOpen(EntityPlayerMP player, PacketPipeInteger packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe instanceof PipeLogisticsChassi)) {
			return;
		}

		final PipeLogisticsChassi cassiPipe = (PipeLogisticsChassi) pipe.pipe;

		player.openGui(mod_LogisticsPipes.instance, cassiPipe.getLogisticsModule().getSubModule(packet.integer).getGuiHandlerID()
				+ (100 * (packet.integer + 1)), player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (cassiPipe.getLogisticsModule().getSubModule(packet.integer) instanceof ModuleItemSink) {
			player.playerNetServerHandler.sendPacket(new PacketPipeInteger(NetworkConstants.ITEM_SINK_STATUS, packet.posX, packet.posY, packet.posZ,
					(((ModuleItemSink) cassiPipe.getLogisticsModule().getSubModule(packet.integer)).isDefaultRoute() ? 1 : 0)).getPacket());
		}
		if (cassiPipe.getLogisticsModule().getSubModule(packet.integer) instanceof ModuleExtractor) {
			player.playerNetServerHandler.sendPacket(new PacketPipeInteger(NetworkConstants.EXTRACTOR_MODULE_RESPONSE, packet.posX, packet.posY, packet.posZ,
					(((ModuleExtractor) cassiPipe.getLogisticsModule().getSubModule(packet.integer)).getSneakyOrientation().ordinal())).getPacket());
		}
		if (cassiPipe.getLogisticsModule().getSubModule(packet.integer) instanceof ModuleProvider) {
			player.playerNetServerHandler.sendPacket(new PacketPipeInteger(NetworkConstants.PROVIDER_MODULE_INCLUDE_CONTENT, packet.posX, packet.posY, packet.posZ, (((ModuleProvider) cassiPipe.getLogisticsModule().getSubModule(packet.integer)).isExcludeFilter() ? 1 : 0)).getPacket());
			player.playerNetServerHandler.sendPacket(new PacketPipeInteger(NetworkConstants.PROVIDER_MODULE_MODE_CONTENT, packet.posX, packet.posY, packet.posZ, (((ModuleProvider) cassiPipe.getLogisticsModule().getSubModule(packet.integer)).getExtractionMode().ordinal())).getPacket());
		}
		if (cassiPipe.getLogisticsModule().getSubModule(packet.integer) instanceof ModuleAdvancedExtractor) {
			player.playerNetServerHandler.sendPacket(new PacketPipeInteger(NetworkConstants.ADVANCED_EXTRACTOR_MODULE_INCLUDED_RESPONSE, packet.posX, packet.posY, packet.posZ, (((ModuleAdvancedExtractor) cassiPipe.getLogisticsModule().getSubModule(packet.integer)).areItemsIncluded() ? 1 : 0)).getPacket());
		}
	}

	private void onGuiBackOpen(EntityPlayerMP player, PacketPipeInteger packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe instanceof PipeLogisticsChassi)) {
			return;
		}

		player.openGui(mod_LogisticsPipes.instance, packet.integer, player.worldObj, packet.posX, packet.posY, packet.posZ);
	}

	private void onRequestSubmit(EntityPlayerMP player, PacketRequestSubmit packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe instanceof PipeItemsRequestLogistics)) {
			return;
		}

		OrdererRequests.request(player, packet, (PipeItemsRequestLogistics) pipe.pipe);
	}

	private void onRefreshRequest(EntityPlayerMP player, PacketPipeInteger packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe instanceof CoreRoutedPipe)) {
			return;
		}

		OrdererRequests.DisplayOptions option;
		switch (packet.integer) {
			case 0:
				option = OrdererRequests.DisplayOptions.Both;
				break;
			case 1:
				option = OrdererRequests.DisplayOptions.SupplyOnly;
				break;
			case 2:
				option = OrdererRequests.DisplayOptions.CraftOnly;
				break;
			default:
				option = OrdererRequests.DisplayOptions.Both;
				break;
		}

		OrdererRequests.refresh(player, (CoreRoutedPipe) pipe.pipe, option);
	}

	private void onItemSinkDefault(EntityPlayerMP player, PacketPipeInteger packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe instanceof CoreRoutedPipe)) {
			return;
		}

		final CoreRoutedPipe piperouted = (CoreRoutedPipe) pipe.pipe;

		final int value = packet.integer % 10;
		final int slot = packet.integer / 10;

		if (piperouted.getLogisticsModule() == null) {
			return;
		}

		if (slot <= 0) {
			if (piperouted.getLogisticsModule() instanceof ModuleItemSink) {
				final ModuleItemSink module = (ModuleItemSink) piperouted.getLogisticsModule();
				module.setDefaultRoute(value == 1);
				return;
			}
		} else {
			if (piperouted.getLogisticsModule().getSubModule(slot - 1) instanceof ModuleItemSink) {
				final ModuleItemSink module = (ModuleItemSink) piperouted.getLogisticsModule().getSubModule(slot - 1);
				module.setDefaultRoute(value == 1);
				return;
			}
		}
	}

	private void onProviderModeChange(EntityPlayerMP player, PacketCoordinates packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe instanceof PipeItemsProviderLogistics)) {
			return;
		}
		final PipeItemsProviderLogistics providerpipe = (PipeItemsProviderLogistics) pipe.pipe;
		final LogicProvider logic = (LogicProvider)providerpipe.logic;
		logic.nextExtractionMode();
		player.playerNetServerHandler.sendPacket(new PacketPipeInteger(NetworkConstants.PROVIDER_PIPE_MODE_CONTENT, packet.posX, packet.posY, packet.posZ, logic.getExtractionMode().ordinal()).getPacket());
	}

	private void onProviderIncludeChange(EntityPlayerMP player, PacketCoordinates packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe instanceof PipeItemsProviderLogistics)) {
			return;
		}
		final PipeItemsProviderLogistics providerpipe = (PipeItemsProviderLogistics) pipe.pipe;
		final LogicProvider logic = (LogicProvider)providerpipe.logic;
		logic.setFilterExcluded(!logic.isExcludeFilter());
		player.playerNetServerHandler.sendPacket(new PacketPipeInteger(NetworkConstants.PROVIDER_PIPE_INCLUDE_CONTENT, packet.posX, packet.posY, packet.posZ, logic.isExcludeFilter() ? 1 : 0).getPacket());
	}

	private void onSupplierModeChange(EntityPlayerMP player, PacketCoordinates packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe.logic instanceof LogicSupplier)) {
			return;
		}
		final LogicSupplier logic = (LogicSupplier) pipe.pipe.logic;
		logic.setRequestingPartials(!logic.isRequestingPartials());
		player.playerNetServerHandler.sendPacket(new PacketPipeInteger(NetworkConstants.SUPPLIER_PIPE_MODE_RESPONSE, packet.posX, packet.posY, packet.posZ, logic.isRequestingPartials() ? 1 : 0).getPacket());
	}

	private void onExtractorModeChange(EntityPlayerMP player, PacketPipeInteger packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}

		if (!(pipe.pipe instanceof CoreRoutedPipe)) {
			return;
		}

		final CoreRoutedPipe piperouted = (CoreRoutedPipe) pipe.pipe;

		final int value = packet.integer % 10;
		final int slot = packet.integer / 10;

		if (piperouted.getLogisticsModule() == null) {
			return;
		}

		if (slot <= 0) {
			if (piperouted.getLogisticsModule() instanceof ISneakyOrientationreceiver) {
				final ISneakyOrientationreceiver module = (ISneakyOrientationreceiver) piperouted.getLogisticsModule();
				switch (value){
				case 0:
					module.setSneakyOrientation(SneakyOrientation.Top);
					break;
				
				case 1:
					module.setSneakyOrientation(SneakyOrientation.Side);
					break;
				
				case 2:
					module.setSneakyOrientation(SneakyOrientation.Bottom);
					break;
					
				case 3:
					module.setSneakyOrientation(SneakyOrientation.Default);
					break;
				}
				player.playerNetServerHandler.sendPacket(new PacketPipeInteger(NetworkConstants.EXTRACTOR_MODULE_RESPONSE, packet.posX, packet.posY, packet.posZ, module.getSneakyOrientation().ordinal()).getPacket());
				return;
			}
		} else {
			if (piperouted.getLogisticsModule().getSubModule(slot - 1) instanceof ISneakyOrientationreceiver) {
				final ISneakyOrientationreceiver module = (ISneakyOrientationreceiver) piperouted.getLogisticsModule().getSubModule(slot - 1);
				switch (value){
				case 0:
					module.setSneakyOrientation(SneakyOrientation.Top);
					break;
				
				case 1:
					module.setSneakyOrientation(SneakyOrientation.Side);
					break;
				
				case 2:
					module.setSneakyOrientation(SneakyOrientation.Bottom);
					break;
					
				case 3:
					module.setSneakyOrientation(SneakyOrientation.Default);
					break;
				}
				player.playerNetServerHandler.sendPacket(new PacketPipeInteger(NetworkConstants.EXTRACTOR_MODULE_RESPONSE, packet.posX, packet.posY, packet.posZ, module.getSneakyOrientation().ordinal()).getPacket());
				return;
			}
		}
	}

	private void onProviderModuleModeChange(EntityPlayerMP player, PacketPipeInteger packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}


		if (!(pipe.pipe instanceof CoreRoutedPipe)) {
			return;
		}

		final CoreRoutedPipe piperouted = (CoreRoutedPipe) pipe.pipe;

		final int slot = packet.integer;
		
		if (piperouted.getLogisticsModule() == null) {
			return;
		}
		
		if (slot <= 0) {
			if (piperouted.getLogisticsModule() instanceof ModuleProvider) {
				final ModuleProvider module = (ModuleProvider)piperouted.getLogisticsModule();
				module.nextExtractionMode();
				player.playerNetServerHandler.sendPacket(new PacketPipeInteger(NetworkConstants.PROVIDER_MODULE_MODE_CONTENT, packet.posX, packet.posY, packet.posZ, module.getExtractionMode().ordinal()).getPacket());
				return;
			}
		} else {
			if (piperouted.getLogisticsModule().getSubModule(slot - 1) instanceof ModuleProvider) {
				final ModuleProvider module = (ModuleProvider)piperouted.getLogisticsModule().getSubModule(slot - 1);
				module.nextExtractionMode();
				player.playerNetServerHandler.sendPacket(new PacketPipeInteger(NetworkConstants.PROVIDER_MODULE_MODE_CONTENT, packet.posX, packet.posY, packet.posZ, module.getExtractionMode().ordinal()).getPacket());
				return;
			}
		}
	}

	private void onProviderModuleIncludeChange(EntityPlayerMP player, PacketPipeInteger packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}


		if (!(pipe.pipe instanceof CoreRoutedPipe)) {
			return;
		}

		final CoreRoutedPipe piperouted = (CoreRoutedPipe) pipe.pipe;

		final int slot = packet.integer;

		if (piperouted.getLogisticsModule() == null) {
			return;
		}
		
		
		if (slot <= 0) {
			if (piperouted.getLogisticsModule() instanceof ModuleProvider) {
				final ModuleProvider module = (ModuleProvider)piperouted.getLogisticsModule();
				module.setFilterExcluded(!module.isExcludeFilter());
				player.playerNetServerHandler.sendPacket(new PacketPipeInteger(NetworkConstants.PROVIDER_MODULE_INCLUDE_CONTENT, packet.posX, packet.posY, packet.posZ, module.isExcludeFilter() ? 1 : 0).getPacket());
				return;
			}
		} else {
			if (piperouted.getLogisticsModule().getSubModule(slot - 1) instanceof ModuleProvider) {
				final ModuleProvider module = (ModuleProvider)piperouted.getLogisticsModule().getSubModule(slot - 1);
				module.setFilterExcluded(!module.isExcludeFilter());
				player.playerNetServerHandler.sendPacket(new PacketPipeInteger(NetworkConstants.PROVIDER_MODULE_INCLUDE_CONTENT, packet.posX, packet.posY, packet.posZ, (module.isExcludeFilter() ? 1 : 0)).getPacket());
				return;
			}
		}
	}

	private void onAdvancedExtractorModuleIncludeChange(EntityPlayerMP player, PacketPipeInteger packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}


		if (!(pipe.pipe instanceof CoreRoutedPipe)) {
			return;
		}

		final CoreRoutedPipe piperouted = (CoreRoutedPipe) pipe.pipe;

		final int slot = packet.integer / 10;

		if (piperouted.getLogisticsModule() == null) {
			return;
		}
		
		if (slot <= 0) {
			if (piperouted.getLogisticsModule() instanceof ModuleAdvancedExtractor) {
				final ModuleAdvancedExtractor module = (ModuleAdvancedExtractor)piperouted.getLogisticsModule();
				module.setItemsIncluded(!module.areItemsIncluded());
				player.playerNetServerHandler.sendPacket(new PacketPipeInteger(NetworkConstants.ADVANCED_EXTRACTOR_MODULE_INCLUDED_RESPONSE, packet.posX, packet.posY, packet.posZ, module.areItemsIncluded() ? 1 : 0).getPacket());
				return;
			}
		} else {
			if (piperouted.getLogisticsModule().getSubModule(slot - 1) instanceof ModuleAdvancedExtractor) {
				final ModuleAdvancedExtractor module = (ModuleAdvancedExtractor)piperouted.getLogisticsModule().getSubModule(slot - 1);
				module.setItemsIncluded(!module.areItemsIncluded());
				player.playerNetServerHandler.sendPacket(new PacketPipeInteger(NetworkConstants.ADVANCED_EXTRACTOR_MODULE_INCLUDED_RESPONSE, packet.posX, packet.posY, packet.posZ, (module.areItemsIncluded() ? 1 : 0)).getPacket());
				return;
			}
		}
	}

	private void onAdvancedExtractorModuleGuiSneaky(EntityPlayerMP player, PacketPipeInteger packet) {
		final TileGenericPipe pipe = getPipe(player.worldObj, packet.posX, packet.posY, packet.posZ);
		if (pipe == null) {
			return;
		}


		if (!(pipe.pipe instanceof CoreRoutedPipe)) {
			return;
		}

		final CoreRoutedPipe piperouted = (CoreRoutedPipe) pipe.pipe;

		final int slot = packet.integer;

		if (piperouted.getLogisticsModule() == null) {
			return;
		}

		if (slot <= 0) {
			if (piperouted.getLogisticsModule() instanceof ModuleAdvancedExtractor) {
				final ModuleAdvancedExtractor module = (ModuleAdvancedExtractor)piperouted.getLogisticsModule();
				player.openGui(mod_LogisticsPipes.instance, GuiIDs.GUI_Module_Extractor_ID + (100 * packet.integer), player.worldObj, packet.posX, packet.posY, packet.posZ);
				player.playerNetServerHandler.sendPacket(new PacketPipeInteger(NetworkConstants.EXTRACTOR_MODULE_RESPONSE, packet.posX, packet.posY, packet.posZ, module.getSneakyOrientation().ordinal()).getPacket());
				return;
			}
		} else {
			if (piperouted.getLogisticsModule().getSubModule(slot - 1) instanceof ModuleAdvancedExtractor) {
				final ModuleAdvancedExtractor module = (ModuleAdvancedExtractor)piperouted.getLogisticsModule().getSubModule(slot - 1);
				player.openGui(mod_LogisticsPipes.instance, GuiIDs.GUI_Module_Extractor_ID + (100 * packet.integer), player.worldObj, packet.posX, packet.posY, packet.posZ);
				player.playerNetServerHandler.sendPacket(new PacketPipeInteger(NetworkConstants.EXTRACTOR_MODULE_RESPONSE, packet.posX, packet.posY, packet.posZ, module.getSneakyOrientation().ordinal()).getPacket());
				return;
			}
		}
	}

	// BuildCraft method
	/**
	 * Retrieves pipe at specified coordinates if any.
	 * 
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	private TileGenericPipe getPipe(World world, int x, int y, int z) {
		if (!world.blockExists(x, y, z)) {
			return null;
		}

		final TileEntity tile = world.getBlockTileEntity(x, y, z);
		if (!(tile instanceof TileGenericPipe)) {
			return null;
		}

		return (TileGenericPipe) tile;
	}
	// BuildCraft method end
}
