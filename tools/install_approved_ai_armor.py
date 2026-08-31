"""Replace procedural textures with ornate fire UV + approved AI overlays."""
from __future__ import annotations

import subprocess
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
ASSETS = Path(r"C:\Users\hp\.cursor\projects\d-xnestorio-mode\assets")
HD = ROOT / "src/main/resources/assets/customapples/textures/models/armor/hd"
VAN = ROOT / "src/main/resources/assets/customapples/textures/models/armor"
BB = ROOT / "blockbench"

HELM_AI = ASSETS / "iron_apple_helmet_uv_piece_v2.png"
CHEST_AI = ASSETS / "apple_chestplate_uv_piece.png"

# GLTF mesh regions on 2048x1024 mat0
HELM_BOX = (512, 0, 1280, 512)
TORSO_BOX = (0, 512, 1024, 1024)


def overlay_box(base: Image.Image, piece: Path, box: tuple[int, int, int, int]) -> None:
    x0, y0, x1, y1 = box
    w, h = x1 - x0, y1 - y0
    art = Image.open(piece).convert("RGBA").resize((w, h), Image.Resampling.LANCZOS)
    base.paste(art, (x0, y0))


def save_van(img: Image.Image, van_name: str) -> None:
    VAN.mkdir(parents=True, exist_ok=True)
    img.resize((img.width // 4, img.height // 4), Image.Resampling.NEAREST).save(VAN / van_name)


def main() -> None:
    # 1) Ornate fire UV base (correct GLTF placement for legs/boots/arms)
    subprocess.run(["python", str(ROOT / "tools" / "install_uv_correct_armor.py")], check=True)

    # 2) Overlay approved AI art on top of fire base
    for name in ("apple_chestplate.png", "apple_armor_texture.png"):
        path = HD / name
        if not path.exists():
            continue
        mat0 = Image.open(path).convert("RGBA")
        if CHEST_AI.exists():
            overlay_box(mat0, CHEST_AI, TORSO_BOX)
            print(f"Overlay ornate chest AI -> {name}")
        mat0.save(path)
        mat0.save(BB / name)

    if HELM_AI.exists():
        iron_path = HD / "iron_apple_helmet.png"
        iron = Image.new("RGBA", (2048, 1024), (0, 0, 0, 0))
        overlay_box(iron, HELM_AI, HELM_BOX)
        for d in (HD, BB):
            iron.save(d / "iron_apple_helmet.png")
        save_van(iron, "iron_apple_layer_1.png")
        print("Installed iron apple helmet AI on mesh_0 region")

    save_van(Image.open(HD / "apple_chestplate.png"), "apple_layer_1.png")
    print("Done — ornate fire UV + your approved AI chest/helmet. Press F3+T in game to reload textures.")


if __name__ == "__main__":
    main()
