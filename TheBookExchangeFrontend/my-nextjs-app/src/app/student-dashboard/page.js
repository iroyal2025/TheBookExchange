'use client';
import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { Button } from "@/components/ui/button";
import Link from 'next/link'; // Import Link for navigation

export default function StudentDashboard() {
    const router = useRouter();
    const [activeTab, setActiveTab] = useState('textbooks');

    const handleTabClick = (tab) => {
        setActiveTab(tab);
        router.push(`/student-dashboard/${tab}`);
    };

    const handleLogout = () => {
        // Clear authentication data (if any)
        localStorage.removeItem('authToken'); // Or whatever your auth token is called

        // Redirect to the login page (root of your app)
        router.push('/');
    };

    const handleProfileClick = () => {
        handleTabClick('profile'); // Use the existing tab navigation logic
    };

    const handleNotificationsClick = () => {
        router.push('/student-dashboard/notifications');
    };

    const handleTransactionsClick = () => {
        router.push('/student-dashboard/transactions');
    };

    const handleWishlistClick = () => {
        router.push('/student-dashboard/wishlist');// Navigate to the wishlist page
    };

    return (
        <div className="min-h-screen bg-gradient-to-r from-orange-500 to-green-500 flex flex-col items-center justify-center">
            <div className="bg-white p-10 rounded-2xl shadow-2xl text-center w-full max-w-4xl">
                <h2 className="text-4xl font-bold text-orange-600 mb-6">Student Dashboard</h2>
                <img src="/Book Exchange side photo.jpg" alt="Book Exchange side photo" className="w-48 mx-auto mb-6" />

                <div className="flex justify-center mb-8 space-x-4">
                    <Button
                        variant={activeTab === 'textbooks' ? 'default' : 'outline'}
                        className={`mr-4 ${activeTab === 'textbooks' ? 'bg-green-600 hover:bg-green-700 text-white' : 'text-orange-600'}`}
                        onClick={() => handleTabClick('textbooks')}
                    >
                        Browse Textbooks
                    </Button>
                    <Button
                        variant={activeTab === 'mybooks' ? 'default' : 'outline'}
                        className={`mr-4 ${activeTab === 'mybooks' ? 'bg-green-600 hover:bg-green-700 text-white' : 'text-orange-600'}`}
                        onClick={() => handleTabClick('mybooks')}
                    >
                        My Books
                    </Button>
                    <Button
                        variant={activeTab === 'profile' ? 'default' : 'outline'}
                        className={`mr-4 ${activeTab === 'profile' ? 'bg-green-600 hover:bg-green-700 text-white' : 'text-blue-600'}`}
                        onClick={handleProfileClick}
                    >
                        Profile
                    </Button>
                    <Button
                        onClick={handleNotificationsClick}
                        className="bg-blue-500 text-white hover:bg-blue-600"
                    >
                        Notifications
                    </Button>
                    <Button
                        onClick={handleTransactionsClick}
                        className="bg-purple-500 text-white hover:bg-purple-600"
                    >
                        Transactions
                    </Button>
                    <Button
                        onClick={handleWishlistClick}
                        className="bg-yellow-500 text-white hover:bg-yellow-600"
                    >
                        Wishlist
                    </Button>
                </div>
                <Button onClick={handleLogout} className="mt-6 bg-red-500 text-white hover:bg-red-600">
                    Logout
                </Button>
                {/* Content will be rendered by the routes */}
            </div>
        </div>
    );
}