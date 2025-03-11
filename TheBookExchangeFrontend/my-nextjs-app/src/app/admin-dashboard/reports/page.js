'use client';
import React from 'react';

export default function ViewReports() {
    return (
        <div className="min-h-screen bg-gradient-to-r from-orange-500 to-green-500 flex items-center justify-center p-4">
            <div className="bg-white p-10 rounded-2xl shadow-2xl w-full max-w-2xl text-center">
                <h2 className="text-2xl font-semibold mb-6 text-orange-600">View Reports</h2>
                <p className="text-gray-700">Reports will be displayed here.</p>
            </div>
        </div>
    );
}