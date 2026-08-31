"""Import blockbench/model.gltf cubes into AppleArmorModelLayers.java."""
from __future__ import annotations

import base64
import json
import struct
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
GLTF = ROOT / "blockbench" / "model.gltf"
TARGET = ROOT / "src/main/java/com/customapples/client/AppleArmorModelLayers.java"
MARKER = "public static LayerDefinition appleArmorSet()"
END_MARKER = "\n    public static LayerDefinition appleChestplate()"

TEX_W, TEX_H = 64, 32
SCALE = 16.0


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


def mesh_bounds(gltf: dict, blob: bytes, mesh_idx: int) -> tuple[list, list, tuple[int, int]]:
    prim = gltf["meshes"][mesh_idx]["primitives"][0]
    pos = accessor_data(gltf, blob, prim["attributes"]["POSITION"])
    xs = [p[0] for p in pos]
    ys = [p[1] for p in pos]
    zs = [p[2] for p in pos]
    pos_min = [min(xs), min(ys), min(zs)]
    pos_max = [max(xs), max(ys), max(zs)]

    uvs = accessor_data(gltf, blob, prim["attributes"]["TEXCOORD_0"])
    us = [u[0] for u in uvs]
    vs = [u[1] for u in uvs]
    u0, v0 = min(us), min(vs)
    tex_u = int(u0 * TEX_W)
    tex_v = int(v0 * TEX_H)
    mat = prim.get("material", 0)
    return pos_min, pos_max, (tex_u, tex_v, mat)


def build_java(gltf: dict, blob: bytes) -> str:
    nodes = gltf["nodes"]
    root_idx = next(i for i, n in enumerate(nodes) if n.get("name") == "root")
    root_t = nodes[root_idx].get("translation", [0, 0, 0])
    rx, ry, rz = [c * SCALE for c in root_t]
    children = nodes[root_idx].get("children", [])

    lines = [
        "    public static LayerDefinition appleArmorSet() {",
        "        MeshDefinition mesh = new MeshDefinition();",
        "        PartDefinition root = mesh.getRoot();",
        f"        PartDefinition armor = root.addOrReplaceChild(\"root\", CubeListBuilder.create(),"
        f" PartPose.offset({rx}F, {ry}F, {rz}F));",
        "        PartDefinition mat0 = armor.addOrReplaceChild(\"mat0\", CubeListBuilder.create(), PartPose.ZERO);",
        "        PartDefinition mat1 = armor.addOrReplaceChild(\"mat1\", CubeListBuilder.create(), PartPose.ZERO);",
    ]

    for child_idx in children:
        node = nodes[child_idx]
        if "mesh" not in node:
            continue
        mesh_idx = node["mesh"]
        pos_min, pos_max, (tu, tv, mat) = mesh_bounds(gltf, blob, mesh_idx)
        t = node.get("translation", [0, 0, 0])
        ox, oy, oz = t[0] * SCALE, t[1] * SCALE, t[2] * SCALE
        sx = (pos_max[0] - pos_min[0]) * SCALE
        sy = (pos_max[1] - pos_min[1]) * SCALE
        sz = (pos_max[2] - pos_min[2]) * SCALE
        bx = pos_min[0] * SCALE
        by = pos_min[1] * SCALE
        bz = pos_min[2] * SCALE
        parent = "mat1" if mat == 1 else "mat0"
        name = f"mesh_{mesh_idx}"
        lines.extend([
            f"        {parent}.addOrReplaceChild(",
            f"                \"{name}\",",
            f"                CubeListBuilder.create().texOffs({tu}, {tv})"
            f".addBox({bx}F, {by}F, {bz}F, {sx}F, {sy}F, {sz}F),",
            f"                PartPose.offset({ox}F, {oy}F, {oz}F));",
        ])

    lines.extend([
        f"        return LayerDefinition.create(mesh, {TEX_W}, {TEX_H});",
        "    }",
    ])
    return "\n".join(lines), mesh_bounds(gltf, blob, 0)


def build_iron_helmet_java(gltf: dict, blob: bytes, mesh0: tuple) -> str:
    pos_min, pos_max, (tu, tv, _) = mesh0
    sx = (pos_max[0] - pos_min[0]) * SCALE
    sy = (pos_max[1] - pos_min[1]) * SCALE
    sz = (pos_max[2] - pos_min[2]) * SCALE
    bx, by, bz = pos_min[0] * SCALE, pos_min[1] * SCALE, pos_min[2] * SCALE
    return "\n".join([
        "    public static LayerDefinition ironAppleHelmet() {",
        "        MeshDefinition mesh = new MeshDefinition();",
        "        PartDefinition root = mesh.getRoot();",
        "        root.addOrReplaceChild(",
        "                \"piece\",",
        f"                CubeListBuilder.create().texOffs({tu}, {tv})"
        f".addBox({bx}F, {by}F, {bz}F, {sx}F, {sy}F, {sz}F),",
        "                PartPose.ZERO);",
        f"        return LayerDefinition.create(mesh, {TEX_W}, {TEX_H});",
        "    }",
    ])


def main() -> None:
    if not GLTF.exists():
        raise SystemExit(f"Missing {GLTF}")
    gltf = json.loads(GLTF.read_text(encoding="utf-8"))
    blob = decode_uri(gltf["buffers"][0]["uri"])
    java, mesh0 = build_java(gltf, blob)
    iron_java = build_iron_helmet_java(gltf, blob, mesh0)
    text = TARGET.read_text(encoding="utf-8")
    start = text.index(MARKER)
    end = text.index(END_MARKER, start)
    text = text[:start] + java + text[end:]
    iron_start = text.index("public static LayerDefinition ironAppleHelmet()")
    iron_end = text.index("    public static LayerDefinition appleArmorSet()", iron_start)
    text = text[:iron_start] + iron_java + text[iron_end:]
    TARGET.write_text(text, encoding="utf-8")
    print(f"OK imported GLTF from {GLTF}")


if __name__ == "__main__":
    main()
