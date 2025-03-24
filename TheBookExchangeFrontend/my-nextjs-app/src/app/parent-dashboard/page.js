'use client';
import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import { Button } from "@/components/ui/button";

export default function ParentDashboard() {
    const router = useRouter();
    const [activeTab, setActiveTab] = useState('students');

    const handleTabClick = (tab) => {
        setActiveTab(tab);
        router.push(`/parent-dashboard/${tab}`);
    };

    const handleLogout = () => {
        // Clear authentication data (if any)
        localStorage.removeItem('authToken'); // Or whatever your auth token is called

        // Redirect to the login page (root of your app)
        router.push('/');
    };

    return (
        <div className="min-h-screen bg-gradient-to-r from-orange-500 to-green-500 flex flex-col items-center justify-center">
            <div className="bg-white p-10 rounded-2xl shadow-2xl text-center w-full max-w-2xl">
                <h2 className="text-4xl font-bold text-orange-600 mb-6">Parent Dashboard</h2>
                <img src="/Book Exchange side photo.jpg" alt="Book Exchange side photo" className="w-48 mx-auto mb-6" />

                <div className="flex justify-center mb-8">
                    <Button
                        variant={activeTab === 'students' ? 'default' : 'outline'}
                        className={`mr-4 ${activeTab === 'students' ? 'bg-green-600 hover:bg-green-700 text-white' : 'text-orange-600'}`}
                        onClick={() => handleTabClick('students')}
                    >
                        View Students
                    </Button>
                </div>

                <p className="text-gray-700">Welcome to the Parent Dashboard.</p>

                <Button onClick={handleLogout} className="mt-6 bg-red-500 text-white hover:bg-red-600">
                    Logout
                </Button>
            </div>
        </div>
    );
}