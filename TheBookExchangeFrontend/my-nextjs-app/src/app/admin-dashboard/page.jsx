'use client';
import React from 'react';

export default function AdminDashboard() {
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
            <h2 style={{ color: '#28a745', marginBottom: '20px' }}>Admin Dashboard</h2>
            <img src="/Book Exchange side photo.jpg" alt="Book Exchange side photo" width="200" style={{ marginBottom: '20px' }} />
            <nav>
                <ul style={{ listStyle: 'none', padding: 0 }}>
                    <li style={{ marginBottom: '10px' }}>
                        <a href="/admin-dashboard/users" style={{ color: '#007bff', textDecoration: 'none' }}>Manage Users</a>
                    </li>
                    <li style={{ marginBottom: '10px' }}>
                        <a href="/admin-dashboard/books" style={{ color: '#007bff', textDecoration: 'none' }}>Manage Books</a>
                    </li>
                    <li style={{ marginBottom: '10px' }}>
                        <a href="/admin-dashboard/reports" style={{ color: '#007bff', textDecoration: 'none' }}>View Reports</a>
                    </li>
                </ul>
            </nav>
            <p style={{ color: '#333' }}>Welcome to the Admin Dashboard.</p>
        </div>
    );
}