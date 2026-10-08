# Robloxify

Minecraft Java Edition **26.2** + Fabric, progressively invaded by Roblox.

The mod project lives in [`Robloxify/`](Robloxify/).

- **Build:** `cd Robloxify && ./gradlew clean build`
- **Jar:** `Robloxify/build/libs/robloxify-1.0.0.jar`
- **Full documentation:** [Robloxify/README.md](Robloxify/README.md)

Requirements: Minecraft 26.2, Java 25, Fabric Loader 0.19.5, Fabric API 0.161.0+26.2,
Fabric Loom 1.17.21, Gradle 9.5.1.

Minecraft 26.2 is unobfuscated (Mojang names), so this project uses **no mappings** and **no
`modImplementation`** - only `implementation`, exactly as intended.
