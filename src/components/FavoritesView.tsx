import { Heart } from "lucide-react";
import { usePlayer } from "../context/PlayerContext";
import SongRow from "./SongRow";
import EmptyState from "./EmptyState";

export default function FavoritesView() {
  const { tracks, favorites } = usePlayer();
  const favTracks = tracks.filter((t) => favorites.includes(t.id));

  if (favTracks.length === 0) {
    return (
      <EmptyState
        icon={Heart}
        title="No favorites yet"
        subtitle="Tap the heart on any track to keep it close — it will instantly appear here."
      />
    );
  }

  return (
    <div className="space-y-4 px-4 pb-4">
      <div className="flex items-center justify-between px-1.5 text-[11px] text-white/35">
        <span>{favTracks.length} favorite{favTracks.length === 1 ? "" : "s"}</span>
      </div>
      <div className="space-y-0.5">
        {favTracks.map((track, i) => (
          <SongRow key={track.id} track={track} index={i} />
        ))}
      </div>
    </div>
  );
}
