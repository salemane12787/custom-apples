# Changelog

## v1.0.1 — Bug fixes & polish

This update fixes issues found after the initial release and improves apple juice, tools, and progression.

### Apple Bell

- **Fixed** placed Apple Bell showing only the frame with no swinging bell body
- Dedicated block entity type, client/server tickers, and renderer for the apple bell model
- Vanilla bells are unaffected

### Apple Bucket & juice

- **Fixed** bucket on leaves removing a huge connected canopy — now removes **only the one leaf block** you click
- Trees grow **above** juice blocks (juice stays visible underneath)
- Each juice block grows a tree **3 seconds after that block received juice** (center pours first, then spread blocks staggered — not all at once)

### Super Apple Pickaxe

- Mines a full **3×3×3** cube (not just a flat 3×3 plane)
- **No longer spawns apple trees** when mining
- Durability raised to **8192**

### Golden & super tool durability

- **Golden Apple Sword** & **Golden Apple Pickaxe**: **1024** durability (golden pick was only 32 before)
- **Super Apple Sword** & **Super Apple Pickaxe**: **8192** durability
- Golden tools repair with Golden Apple; super tools repair with Enchanted Golden Apple

### Progression

- Ore apples unlock in order: **Emerald → Redstone → Diamond → Lapis** (before golden tier)
- Lapis Apple is the end of the ore-apple line; golden tier comes after
- HUD tier bar matches the new unlock order

### Tools & combat

- **Apple Axe** no longer breaks leaves; tree cap harvests logs only
- **Lapis Apple** no longer adds curse enchantments when max-enchanting your inventory

### Food

- Eatable apples can be eaten **even when hunger is full** (`alwaysEat` on food items)
- **Letter A** uses fast-eat

### Recipes

- Corrected shapes and ingredient counts (Iron Apple, Apple Bell, Golden Pickaxe, armor, Bow, Fishing Rod, and more)
- Ore apple recipes use proper ore tags instead of single blocks

### Performance

- Apple Block transmutation and Apple Bow effects scan near players instead of the whole world

### UI

- Item hint tooltips from lang files

### Textures

- Updated item icons, worm entity texture, iron apple armor layer, and bell block textures

### If you already played v1.0.0

- **Break and re-place** old Apple Bells so they render correctly
- For the cleanest progression order, consider a **new world**

---

## v1.0.0

- Initial release
- Full apple progression chain from A to Dragon Apple
- Apple juice fluid, worm entity, apple villagers, throwable apples
- Real APPLE kick mechanic and Big Tree structure
