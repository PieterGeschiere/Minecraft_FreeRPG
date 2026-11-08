# FreeRPG Reborn

![GitHub license](https://img.shields.io/github/license/PieterGeschiere/Minecraft_FreeRPG.svg)
![GitHub issues](https://img.shields.io/github/issues/PieterGeschiere/Minecraft_FreeRPG.svg)
![GitHub release](https://img.shields.io/github/v/release/PieterGeschiere/Minecraft_FreeRPG.svg)

FreeRPG Reborn is an RPG skill system plugin for **Minecraft 26.3** (Paper and Spigot). Inspired by
mcMMO, it aims to be a free alternative. Each skill has a skill tree, with an additional global
skill tree, all accessible through a simple, Hypixel inspired GUI.

This is a fork of [FreeRPG 1.16 by AdvancedWipe](https://github.com/AdvancedWipe/Minecraft_FreeRPG_1.16),
which is itself a fork of the original FreeRPG by Jack Carlton. Both are credited in the licence and
in `plugin.yml`. This project is not affiliated with or endorsed by them.

## Requirements

* Minecraft Java Edition **26.3** (Paper or Spigot)
* **Java 25** or newer
* Optional: WorldGuard and PlaceholderAPI

## Download

Download the latest jar from the [GitHub Releases](https://github.com/PieterGeschiere/Minecraft_FreeRPG/releases) page and place it in your
server's `plugins` folder.

> **Upgrading from an older FreeRPG version?** Back up your `plugins/FreeRPG` folder first. Old
> config files are not updated automatically, so new settings fall back to built-in defaults.

## What's new in 2.0.0

* Ported to Minecraft 26.3 (new enchantment, potion, attribute and particle names, Java 25).
* Exp for blocks added since 1.18: all ore and storage blocks (including raw blocks, gold and
  copper blocks), amethyst, deepslate, tuff, copper, mud, resin, mangrove, cherry, pale oak and
  bamboo wood, pitcher and torchflower crops, honey, pale moss and more.
* Kill exp for newer mobs such as the Warden, Breeze, Bogged, Creaking, Camel, Nautilus, Sniffer,
  Armadillo, Frog, Allay and Copper Golem. Camel taming, riding and breeding exp.
* Copper tools and armor are recognised by the combat and repair skills, the Mace and spears by the repair skill and the Weapon Skills.
* **Weapon Skills**: new Mace, Trident, Spear, Bow and Crossbow Mastery skills with their own skill trees and
  abilities (see below), configured in `weaponSkills.yml` and opened with `/frpg weapons`. The Fighter
  and Training global perks apply to them too.
* **Repair and Salvage Stations** (see below).
* Many bug fixes, among them wrong playtime display, missing exp for deepslate and copper ores,
  raw metals smelting into themselves, Luck and Turtle Master potions without an effect and an
  error when killing glow squids.

## Weapon Skills

All weapon skills live in one menu, opened with `/frpg weapons` (or the mace icon in the main menu): Swordsmanship
and Axe Mastery (which open their normal skill trees) and the newer weapon skills below. The newer weapon skills have their own level, experience and perks, saved in `plugins/FreeRPG/weaponSkills/`.
All values can be changed in `weaponSkills.yml`.

**Mace Mastery**: earn exp by hitting, smashing (hitting while falling) and killing with a mace. It has its
own skill tree, opened by clicking it in the weapons menu. Passive tokens (1 per level) are invested in
passives, skill tokens (1 per 100 levels) unlock skills, like in the main skills.

| Type | Name | Effect |
|---|---|---|
| Passive | **Slam Duration** | Seismic Slam stays ready longer (up to 12 s) |
| Passive | **Smash Power** | Smash attacks deal bonus damage |
| Passive | **Soft Landing** | Less fall damage while holding a mace |
| Ability | **Seismic Slam** | Right-click a mace to ready it. For a few seconds (up to 3 smashes) your smash attacks send a shockwave through nearby hostile mobs |
| Skill 1a | **Wide Shockwave** | Bigger shockwave radius |
| Skill 1b | **Stagger** | Smash attacks slow the target |
| Skill 2a | **Aftershock** | Stronger shockwave damage |
| Skill 2b | **Rattle** | Smash attacks weaken the target |
| Skill 3a | **Earthshaker** | The shockwave launches mobs into the air and deals more damage |
| Skill 3b | **Adrenaline** | Smash attacks grant you Speed and Resistance for a few seconds |
| Mastery | **Titan's Wrath** | More smash damage and a wider shockwave |

**Trident Mastery**: earn exp by stabbing, throwing (more for long throws) and killing with a trident. It has the
same kind of skill tree as the Mace.

| Type | Name | Effect |
|---|---|---|
| Passive | **Maelstrom Duration** | Maelstrom stays ready longer (up to 14 s) |
| Passive | **Throwing Power** | Thrown tridents deal bonus damage |
| Passive | **Tidal Might** | Tridents deal bonus damage in water or rain |
| Ability | **Maelstrom** | Left-click a trident to ready it. Your next 2 thrown tridents create a whirlpool that pulls nearby hostile mobs towards it |
| Skill 1a | **Wide Maelstrom** | Bigger whirlpool |
| Skill 1b | **Undertow** | The whirlpool pulls harder |
| Skill 2a | **Maelstrom Bite** | The whirlpool damages the mobs it pulls |
| Skill 2b | **Barbed Tips** | Thrown tridents slow the target |
| Skill 3a | **Tidal Burst** | The whirlpool ends with a burst that damages and launches mobs |
| Skill 3b | **Sea Shield** | Thrown tridents that hit grant you Absorption |
| Mastery | **Poseidon's Wrath** | More thrown damage and a bigger whirlpool |

**Spear Mastery**: earn exp by hitting and killing with a spear, with bonus exp for charging in at full sprint or on a
mount. It has the same kind of skill tree.

| Type | Name | Effect |
|---|---|---|
| Passive | **Rush Duration** | Piercing Rush stays ready longer (up to 14 s) |
| Passive | **Momentum** | Spear hits deal bonus damage while sprinting |
| Passive | **Mounted Charge** | Spear hits deal bonus damage while riding a mount |
| Ability | **Piercing Rush** | Sneak and left-click a spear to ready it. Your next 3 hits also pierce every hostile mob in a line behind the target |
| Skill 1a | **Long Reach** | The pierce reaches further |
| Skill 1b | **Wide Pierce** | The pierce is wider |
| Skill 2a | **Skewer** | The pierce deals more damage |
| Skill 2b | **Hamstring** | Spear hits slow the target |
| Skill 3a | **Pin** | The last mob pierced is pinned in place |
| Skill 3b | **Triumph** | Killing a mob grants you Speed II |
| Mastery | **Dragoon's Wrath** | More spear damage and a longer pierce |

**Bow Mastery**: earn exp by hitting and killing with a bow, with bonus exp for long shots (up to 50 blocks).
It has the same kind of skill tree. Bow Mastery and Crossbow Mastery replace the old Archery skill, which is retired (it stays in
the player files but is always off and no longer shown).

| Tree | Name | Effect |
|---|---|---|
| Passive | **Storm Duration** | Arrow Storm stays ready longer |
| Passive | **Draw Power** | Arrows deal bonus damage |
| Passive | **Sniper** | Arrows deal more damage the further away the target is (full bonus at 30 blocks) |
| Ability | **Arrow Storm** | Left-click a bow to ready it. Your next 3 shots also fire 2 extra arrows in a fan |
| Skill 1a | **Extra Arrows** | More extra arrows per shot |
| Skill 1b | **Pinning Shot** | Arrows slow the target |
| Skill 2a | **Volley Power** | The extra arrows deal more damage |
| Skill 2b | **Marked Prey** | Arrows make the target glow |
| Skill 3a | **Flaming Volley** | The extra arrows set targets on fire |
| Skill 3b | **Fleet Foot** | Arrow hits grant you Speed |
| Mastery | **Hawkeye's Wrath** | More arrow damage and more extra arrows |

**Crossbow Mastery**: earn exp by hitting and killing with a crossbow, with bonus exp for long shots. It has
the same kind of skill tree.

| Tree | Name | Effect |
|---|---|---|
| Passive | **Blast Duration** | Blast Bolt stays ready longer |
| Passive | **Bolt Power** | Bolts deal bonus damage |
| Passive | **Point Blank** | Bolts deal more damage to targets within 8 blocks |
| Ability | **Blast Bolt** | Left-click a crossbow to ready it. Your next 3 bolts explode where they land and hurt the hostile mobs around (no block damage) |
| Skill 1a | **Wide Blast** | Bigger explosion |
| Skill 1b | **Concussion** | Explosions slow the mobs they hit |
| Skill 2a | **Heavy Charge** | The explosion deals more damage |
| Skill 2b | **Rend** | Explosions weaken the mobs they hit |
| Skill 3a | **Shrapnel** | The explosion deals more damage and launches mobs into the air |
| Skill 3b | **Recoil Boost** | Shooting grants you Speed |
| Mastery | **Siege Master's Wrath** | More bolt damage and a bigger explosion |



## Repair and Salvage Stations

Repairing and salvaging no longer works on every iron or gold block, so you can build freely with
them. Craft a station instead:

| Station | Recipe | Use |
|---|---|---|
| **Repair Station** | 1 iron block in the middle, surrounded by 8 iron ingots | Sneak + right-click it while holding a damaged item |
| **Salvage Station** | 1 gold block in the middle, surrounded by 8 gold ingots | Sneak + right-click it while holding an item |

Both recipes show up in the recipe book. Stations drop themselves when broken, cannot be pushed by
pistons and survive explosions. The recipes and the on/off switch live in the `stations:` section of
`advancedConfig.yml`. Setting `enabled: false` restores the old behaviour (every iron block repairs,
every gold block salvages).

## Getting Started

![FreeRPG GUI](https://cdn-raw.modrinth.com/data/m3YFnJQM/images/c896cb6a3921b4687e9a45ba878ebdfff39bab38.png)

To get started playing, type ``/frpg``. This will bring up the GUI seen in the screenshot above.
Clicking on any of the icons will take you to that skill's skill tree. Hovering over any of the
icons or clicking on them will tell you more about the perk as shown in the screenshot below.

![](https://cdn-raw.modrinth.com/data/m3YFnJQM/images/bb28b3843f16076c7c07b5c4d30650a7adc314d1.png)

Earning experience is straightforward, mine blocks to get mining experience, cut trees to get woodcutting
experience, etc.

By the default configuration, each level you finish, will gain **1 passive token** which can be
invested in the passive skills represented by the dyes on the left. Every 100 levels, you will
gain **1 skill token** which can be invested in the skills represented by the terracotta skill tree.
Every 1000 levels, you will gain **1 global token** that can be used in the global skill tree.
Please check the wiki, for further information on how to configure FreeRPG to your needs.

FreeRPG provides 10 abilities to the player which can be leveled.

| Name                     | Related Skill | Description                                                                     | Activation                                                                 | Duration by default |
|--------------------------|---------------|---------------------------------------------------------------------------------|----------------------------------------------------------------------------|---------------------|
| **Big Dig**              | Digging       | Increases dig speed drastically.                                                | Right-clicking a shovel, then breaking a block.                            | 300 sec             |
| **Timber**               | Woodcutting   | Will cause all logs above to break.                                             | Right-clicking a pickaxe, then breaking a block.                           | 300 sec             |
| **Berserk Pick**         | Mining        | Will increase mining speed drastically.                                         | Right-clicking a pickaxe, then breaking a block.                           | 300 sec             |
| **Natural Regeneration** | Farming       | Automatically replants all crops at a random growth stage.                      | Right-clicking a hoe, then breaking a crop.                                | 300 sec             |
| **Super Bait**           | Fishing       | Fish are instantly pulled out of the water, without waiting to get a bite.      | Left-clicking a fishing rod, then fishing.                                 | 300 sec             |
| **Rapid Fire**           | Archery       | Arrow shots come out at maximum speed no matter how far the bow is pulled back. | Left-clicking a bow.                                                       | 300 sec             |
| **Spur Kick**            | Beast Mastery | Grants your horse a temporary speed boost.                                      | Right-clicking most items while on a horse, then left-clicking most items. | 300 sec             |
| **Swift Strikes**        | Swordsmanship | Your swings no longer have a cooldown.                                          | Right-clicking a sword then hitting an entity.                             | 300 sec             |
| **Stone Solid**          | Defense       | Grants resistance at the cost of being slowed down.                             | Right-clicking with an empty hand, then hitting an entity.                 | 300 sec             |
| **Great Axe**            | Axe Mastery   | Your attacks become an AOE strike that deals damage in a certain radius.        | Right-clicking an axe, then hitting an entity.                             | 300 sec             |

For further features and information on configuration, permissions and usage check out
the [original project wiki](https://github.com/AdvancedWipe/Minecraft_FreeRPG_1.16/wiki) (most features are unchanged).

## Language Support

FreeRPG fully/partially supports the following languages by default:

- 🇺🇸 English
- 🇭🇺 Hungarian (Translated by: vERKE)
- 🇫🇷 French (Translated by: Temuel)
- 🇩🇪 German (Translated by: KlenerTeufel96 of FruitLab.gg)
- 🇵🇱 Polish (Translated by: QuarVey)
- 🇪🇸 Spanish (Translated by: PibeChileno)
- 🇷🇺 Russian (Translated by: Cr1stalz_, MoKotik, and DevilPlay)
- 🇵🇹 Portuguese (Translated by: gbuueno)
- 🇨🇿 Czech (Translated by: Pieck444 and Fractvival)
- 🇰🇷 Korean (Translated by: Re_Oh)

The default server language can be set in config.yml. Additionally, each player can pick which
language they want the plugin to be represented in. Server administrators can add their own
languages to languages.yml. If you want to add your translation to be hardcoded into languages.yml,
please open a pull request with the added language. For further information check the [CONTRIBUTING](https://github.com/PieterGeschiere/Minecraft_FreeRPG/blob/master/CONTRIBUTING.md) file in this repository

## Wiki

The [original project wiki](https://github.com/AdvancedWipe/Minecraft_FreeRPG_1.16/wiki) provides you with detailed information about the abilities, skill trees and plugin configuration.

## Compatibility

FreeRPG should be compatible with most popular plugins. It has been tested with Essentials,
Bssentials, World Edit, Worldguard, Silk Spawners, and PlaceholderAPI and yielded no issues. It has
additionally, been tested with Multiverse and has no major issues. However, player profiles for
different worlds are not possible at the moment.

## Links

* [Original project wiki](https://github.com/AdvancedWipe/Minecraft_FreeRPG_1.16/wiki)
* [GitHub](https://github.com/PieterGeschiere/Minecraft_FreeRPG)
* [Original FreeRPG 1.16 project](https://github.com/AdvancedWipe/Minecraft_FreeRPG_1.16)

## Contribute

You want to contribute to this plugin by providing a translation, balancing changes or source code 
changes? Great, please check the [CONTRIBUTING](https://github.com/PieterGeschiere/Minecraft_FreeRPG/blob/master/CONTRIBUTING.md) 
file in this repository for further information.

### Reporting Issues

You can report bugs, crashes or any other issue you found by opening a new issue on
our [issue tracker](https://github.com/PieterGeschiere/Minecraft_FreeRPG/issues). Before opening a
new issue, use the search tool to make sure that your issue has not already been reported and ensure
that you have completely filled out the issue template.

Please provide the following details in your issue:

* The exact version of the plugin you are running and the server version you are using.
* If your issue is a crash, attach the latest log which include the crash you are referencing to.
* If your issue is a bug or otherwise unexpected behavior, explain what you expected to happen.
* If your issue only occurs with other plugins installed, be sure to specify the names and versions
  of those.

## Licence

FreeRPG is licensed under MIT license, a free and open-source license. For more information, please
see the [license](LICENSE) file.








