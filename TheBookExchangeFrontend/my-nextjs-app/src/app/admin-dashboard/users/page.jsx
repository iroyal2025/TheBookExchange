'use client';
import React, { useState, useEffect } from 'react';
import axios from 'axios';

export default function ManageUsers() {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchUsers = async () => {
            try {
                console.log("Fetching users from http://localhost:8080/Users/");
                const response = await axios.get('http://localhost:8080/Users/');
                console.log("Users fetched:", response.data);
                setUsers(response.data.data);
                setLoading(false);
            } catch (error) {
                console.error('Error fetching users:', error);
                if (axios.isAxiosError(error)) {
                    console.error('Axios Error Details:', error.toJSON());
                }
                setLoading(false);
            }
        };
        fetchUsers();
    }, []);

    if (loading) return <div>Loading users...</div>;

    return (
        <div style={{ padding: '20px' }}>
            <h2>Manage Users</h2>
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                <thead>
                <tr>
                    <th style={{ border: '1px solid #ddd', padding: '8px' }}>Email</th>
                    <th style={{ border: '1px solid #ddd', padding: '8px' }}>Role</th>
                    <th style={{ border: '1px solid #ddd', padding: '8px' }}>Actions</th>
                </tr>
                </thead>
                <tbody>
                {users.map((user) => (
                    <tr key={user.userId.id}>
                        <td style={{ border: '1px solid #ddd', padding: '8px' }}>{user.email}</td>
                        <td style={{ border: '1px solid #ddd', padding: '8px' }}>{user.role}</td>
                        <td style={{ border: '1px solid #ddd', padding: '8px' }}>
                            <button style={{ marginRight: '5px' }}>Edit</button>
                            <button>Delete</button>
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
}