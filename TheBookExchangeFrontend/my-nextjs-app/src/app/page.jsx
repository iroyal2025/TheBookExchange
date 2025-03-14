'use client';
import React, { useState } from 'react';
import axios from '../lib/axiosConfig';
import { useRouter } from 'next/navigation';

export default function Login() {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const router = useRouter();

    const handleLogin = async (e) => {
        e.preventDefault();
        setError('');

        try {
            // 1. Backend Authentication - Verify Role and isActive
            const response = await axios.post(
                'http://localhost:8080/auth/verify',
                null,
                {
                    params: {
                        email: email,
                        password: password, // Send email and password
                    },
                }
            );

            const role = response.data.role;

            // 2. Role-Based Navigation
            if (role === 'admin') {
                router.push('/admin-dashboard');
            } else if (role === 'teacher') {
                router.push('/teacher-dashboard');
            } else if (role === 'student') {
                router.push('/student-dashboard');
            } else if (role === 'parent') {
                router.push('/parent-dashboard');
            } else {
                router.push('/dashboard');
            }
        } catch (err) {
            if (err.response && err.response.status === 403) {
                setError('Your account is deactivated. Please contact an administrator.');
            } else {
                setError('Invalid credentials or account issue');
            }
            console.error("Login Error: ", err);
            if (err.response) {
                console.error("Backend Error:", err.response.data);
            }
        }
    };

    return (
        <div style={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            height: '100vh'
        }}>
            <img src="/Book Exchange Logo.png" alt="Book Exchange Logo" width="225" style={{ marginBottom: '20px' }} />
            {error && <p style={{ color: 'red' }}>{error}</p>}
            <h2>Login</h2>
            <form onSubmit={handleLogin} style={{ display: 'flex', flexDirection: 'column', width: '300px' }}>
                <input
                    type="email"
                    placeholder="Email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    style={{ padding: '10px', marginBottom: '10px', borderRadius: '5px', border: '1px solid #ccc' }}
                />
                <input
                    type="password"
                    placeholder="Password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    style={{ padding: '10px', marginBottom: '20px', borderRadius: '5px', border: '1px solid #ccc' }}
                />
                <button type="submit" style={{
                    padding: '10px 20px',
                    backgroundColor: '#007bff',
                    color: 'white',
                    border: 'none',
                    borderRadius: '5px',
                    cursor: 'pointer'
                }}>Login
                </button>
            </form>
        </div>
    );
}