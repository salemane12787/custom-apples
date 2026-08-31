"""Copy HD chestplate art at full resolution — no resize, no pixelization."""
from __future__ import annotations

from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
ASSETS = Path(r"C:\Users\hp\.cursor\projects\d-xnestorio-mode\assets")
HD_OUT = ROOT / "src/main/resources/assets/customapples/textures/models/armor/hd"

DEFAULT_REF = ASSETS / (
    "c__Users_hp_AppData_Roaming_Cursor_User_workspaceStorage_1da29c5ffe78f49bf665a4a0e78eabc5"
    "_images_Gemini_Generated_Image_tdaz0tdaz0tdaz0t__1_-50865fcf-c5ee-47ab-a4ad-d4574ed5dcfd.png"
)


def black_to_alpha(img: Image.Image, threshold: int = 24) -> Image.Image:
    img = img.convert("RGBA")
    px = img.load()
    for y in range(img.height):
        for x in range(img.width):
            r, g, b, a = px[x, y]
            if r <= threshold and g <= threshold and b <= threshold:
                px[x, y] = (0, 0, 0, 0)
    return img


def install(reference: Path = DEFAULT_REF) -> None:
    if not reference.exists():
        raise SystemExit(f"Missing reference: {reference}")

    img = black_to_alpha(Image.open(reference))
    HD_OUT.mkdir(parents=True, exist_ok=True)
    out = HD_OUT / "apple_chestplate.png"
    img.save(out)
    print(f"OK {out} ({img.size[0]}x{img.size[1]}, full resolution)")


if __name__ == "__main__":
    install()
