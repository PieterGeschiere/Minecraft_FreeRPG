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
> config files are not updated automatically, so new settings fall back to built-in defaults. This
> includes the Building and Aeronautics skills: a config without their sections still works, with
> the default numbers from this README. `perkConfig.yml` is now copied to your plugin folder the
> first time the plugin starts (an existing file is never overwritten), so you can edit the perk
> numbers there.
>
> **Saved toggles may swap once.** An older version saved each skill's exp-bar toggle and ability
> toggle into each other's slot. They are saved correctly now, so a player whose two toggles
> differed may find them swapped one time after updating. They stay correct afterwards.

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
* **Daily, weekly and seasonal Quests** with a reward wheel (see below).
* **Building**, a new skill that levels by placing blocks and protects your own builds (see below).
* **Aeronautics**, a new skill that levels by gliding with an elytra (see below).
* **Elite mobs and the Blood Moon**: hostile mobs now and then spawn as elites with random affixes,
  and some nights turn into a Blood Moon (see below).
* **Parties** with bonus XP sharing, an XP buff for nearby members, shared party quests, a party
  leaderboard, teleporting and more (see below).
* **Dutch** added as a language, and every language now has a text for everything (see Language Support).
* The `/frpg` command code was split into one small class per command, with a single skill
  registry, so adding a skill no longer means editing a dozen hard-coded lists.
* Many bug fixes, among them wrong playtime display, missing exp for deepslate and copper ores,
  raw metals smelting into themselves, Luck and Turtle Master potions without an effect and an
  error when killing glow squids. Also fixed: a player's exp-bar and ability toggles swapping places
  every time their stats were saved, a player standing exactly on a level's exp threshold being
  counted one level too low, `/frpg top playTime` and `/frpg setTokens` failing from the console,
  and `/frpg skillTreeGUI` ignoring skill names typed with capitals (`Smelting`, `Global`).

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
It has the same kind of skill tree. Bow Mastery and Crossbow Mastery replaced the old Archery skill, which has been removed.

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



**Effects**: every weapon ability has sounds and visuals, all switchable in the `effects:` section of
`weaponSkills.yml`: a burst and sound when an ability is readied, an aura around you and a boss bar
with the time and shots left while it is ready, a puff when it ends, a different sound for every
perk that works, a fanfare for each weapon skill when you level up, particle trails on the arrows
of Arrow Storm and the bolts of Blast Bolt (a layer for each perk you own, like flames for Flaming
Volley or a glow for Marked Prey), and rings and floating damage numbers where Blast Bolt and
Piercing Rush hit.

**Effects for the main skills and perks**: the abilities of the main skills (Big Dig, Timber, Berserk
Pick, Natural Regeneration, Super Bait, Spur Kick, Swift Strikes, Stone Soldier and Great
Axe) have a burst, an aura while they last and a puff when they end, each fitting the skill. The perks
you trigger yourself have effects too: Grappling Hook (a rope to the hook), Super Bait's extra catch,
Rob, Vein Miner, Leaf Blower, Growth Hormones, Roll and Dodge. All of these are played in the world, so
other players nearby see and hear them (only the "ability ready again" chime is private). They can be
switched off in the `effects:` section of `weaponSkills.yml` with `skillAbilitySounds`,
`skillAbilityParticles`, `perkSounds` and `perkParticles`.


## Parties

Play together in a party. `/party` opens the party menu (also reachable with the banner icon in the
`/frpg` main menu) with the party name, its members, and every setting. Parties are saved in
`plugins/FreeRPG/parties/` and settings live in `party.yml` (reload with `/frpg party reload`).

* **Roles:** *Owner*, *Officer* and *Member*. A party owner who is offline keeps the party. Ownership
  only moves when the owner leaves (to the longest-standing officer, then member), uses
  `/party transfer`, or has been offline for 30 days (it goes to an active officer or member and the
  former owner becomes an officer). A party whose members have all been offline for 60 days is deleted.
* **Open or invite only:** owners and officers can open a party so anyone can join with
  `/party join <party name>` or `/party join <owner name>`. Otherwise players need an invite
  (`/party invite <player>`, with clickable **[Accept] [Deny]** buttons in chat).
* **Bonus XP:** you keep all your XP. Party members within 30 blocks (same world, online, not AFK)
  also get 25% of it in the same skill, for the main skills and the weapon skills. Owners and
  officers can switch it on or off for the party, and every member can switch off receiving it.
* **XP buff:** for every party member within 40 blocks of you (same world, online, not AFK) you earn 3%
  more experience, up to 15% extra in total. This is on top of the bonus XP sharing above and can be
  changed or switched off under `buffs` in `party.yml`.
* **Party quests:** every party gets its own daily quests, and the progress of all members adds up.
  `/party quests` shows them. When the party finishes all of them, every member who is online gets a
  prize rolled like a reward wheel prize. Settings are under `party:` in `quests.yml`.
* **Party leaderboard:** `/party top` lists the best 10 parties by the combined total level of their
  members and tells you where your own party stands.
* **Friendly fire:** off by default, owners and officers can turn it on in the menu.
* **Teleporting:** `/party tpa <member>` asks to teleport to a party member (they answer with the
  chat buttons or `/party tpaccept` / `/party tpdeny`). Owners and officers can set a party spawn with
  `/party setspawn` and everyone in the party can use `/party spawn`. You have to stand still for 5
  seconds (turning your head is fine), with a particle effect and sound everyone nearby sees and
  hears. Not possible in combat or while riding, and blocked worlds and WorldGuard regions that deny
  entry are respected. `/party tptoggle` turns teleport requests off.
* **Tab list and name:** party members get a blue **P** before their name.

| Command | What it does |
|---|---|
| `/party` | Opens the party menu |
| `/party create <name>` | Creates a party (3-16 letters, numbers or `_`) |
| `/party invite <player>` | Invites a player (owner, officer) |
| `/party accept [party]` / `deny [party]` | Answers an invite |
| `/party join <party or owner>` | Joins an open party |
| `/party leave` / `list` | Leaves your party / shows it |
| `/party kick <player>` | Kicks a member (officers cannot kick officers) |
| `/party promote` / `demote <player>` | Makes a member an officer, or back (owner) |
| `/party transfer <player>` / `disband` | Gives the party away / ends it (owner) |
| `/party open` / `closed` | Anyone can join / invite only (owner, officer) |
| `/party xpshare [on/off]` | Bonus XP sharing (owner, officer) |
| `/party friendlyfire [on/off]` | Friendly fire (owner, officer) |
| `/party setspawn` / `spawn` | Sets / teleports to the party spawn |
| `/party tpa <member>`, `tpaccept`, `tpdeny`, `tptoggle` | Teleport requests |
| `/party receivexp [on/off]` | Receive shared XP or not |
| `/party top` | The best parties by combined total level |
| `/party quests` | The shared daily quests of your party |
| `/frpg party disband <party>`, `kick <player>`, `reload`, `info <party>` | Administration |

Permissions: `freerpg.party.use` and `freerpg.party.create` (everyone by default) and
`freerpg.party.admin` (ops). With PlaceholderAPI you also get `%freerpg_party_name%`, `_role`,
`_owner`, `_size`, `_maxSize`, `_online`, `_open` and `_friendlyFire`. The texts are translated into all
the languages (see Language Support).

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

## Daily Quests

Every day each player gets 3 random quests, for example "mine 40 ores", "kill 25 hostile mobs" or "earn
800 Mining exp". Open them with `/quests` (or the clock icon in the `/frpg` main menu). Progress counts
while you play in survival or adventure mode; blocks you placed yourself do not count.

When all 3 quests are done you get **one spin of the reward wheel**. Click the glowing wheel button in the
quest menu: a ring of prizes spins, slows down and stops on your prize, which you receive at once. The
prize is completely random:

* an **enchanted book** with any enchantment (Mending and the other treasure enchantments included) at
  a random level up to its maximum, or
* **Minecraft exp**, or
* **FRPG skill exp** in a random skill.

A spin you do not use stays until the next daily reset. New quests are dealt at midnight server time.
Everything is set in `quests.yml` (reload with `/frpg quests reload`): the quest pool and amounts, how many
quests per day, the reset time and time zone, how often each prize type shows up, the exp ranges, the
worlds where quests are off and the sound and particle effects. Admins can give a player new quests with
`/frpg quests reset <player>`.

### Weekly and seasonal quests

Besides the daily quests there are **weekly quests** (a new set every Monday, at the reset hour) and
**seasonal quests** (a new set every calendar quarter: January, April, July and October). They count
the same things at the same time as the daily ones, but ask for more (5 times as much for weekly, 20 times
for seasonal, by default) and give one wheel spin with **bigger prizes** (3 times and 8 times the exp) when
all of them are done. Open them with the clock, compass and sapling tabs in the `/quests` menu, or with
`/quests weekly` and `/quests seasonal`. Set the number of quests, the scale and the prize multiplier
under `weekly:` and `seasonal:` in `quests.yml`, or give them a quest pool of their own with
`weekly.pool` / `seasonal.pool` (same format as `quests`). Admins can reset them with
`/frpg quests reset <player> [daily|weekly|seasonal|all]`.

Party quests are described under Parties.

Permissions: `freerpg.quests.use` (everyone by default) and `freerpg.quests.admin` (ops). The quest texts
are translated into all the languages (see Language Support).

## Building

Building is a passive skill, like Smelting or Enchanting. It has no ability to activate. Open its skill tree with
`/building` or the bricks icon in the `/frpg` menu. You earn Building exp by **placing blocks**: a flat amount per
block (more for decorative blocks such as stairs, slabs, walls and glass), never based on how valuable the block is.
You get no exp for placing a block where you just broke one, and each material only counts a limited number
of times per minute, so place-and-break loops do not pay.

| Skill tree entry | Levels | What it does |
|------------------|--------|--------------|
| **Builder's Reach** (passive) | rises with your level | Your block reach grows by 0.01 blocks per level, up to +3. |
| **Weatherproof** | 5 | Blocks *you placed* survive burning (fire, lava, lightning) with 20% / 40% / 60% / 80% / 100% chance. |
| **Fortified Blocks** | 5 | Blocks you placed survive creeper and TNT explosions with 10% / 20% / 30% / 40% / 50% chance. Needs Weatherproof level 2. |
| **Demolition Expert** | 1 | Blocks you placed break 25% faster and your tools lose 50% less durability breaking them. Needs 10 points in the two perks above. |

Building never gives you items, drops or free blocks. The game remembers who placed which block in
`plugins/FreeRPG/ServerData/buildingBlocks.dat` (up to 2,000,000 blocks; after that new blocks are no longer
protected). Blocks moved by pistons lose their protection. Numbers are in `perkConfig.yml` (perks and reach)
and `advancedConfig.yml` under `building:` (exp, limits). The skill can be turned off with `building.skillAllowed`
in `config.yml`. Permission: `freeRPG.buildingEXP`.

## Aeronautics

Aeronautics is a passive skill for elytra flying, opened with `/aeronautics` or the elytra icon in the `/frpg`
menu. You earn exp for the **distance you actually glide** (3 exp per 50 blocks by default, up to 30 steps per
minute so flying in circles cannot be farmed). Firework rockets themselves give no exp.

| Skill tree entry | Levels | What it does |
|------------------|--------|--------------|
| **Glide Efficiency** (passive) | rises with your level | Your elytra lose 0.5% less durability per level, up to 50% less. |
| **Soft Landing** | 5 | You take 10% less damage per level (up to 50%) when you fly into a wall. This is separate from Agility's Roll. |
| **Long Burn** | 5 | Firework rockets you boost with last longer: +1, +1, +2, +2, +3 flight power. The rocket is still used up. |
| **Tailwind** | 1 | The first rocket after you take off is stronger (+1 flight power). Needs 10 points in the two perks above. |

There is no perk that saves rockets, on purpose: that would be free items. A rocket boost is recognised
as a rocket launched straight ahead by a gliding player, so rockets shot from a crossbow do not count.
Numbers are in `perkConfig.yml` and `advancedConfig.yml` under `aeronautics:`. Permission: `freeRPG.aeronauticsEXP`.

## Elite Mobs and the Blood Moon

**Elites.** Every naturally spawned hostile mob has a small chance (3% by default) to be an **elite**. An
elite has a red name, a glow, a boss bar for players within 30 blocks, 3 times the health and 1.5 times
the damage, a quarter bigger body (`sizeScale`), and 1 to 3 random **affixes**. The chance and the strength are the same for every player: there
is no scaling with level or distance. Elites despawn like any other mob. Mobs from spawners, eggs, breeding,
commands and raids are never elites, and at most 20 elites live in one chunk.

| Affix | What it does |
|-------|--------------|
| **Volatile** | Explodes when it dies and hurts players nearby (no block damage) |
| **Vampiric** | Heals itself with part of the damage it deals |
| **Swift** | Moves faster and lunges at its target |
| **Armored** | Takes 30% less damage |
| **Splitting** | Splits into weaker copies when it dies |
| **Thorns** | Reflects part of the melee damage back at you |
| **Summoner** | Calls minions to its side while it fights (never more than 6 alive) |
| **Frostbound** | Slows whoever it hits |
| **Burning** | Sets whoever it hits on fire |
| **Teleporter** | Blinks behind its target |

Killing an elite gives extra FRPG exp in the skill of your weapon (Defense when you hold neither a sword nor
an axe) and extra drops. Nothing extra is given for mobs from spawners or mob farms (exp farm detection), and a
chunk pays these rewards at most once every 2 minutes (`rewards.chunkCooldownSeconds`). The copies and minions
of Splitting and Summoner drop nothing and give no vanilla exp.

**Blood Moon.** Every night of the configured worlds (the overworld by default) has a small chance (1 in 16 by
default) to become a Blood Moon, until the end of the night. Players get a big title and a sound, a red boss bar
with the time left, red particles and a **red screen edge** (the warning of a world border that only you can
see; it never blocks or hurts you). A Blood Moon is not a red sky: Minecraft has no server side way to tint the
sky. While it lasts, naturally spawned hostile mobs have 50% more health and 25% more damage, zombies and
skeletons wear random damaged gear that almost never drops, and the elite chance is 4 times as high.

Admins can use `/frpg bloodmoon start|stop|status [world]` and `/frpg elites reload` or
`/frpg elites spawn <mob> <affix[,affix]>` (permission `freerpg.elites.admin`, ops by default). Everything is
set in `elites.yml`.

## World Events

The Blood Moon is one of eleven **world events**. Only one event runs at a time on the whole server. Every
Minecraft day each event rolls once for its chance, inside its own time window, and after any event ends
nothing starts by itself for 2 Minecraft days (`minGapDays`). Every event shows a title, a sound and a boss
bar with the time left.

| Event | When | What it does |
|-------|------|--------------|
| **Blood Moon** | night, 6.25% per night | Tougher mobs, gear and more elites (see above) |
| **Bountiful Harvest** | daytime, 5% per day | 1.5x exp for farming, fishing and woodcutting, and ripe crops, logs and fish sometimes drop twice |
| **Gold Rush** | any time, 4% per day, 10 minutes | 1.5x mining exp and ore sometimes drops twice, but breaking natural ore is loud and can draw a few mobs |
| **Eclipse** | night, 5% per day | 60% of the natural hostile spawns do not happen (elites and spawner mobs are kept) |
| **Fishing Festival** | any time, 4% per day, 10 minutes | Everyone fishes for points. Needs 2 players online to start by itself |
| **Lumberjack's Day** | daytime, 5% per day | 1.5x exp for woodcutting and axe mastery, and logs sometimes drop twice |
| **Forge Fever** | any time, 4% per day, 10 minutes | 1.5x exp for smelting and repair, and flame pick smelts a double drop 25 points more often (only with the flame pick perk) |
| **Scholar's Moon** | night, 5% per day | 1.5x exp for enchanting and alchemy, and enchanting costs half the levels (given back right after the enchant) |
| **Adventurer's Wind** | daytime, 5% per day | 1.5x exp for agility and aeronautics, a Speed I effect, and a small push for gliding players |
| **Titan Night** | night, 5% per day | Natural hostile mobs spawn 1.4x to 2.2x bigger, with more health and damage the bigger they are. Killing one gives 1.5x kill exp (in the skill of your weapon) and its loot sometimes drops twice, but not in spawner or mob farms. A mob never grows into a ceiling or a tight gap |
| **Mutator Night** | night, 5% per day | Two random rule twists for the night (see below) |

Extra drops and mining noise only count for natural blocks and ripe crops, never for blocks players placed.

**Mutator Night twists.** Each Mutator Night picks 2 different twists from this pool. Every twist can be switched
off or given a weight in `worldevents.yml`.

| Twist | What it does |
|-------|--------------|
| **Low Gravity** | You jump higher, fall slower and have a larger safe fall distance |
| **Glass Cannon** | You deal double damage to mobs and they deal double damage to you (player against player is unchanged) |
| **Exploding Loot** | Mobs killed by a player sometimes drop extra loot and sometimes explode (no block damage, no fire) |
| **Hungry Night** | You get hungry faster (it stops at 6 food, so it never starves you), and food restores 1.5x as much |
| **Swift Mobs** | Natural hostile mobs move 30% faster |
| **Double Trouble** | Natural hostile spawns often bring a twin that drops nothing and gives no exp |
| **Soul Storm** | Souls from kills are doubled (you still need the Soul Harvest perk) |
| **Glowing Mobs** | Natural hostile mobs glow, so you can see them through walls |

**Fishing Festival prizes.** Everyone who scored gets souls (even without the Soul Harvest perk), Minecraft exp
and fishing exp. The top 3 get more and a free spin of the quest wheel, which you use with `/quests bonus`.
Players who caught nothing, or are offline at the end, get nothing. An admin stop pays nothing.

Admins can use `/frpg event start <type> [world]`, `/frpg event stop`, `/frpg event status` and
`/frpg event list` (permission `freerpg.events.admin`, ops by default; an admin start ignores the chance, the gap
and the 2 player rule). The types are `blood_moon`, `bountiful_harvest`, `gold_rush`, `eclipse`,
`fishing_festival`, `lumberjacks_day`, `forge_fever`, `scholars_moon`, `adventurers_wind`,
`titan_night` and `mutator_night`. Everything except the Blood Moon is set in `worldevents.yml`; `/frpg elites reload` reloads it.

## Configuration files

Everything lives in `plugins/FreeRPG/`:

| File | What it holds |
|------|---------------|
| `config.yml` | Small, simple settings: default language, which skills are on, maximum levels, exp multipliers. |
| `advancedConfig.yml` | The detailed settings: ability durations and cooldowns, exp values for placing, gliding and so on. |
| `experienceConfig.yml` | The experience curve and per-skill exp settings. |
| `perkConfig.yml` | Skill tree and passive numbers (for example the Building and Aeronautics perks). Created on first start, never overwritten. |
| `languages.yml` | All texts, one block per language. |
| `weaponSkills.yml`, `party.yml`, `quests.yml` | Settings of the weapon skills, parties and daily quests. |
| `elites.yml` | Elite chance and strength, the affix numbers, rewards and the Blood Moon. |
| `worldevents.yml` | The other world events and the gap between events. |

`config.yml` and `advancedConfig.yml` repair themselves when a setting is missing (this removes their comments);
`general.autoUpdateConfig: false` in `config.yml` turns that off. The bundled `config2.yml` is an unused draft of a
planned split of the config files and is not read by the plugin.

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
- 🇳🇱 Dutch (Translated by: Claude, not yet checked by a native speaker)

Every language has a text for everything (a test checks this), including Parties, Quests, the Weapon Skills,
Building and Aeronautics. The newest texts (Parties, Quests, Building, Aeronautics, and the whole Dutch
translation) were written without a native speaker, so corrections are very welcome.

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








