package com.robloxify.mixin;

import com.robloxify.avatar.AvatarAppearance;
import com.robloxify.client.ClientRobloxState;
import com.robloxify.client.avatar.AvatarSkinFactory;
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
	@Inject(method = "getTextureLocation(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)Lnet/minecraft/resources/Identifier;",
			at = @At("HEAD"), cancellable = true)
	private void robloxify$avatarTexture(AvatarRenderState state, CallbackInfoReturnable<Identifier> cir) {
		AvatarAppearance appearance = ClientRobloxState.appearanceFor(state.id);
		if (appearance != null) {
			cir.setReturnValue(AvatarSkinFactory.textureFor(appearance));
		}
	}
}
