"""
Assemble per-face AI chestplate images into the vanilla humanoid layer-1 UV atlas (64x32),
then upscale 4x for sharper equipped armor in-game.
"""
from __future__ import annotations

from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
ASSETS = Path(r"C:\Users\hp\.cursor\projects\d-xnestorio-mode\assets")
ARMOR_OUT = ROOT / "src/main/resources/assets/customapples/textures/models/armor"
PREVIEW_OUT = ASSETS / "chestplate_faces_preview.png"

W, H = 64, 32
UPSCALE = 4


def blank() -> Image.Image:
    return Image.new("RGBA", (W, H), (0, 0, 0, 0))


def box_faces(u: int, v: int, dx: int, dy: int, dz: int) -> list[tuple[int, int, int, int]]:
    return [
        (u, v + dz, dz, dy),
        (u + dz + dx, v + dz, dz, dy),
        (u + dz, v, dx, dz),
        (u + dz + dx, v, dx, dz),
        (u + dz, v + dz, dx, dy),
        (u + dz + dx + dx, v + dz, dx, dy),
    ]


def paste_face(layer: Image.Image, rect: tuple[int, int, int, int], path: Path) -> None:
    x, y, rw, rh = rect
    if not path.exists():
        raise SystemExit(f"Missing face image: {path}")
    face = Image.open(path).convert("RGBA")
    face = face.resize((rw, rh), Image.Resampling.LANCZOS)
    layer.paste(face, (x, y), face)


def upscale(layer: Image.Image) -> Image.Image:
    return layer.resize((W * UPSCALE, H * UPSCALE), Image.Resampling.NEAREST)


def main() -> None:
    torso = box_faces(16, 16, 8, 12, 4)
    arm = box_faces(40, 16, 4, 12, 4)

    torso_files = [
        "chestplate_torso_left.png",
        "chestplate_torso_right.png",
        "chestplate_torso_top.png",
        "chestplate_torso_bottom.png",
        "chestplate_torso_front.png",
        "chestplate_torso_back.png",
    ]
    arm_files = [
        "chestplate_arm_outer.png",
        "chestplate_arm_inner.png",
        "chestplate_arm_top.png",
        "chestplate_arm_bottom.png",
        "chestplate_arm_front.png",
        "chestplate_arm_back.png",
    ]

    layer = blank()
    for rect, name in zip(torso, torso_files, strict=True):
        paste_face(layer, rect, ASSETS / name)
    for rect, name in zip(arm, arm_files, strict=True):
        paste_face(layer, rect, ASSETS / name)

    ARMOR_OUT.mkdir(parents=True, exist_ok=True)
    out = upscale(layer)
    out_path = ARMOR_OUT / "apple_chestplate_layer_1.png"
    out.save(out_path)
    print(f"OK {out_path} ({out.size[0]}x{out.size[1]})")

    # Labeled preview of each source face for review / Blockbench reference
    labels = [f.replace(".png", "").replace("chestplate_", "") for f in torso_files + arm_files]
    files = torso_files + arm_files
    cell = 128
    preview = Image.new("RGBA", (cell * len(files), cell + 24), (32, 32, 32, 255))
    for i, (name, label) in enumerate(zip(files, labels, strict=True)):
        img = Image.open(ASSETS / name).convert("RGBA")
        img.thumbnail((cell, cell), Image.Resampling.LANCZOS)
        ox = i * cell + (cell - img.width) // 2
        oy = (cell - img.height) // 2
        preview.paste(img, (ox, oy), img)
    preview.save(PREVIEW_OUT)
    print(f"OK preview {PREVIEW_OUT}")


if __name__ == "__main__":
    main()
