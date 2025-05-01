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

    useEffect(() => {
        const storedUser = localStorage.getItem('currentUser');
        const storedUserData = localStorage.getItem('userData');
        console.log('AuthContext: useEffect - storedUser:', storedUser); // Log storedUser
        console.log('AuthContext: useEffect - storedUserData:', storedUserData); // Log storedUserData
        if (storedUser) {
            try {
                setCurrentUser(JSON.parse(storedUser));
            } catch (error) {
                console.error('AuthContext: useEffect - Error parsing storedUser:', error);
                localStorage.removeItem('currentUser'); // Remove invalid data
            }
        }
        if (storedUserData) {
            try {
                setUserData(JSON.parse(storedUserData));
            } catch (error) {
                console.error('AuthContext: useEffect - Error parsing storedUserData:', error);
                localStorage.removeItem('userData'); // Remove invalid data
            }
        }
        setLoading(false);
        console.log('AuthContext: useEffect - loading set to false'); // Log loading state
    }, []);

    const loginWithEmailAndPassword = async (email, password) => {
        setLoading(true);
        setAuthError(null);
        console.log('AuthContext: loginWithEmailAndPassword - email:', email);
        console.log('AuthContext: loginWithEmailAndPassword - loading set to true');

        try {
            const response = await axios.post('http://localhost:8080/auth/verify', null, {
                params: { email, password },
            });

            console.log('AuthContext: loginWithEmailAndPassword - response.data:', response.data);

            const user = {
                email: response.data.email,
                role: response.data.role,
                uid: response.data.id || response.data._id || response.data.uid || response.data.userId,
            };

            setCurrentUser(user);
            setUserData(response.data);
            localStorage.setItem('currentUser', JSON.stringify(user));
            localStorage.setItem('userData', JSON.stringify(response.data));
            setLoading(false);
            console.log('AuthContext: loginWithEmailAndPassword - currentUser set:', user);
            console.log('AuthContext: loginWithEmailAndPassword - userData set:', response.data);
            console.log('AuthContext: loginWithEmailAndPassword - loading set to false');
            return true; // Explicitly return true on success
        } catch (error) {
            console.error('AuthContext: login error:', error);
            setAuthError(error.message || 'Login failed. Please try again.');
            setLoading(false);
            console.log('AuthContext: loginWithEmailAndPassword - loading set to false due to error');
            throw error;
        }
    };
    const logout = () => {
        console.log('AuthContext: logout - initiated'); // Log logout initiation
        setCurrentUser(null);
        setUserData(null);
        localStorage.removeItem('currentUser');
        localStorage.removeItem('userData');
        console.log('AuthContext: logout - currentUser and userData set to null, localStorage cleared'); // Log logout completion
    };

    return (
        <AuthContext.Provider value={{ currentUser, userData, loginWithEmailAndPassword, logout, loading, authError, setUserData, setCurrentUser }}>
            {loading ? <div>Loading...</div> : children}
        </AuthContext.Provider>
    );
};