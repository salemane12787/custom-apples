"""Export exact GLTF UV mask at 1024x512 (16px per atlas pixel) for AI texture painting."""
from __future__ import annotations

import base64
import json
import struct
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent.parent
GLTF = ROOT / "blockbench" / "model.gltf"
ASSETS = Path(r"C:\Users\hp\.cursor\projects\d-xnestorio-mode\assets")

# 32 texels per vanilla atlas pixel (64*32 = 2048, 32*32 = 1024)
W, H = 2048, 1024


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
        return int(round(u)), int(round(v))
    return int(round(u * W)), int(round((1.0 - v) * H))


def rasterize_mesh(gltf: dict, blob: bytes, mesh_idx: int, mask: Image.Image, mat_mask: Image.Image, mat_id: int):
    prim = gltf["meshes"][mesh_idx]["primitives"][0]
    uvs = accessor_data(gltf, blob, prim["attributes"]["TEXCOORD_0"])
    indices = accessor_data(gltf, blob, prim["indices"])
    mp, mmp = mask.load(), mat_mask.load()

    for i in range(0, len(indices), 3):
        tri = indices[i : i + 3]
        pts = [uv_px(uvs[idx][0], uvs[idx][1]) for idx in tri]
        xs, ys = [p[0] for p in pts], [p[1] for p in pts]
        xmin, xmax = max(0, min(xs)), min(W - 1, max(xs))
        ymin, ymax = max(0, min(ys)), min(H - 1, max(ys))
        for y in range(ymin, ymax + 1):
            for x in range(xmin, xmax + 1):
                if point_in_tri((x, y), pts[0], pts[1], pts[2]):
                    mp[x, y] = 255
                    mmp[x, y] = mat_id


def main() -> None:
    gltf = json.loads(GLTF.read_text(encoding="utf-8"))
    blob = decode_uri(gltf["buffers"][0]["uri"])

    mask = Image.new("L", (W, H), 0)
    mat_mask = Image.new("L", (W, H), 0)
    for i in range(len(gltf["meshes"])):
        rasterize_mesh(gltf, blob, i, mask, mat_mask, gltf["meshes"][i]["primitives"][0].get("material", 0))

    ASSETS.mkdir(parents=True, exist_ok=True)
    mask.save(ASSETS / "gltf_uv_mask_layer1.png")
    mat_mask.save(ASSETS / "gltf_uv_mat_mask.png")

    # Color guide for AI: white = paint here, colored by material
    guide = Image.new("RGB", (W, H), (0, 0, 0))
    gp, mp, mmp = guide.load(), mask.load(), mat_mask.load()
    for y in range(H):
        for x in range(W):
            if mp[x, y] < 128:
                continue
            if mmp[x, y] == 1:
                gp[x, y] = (0, 180, 255)  # leggings islands = cyan
            else:
                gp[x, y] = (255, 255, 255)  # layer1 = white

    guide.save(ASSETS / "gltf_uv_guide_for_ai.png")

    mat0 = Image.new("RGB", (W, H), (0, 0, 0))
    mat1 = Image.new("RGB", (W, H), (0, 0, 0))
    m0p, m1p = mat0.load(), mat1.load()
    for y in range(H):
        for x in range(W):
            if mp[x, y] < 128:
                continue
            if mmp[x, y] == 1:
                m1p[x, y] = (255, 255, 255)
            else:
                m0p[x, y] = (255, 255, 255)
    mat0.save(ASSETS / "gltf_uv_mat0_guide.png")
    mat1.save(ASSETS / "gltf_uv_mat1_guide.png")

    # Wireframe overlay
    wire = guide.copy()
    draw = ImageDraw.Draw(wire)
    px = mask.load()
    for y in range(H):
        for x in range(W):
            if px[x, y] < 128:
                continue
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                nx, ny = x + dx, y + dy
                if nx < 0 or ny < 0 or nx >= W or ny >= H or px[nx, ny] < 128:
                    draw.point((x, y), fill=(255, 0, 0))
    wire.save(ASSETS / "gltf_uv_wireframe_guide.png")
    print(f"OK UV mask {W}x{H} — {sum(1 for y in range(H) for x in range(W) if mp[x,y]>0)} pixels")


if __name__ == "__main__":
    main()
