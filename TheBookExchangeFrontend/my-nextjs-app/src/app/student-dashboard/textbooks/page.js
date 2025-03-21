'use client';
import React, { useState, useEffect } from 'react';
import axios from '@/lib/axiosConfig';
import { Button } from "@/components/ui/button";
import { Spinner } from "@/components/ui/spinner";
import { useFirebaseAuth } from './firebaseAuth';

export default function BrowseTextbooks() {
    const [books, setBooks] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [purchaseLoading, setPurchaseLoading] = useState(false);
    const [purchaseError, setPurchaseError] = useState(null);
    const [message, setMessage] = useState(null);
    const [userBalance, setUserBalance] = useState(0);

    const { user, loading: authLoading } = useFirebaseAuth();

    useEffect(() => {
        const fetchBooks = async () => {
            try {
                const response = await axios.get('http://localhost:8080/Books/');
                if (response.data && response.data.success && Array.isArray(response.data.data)) {
                    setBooks(response.data.data);
                    setLoading(false);
                } else {
                    setError(response.data.message || "Invalid API response");
                    setLoading(false);
                }
            } catch (err) {
                setError(err.message);
                setLoading(false);
            }
        };

        const fetchUserBalance = async () => {
            if (user) {
                console.log("useEffect: user =", user);
                console.log("useEffect: typeof user =", typeof user);
                console.log("useEffect: Boolean(user) =", Boolean(user));
                try {
                    const response = await axios.get(`http://localhost:8080/users/${user.uid}/balance`);
                    if (response.data && response.data.success) {
                        setUserBalance(response.data.data);
                    } else {
                        console.error('Failed to fetch user balance:', response.data.message);
                    }
                } catch (err) {
                    console.error('Error fetching user balance:', err);
                }
            }
        };

        if (!authLoading) {
            console.log("authLoading state: ", authLoading);
            fetchBooks();
            fetchUserBalance();
        }
    }, [authLoading, user]);

    useEffect(() => {
        if (message) {
            const timer = setTimeout(() => {
                setMessage(null);
            }, 10000);
            return () => clearTimeout(timer);
        }
    }, [message]);

    const handleBuy = async (bookId, price) => {
        setPurchaseLoading(true);
        setPurchaseError(null);
        try {
            if (user) {
                if (userBalance >= price) {
                    const userId = user.uid;
                    const response = await axios.put(`http://localhost:8080/Books/${bookId}/purchase/${userId}`);

                    if (response.data.success) {
                        setMessage(response.data.message);
                        fetchBooks();
                        fetchUserBalance();
                    } else {
                        setPurchaseError(response.data.message);
                    }
                } else {
                    setPurchaseError('Insufficient funds.');
                }
            } else {
                setPurchaseError('An error occurred. Please refresh and try again.');
            }
        } catch (err) {
            console.error('Purchase error:', err);
            if (err.response && err.response.data && err.response.data.message) {
                setPurchaseError(err.response.data.message);
            } else {
                setPurchaseError('An error occurred during purchase.');
            }
        } finally {
            setPurchaseLoading(false);
        }
    };

    if (loading || authLoading) return <div className="p-4 flex justify-center items-center"><Spinner /></div>;
    if (error) return <div className="p-4 text-red-600">Error loading textbooks: {error}</div>;

    console.log("Rendering: user =", user);
    console.log("Rendering: typeof user =", typeof user);
    console.log("Rendering: Boolean(user) =", Boolean(user));
    console.log("The return statement is running");

    return (
        <div className="min-h-screen bg-gradient-to-r from-orange-500 to-green-500 flex flex-col items-center justify-center p-4">
            <div className="bg-white p-10 rounded-2xl shadow-2xl w-full max-w-4xl">
                <div className="flex justify-between items-center mb-6">
                    <h2 className="text-2xl font-semibold text-orange-600">Browse Textbooks</h2>
                    {user && <p style={{color: 'red', fontSize: '20px'}}>Your Balance: $350</p>}
                </div>

                {message && (
                    <div className="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded relative mb-4" role="alert">
                        <span className="block sm:inline">{message}</span>
                    </div>
                )}

                <div className="overflow-x-auto">
                    <table className="min-w-full border border-gray-200">
                        <thead>
                        <tr>
                            <th className="p-2 text-left">Title</th>
                            <th className="p-2 text-left">Author</th>
                            <th className="p-2 text-left">Price</th>
                            <th className="p-2 text-left">Action</th>
                        </tr>
                        </thead>
                        <tbody>
                        {books.map((book) => (
                            <tr key={book.bookId} className="border-b border-gray-200">
                                <td className="p-2">{book.title}</td>
                                <td className="p-2">{book.author}</td>
                                <td className="p-2">${book.price}</td>
                                <td className="p-2">
                                    <Button
                                        onClick={() => handleBuy(book.bookId, book.price)}
                                        disabled={purchaseLoading || !user}
                                    >
                                        {purchaseLoading ? <Spinner size="sm" /> : "Buy"}
                                    </Button>
                                    {purchaseError && <p className="text-red-500 mt-1">{purchaseError}</p>}
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
}