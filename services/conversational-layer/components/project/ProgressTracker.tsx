'use client'

import { useConversationStore } from '@/lib/store';
import { CheckCircle2, Circle, Loader2 } from 'lucide-react';
import { cn } from '@/lib/utils';

export default function ProgressTracker() {
  const progressUpdates = useConversationStore((state) => state.progressUpdates);
  
  if (progressUpdates.length === 0) return null;

  const latestUpdate = progressUpdates[progressUpdates.length - 1];
  const uniqueSteps = Array.from(
    new Map(progressUpdates.map(u => [u.step, u])).values()
  );

  return (
    <div className="p-4 space-y-4">
      {/* Current Progress */}
      <div className="space-y-2">
        <div className="flex items-center justify-between text-sm">
          <span className="font-medium">{latestUpdate.step}</span>
          <span className="text-muted-foreground">{latestUpdate.progress}%</span>
        </div>
        <div className="relative h-2 bg-secondary rounded-full overflow-hidden">
          <div
            className="absolute top-0 left-0 h-full bg-primary transition-all duration-500 ease-out"
            style={{ width: `${latestUpdate.progress}%` }}
          />
        </div>
        <p className="text-sm text-muted-foreground">{latestUpdate.message}</p>
      </div>

      {/* Steps Overview */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-2">
        {uniqueSteps.map((step, index) => {
          const isCompleted = step.progress === 100;
          const isCurrent = step.step === latestUpdate.step;
          
          return (
            <div
              key={step.step}
              className={cn(
                'flex items-center gap-2 p-2 rounded-lg text-sm',
                isCurrent && 'bg-secondary',
                isCompleted && 'text-muted-foreground'
              )}
            >
              {isCompleted ? (
                <CheckCircle2 size={16} className="text-green-500" />
              ) : isCurrent ? (
                <Loader2 size={16} className="animate-spin text-primary" />
              ) : (
                <Circle size={16} className="text-muted-foreground" />
              )}
              <span className="truncate">{step.step}</span>
            </div>
          );
        })}
      </div>
    </div>
  );
}
