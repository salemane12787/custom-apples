"""Install AI texture — only inside exact GLTF UV mask pixels. No recolor code."""
from __future__ import annotations

from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
ASSETS = Path(r"C:\Users\hp\.cursor\projects\d-xnestorio-mode\assets")
HD = ROOT / "src/main/resources/assets/customapples/textures/models/armor/hd"
BB = ROOT / "blockbench"
VANILLA = ROOT / "src/main/resources/assets/customapples/textures/models/armor"

W, H = 2048, 1024


def load_mask(path: Path) -> Image.Image:
    return Image.open(path).convert("L").resize((W, H), Image.Resampling.NEAREST)


def apply_mask(paint: Path, mask: Image.Image, mat_mask: Image.Image | None, mat_id: int | None) -> Image.Image:
    src = Image.open(paint).convert("RGBA").resize((W, H), Image.Resampling.NEAREST)
    out = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    sp, op = src.load(), out.load()
    mp = mask.load()
    mmp = mat_mask.load() if mat_mask else None
    for y in range(H):
        for x in range(W):
            if mp[x, y] < 128:
                continue
            if mat_id is not None and mmp is not None and mmp[x, y] != mat_id:
                continue
            op[x, y] = sp[x, y]
    return out


def save(img: Image.Image, hd_names: list[str], vanilla_name: str | None = None) -> None:
    for base in (HD, BB):
        base.mkdir(parents=True, exist_ok=True)
        for n in hd_names:
            img.save(base / n)
            print(f"OK {base / n}")
    if vanilla_name:
        VANILLA.mkdir(parents=True, exist_ok=True)
        van = img.resize((512, 256), Image.Resampling.NEAREST)
        van.save(VANILLA / vanilla_name)
        print(f"OK {VANILLA / vanilla_name}")


def main() -> None:
    mask = load_mask(ASSETS / "gltf_uv_mask_layer1.png")
    mat = load_mask(ASSETS / "gltf_uv_mat_mask.png")

    ai1 = ASSETS / "ai_apple_layer1_hd.png"
    ai2 = ASSETS / "ai_apple_layer2_hd.png"
    if not ai1.exists():
        raise SystemExit(f"Run AI generation first — missing {ai1}")

    layer1 = apply_mask(ai1, mask, mat, 0)
    save(layer1, ["apple_chestplate.png", "apple_armor_texture.png"], "apple_layer_1.png")

    if ai2.exists():
        layer2 = apply_mask(ai2, mask, mat, 1)
        save(layer2, ["apple_layer_2.png"], "apple_layer_2.png")

    iron = ASSETS / "ai_iron_helmet_hd.png"
    if iron.exists():
        # helmet = top 42% of layer1 mask
        helm = Image.new("L", (W, H), 0)
        mp, hp = mask.load(), helm.load()
        for y in range(int(H * 0.42)):
            for x in range(W):
                if mp[x, y] > 128:
                    hp[x, y] = 255
        iron_img = apply_mask(iron, helm, None, None)
        save(iron_img, ["iron_apple_helmet.png"], "iron_apple_layer_1.png")


if __name__ == "__main__":
    main()
