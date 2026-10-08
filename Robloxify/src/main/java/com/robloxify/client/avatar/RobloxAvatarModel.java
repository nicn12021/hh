package com.robloxify.client.avatar;

import com.robloxify.Robloxify;
import com.robloxify.avatar.AvatarAppearance;
import com.robloxify.avatar.CosmeticCategory;
import com.robloxify.client.ClientRobloxState;
import com.robloxify.config.RobloxifyConfig;
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

import java.util.HashMap;
import java.util.Map;

/**
 * The Robloxify avatar: a blocky, simple, game-like character built to read as a Roblox avatar
 * rather than a Minecraft humanoid.
 * <p>
 * It extends {@link PlayerModel} so held items, armour, name tags and vanilla states keep working.
 * On top of that it adds thinner limbs, segmented elbows and knees, switchable hat and accessory
 * geometry, and a Roblox style animation language.
 */
public class RobloxAvatarModel extends PlayerModel {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(Robloxify.id("roblox_avatar"), "main");

	private final ModelPart cap;
	private final ModelPart topHat;
	private final ModelPart topHatBrim;
	private final ModelPart crown;
	private final ModelPart headphoneBand;
	private final ModelPart headphoneLeft;
	private final ModelPart headphoneRight;
	private final ModelPart backpack;
	private final ModelPart wingLeft;
	private final ModelPart wingRight;
	private final ModelPart shoulderLeft;
	private final ModelPart shoulderRight;
	private final ModelPart leftElbow;
	private final ModelPart rightElbow;
	private final ModelPart leftKnee;
	private final ModelPart rightKnee;

	/** Tiny per-entity landing squash timer. Bounded by the number of visible players. */
	private final Map<Integer, Integer> landTimers = new HashMap<>();
	private final Map<Integer, Boolean> wasAirborne = new HashMap<>();

	public RobloxAvatarModel(ModelPart root) {
		super(root, false);
		this.cap = this.head.getChild("cap");
		this.topHat = this.head.getChild("top_hat");
		this.topHatBrim = this.head.getChild("top_hat_brim");
		this.crown = this.head.getChild("crown");
		this.headphoneBand = this.head.getChild("headphone_band");
		this.headphoneLeft = this.head.getChild("headphone_left");
		this.headphoneRight = this.head.getChild("headphone_right");
		this.backpack = this.body.getChild("backpack");
		this.wingLeft = this.body.getChild("wing_left");
		this.wingRight = this.body.getChild("wing_right");
		this.shoulderLeft = this.leftArm.getChild("shoulder_pad");
		this.shoulderRight = this.rightArm.getChild("shoulder_pad");
		this.leftElbow = this.leftArm.getChild("left_elbow");
		this.rightElbow = this.rightArm.getChild("right_elbow");
		this.leftKnee = this.leftLeg.getChild("left_knee");
		this.rightKnee = this.rightLeg.getChild("right_knee");
	}

	// ------------------------------------------------------------------- mesh

	public static LayerDefinition createLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();

		PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0)
				.addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
		head.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
		head.addOrReplaceChild("cap", CubeListBuilder.create().texOffs(64, 0)
				.addBox(-4.5F, -9.0F, -4.5F, 9.0F, 2.0F, 9.0F), PartPose.ZERO);
		head.addOrReplaceChild("top_hat", CubeListBuilder.create().texOffs(64, 16)
				.addBox(-3.5F, -13.0F, -3.5F, 7.0F, 5.0F, 7.0F), PartPose.ZERO);
		head.addOrReplaceChild("top_hat_brim", CubeListBuilder.create().texOffs(64, 32)
				.addBox(-4.5F, -9.0F, -4.5F, 9.0F, 1.0F, 9.0F), PartPose.ZERO);
		head.addOrReplaceChild("crown", CubeListBuilder.create().texOffs(64, 48)
				.addBox(-4.5F, -11.0F, -4.5F, 9.0F, 3.0F, 9.0F), PartPose.ZERO);
		head.addOrReplaceChild("headphone_band", CubeListBuilder.create().texOffs(64, 64)
				.addBox(-4.5F, -9.5F, -1.0F, 9.0F, 1.0F, 2.0F), PartPose.ZERO);
		head.addOrReplaceChild("headphone_left", CubeListBuilder.create().texOffs(64, 72)
				.addBox(4.0F, -7.0F, -2.0F, 1.0F, 3.0F, 3.0F), PartPose.ZERO);
		head.addOrReplaceChild("headphone_right", CubeListBuilder.create().texOffs(64, 72).mirror()
				.addBox(-5.0F, -7.0F, -2.0F, 1.0F, 3.0F, 3.0F), PartPose.ZERO);

		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16)
				.addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
		body.addOrReplaceChild("jacket", CubeListBuilder.create(), PartPose.ZERO);
		body.addOrReplaceChild("backpack", CubeListBuilder.create().texOffs(64, 80)
				.addBox(-3.0F, 2.0F, 2.0F, 6.0F, 7.0F, 2.0F), PartPose.ZERO);
		body.addOrReplaceChild("wing_left", CubeListBuilder.create().texOffs(64, 96)
				.addBox(1.0F, 1.0F, 2.0F, 1.0F, 8.0F, 5.0F), PartPose.ZERO);
		body.addOrReplaceChild("wing_right", CubeListBuilder.create().texOffs(64, 96).mirror()
				.addBox(-2.0F, 1.0F, 2.0F, 1.0F, 8.0F, 5.0F), PartPose.ZERO);

		PartDefinition rightArm = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16)
				.addBox(-1.5F, -2.0F, -1.5F, 3.0F, 8.0F, 3.0F), PartPose.offset(-5.5F, 2.0F, 0.0F));
		rightArm.addOrReplaceChild("right_sleeve", CubeListBuilder.create(), PartPose.ZERO);
		rightArm.addOrReplaceChild("right_elbow", CubeListBuilder.create().texOffs(0, 32)
				.addBox(-1.5F, 0.0F, -1.5F, 3.0F, 4.0F, 3.0F), PartPose.offset(0.0F, 6.0F, 0.0F));
		rightArm.addOrReplaceChild("shoulder_pad", CubeListBuilder.create().texOffs(64, 112)
				.addBox(-2.0F, -2.5F, -2.0F, 4.0F, 1.0F, 4.0F), PartPose.ZERO);

		PartDefinition leftArm = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 16).mirror()
				.addBox(-1.5F, -2.0F, -1.5F, 3.0F, 8.0F, 3.0F), PartPose.offset(5.5F, 2.0F, 0.0F));
		leftArm.addOrReplaceChild("left_sleeve", CubeListBuilder.create(), PartPose.ZERO);
		leftArm.addOrReplaceChild("left_elbow", CubeListBuilder.create().texOffs(12, 32).mirror()
				.addBox(-1.5F, 0.0F, -1.5F, 3.0F, 4.0F, 3.0F), PartPose.offset(0.0F, 6.0F, 0.0F));
		leftArm.addOrReplaceChild("shoulder_pad", CubeListBuilder.create().texOffs(64, 112).mirror()
				.addBox(-2.0F, -2.5F, -2.0F, 4.0F, 1.0F, 4.0F), PartPose.ZERO);

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

		return LayerDefinition.create(mesh, 128, 128);
	}

	// -------------------------------------------------------------- animation

	@Override
	public void setupAnim(AvatarRenderState state) {
		super.setupAnim(state);
		this.hat.visible = false;

		AvatarAppearance appearance = ClientRobloxState.appearanceFor(state.id);
		applyCosmetics(appearance);

		Entity entity = ClientRobloxState.entityFor(state.id);
		String style = appearance != null ? appearance.cosmetic(CosmeticCategory.ANIMATION).shape() : "";
		if (!RobloxifyConfig.get().avatarAnimations) {
			applySegments(0.0F, 0.0F);
			return;
		}
		applyAnimation(state, entity, style);

		if (entity != null) {
			String emote = ClientRobloxState.emoteOf(entity.getUUID());
			if (emote != null) {
				applyEmote(emote, ClientRobloxState.emoteProgress(entity.getUUID()));
			}
		}
	}

	private void applyCosmetics(AvatarAppearance appearance) {
		String hat = appearance == null ? "none" : appearance.cosmetic(CosmeticCategory.HAT).shape();
		String accessory = appearance == null ? "none" : appearance.cosmetic(CosmeticCategory.ACCESSORY).shape();

		this.cap.visible = "cap".equals(hat);
		this.topHat.visible = "tophat".equals(hat);
		this.topHatBrim.visible = "tophat".equals(hat);
		this.crown.visible = "crown".equals(hat);
		this.headphoneBand.visible = "headphones".equals(hat);
		this.headphoneLeft.visible = "headphones".equals(hat);
		this.headphoneRight.visible = "headphones".equals(hat);

		this.backpack.visible = "backpack".equals(accessory);
		this.wingLeft.visible = "wings".equals(accessory);
		this.wingRight.visible = "wings".equals(accessory);
		this.shoulderLeft.visible = "shoulder_pads".equals(accessory);
		this.shoulderRight.visible = "shoulder_pads".equals(accessory);
	}

	private void applyAnimation(AvatarRenderState state, Entity entity, String style) {
		boolean silly = "silly".equals(style);
		boolean ninja = "ninja".equals(style);
		float amp = silly ? 1.45F : ninja ? 1.1F : 1.0F;

		// --- vertical state -------------------------------------------------
		double velocityY = entity != null ? entity.getDeltaMovement().y : 0.0;
		boolean airborne = entity != null && !entity.onGround() && !entity.isInWater();
		int id = state.id;

		if (airborne) {
			wasAirborne.put(id, Boolean.TRUE);
		} else if (Boolean.TRUE.equals(wasAirborne.remove(id))) {
			landTimers.put(id, 6);
		}
		int land = landTimers.getOrDefault(id, 0);
		if (land > 0) {
			landTimers.put(id, land - 1);
		}

		// --- death ----------------------------------------------------------
		if (state.deathTime > 0.0F) {
			float t = Math.min(1.0F, state.deathTime / 12.0F);
			this.rightArm.xRot = Mth.lerp(t, this.rightArm.xRot, -2.4F);
			this.leftArm.xRot = Mth.lerp(t, this.leftArm.xRot, -2.4F);
			this.rightArm.zRot = -0.5F * t;
			this.leftArm.zRot = 0.5F * t;
			this.rightLeg.xRot = 0.35F * t;
			this.leftLeg.xRot = -0.35F * t;
			this.head.xRot = 0.5F * t;
			applySegments(0.9F * t, 0.0F);
			return;
		}

		// --- crouch ---------------------------------------------------------
		if (state.isCrouching) {
			this.rightArm.xRot -= 0.35F;
			this.leftArm.xRot -= 0.35F;
			applySegments(0.7F, 0.6F);
			return;
		}

		// --- airborne -------------------------------------------------------
		if (airborne) {
			if (velocityY > 0.02) {
				// Jump: arms swept back, legs tucked slightly.
				this.rightArm.xRot = -2.2F;
				this.leftArm.xRot = -2.2F;
				this.rightArm.zRot = -0.25F;
				this.leftArm.zRot = 0.25F;
				this.rightLeg.xRot = -0.35F;
				this.leftLeg.xRot = -0.35F;
				applySegments(0.8F, 0.9F);
			} else {
				// Fall: arms out wide.
				this.rightArm.xRot = -0.4F;
				this.leftArm.xRot = -0.4F;
				this.rightArm.zRot = -1.15F;
				this.leftArm.zRot = 1.15F;
				this.rightLeg.xRot = 0.25F;
				this.leftLeg.xRot = -0.25F;
				applySegments(0.35F, 0.3F);
			}
			return;
		}

		// --- grounded locomotion --------------------------------------------
		float speed = state.walkAnimationSpeed;
		float pos = state.walkAnimationPos;
		boolean running = speed > 0.62F;
		float cycleAmp = (running ? 1.45F : 0.95F) * amp;
		float swing = Mth.cos(pos * 0.6662F) * cycleAmp * Math.min(speed, 1.0F);

		this.rightArm.xRot = -swing;
		this.leftArm.xRot = swing;
		this.rightLeg.xRot = swing;
		this.leftLeg.xRot = -swing;

		// Stiff Roblox limbs: only a small bend, mostly at the elbows and knees.
		applySegments(Math.max(0.0F, -swing) * 0.35F, Math.max(0.0F, swing) * 0.45F);

		// Idle sway so the avatar is never perfectly still.
		float idle = Mth.sin(state.ageInTicks * 0.07F) * 0.035F;
		this.rightArm.zRot = -idle * 2.0F;
		this.leftArm.zRot = idle * 2.0F;
		this.rightArm.xRot += idle * 0.6F;
		this.leftArm.xRot -= idle * 0.6F;

		if (running) {
			this.body.xRot = 0.14F;
			this.rightArm.zRot -= 0.12F;
			this.leftArm.zRot += 0.12F;
		} else {
			this.body.xRot = idle * 0.5F;
		}

		if (ninja) {
			this.rightArm.xRot = this.rightArm.xRot * 0.35F - 0.55F;
			this.leftArm.xRot = this.leftArm.xRot * 0.35F - 0.55F;
			this.body.xRot = 0.2F;
		}

		if (land > 0) {
			// Landing squash: knees bend and the body dips.
			float squash = land / 6.0F;
			this.rightLeg.xRot = -0.5F * squash;
			this.leftLeg.xRot = -0.5F * squash;
			applySegments(0.2F, 1.1F * squash);
			this.body.y += 0.6F * squash;
			this.head.y += 0.6F * squash;
		}

		if (silly) {
			this.body.yRot = Mth.sin(state.ageInTicks * 0.3F) * 0.12F;
			this.head.yRot += Mth.sin(state.ageInTicks * 0.45F) * 0.25F;
		}
	}

	private void applySegments(float elbow, float knee) {
		this.rightElbow.xRot = elbow;
		this.leftElbow.xRot = elbow;
		this.rightKnee.xRot = knee;
		this.leftKnee.xRot = knee;
	}

	private void applyEmote(String emote, float progress) {
		float phase = progress * (float) Math.PI * 4.0F;
		switch (emote) {
			case "wave" -> {
				this.rightArm.xRot = -2.6F;
				this.rightArm.zRot = -0.3F + Mth.sin(phase * 1.6F) * 0.5F;
				this.rightElbow.xRot = 0.6F;
			}
			case "dance" -> {
				this.rightArm.xRot = -1.2F + Mth.sin(phase) * 0.9F;
				this.leftArm.xRot = -1.2F - Mth.sin(phase) * 0.9F;
				this.rightArm.zRot = -0.35F;
				this.leftArm.zRot = 0.35F;
				this.head.yRot = Mth.sin(phase) * 0.45F;
				this.body.yRot = Mth.sin(phase) * 0.2F;
				this.rightKnee.xRot = 0.4F;
				this.leftKnee.xRot = 0.4F;
			}
			case "point" -> {
				this.rightArm.xRot = -1.57F;
				this.rightArm.yRot = -0.4F;
				this.rightElbow.xRot = 0.0F;
			}
			case "jump" -> {
				this.rightArm.xRot = -2.2F;
				this.leftArm.xRot = -2.2F;
			}
			case "fall" -> {
				this.rightArm.zRot = -1.2F;
				this.leftArm.zRot = 1.2F;
			}
			case "walk" -> applySegments(0.3F, 0.4F);
			case "run" -> {
				this.rightArm.xRot = -1.1F;
				this.leftArm.xRot = 1.1F;
				this.rightLeg.xRot = 1.1F;
				this.leftLeg.xRot = -1.1F;
				applySegments(0.5F, 0.8F);
			}
			default -> {
			}
		}
	}
}
