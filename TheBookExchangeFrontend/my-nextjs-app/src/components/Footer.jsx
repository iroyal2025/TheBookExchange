// src/components/Footer.jsx

import React from 'react';

export default function Footer() {
    return (
        <footer className="bg-[#2E4F4F] text-[#CBE4DE] p-4 text-center">
            <p>&copy; {new Date().getFullYear()} The Book Exchange </p>
        </footer>
    );
}