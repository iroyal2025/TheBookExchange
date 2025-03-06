'use client';
import React, { useState } from 'react';
import axios from './axiosConfig'; // Import configured Axios
import { useRouter } from 'next/navigation';

export default function Login() {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const router = useRouter();

    const handleLogin = async (e) => {
        e.preventDefault();
        try {
            const response = await axios.post('http://localhost:8080/auth/login', {
                email,
                password,
            });
            console.log("Login successful:", response.data); // Log the response message
            // localStorage.setItem('token', response.data.token); // Remove token storage
            // localStorage.setItem('role', response.data.role); // Remove role storage
            // console.log("Redirecting to:", `/${response.data.role}-dashboard`); // Remove role-based redirect
            router.push(`/dashboard`); // Redirect to a default dashboard
        } catch (err) {
            setError('Invalid credentials');
            console.error("Login Error: ", err);
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