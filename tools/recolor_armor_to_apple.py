"""
Recolor the user's working Blockbench fire armor atlas into apple style.

Keeps exact UV islands and armor shapes — only shifts palette (no AI slop).
"""
from __future__ import annotations

from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
ASSETS = Path(r"C:\Users\hp\.cursor\projects\d-xnestorio-mode\assets")
HD_OUT = ROOT / "src/main/resources/assets/customapples/textures/models/armor/hd"
BLOCKBENCH_OUT = ROOT / "blockbench"

FIRE_SRC = ASSETS / (
    "c__Users_hp_AppData_Roaming_Cursor_User_workspaceStorage_1da29c5ffe78f49bf665a4a0e78eabc5"
    "_images_apple_chestplate-08427d0e-ef21-4e0a-a074-d656a98e5211.png"
)


def recolor_fire_to_apple(img: Image.Image) -> Image.Image:
    img = img.convert("RGBA")
    out = Image.new("RGBA", img.size, (0, 0, 0, 0))
    sp, op = img.load(), out.load()

    for y in range(img.height):
        for x in range(img.width):
            r, g, b, a = sp[x, y]
            if a < 12:
                continue
            if r + g + b < 28:
                op[x, y] = (0, 0, 0, 0)
                continue

            # Preserve gold/bronze trim
            if r > 140 and g > 90 and b < 120 and r > g:
                nr = min(255, int(r * 1.05))
                ng = min(255, int(g * 0.92))
                nb = max(0, int(b * 0.55))
                op[x, y] = (nr, ng, nb, a)
                continue

            # Bright fire highlights -> apple cream highlight
            if r > 200 and g > 120 and b < 100:
                op[x, y] = (min(255, r), min(255, g + 40), min(255, b + 60), a)
                continue

            # Orange lava / phoenix glow -> glossy apple red
            if r > 100 and g < 140:
                nr = min(255, int(r * 1.08))
                ng = max(0, int(g * 0.35))
                nb = max(0, int(b * 0.35))
                op[x, y] = (nr, ng, nb, a)
                continue

            # Dark metal / charcoal -> deep burgundy leather
            if r < 90 and g < 80 and b < 90:
                op[x, y] = (max(r, 45), max(0, g // 3), max(0, b // 3), a)
                continue

            # Mid tones -> crimson apple body
            nr = min(255, int(r * 1.1))
            ng = max(0, int(g * 0.45))
            nb = max(0, int(b * 0.45))
            op[x, y] = (nr, ng, nb, a)

    return out


def recolor_to_iron_helmet(img: Image.Image) -> Image.Image:
    """Helmet region only: steel gray from fire atlas top area."""
    img = img.convert("RGBA")
    w, h = img.size
    helm_h = int(h * 0.38)
    out = Image.new("RGBA", img.size, (0, 0, 0, 0))
    sp, op = img.load(), out.load()

    for y in range(h):
        for x in range(w):
            if y > helm_h:
                continue
            r, g, b, a = sp[x, y]
            if a < 12 or r + g + b < 28:
                continue
            # desaturate + lift toward silver
            avg = (r + g + b) / 3
            nr = int(min(255, avg * 1.15))
            ng = int(min(255, avg * 1.12))
            nb = int(min(255, avg * 1.18))
            # tiny red apple accent on warm pixels
            if r > g + 20 and r > 120:
                nr = min(255, int(r * 0.95))
                ng = int(g * 0.4)
                nb = int(b * 0.4)
            op[x, y] = (nr, ng, nb, a)

    return out


def save(img: Image.Image, rel: str) -> None:
    for base in (HD_OUT, BLOCKBENCH_OUT):
        base.mkdir(parents=True, exist_ok=True)
        path = base / rel
        img.save(path)
        print(f"OK {path} ({img.size[0]}x{img.size[1]})")


def main() -> None:
    if not FIRE_SRC.exists():
        raise SystemExit(f"Missing fire reference: {FIRE_SRC}")

    fire = Image.open(FIRE_SRC).convert("RGBA")
    apple = recolor_fire_to_apple(fire)
    iron = recolor_to_iron_helmet(fire)

    save(apple, "apple_chestplate.png")
    save(apple, "apple_armor_texture.png")
    save(iron, "iron_apple_helmet.png")
    print("Done — recolored YOUR armor layout, not random AI art.")


if __name__ == "__main__":
    main()
