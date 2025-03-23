// src/components/MainLayout.js

import React from 'react';
import Header from './Header'; // Assuming you have a Header component
import Sidebar from './Sidebar'; // Assuming you have a Sidebar component
import Footer from './Footer'; // Assuming you have a Footer component

export default function MainLayout({ children }) {
    return (
        <div className="flex flex-col min-h-screen">
            <Header />
            <div className="flex flex-1">
                <Sidebar />
                <main className="flex-1 p-4">
                    {children}
                </main>
            </div>
            <Footer />
        </div>
    );
}