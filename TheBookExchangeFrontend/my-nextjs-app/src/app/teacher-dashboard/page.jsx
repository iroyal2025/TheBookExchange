'use client';
import React from 'react';

export default function TeacherDashboard() {
    return (
        <div style={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            height: '100vh',
            backgroundColor: '#f0f8f0',
            fontFamily: 'Arial, sans-serif'
        }}>
            <h2 style={{ color: '#28a745', marginBottom: '20px' }}>Teacher Dashboard</h2>
            <img src="/Book Exchange side photo.jpg" alt="Book Exchange side photo" width="200" style={{ marginBottom: '20px' }} />
            <nav>
                <ul style={{ listStyle: 'none', padding: 0 }}>
                    <li style={{ marginBottom: '10px' }}>
                        <a href="/teacher/classes" style={{ color: '#007bff', textDecoration: 'none' }}>Manage Classes</a>
                    </li>
                    <li style={{ marginBottom: '10px' }}>
                        <a href="/teacher/profile" style={{ color: '#007bff', textDecoration: 'none' }}>View Profile</a>
                    </li>
                </ul>
            </nav>
            <p style={{ color: '#333' }}>Welcome to the Teacher Dashboard.</p>
        </div>
    );
}