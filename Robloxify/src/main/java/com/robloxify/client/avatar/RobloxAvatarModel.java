package com.robloxify.client.avatar;

import com.robloxify.Robloxify;
import com.robloxify.client.ClientRobloxState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

/**
 * A blocky, Roblox inspired avatar.
 * <p>
 * It extends {@link PlayerModel} so all of the vanilla machinery keeps working: held items,
 * armour layers, name tags, sneaking, swimming and so on. Only the geometry differs - the head is
 * a proper cube, the limbs are thinner, and the arms and legs are split into two segments so they
 * can bend while walking.
 */
public class RobloxAvatarModel extends PlayerModel {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(Robloxify.id("roblox_avatar"), "main");

	private final ModelPart cap;
	private final ModelPart shades;
	private final ModelPart leftElbow;
	private final ModelPart rightElbow;
	private final ModelPart leftKnee;
	private final ModelPart rightKnee;

	public RobloxAvatarModel(ModelPart root) {
		super(root, false);
		this.cap = this.head.getChild("cap");
		this.shades = this.head.getChild("shades");
		this.leftElbow = this.leftArm.getChild("left_elbow");
		this.rightElbow = this.rightArm.getChild("right_elbow");
		this.leftKnee = this.leftLeg.getChild("left_knee");
		this.rightKnee = this.rightLeg.getChild("right_knee");
	}

	public static LayerDefinition createLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();

		PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0)
				.addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
		head.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(32, 0)
				.addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.ZERO);
		head.addOrReplaceChild("cap", CubeListBuilder.create().texOffs(0, 40)
				.addBox(-4.5F, -9.0F, -4.5F, 9.0F, 2.0F, 9.0F), PartPose.ZERO);
		head.addOrReplaceChild("shades", CubeListBuilder.create().texOffs(40, 40)
				.addBox(-4.0F, -5.0F, -5.0F, 8.0F, 2.0F, 1.0F), PartPose.ZERO);

		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16)
				.addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
		body.addOrReplaceChild("jacket", CubeListBuilder.create(), PartPose.ZERO);

		PartDefinition rightArm = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16)
				.addBox(-1.5F, -2.0F, -1.5F, 3.0F, 10.0F, 3.0F), PartPose.offset(-5.5F, 2.0F, 0.0F));
		rightArm.addOrReplaceChild("right_sleeve", CubeListBuilder.create(), PartPose.ZERO);
		rightArm.addOrReplaceChild("right_elbow", CubeListBuilder.create().texOffs(0, 32)
				.addBox(-1.5F, 0.0F, -1.5F, 3.0F, 4.0F, 3.0F), PartPose.offset(0.0F, 8.0F, 0.0F));

		PartDefinition leftArm = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 16).mirror()
				.addBox(-1.5F, -2.0F, -1.5F, 3.0F, 10.0F, 3.0F), PartPose.offset(5.5F, 2.0F, 0.0F));
		leftArm.addOrReplaceChild("left_sleeve", CubeListBuilder.create(), PartPose.ZERO);
		leftArm.addOrReplaceChild("left_elbow", CubeListBuilder.create().texOffs(12, 32).mirror()
				.addBox(-1.5F, 0.0F, -1.5F, 3.0F, 4.0F, 3.0F), PartPose.offset(0.0F, 8.0F, 0.0F));

		PartDefinition rightLeg = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16)
				.addBox(-1.5F, 0.0F, -1.5F, 3.0F, 8.0F, 3.0F), PartPose.offset(-2.0F, 12.0F, 0.0F));
		rightLeg.addOrReplaceChild("right_pants", CubeListBuilder.create(), PartPose.ZERO);
		rightLeg.addOrReplaceChild("right_knee", CubeListBuilder.create().texOffs(24, 32)
				.addBox(-1.5F, 0.0F, -1.5F, 3.0F, 4.0F, 3.0F), PartPose.offset(0.0F, 8.0F, 0.0F));

		PartDefinition leftLeg = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).mirror()
				.addBox(-1.5F, 0.0F, -1.5F, 3.0F, 8.0F, 3.0F), PartPose.offset(2.0F, 12.0F, 0.0F));
		leftLeg.addOrReplaceChild("left_pants", CubeListBuilder.create(), PartPose.ZERO);
		leftLeg.addOrReplaceChild("left_knee", CubeListBuilder.create().texOffs(36, 32).mirror()
				.addBox(-1.5F, 0.0F, -1.5F, 3.0F, 4.0F, 3.0F), PartPose.offset(0.0F, 8.0F, 0.0F));

		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void setupAnim(AvatarRenderState state) {
		super.setupAnim(state);

		// Segmented limbs: bend the elbows and knees along with the walk cycle.
		float swing = Mth.cos(state.walkAnimationPos * 0.6662F) * 1.4F * state.walkAnimationSpeed;
		float bend = Math.max(0.0F, -swing) * 0.5F;
		float knee = Math.max(0.0F, swing) * 0.5F;
		this.rightElbow.xRot = bend;
		this.leftElbow.xRot = -bend;
		this.rightKnee.xRot = knee;
		this.leftKnee.xRot = -knee;

		Entity entity = ClientRobloxState.entityFor(state.id);
		boolean self = entity != null && entity == Minecraft.getInstance().player;
		this.cap.visible = self && ClientRobloxState.owns("builder_cap");
		this.shades.visible = self && ClientRobloxState.owns("shades");

		if (entity != null) {
			String emote = ClientRobloxState.emoteOf(entity.getUUID());
			if (emote != null) {
				applyEmote(emote, ClientRobloxState.emoteProgress(entity.getUUID()));
			}
		}
	}

	private void applyEmote(String emote, float progress) {
		float phase = progress * (float) Math.PI * 4.0F;
		switch (emote) {
			case "wave" -> {
				this.rightArm.xRot = -2.6F;
				this.rightArm.zRot = -0.3F + Mth.sin(phase * 1.5F) * 0.45F;
				this.rightElbow.xRot = 0.5F;
			}
			case "dance" -> {
				this.rightArm.xRot = -1.2F + Mth.sin(phase) * 0.9F;
				this.leftArm.xRot = -1.2F - Mth.sin(phase) * 0.9F;
				this.rightArm.zRot = -0.3F;
				this.leftArm.zRot = 0.3F;
				this.head.yRot = Mth.sin(phase) * 0.4F;
				this.body.yRot = Mth.sin(phase) * 0.15F;
			}
			case "point" -> {
				this.rightArm.xRot = -1.57F;
				this.rightArm.yRot = -0.35F;
			}
			case "jump" -> {
				this.rightArm.xRot = -2.2F;
				this.leftArm.xRot = -2.2F;
			}
			case "fall" -> {
				this.rightArm.xRot = -0.6F;
				this.leftArm.xRot = -0.6F;
				this.rightArm.zRot = -1.2F;
				this.leftArm.zRot = 1.2F;
			}
			default -> {
			}
		}
	}
}
