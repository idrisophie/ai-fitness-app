import api from '../config/api';

export const recommendationService = {
  getUserRecommendations: async (userId) => {
    const response = await api.get(`/api/recommendations/user/${userId}`);
    return response.data;
  },

  getActivityRecommendation: async (activityId) => {
    const response = await api.get(`/api/recommendations/activity/${activityId}`);
    return response.data;
  },
};
