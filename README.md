# Magical Minions

A NeoForge 1.21.1 mod about conjuring and customizing your own minions: rune golems, ember imps,
bonebound acolytes, starlight wisps and mosslings. Build them part by part at the Conjurer's Bench,
mix parts across lineages, bond with them and take them adventuring.

## Stack

- Minecraft 1.21.1, NeoForge 21.1.x (ModDevGradle)
- [GeckoLib](https://github.com/bernie-g/geckolib) 4.9 for animated models
- Models and animations made in [Blockbench](https://www.blockbench.net/) with the GeckoLib Animation Utils plugin
- Java 21

## Building and running

```bash
./gradlew build          # jar in build/libs/
./gradlew runClient      # dev client
./gradlew runServer      # dev server
./gradlew runData        # data generators -> src/generated/resources
```

On Windows use `gradlew.bat`. IntelliJ IDEA: open the folder and import the Gradle project.

## Layout

- `src/main/java/com/aomarai/magicalminions/` mod code
- `src/main/resources/assets/magicalminions/` models (`geo/`), animations, textures, lang
- `src/main/resources/data/magicalminions/` datapack registries (parts, lineages, materials, runes)
- `src/main/templates/META-INF/neoforge.mods.toml` mod metadata, filled in from `gradle.properties`

## License

[AGPL-3.0](LICENSE)
