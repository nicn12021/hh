# Robloxify

**Minecraft Java Edition 26.2 + Fabric, progressively invaded by Roblox.**

Minecraft stays the base game. Robloxify is a Fabric mod that layers blocky avatars, Robux, badges,
Experiences, obbies, emotes and a few extremely suspicious NPCs on top of it. The goal is the feeling:

> "I'm playing Minecraft..."
>
> "...WHY IS THERE ROBLOX IN HERE?"

---

## ⚠️ Read this first: this project targets Minecraft 26.2 *exactly*

Minecraft 26.2 belongs to the 26.x generation which ships **unobfuscated (Mojang-named) classes**.
That has concrete consequences, and this project was built around them:

| Thing | Status in this project |
|---|---|
| Yarn mappings | **Not used.** Yarn has no 26.x builds at all. |
| `loom.officialMojangMappings()` | **Not used.** There is nothing to map. |
| Any manual mapping dependency | **None.** |
| `intermediary` | Reported as `0.0.0` by Fabric's meta for 26.2 - i.e. identity. |
| Mappings line in `build.gradle` | **Absent on purpose.** Only `minecraft "com.mojang:minecraft:26.2"`. |
| `modImplementation` | **Not used anywhere.** Everything is plain `implementation`. |

Proof, straight from the 26.2 client jar (10 952 classes, 0 obfuscated packages):

```
net/minecraft/client/Minecraft.class
net/minecraft/client/gui/GuiGraphicsExtractor.class
net/minecraft/client/renderer/entity/player/AvatarRenderer.class
net/minecraft/client/model/player/PlayerModel.class
net/minecraft/world/entity/EntityTypes.class
```

Because the game is already deobfuscated, `implementation` and `modImplementation` are equivalent for
this target, which is exactly why the requested `implementation`-only setup works here.

---

## Requirements

| Component | Version |
|---|---|
| Minecraft | **26.2** |
| Java | **25** (26.2 requires `java-runtime-epsilon`, major 25) |
| Fabric Loader | **0.19.5** |
| Fabric API | **0.161.0+26.2** |
| Fabric Loom | **1.17.21** |
| Gradle | **9.5.1** |

All of the above were verified against `meta.fabricmc.net`, `maven.fabricmc.net` and Mojang's
`version_manifest_v2.json` at build time, not guessed.

---

## Installation

1. Install Fabric Loader 0.19.5 for Minecraft 26.2.
2. Drop the built jar plus **Fabric API 0.161.0+26.2** into `.minecraft/mods/`.
3. Launch the game. The log shows:

```
Robloxify initialized!
Robloxify client initialized!
```

---

## Building

```bash
cd Robloxify
./gradlew clean build
```

On Windows:

```powershell
.\gradlew.bat clean build
```

The jar lands in `build/libs/robloxify-1.0.0.jar`.

### Running a dev environment

```bash
./gradlew runClient   # client (needs a GPU/display)
./gradlew runServer   # dedicated server
```

> Note: `gradle/wrapper/gradle-wrapper.properties` has `validateDistributionUrl=false`. Gradle's
> wrapper validation issues a `HEAD` request, which the CI egress proxy used to build this project
> rejects. The download itself (`GET`) works normally. Flip it back to `true` if your network allows it.

---

## Commands

All commands live under `/robloxify`. They are server-side (they work in single-player through the
integrated server, and on a dedicated server).

| Command | Description |
|---|---|
| `/robloxify` / `/robloxify info` | Mod version and command list |
| `/robloxify robux get` | Show your balance |
| `/robloxify robux add <amount>` | Add (or subtract) Robux - test helper |
| `/robloxify robux set <amount>` | Set your balance - test helper |
| `/robloxify avatar <on\|off\|toggle>` | Switch between the Minecraft player and the Roblox avatar |
| `/robloxify badge list` | List every badge and its state |
| `/robloxify badge grant <id>` | Grant a badge (test helper) |
| `/robloxify emote <name>` | Play an emote: `wave`, `dance`, `point`, `idle`, `walk`, `run`, `jump`, `fall` |
| `/robloxify obby start` / `stop` | Build and start/stop an obby Experience |
| `/robloxify config list` | Show every feature toggle |
| `/robloxify config <key> <true\|false>` | Flip a feature toggle |
| `/robloxify easteregg npc` | Spawn a very suspicious NPC |
| `/robloxify easteregg area` | Build the secret Roblox Experience room |

### Key bindings (all rebindable, category "Robloxify")

| Key | Action |
|---|---|
| `R` | Open the Robloxify UI |
| `V` | Toggle the Roblox avatar |
| `X` | Wave emote |

---

## Configuration

`config/robloxify.json` is created on first launch. Every feature can be toggled independently,
both from the in-game **Settings** tab and from `/robloxify config <key> <value>`.

| Key | Default | Effect |
|---|---|---|
| `roblox_avatar` | `true` | Blocky Roblox avatar rendering |
| `roblox_hud` | `true` | Robux HUD chip |
| `robux` | `true` | Robux currency system |
| `badges` | `true` | Badge system |
| `experiences` | `true` | Experiences / obby HUD |
| `roblox_sounds` | `true` | Robloxify sound effects |
| `emotes` | `true` | Emotes |
| `roblox_ui` | `true` | The `R` menu |
| `easter_eggs` | `true` | Studs, the suspicious NPC, secret areas |

Extra values: `startingRobux` (default `1250`), `hudOffsetX` / `hudOffsetY`.

Player data (Robux, badges with unlock timestamps, stats, obby records, owned cosmetics) is stored
server-side in `<server dir>/robloxify_data.json` and is only written when something actually changed.

---

## Features

### 1. Foundation
`com.robloxify.Robloxify` (common) and `com.robloxify.RobloxifyClient` (client) initializers, a
`fabric.mod.json`, and a mixin config with exactly two client mixins.

### 2. Configuration
`RobloxifyConfig` - a small Gson-backed JSON file. No third-party libraries: Gson already ships with
Minecraft.

### 3. Roblox HUD
A discrete Robux chip (`◈ 1,250`), an obby progress panel and a badge banner. Drawn with plain
rectangles and text through `HudElementRegistry`, so it costs nothing when idle and respects GUI scale.

### 4. Robux
A completely fictional in-game currency. **No real money, no microtransactions, no Roblox account,
no Roblox API, no network calls.** It is earned from badges, obbies and studs, and spent in the Shop.

### 5. Roblox UI
Six tabs built with vanilla widgets, opened with `R`: **Avatar, Robux, Shop, Badges, Profile, Settings**.
Vanilla menus are untouched - this is an extra layer, not a replacement.

### 6. Roblox Avatar
`RobloxAvatarModel extends PlayerModel`, so held items, armour layers, name tags, sneaking, swimming
and first/third person all keep working. The geometry is blocky: cube head, thinner limbs, and
**segmented arms and legs** (elbows and knees bend with the walk cycle).

The model is swapped at the start of `LivingEntityRenderer.submit` because 26.2 extracts every entity
into a render state *first* and only submits later - swapping during extraction would apply the wrong
model. The texture is swapped through `AvatarRenderer.getTextureLocation`.

### 7. Badges
| Badge | Condition |
|---|---|
| First Block | Break your first block |
| Mining Experience | Mine 100 blocks |
| Robloxian | Activate the Roblox avatar |
| Builder | Place 500 blocks |
| Obby Survivor | Complete an obby |
| Ender Robloxian | Enter the End wearing the Roblox avatar |
| Juaninho The Myth | *"Bro thought he cooked."* (secret) |

Each badge has an id, name, description, icon, condition, unlock state and unlock timestamp.

### 8. Experiences - the Obby
`/robloxify obby start` builds an 8-platform obby out of plain vanilla blocks in front of you, with 3
gold-block checkpoints. It tracks your timer, teleports you back to the last checkpoint if you fall,
and rewards the badge + 250 Robux on completion. Only players with an active session are ever
inspected, so the tick cost is effectively zero the rest of the time.

### 9. Robloxifying the world
- **Stud block** (`robloxify:stud`) - placeable, clicks, and pays out Robux.
- **Suspicious NPC** - a `mannequin` (new in 26.x) named *Juaninho The Myth*, which renders through
the player renderer and therefore shows up as a blocky Roblox character.
- **Secret Roblox Experience** - a hidden stud room with end-rod particles.
- Badge particles on completion.

Minecraft still looks like Minecraft. The invasion is gradual.

### 10. Animations
Idle, walk, run, jump, fall are derived from the vanilla walk cycle; `wave`, `dance` and `point` are
emote overrides. `/robloxify emote wave` broadcasts to every client, so other players see it too.

### 11. Sound
Eight registered sound events: `ui_open`, `ui_close`, `purchase`, `badge_unlock`, `checkpoint`,
`experience_complete`, `avatar_switch`, `oof`. See *Known limitations* about the audio files.

### 12. Easter eggs
- **Stud** - the secret block.
- **OOF** - a textual `oof` plus a sound when you die.
- **Suspicious NPC** - Juaninho, who knows you are not ready.
- **Roblox Experience** - the secret area.
- **Juaninho The Myth** - deliberately absurd: right-click the suspicious NPC while wearing the
  Roblox avatar, in the End, holding a stud block.

---

## Performance

Written for a modest machine:

- The HUD allocates nothing per frame beyond a short string.
- The obby tick handler returns immediately when no session is active.
- Per-player data is flushed at most every 10 seconds, and only when dirty.
- Rendering only swaps a model reference and returns a texture id.
- No chunk scanning, no per-tick entity loops, no shaders, no custom render pipelines.
- Cosmetics particles only run for players who own them.

---

## Verification - what was actually tested

Nothing here is a claim of "it should work". Concretely:

1. **Every version was checked against a live registry** (`meta.fabricmc.net`, `maven.fabricmc.net`,
   Mojang's `version_manifest_v2.json`) before being written into `gradle.properties`.
2. **Every Minecraft and Fabric API signature used was read from the real 26.2 artifacts** - the
   client jar, the Fabric API module jars, and a full `./gradlew genSources` decompilation. This is
   how things like `Screen.extractRenderState` (which replaced `render`), `Minecraft.gui.setScreen`
   (the old `setScreen` moved), `KeyMapping.Category`, `EntityTypes` and `Mannequin` were discovered.
3. **`./gradlew clean build` produces `BUILD SUCCESSFUL`.**
4. **A dedicated server was actually booted with the mod**, printing `Robloxify initialized!` and
   `Done (0.223s)!` with no exceptions.
5. **Commands were executed on the live server**, including `/robloxify config list`,
   `/robloxify config roblox_hud false` (verified persisted to `config/robloxify.json`) and
   `/robloxify`.
6. **The mixin injection points were verified against the compiled class files** with `javap -s`:
   `LivingEntityRenderer.model` -> `Lnet/minecraft/client/model/EntityModel;`,
   the constructor -> `(Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;Lnet/minecraft/client/model/EntityModel;F)V`,
   `submit` -> `(...LivingEntityRenderState;...PoseStack;...SubmitNodeCollector;...CameraRenderState;)V`,
   and `AvatarRenderer.getTextureLocation` -> `(...AvatarRenderState;)Lnet/minecraft/resources/Identifier;`.

### Known limitations

- **The client was not launched in this environment.** There is no GPU, no X server and no OpenGL
  here, so the visual side (avatar rendering, screens, HUD) could not be run. It compiles, the mixin
  targets are verified against the real class files, and the server side is verified at runtime - but
  the client has not been executed. That is the honest status.
- **Sound files are placeholders.** Minecraft only decodes Ogg Vorbis, and no Vorbis encoder was
  available in the build environment. The eight Robloxify sound *events* are real registered events,
  but `sounds.json` maps them to vanilla Minecraft sound events via `"type": "event"`. Swapping in
  your own `.ogg` files under `assets/robloxify/sounds/` and pointing `sounds.json` at them is a
  drop-in change.
- **First-person hand.** In first person the vanilla arm is still drawn; the Roblox arm shows in third
  person. The avatar is a third-person feature by nature.
- **Armour geometry.** Armour layers render on the Roblox avatar using vanilla armour meshes, so they
  look slightly oversized on the thinner Roblox limbs.
- **Cosmetics are per-player.** Cap and shades are shown based on the local player's owned items,
  because only the local profile is synced in full.
- **`gradle-wrapper.properties` uses `validateDistributionUrl=false`** (see the note in *Building*).
- 26.2 is very new, and this mod uses several APIs that changed in 26.x. If a future 26.2 patch
  reshuffles them again, the build will fail loudly rather than silently misbehave.

---

## Architecture

```
com/robloxify/
  Robloxify.java              common entrypoint
  RobloxifyClient.java        client entrypoint
  config/                     RobloxifyConfig (JSON)
  data/                       Profile + RobloxifyData (server-side persistence)
  badge/                      Badge + Badges catalogue
  shop/                       ShopItem + Shop
  net/                        RobloxifyPayloads + RobloxifyNetworking
  server/                     RobloxifyService (authoritative logic)
  experience/                 ObbyManager
  event/                      RobloxifyEvents (Fabric event wiring)
  command/                    RobloxifyCommands
  world/                      RobloxifyBlocks (the stud)
  easteregg/                  EasterEggs
  sound/                      RobloxifySounds
  util/                       Emotes
  client/                     client state, networking, keybinds, sounds, effects
  client/hud/                 RobloxHud
  client/ui/                  theme, base screen, 6 tabs
  client/avatar/              RobloxAvatarModel
  mixin/                      AvatarRendererMixin, LivingEntityRendererMixin
```

Everything that can be client-side is client-side (HUD, menus, avatar visuals, sounds, settings).
Anything shared - Robux, badges, avatar state, obbies - is server-authoritative and synced with typed
custom payloads.

---

## Licence

MIT. Robloxify is a fan-made parody mod. It is not affiliated with, endorsed by, or connected to
Roblox Corporation or Mojang. No Roblox assets are used or downloaded: all textures are generated
by this project, and the Robux currency is entirely fictional with no real-money component.
