import { motion } from "framer-motion";
import { Heart, Music2, Pause, Play } from "lucide-react";
import { Track } from "../types";
import { usePlayer } from "../context/PlayerContext";
import { formatTime } from "../utils/format";
import { cn } from "../utils/cn";

export default function SongRow({ track, index }: { track: Track; index: number }) {
  const { currentTrack, isPlaying, playTrack, togglePlay, toggleFavorite, isFavorite } = usePlayer();
  const active = currentTrack?.id === track.id;
  const fav = isFavorite(track.id);

  const handleTap = () => {
    if (active) {
      togglePlay();
    } else {
      playTrack(track.id);
    }
  };

  return (
    <motion.div
      initial={{ opacity: 0, y: 10 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.35, delay: Math.min(index * 0.02, 0.3) }}
      whileTap={{ scale: 0.98 }}
      onClick={handleTap}
      className={cn(
        "group flex cursor-pointer items-center gap-3 rounded-2xl border border-transparent px-2.5 py-2 transition-colors",
        active ? "border-white/15 bg-white/10" : "hover:bg-white/5"
      )}
    >
      <div className="relative h-12 w-12 shrink-0 overflow-hidden rounded-xl bg-white/10 ring-1 ring-white/10">
        {track.coverUrl ? (
          <img src={track.coverUrl} alt="" className="h-full w-full object-cover" />
        ) : (
          <div className="flex h-full w-full items-center justify-center text-white/40">
            <Music2 size={18} />
          </div>
        )}
        {active && (
          <div className="absolute inset-0 flex items-center justify-center bg-black/40 text-white">
            {isPlaying ? <Pause size={16} /> : <Play size={16} />}
          </div>
        )}
      </div>

      <div className="min-w-0 flex-1">
        <p
          className={cn(
            "truncate text-[13.5px] font-medium",
            active ? "text-white" : "text-white/90"
          )}
        >
          {track.title}
        </p>
        <p className="truncate text-[11.5px] text-white/45">{track.artist}</p>
      </div>

      <span className="shrink-0 text-[11px] tabular-nums text-white/35">
        {formatTime(track.duration)}
      </span>

      <button
        onClick={(e) => {
          e.stopPropagation();
          toggleFavorite(track.id);
        }}
        className="shrink-0 rounded-full p-1.5 text-white/40 transition-colors hover:bg-white/10 hover:text-rose-300"
        aria-label="Toggle favorite"
      >
        <Heart size={15} className={cn(fav && "fill-rose-400 text-rose-400")} />
      </button>
    </motion.div>
  );
}
