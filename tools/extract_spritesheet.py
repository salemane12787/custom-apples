"""Extract item textures from the core collection spritesheet."""
from pathlib import Path
from PIL import Image

SRC = Path(
    r"C:\Users\hp\.cursor\projects\d-xnestorio-mode\assets"
    r"\c__Users_hp_AppData_Roaming_Cursor_User_workspaceStorage_1da29c5ffe78f49bf665a4a0e78eabc5_images"
    r"_Gemini_Generated_Image_mkpthemkpthemkpt-89d43884-89a6-4500-8e8d-ab9cbfdddffd.jpg"
)
ROOT = Path(__file__).resolve().parent.parent
ITEM_OUT = ROOT / "src/main/resources/assets/customapples/textures/item"
BLOCK_OUT = ROOT / "src/main/resources/assets/customapples/textures/block"

# (row, col) 0-based -> texture id; None = skip cell
GRID_MAP: dict[tuple[int, int], str] = {
    (0, 3): "wooden_apple",
    (0, 4): "appl",
    (0, 5): "dirt_apple",
    (0, 6): "bread_apple",
    (0, 7): "emerald_apple",
    (0, 8): "redstone_apple",
    (1, 0): "letter_a",
    (1, 1): "diamond_apple",
    (1, 2): "appletizer",
    (1, 3): "lapis_apple",
    (1, 4): "end_apple",
    (1, 6): "dragon_apple",
    (1, 7): "apple_axe",
    (1, 8): "splinter",
    (2, 1): "apple_sword",
    (2, 2): "apple_fishing_rod",
    (2, 3): "flint_and_apple",
    (2, 4): "apple_bow",
    (2, 5): "golden_apple_sword",
    (2, 6): "golden_apple_pickaxe",
    (2, 8): "super_apple_sword",
    (3, 1): "super_apple_pickaxe",
    (3, 2): "iron_apple",
    (3, 3): "apple_boots",
    (3, 4): "apple_leggings",
    (3, 5): "apple_chestplate",
    (3, 7): "diamond_apple_chestplate",
    (4, 1): "worm",
    (4, 2): "app",
    (4, 3): "apple_bucket",
    (4, 4): "apple_bell",
    (4, 5): "apple_apple_apple",
    (4, 6): "apple_block",
    (4, 7): "golden_apple_block",
}

BLOCK_COPY = {
    "apple_block": "apple_block",
    "apple_bell": "apple_bell",
    "golden_apple_block": "golden_apple_block",
}


def is_background(r: int, g: int, b: int) -> bool:
    if abs(r - g) < 18 and abs(g - b) < 18 and abs(r - b) < 18:
        if 95 <= r <= 215:
            return True
    if r >= 195 and g >= 195 and b >= 195:
        return True
    return False


def remove_background(img: Image.Image) -> Image.Image:
    img = img.convert("RGBA")
    px = img.load()
    w, h = img.size
    for y in range(h):
        for x in range(w):
            r, g, b, a = px[x, y]
            if is_background(r, g, b):
                px[x, y] = (r, g, b, 0)
    return img


def trim_transparent(img: Image.Image) -> Image.Image:
    bbox = img.getbbox()
    if bbox is None:
        return img
    return img.crop(bbox)


def extract_cell(sheet: Image.Image, row: int, col: int, cols: int, rows: int) -> Image.Image:
    w, h = sheet.size
    cell_w = w / cols
    cell_h = h / rows
    margin = 0.12
    x0 = int(col * cell_w + cell_w * margin)
    y0 = int(row * cell_h + cell_h * margin)
    x1 = int((col + 1) * cell_w - cell_w * margin)
    y1 = int((row + 1) * cell_h - cell_h * margin)
    return sheet.crop((x0, y0, x1, y1))


def to_item_icon(img: Image.Image, size: int = 16) -> Image.Image:
    img = trim_transparent(img)
    if img.width == 0 or img.height == 0:
        return Image.new("RGBA", (size, size), (0, 0, 0, 0))
    scale = min(size / img.width, size / img.height)
    nw = max(1, int(img.width * scale))
    nh = max(1, int(img.height * scale))
    resized = img.resize((nw, nh), Image.Resampling.LANCZOS)
    out = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    ox = (size - nw) // 2
    oy = (size - nh) // 2
    out.paste(resized, (ox, oy), resized)
    return out


def main() -> None:
    ITEM_OUT.mkdir(parents=True, exist_ok=True)
    BLOCK_OUT.mkdir(parents=True, exist_ok=True)
    sheet = Image.open(SRC)
    cols, rows = 9, 5

    for (row, col), name in GRID_MAP.items():
        cell = extract_cell(sheet, row, col, cols, rows)
        cell = remove_background(cell)
        icon = to_item_icon(cell, 16)
        out_path = ITEM_OUT / f"{name}.png"
        icon.save(out_path)
        print(f"item/{name}.png")

    for block_id, item_id in BLOCK_COPY.items():
        src = ITEM_OUT / f"{item_id}.png"
        if src.exists():
            block_icon = to_item_icon(remove_background(Image.open(src)), 16)
            block_icon.save(BLOCK_OUT / f"{block_id}.png")
            print(f"block/{block_id}.png")

    # Entity worm (32x16 style from item)
    worm_src = ITEM_OUT / "worm.png"
    if worm_src.exists():
        worm = Image.open(worm_src)
        worm_big = worm.resize((32, 16), Image.Resampling.LANCZOS)
        entity_dir = ROOT / "src/main/resources/assets/customapples/textures/entity"
        entity_dir.mkdir(parents=True, exist_ok=True)
        worm_big.save(entity_dir / "worm.png")
        print("entity/worm.png")


if __name__ == "__main__":
    main()
