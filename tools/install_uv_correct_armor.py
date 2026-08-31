"""Install armor textures aligned to exact GLTF UV — uses UV-correct source atlases, not centered AI."""
from __future__ import annotations

import base64
import json
import struct
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
GLTF = ROOT / "blockbench" / "model.gltf"
ASSETS = Path(r"C:\Users\hp\.cursor\projects\d-xnestorio-mode\assets")
HD = ROOT / "src/main/resources/assets/customapples/textures/models/armor/hd"
BB = ROOT / "blockbench"
VANILLA = ROOT / "src/main/resources/assets/customapples/textures/models/armor"

W, H = 2048, 1024
# fire_uv atlas was painted at 24x (1536x1024); model uses 32x (2048x1024)
FIRE_SCALE = W / 1536.0


def decode_uri(uri: str) -> bytes:
    return base64.b64decode(uri.split(",", 1)[1])


def accessor_data(gltf: dict, blob: bytes, idx: int):
    acc = gltf["accessors"][idx]
    bv = gltf["bufferViews"][acc["bufferView"]]
    start = bv.get("byteOffset", 0)
    stride = bv.get("byteStride", 0)
    count = acc["count"]
    fmt = {5126: "f", 5123: "H"}[acc["componentType"]]
    comp = {"SCALAR": 1, "VEC2": 2, "VEC3": 3}[acc["type"]]
    size = struct.calcsize(fmt) * comp
    if stride == 0:
        stride = size
    out = []
    for i in range(count):
        off = start + i * stride
        vals = struct.unpack_from("<" + fmt * comp, blob, off)
        out.append(vals if comp > 1 else vals[0])
    return out


def point_in_tri(p, a, b, c) -> bool:
    def sign(p1, p2, p3):
        return (p1[0] - p3[0]) * (p2[1] - p3[1]) - (p2[0] - p3[0]) * (p1[1] - p3[1])
    d1, d2, d3 = sign(p, a, b), sign(p, b, c), sign(p, c, a)
    return not ((d1 < 0 or d2 < 0 or d3 < 0) and (d1 > 0 or d2 > 0 or d3 > 0))


def uv_px(u: float, v: float) -> tuple[int, int]:
    if u > 1.5 or v > 1.5:
        return int(round(u * FIRE_SCALE)), int(round(v * FIRE_SCALE))
    return int(round(u * W)), int(round((1.0 - v) * H))


def build_masks(gltf: dict, blob: bytes) -> tuple[Image.Image, Image.Image, Image.Image, Image.Image]:
    full = Image.new("L", (W, H), 0)
    mat0 = Image.new("L", (W, H), 0)
    mat1 = Image.new("L", (W, H), 0)
    mesh0 = Image.new("L", (W, H), 0)

    for mesh_idx in range(len(gltf["meshes"])):
        prim = gltf["meshes"][mesh_idx]["primitives"][0]
        mat_id = prim.get("material", 0)
        uvs = accessor_data(gltf, blob, prim["attributes"]["TEXCOORD_0"])
        indices = accessor_data(gltf, blob, prim["indices"])
        fp = full.load()
        m0p = mat0.load()
        m1p = mat1.load()
        h0p = mesh0.load()

        for i in range(0, len(indices), 3):
            tri = indices[i : i + 3]
            pts = [uv_px(uvs[idx][0], uvs[idx][1]) for idx in tri]
            xs, ys = [p[0] for p in pts], [p[1] for p in pts]
            xmin, xmax = max(0, min(xs)), min(W - 1, max(xs))
            ymin, ymax = max(0, min(ys)), min(H - 1, max(ys))
            for y in range(ymin, ymax + 1):
                for x in range(xmin, xmax + 1):
                    if point_in_tri((x, y), pts[0], pts[1], pts[2]):
                        fp[x, y] = 255
                        if mat_id == 0:
                            m0p[x, y] = 255
                        else:
                            m1p[x, y] = 255
                        if mesh_idx == 0:
                            h0p[x, y] = 255

    return full, mat0, mat1, mesh0


def scale_fire_atlas(path: Path) -> Image.Image:
    img = Image.open(path).convert("RGBA")
    return img.resize((W, H), Image.Resampling.NEAREST)


def apply_mask(src: Image.Image, mask: Image.Image) -> Image.Image:
    out = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    sp, mp, op = src.load(), mask.load(), out.load()
    for y in range(H):
        for x in range(W):
            if mp[x, y] > 128:
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


def preview(img: Image.Image, mask: Image.Image, path: Path) -> None:
    prev = img.copy()
    draw = prev.load()
    mp = mask.load()
    for y in range(H):
        for x in range(W):
            if mp[x, y] > 128:
                r, g, b, a = draw[x, y]
                draw[x, y] = (min(255, r + 40), g, min(255, b + 40), a)
    prev.save(path)
    print(f"OK preview {path}")


def main() -> None:
    gltf = json.loads(GLTF.read_text(encoding="utf-8"))
    blob = decode_uri(gltf["buffers"][0]["uri"])
    _, mat0_mask, mat1_mask, mesh0_mask = build_masks(gltf, blob)

    fire = scale_fire_atlas(ASSETS / "apple_armor_from_fire_uv.png")
    layer1 = apply_mask(fire, mat0_mask)
    save(layer1, ["apple_chestplate.png", "apple_armor_texture.png"], "apple_layer_1.png")
    preview(layer1, mat0_mask, ASSETS / "uv_aligned_layer1_preview.png")

    # Leggings: fire atlas has mat1 islands in same sheet — mask material 1 only
    layer2 = apply_mask(fire, mat1_mask)
    if sum(1 for y in range(H) for x in range(W) if mat1_mask.load()[x, y] > 128) > 100:
        save(layer2, ["apple_layer_2.png"], "apple_layer_2.png")
        preview(layer2, mat1_mask, ASSETS / "uv_aligned_layer2_preview.png")
    else:
        leggings = ASSETS / "ai_apple_leggings_cool_atlas.png"
        if leggings.exists():
            l2 = apply_mask(scale_fire_atlas(leggings), mat1_mask)
            save(l2, ["apple_layer_2.png"], "apple_layer_2.png")

    # Iron helmet: mesh_0 only at px x512-1280 y0-512 (center-top, NOT left corner)
    iron_candidates = [
        ASSETS / "c__Users_hp_AppData_Roaming_Cursor_User_workspaceStorage_1da29c5ffe78f49bf665a4a0e78eabc5_images_image-418de701-8d54-4e35-9016-7c6c04752a95.png",
        ASSETS / "ai_iron_helmet_hd.png",
    ]
    for iron_src in iron_candidates:
        if iron_src.exists():
            iron_hd = scale_fire_atlas(iron_src)
            iron = apply_mask(iron_hd, mesh0_mask)
            save(iron, ["iron_apple_helmet.png"], "iron_apple_layer_1.png")
            preview(iron, mesh0_mask, ASSETS / "uv_aligned_iron_helmet_preview.png")
            break


if __name__ == "__main__":
    main()
