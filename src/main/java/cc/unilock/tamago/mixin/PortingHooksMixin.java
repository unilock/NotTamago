package cc.unilock.tamago.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.architectury.event.events.common.BlockEvent;
import dev.architectury.utils.value.IntValue;
import io.github.fabricators_of_create.porting_lib.event.common.BlockEvents;
import io.github.fabricators_of_create.porting_lib.util.PortingHooks;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PortingHooks.class)
public class PortingHooksMixin {
	@Inject(method = "onBlockBreakEvent(Lnet/minecraft/world/World;Lnet/minecraft/world/GameMode;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/util/math/BlockPos;Z)I", at = @At(value = "INVOKE", target = "Lio/github/fabricators_of_create/porting_lib/event/common/BlockEvents$BreakEvent;isCanceled()Z", ordinal = 0, remap = false))
	private static void onBlockBreakEventPre(World world, GameMode gameType, ServerPlayerEntity entityPlayer, BlockPos pos, boolean canAttackBlock, CallbackInfoReturnable<Integer> cir, @Local BlockState state, @Local BlockEvents.BreakEvent event) {
		boolean result = PlayerBlockBreakEvents.BEFORE.invoker().beforeBlockBreak(world, entityPlayer, pos, state, null);

		if (!result) {
			PlayerBlockBreakEvents.CANCELED.invoker().onBlockBreakCanceled(world, entityPlayer, pos, state, null);

			event.setCanceled(true);
			return;
		}

		if (BlockEvent.BREAK.invoker().breakBlock(world, pos, state, entityPlayer, new IntValue() {
			@Override
			public void accept(int value) {
				event.setExpToDrop(value);
			}

			@Override
			public int getAsInt() {
				return event.getExpToDrop();
			}
		}).isFalse()) {
			event.setCanceled(true);
		}
	}

	@Inject(method = "onBlockBreakEvent(Lnet/minecraft/world/World;Lnet/minecraft/world/GameMode;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/util/math/BlockPos;Z)I", at = @At(value = "TAIL"))
	private static void onBlockBreakEventPost(World world, GameMode gameType, ServerPlayerEntity entityPlayer, BlockPos pos, boolean canAttackBlock, CallbackInfoReturnable<Integer> cir, @Local BlockState state) {
		PlayerBlockBreakEvents.AFTER.invoker().afterBlockBreak(world, entityPlayer, pos, state, null);
	}
}
