# Jump Duplicator

Jump Duplicator is a server-side Fabric mod for Minecraft Java Edition 26.2. When a survival or adventure player starts a valid jump, nearby non-player entities are copied around the player.

## Installation

1. Install Fabric Loader 0.19.3 or newer for Minecraft 26.2.
2. Install Fabric API for Minecraft 26.2 on the server.
3. Put the built `jumpduplicator` JAR into the server `mods` folder.
4. Start the server once to create `config/jumpduplicator.json`.

The mod runs its logic on the logical server. Clients do not need the mod installed for multiplayer servers.

## Building

Use the Gradle wrapper:

```bash
./gradlew build
```

On Windows:

```powershell
.\gradlew.bat build
```

The remapped mod JAR is written to `build/libs/`.

## Commands

Root command aliases:

- `/jumpduplicator`
- `/jumpdupe`
- `/jd`

Available commands:

- `/jumpduplicator help`
- `/jumpduplicator status`
- `/jumpduplicator enable`
- `/jumpduplicator disable`
- `/jumpduplicator reload`
- `/jumpduplicator radius <value>`
- `/jumpduplicator offset <value>`
- `/jumpduplicator amount <value>`
- `/jumpduplicator cooldown <ticks>`
- `/jumpduplicator maxentities <amount>`
- `/jumpduplicator notify <true|false>`
- `/jumpduplicator duplicate`

Administrative commands require the configured vanilla permission level. `/jumpduplicator duplicate` manually runs duplication for the executing player.

## Configuration

The config file is `config/jumpduplicator.json` and is created automatically on first launch.

```json
{
  "enabled": true,
  "radius": 16.0,
  "verticalOffset": 1.0,
  "duplicationAmount": 1,
  "cooldownTicks": 0,
  "maximumEntitiesPerJump": 100,
  "duplicateItems": true,
  "duplicateOnlySingleItems": true,
  "duplicateExperienceOrbs": true,
  "duplicatePassiveMobs": true,
  "duplicateHostileMobs": true,
  "duplicateVillagers": true,
  "duplicateProjectiles": true,
  "duplicateVehicles": true,
  "duplicateArmorStands": true,
  "duplicateTnt": true,
  "duplicateFallingBlocks": true,
  "duplicateOtherEntities": true,
  "copyVelocity": true,
  "copyCustomNbt": true,
  "requirePermission": false,
  "permissionLevel": 2,
  "notifyPlayer": false,
  "skipBosses": true,
  "skipTamedEntities": false,
  "skipNamedEntities": false,
  "safeSpawnCheck": true
}
```

Invalid values are clamped back to safe ranges during load. Use `/jumpduplicator reload` to reload the config without restarting.

## Performance Warning

Entity duplication can create severe server lag. Keep `radius`, `duplicationAmount`, and `maximumEntitiesPerJump` conservative, especially on public servers or farms with many item entities.

## Example Behavior

If a dropped stack of oak logs is within the radius and a player starts a valid jump, a copied item entity appears on the same X/Z position with `verticalOffset` applied. By default, item stack copies contain only 1 item, even when the original stack contains more.
