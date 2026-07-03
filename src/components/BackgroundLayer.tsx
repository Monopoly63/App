import { AnimatePresence, motion } from "framer-motion";
import { usePlayer } from "../context/PlayerContext";

export default function BackgroundLayer() {
  const { currentTrack, currentAccent } = usePlayer();

  return (
    <div
      className="absolute inset-0 overflow-hidden bg-[#0a0a0c]"
      style={
        {
          "--accent-primary": currentAccent.primary,
          "--accent-soft": currentAccent.soft,
          "--accent-glow": currentAccent.glow,
        } as React.CSSProperties
      }
    >
      {/* Base tonal gradient, always present for the "sophisticated default" */}
      <div
        className="absolute inset-0 transition-colors duration-[1400ms]"
        style={{
          background: `radial-gradient(120% 90% at 15% 0%, hsl(var(--accent-glow) / 0.35), transparent 60%),
                       radial-gradient(120% 100% at 90% 100%, hsl(var(--accent-primary) / 0.28), transparent 65%),
                       linear-gradient(180deg, #0b0b0d 0%, #08080a 60%, #050506 100%)`,
        }}
      />

      <AnimatePresence mode="sync">
        {currentTrack?.coverUrl && (
          <motion.div
            key={currentTrack.id}
            initial={{ opacity: 0 }}
            animate={{ opacity: 0.55 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 0.32, ease: "easeOut" }}
            className="absolute -inset-10 will-change-opacity"
          >
            <img
              src={currentTrack.coverUrl}
              alt=""
              className="h-full w-full object-cover blur-[34px] saturate-[0.6] contrast-[0.9] will-change-transform [transform:translate3d(0,0,0)_scale(1.06)]"
            />
          </motion.div>
        )}
      </AnimatePresence>

      {/* Frosted vignette so foreground glass content stays legible */}
      <div className="absolute inset-0 bg-black/35" />
      <div className="absolute inset-0 bg-gradient-to-b from-black/40 via-transparent to-black/60" />
      <div className="pointer-events-none absolute inset-0 opacity-[0.05] mix-blend-overlay [background-image:radial-gradient(rgba(255,255,255,0.6)_1px,transparent_1px)] [background-size:3px_3px]" />
    </div>
  );
}
