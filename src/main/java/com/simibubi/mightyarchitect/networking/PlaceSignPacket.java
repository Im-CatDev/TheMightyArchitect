package com.simibubi.mightyarchitect.networking;

import java.util.function.Supplier;

import com.simibubi.mightyarchitect.foundation.utility.Lang;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraftforge.network.NetworkEvent;

public class PlaceSignPacket {

	public String text1;
	public String text2;
	public String text3;
	public String text4;
	public BlockPos position;

	public PlaceSignPacket() {}

	public PlaceSignPacket(String textLine1, String textLine2, String textLine3, String textLine4, BlockPos position) {
		this.text1 = textLine1;
		this.text2 = textLine2;
		this.text3 = textLine3;
		this.text4 = textLine4;
		this.position = position;
	}

	public PlaceSignPacket(FriendlyByteBuf buffer) {
		this(buffer.readUtf(128), buffer.readUtf(128), buffer.readUtf(128), buffer.readUtf(128), buffer.readBlockPos());
	}

	public void toBytes(FriendlyByteBuf buffer) {
		buffer.writeUtf(text1);
		buffer.writeUtf(text2);
		buffer.writeUtf(text3);
		buffer.writeUtf(text4);
		buffer.writeBlockPos(position);
	}

	public void handle(Supplier<NetworkEvent.Context> context) {
		context.get()
			.enqueueWork(() -> {
				Level entityWorld = context.get()
					.getSender()
					.getCommandSenderWorld();
				entityWorld.setBlockAndUpdate(position, Blocks.DARK_OAK_WALL_SIGN.defaultBlockState()
					.setValue(WallSignBlock.FACING, Direction.SOUTH));
				SignBlockEntity sign = (SignBlockEntity) entityWorld.getBlockEntity(position);
				SignText pText = new SignText();
				pText.setColor(DyeColor.WHITE);
				pText.setMessage(0, Lang.text(text1)
					.color(0xD7C2D7)
					.component());
				pText.setMessage(1, Lang.text(text2)
					.color(0xaaaaaa)
					.component());
				pText.setMessage(2, Lang.text(text3)
					.color(0xdddddd)
					.component());
				pText.setMessage(3, Lang.text(text4)
					.color(0xdddddd)
					.component());
				sign.setText(pText, true);
				sign.setChanged();
				if (entityWorld instanceof ServerLevel sl)
					sl.getChunkSource()
						.blockChanged(position);
			});
	}

}
