"""Install AI-generated PNGs into mod textures with edge-only background removal."""
from __future__ import annotations

from collections import deque
from pathlib import Path

from PIL import Image

ASSETS = Path(r"C:\Users\hp\.cursor\projects\d-xnestorio-mode\assets")
ITEM_OUT = Path(__file__).resolve().parent.parent / "src/main/resources/assets/customapples/textures/item"
BLOCK_OUT = Path(__file__).resolve().parent.parent / "src/main/resources/assets/customapples/textures/block"
ENTITY_OUT = Path(__file__).resolve().parent.parent / "src/main/resources/assets/customapples/textures/entity"
ENTITY_BELL_OUT = (
    Path(__file__).resolve().parent.parent
    / "src/main/resources/assets/customapples/textures/entity/bell"
)

# Exact vanilla 32x32 bell_body UV atlas (gold bell layout — model UVs match this only)
VANILLA_BELL_ATLAS = ASSETS / (
    "c__Users_hp_AppData_Roaming_Cursor_User_workspaceStorage_1da29c5ffe78f49bf665a4a0e78eabc5"
    "_images_image-6967e2d6-157f-49d1-9a31-0814ac9a8e0e.png"
)

# AI-generated in-world block textures (source name -> output filename)
# Block face textures come from install_block_faces.py (AI top/side gens).
# Bell frame/post + swinging body only here.
BLOCK_PLACED = {
    "apple_bell_frame_gen": ("apple_bell_frame.png", 16),
    "apple_bell_post_gen": ("apple_bell_post.png", 16),
}

BLOCK_ITEMS = {
    "apple_block": "apple_block",
    "apple_bell": "apple_bell",
    "golden_apple_block": "golden_apple_block",
}


def remove_edge_background(img: Image.Image, tolerance: int = 28) -> Image.Image:
    """Remove only background connected to image borders (keeps internal gray)."""
    img = img.convert("RGBA")
    w, h = img.size
    px = img.load()

    def matches_bg(r: int, g: int, b: int, br: int, bg: int, bb: int) -> bool:
        return abs(r - br) <= tolerance and abs(g - bg) <= tolerance and abs(b - bb) <= tolerance

    corner_samples = [
        px[0, 0][:3],
        px[w - 1, 0][:3],
        px[0, h - 1][:3],
        px[w - 1, h - 1][:3],
    ]

    def is_border_bg(x: int, y: int) -> bool:
        r, g, b, a = px[x, y]
        if a < 8:
            return True
        return any(matches_bg(r, g, b, *sample) for sample in corner_samples)

    seen: set[tuple[int, int]] = set()
    q: deque[tuple[int, int]] = deque()
    for x in range(w):
        q.append((x, 0))
        q.append((x, h - 1))
    for y in range(h):
        q.append((0, y))
        q.append((w - 1, y))

    while q:
        x, y = q.popleft()
        if (x, y) in seen or x < 0 or y < 0 or x >= w or y >= h:
            continue
        if not is_border_bg(x, y):
            continue
        seen.add((x, y))
        q.extend([(x + 1, y), (x - 1, y), (x, y + 1), (x, y - 1)])

    for x, y in seen:
        r, g, b, _ = px[x, y]
        px[x, y] = (r, g, b, 0)

    return img


def trim_and_resize(img: Image.Image, size: int) -> Image.Image:
    bbox = img.getbbox()
    if bbox:
        img = img.crop(bbox)
    if img.width == 0 or img.height == 0:
        return Image.new("RGBA", (size, size), (0, 0, 0, 0))
    scale = min(size / img.width, size / img.height)
    nw = max(1, int(img.width * scale))
    nh = max(1, int(img.height * scale))
    resized = img.resize((nw, nh), Image.Resampling.NEAREST)
    out = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    out.paste(resized, ((size - nw) // 2, (size - nh) // 2), resized)
    return out


def install_block_item_icons() -> None:
    """Item icons for placeable blocks — always from AI isometric sources."""
    pairs = {
        "apple_block": "apple_block",
        "golden_apple_block": "golden_apple_block",
    }
    for item_name, src_name in pairs.items():
        src = ASSETS / f"{src_name}.png"
        if not src.exists():
            print(f"MISSING item source {src_name}")
            continue
        img = Image.open(src).convert("RGBA")
        if max(img.size) > 512:
            img.thumbnail((512, 512), Image.Resampling.NEAREST)
        img = remove_edge_background(img)
        icon = trim_and_resize(img, 64)
        ITEM_OUT.mkdir(parents=True, exist_ok=True)
        icon.save(ITEM_OUT / f"{item_name}.png")
        print(f"OK item/{item_name}.png (AI isometric)")


def install_seamless_tile(name: str, out_name: str, size: int = 16) -> bool:
    """Seamless block tile (wood planks) — never flood-fill; dark pixels are the texture."""
    src = ASSETS / f"{name}.png"
    if not src.exists():
        print(f"MISSING {name}")
        return False
    img = Image.open(src).convert("RGBA")
    if img.size != (size, size):
        img = img.resize((size, size), Image.Resampling.NEAREST)
    BLOCK_OUT.mkdir(parents=True, exist_ok=True)
    img.save(BLOCK_OUT / out_name)
    print(f"OK block/{out_name} (seamless tile)")
    return True


def install_block_texture(name: str, out_name: str, size: int) -> bool:
    """Install one AI image directly as a block texture (no procedural face split)."""
    src = ASSETS / f"{name}.png"
    if not src.exists():
        print(f"MISSING {name}")
        return False
    img = Image.open(src).convert("RGBA")
    if max(img.size) > 512:
        img.thumbnail((512, 512), Image.Resampling.NEAREST)
    img = remove_edge_background(img)
    face = trim_and_resize(img, size)
    BLOCK_OUT.mkdir(parents=True, exist_ok=True)
    face.save(BLOCK_OUT / out_name)
    print(f"OK block/{out_name}")
    return True


def gold_to_apple(r: int, g: int, b: int, a: int) -> tuple[int, int, int, int]:
    """Remap vanilla gold bell atlas pixels to apple red — same positions, no layout change."""
    if a < 8 or r + g + b < 15:
        return (0, 0, 0, 0)
    bright = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0
    if bright > 0.92:
        return (255, 228, 210, a)
    if bright > 0.72:
        t = (bright - 0.72) / 0.20
        return (int(205 + 50 * t), int(35 + 70 * t), int(28 + 45 * t), a)
    if bright > 0.42:
        t = (bright - 0.42) / 0.30
        return (int(145 + 60 * t), int(18 + 17 * t), int(14 + 14 * t), a)
    if bright > 0.18:
        t = (bright - 0.18) / 0.24
        return (int(72 + 73 * t), int(10 + 8 * t), int(8 + 6 * t), a)
    return (int(35 + bright * 120), int(6 + bright * 8), int(5 + bright * 6), a)


def remap_vanilla_bell_atlas(src: Image.Image, size: int = 32) -> Image.Image:
    src = src.convert("RGBA")
    if src.size != (size, size):
        src = src.resize((size, size), Image.Resampling.NEAREST)
    out = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    spx, opx = src.load(), out.load()
    for y in range(size):
        for x in range(size):
            opx[x, y] = gold_to_apple(*spx[x, y])
    return out


def install_bell_body(size: int = 32) -> bool:
    """Swinging bell uses ModelLayers.BELL — texture MUST be vanilla 32x32 UV layout."""
    if not VANILLA_BELL_ATLAS.exists():
        print(f"MISSING vanilla bell atlas: {VANILLA_BELL_ATLAS.name}")
        return False
    body = remap_vanilla_bell_atlas(Image.open(VANILLA_BELL_ATLAS), size)
    ENTITY_BELL_OUT.mkdir(parents=True, exist_ok=True)
    body.save(ENTITY_BELL_OUT / "bell_body.png")
    BLOCK_OUT.mkdir(parents=True, exist_ok=True)
    body.save(BLOCK_OUT / "apple_bell_body.png")
    # Particle: bottom opening from atlas (vanilla UV region ~ lower-right of 32x32 sheet)
    bottom = body.crop((16, 20, 32, 32)).resize((16, 16), Image.Resampling.NEAREST)
    bottom.save(BLOCK_OUT / "apple_bell_body_bottom.png")
    print("OK bell_body (vanilla UV atlas, apple remap)")
    return True


def install_placed_blocks() -> None:
    for src_name, (out_name, size) in BLOCK_PLACED.items():
        install_seamless_tile(src_name, out_name, size)
    install_bell_body()


def install(name: str, size: int = 64) -> bool:
    src = ASSETS / f"{name}.png"
    if not src.exists():
        print(f"MISSING {name}")
        return False
    img = Image.open(src)
    # Downscale before flood-fill (AI images are often 1024px+)
    if max(img.size) > 512:
        img.thumbnail((512, 512), Image.Resampling.NEAREST)
    img = remove_edge_background(img)
    icon = trim_and_resize(img, size)
    ITEM_OUT.mkdir(parents=True, exist_ok=True)
    icon.save(ITEM_OUT / f"{name}.png")
    if name == "worm":
        ENTITY_OUT.mkdir(parents=True, exist_ok=True)
        trim_and_resize(img, 64).resize((64, 32), Image.Resampling.LANCZOS).save(ENTITY_OUT / "worm.png")
    print(f"OK {name}")
    return True


if __name__ == "__main__":
    import sys

    if len(sys.argv) > 1 and sys.argv[1] == "--placed-blocks":
        install_placed_blocks()
        sys.exit(0)

    ALL_ITEMS = [
        "letter_a", "wooden_apple", "appl", "dirt_apple", "bread_apple", "emerald_apple",
        "redstone_apple", "diamond_apple", "appletizer", "lapis_apple", "end_apple", "dragon_apple",
        "apple_axe", "splinter", "apple_sword", "apple_fishing_rod", "flint_and_apple", "apple_bow",
        "golden_apple_sword", "golden_apple_pickaxe", "super_apple_sword", "super_apple_pickaxe",
        "iron_apple", "apple_boots", "apple_leggings", "apple_chestplate", "diamond_apple_chestplate",
        "worm", "app", "apple_bucket", "apple_bell", "apple_apple_apple", "apple_block", "golden_apple_block",
    ]
    names = sys.argv[1:] if len(sys.argv) > 1 else ALL_ITEMS
    for n in names:
        install(n)
    install_block_item_icons()
