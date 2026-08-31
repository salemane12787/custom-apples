"""Copy the raw photoscan GLB into game assets — mesh/UV/texture unchanged."""
from __future__ import annotations

import json
import shutil
from pathlib import Path

import trimesh
from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
GLB = Path(r"c:\Users\hp\Downloads\Raw photoscan of an Apple .glb")
OUT_MESH = ROOT / "src/main/resources/assets/customapples/models/item/real_apple_mesh.json"
OUT_TEX = ROOT / "src/main/resources/assets/customapples/textures/item/real_apple_scan.png"
OUT_ICON = ROOT / "src/main/resources/assets/customapples/textures/item/real_apple.png"
REF_GLB = ROOT / "blockbench/real_apple.glb"
ICON_SIZE = 16


def make_icon(texture: Image.Image) -> Image.Image:
    w, h = texture.size
    side = min(w, h)
    left = (w - side) // 2
    top = (h - side) // 2
    crop = texture.crop((left, top, left + side, top + side))
    return crop.resize((ICON_SIZE, ICON_SIZE), Image.Resampling.LANCZOS)


def main() -> None:
    if not GLB.exists():
        raise SystemExit(f"Missing GLB: {GLB}")

    REF_GLB.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(GLB, REF_GLB)

    mesh = trimesh.util.concatenate(list(trimesh.load(GLB).geometry.values()))
    if mesh.visual.uv is None:
        raise SystemExit("GLB has no UVs")

    tex = mesh.visual.material.baseColorTexture
    if tex is None:
        raise SystemExit("GLB has no embedded texture")

    OUT_TEX.parent.mkdir(parents=True, exist_ok=True)
    tex.save(OUT_TEX)
    make_icon(tex).save(OUT_ICON)

    extent = float(max(mesh.extents))
    payload = {
        "positions": mesh.vertices.astype(float).reshape(-1).tolist(),
        "normals": mesh.vertex_normals.astype(float).reshape(-1).tolist(),
        "uvs": mesh.visual.uv.astype(float).reshape(-1).tolist(),
        "indices": mesh.faces.astype(int).reshape(-1).tolist(),
        "extent": extent,
    }
    OUT_MESH.write_text(json.dumps(payload), encoding="utf-8")

    print(f"GLB copy -> {REF_GLB}")
    print(f"Texture -> {OUT_TEX} ({tex.size[0]}x{tex.size[1]})")
    print(f"Mesh -> {OUT_MESH} ({len(mesh.faces)} faces, extent {extent:.4f})")
    print("Done. Restart the game client.")


if __name__ == "__main__":
    main()
