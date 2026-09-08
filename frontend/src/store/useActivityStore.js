import { create } from 'zustand';

export const useActivityStore = create((set) => ({
  activities: [],
  currentActivity: null,
  loading: false,
  error: null,
  
  setActivities: (activities) => set({ activities }),
  setCurrentActivity: (activity) => set({ currentActivity: activity }),
  setLoading: (loading) => set({ loading }),
  setError: (error) => set({ error }),
  addActivity: (activity) => set((state) => ({ 
    activities: [activity, ...state.activities] 
  })),
}));
