package cc.unilock.tamago.mixin;

import dev.architectury.event.events.common.BlockEvent;
import dev.architectury.utils.value.IntValue;
import io.github.fabricators_of_create.porting_lib.event.common.BlockEvents;
import io.github.fabricators_of_create.porting_lib.util.PortingHooks;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = PortingHooks.class, remap = false)
public class PortingHooksMixin {
	@Inject(method = "onBlockBreakEvent", at = @At(value = "INVOKE", target = "Lio/github/fabricators_of_create/porting_lib/event/common/BlockEvents$BreakEvent;isCanceled()Z", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
	private static void onBlockBreakEventPre(World world, GameMode gameType, ServerPlayerEntity entityPlayer, BlockPos pos, CallbackInfoReturnable<Integer> cir, boolean preCancelEvent, ItemStack itemstack, BlockState state, BlockEvents.BreakEvent event) {
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

	@Inject(method = "onBlockBreakEvent", at = @At(value = "TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
	private static void onBlockBreakEventPost(World world, GameMode gameType, ServerPlayerEntity entityPlayer, BlockPos pos, CallbackInfoReturnable<Integer> cir, boolean preCancelEvent, ItemStack itemstack, BlockState state, BlockEvents.BreakEvent event) {
		PlayerBlockBreakEvents.AFTER.invoker().afterBlockBreak(world, entityPlayer, pos, state, null);
	}
}
