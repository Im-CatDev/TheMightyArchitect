package com.simibubi.mightyarchitect.block;

import com.simibubi.mightyarchitect.control.ArchitectManager;
import com.simibubi.mightyarchitect.control.design.DesignExporter;
import com.simibubi.mightyarchitect.control.design.DesignLayer;
import com.simibubi.mightyarchitect.control.design.DesignType;
import com.simibubi.mightyarchitect.control.phase.ArchitectPhases;
import com.simibubi.mightyarchitect.control.phase.export.PhaseEditTheme;
import com.simibubi.mightyarchitect.foundation.utility.Lang;
import com.simibubi.mightyarchitect.gui.DesignExporterScreen;
import com.simibubi.mightyarchitect.gui.ScreenHelper;

import net.minecraft.advancements.critereon.BlockPredicate.Builder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

public class DesignAnchorBlock extends Block {

	public static final BooleanProperty compass = BooleanProperty.create("compass");

	public DesignAnchorBlock() {
		super(Properties.copy(Blocks.STONE));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(compass);
		super.createBlockStateDefinition(builder);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(compass, true);
	}

	@Override
	public InteractionResult use(BlockState blockState, Level world, BlockPos pos, Player player, InteractionHand pHand,
		BlockHitResult pHit) {
		if (world.isClientSide)
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
				() -> () -> handleUseOnDesignAnchor(player, world, pos, blockState));
		return InteractionResult.SUCCESS;
	}

	@OnlyIn(value = Dist.CLIENT)
	protected void handleUseOnDesignAnchor(Player player, Level world, BlockPos anchor, BlockState blockState) {
		if (!ArchitectManager.inPhase(ArchitectPhases.EditingThemes))
			return;

		boolean crouching = player.isShiftKeyDown();

		if (!PhaseEditTheme.isVisualizing()) {
			if (crouching)
				return;
			boolean resaving = false;
			if (world.getBlockEntity(anchor.south()) instanceof SignBlockEntity sign) {
				resaving = true;
				DesignExporter.layer = DesignLayer.valueOf(sign.getMessage(2, false)
					.getString());
				String[] split = sign.getMessage(3, false)
					.getString()
					.split(", ");
				DesignExporter.type = DesignType.valueOf(split[0]);
				if (split.length > 1)
					DesignExporter.designParameter = Integer.valueOf(split[1]);
			}
			ScreenHelper.open(new DesignExporterScreen(anchor, resaving));
			return;
		}

		if (crouching) {
			PhaseEditTheme.resetVisualization();
			Lang.text("Scan cancelled")
				.sendStatus(player);
			return;
		}

		String name = DesignExporter.exportDesign(world, anchor);
		if (name.isEmpty())
			return;
		Lang.text("Saved as '" + name + "'")
			.sendStatus(player);
	}

}
