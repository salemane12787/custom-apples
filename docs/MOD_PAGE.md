# Mod page copy — Modrinth & CurseForge

Use the sections below when publishing. Replace `[YOUR_NAME]` and add screenshot URLs after upload.

---

## Title (display name)

**Custom Apples**

Alternative (more searchable): **Custom Apples — Apple Progression Mod**

---

## Short summary (CurseForge summary / Modrinth subtitle)

**~200 characters**

> A Forge progression mod where every tool, food, and block is an apple. Unlock recipes in the recipe book, grow apple trees from juice, shrink mobs with the Apple Sword, and find the real APPLE in the Big Tree.

---

## Tags / categories

**Modrinth tags:** `adventure`, `food`, `game-mechanics`, `technology`, `worldgen`, `equipment`

**CurseForge categories:** Adventure and RPG, Food, Mobs, Armor & Weapons, World Gen

**Loaders:** Forge  
**Minecraft:** 1.20.1  
**Forge:** 47.4.0+

---

## Full description (Markdown — works on Modrinth; CurseForge supports similar formatting)

### 🍎 Custom Apples

**Custom Apples** turns Minecraft into an apple-only progression adventure. Start by eating a letter **A**, craft stranger apples, unlock new recipes in the vanilla recipe book, and climb from wooden splinters to dragon-tier chaos.

Inspired by “everything is apples” progression gameplay — one weird item leads to the next until the world is nothing but fruit.

---

### ✨ Highlights

- **40+ custom items** — food, tools, armor, blocks, and throwable apples
- **Recipe book progression** — recipes unlock as you craft; no custom progression GUI
- **Apple juice fluid** — orange water-like juice from leaves; spreads and grows apple trees
- **Custom worm pet** — from the Dirt Apple, with naming GUI and crawl animation
- **Apple villagers** — ring the Apple Bell to convert trades to apples
- **3D photoreal APPLE** — reward in the Big Tree chest (handle with care)
- **Endgame Dragon Apple** — spawns a giant tree and exterior reward chest

---

### 📜 Progression (early → late)

| Stage | Items | What they do |
|-------|--------|----------------|
| Start | **A**, Apple Axe, Wooden Apple, Splinter | Fast-eat A; axe tree-capitates logs into wooden apples; splinter weapon |
| Early | Appl, Dirt Apple, Bread Apple, App | Slow eat; poison + worm; omnivore effect; App Store trades |
| Mid | Apple Bucket, Apple Sword, Iron Apple, Bell | Juice from leaves; shrink mobs + loot per hit; helmet strength; apple villagers |
| Mid+ | Apple Apple Apple, Emerald Apple, Apple Block, Redstone Apple | Throwable explosions + trees; villager burst; item transmutation; bedrock hole |
| Tools | Flint & Apple, Apple Bow, Diamond Apple | Red apple fire; sticking arrows + loot; inventory → diamonds |
| Golden | Golden Pickaxe, Golden Block, Super Pickaxe | Tree-shaped mining; golden transmutation; 3×3 netherite pickaxe |
| Late | Lapis Apple, End Apple, Diamond Chestplate | Enchant golden apples in inventory; End step; mega hearts + wide player |
| Finale | **Dragon Apple** → Big Tree + **APPLE** | Giant tree structure; chest on the outside holds the real apple |

---

### 🔧 Notable mechanics

**Apple Sword** — Shrinks enemies on hit (client-synced) and drops loot without killing them.

**Bread Apple** — Temporary **Omnivore**: eat vanilla non-food items with chew sounds, particles, and animation.

**Apple Juice** — Bucket milk from leaves; pour orange fluid that spreads like water; apple trees sprout where juice spreads (not at the pour site).

**Emerald Apple** — Villagers explode into baby-sized apple traders (emeralds for apples).

**Diamond Apple Chestplate** — Massive max health and **Wide** effect (you become very wide).

**Super Apple Pickaxe** — 3×3 mining pattern + sprouts apple trees.

**APPLE (Real Apple)** — Photoscanned 3D item. Eating it disconnects you from the world with a message: *That APPLE was for real life — not for this game.*

---

### 📦 Requirements

- **Minecraft** 1.20.1
- **Forge** 47.4.0 or newer (tested on 47.4.16)
- **Java** 17+

No other mods required. Works in singleplayer and multiplayer.

---

### 🖼️ Screenshots (suggested)

1. Early progression — Letter A and Apple Axe tree cap
2. Apple juice spreading with orange tint + trees growing
3. Apple Bell + apple-textured villagers
4. Big Tree and exterior chest after Dragon Apple
5. 3D APPLE item in hand / in chest
6. Named worm crawling on grass

---

## CurseForge-only fields

**Project name:** Custom Apples  
**Summary:** (use Short summary above)  
**License:** MIT  
**Issue tracker URL:** (optional)  
**Source code URL:** (optional)  
**Discord:** (optional)

---

## Modrinth-only fields

**Project ID suggestion:** `custom-apples`  
**Summary:** (use Short summary above)  
**Description:** (use Full description above)  
**License:** MIT  
**Client / server:** Both required (or Client & server optional on both sides)

---

## SEO / search keywords (internal notes)

custom apples, apple mod, progression mod, forge 1.20.1, apple sword, dragon apple, omnivore, apple juice, xnestorio style apples

---

## Credits

- Mod by **[YOUR_NAME]**
- Worm model & animation: Blockbench
- Real Apple: photoscan 3D mesh
- Armor textures: custom Blockbench UV layout

---

## Changelog v1.0.1

**Bug fix & polish update** — progression order, recipes, performance, Apple Bell rendering, and tool behavior.

### Apple Bell
- Fixed placed Apple Bell showing only the wooden frame with **no swinging bell body**
- Added a dedicated block entity type and proper client/server tickers so the bell animates and renders correctly
- Apple bell renderer no longer replaces vanilla bells

### Progression
- Reworked unlock order so ore apples (**Emerald → Redstone → Diamond → Lapis**) come **before** the golden tier
- Lapis Apple is now the capstone of the ore-apple line (not mixed into golden/endgame)
- Tier bar and recipe unlocks match the new order

### Tools & combat
- **Apple Axe** no longer breaks leaves; tree cap only harvests logs
- **Lapis Apple** max-enchant no longer applies curse enchantments (Binding / Vanishing)

### Food
- **Letter A** and standard foods use fast-eat (`alwaysEat`) where intended
- Eatable apples can be consumed **even when hunger is full**

### Recipes
- Fixed illogical shapes and ingredient counts across many recipes
- Ore apples now use proper **ore tags** (`emerald_ores`, `redstone_ores`, `diamond_ores`, `lapis_ores`)
- Iron Apple, Apple Bell, Golden Pickaxe, armor, Bow, Fishing Rod, and related recipes corrected

### Performance
- Apple Block transmutation and Apple Bow arrow effects now scan **near players** instead of the entire world

### UI
- Item hint tooltips read from lang keys (`item.customapples.<id>.tooltip`)

### Apple Bucket & juice
- Bucket on leaves removes **only one leaf block** (not the whole connected canopy)
- Trees grow **above** juice blocks, not in the same block
- Each spread block grows a tree **3 seconds after that block gets juice** (staggered from center outward)

### Super Apple Pickaxe
- **3×3×3** mining cube
- No apple trees when mining
- **8192** durability

### Golden & super tool durability
- Golden Apple Sword & Pickaxe: **1024** (pick was 32 before)
- Super Apple Sword & Pickaxe: **8192**

### Note for existing worlds
- **Break and re-place** any Apple Bells placed before this update so they use the new block entity
- Progression index may differ after the reorder — a **new world** gives the cleanest experience

---

## Changelog v1.0.0

- Initial release
- Full apple progression chain from A to Dragon Apple
- Apple juice fluid, worm entity, apple villagers, throwable apples
- Real APPLE kick mechanic and Big Tree structure
