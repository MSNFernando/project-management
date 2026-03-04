import api from './api';

// Dashboard API calls
export const dashboardService = {
  // Get dashboard statistics
  getStats: async (workspaceId) => {
    const response = await api.get(`/workspaces/${workspaceId}/dashboard/stats`);
    return response.data.data;
  },

  // Get recent activity
  getRecentActivity: async (workspaceId) => {
    const response = await api.get(`/workspaces/${workspaceId}/dashboard/recent-activity`);
    return response.data.data;
  },

  // Get user task summary
  getTasksSummary: async (workspaceId) => {
    const response = await api.get(`/workspaces/${workspaceId}/dashboard/tasks-summary`);
    return response.data.data;
  },

  // Get project overview
  getProjectOverview: async (workspaceId) => {
    const response = await api.get(`/workspaces/${workspaceId}/dashboard/project-overview`);
    return response.data.data;
  },
};

export default dashboardService;
