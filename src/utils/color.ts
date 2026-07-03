import { getColor } from "colorthief";
import { AccentPalette, DEFAULT_ACCENT } from "../types";

/**
 * Extracts a dominant color from album art, then heavily desaturates and
 * clamps contrast so the result stays elegant and "quiet" instead of loud —
 * the "Silent Luxury" rule: tone, never shout.
 */
export async function extractAccent(imageUrl: string | null): Promise<AccentPalette> {
  if (!imageUrl) return DEFAULT_ACCENT;

  try {
    const img = new Image();
    img.crossOrigin = "anonymous";
    const loaded = new Promise<void>((resolve, reject) => {
      img.onload = () => resolve();
      img.onerror = () => reject(new Error("image load failed"));
    });
    img.src = imageUrl;
    await loaded;

    const color = await getColor(img, { colorCount: 6, quality: 4 });
    if (!color) return DEFAULT_ACCENT;

    const { h, s, l } = color.hsl();

    // Heavy desaturation + lightness clamping for a premium, muted feel.
    const primaryS = Math.min(Math.max(s * 0.32, 8), 26);
    const primaryL = Math.min(Math.max(l, 32), 48);
    const glowS = Math.min(primaryS + 12, 34);
    const glowL = Math.min(primaryL + 24, 68);

    return {
      primary: `${Math.round(h)} ${Math.round(primaryS)}% ${Math.round(primaryL)}%`,
      soft: `${Math.round(h)} ${Math.round(primaryS * 0.7)}% ${Math.round(primaryL)}%`,
      glow: `${Math.round(h)} ${Math.round(glowS)}% ${Math.round(glowL)}%`,
    };
  } catch {
    return DEFAULT_ACCENT;
  }
}
