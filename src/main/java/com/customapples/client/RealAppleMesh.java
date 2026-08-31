package com.customapples.client;

import com.customapples.CustomApplesMod;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public final class RealAppleMesh {
  public static final ResourceLocation TEXTURE =
      CustomApplesMod.loc("textures/item/real_apple_scan.png");

  private static RealAppleMesh instance;

  private final float[] positions;
  private final float[] normals;
  private final float[] uvs;
  private final int[] indices;
  private final float extent;

  private RealAppleMesh(float[] positions, float[] normals, float[] uvs, int[] indices, float extent) {
    this.positions = positions;
    this.normals = normals;
    this.uvs = uvs;
    this.indices = indices;
    this.extent = extent;
  }

  public float getExtent() {
    return extent;
  }

  public static RealAppleMesh get() {
    if (instance == null) {
      instance = load();
    }
    return instance;
  }

  public static void clearCache() {
    instance = null;
  }

  private static RealAppleMesh load() {
    ResourceLocation id = CustomApplesMod.loc("models/item/real_apple_mesh.json");
  try (var stream = Minecraft.getInstance()
          .getResourceManager()
          .getResource(id)
          .orElseThrow()
          .open()) {
      JsonObject root = JsonParser.parseReader(new java.io.InputStreamReader(stream)).getAsJsonObject();
      return new RealAppleMesh(
          jsonFloats(root.getAsJsonArray("positions")),
          jsonFloats(root.getAsJsonArray("normals")),
          jsonFloats(root.getAsJsonArray("uvs")),
          jsonInts(root.getAsJsonArray("indices")),
          root.has("extent") ? root.get("extent").getAsFloat() : 3.5F);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to load real apple mesh: " + id, e);
    }
  }

  private static float[] jsonFloats(JsonArray array) {
    float[] out = new float[array.size()];
    for (int i = 0; i < array.size(); i++) {
      out[i] = array.get(i).getAsFloat();
    }
    return out;
  }

  private static int[] jsonInts(JsonArray array) {
    int[] out = new int[array.size()];
    for (int i = 0; i < array.size(); i++) {
      out[i] = array.get(i).getAsInt();
    }
    return out;
  }

  public void render(PoseStack pose, VertexConsumer consumer, int light, int overlay) {
    Matrix4f matrix = pose.last().pose();
    Matrix3f normalMatrix = pose.last().normal();
    for (int i = 0; i < indices.length; i++) {
      int vi = indices[i];
      float x = positions[vi * 3];
      float y = positions[vi * 3 + 1];
      float z = positions[vi * 3 + 2];
      float nx = normals[vi * 3];
      float ny = normals[vi * 3 + 1];
      float nz = normals[vi * 3 + 2];
      float u = uvs[vi * 2];
      // Blender/glTF UVs use bottom-left origin; Minecraft PNGs use top-left — flip V only at draw time.
      float v = 1.0F - uvs[vi * 2 + 1];
      consumer
          .vertex(matrix, x, y, z)
          .color(255, 255, 255, 255)
          .uv(u, v)
          .overlayCoords(overlay)
          .uv2(light)
          .normal(normalMatrix, nx, ny, nz)
          .endVertex();
    }
  }

  public static RenderType renderType() {
    return RenderType.entitySolid(TEXTURE);
  }
}
