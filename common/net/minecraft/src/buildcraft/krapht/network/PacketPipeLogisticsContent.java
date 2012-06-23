package net.minecraft.src.buildcraft.krapht.network;

import java.util.UUID;

import net.minecraft.src.ModLoader;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.EntityPassiveItem;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.core.network.PacketPayload;
import net.minecraft.src.buildcraft.core.network.PacketPipeTransportContent;
import net.minecraft.src.buildcraft.krapht.CoreRoutedPipe;
import net.minecraft.src.buildcraft.krapht.routing.RoutedEntityItem;
import net.minecraft.src.buildcraft.krapht.routing.Router;
import net.minecraft.src.buildcraft.krapht.routing.RouterManager;
import net.minecraft.src.buildcraft.transport.Pipe;
import net.minecraft.src.buildcraft.transport.TileGenericPipe;

public class PacketPipeLogisticsContent extends PacketPipeTransportContent {
	
	public PacketPipeLogisticsContent() {
		super();
	}
	
	public PacketPipeLogisticsContent(PacketPipeTransportContent packet) {
		super();
		this.payload = packet.payload;
		this.posX = packet.posX;
		this.posY = packet.posY;
		this.posZ = packet.posZ;
	}
	
	public PacketPipeLogisticsContent(int x, int y, int z, RoutedEntityItem item, Orientations orientation) {
		super(x,y,z,item,orientation);
		final Router routerSource = RouterManager.get(item.getSource());
		final Router routerDest = RouterManager.get(item.getDestination());
		if(routerDest == null) {
			return;
		}
		final Pipe pipeSource;
		if(routerSource != null) {
			pipeSource = routerSource.getPipe();
		} else {
			pipeSource = null;
		}
		final Pipe pipeDest = routerDest.getPipe();
		PacketPayload additions = new PacketPayload(6,0,0);
		if(pipeSource != null) {
			additions.intPayload[0] = pipeSource.xCoord;
			additions.intPayload[1] = pipeSource.yCoord;
			additions.intPayload[2] = pipeSource.zCoord;
		}
		additions.intPayload[3] = pipeDest.xCoord;
		additions.intPayload[4] = pipeDest.yCoord;
		additions.intPayload[5] = pipeDest.zCoord;
		this.payload.append(additions);
	}
	
	public static boolean isPacket(PacketPipeTransportContent packet) {
		if(packet.payload.intPayload.length < 12) {
			return false;
		}
		return true;
	}
	
	public UUID getSourceUUID(World world) {
		if(this.payload.intPayload.length < 12) {
			return null;
		}
		TileGenericPipe tile = getPipe(world,payload.intPayload[6],payload.intPayload[7],payload.intPayload[8]);
		if(tile.pipe instanceof CoreRoutedPipe) {
			return ((CoreRoutedPipe)tile.pipe).getRouter().getId();
		}
		return null;
	}
	
	public UUID getDestUUID(World world) {
		if(this.payload.intPayload.length < 12) {
			return null;
		}
		TileGenericPipe tile = getPipe(world,payload.intPayload[9],payload.intPayload[10],payload.intPayload[11]);
		if(tile.pipe instanceof CoreRoutedPipe) {
			return ((CoreRoutedPipe)tile.pipe).getRouter().getId();
		}
		return null;
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
