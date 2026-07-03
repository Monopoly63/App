import { Music2 } from "lucide-react";
import { usePlayer } from "../context/PlayerContext";
import SongRow from "./SongRow";
import EmptyState from "./EmptyState";

export default function AllSongsView() {
  const { filteredTracks, tracks } = usePlayer();

  if (tracks.length === 0) {
    return (
      <EmptyState
        icon={Music2}
        title="Your library is quiet"
        subtitle="Tap “Add music” above to select local audio files or an entire folder from your device."
      />
    );
  }

  if (filteredTracks.length === 0) {
    return (
      <EmptyState
        icon={Music2}
        title="Everything is filtered out"
        subtitle="Head to the Folders tab and include at least one folder to populate your library."
      />
    );
  }

  return (
    <div className="space-y-4 px-4 pb-4">
      <div className="flex items-center justify-between px-1.5 text-[11px] text-white/35">
        <span>{filteredTracks.length} tracks</span>
      </div>
      <div className="space-y-0.5">
        {filteredTracks.map((track, i) => (
          <SongRow key={track.id} track={track} index={i} />
        ))}
      </div>
    </div>
  );
}
