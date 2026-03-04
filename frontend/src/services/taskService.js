import api from './api';

// Task API calls
export const taskService = {
  // Get all tasks in project
  getTasks: async (workspaceId, projectId) => {
    const response = await api.get(`/workspaces/${workspaceId}/projects/${projectId}/tasks`);
    return response.data.data;
  },

  // Get task by ID
  getTask: async (workspaceId, projectId, taskId) => {
    const response = await api.get(`/workspaces/${workspaceId}/projects/${projectId}/tasks/${taskId}`);
    return response.data.data;
  },

  // Create new task
  createTask: async (workspaceId, projectId, taskData) => {
    const response = await api.post(
      `/workspaces/${workspaceId}/projects/${projectId}/tasks`,
      taskData
    );
    return response.data.data;
  },

  // Update task
  updateTask: async (workspaceId, projectId, taskId, taskData) => {
    const response = await api.put(
      `/workspaces/${workspaceId}/projects/${projectId}/tasks/${taskId}`,
      taskData
    );
    return response.data.data;
  },

  // Delete task
  deleteTask: async (workspaceId, projectId, taskId) => {
    await api.delete(`/workspaces/${workspaceId}/projects/${projectId}/tasks/${taskId}`);
  },

  // Get task comments
  getComments: async (workspaceId, projectId, taskId) => {
    const response = await api.get(
      `/workspaces/${workspaceId}/projects/${projectId}/tasks/${taskId}/comments`
    );
    return response.data.data;
  },

  // Add comment to task
  addComment: async (workspaceId, projectId, taskId, content) => {
    const response = await api.post(
      `/workspaces/${workspaceId}/projects/${projectId}/tasks/${taskId}/comments`,
      { content }
    );
    return response.data.data;
  },

  // Delete comment
  deleteComment: async (workspaceId, projectId, taskId, commentId) => {
    await api.delete(
      `/workspaces/${workspaceId}/projects/${projectId}/tasks/${taskId}/comments/${commentId}`
    );
  },

  // Get assigned tasks for user
  getAssignedTasks: async (workspaceId) => {
    const response = await api.get(`/workspaces/${workspaceId}/tasks/assigned`);
    return response.data.data;
  },
};

export default taskService;
