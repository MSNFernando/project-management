import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";
import workspaceService from "../services/workspaceService";

// Async thunk to fetch workspaces from the API
export const fetchWorkspaces = createAsyncThunk(
    "workspace/fetchWorkspaces",
    async (_, { rejectWithValue }) => {
        try {
            return await workspaceService.getAllWorkspaces();
        } catch (error) {
            return rejectWithValue(error.response?.data?.message || "Failed to fetch workspaces");
        }
    }
);

const initialState = {
    workspaces: [],
    currentWorkspace: null,
    loading: false,
    error: null,
};

const workspaceSlice = createSlice({
    name: "workspace",
    initialState,
    reducers: {
        setWorkspaces: (state, action) => {
            state.workspaces = action.payload;
        },
        setCurrentWorkspace: (state, action) => {
            localStorage.setItem("currentWorkspaceId", action.payload);
            state.currentWorkspace = state.workspaces.find((w) => w.id === action.payload) || null;
        },
        addWorkspace: (state, action) => {
            state.workspaces.push(action.payload);
            if (!state.currentWorkspace || state.currentWorkspace.id !== action.payload.id) {
                state.currentWorkspace = action.payload;
            }
        },
        updateWorkspace: (state, action) => {
            state.workspaces = state.workspaces.map((w) =>
                w.id === action.payload.id ? action.payload : w
            );
            if (state.currentWorkspace?.id === action.payload.id) {
                state.currentWorkspace = action.payload;
            }
        },
        deleteWorkspace: (state, action) => {
            state.workspaces = state.workspaces.filter((w) => w.id !== action.payload);
            if (state.currentWorkspace?.id === action.payload) {
                state.currentWorkspace = state.workspaces[0] || null;
            }
        },
        clearWorkspaces: (state) => {
            state.workspaces = [];
            state.currentWorkspace = null;
            state.loading = false;
            state.error = null;
        },
    },
    extraReducers: (builder) => {
        builder
            .addCase(fetchWorkspaces.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(fetchWorkspaces.fulfilled, (state, action) => {
                state.loading = false;
                state.workspaces = action.payload;
                // Restore previously selected workspace, or default to first
                const savedId = localStorage.getItem("currentWorkspaceId");
                const saved = savedId ? action.payload.find((w) => w.id === savedId) : null;
                state.currentWorkspace = saved || action.payload[0] || null;
            })
            .addCase(fetchWorkspaces.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload;
            });
    },
});

export const {
    setWorkspaces,
    setCurrentWorkspace,
    addWorkspace,
    updateWorkspace,
    deleteWorkspace,
    clearWorkspaces,
} = workspaceSlice.actions;
export default workspaceSlice.reducer;
