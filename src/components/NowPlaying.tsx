import { motion } from "framer-motion";
import {
  ChevronDown,
  Heart,
  ListMusic,
  Music2,
  Pause,
  Play,
  Repeat,
  Repeat1,
  Shuffle,
  SkipBack,
  SkipForward,
} from "lucide-react";
import { usePlayer } from "../context/PlayerContext";
import { formatTime } from "../utils/format";
import { cn } from "../utils/cn";

export default function NowPlaying() {
  const {
    currentTrack,
    currentAccent,
    isPlaying,
    togglePlay,
    next,
    prev,
    currentTime,
    duration,
    seek,
    shuffle,
    setShuffle,
    repeatMode,
    cycleRepeat,
    toggleFavorite,
    isFavorite,
    closeNowPlaying,
    filteredTracks,
  } = usePlayer();

  if (!currentTrack) return null;
  const fav = isFavorite(currentTrack.id);
  const queueIndex = filteredTracks.findIndex((t) => t.id === currentTrack.id);

  return (
    <motion.div
      layoutId="player-surface"
      drag="y"
      dragConstraints={{ top: 0, bottom: 0 }}
      dragElastic={{ top: 0, bottom: 0.6 }}
      onDragEnd={(_, info) => {
        if (info.offset.y > 140) closeNowPlaying();
      }}
      transition={{ type: "spring", stiffness: 260, damping: 32 }}
      className="glass-panel absolute inset-0 z-30 flex flex-col overflow-hidden rounded-none px-6 pb-8 pt-[calc(env(safe-area-inset-top)+18px)]"
      style={
        {
          "--accent-primary": currentAccent.primary,
        } as React.CSSProperties
      }
    >
      <div className="mx-auto mb-2 h-1.5 w-10 shrink-0 rounded-full bg-white/25" />

      <div className="flex shrink-0 items-center justify-between">
        <button
          onClick={closeNowPlaying}
          className="flex h-9 w-9 items-center justify-center rounded-full text-white/70 transition-transform active:scale-90"
        >
          <ChevronDown size={20} />
        </button>
        <div className="text-center">
          <p className="text-[10px] font-semibold uppercase tracking-[0.2em] text-white/40">
            Now Playing
          </p>
          {queueIndex !== -1 && (
            <p className="text-[10px] text-white/25">
              {queueIndex + 1} of {filteredTracks.length}
            </p>
          )}
        </div>
        <button className="flex h-9 w-9 items-center justify-center rounded-full text-white/70">
          <ListMusic size={18} />
        </button>
      </div>

      <div className="flex flex-1 flex-col items-center justify-center gap-8 py-4">
        <motion.div
          layoutId="player-art"
          className="aspect-square w-full max-w-[280px] overflow-hidden rounded-[28px] shadow-[0_30px_60px_-15px_rgba(0,0,0,0.6)] ring-1 ring-white/15"
        >
          {currentTrack.coverUrl ? (
            <img src={currentTrack.coverUrl} alt="" className="h-full w-full object-cover" />
          ) : (
            <div
              className="flex h-full w-full items-center justify-center"
              style={{ background: `hsl(var(--accent-primary) / 0.5)` }}
            >
              <Music2 size={64} className="text-white/50" strokeWidth={1} />
            </div>
          )}
        </motion.div>

        <div className="w-full max-w-[320px] text-center">
          <div className="flex items-center justify-center gap-2">
            <h2 className="truncate text-[21px] font-semibold tracking-tight text-white">
              {currentTrack.title}
            </h2>
          </div>
          <p className="mt-1 truncate text-[13px] text-white/50">
            {currentTrack.artist} · {currentTrack.album}
          </p>
        </div>
      </div>

      <div className="w-full max-w-[340px] shrink-0 self-center">
        <input
          type="range"
          className="scrubber w-full"
          min={0}
          max={duration || 0}
          step={0.1}
          value={Math.min(currentTime, duration || 0)}
          onChange={(e) => seek(Number(e.target.value))}
        />
        <div className="mt-1 flex justify-between text-[10.5px] tabular-nums text-white/35">
          <span>{formatTime(currentTime)}</span>
          <span>{formatTime(duration)}</span>
        </div>

        <div className="mt-6 flex items-center justify-between">
          <button
            onClick={() => setShuffle(!shuffle)}
            className={cn(
              "flex h-9 w-9 items-center justify-center rounded-full transition-colors",
              shuffle ? "text-white" : "text-white/35"
            )}
          >
            <Shuffle size={16} />
          </button>

          <div className="flex items-center gap-5">
            <motion.button
              whileTap={{ scale: 0.85 }}
              onClick={prev}
              className="flex h-11 w-11 items-center justify-center text-white"
            >
              <SkipBack size={24} fill="currentColor" />
            </motion.button>

            <motion.button
              whileTap={{ scale: 0.88 }}
              onClick={togglePlay}
              className="flex h-16 w-16 items-center justify-center rounded-full bg-white text-black shadow-[0_10px_30px_rgba(255,255,255,0.15)]"
            >
              {isPlaying ? (
                <Pause size={26} fill="currentColor" />
              ) : (
                <Play size={26} fill="currentColor" className="ml-0.5" />
              )}
            </motion.button>

            <motion.button
              whileTap={{ scale: 0.85 }}
              onClick={next}
              className="flex h-11 w-11 items-center justify-center text-white"
            >
              <SkipForward size={24} fill="currentColor" />
            </motion.button>
          </div>

          <button
            onClick={cycleRepeat}
            className={cn(
              "flex h-9 w-9 items-center justify-center rounded-full transition-colors",
              repeatMode !== "off" ? "text-white" : "text-white/35"
            )}
          >
            {repeatMode === "one" ? <Repeat1 size={16} /> : <Repeat size={16} />}
          </button>
        </div>

        <div className="mt-6 flex justify-center">
          <button
            onClick={() => toggleFavorite(currentTrack.id)}
            className="glass-pill flex items-center gap-2 rounded-full px-5 py-2 text-[12px] font-medium text-white/80 transition-transform active:scale-95"
          >
            <Heart size={14} className={cn(fav && "fill-rose-400 text-rose-400")} />
            {fav ? "In Favorites" : "Add to Favorites"}
          </button>
        </div>
      </div>
    </motion.div>
  );
}
