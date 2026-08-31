"""Install user's custom chestplate texture onto armor atlases."""
from __future__ import annotations

from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
ASSETS = Path(r"C:\Users\hp\.cursor\projects\d-xnestorio-mode\assets")
USER_CHEST = ASSETS / (
    "c__Users_hp_AppData_Roaming_Cursor_User_workspaceStorage_1da29c5ffe78f49bf665a4a0e78eabc5_images_"
    "Generated_Image_August_30__2026_-_2_08PM-b215bee2-ccd6-4076-92d3-4acc8fa1bd77.png"
)
HD = ROOT / "src/main/resources/assets/customapples/textures/models/armor/hd"
VAN = ROOT / "src/main/resources/assets/customapples/textures/models/armor"
BB = ROOT / "blockbench"

TORSO_BOX = (0, 512, 1024, 1024)
ARM_BOX = (1280, 0, 1792, 512)


def main() -> None:
    if not USER_CHEST.exists():
        raise SystemExit(f"Missing {USER_CHEST}")

    user = Image.open(USER_CHEST).convert("RGBA")
    w, h = user.size
  # torso front+back = left half of strip
    torso_w = w // 2
    torso = user.crop((0, 0, torso_w, h))
    arm = user.crop((torso_w, 0, w, h))

    hd_path = HD / "apple_chestplate.png"
    if hd_path.exists():
        mat0 = Image.open(hd_path).convert("RGBA")
    else:
        mat0 = Image.new("RGBA", (2048, 1024), (0, 0, 0, 0))

    x0, y0, x1, y1 = TORSO_BOX
    mat0.paste(torso.resize((x1 - x0, y1 - y0), Image.Resampling.NEAREST), (x0, y0))

    ax0, ay0, ax1, ay1 = ARM_BOX
    mat0.paste(arm.resize((ax1 - ax0, ay1 - ay0), Image.Resampling.NEAREST), (ax0, ay0))

    for d in (HD, BB):
        d.mkdir(parents=True, exist_ok=True)
        mat0.save(d / "apple_chestplate.png")
        mat0.save(d / "apple_armor_texture.png")
        print(f"OK {d / 'apple_chestplate.png'}")

    VAN.mkdir(parents=True, exist_ok=True)
    mat0.resize((512, 256), Image.Resampling.NEAREST).save(VAN / "apple_layer_1.png")
    user.resize((512, 256), Image.Resampling.NEAREST).save(VAN / "apple_chestplate.png")
    print(f"OK {VAN / 'apple_layer_1.png'}")
    user.save(ASSETS / "user_apple_chestplate.png")
    print("Your chestplate is applied. Press F3+T in game to reload textures.")


if __name__ == "__main__":
    main()
