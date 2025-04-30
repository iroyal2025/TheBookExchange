'use client';
import React, { useState, useEffect } from 'react';
import { Button } from "@/components/ui/button";
import { Spinner } from "@/components/ui/spinner";
import { useRouter } from 'next/navigation';
import { toast } from 'react-hot-toast';

const getLoggedInUserId = () => {
    // Replace this with your actual logic to retrieve the user ID
    return localStorage.getItem('userId'); // Example using local storage
};

const AdminNotificationsPage = () => {
    const [notifications, setNotifications] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const router = useRouter();

    const fetchNotifications = async () => {
        setLoading(true);
        setError(null);
        const userId = getLoggedInUserId(); // Get the user ID
        if (!userId) {
            setError('User ID not found. Please log in again.');
            setLoading(false);
            return;
        }
        try {
            const response = await fetch(`http://localhost:8080/Notifications/admin?userId=${userId}`); // Include userId as a query parameter
            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Failed to fetch notifications'); // Generalize error message
            }
            const data = await response.json();
            if (data.success) {
                setNotifications(data.data);
            } else {
                setError(data.message);
            }
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchNotifications();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const deleteNotification = async (notificationId) => {
        if (window.confirm('Are you sure you want to delete this notification?')) {
            try {
                const response = await fetch(`http://localhost:8080/Notifications/byMessage?message=${notifications.find(n => n.notificationId === notificationId)?.message}`, {
                    method: 'DELETE',
                });
                if (!response.ok) {
                    const errorData = await response.json();
                    throw new Error(errorData.message || 'Failed to delete notification');
                }
                const data = await response.json();
                if (data.success) {
                    setNotifications(notifications.filter(n => n.notificationId !== notificationId));
                    toast.success('Notification deleted successfully!');
                } else {
                    toast.error(data.message || 'Failed to delete notification.');
                }
            } catch (err) {
                setError(err.message);
                toast.error('Failed to delete notification.');
            }
        }
    };

    const handleNotificationClick = (link) => {
        if (link) {
            router.push(link);
        }
    };

    const handleBackToDashboard = () => {
        router.push('/admin-dashboard');
    };

    if (loading) return <div className="p-4 flex justify-center items-center"><Spinner /></div>;
    if (error) return <div className="p-4 text-red-600">Error loading notifications: {error}</div>;
    if (notifications.length === 0) return (
        <div className="p-6 bg-white rounded-md shadow-md">
            <h2 className="text-xl font-semibold mb-4 text-blue-600">Your Notifications</h2> {/* Updated heading */}
            <p>No new notifications.</p> {/* Updated message */}
            <Button onClick={handleBackToDashboard} variant="outline" className="mt-4">
                Back to Dashboard
            </Button>
        </div>
    );

    return (
        <div className="min-h-screen bg-gradient-to-r from-purple-500 to-indigo-500 flex flex-col items-center justify-center p-4">
            <div className="bg-white p-10 rounded-2xl shadow-2xl w-full max-w-4xl">
                <div className="flex justify-between items-center mb-6">
                    <h2 className="text-2xl font-semibold text-indigo-600">Your Notifications</h2> {/* Updated heading */}
                    <Button onClick={handleBackToDashboard} variant="outline">
                        Back to Dashboard
                    </Button>
                </div>

                <div className="overflow-x-auto">
                    <table className="min-w-full border border-gray-200">
                        <thead>
                        <tr>
                            <th className="p-2 text-left">Type</th>
                            <th className="p-2 text-left">Message</th>
                            <th className="p-2 text-left">Timestamp</th>
                            <th className="p-2 text-left">Actions</th>
                        </tr>
                        </thead>
                        <tbody>
                        {notifications.map(notification => (
                            <tr
                                key={notification.notificationId}
                                className={`border-b border-gray-200 ${notification.isRead ? 'opacity-50' : ''}
                                                ${notification.type === 'user_deleted' ? 'bg-red-100' : ''}
                                                ${notification.type === 'user_edited' ? 'bg-yellow-100' : ''}
                                                ${notification.type === 'user_activated' ? 'bg-green-100' : ''}
                                                ${notification.type === 'user_deactivated' ? 'bg-orange-100' : ''}
                                                ${notification.type === 'user_added' ? 'bg-blue-100' : ''}`}
                            >
                                <td className="p-2">{notification.type.replace(/_/g, ' ').toUpperCase()}</td>
                                <td className="p-2 cursor-pointer underline text-blue-600" onClick={() => handleNotificationClick(notification.link)}>
                                    {notification.message}
                                </td>
                                <td className="p-2">{new Date(notification.timestamp * 1000).toLocaleString()}</td>
                                <td className="p-2">
                                    <Button
                                        onClick={() => deleteNotification(notification.notificationId)}
                                        className="bg-red-500 text-white rounded-md text-sm hover:bg-red-600 focus:outline-none mr-2"
                                        size="sm"
                                    >
                                        Delete
                                    </Button>
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
};

export default AdminNotificationsPage;