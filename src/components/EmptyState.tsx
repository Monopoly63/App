import { LucideIcon } from "lucide-react";

export default function EmptyState({
  icon: Icon,
  title,
  subtitle,
}: {
  icon: LucideIcon;
  title: string;
  subtitle: string;
}) {
  return (
    <div className="flex flex-col items-center justify-center gap-3 px-10 py-20 text-center">
      <div className="glass-panel flex h-16 w-16 items-center justify-center rounded-2xl text-white/70">
        <Icon size={26} strokeWidth={1.5} />
      </div>
      <p className="text-[15px] font-medium text-white/85">{title}</p>
      <p className="text-[12.5px] leading-relaxed text-white/40">{subtitle}</p>
    </div>
  );
}
