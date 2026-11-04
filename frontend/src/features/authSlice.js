import { createSlice } from '@reduxjs/toolkit';

// Restore the session before the first render, so a refresh or a shared link
// opens the page it points to instead of bouncing through /login to the dashboard.
const storedSession = () => {
  try {
    const token = localStorage.getItem('accessToken');
    const user = JSON.parse(localStorage.getItem('user') || 'null');
    if (token && user) return { user, token, isAuthenticated: true };
  } catch {
    // Unreadable storage: start signed out.
  }
  return { user: null, token: null, isAuthenticated: false };
};

const authSlice = createSlice({
  name: 'auth',
  initialState: {
    ...storedSession(),
    loading: false,
    error: null,
  },
  reducers: {
    setAuth: (state, action) => {
      state.user = action.payload.user;
      state.isAuthenticated = action.payload.isAuthenticated;
      state.token = action.payload.token;
      state.error = null;
    },
    setLoading: (state, action) => {
      state.loading = action.payload;
    },
    setError: (state, action) => {
      state.error = action.payload;
    },
    logout: (state) => {
      state.user = null;
      state.isAuthenticated = false;
      state.token = null;
      localStorage.clear();
    },
  },
});

export const { setAuth, setLoading, setError, logout } = authSlice.actions;
export default authSlice.reducer;
