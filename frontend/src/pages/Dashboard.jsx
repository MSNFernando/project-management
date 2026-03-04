import { Plus } from 'lucide-react'
import { useState, useEffect } from 'react'
import { useSelector } from 'react-redux'
import { toast } from 'react-hot-toast'
import StatsGrid from '../components/StatsGrid'
import ProjectOverview from '../components/ProjectOverview'
import RecentActivity from '../components/RecentActivity'
import TasksSummary from '../components/TasksSummary'
import CreateProjectDialog from '../components/CreateProjectDialog'
import dashboardService from '../services/dashboardService'

const Dashboard = () => {
    const { user } = useSelector(state => state.auth)
    const { currentWorkspace } = useSelector(state => state.workspace)
    
    const [isDialogOpen, setIsDialogOpen] = useState(false)
    const [loading, setLoading] = useState(true)
    const [stats, setStats] = useState(null)
    const [recentActivity, setRecentActivity] = useState([])
    const [tasksSummary, setTasksSummary] = useState(null)
    const [projectOverview, setProjectOverview] = useState(null)

    useEffect(() => {
        if (!currentWorkspace?.id) return

        const fetchDashboardData = async () => {
            try {
                setLoading(true)
                const [statsData, activityData, summaryData, overviewData] = await Promise.all([
                    dashboardService.getStats(currentWorkspace.id),
                    dashboardService.getRecentActivity(currentWorkspace.id),
                    dashboardService.getTasksSummary(currentWorkspace.id),
                    dashboardService.getProjectOverview(currentWorkspace.id)
                ])
                
                setStats(statsData)
                setRecentActivity(activityData)
                setTasksSummary(summaryData)
                setProjectOverview(overviewData)
            } catch (error) {
                console.error('Failed to fetch dashboard data:', error)
                toast.error(error.response?.data?.message || 'Failed to load dashboard')
            } finally {
                setLoading(false)
            }
        }

        fetchDashboardData()
    }, [currentWorkspace?.id])

    if (loading) {
        return (
            <div className='max-w-6xl mx-auto flex items-center justify-center py-20'>
                <div className="text-center">
                    <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
                    <p className="mt-4 text-gray-600 dark:text-gray-400">Loading dashboard...</p>
                </div>
            </div>
        )
    }

    return (
        <div className='max-w-6xl mx-auto'>
            <div className="flex flex-col lg:flex-row justify-between items-start lg:items-center gap-6 ">
                <div>
                    <h1 className="text-xl sm:text-2xl font-semibold text-gray-900 dark:text-white mb-1">
                        Welcome back, {user?.fullName || 'User'}
                    </h1>
                    <p className="text-gray-500 dark:text-zinc-400 text-sm">
                        Here's what's happening with your projects today
                    </p>
                </div>

                <button 
                    onClick={() => setIsDialogOpen(true)} 
                    className="flex items-center gap-2 px-5 py-2 text-sm rounded bg-gradient-to-br from-blue-500 to-blue-600 text-white space-x-2 hover:opacity-90 transition"
                >
                    <Plus size={16} /> New Project
                </button>

                <CreateProjectDialog isDialogOpen={isDialogOpen} setIsDialogOpen={setIsDialogOpen} />
            </div>

            <StatsGrid stats={stats} />

            <div className="grid lg:grid-cols-3 gap-8">
                <div className="lg:col-span-2 space-y-8">
                    <ProjectOverview projects={projectOverview} />
                    <RecentActivity activities={recentActivity} />
                </div>
                <div>
                    <TasksSummary summary={tasksSummary} />
                </div>
            </div>
        </div>
    )
}

export default Dashboard
