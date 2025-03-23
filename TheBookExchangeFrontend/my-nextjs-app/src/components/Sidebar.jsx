// src/components/Sidebar.jsx

import React from 'react';
import Link from 'next/link';

export default function Sidebar() {
    return (
        <aside className="bg-[#CBE4DE] text-[#2C3333] p-4 w-64">
            <nav>
                <ul>
                    <li className="mb-2">
                        <Link href="/parent-dashboard" className="block p-2 hover:bg-[#A6BEB8]">Dashboard</Link>
                    </li>

                </ul>
            </nav>
        </aside>
    );
}