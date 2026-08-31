# UV Texture Generator (schema-driven)

Generates armor textures that **exactly match your UV layout** using a JSON schema.

## Quick start

```bash
python tools/generate_uv_texture.py iron_apple_helmet
python tools/generate_uv_texture.py apple_chestplate
python tools/generate_uv_texture.py --all
```

Output: `assets/generated/<name>.png`

## How it works

1. **UV reference** — your screenshot/wireframe PNG (black = empty, anything else = island)
2. **Schema** — defines each island's position (`bbox_norm`) and painter (`generator`)
3. **Generator** — code paints each island at the exact pixel coordinates

## Schema format

```json
{
  "name": "my_piece",
  "uv_reference": "path_or_assets_filename.png",
  "scale": 16,
  "layout": "manual",
  "seed": 42,
  "islands": {
    "front": {
      "generator": "iron_apple",
      "view": "front",
      "bbox_norm": [0.125, 0.25, 0.25, 0.5]
    }
  }
}
```

### `bbox_norm`

Normalized rectangle `[x0, y0, x1, y1]` from 0.0 to 1.0 across the reference image.
Example: Minecraft head-cross top face = `[0.125, 0.0, 0.25, 0.25]` on a 64×32 atlas.

### Generators

| Generator       | Options              | Description                    |
|----------------|----------------------|--------------------------------|
| `iron_apple`   | `view`: top/front/side/back | Metallic iron apple fill  |
| `plain_iron`   | —                    | Brushed iron metal             |
| `ornate_chest` | `part`: front/back/arm | Red/gold apple chestplate   |

Add new generators in `generate_uv_texture.py` → `GENERATORS` dict.

## New piece workflow

1. Send UV screenshot → save to `assets/`
2. Copy a schema, set `bbox_norm` per island (or ask me to measure)
3. Run generator → check `assets/generated/`
4. Adjust schema / generator until it looks right
