export interface Track {
  id: string;
  file: File;
  url: string;
  title: string;
  artist: string;
  album: string;
  duration: number;
  coverUrl: string | null;
  folderPath: string;
  relativePath: string;
  accent: AccentPalette;
}

export interface AccentPalette {
  primary: string; // desaturated hsl string, e.g. "220 18% 46%"
  soft: string; // very low alpha wash
  glow: string; // for glow highlights
}

export type RepeatMode = "off" | "all" | "one";

export interface FolderNode {
  name: string;
  path: string;
  children: Map<string, FolderNode>;
  trackCount: number;
  depth: number;
}

export const DEFAULT_ACCENT: AccentPalette = {
  primary: "230 14% 58%",
  soft: "230 14% 58%",
  glow: "230 30% 70%",
};
