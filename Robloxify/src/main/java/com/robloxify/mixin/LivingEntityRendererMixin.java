package com.robloxify.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.robloxify.client.ClientRobloxState;
import com.robloxify.client.avatar.RobloxAvatarModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Swaps the model used for players between the vanilla one and the Roblox avatar.
 * <p>
 * The swap happens at the start of {@code submit} because 26.2 extracts every entity into a
 * render state first and only then submits them, so the model has to be chosen right before the
 * draw call rather than during extraction.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
	@Shadow
	protected EntityModel<?> model;

	@Unique
	private PlayerModel robloxify$vanillaModel;
	@Unique
	private RobloxAvatarModel robloxify$robloxModel;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void robloxify$captureModels(EntityRendererProvider.Context context, EntityModel<?> model,
			float shadowRadius, CallbackInfo ci) {
		if ((Object) this instanceof AvatarRenderer<?> && model instanceof PlayerModel playerModel) {
			this.robloxify$vanillaModel = playerModel;
			this.robloxify$robloxModel = new RobloxAvatarModel(context.bakeLayer(RobloxAvatarModel.LAYER));
		}
	}

	@Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
			at = @At("HEAD"))
	private void robloxify$swapModel(LivingEntityRenderState state, PoseStack poseStack,
			SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
		if (this.robloxify$vanillaModel == null || this.robloxify$robloxModel == null) {
			return;
		}
		boolean roblox = state instanceof AvatarRenderState avatarState
				&& ClientRobloxState.isRobloxEntity(avatarState.id);
		this.model = roblox ? this.robloxify$robloxModel : this.robloxify$vanillaModel;
	}
}
