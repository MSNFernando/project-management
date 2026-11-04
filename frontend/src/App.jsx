import { Routes, Route, Navigate, useLocation } from "react-router-dom";
import { useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import Layout from "./pages/Layout";
import Login from "./pages/Login";
import Signup from "./pages/Signup";
import { Toaster } from "react-hot-toast";
import Dashboard from "./pages/Dashboard";
import Projects from "./pages/Projects";
import Team from "./pages/Team";
import ProjectDetails from "./pages/ProjectDetails";
import TaskDetails from "./pages/TaskDetails";
import ProtectedRoute from "./components/ProtectedRoute";
import { setAuth } from "./features/authSlice";
import { fetchWorkspaces } from "./features/workspaceSlice";

// Signed in already: go where ProtectedRoute was sending them, or the dashboard
const SignedInRedirect = () => {
    const from = useLocation().state?.from;
    return <Navigate to={from ? `${from.pathname}${from.search || ""}` : "/"} replace />;
};

const App = () => {
    const dispatch = useDispatch();
    const { isAuthenticated } = useSelector(state => state.auth);

    // Check for existing token on app load
    useEffect(() => {
        const token = localStorage.getItem('accessToken');
        const user = localStorage.getItem('user');
        
        if (token && user) {
            try {
                dispatch(setAuth({
                    user: JSON.parse(user),
                    isAuthenticated: true,
                    token,
                }));
                dispatch(fetchWorkspaces());
            } catch (error) {
                console.error('Failed to restore auth state:', error);
                localStorage.clear();
            }
        }
    }, [dispatch]);

    return (
        <>
            <Toaster />
            <Routes>
                {/* Auth Routes */}
                <Route path="/login" element={isAuthenticated ? <SignedInRedirect /> : <Login />} />
                <Route path="/signup" element={isAuthenticated ? <SignedInRedirect /> : <Signup />} />

                {/* Protected Routes */}
                <Route path="/" element={
                    <ProtectedRoute>
                        <Layout />
                    </ProtectedRoute>
                }>
                    <Route index element={<Dashboard />} />
                    <Route path="team" element={<Team />} />
                    <Route path="projects" element={<Projects />} />
                    <Route path="projectsDetail" element={<ProjectDetails />} />
                    <Route path="taskDetails" element={<TaskDetails />} />
                </Route>

                {/* Catch all */}
                <Route path="*" element={<Navigate to="/" />} />
            </Routes>
        </>
    );
};

export default App;
