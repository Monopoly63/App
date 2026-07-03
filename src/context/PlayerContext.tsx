import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
} from "react";
import type { ReactNode } from "react";
import { AccentPalette, DEFAULT_ACCENT, RepeatMode, Track } from "../types";
import { parseTrackFile } from "../utils/metadata";
import { extractAccent } from "../utils/color";

const FAVORITES_KEY = "silent-luxury.favorites.v1";
const FOLDER_FILTERS_KEY = "silent-luxury.folderFilters.v1";

function loadJSON<T>(key: string, fallback: T): T {
  try {
    const raw = localStorage.getItem(key);
    return raw ? (JSON.parse(raw) as T) : fallback;
  } catch {
    return fallback;
  }
}

function trackId(relativePath: string, size: number) {
  return `${relativePath}::${size}`;
}

interface PlayerContextValue {
  tracks: Track[];
  filteredTracks: Track[];
  favorites: string[];
  folderFilters: Record<string, boolean>;
  isLibraryLoading: boolean;
  importProgress: { done: number; total: number } | null;
  currentTrack: Track | null;
  currentAccent: AccentPalette;
  isPlaying: boolean;
  currentTime: number;
  duration: number;
  shuffle: boolean;
  repeatMode: RepeatMode;
  nowPlayingOpen: boolean;
  addFiles: (files: FileList | File[]) => Promise<void>;
  toggleFavorite: (id: string) => void;
  isFavorite: (id: string) => boolean;
  setFolderIncluded: (path: string, included: boolean) => void;
  isFolderIncluded: (path: string) => boolean;
  playTrack: (id: string) => void;
  togglePlay: () => void;
  next: () => void;
  prev: () => void;
  seek: (time: number) => void;
  setShuffle: (v: boolean) => void;
  cycleRepeat: () => void;
  openNowPlaying: () => void;
  closeNowPlaying: () => void;
  clearLibrary: () => void;
}

const PlayerContext = createContext<PlayerContextValue | null>(null);

export function PlayerProvider({ children }: { children: ReactNode }) {
  const [tracks, setTracks] = useState<Track[]>([]);
  const [favorites, setFavorites] = useState<string[]>(() => loadJSON(FAVORITES_KEY, [] as string[]));
  const [folderFilters, setFolderFilters] = useState<Record<string, boolean>>(() =>
    loadJSON(FOLDER_FILTERS_KEY, {} as Record<string, boolean>)
  );
  const [isLibraryLoading, setIsLibraryLoading] = useState(false);
  const [importProgress, setImportProgress] = useState<{ done: number; total: number } | null>(null);

  const [currentTrackId, setCurrentTrackId] = useState<string | null>(null);
  const [isPlaying, setIsPlaying] = useState(false);
  const [currentTime, setCurrentTime] = useState(0);
  const [duration, setDuration] = useState(0);
  const [shuffle, setShuffle] = useState(false);
  const [repeatMode, setRepeatMode] = useState<RepeatMode>("off");
  const [nowPlayingOpen, setNowPlayingOpen] = useState(false);
  const [accentCache, setAccentCache] = useState<Record<string, AccentPalette>>({});

  const audioRef = useRef<HTMLAudioElement | null>(null);
  if (!audioRef.current && typeof Audio !== "undefined") {
    audioRef.current = new Audio();
  }

  useEffect(() => {
    localStorage.setItem(FAVORITES_KEY, JSON.stringify(favorites));
  }, [favorites]);

  useEffect(() => {
    localStorage.setItem(FOLDER_FILTERS_KEY, JSON.stringify(folderFilters));
  }, [folderFilters]);

  const filteredTracks = useMemo(
    () => tracks.filter((t) => folderFilters[t.folderPath] !== false),
    [tracks, folderFilters]
  );

  const currentTrack = useMemo(
    () => tracks.find((t) => t.id === currentTrackId) ?? null,
    [tracks, currentTrackId]
  );

  // Lazily extract a desaturated accent palette for the active track only.
  useEffect(() => {
    if (!currentTrack) return;
    if (accentCache[currentTrack.id]) return;
    let cancelled = false;
    extractAccent(currentTrack.coverUrl).then((accent) => {
      if (!cancelled) {
        setAccentCache((prev) => ({ ...prev, [currentTrack.id]: accent }));
      }
    });
    return () => {
      cancelled = true;
    };
  }, [currentTrack, accentCache]);

  const currentAccent = currentTrack ? accentCache[currentTrack.id] ?? DEFAULT_ACCENT : DEFAULT_ACCENT;

  // Wire up the underlying <audio> element.
  useEffect(() => {
    const audio = audioRef.current;
    if (!audio || !currentTrack) return;
    if (audio.src !== currentTrack.url) {
      audio.src = currentTrack.url;
      audio.currentTime = 0;
    }
    if (isPlaying) {
      audio.play().catch(() => setIsPlaying(false));
    } else {
      audio.pause();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [currentTrack?.id]);

  useEffect(() => {
    const audio = audioRef.current;
    if (!audio) return;
    if (isPlaying) {
      audio.play().catch(() => setIsPlaying(false));
    } else {
      audio.pause();
    }
  }, [isPlaying]);

  const advance = useCallback(
    (direction: 1 | -1) => {
      if (filteredTracks.length === 0) return;
      const idx = filteredTracks.findIndex((t) => t.id === currentTrackId);
      if (shuffle) {
        const randIdx = Math.floor(Math.random() * filteredTracks.length);
        setCurrentTrackId(filteredTracks[randIdx].id);
        return;
      }
      const base = idx === -1 ? 0 : idx;
      const nextIdx = (base + direction + filteredTracks.length) % filteredTracks.length;
      setCurrentTrackId(filteredTracks[nextIdx].id);
    },
    [filteredTracks, currentTrackId, shuffle]
  );

  useEffect(() => {
    const audio = audioRef.current;
    if (!audio) return;

    const onTime = () => setCurrentTime(audio.currentTime);
    const onLoaded = () => setDuration(audio.duration || 0);
    const onEnded = () => {
      if (repeatMode === "one") {
        audio.currentTime = 0;
        audio.play().catch(() => {});
        return;
      }
      if (repeatMode === "off" && !shuffle) {
        const idx = filteredTracks.findIndex((t) => t.id === currentTrackId);
        if (idx === filteredTracks.length - 1) {
          setIsPlaying(false);
          return;
        }
      }
      advance(1);
    };

    audio.addEventListener("timeupdate", onTime);
    audio.addEventListener("loadedmetadata", onLoaded);
    audio.addEventListener("ended", onEnded);
    return () => {
      audio.removeEventListener("timeupdate", onTime);
      audio.removeEventListener("loadedmetadata", onLoaded);
      audio.removeEventListener("ended", onEnded);
    };
  }, [repeatMode, shuffle, advance, filteredTracks, currentTrackId]);

  const addFiles = useCallback(async (fileList: FileList | File[]) => {
    const files = Array.from(fileList).filter((f) => f.type.startsWith("audio") || /\.(mp3|m4a|flac|wav|aac|ogg|opus)$/i.test(f.name));
    if (files.length === 0) return;
    setIsLibraryLoading(true);
    setImportProgress({ done: 0, total: files.length });

    const newTracks: Track[] = [];
    for (let i = 0; i < files.length; i++) {
      const file = files[i];
      const relativePath = (file as File & { webkitRelativePath?: string }).webkitRelativePath || file.name;
      const segments = relativePath.split("/");
      const folderPath = segments.length > 1 ? segments.slice(0, -1).join("/") : "Imported Files";
      const id = trackId(relativePath, file.size);

      try {
        const meta = await parseTrackFile(file);
        newTracks.push({
          id,
          file,
          url: URL.createObjectURL(file),
          title: meta.title,
          artist: meta.artist,
          album: meta.album,
          duration: meta.duration,
          coverUrl: meta.coverUrl,
          folderPath,
          relativePath,
          accent: DEFAULT_ACCENT,
        });
      } catch {
        // skip unreadable file
      }
      setImportProgress({ done: i + 1, total: files.length });
    }

    setTracks((prev) => {
      const existingIds = new Set(prev.map((t) => t.id));
      const merged = [...prev, ...newTracks.filter((t) => !existingIds.has(t.id))];
      merged.sort((a, b) => a.title.localeCompare(b.title));
      return merged;
    });
    setIsLibraryLoading(false);
    setImportProgress(null);
  }, []);

  const toggleFavorite = useCallback((id: string) => {
    setFavorites((prev) => (prev.includes(id) ? prev.filter((f) => f !== id) : [...prev, id]));
  }, []);

  const isFavorite = useCallback((id: string) => favorites.includes(id), [favorites]);

  const setFolderIncluded = useCallback((path: string, included: boolean) => {
    setFolderFilters((prev) => ({ ...prev, [path]: included }));
  }, []);

  const isFolderIncluded = useCallback(
    (path: string) => folderFilters[path] !== false,
    [folderFilters]
  );

  const playTrack = useCallback((id: string) => {
    setCurrentTrackId(id);
    setIsPlaying(true);
  }, []);

  const togglePlay = useCallback(() => {
    if (!currentTrackId && filteredTracks.length > 0) {
      setCurrentTrackId(filteredTracks[0].id);
      setIsPlaying(true);
      return;
    }
    setIsPlaying((p) => !p);
  }, [currentTrackId, filteredTracks]);

  const next = useCallback(() => advance(1), [advance]);
  const prev = useCallback(() => {
    const audio = audioRef.current;
    if (audio && audio.currentTime > 3) {
      audio.currentTime = 0;
      return;
    }
    advance(-1);
  }, [advance]);

  const seek = useCallback((time: number) => {
    const audio = audioRef.current;
    if (audio) {
      audio.currentTime = time;
      setCurrentTime(time);
    }
  }, []);

  const cycleRepeat = useCallback(() => {
    setRepeatMode((prev) => (prev === "off" ? "all" : prev === "all" ? "one" : "off"));
  }, []);

  const clearLibrary = useCallback(() => {
    tracks.forEach((t) => {
      URL.revokeObjectURL(t.url);
      if (t.coverUrl) URL.revokeObjectURL(t.coverUrl);
    });
    setTracks([]);
    setCurrentTrackId(null);
    setIsPlaying(false);
  }, [tracks]);

  const value: PlayerContextValue = {
    tracks,
    filteredTracks,
    favorites,
    folderFilters,
    isLibraryLoading,
    importProgress,
    currentTrack,
    currentAccent,
    isPlaying,
    currentTime,
    duration,
    shuffle,
    repeatMode,
    nowPlayingOpen,
    addFiles,
    toggleFavorite,
    isFavorite,
    setFolderIncluded,
    isFolderIncluded,
    playTrack,
    togglePlay,
    next,
    prev,
    seek,
    setShuffle,
    cycleRepeat,
    openNowPlaying: () => setNowPlayingOpen(true),
    closeNowPlaying: () => setNowPlayingOpen(false),
    clearLibrary,
  };

  return <PlayerContext.Provider value={value}>{children}</PlayerContext.Provider>;
}

export function usePlayer() {
  const ctx = useContext(PlayerContext);
  if (!ctx) throw new Error("usePlayer must be used within PlayerProvider");
  return ctx;
}
