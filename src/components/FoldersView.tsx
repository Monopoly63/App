import { useMemo, useRef, useEffect, useState } from "react";
import { ChevronRight, FolderTree as FolderTreeIcon, AlertTriangle, Sparkles } from "lucide-react";
import { usePlayer } from "../context/PlayerContext";
import { buildFolderTree, FolderTreeNode } from "../utils/folderTree";
import EmptyState from "./EmptyState";
import { cn } from "../utils/cn";

function useFolderState(node: FolderTreeNode) {
  const { folderFilters, setFolderIncluded } = usePlayer();
  const paths = node.descendantFolderPaths;
  const includedCount = paths.filter((p) => folderFilters[p] !== false).length;
  const allIncluded = includedCount === paths.length;
  const noneIncluded = includedCount === 0;
  const indeterminate = !allIncluded && !noneIncluded;

  const toggle = () => {
    const next = !allIncluded;
    paths.forEach((p) => setFolderIncluded(p, next));
  };

  return { allIncluded, indeterminate, toggle };
}

function TriCheckbox({
  checked,
  indeterminate,
  onChange,
}: {
  checked: boolean;
  indeterminate: boolean;
  onChange: () => void;
}) {
  const ref = useRef<HTMLInputElement>(null);
  useEffect(() => {
    if (ref.current) ref.current.indeterminate = indeterminate;
  }, [indeterminate]);

  return (
    <button
      onClick={(e) => {
        e.stopPropagation();
        onChange();
      }}
      className={cn(
        "flex h-5 w-5 shrink-0 items-center justify-center rounded-md border transition-colors",
        checked
          ? "border-white/0 bg-white text-black"
          : indeterminate
          ? "border-white/40 bg-white/20 text-white"
          : "border-white/25 bg-white/5 text-transparent"
      )}
    >
      {checked && (
        <svg viewBox="0 0 24 24" width="12" height="12" fill="none" stroke="currentColor" strokeWidth={3}>
          <path d="M5 12l5 5L19 7" strokeLinecap="round" strokeLinejoin="round" />
        </svg>
      )}
      {indeterminate && !checked && <div className="h-[2px] w-2.5 rounded-full bg-white" />}
    </button>
  );
}

function FolderRow({ node }: { node: FolderTreeNode }) {
  const [open, setOpen] = useState(node.depth === 0);
  const { allIncluded, indeterminate, toggle } = useFolderState(node);
  const hasChildren = node.children.length > 0;

  return (
    <div>
      <div
        onClick={() => hasChildren && setOpen((o) => !o)}
        className={cn(
          "flex items-center gap-2.5 rounded-2xl px-2.5 py-2.5 transition-colors",
          hasChildren ? "cursor-pointer hover:bg-white/5" : "",
          !allIncluded && "opacity-50"
        )}
        style={{ marginLeft: node.depth * 14 }}
      >
        {hasChildren ? (
          <ChevronRight
            size={14}
            className={cn("shrink-0 text-white/35 transition-transform", open && "rotate-90")}
          />
        ) : (
          <span className="w-[14px] shrink-0" />
        )}

        <TriCheckbox checked={allIncluded} indeterminate={indeterminate} onChange={toggle} />

        <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-white/8 text-white/60">
          <FolderTreeIcon size={14} />
        </div>

        <div className="min-w-0 flex-1">
          <div className="flex items-center gap-1.5">
            <p className="truncate text-[13px] font-medium text-white/90">{node.name}</p>
            {node.suspicious && (
              <span title="Looks like system audio, not music">
                <AlertTriangle size={11} className="shrink-0 text-amber-300/80" />
              </span>
            )}
          </div>
          <p className="text-[11px] text-white/40">
            {node.totalCount} track{node.totalCount === 1 ? "" : "s"}
          </p>
        </div>
      </div>

      {hasChildren && open && (
        <div className="space-y-0.5">
          {node.children.map((child) => (
            <FolderRow key={child.path} node={child} />
          ))}
        </div>
      )}
    </div>
  );
}

export default function FoldersView() {
  const { tracks, folderFilters, setFolderIncluded } = usePlayer();
  const tree = useMemo(() => buildFolderTree(tracks), [tracks]);

  const allFolderPaths = useMemo(() => {
    const set = new Set<string>();
    tracks.forEach((t) => set.add(t.folderPath));
    return Array.from(set);
  }, [tracks]);

  const suspiciousPaths = useMemo(() => {
    const suspicious: string[] = [];
    function walk(nodes: FolderTreeNode[]) {
      nodes.forEach((n) => {
        if (n.suspicious) suspicious.push(...n.descendantFolderPaths);
        walk(n.children);
      });
    }
    walk(tree);
    return Array.from(new Set(suspicious));
  }, [tree]);

  if (tracks.length === 0) {
    return (
      <EmptyState
        icon={FolderTreeIcon}
        title="No folders indexed yet"
        subtitle="Add a music folder from the top bar to build your directory whitelist here."
      />
    );
  }

  const excludedCount = allFolderPaths.filter((p) => folderFilters[p] === false).length;

  return (
    <div className="space-y-3 px-4 pb-4">
      <div className="flex items-center justify-between gap-2 px-1">
        <p className="text-[11px] text-white/35">
          {allFolderPaths.length} folders · {excludedCount} excluded
        </p>
        <div className="flex gap-1.5">
          <button
            onClick={() => allFolderPaths.forEach((p) => setFolderIncluded(p, true))}
            className="glass-pill rounded-full px-2.5 py-1 text-[10.5px] font-medium text-white/70 active:scale-95"
          >
            Include all
          </button>
          {suspiciousPaths.length > 0 && (
            <button
              onClick={() => suspiciousPaths.forEach((p) => setFolderIncluded(p, false))}
              className="glass-pill flex items-center gap-1 rounded-full px-2.5 py-1 text-[10.5px] font-medium text-amber-200 active:scale-95"
            >
              <Sparkles size={11} />
              Clean up
            </button>
          )}
        </div>
      </div>

      <div className="glass-panel rounded-3xl p-2">
        <div className="space-y-0.5">
          {tree.map((node) => (
            <FolderRow key={node.path} node={node} />
          ))}
        </div>
      </div>

      <p className="px-2 text-[10.5px] leading-relaxed text-white/30">
        Uncheck a folder to hide it everywhere in the app — perfect for silencing WhatsApp voice
        notes, call recordings, or ringtones. Your choices are remembered on this device.
      </p>
    </div>
  );
}
