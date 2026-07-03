import { useRef } from "react";
import { LayoutGrid, Plus, FolderInput, ScanLine } from "lucide-react";
import { usePlayer } from "../context/PlayerContext";

const TAB_TITLES: Record<string, { title: string; sub: string }> = {
  all: { title: "Your Library", sub: "Every track, beautifully arranged" },
  folders: { title: "Folders", sub: "Curate exactly what belongs here" },
  favorites: { title: "Favorites", sub: "The ones you keep coming back to" },
};

export default function TopBar({
  tab,
  onOpenWidget,
}: {
  tab: string;
  onOpenWidget: () => void;
}) {
  const { addFiles, scanDeviceLibrary, nativeScanAvailable, isLibraryLoading, importProgress } = usePlayer();
  const filesInputRef = useRef<HTMLInputElement | null>(null);
  const folderInputRef = useRef<HTMLInputElement | null>(null);
  const meta = TAB_TITLES[tab] ?? TAB_TITLES.all;

  return (
    <div className="relative z-20 px-5 pb-3 pt-[calc(env(safe-area-inset-top)+14px)]">
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0">
          <p className="text-[10px] font-semibold uppercase tracking-[0.22em] text-white/40">
            Silent Luxury
          </p>
          <h1 className="mt-0.5 truncate text-[26px] font-semibold tracking-tight text-white">
            {meta.title}
          </h1>
          <p className="truncate text-[12px] text-white/40">{meta.sub}</p>
        </div>

        <div className="flex shrink-0 items-center gap-2 pt-1">
          <button
            onClick={onOpenWidget}
            className="glass-pill flex h-9 w-9 items-center justify-center text-white/80 transition-transform active:scale-90"
            aria-label="Widget preview"
          >
            <LayoutGrid size={16} />
          </button>
          {nativeScanAvailable && (
            <button
              onClick={() => void scanDeviceLibrary()}
              className="glass-pill flex h-9 items-center gap-1.5 rounded-full px-3.5 text-[12.5px] font-medium text-white transition-transform active:scale-95"
              aria-label="Scan device music"
            >
              <ScanLine size={14} />
              Scan
            </button>
          )}
          <button
            onClick={() => folderInputRef.current?.click()}
            className="glass-pill flex h-9 w-9 items-center justify-center text-white/80 transition-transform active:scale-90"
            aria-label="Add folder"
          >
            <FolderInput size={16} />
          </button>
          <button
            onClick={() => filesInputRef.current?.click()}
            className="glass-pill flex h-9 items-center gap-1.5 rounded-full px-3.5 text-[12.5px] font-medium text-white transition-transform active:scale-95"
          >
            <Plus size={14} />
            Files
          </button>
        </div>
      </div>

      {isLibraryLoading && importProgress && (
        <div className="mt-3 flex items-center gap-2 text-[11px] text-white/50">
          <div className="h-1 flex-1 overflow-hidden rounded-full bg-white/10">
            <div
              className="h-full rounded-full bg-white/70 transition-all duration-300"
              style={{ width: `${(importProgress.done / importProgress.total) * 100}%` }}
            />
          </div>
          <span className="tabular-nums">
            {importProgress.done}/{importProgress.total}
          </span>
        </div>
      )}

      <input
        ref={filesInputRef}
        type="file"
        multiple
        accept="audio/*,.mp3,.m4a,.flac,.wav,.aac,.ogg,.opus"
        className="hidden"
        onChange={(e) => {
          if (e.target.files) addFiles(e.target.files);
          e.target.value = "";
        }}
      />
      <input
        ref={folderInputRef}
        type="file"
        // @ts-expect-error non-standard attribute for directory selection
        webkitdirectory="true"
        directory="true"
        multiple
        className="hidden"
        onChange={(e) => {
          if (e.target.files) addFiles(e.target.files);
          e.target.value = "";
        }}
      />
    </div>
  );
}
