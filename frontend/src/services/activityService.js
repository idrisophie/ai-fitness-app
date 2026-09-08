import api from '../config/api';

export const activityService = {
  trackActivity: async (activityData) => {
    const response = await api.post('/api/activities', activityData);
    return response.data;
  },

  getUserActivities: async (userId) => {
    const response = await api.get('/api/activities', {
      headers: {
        'X-User-ID': userId,
      },
    });
    return response.data;
  },

  getActivityById: async (activityId) => {
    const response = await api.get(`/api/activities/${activityId}`);
    return response.data;
  },
};
