import { motion } from "framer-motion";
import { Disc3, FolderTree, Heart } from "lucide-react";
import { cn } from "../utils/cn";

export type TabKey = "all" | "folders" | "favorites";

const TABS: { key: TabKey; label: string; icon: typeof Disc3 }[] = [
  { key: "all", label: "Songs", icon: Disc3 },
  { key: "folders", label: "Folders", icon: FolderTree },
  { key: "favorites", label: "Favorites", icon: Heart },
];

export default function BottomNav({
  active,
  onChange,
}: {
  active: TabKey;
  onChange: (tab: TabKey) => void;
}) {
  return (
    <div className="glass-panel mx-auto flex w-full max-w-[280px] items-center justify-between rounded-full px-2 py-2">
      {TABS.map(({ key, label, icon: Icon }) => {
        const isActive = active === key;
        return (
          <button
            key={key}
            onClick={() => onChange(key)}
            className="relative flex flex-1 flex-col items-center gap-0.5 rounded-full px-2 py-1.5"
          >
            {isActive && (
              <motion.div
                layoutId="nav-pill"
                className="absolute inset-0 rounded-full bg-white/12 ring-1 ring-white/15"
                transition={{ type: "spring", stiffness: 400, damping: 32 }}
              />
            )}
            <Icon
              size={17}
              strokeWidth={isActive ? 2.1 : 1.6}
              className={cn("relative z-10 transition-colors", isActive ? "text-white" : "text-white/45")}
              fill={key === "favorites" && isActive ? "currentColor" : "none"}
            />
            <span
              className={cn(
                "relative z-10 text-[9.5px] font-medium tracking-wide transition-colors",
                isActive ? "text-white" : "text-white/40"
              )}
            >
              {label}
            </span>
          </button>
        );
      })}
    </div>
  );
}
