import api from './api';

// Workspace API calls
export const workspaceService = {
  // Get all workspaces for current user
  getAllWorkspaces: async () => {
    const response = await api.get('/workspaces');
    return response.data.data;
  },

  // Get workspace by ID
  getWorkspace: async (workspaceId) => {
    const response = await api.get(`/workspaces/${workspaceId}`);
    return response.data.data;
  },

  // Create new workspace
  createWorkspace: async (name, description) => {
    const response = await api.post('/workspaces', { name, description });
    return response.data.data;
  },

  // Update workspace
  updateWorkspace: async (workspaceId, name, description) => {
    const response = await api.put(`/workspaces/${workspaceId}`, { name, description });
    return response.data.data;
  },

  // Delete workspace
  deleteWorkspace: async (workspaceId) => {
    await api.delete(`/workspaces/${workspaceId}`);
  },

  // Get workspace members
  getMembers: async (workspaceId) => {
    const response = await api.get(`/workspaces/${workspaceId}/members`);
    return response.data.data;
  },

  // Invite user to workspace
  inviteUser: async (workspaceId, email, role = 'MEMBER') => {
    const response = await api.post(`/workspaces/${workspaceId}/members/invite`, { email, role });
    return response.data.data;
  },

  // Update member role
  updateMemberRole: async (workspaceId, userId, role) => {
    const response = await api.put(`/workspaces/${workspaceId}/members/${userId}`, { role });
    return response.data.data;
  },

  // Remove member from workspace
  removeMember: async (workspaceId, userId) => {
    await api.delete(`/workspaces/${workspaceId}/members/${userId}`);
  },
};

export default workspaceService;
