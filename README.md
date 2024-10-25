![kotlin_version](https://img.shields.io/badge/kotlin-2.0.0-blueviolet?style=flat-square)
![kotlin_version](https://img.shields.io/badge/java-21-blueviolet?style=flat-square)
![minecraft_version](https://img.shields.io/badge/minecraft-1.20.1-green?style=flat-square)
![platforms](https://img.shields.io/badge/platform-spigot-blue?style=flat-square)

Originally [DeadSouls](https://github.com/Astra-Interactive/DeadSoulsKT)

> [!CAUTION]
> Java 21 and Paper 1.21.1 only supported! Use other versions on your own risk!

# SoulKeeper

Plugin to prevent XP and/or Items loss on your death. The items will not despawn no matter what - lava, fire, void!

All your inventory will be safely stored inside a soul until someone will take it(in case of soul is freed).

## Screenshot

![souls.png](media%2Fsouls.png)

## How to create a soul

- **Die** (in minecraft)
- Find the place of death. Or use `/souls` command. There will be a soul with your items and XP. Stand near soul a few
  sec. Whoosh! Your items now collected
- If you don't have enough space, the soul will make a sound and purple smoke will appear indicating lack of space.
- The soul will become public after a configured time.

Features:

- [x] Fully async
- [x] Highly customizable particle effects
- [x] Highly customizable sound effects
- [x] Runtime reloadable database
- [x] Control retained XP amount

| Command          | Description                  | Permission          |
|:-----------------|:-----------------------------|:--------------------|
| ️`/skreload`     | Reload plugin                | soulkeeper.reload   |
| ️`/souls <page>` | List souls                   | `-`                 |
| ️`-`             | View all souls in `/souls`   | soulkeeper.all      |
| ️`-`             | Free any soul in `/souls`    | soulkeeper.free.all |
| ️`-`             | Teleport to soul in `/souls` | soulkeeper.teleport |

## Support

If anything go wrong, please:

1. Define a clear steps to reproduce
2. Try to reproduce again in clear environment
3. Obtain latest console logs
4. [Join Discord](https://discord.com/invite/8fEzV2TDS2) and create a ticket in `support`

## Feature request

Almost the same as support.

- Join discord
- Create a ticket in `suggestions`
- We will have a little talk and after that I will create issue on github

## Incompatible plugin

- Any plugin which is editing `PlayerDeathEvent`

# Preview

## Killing

https://github.com/user-attachments/assets/ec5f1437-0dab-404f-a4a8-725ee1cd9129

## Collecting

https://github.com/user-attachments/assets/75f58f92-7d13-4c23-a7fb-b2d89476748b

More plugins from AstraInteractive [AstraInteractive](https://github.com/Astra-Interactive)

<img src="https://bstats.org/signatures/bukkit/SoulKeeper.svg"/>
