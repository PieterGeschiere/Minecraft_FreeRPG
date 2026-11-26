# Changelog

## 2.0.0

FreeRPG Reborn is a fork of [FreeRPG 1.16 by AdvancedWipe](https://github.com/AdvancedWipe/Minecraft_FreeRPG_1.16),
which is itself a fork of the original FreeRPG by Jack Carlton. This is the first release of the Reborn fork.

**This is not the original FreeRPG.** It is a separate fork for Minecraft 26.3 only. For Minecraft 1.15 to 1.21
use the original [FreeRPG](https://modrinth.com/plugin/freerpg). Inside the server the plugin is still named
`FreeRPG`, so do not run both at once.

> **Upgrading from an older FreeRPG version?** Back up your `plugins/FreeRPG` folder first. Old config
> files are not updated automatically, so new settings fall back to built-in defaults.
> `perkConfig.yml` is copied to your plugin folder the first time the plugin starts (an existing file is
> never overwritten). A player's exp-bar and ability toggles may swap once after updating, because older
> versions saved them in each other's slot. They stay correct afterwards.

### Requirements
- Minecraft Java Edition 26.3 (Paper or Spigot)
- Java 25 or newer
- Optional: WorldGuard, PlaceholderAPI

### New features
- **Weapon Skills**: new Mace, Trident, Spear, Bow and Crossbow Mastery skills, each with its own skill
  tree and ability. Open them with `/frpg weapons` or a shortcut command such as `/mace`. Configured in
  `weaponSkills.yml`.
- **Building**: a new skill that levels by placing blocks and protects your own builds.
- **Aeronautics**: a new skill that levels by gliding with an elytra.
- **Daily, weekly and seasonal quests** with a reward wheel (`/quests`).
- **Parties** (`/party`): bonus XP sharing, an XP buff for nearby members, shared party quests, a party
  leaderboard and teleporting.
- **Elite mobs and the Blood Moon**: hostile mobs sometimes spawn as elites with random affixes, and some
  nights turn into a Blood Moon.
- **World events**: eleven server-wide events, one at a time, each with a title, sound and boss bar.
  Blood Moon, Bountiful Harvest, Gold Rush, Eclipse, Fishing Festival, Lumberjack's Day, Forge Fever,
  Scholar's Moon, Adventurer's Wind, Titan Night and Mutator Night (two random rule twists per night).
  Admins can control them with `/frpg event`, and everything is set in `worldevents.yml`.
- **Repair and Salvage Stations**.
- **Feedback effects**: more sounds, particles and messages across skills, quests and events.
- **Skill shortcut commands**: `/mining`, `/farming`, `/mace` and so on open the matching skill tree.
- **Dutch** added as a language, and every language now has a text for everything.

### Minecraft 26.3 support
- Ported to Minecraft 26.3 (new enchantment, potion, attribute and particle names) and Java 25.
- Exp for blocks added since 1.18: ore and storage blocks (including raw, gold and copper blocks), amethyst,
  deepslate, tuff, copper, mud, resin, mangrove, cherry, pale oak and bamboo wood, pitcher and torchflower
  crops, honey, pale moss and more.
- Kill exp for newer mobs such as the Warden, Breeze, Bogged, Creaking, Camel, Nautilus, Sniffer, Armadillo,
  Frog, Allay and Copper Golem, plus Camel taming, riding and breeding exp.
- Copper tools and armor are recognised by the combat and repair skills. The Mace and spears are recognised
  by the repair skill and the Weapon Skills.

### Fixes
- Wrong playtime display.
- Missing exp for deepslate and copper ores.
- Raw metals smelting into themselves.
- Luck and Turtle Master potions without an effect.
- An error when killing glow squids.
- Exp-bar and ability toggles swapping places every time stats were saved.
- A player standing exactly on a level's exp threshold being counted one level too low.
- `/frpg top playTime` and `/frpg setTokens` failing from the console.
- `/frpg skillTreeGUI` ignoring skill names typed with capitals (`Smelting`, `Global`).

### Internal
- The `/frpg` command code was split into one small class per command, with a single skill registry, so
  adding a skill no longer means editing a dozen hard-coded lists.
