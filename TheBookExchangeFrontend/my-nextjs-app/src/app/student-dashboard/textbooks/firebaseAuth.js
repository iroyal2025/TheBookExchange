// firebaseAuth.js (or useFirebaseAuth.js)

import { getAuth, onAuthStateChanged } from "firebase/auth";
import { initializeApp } from "firebase/app";
import { getAnalytics } from "firebase/analytics"; // Import getAnalytics
import { useState, useEffect } from "react";

// Your web app's Firebase configuration
const firebaseConfig = {
    apiKey: "AIzaSyAFcX91IhTiIEH0JGeqU_MJs1GYK5Mpk4M",
    authDomain: "the-book-exchange-19ff2.firebaseapp.com",
    databaseURL: "https://the-book-exchange-19ff2-default-rtdb.firebaseio.com",
    projectId: "the-book-exchange-19ff2",
    storageBucket: "the-book-exchange-19ff2.firebasestorage.app",
    messagingSenderId: "522962222403",
    appId: "1:522962222403:web:30c5aeb0a19205aa9283f6",
    measurementId: "G-PCGFQDTESC"
};

let app;
let analytics; // Declare analytics

export const useFirebaseAuth = () => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (typeof window !== 'undefined' && !app) {
            app = initializeApp(firebaseConfig);
            analytics = getAnalytics(app); // Initialize analytics
        }
        if (app) {
            const auth = getAuth(app);
            const unsubscribe = onAuthStateChanged(auth, (authUser) => {
                setUser(authUser);
                setLoading(false);
            });

            return () => unsubscribe();
        } else {
            setLoading(false);
        }

    }, []);

    return { user, loading };
};