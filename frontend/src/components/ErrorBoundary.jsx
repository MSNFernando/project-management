import { Component } from 'react';

/**
 * Catches a crash in one page so the rest of the app (sidebar, navbar) keeps working.
 * Layout keys it on the URL, so moving to another page clears the error.
 */
export default class ErrorBoundary extends Component {
    constructor(props) {
        super(props);
        this.state = { error: null };
    }

    static getDerivedStateFromError(error) {
        return { error };
    }

    componentDidCatch(error, info) {
        console.error('Page crashed:', error, info?.componentStack);
    }

    render() {
        if (!this.state.error) return this.props.children;
        return (
            <div role="alert" className="max-w-xl mx-auto mt-20 p-6 rounded-lg border border-red-200 dark:border-red-900 bg-red-50 dark:bg-red-950/40 text-zinc-900 dark:text-zinc-100">
                <h2 className="text-lg font-semibold">This page hit an error</h2>
                <p className="mt-2 text-sm text-zinc-700 dark:text-zinc-300">
                    The rest of SpringPM still works. Try again, or go to another page.
                </p>
                <pre className="mt-3 text-xs whitespace-pre-wrap text-red-800 dark:text-red-300">{String(this.state.error?.message || this.state.error)}</pre>
                <button
                    type="button"
                    onClick={() => this.setState({ error: null })}
                    className="mt-4 px-4 py-2 rounded bg-blue-600 text-white text-sm hover:bg-blue-700 focus:outline-none focus-visible:ring-2 focus-visible:ring-blue-600 focus-visible:ring-offset-2"
                >
                    Try again
                </button>
            </div>
        );
    }
}
