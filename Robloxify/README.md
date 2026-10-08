# Robloxify

**Minecraft Java Edition 26.2 + Fabric, with Roblox ported into it.**

Minecraft stays the game: same world, same blocks, same mobs, same survival loop. Robloxify layers a
complete Roblox-style platform on top of it - a blocky avatar with a real customisation system, a
Roblox animation language, a launcher-style UI, six Experiences, Robux, a badge collection and
platform notifications.

The target reaction:

> "Wait. This is Minecraft... but it feels like Roblox."

---

## Version facts (verified, not guessed)

| Component | Version |
|---|---|
| Minecraft | **26.2** |
| Java | **25** |
| Fabric Loader | **0.19.5** |
| Fabric API | **0.161.0+26.2** |
| Fabric Loom | **1.17.21** |
| Gradle | **9.5.1** |

Minecraft 26.2 ships **unobfuscated** (Mojang-named) classes - the client jar has 10,952 classes and
zero obfuscated packages. Therefore this project uses:

* **no Yarn mappings** (no 26.x builds exist),
* **no `loom.officialMojangMappings()`**,
* **no `mappings` line at all** in `build.gradle`,
* **`implementation` everywhere, never `modImplementation`**.

---

## Requirements & installation

1. Fabric Loader 0.19.5 for Minecraft 26.2.
2. Put the built jar **and** Fabric API 0.161.0+26.2 into `.minecraft/mods/`.
3. Launch. The log shows `Robloxify initialized!` and `Robloxify client initialized!`.

## Building

```bash
cd Robloxify
./gradlew clean build
```

The jar lands in `build/libs/robloxify-1.0.0.jar`.

> `gradle/wrapper/gradle-wrapper.properties` sets `validateDistributionUrl=false` because Gradle's
> wrapper validation uses a `HEAD` request that the proxy used to build this project rejects. The
> `GET` download works normally. Set it back to `true` if your network allows it.

---

## The Roblox layer

### 1. Avatar - generated, not a reskin

The avatar is a blocky Roblox-style character: cube head, thin limbs, and **segmented arms and legs**
with elbows and knees that bend during the walk cycle.

The skin texture is **generated at runtime** (`AvatarSkinFactory`) from the equipped cosmetics into a
128x128 image, registered as a `DynamicTexture` and cached per appearance (oldest-first eviction, so
it cannot grow unbounded). That is what makes the customisation system real instead of a fixed skin:

* **Body** - 5 skin tones
* **Face** - 6 original expressions (neutral, happy, surprised, silly, angry, sleepy), drawn pixel by
  pixel onto the front of the head
* **Hair** - 6 hair colours, including bald
* **Shirt** - 6 shirts with solid, trim, stripe and speckle patterns
* **Pants** - 4 pairs
* **Hat** - cap, top hat, golden crown, headphones (real geometry, toggled on the model)
* **Accessory** - adventure backpack, angel wings, shoulder pads
* **Effect** - sparkle, flame and gold auras (light particle emitters)
* **Animation** - Classic, Silly and Ninja animation styles

The model extends `PlayerModel`, so held items, armour, name tags, sneaking and swimming all keep
working. The model swap happens at the head of `LivingEntityRenderer.submit` (not during extraction)
because 26.2 extracts every entity into a render state first and only submits afterwards.

### 2. Animation language

Deliberately stiff and readable, like a game character rather than a person: idle sway, walk, run with
a forward lean, jump with swept-back arms, fall with arms out wide, a landing squash, crouch, and a
limp collapse on death. Emotes (`wave`, `dance`, `point`, ...) broadcast to every client.

### 3. UI - a launcher, not a chest GUI

Every Robloxify screen is built from custom `RobloxButton` widgets and flat drawn panels, so none of
it looks like a vanilla Minecraft GUI:

```
ROBLOXIFY                                    [ R$ 1,250 ]
────────────────────────────────────────────────────────
 Home       │  WELCOME BACK
 Play       │  Steve
 Avatar     │  Balance      Badges        Experiences
 Shop       │  R$ 1,250      3 / 14        2
 Badges     │  ─────────────────────────────────────
 Items      │  The loop: play an Experience, earn Robux,
 Config     │  collect badges, customise your avatar.
```

Sections: **Home, Play, Avatar, Shop, Badges, Items, Config**. A Robux chip with a drawn `R$` mark
sits in the header, and the currency is shown as `R$ 1,250` everywhere.

### 4. Experiences

Six Experiences, each with its own identity, arena and objective:

| Experience | Type | Objective | Reward |
|---|---|---|---|
| **Obby** | Platforming | Checkpoints, bounce pads, disappearing platforms, a moving platform, lava hazards | R$ 250 |
| **Sword Arena** | Combat | Clear 3 waves of mobs | R$ 300 |
| **Speed Race** | Racing | Hit every checkpoint inside 90s | R$ 300 |
| **Tycoon** | Progression | Stand on your plot, collect 100 income | R$ 350 |
| **Survival** | Waves | Survive 3 escalating waves | R$ 400 |
| **Minigame** | Challenge | Break all 8 targets before the clock runs out | R$ 200 |

Arenas are built from ordinary Minecraft blocks plus Robloxify blocks. The tick handler returns
immediately when nobody is playing and only inspects players with an active session.

### 5. Robloxify blocks

Real, placeable, Creative-available blocks that put Roblox in the actual world:

`stud`, `checkpoint`, `bounce_pad` (launches you up), `finish_pad`, `spawn_pad`,
`disappearing_platform` (vanishes when stepped on, comes back).

### 6. Robux

A fictional in-game currency shown as `R$ 1,250`. Earned from badges, Experiences and studs; spent in
the Avatar Shop. **No real money, no microtransactions, no Roblox account, no network calls.**
In Creative mode cosmetics are free, so testing never needs grinding.

### 7. Badges

14 badges with icon, title, description, condition, unlock timestamp and a collection screen:
`Welcome!`, `First Block`, `Mining Experience`, `Robloxian`, `Builder`, `Obby Beginner`,
`Obby Survivor`, `Speedrunner`, `First Experience`, `Explorer`, `Avatar Collector`, `Millionaire`,
`Ender Robloxian`, and the secret `Juaninho The Myth`.

### 8. Notifications

A Roblox-style notification stack (slide-in, accent bar, icon, reward) instead of chat spam:
`Badge Awarded!`, `Checkpoint reached`, `Experience complete!`, `New Avatar Item!`, `You died!`.

### 9. Death and reset inside Experiences

Vanilla Minecraft death is untouched. Inside an Experience, dying shows a `You died!` notification
and the player respawns **at their last checkpoint** rather than the world spawn. Falling out of the
world does the same.

---

## Commands

| Command | Notes |
|---|---|
| `/robloxify` or `/robloxify help` | Command list |
| `/robloxify avatar <on\|off\|toggle>` | Switch between Minecraft player and Roblox avatar |
| `/robloxify robux get` | Show your balance |
| `/robloxify robux add\|set <amount>` | OP only (Gamemaster) |
| `/robloxify cosmetics` | Full catalogue with owned/equipped state |
| `/robloxify cosmetics equip <id>` | Equip an owned item |
| `/robloxify cosmetics buy <id>` | OP only |
| `/robloxify badge list` | Badge collection |
| `/robloxify badge grant <id>` | OP only |
| `/robloxify emote <wave\|dance\|point\|...>` | Emote |
| `/robloxify experience [list]` | List Experiences |
| `/robloxify experience start <id>` / `stop` | Play / leave |
| `/robloxify config list` | Feature toggles |
| `/robloxify config <key> <true\|false>` | OP only |
| `/robloxify reload` | OP only, reload config |
| `/robloxify easteregg npc` / `area` | OP only |

26.2 replaced numeric op levels with a permission-set model, so OP checks use
`Permissions.COMMANDS_GAMEMASTER`.

### Key bindings

`R` open Robloxify - `V` toggle avatar - `X` wave. All rebindable, category "Robloxify".

---

## Configuration

`config/robloxify.json`, editable in the **Config** tab or with `/robloxify config`:

`roblox_avatar`, `avatar_animations`, `roblox_physics`, `roblox_hud`, `show_robux`, `show_badges`,
`notifications`, `experiences`, `badges`, `robux`, `roblox_ui`, `roblox_sounds`, `emotes`,
`easter_eggs`. Plus `startingRobux`, `hudOffsetX`, `hudOffsetY`.

Player data lives server-side in `<server dir>/robloxify_data.json` and is only written when it
actually changed (at most every 10 seconds).

---

## Performance

Built for a modest laptop:

* The HUD and notifications allocate nothing per frame beyond short strings.
* The Experience tick returns immediately when no session is active; only session players are checked.
* Moving platforms move once every 30 ticks; vanishing platforms are tracked in a small map.
* Skin textures are generated once per appearance and cached with oldest-first eviction.
* Cosmetic particles run only for players who actually own an effect.
* No shaders, no custom render pipelines, no chunk scanning, no entity spam, small textures (16x16
  blocks, one 128x128 avatar skin).

---

## Verification - what was actually tested

Nothing here is a "should work".

1. **Every version** was checked against `meta.fabricmc.net`, `maven.fabricmc.net` and Mojang's
   `version_manifest_v2.json` before being written into `gradle.properties`.
2. **Every API used was read from the real 26.2 artifacts** - the client jar, the Fabric API module
   jars, and a full `./gradlew genSources` decompilation. That is how 26.x changes were found, e.g.
   `Screen.render` -> `extractRenderState`, `Minecraft.setScreen` -> `Minecraft.gui.setScreen`,
   `EntityType` -> `EntityTypes`, the new `Mannequin` entity, `KeyMapping.Category`, and the new
   `PermissionSet`/`Permissions` model that replaced numeric op levels.
3. **`./gradlew clean build` produces `BUILD SUCCESSFUL`.**
4. **A dedicated server was booted with the mod**: `Robloxify initialized!` and `Done (0.236s)!` with
   **zero errors or exceptions**.
5. **Commands were executed on the live server**: the Experience catalogue printed all six
   Experiences with their `R$` rewards, and `config list` printed all 14 toggles.
6. **Bugs found by actually running it** (not by reading): missing block IDs in
   `BlockBehaviour.Properties` (26.2 requires `setId`), `CustomPacketPayload.createType` silently
   forcing the `minecraft:` namespace, a null parent path when persisting player data, and a static
   initialiser ordering bug in the block registry.
7. **Mixin targets were verified against the compiled class files** with `javap -s`:
   `LivingEntityRenderer.model`, its constructor descriptor, the `submit` descriptor, and
   `AvatarRenderer.getTextureLocation`.

### Known limitations

* **The client was not launched in this environment.** There is no GPU, no X server and no OpenGL
  here, so the visual side (avatar rendering, generated skins, screens, HUD, notifications) could not
  be run. It compiles, mixin targets are verified against the real class files, and the server side is
  verified at runtime - but the client has not been executed. That is the honest status.
* **Sound files are placeholders.** Minecraft only decodes Ogg Vorbis and no Vorbis encoder was
  available in the build environment. The 15 Robloxify sound *events* are real registered events, but
  `sounds.json` maps them to vanilla Minecraft sounds via `"type": "event"`. Dropping your own
  `.ogg` files into `assets/robloxify/sounds/` and pointing `sounds.json` at them is a drop-in change.
* **First-person hand** still uses the vanilla arm; the Roblox arm shows in third person.
* **Armour** renders on the Roblox avatar using vanilla armour meshes, so it looks slightly oversized
  on the thinner Roblox limbs.
* **Moving platforms** carry the player by a one-block teleport per step, which reads correctly but is
  not a physics-accurate platform.
* **Knockback** is not customised; vanilla knockback applies.
* 26.2 is new. If a future patch reshuffles these APIs again the build will fail loudly rather than
  silently misbehave.

---

## Architecture

```
com/robloxify/
  Robloxify.java            common entrypoint
  RobloxifyClient.java      client entrypoint
  avatar/                   Cosmetic, CosmeticCatalog, CosmeticCategory, AvatarAppearance
  config/                   RobloxifyConfig (JSON)
  data/                     Profile + RobloxifyData (server-side persistence)
  badge/                    Badge + Badges catalogue (14 badges)
  util/                     Robux formatting, Emotes
  net/                      RobloxifyPayloads + RobloxifyNetworking
  server/                   RobloxifyService (authoritative logic)
  experience/               ExperienceType, ArenaBuilder, ExperienceManager, Mover
  event/                    RobloxifyEvents (Fabric wiring, Creative tab)
  command/                  RobloxifyCommands
  world/                    RobloxifyBlocks + world/block/ (6 blocks)
  easteregg/                EasterEggs
  sound/                    RobloxifySounds (15 events)
  client/                   state, networking, keybinds, sounds, effects
  client/hud/               RobloxHud
  client/notification/      RobloxNotification + NotificationManager
  client/ui/                theme, RobloxUi, RobloxButton, base screen, 7 sections
  client/avatar/            RobloxAvatarModel + AvatarSkinFactory
  mixin/                    AvatarRendererMixin, LivingEntityRendererMixin
```

Everything that can be client-side is client-side (HUD, menus, avatar rendering, skins, sounds,
notifications). Anything shared - Robux, badges, equipped cosmetics, avatar state, Experience
progress - is server-authoritative and synced with typed custom payloads.

---

## Licence

MIT. Robloxify is a fan-made parody mod, not affiliated with or endorsed by Roblox Corporation or
Mojang. **No proprietary Roblox assets are used or downloaded**: every texture is generated by this
project, the faces and geometry are original, the sounds are placeholders mapped to Minecraft's own
audio, and Robux is entirely fictional with no real-money component.
