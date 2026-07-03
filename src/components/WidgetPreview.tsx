import { AnimatePresence, motion } from "framer-motion";
import { Music2, Pause, Play, SkipForward, X } from "lucide-react";
import { usePlayer } from "../context/PlayerContext";

export default function WidgetPreview({ open, onClose }: { open: boolean; onClose: () => void }) {
  const { currentTrack, isPlaying } = usePlayer();

  const title = currentTrack?.title ?? "Nothing Playing";
  const artist = currentTrack?.artist ?? "Open the app to start";

  return (
    <AnimatePresence>
      {open && (
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          className="absolute inset-0 z-40 flex items-end justify-center bg-black/60 backdrop-blur-sm"
          onClick={onClose}
        >
          <motion.div
            initial={{ y: 40, opacity: 0 }}
            animate={{ y: 0, opacity: 1 }}
            exit={{ y: 40, opacity: 0 }}
            transition={{ type: "spring", stiffness: 300, damping: 30 }}
            onClick={(e) => e.stopPropagation()}
            className="glass-panel w-full rounded-t-[32px] px-5 pb-9 pt-4"
          >
            <div className="mx-auto mb-4 h-1.5 w-10 rounded-full bg-white/25" />
            <div className="mb-4 flex items-center justify-between">
              <div>
                <p className="text-[15px] font-semibold text-white">Home Screen Widget</p>
                <p className="text-[11.5px] text-white/40">Jetpack Glance · Glassmorphic style</p>
              </div>
              <button
                onClick={onClose}
                className="flex h-8 w-8 items-center justify-center rounded-full bg-white/10 text-white/70"
              >
                <X size={15} />
              </button>
            </div>

            {/* Wallpaper-like backdrop to mimic a real home screen */}
            <div
              className="relative overflow-hidden rounded-[28px] p-5"
              style={{
                background:
                  "radial-gradient(120% 100% at 20% 0%, hsl(var(--accent-glow) / 0.4), transparent 60%), linear-gradient(160deg, #1c1c22, #0a0a0d)",
              }}
            >
              {/* Medium 4x2 widget */}
              <div className="glass-panel flex items-center gap-3 rounded-[24px] p-3.5">
                <div className="h-14 w-14 shrink-0 overflow-hidden rounded-2xl bg-white/10 ring-1 ring-white/10">
                  {currentTrack?.coverUrl ? (
                    <img src={currentTrack.coverUrl} className="h-full w-full object-cover" alt="" />
                  ) : (
                    <div className="flex h-full w-full items-center justify-center text-white/40">
                      <Music2 size={20} />
                    </div>
                  )}
                </div>
                <div className="min-w-0 flex-1">
                  <p className="truncate text-[13px] font-semibold text-white">{title}</p>
                  <p className="truncate text-[11px] text-white/45">{artist}</p>
                  <div className="mt-1.5 h-[3px] w-full overflow-hidden rounded-full bg-white/10">
                    <div className="h-full w-2/5 rounded-full bg-white/60" />
                  </div>
                </div>
                <div className="flex shrink-0 items-center gap-1.5">
                  <button className="flex h-8 w-8 items-center justify-center rounded-full bg-white/10 text-white">
                    {isPlaying ? <Pause size={13} fill="currentColor" /> : <Play size={13} fill="currentColor" />}
                  </button>
                  <button className="flex h-8 w-8 items-center justify-center rounded-full bg-white text-black">
                    <SkipForward size={13} fill="currentColor" />
                  </button>
                </div>
              </div>

              {/* Small square widget */}
              <div className="glass-panel mt-3 inline-flex w-[128px] flex-col items-center gap-2 rounded-[22px] p-3">
                <div className="h-16 w-16 overflow-hidden rounded-2xl bg-white/10 ring-1 ring-white/10">
                  {currentTrack?.coverUrl ? (
                    <img src={currentTrack.coverUrl} className="h-full w-full object-cover" alt="" />
                  ) : (
                    <div className="flex h-full w-full items-center justify-center text-white/40">
                      <Music2 size={18} />
                    </div>
                  )}
                </div>
                <div className="flex items-center gap-3 text-white">
                  <SkipForward size={14} className="rotate-180 opacity-70" />
                  <button className="flex h-8 w-8 items-center justify-center rounded-full bg-white text-black">
                    {isPlaying ? <Pause size={13} fill="currentColor" /> : <Play size={13} fill="currentColor" />}
                  </button>
                  <SkipForward size={14} className="opacity-70" />
                </div>
              </div>
            </div>

            <p className="mt-4 px-1 text-[11px] leading-relaxed text-white/40">
              Rounded to the system corner radius, frosted with real-time blur, and tinted with the
              same desaturated accent as the app. Buttons call the Media3 session directly — tapping
              the artwork or title is the only action that launches the app.
            </p>
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>
  );
}
