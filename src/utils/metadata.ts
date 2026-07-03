import jsmediatags from "jsmediatags/dist/jsmediatags.min.js";
import { fileStem } from "./format";

export interface ParsedMeta {
  title: string;
  artist: string;
  album: string;
  coverUrl: string | null;
}

function readAudioDuration(file: File): Promise<number> {
  return new Promise((resolve) => {
    const audio = document.createElement("audio");
    audio.preload = "metadata";
    const url = URL.createObjectURL(file);
    audio.src = url;
    const cleanup = () => URL.revokeObjectURL(url);
    audio.onloadedmetadata = () => {
      resolve(Number.isFinite(audio.duration) ? audio.duration : 0);
      cleanup();
    };
    audio.onerror = () => {
      resolve(0);
      cleanup();
    };
  });
}

function readTags(file: File): Promise<ParsedMeta> {
  return new Promise((resolve) => {
    try {
      jsmediatags.read(file, {
        onSuccess: (result) => {
          const tags = result.tags;
          let coverUrl: string | null = null;
          if (tags.picture) {
            try {
              const { data, format } = tags.picture;
              const bytes = new Uint8Array(data);
              const blob = new Blob([bytes], { type: format });
              coverUrl = URL.createObjectURL(blob);
            } catch {
              coverUrl = null;
            }
          }
          resolve({
            title: (tags.title && String(tags.title).trim()) || fileStem(file.name),
            artist: (tags.artist && String(tags.artist).trim()) || "Unknown Artist",
            album: (tags.album && String(tags.album).trim()) || "Unknown Album",
            coverUrl,
          });
        },
        onError: () => {
          resolve({
            title: fileStem(file.name),
            artist: "Unknown Artist",
            album: "Unknown Album",
            coverUrl: null,
          });
        },
      });
    } catch {
      resolve({
        title: fileStem(file.name),
        artist: "Unknown Artist",
        album: "Unknown Album",
        coverUrl: null,
      });
    }
  });
}

export async function parseTrackFile(file: File): Promise<ParsedMeta & { duration: number }> {
  const [meta, duration] = await Promise.all([readTags(file), readAudioDuration(file)]);
  return { ...meta, duration };
}
