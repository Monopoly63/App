import { Track } from "../types";

export interface FolderTreeNode {
  name: string;
  path: string;
  depth: number;
  children: FolderTreeNode[];
  directCount: number;
  descendantFolderPaths: string[];
  totalCount: number;
  suspicious: boolean;
}

const SUSPICIOUS_PATTERN = /whatsapp|voice ?note|call ?record|recordings?|ringtone|notification|alarm|private ?audio/i;

export function buildFolderTree(tracks: Track[]): FolderTreeNode[] {
  const roots = new Map<string, FolderTreeNode>();

  const countsByPath = new Map<string, number>();
  tracks.forEach((t) => {
    countsByPath.set(t.folderPath, (countsByPath.get(t.folderPath) ?? 0) + 1);
  });

  function ensureNode(map: Map<string, FolderTreeNode>, name: string, path: string, depth: number): FolderTreeNode {
    let node = map.get(name);
    if (!node) {
      node = {
        name,
        path,
        depth,
        children: [],
        directCount: 0,
        descendantFolderPaths: [],
        totalCount: 0,
        suspicious: SUSPICIOUS_PATTERN.test(name),
      };
      map.set(name, node);
    }
    return node;
  }

  const childMaps = new Map<string, Map<string, FolderTreeNode>>();

  Array.from(countsByPath.keys()).forEach((folderPath) => {
    const segments = folderPath.split("/");
    let currentMap = roots;
    let accPath = "";
    segments.forEach((seg, i) => {
      accPath = accPath ? `${accPath}/${seg}` : seg;
      const node = ensureNode(currentMap, seg, accPath, i);
      if (i === segments.length - 1) {
        node.directCount = countsByPath.get(folderPath) ?? 0;
        node.descendantFolderPaths.push(folderPath);
      }
      if (!childMaps.has(node.path)) childMaps.set(node.path, new Map());
      currentMap = childMaps.get(node.path)!;
    });
  });

  function attachChildren(map: Map<string, FolderTreeNode>): FolderTreeNode[] {
    const nodes = Array.from(map.values());
    nodes.forEach((node) => {
      const childMap = childMaps.get(node.path);
      if (childMap) {
        node.children = attachChildren(childMap).sort((a, b) => a.name.localeCompare(b.name));
      }
    });
    nodes.forEach((node) => {
      const childPaths = node.children.flatMap((c) => c.descendantFolderPaths);
      node.descendantFolderPaths = Array.from(new Set([...node.descendantFolderPaths, ...childPaths]));
      node.totalCount =
        node.directCount + node.children.reduce((sum, c) => sum + c.totalCount, 0);
    });
    return nodes;
  }

  return attachChildren(roots).sort((a, b) => a.name.localeCompare(b.name));
}
