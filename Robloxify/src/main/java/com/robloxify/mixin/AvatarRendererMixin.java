package com.robloxify.mixin;

import com.robloxify.Robloxify;
import com.robloxify.client.ClientRobloxState;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Uses the Robloxify skin texture for players wearing the Roblox avatar. */
@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin {
	private static final Identifier ROBLOXIFY$AVATAR_TEXTURE = Robloxify.id("textures/entity/roblox_avatar.png");

	@Inject(method = "getTextureLocation(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)Lnet/minecraft/resources/Identifier;",
			at = @At("HEAD"), cancellable = true)
	private void robloxify$avatarTexture(AvatarRenderState state, CallbackInfoReturnable<Identifier> cir) {
		if (ClientRobloxState.isRobloxEntity(state.id)) {
			cir.setReturnValue(ROBLOXIFY$AVATAR_TEXTURE);
		}
	}
}
