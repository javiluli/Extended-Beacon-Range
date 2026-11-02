package com.javiluli.extendedbeaconrange.mixin.perimeter.client;

import com.javiluli.extendedbeaconrange.feature.perimeter.client.BeaconAreaCollector;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public class BeaconClientLevelMixin {
	/** Encola descubrimiento incremental al recibir un chunk. */
	@Inject(method = "onChunkLoaded", at = @At("TAIL"))
	private void extendedbeaconrange$chunkLoaded(ChunkPos pos, CallbackInfo ci) {
		BeaconAreaCollector.chunkChanged((ClientLevel) (Object) this, pos.x(), pos.z());
	}

	/** Invalida chunks descargados sin retener sus entidades. */
	@Inject(method = "unload", at = @At("HEAD"))
	private void extendedbeaconrange$chunkUnloaded(LevelChunk chunk, CallbackInfo ci) {
		BeaconAreaCollector.chunkChanged((ClientLevel) (Object) this, chunk.getPos().x(), chunk.getPos().z());
	}

	/** Detecta beacons nuevos dentro de chunks ya cargados. */
	@Inject(method = "onBlockEntityAdded", at = @At("TAIL"))
	private void extendedbeaconrange$beaconAdded(BlockEntity entity, CallbackInfo ci) {
		if (entity instanceof BeaconBlockEntity) {
			BlockPos pos = entity.getBlockPos();
			BeaconAreaCollector.chunkChanged((ClientLevel) (Object) this, pos.getX() >> 4, pos.getZ() >> 4);
		}
	}

	/** Invalida colocaciones y destrucciones, sin reaccionar a bloques ajenos. */
	@Inject(method = "setBlocksDirty", at = @At("TAIL"))
	private void extendedbeaconrange$beaconChanged(BlockPos pos, BlockState oldState, BlockState newState, CallbackInfo ci) {
		if (oldState.is(Blocks.BEACON) || newState.is(Blocks.BEACON)) {
			BeaconAreaCollector.chunkChanged((ClientLevel) (Object) this, pos.getX() >> 4, pos.getZ() >> 4);
		}
	}
}
