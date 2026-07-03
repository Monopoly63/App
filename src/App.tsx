import { useState } from "react";
import { AnimatePresence, motion } from "framer-motion";
import { Signal, Wifi, BatteryFull } from "lucide-react";
import { PlayerProvider, usePlayer } from "./context/PlayerContext";
import BackgroundLayer from "./components/BackgroundLayer";
import TopBar from "./components/TopBar";
import BottomNav, { TabKey } from "./components/BottomNav";
import MiniPlayer from "./components/MiniPlayer";
import NowPlaying from "./components/NowPlaying";
import WidgetPreview from "./components/WidgetPreview";
import AllSongsView from "./components/AllSongsView";
import FoldersView from "./components/FoldersView";
import FavoritesView from "./components/FavoritesView";

function StatusBar() {
  const [now] = useState(() =>
    new Date().toLocaleTimeString([], { hour: "numeric", minute: "2-digit" })
  );
  return (
    <div className="pointer-events-none relative z-30 flex items-center justify-between px-7 pt-3 text-[13px] font-medium text-white">
      <span>{now}</span>
      <div className="flex items-center gap-1.5">
        <Signal size={13} />
        <Wifi size={13} />
        <BatteryFull size={16} />
      </div>
    </div>
  );
}

function AppShell() {
  const [tab, setTab] = useState<TabKey>("all");
  const [widgetOpen, setWidgetOpen] = useState(false);
  const { currentAccent, currentTrack, nowPlayingOpen } = usePlayer();

  return (
    <div
      className="relative flex h-full w-full flex-col overflow-hidden text-white"
      style={
        {
          "--accent-primary": currentAccent.primary,
          "--accent-soft": currentAccent.soft,
          "--accent-glow": currentAccent.glow,
        } as React.CSSProperties
      }
    >
      <BackgroundLayer />
      <StatusBar />
      <TopBar tab={tab} onOpenWidget={() => setWidgetOpen(true)} />

      <div className="relative z-10 flex-1 overflow-y-auto no-scrollbar">
        <AnimatePresence mode="wait">
          <motion.div
            key={tab}
            initial={{ opacity: 0, y: 8 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -8 }}
            transition={{ duration: 0.22, ease: "easeOut" }}
            className="pb-[148px]"
          >
            {tab === "all" && <AllSongsView />}
            {tab === "folders" && <FoldersView />}
            {tab === "favorites" && <FavoritesView />}
          </motion.div>
        </AnimatePresence>
      </div>

      <div className="pointer-events-none absolute inset-x-0 bottom-0 z-20 h-40 bg-gradient-to-t from-black/50 to-transparent" />

      <div className="absolute inset-x-0 bottom-0 z-20 pb-[calc(env(safe-area-inset-bottom)+10px)]">
        <AnimatePresence>{currentTrack && !nowPlayingOpen && <MiniPlayer />}</AnimatePresence>
        <div className="px-4 pb-1">
          <BottomNav active={tab} onChange={setTab} />
        </div>
      </div>

      <AnimatePresence>{nowPlayingOpen && <NowPlaying />}</AnimatePresence>

      <WidgetPreview open={widgetOpen} onClose={() => setWidgetOpen(false)} />
    </div>
  );
}

export default function App() {
  return (
    <div className="flex min-h-dvh w-full items-center justify-center bg-[#020203] p-0 sm:p-8">
      <div className="pointer-events-none fixed inset-0 opacity-60">
        <div className="absolute left-1/4 top-0 h-[500px] w-[500px] -translate-x-1/2 rounded-full bg-indigo-500/10 blur-[140px]" />
        <div className="absolute bottom-0 right-1/4 h-[500px] w-[500px] translate-x-1/2 rounded-full bg-fuchsia-500/10 blur-[140px]" />
      </div>

      <div className="relative h-dvh w-full overflow-hidden bg-black sm:h-[860px] sm:w-[400px] sm:rounded-[56px] sm:ring-[10px] sm:ring-[#111214] sm:shadow-[0_60px_120px_-30px_rgba(0,0,0,0.9)]">
        <div className="pointer-events-none absolute left-1/2 top-0 z-40 hidden h-6 w-32 -translate-x-1/2 rounded-b-2xl bg-black sm:block" />
        <PlayerProvider>
          <AppShell />
        </PlayerProvider>
      </div>
    </div>
  );
}
