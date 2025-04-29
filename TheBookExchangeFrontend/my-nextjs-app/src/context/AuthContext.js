// AuthContext.js
'use client';

import React, { createContext, useState, useEffect } from 'react';
import axios from '../lib/axiosConfig';

export const AuthContext = createContext();

export const AuthContextProvider = ({ children }) => {
    const [currentUser, setCurrentUser] = useState(null);
    const [userData, setUserData] = useState(null);
    const [userId, setUserId] = useState(null); // New state for userId
    const [loading, setLoading] = useState(true);
    const [authError, setAuthError] = useState(null);

    // Load user data from localStorage on app start
    useEffect(() => {
        const storedUser = localStorage.getItem('currentUser');
        const storedUserData = localStorage.getItem('userData');
        const storedUserId = localStorage.getItem('userId'); // Load userId

        if (storedUser) {
            setCurrentUser(JSON.parse(storedUser));
        }

        if (storedUserData) {
            setUserData(JSON.parse(storedUserData));
        }

        if (storedUserId) {
            setUserId(storedUserId); // Set userId from localStorage
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

            console.log('AuthContext: login response data:', response.data); // HERE IS THE ADDED LINE

            const user = {
                email: response.data.email,
                role: response.data.role,
            };
            const fetchedUserId = response.data.id || response.data._id || response.data.uid || response.data.userId; // Assuming your backend returns an 'id' or similar

            setCurrentUser(user);
            setUserData(response.data);
            setUserId(fetchedUserId); // Set userId in state

            localStorage.setItem('currentUser', JSON.stringify(user));
            localStorage.setItem('userData', JSON.stringify(response.data));
            localStorage.setItem('userId', fetchedUserId); // Store userId in localStorage

            console.log('AuthContext: login user:', user);
            console.log('AuthContext: login userData:', response.data);
            console.log('AuthContext: login userId:', fetchedUserId); // Log the userId
            console.log('AuthContext: localStorage currentUser:', localStorage.getItem('currentUser'));
            console.log('AuthContext: localStorage userData:', localStorage.getItem('userData'));
            console.log('AuthContext: localStorage userId:', localStorage.getItem('userId')); // Log localStorage userId

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
        setUserId(null); // Clear userId on logout
        localStorage.removeItem('currentUser');
        localStorage.removeItem('userData');
        localStorage.removeItem('userId'); // Remove userId from localStorage

        console.log('AuthContext: logout');
        console.log('AuthContext: localStorage currentUser:', localStorage.getItem('currentUser'));
        console.log('AuthContext: localStorage userData:', localStorage.getItem('userData'));
        console.log('AuthContext: localStorage userId:', localStorage.getItem('userId')); // Log localStorage userId
    };

    return (
        <AuthContext.Provider value={{ currentUser, userData, userId, loginWithEmailAndPassword, logout, loading, authError, setUserData, setCurrentUser, setUserId }}>
            {loading ? <div>Loading...</div> : children}
        </AuthContext.Provider>
    );
};