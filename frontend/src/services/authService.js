import api from '../config/api';

export const authService = {
  register: async (userData) => {
    const response = await api.post('/api/users/register', userData);
    return response.data;
  },

  validateUser: async (userId) => {
    const response = await api.get(`/api/users/${userId}/validate`);
    return response.data;
  },

  getUserProfile: async (userId) => {
    const response = await api.get(`/api/users/${userId}`);
    return response.data;
  },
};
