import type { HTMLAttributes } from "react";
import { cn } from "../utils/cn";

export function GlassCard({ className, ...props }: HTMLAttributes<HTMLDivElement>) {
  return <div className={cn("glass-panel rounded-3xl", className)} {...props} />;
}

export function GlassPill({ className, ...props }: HTMLAttributes<HTMLDivElement>) {
  return <div className={cn("glass-pill rounded-full", className)} {...props} />;
}
