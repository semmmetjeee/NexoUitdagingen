# NexoUitdagingen

Configurable Paper quest plugin with three categories:

- 3 random personal daily challenges per player
- 3 random personal weekly challenges per player
- 3 shared global server challenges

Everything important is YAML configurable: command + aliases, messages, quest pools, rewards, GUI layouts, lore, colors, slots and reset settings.

## Requirements

- Paper 1.21.x
- Java 21
- PlaceholderAPI optional

## Quest pools

- `quests/daily.yml`
- `quests/weekly.yml`
- `quests/global.yml`

Supported types include block breaking/placing, mobs and players killed, fishing, crafting, smelting/cooking, breeding, taming, shearing, milking, enchanting, anvils, consuming, pickup/drop, trading, damage dealt/taken, healing, walking/sprinting/swimming/flying, boat/minecart/horse travel, XP/levels, deaths, jumping, sleeping, joins, advancements, commands and PlaceholderAPI condition quests.

Targets can be `ANY`, one Bukkit enum name, or multiple alternatives separated by `|`.

## GUIs

- `guis/main.yml`
- `guis/daily.yml`
- `guis/weekly.yml`
- `guis/global.yml`

GUI supports border, panes, decorations, custom items/actions and configurable quest slots.

## Commands

Command and aliases come from `config.yml`.

- `/uitdagingen`
- `/uitdagingen daily|weekly|global`
- `/uitdagingen reload`
- `/uitdagingen reroll <player> <daily|weekly>`
- `/uitdagingen reset <player>`
- `/uitdagingen rerollglobal`
- `/uitdagingen addprogress <player> <type> <target> <amount>`

Admin permission: `nexouitdagingen.admin`

## Release

Push `v1.0.0` and GitHub Actions builds the jar and adds it to a GitHub Release.
