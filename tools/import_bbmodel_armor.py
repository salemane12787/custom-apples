"""
Import a Blockbench .bbmodel into AppleArmorModelLayers.appleArmorSet() Java code.

Save your Blockbench project as:
  blockbench/apple_armor.bbmodel

Then run:
  python tools/import_bbmodel_armor.py
"""
from __future__ import annotations

import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
BBMODEL = ROOT / "blockbench" / "apple_armor.bbmodel"
TARGET = ROOT / "src/main/java/com/customapples/client/AppleArmorModelLayers.java"
MARKER = "public static LayerDefinition appleArmorSet()"


def sanitize(name: str) -> str:
    name = re.sub(r"[^a-zA-Z0-9_]", "_", name)
    if not name or name[0].isdigit():
        name = "cube_" + name
    return name


def face_uv(el: dict) -> tuple[int, int]:
    faces = el.get("faces") or {}
    for data in faces.values():
        uv = data.get("uv")
        if uv and len(uv) == 4:
            return int(min(uv[0], uv[2])), int(min(uv[1], uv[3]))
    return 0, 0


def element_box(el: dict) -> tuple[float, float, float, float, float, float]:
    from_ = el["from"]
    to_ = el["to"]
    ox, oy, oz = el.get("origin", [0, 0, 0])
    x = from_[0] - ox
    y = from_[1] - oy
    z = from_[2] - oz
    sx = to_[0] - from_[0]
    sy = to_[1] - from_[1]
    sz = to_[2] - from_[2]
    return x, y, z, sx, sy, sz


def build_java(elements: list[dict], tex_w: int, tex_h: int) -> str:
    lines = [
        "    public static LayerDefinition appleArmorSet() {",
        "        MeshDefinition mesh = new MeshDefinition();",
        "        PartDefinition root = mesh.getRoot();",
        "        PartDefinition armor = root.addOrReplaceChild(",
        "                \"root\",",
        "                CubeListBuilder.create(),",
        "                PartPose.ZERO);",
    ]
    for el in elements:
        name = sanitize(el.get("name", "cube"))
        u, v = face_uv(el)
        x, y, z, sx, sy, sz = element_box(el)
        ox, oy, oz = el.get("origin", [0, 0, 0])
        lines.append(f"        armor.addOrReplaceChild(")
        lines.append(f"                \"{name}\",")
        lines.append(
            f"                CubeListBuilder.create().texOffs({u}, {v})"
            f".addBox({x}F, {y}F, {z}F, {sx}F, {sy}F, {sz}F),"
        )
        lines.append(f"                PartPose.offset({ox}F, {oy}F, {oz}F));")
    lines.extend([
        f"        return LayerDefinition.create(mesh, {tex_w}, {tex_h});",
        "    }",
    ])
    return "\n".join(lines)


def main() -> None:
    if not BBMODEL.exists():
        raise SystemExit(f"Save Blockbench project to {BBMODEL}")

    data = json.loads(BBMODEL.read_text(encoding="utf-8"))
    res = data.get("resolution", {})
    tex_w = int(res.get("width", 1024))
    tex_h = int(res.get("height", 512))
    elements = data.get("elements", [])
    if not elements:
        raise SystemExit("No elements in bbmodel")

    java_method = build_java(elements, tex_w, tex_h)
    text = TARGET.read_text(encoding="utf-8")
    start = text.index(MARKER)
    end = text.index("\n    public static LayerDefinition appleChestplate()", start)
    new_text = text[:start] + java_method + text[end:]
    TARGET.write_text(new_text, encoding="utf-8")
    print(f"OK imported {len(elements)} cubes from {BBMODEL}")


if __name__ == "__main__":
    main()
