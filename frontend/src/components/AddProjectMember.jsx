import { useEffect, useState } from "react";
import { Mail, UserPlus } from "lucide-react";
import toast from "react-hot-toast";
import projectService from "../services/projectService";
import workspaceService from "../services/workspaceService";

const AddProjectMember = ({ isDialogOpen, setIsDialogOpen, project, onAdded }) => {

    const [workspaceMembers, setWorkspaceMembers] = useState([]);
    const [userId, setUserId] = useState('');
    const [isAdding, setIsAdding] = useState(false);

    const projectUserIds = (project?.members ?? []).map((member) => member?.user?.id);
    const candidates = workspaceMembers.filter((m) => !projectUserIds.includes(m.userId));

    // Load the workspace's members when the dialog opens: anyone in the workspace can be added
    useEffect(() => {
        if (!isDialogOpen || !project?.workspaceId) return;
        workspaceService.getMembers(project.workspaceId)
            .then((members) => setWorkspaceMembers(members || []))
            .catch(() => setWorkspaceMembers([]));
    }, [isDialogOpen, project?.workspaceId]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!userId) return;
        setIsAdding(true);
        try {
            await projectService.addMember(project.workspaceId, project.id, userId);
            toast.success("Member added to the project");
            setUserId('');
            setIsDialogOpen(false);
            await onAdded?.();
        } catch (error) {
            toast.error(error?.response?.data?.message || "Couldn't add the member");
        } finally {
            setIsAdding(false);
        }
    };

    if (!isDialogOpen) return null;

    return (
        <div className="fixed inset-0 bg-black/20 dark:bg-black/50 backdrop-blur flex items-center justify-center z-50">
            <div className="bg-white dark:bg-zinc-950 border border-zinc-300 dark:border-zinc-800 rounded-xl p-6 w-full max-w-md text-zinc-900 dark:text-zinc-200">
                {/* Header */}
                <div className="mb-4">
                    <h2 className="text-xl font-bold flex items-center gap-2">
                        <UserPlus className="size-5 text-zinc-900 dark:text-zinc-200" /> Add Member to Project
                    </h2>
                    {project && (
                        <p className="text-sm text-zinc-700 dark:text-zinc-400">
                            Adding to Project: <span className="text-blue-600 dark:text-blue-400">{project.name}</span>
                        </p>
                    )}
                </div>

                {/* Form */}
                <form onSubmit={handleSubmit} className="space-y-4">
                    <div className="space-y-2">
                        <label htmlFor="project-member" className="text-sm font-medium text-zinc-900 dark:text-zinc-200">
                            Workspace member
                        </label>
                        <div className="relative">
                            <Mail className="absolute left-3 top-1/2 -translate-y-1/2 text-zinc-500 dark:text-zinc-400 w-4 h-4" />
                            <select id="project-member" value={userId} onChange={(e) => setUserId(e.target.value)} className="pl-10 mt-1 w-full rounded border border-zinc-300 dark:border-zinc-700 dark:bg-zinc-900 text-zinc-900 dark:text-zinc-200 text-sm py-2 focus:outline-none focus:border-blue-500" required >
                                <option value="">Select a member</option>
                                {candidates.map((member) => (
                                    <option key={member.userId} value={member.userId}>
                                        {member.userName ? `${member.userName} (${member.email})` : member.email}
                                    </option>
                                ))}
                            </select>
                        </div>
                        {workspaceMembers.length > 0 && candidates.length === 0 && (
                            <p className="text-xs text-zinc-600 dark:text-zinc-400">Everyone in this workspace is already on the project. Invite more people from the Team page.</p>
                        )}
                    </div>

                    {/* Footer */}
                    <div className="flex justify-end gap-3 pt-2">
                        <button type="button" onClick={() => setIsDialogOpen(false)} className="px-5 py-2 text-sm rounded border border-zinc-300 dark:border-zinc-700 text-zinc-900 dark:text-zinc-200 hover:bg-zinc-200 dark:hover:bg-zinc-800 transition" >
                            Cancel
                        </button>
                        <button type="submit" disabled={isAdding || !userId} className="px-5 py-2 text-sm rounded bg-gradient-to-br from-blue-500 to-blue-600 hover:opacity-90 text-white disabled:opacity-50 transition" >
                            {isAdding ? "Adding..." : "Add Member"}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default AddProjectMember;
