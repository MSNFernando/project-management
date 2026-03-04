import api from './api';

// Project API calls
export const projectService = {
  // Get all projects in workspace
  getProjects: async (workspaceId) => {
    const response = await api.get(`/workspaces/${workspaceId}/projects`);
    return response.data.data;
  },

  // Get project by ID
  getProject: async (workspaceId, projectId) => {
    const response = await api.get(`/workspaces/${workspaceId}/projects/${projectId}`);
    return response.data.data;
  },

  // Create new project
  createProject: async (workspaceId, projectData) => {
    const response = await api.post(`/workspaces/${workspaceId}/projects`, projectData);
    return response.data.data;
  },

  // Update project
  updateProject: async (workspaceId, projectId, projectData) => {
    const response = await api.put(`/workspaces/${workspaceId}/projects/${projectId}`, projectData);
    return response.data.data;
  },

  // Delete project
  deleteProject: async (workspaceId, projectId) => {
    await api.delete(`/workspaces/${workspaceId}/projects/${projectId}`);
  },

  // Get project members
  getMembers: async (workspaceId, projectId) => {
    const response = await api.get(`/workspaces/${workspaceId}/projects/${projectId}/members`);
    return response.data.data;
  },

  // Add member to project
  addMember: async (workspaceId, projectId, userId) => {
    const response = await api.post(
      `/workspaces/${workspaceId}/projects/${projectId}/members`,
      { userId }
    );
    return response.data.data;
  },

  // Remove member from project
  removeMember: async (workspaceId, projectId, userId) => {
    await api.delete(`/workspaces/${workspaceId}/projects/${projectId}/members/${userId}`);
  },

  // Get project analytics
  getAnalytics: async (workspaceId, projectId) => {
    const response = await api.get(`/workspaces/${workspaceId}/projects/${projectId}/analytics`);
    return response.data.data;
  },
};

export default projectService;
