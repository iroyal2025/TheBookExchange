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
        localStorage.removeItem('authToken'); // Or whatever your auth token is called
        router.push('/');
    };

    const handleProfileClick = () => {
        handleTabClick('profile'); // Use the existing tab navigation logic
    };

    return (
        <div className="min-h-screen bg-gradient-to-r from-orange-500 to-green-500 flex flex-col items-center justify-center">
            <div className="bg-white p-10 rounded-2xl shadow-2xl text-center w-full max-w-2xl">
                <h2 className="text-4xl font-bold text-orange-600 mb-6">Parent Dashboard</h2>
                <img src="/Book Exchange side photo.jpg" alt="Book Exchange side photo" className="w-48 mx-auto mb-6" />

                <div className="flex justify-center mb-8 space-x-4">
                    <Button
                        variant={activeTab === 'students' ? 'default' : 'outline'}
                        className={`${activeTab === 'students' ? 'bg-green-600 hover:bg-green-700 text-white' : 'text-orange-600'}`}
                        onClick={() => handleTabClick('students')}
                    >
                        View Students
                    </Button>
                    <Button
                        variant={activeTab === 'profile' ? 'default' : 'outline'}
                        className={`${activeTab === 'profile' ? 'bg-green-600 hover:bg-green-700 text-white' : 'text-blue-600'}`}
                        onClick={handleProfileClick}
                    >
                        Profile
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