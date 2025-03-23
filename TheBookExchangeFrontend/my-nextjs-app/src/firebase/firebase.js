// src/lib/firebase.js

import { initializeApp } from "firebase/app";
import { getAuth, setPersistence, browserLocalPersistence } from "firebase/auth"; // Import persistence
import { getFirestore } from "firebase/firestore";

const firebaseConfig = {
    apiKey: "AIzaSyAFcX91IhTiIEH0JGeqU_MJs1GYK5Mpk4M", // Replace later with env variables
    authDomain: "the-book-exchange-19ff2.firebaseapp.com",
    projectId: "the-book-exchange-19ff2",
    storageBucket: "the-book-exchange-19ff2.firebasestorage.app",
    messagingSenderId: "522962222403",
    appId: "1:522962222403:web:30c5aeb0a19205aa9283f6",
    measurementId: "G-PCGFQDTESC"
};

const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);

// Set persistence to local storage
setPersistence(auth, browserLocalPersistence)
    .then(() => {
        console.log("Firebase persistence set to local storage.");
    })
    .catch((error) => {
        console.error("Error setting Firebase persistence:", error);
    });

export const db = getFirestore(app);