'use client';
import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import { Button } from "@/components/ui/button";

export default function ParentDashboard() {
    const router = useRouter();
    const [activeTab, setActiveTab] = useState('children');

    const handleTabClick = (tab) => {
        setActiveTab(tab);
        router.push(`/parent/${tab}`);
    };

    return (
        <div className="min-h-screen bg-gradient-to-r from-orange-500 to-green-500 flex flex-col items-center justify-center">
            <div className="bg-white p-10 rounded-2xl shadow-2xl text-center w-full max-w-2xl">
                <h2 className="text-4xl font-bold text-orange-600 mb-6">Parent Dashboard</h2>
                <img src="/Book Exchange side photo.jpg" alt="Book Exchange side photo" className="w-48 mx-auto mb-6" />

                <div className="flex justify-center mb-8">
                    <Button
                        variant={activeTab === 'children' ? 'default' : 'outline'}
                        className={`mr-4 ${activeTab === 'children' ? 'bg-green-600 hover:bg-green-700 text-white' : 'text-orange-600'}`}
                        onClick={() => handleTabClick('children')}
                    >
                        View Children
                    </Button>
                    <Button
                        variant={activeTab === 'profile' ? 'default' : 'outline'}
                        className={`${activeTab === 'profile' ? 'bg-green-600 hover:bg-green-700 text-white' : 'text-orange-600'}`}
                        onClick={() => handleTabClick('profile')}
                    >
                        View Profile
                    </Button>
                </div>

                <p className="text-gray-700">Welcome to the Parent Dashboard.</p>
            </div>
        </div>
    );
}