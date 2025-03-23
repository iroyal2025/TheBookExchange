// src/components/Header.jsx

import React from 'react';
import Link from 'next/link';

export default function Header() {
    return (
        <header className="bg-[#2E4F4F] text-[#CBE4DE] p-4 flex justify-between items-center">
            <Link href="/parent-dashboard" className="text-xl font-semibold">
                Parent Dashboard
            </Link>
            <nav>
                <ul className="flex space-x-4">
                    <li>
                        <Link href="/parent-dashboard/students">Students</Link>
                    </li>
                    {/* Add more links as needed */}
                </ul>
            </nav>
        </header>
    );
}