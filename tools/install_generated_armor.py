"""Install schema-generated pieces into mod HD armor atlases for in-game preview."""
from __future__ import annotations

from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
GEN = Path(r"C:\Users\hp\.cursor\projects\d-xnestorio-mode\assets\generated")
HD = ROOT / "src/main/resources/assets/customapples/textures/models/armor/hd"
VAN = ROOT / "src/main/resources/assets/customapples/textures/models/armor"
BB = ROOT / "blockbench"

W, H = 2048, 1024


def save_all(img: Image.Image, hd_name: str, van_name: str | None = None) -> None:
    HD.mkdir(parents=True, exist_ok=True)
    BB.mkdir(parents=True, exist_ok=True)
    img.save(HD / hd_name)
    img.save(BB / hd_name)
    print(f"OK {HD / hd_name}")
    if van_name:
        VAN.mkdir(parents=True, exist_ok=True)
        van = img.resize((img.width // 4, img.height // 4), Image.Resampling.NEAREST)
        van.save(VAN / van_name)
        print(f"OK {VAN / van_name}")


def main() -> None:
    leggings = Image.open(GEN / "apple_leggings.png")
    save_all(leggings, "apple_layer_2.png", "apple_layer_2.png")

    mat0 = Image.new("RGBA", (W, H), (0, 0, 0, 0))

    boots = Image.open(GEN / "apple_boots.png").resize((512, 512), Image.Resampling.NEAREST)
    mat0.paste(boots, (0, 0))

    helm = Image.open(GEN / "iron_apple_helmet.png").resize((768, 512), Image.Resampling.NEAREST)
    mat0.paste(helm, (512, 0))

    chest_src = Image.open(GEN / "apple_chestplate.png")
    chest = chest_src.resize((1024, 512), Image.Resampling.NEAREST)
    mat0.paste(chest, (0, 512))

    arm_l = chest_src.crop((320, 0, 480, 256)).resize((512, 512), Image.Resampling.NEAREST)
    mat0.paste(arm_l, (1280, 0))

    save_all(mat0, "apple_chestplate.png", "apple_layer_1.png")
    save_all(mat0, "apple_armor_texture.png")

    iron = Image.open(GEN / "iron_apple_helmet.png").resize((768, 512), Image.Resampling.NEAREST)
    save_all(iron, "iron_apple_helmet.png", "iron_apple_layer_1.png")

    print("Installed generated armor textures for runClient preview.")


if __name__ == "__main__":
    main()
