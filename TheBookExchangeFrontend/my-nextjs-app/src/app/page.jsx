'use client';
import React, { useState, useContext } from 'react';
import axios from '../lib/axiosConfig';
import { useRouter } from 'next/navigation';
import { AuthContext } from '../context/AuthContext'; // Import AuthContext

export default function Login() {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [role, setRole] = useState('student'); // Default role
    const [major, setMajor] = useState('');
    const [profilePicture, setProfilePicture] = useState('');
    const [error, setError] = useState('');
    const [successMessage, setSuccessMessage] = useState(''); // Add success message state
    const router = useRouter();
    const { loginWithEmailAndPassword } = useContext(AuthContext); // Access login function
    const [isCreatingAccount, setIsCreatingAccount] = useState(false); // Track account creation state

    const handleLogin = async (e) => {
        if (e) {
            e.preventDefault();
        }
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

            // 2. Update AuthContext
            await loginWithEmailAndPassword(email, password);

            const role = response.data.role;

            // 3. Role-Based Navigation
            if (role === 'admin') {
                router.push('/admin-dashboard');
            } else if (role === 'teacher') {
                router.push('/teacher-dashboard');
            } else if (role === 'student') {
                router.push('/student-dashboard');
            } else if (role === 'parent') {
                router.push('/parent-dashboard');
            } else if (role === 'seller') {
                router.push('/seller-dashboard');
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

    const handleCreateAccount = async (e) => {
        e.preventDefault();
        setError('');

        try {
            const response = await axios.post('http://localhost:8080/Users/add', {
                email,
                password,
                role,
                major,
                profilePicture,
            });

            if (response.data.success) {
                setSuccessMessage('Account created successfully! Please log in.'); // Updated success message
                setIsCreatingAccount(false); // Go back to the login form
                // No need to call handleLogin immediately
            } else {
                setError(response.data.message);
            }
        } catch (err) {
            setError('Failed to create account. Please try again.');
            console.error("Create Account Error: ", err);
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
            {successMessage && <p style={{ color: 'green' }}>{successMessage}</p>} {/* Display success message */}

            {isCreatingAccount ? (
                <div>
                    <h2 style={{ textAlign: 'center' }}>Create Account</h2>
                    <form onSubmit={handleCreateAccount} style={{ display: 'flex', flexDirection: 'column', width: '300px' }}>
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
                            style={{ padding: '10px', marginBottom: '10px', borderRadius: '5px', border: '1px solid #ccc' }}
                        />
                        <select
                            value={role}
                            onChange={(e) => setRole(e.target.value)}
                            style={{ padding: '10px', marginBottom: '10px', borderRadius: '5px', border: '1px solid #ccc' }}
                        >
                            <option value="student">Student</option>
                            <option value="parent">Parent</option>
                            <option value="teacher">Teacher</option>
                            <option value="seller">Seller</option> {/* Added Seller option */}
                        </select>
                        <input
                            type="text"
                            placeholder="Major"
                            value={major}
                            onChange={(e) => setMajor(e.target.value)}
                            style={{ padding: '10px', marginBottom: '10px', borderRadius: '5px', border: '1px solid #ccc' }}
                        />
                        <input
                            type="text"
                            placeholder="Profile Picture URL"
                            value={profilePicture}
                            onChange={(e) => setProfilePicture(e.target.value)}
                            style={{ padding: '10px', marginBottom: '20px', borderRadius: '5px', border: '1px solid #ccc' }}
                        />
                        <button type="submit" style={{
                            padding: '10px 20px',
                            backgroundColor: '#28a745',
                            color: 'white',
                            border: 'none',
                            borderRadius: '5px',
                            cursor: 'pointer'
                        }}>Create Account
                        </button>
                        <button type="button" onClick={() => setIsCreatingAccount(false)} style={{
                            padding: '10px 20px',
                            backgroundColor: '#6c757d',
                            color: 'white',
                            border: 'none',
                            borderRadius: '5px',
                            cursor: 'pointer',
                            marginTop: '10px'
                        }}>Cancel
                        </button>
                    </form>
                </div>
            ) : (
                // ... (rest of your login form)
                <div>
                    <h2 style={{ textAlign: 'center' }}>Login</h2>
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
                    <button type="button" onClick={() => setIsCreatingAccount(true)} style={{
                        padding: '10px 20px',
                        backgroundColor: '#17a2b8',
                        color: 'white',
                        border: 'none',
                        borderRadius: '5px',
                        cursor: 'pointer',
                        marginTop: '10px',
                        width: '300px'
                    }}>Create Account
                    </button>
                </div>
            )}
        </div>
    );
}