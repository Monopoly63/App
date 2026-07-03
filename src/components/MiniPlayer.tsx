import { motion } from "framer-motion";
import { Music2, Pause, Play, SkipForward } from "lucide-react";
import { usePlayer } from "../context/PlayerContext";

export default function MiniPlayer() {
  const { currentTrack, isPlaying, togglePlay, next, openNowPlaying, currentTime, duration } =
    usePlayer();

  if (!currentTrack) return null;

  const progress = duration > 0 ? (currentTime / duration) * 100 : 0;

  return (
    <motion.div
      layoutId="player-surface"
      onClick={openNowPlaying}
      initial={{ opacity: 0, y: 16 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, y: 16 }}
      transition={{ type: "spring", stiffness: 300, damping: 30 }}
      className="glass-panel relative mx-4 mb-3 flex cursor-pointer items-center gap-3 overflow-hidden rounded-2xl px-2.5 py-2.5"
    >
      <div className="absolute bottom-0 left-0 h-[2px] bg-white/60" style={{ width: `${progress}%` }} />

      <motion.div layoutId="player-art" className="h-10 w-10 shrink-0 overflow-hidden rounded-xl bg-white/10">
        {currentTrack.coverUrl ? (
          <img src={currentTrack.coverUrl} alt="" className="h-full w-full object-cover" />
        ) : (
          <div className="flex h-full w-full items-center justify-center text-white/40">
            <Music2 size={16} />
          </div>
        )}
      </motion.div>

      <div className="min-w-0 flex-1">
        <p className="truncate text-[12.5px] font-medium text-white">{currentTrack.title}</p>
        <p className="truncate text-[10.5px] text-white/45">{currentTrack.artist}</p>
      </div>

      <button
        onClick={(e) => {
          e.stopPropagation();
          togglePlay();
        }}
        className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-white text-black transition-transform active:scale-90"
      >
        {isPlaying ? <Pause size={14} fill="currentColor" /> : <Play size={14} fill="currentColor" />}
      </button>
      <button
        onClick={(e) => {
          e.stopPropagation();
          next();
        }}
        className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full text-white/70 transition-transform active:scale-90"
      >
        <SkipForward size={15} fill="currentColor" />
      </button>
    </motion.div>
  );
}
