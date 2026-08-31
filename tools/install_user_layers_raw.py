"""Copy user 64x32 armor layers exactly — no resize, no edit."""
from __future__ import annotations

import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
ASSETS = Path(r"C:\Users\hp\.cursor\projects\d-xnestorio-mode\assets")
LAYER1 = ASSETS / (
    "c__Users_hp_AppData_Roaming_Cursor_User_workspaceStorage_1da29c5ffe78f49bf665a4a0e78eabc5_images_"
    "layer_1-ee4bdeb2-a552-4228-9d7d-b142a6ab0a58.png"
)
LAYER2 = ASSETS / (
    "c__Users_hp_AppData_Roaming_Cursor_User_workspaceStorage_1da29c5ffe78f49bf665a4a0e78eabc5_images_"
    "layer_2-479e291d-03de-4c5c-b0d2-c69c691235cf.png"
)

ARMOR = ROOT / "src/main/resources/assets/customapples/textures/models/armor"
HD = ARMOR / "hd"
BB = ROOT / "blockbench"


def copy_exact(src: Path, dst: Path) -> None:
    dst.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(src, dst)
    print(f"OK {dst}")


def main() -> None:
    if not LAYER1.exists() or not LAYER2.exists():
        raise SystemExit("Missing layer_1 or layer_2 in assets")

    for dst in (
        ARMOR / "apple_layer_1.png",
        HD / "apple_layer_1.png",
        HD / "apple_chestplate.png",
        HD / "apple_armor_texture.png",
        BB / "apple_layer_1.png",
        BB / "apple_armor_texture.png",
        BB / "apple_chestplate.png",
    ):
        copy_exact(LAYER1, dst)

    for dst in (
        ARMOR / "apple_layer_2.png",
        HD / "apple_layer_2.png",
        BB / "apple_layer_2.png",
    ):
        copy_exact(LAYER2, dst)

    print("Installed 64x32 layers unchanged. F3+T in game to reload.")


if __name__ == "__main__":
    main()
