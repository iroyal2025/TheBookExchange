// AuthContext.js
'use client';

import React, { createContext, useState, useEffect } from 'react';
import axios from '../lib/axiosConfig';

export const AuthContext = createContext();

export const AuthContextProvider = ({ children }) => {
    const [currentUser, setCurrentUser] = useState(null);
    const [userData, setUserData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [authError, setAuthError] = useState(null);

    // Load user from localStorage on app start
    useEffect(() => {
        const storedUser = localStorage.getItem('currentUser');
        const storedUserData = localStorage.getItem('userData');

        if (storedUser) {
            setCurrentUser(JSON.parse(storedUser));
        }

        if (storedUserData) {
            setUserData(JSON.parse(storedUserData));
        }

        setLoading(false);
    }, []);

    const loginWithEmailAndPassword = async (email, password) => {
        setLoading(true);
        setAuthError(null);

        try {
            const response = await axios.post('http://localhost:8080/auth/verify', null, {
                params: {
                    email,
                    password,
                },
            });

            const user = {
                email: response.data.email,
                role: response.data.role,
            };

            setCurrentUser(user);
            setUserData(response.data);

            localStorage.setItem('currentUser', JSON.stringify(user));
            localStorage.setItem('userData', JSON.stringify(response.data));

            console.log('AuthContext: login user:', user);
            console.log('AuthContext: login userData:', response.data);
            console.log('AuthContext: localStorage currentUser:', localStorage.getItem('currentUser'));
            console.log('AuthContext: localStorage userData:', localStorage.getItem('userData'));

            setLoading(false);
        } catch (error) {
            console.error('AuthContext: login error:', error);
            setAuthError(error.message || 'Login failed. Please try again.');
            setLoading(false);
            throw error;
        }
    };

    const logout = () => {
        setCurrentUser(null);
        setUserData(null);
        localStorage.removeItem('currentUser');
        localStorage.removeItem('userData');

        console.log('AuthContext: logout');
        console.log('AuthContext: localStorage currentUser:', localStorage.getItem('currentUser'));
        console.log('AuthContext: localStorage userData:', localStorage.getItem('userData'));
    };

    return (
        <AuthContext.Provider value={{ currentUser, userData, loginWithEmailAndPassword, logout, loading, authError, setUserData, setCurrentUser }}>
            {loading ? <div>Loading...</div> : children}
        </AuthContext.Provider>
    );
};