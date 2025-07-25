'use client'

import { useEffect, useState } from 'react';
import { Database, Server, Layout, Cloud, GitBranch, Shield } from 'lucide-react';
import { cn } from '@/lib/utils';

interface ServiceNode {
  id: string;
  name: string;
  type: 'domain' | 'backend' | 'frontend' | 'database' | 'external';
  icon: any;
  position: { x: number; y: number };
  color: string;
}

interface DataFlow {
  from: string;
  to: string;
  active: boolean;
}

export default function DataFlowVisualization() {
  const [activeFlows, setActiveFlows] = useState<Set<string>>(new Set());
  const [dots, setDots] = useState<{ id: string; from: string; to: string; progress: number }[]>([]);

  const services: ServiceNode[] = [
    { id: 'frontend', name: 'Frontend', type: 'frontend', icon: Layout, position: { x: 50, y: 20 }, color: 'text-blue-500' },
    { id: 'gateway', name: 'API Gateway', type: 'backend', icon: Shield, position: { x: 50, y: 40 }, color: 'text-purple-500' },
    { id: 'auth', name: 'Auth Service', type: 'domain', icon: Server, position: { x: 20, y: 60 }, color: 'text-green-500' },
    { id: 'user', name: 'User Service', type: 'domain', icon: Server, position: { x: 50, y: 60 }, color: 'text-green-500' },
    { id: 'product', name: 'Product Service', type: 'domain', icon: Server, position: { x: 80, y: 60 }, color: 'text-green-500' },
    { id: 'database', name: 'Database', type: 'database', icon: Database, position: { x: 35, y: 80 }, color: 'text-orange-500' },
    { id: 'cache', name: 'Cache', type: 'database', icon: Database, position: { x: 65, y: 80 }, color: 'text-red-500' },
    { id: 'external', name: 'External API', type: 'external', icon: Cloud, position: { x: 90, y: 40 }, color: 'text-cyan-500' },
  ];

  const flows: DataFlow[] = [
    { from: 'frontend', to: 'gateway', active: true },
    { from: 'gateway', to: 'auth', active: true },
    { from: 'gateway', to: 'user', active: true },
    { from: 'gateway', to: 'product', active: true },
    { from: 'auth', to: 'database', active: true },
    { from: 'user', to: 'database', active: true },
    { from: 'product', to: 'database', active: true },
    { from: 'user', to: 'cache', active: true },
    { from: 'product', to: 'cache', active: true },
    { from: 'product', to: 'external', active: true },
  ];

  useEffect(() => {
    const flowSequence = async () => {
      const sequence = [
        ['frontend-gateway'],
        ['gateway-auth', 'gateway-user', 'gateway-product'],
        ['auth-database', 'user-database', 'product-database'],
        ['user-cache', 'product-cache'],
        ['product-external'],
      ];

      for (const group of sequence) {
        group.forEach(flowId => {
          setActiveFlows(prev => new Set(prev).add(flowId));
          
          const [fromId, toId] = flowId.split('-');
          const dotId = `${flowId}-${Date.now()}`;
          
          setDots(prev => [...prev, { id: dotId, from: fromId, to: toId, progress: 0 }]);

          const animateDot = setInterval(() => {
            setDots(prev => prev.map(dot => 
              dot.id === dotId 
                ? { ...dot, progress: Math.min(dot.progress + 2, 100) }
                : dot
            ));
          }, 20);

          setTimeout(() => {
            clearInterval(animateDot);
            setDots(prev => prev.filter(dot => dot.id !== dotId));
          }, 2000);
        });
        
        await new Promise(resolve => setTimeout(resolve, 800));
      }

      setTimeout(() => {
        setActiveFlows(new Set());
        flowSequence();
      }, 2000);
    };

    flowSequence();
  }, []);

  const getNodePosition = (nodeId: string) => {
    const node = services.find(s => s.id === nodeId);
    return node ? node.position : { x: 50, y: 50 };
  };

  const calculateDotPosition = (from: string, to: string, progress: number) => {
    const fromPos = getNodePosition(from);
    const toPos = getNodePosition(to);
    
    return {
      x: fromPos.x + (toPos.x - fromPos.x) * (progress / 100),
      y: fromPos.y + (toPos.y - fromPos.y) * (progress / 100),
    };
  };

  return (
    <div className="relative w-full h-96 bg-card rounded-lg border p-6 overflow-hidden">
      <div className="absolute inset-0 bg-gradient-to-br from-background via-background to-primary/5" />
      
      <svg className="absolute inset-0 w-full h-full" style={{ zIndex: 1 }}>
        {flows.map(flow => {
          const from = getNodePosition(flow.from);
          const to = getNodePosition(flow.to);
          const flowId = `${flow.from}-${flow.to}`;
          const isActive = activeFlows.has(flowId);
          
          return (
            <line
              key={flowId}
              x1={`${from.x}%`}
              y1={`${from.y}%`}
              x2={`${to.x}%`}
              y2={`${to.y}%`}
              stroke="currentColor"
              strokeWidth="1"
              className={cn(
                'transition-all duration-500',
                isActive ? 'text-primary/40' : 'text-muted-foreground/20'
              )}
              strokeDasharray={isActive ? '0' : '5,5'}
            />
          );
        })}

        {dots.map(dot => {
          const pos = calculateDotPosition(dot.from, dot.to, dot.progress);
          return (
            <circle
              key={dot.id}
              cx={`${pos.x}%`}
              cy={`${pos.y}%`}
              r="4"
              className="fill-primary animate-pulse"
            >
              <animate
                attributeName="r"
                values="3;6;3"
                dur="1s"
                repeatCount="indefinite"
              />
            </circle>
          );
        })}
      </svg>

      {services.map(service => {
        const Icon = service.icon;
        return (
          <div
            key={service.id}
            className="absolute transform -translate-x-1/2 -translate-y-1/2 z-10"
            style={{ left: `${service.position.x}%`, top: `${service.position.y}%` }}
          >
            <div className="relative group">
              <div className={cn(
                'absolute inset-0 rounded-full blur-xl opacity-20 group-hover:opacity-40 transition-opacity',
                service.color.replace('text-', 'bg-')
              )} />
              <div className={cn(
                'relative flex flex-col items-center gap-1 p-3 rounded-lg',
                'bg-background border-2 transition-all duration-300',
                'hover:scale-110 hover:shadow-lg',
                activeFlows.has(service.id) || Array.from(activeFlows).some(f => f.includes(service.id))
                  ? 'border-primary shadow-md'
                  : 'border-border'
              )}>
                <Icon size={24} className={service.color} />
                <span className="text-xs font-medium whitespace-nowrap">{service.name}</span>
              </div>
            </div>
          </div>
        );
      })}

      <div className="absolute bottom-4 left-4 flex items-center gap-4 text-xs text-muted-foreground z-10">
        <div className="flex items-center gap-2">
          <div className="w-3 h-3 rounded-full bg-primary animate-pulse" />
          <span>Data Flow</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-8 border-t border-primary/40" />
          <span>Active Connection</span>
        </div>
      </div>
    </div>
  );
}