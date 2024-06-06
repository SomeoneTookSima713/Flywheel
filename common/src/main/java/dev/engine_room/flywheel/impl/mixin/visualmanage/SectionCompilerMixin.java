package dev.engine_room.flywheel.impl.mixin.visualmanage;

import net.minecraft.client.renderer.chunk.SectionCompiler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.engine_room.flywheel.lib.visual.VisualizationHelper;
import net.minecraft.world.level.block.entity.BlockEntity;

@Mixin(SectionCompiler.class)
abstract class SectionCompilerMixin {
	@Inject(method = "handleBlockEntity", at = @At("HEAD"), cancellable = true)
	private void flywheel$tryAddBlockEntity(SectionCompiler.Results results, BlockEntity blockEntity, CallbackInfo ci) {
		if (VisualizationHelper.tryAddBlockEntity(blockEntity)) {
			ci.cancel();
		}
	}
}
