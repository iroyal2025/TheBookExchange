'use client';
import React, { useState, useEffect, useContext } from 'react';
import { Button } from "@/components/ui/button";
import { Spinner } from "@/components/ui/spinner";
import { useRouter } from 'next/navigation';
import { toast } from 'react-hot-toast';
import { AuthContext } from '../../../context/AuthContext'; // Ensure the path to your AuthContext is correct

const NotificationsPage = () => {
    const [notifications, setNotifications] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const { userId } = useContext(AuthContext); // Get userId from context
    const router = useRouter();

    console.log("NotificationsPage: Retrieved userId from AuthContext:", userId);

    const fetchNotifications = async () => {
        setLoading(true);
        setError(null);
        try {
            if (!userId) {
                console.warn("NotificationsPage: userId from context is not available when fetching.");
                setError('User ID not found. Please ensure you are logged in.');
                return;
            }
            const response = await fetch(`http://localhost:8080/Notifications/?userId=${userId}`);
            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Failed to fetch notifications');
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
        if (userId) {
            fetchNotifications();
        } else {
            console.warn("NotificationsPage: userId from context is not available on component mount.");
            setError('User ID not found. Please ensure you are logged in.');
            setLoading(false);
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [userId]);

    const deleteNotification = async (message) => {
        try {
            const response = await fetch(`http://localhost:8080/Notifications/byMessage?message=${encodeURIComponent(message)}`, {
                method: 'DELETE',
            });
            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Failed to delete notification');
            }
            const data = await response.json();
            if (data.success) {
                setNotifications(notifications.filter(n => n.message !== message));
                toast.success('Notification deleted successfully!');
            } else {
                toast.error(data.message || 'Failed to delete notification.');
            }
        } catch (err) {
            setError(err.message);
            toast.error('Failed to delete notification.');
        }
    };

    const handleNotificationClick = (link) => {
        if (link) {
            router.push(link);
        }
    };

    const handleBackToDashboard = () => {
        router.push('/student-dashboard');
    };

    if (loading) return <div className="p-4 flex justify-center items-center"><Spinner /></div>;
    if (error) return <div className="p-4 text-red-600">Error loading notifications: {error}</div>;
    if (notifications.length === 0) return <div className="p-6 bg-white rounded-md shadow-md">
        <h2 className="text-xl font-semibold mb-4 text-blue-600">Notifications</h2>
        <p>No new notifications.</p>
        <Button onClick={handleBackToDashboard} variant="outline" className="mt-4">
            Back to Dashboard
        </Button>
    </div>;

    return (
        <div className="min-h-screen bg-gradient-to-r from-orange-500 to-green-500 flex flex-col items-center justify-center p-4">
            <div className="bg-white p-10 rounded-2xl shadow-2xl w-full max-w-4xl">
                <div className="flex justify-between items-center mb-6">
                    <h2 className="text-2xl font-semibold text-blue-600">Notifications</h2>
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
                                            ${notification.type === 'new_book_added' ? 'bg-yellow-100' : ''}
                                            ${notification.type === 'book_deleted' ? 'bg-red-100' : ''}
                                            ${notification.type === 'book_updated' ? 'bg-blue-100' : ''}
                                            ${notification.type === 'book_purchase' ? 'bg-green-100' : ''}
                                            ${notification.type === 'transaction_update' ? 'bg-purple-100' : ''}`}
                            >
                                <td className="p-2">{notification.type.replace(/_/g, ' ').toUpperCase()}</td>
                                <td className="p-2 cursor-pointer underline text-blue-600" onClick={() => handleNotificationClick(notification.link)}>
                                    {notification.message}
                                </td>
                                <td className="p-2">{new Date(notification.timestamp * 1000).toLocaleString()}</td>
                                <td className="p-2">
                                    <Button
                                        onClick={() => deleteNotification(notification.message)}
                                        className="bg-red-500 text-white rounded-md text-sm hover:bg-red-600 focus:outline-none"
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

export default NotificationsPage;