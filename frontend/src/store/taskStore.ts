import { create } from 'zustand';
import { devtools } from 'zustand/middleware';

interface VoiceTask {
  taskId: string;
  userId: string;
  text: string;
  voiceId: string;
  status: 'pending' | 'processing' | 'completed' | 'failed';
  audioUrl?: string;
  duration?: number;
  createdAt: string;
  updatedAt: string;
}

interface TaskState {
  tasks: VoiceTask[];
  loading: boolean;
  error: string | null;
  
  // Actions
  fetchTasks: (userId: string) => Promise<void>;
  addTask: (task: VoiceTask) => void;
  updateTask: (taskId: string, updates: Partial<VoiceTask>) => void;
  deleteTask: (taskId: string) => void;
  deleteTasks: (taskIds: string[]) => void;
  clearTasks: () => void;
  startPolling: (userId: string, interval?: number) => void;
  stopPolling: () => void;
}

let pollingInterval: NodeJS.Timeout | null = null;

export const useTaskStore = create<TaskState>()(
  devtools((set, get) => ({
    tasks: [],
    loading: false,
    error: null,
    
    fetchTasks: async (userId: string) => {
      set({ loading: true, error: null });
      try {
        const response = await fetch(`/api/voice/tasks?userId=${userId}`);
        if (!response.ok) {
          throw new Error(`Failed to fetch tasks: ${response.status}`);
        }
        const tasks: VoiceTask[] = await response.json();
        set({ tasks, loading: false });
      } catch (error) {
        console.error('Error fetching tasks:', error);
        set({ 
          error: error instanceof Error ? error.message : 'Unknown error',
          loading: false 
        });
      }
    },
    
    addTask: (task: VoiceTask) => {
      set((state) => ({ 
        tasks: [task, ...state.tasks] 
      }));
    },
    
    updateTask: (taskId: string, updates: Partial<VoiceTask>) => {
      set((state) => ({
        tasks: state.tasks.map(task => 
          task.taskId === taskId ? { ...task, ...updates } : task
        )
      }));
    },
    
    deleteTask: (taskId: string) => {
      set((state) => ({
        tasks: state.tasks.filter(task => task.taskId !== taskId)
      }));
    },
    
    deleteTasks: (taskIds: string[]) => {
      set((state) => ({
        tasks: state.tasks.filter(task => !taskIds.includes(task.taskId))
      }));
    },
    
    clearTasks: () => {
      set({ tasks: [] });
    },
    
    startPolling: (userId: string, interval = 5000) => {
      // 清除现有的轮询
      if (pollingInterval) {
        clearInterval(pollingInterval);
      }
      
      // 开始新的轮询
      pollingInterval = setInterval(() => {
        get().fetchTasks(userId);
      }, interval);
    },
    
    stopPolling: () => {
      if (pollingInterval) {
        clearInterval(pollingInterval);
        pollingInterval = null;
      }
    }
  }))
);