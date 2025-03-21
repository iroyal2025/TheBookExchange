'use client';
import React, { useState, useEffect } from 'react';
import axios from '@/lib/axiosConfig';
import { Spinner } from "@/components/ui/spinner";
import { useFirebaseAuth } from './firebaseAuth';

export default function MyBooks() {
    const [ownedBooks, setOwnedBooks] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const { user, loading: authLoading } = useFirebaseAuth();

    useEffect(() => {
        const fetchOwnedBooks = async () => {
            try {
                if (user) {
                    const userId = user.uid;
                    const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080";
                    const response = await axios.get(`${API_BASE_URL}/books/owned/${userId}`);

                    if (response.data.success) {
                        setOwnedBooks(response.data.data);
                        setLoading(false);
                    } else {
                        setError(response.data.message);
                        setLoading(false);
                    }
                } else {
                    // User not authenticated, do not fetch
                    setLoading(false); // Set loading to false
                }
            } catch (err) {
                console.error("Error fetching owned books:", err);
                setError(err.message || "An unexpected error occurred.");
                setLoading(false);
            }
        };

        if (!authLoading) {
            fetchOwnedBooks();
        }
    }, [user, authLoading]);

    if (loading || authLoading) return <div className="p-4 flex justify-center items-center"><Spinner /></div>;
    if (error) return <div className="p-4 text-red-600" aria-live="polite">{error}</div>;

    return (
        <div className="min-h-screen bg-gradient-to-r from-orange-500 to-green-500 flex flex-col items-center justify-center p-4">
            <div className="bg-white p-10 rounded-2xl shadow-2xl w-full max-w-4xl">
                <h2 className="text-2xl font-semibold mb-6 text-orange-600">My Books</h2>
                {ownedBooks.length > 0 ? (
                    <ul className="list-disc list-inside">
                        {ownedBooks.map((book) => (
                            <li key={book.bookId} className="mb-2">
                                <strong>{book.title}</strong> by {book.author} (Edition: {book.edition}, ISBN: {book.ISBN})
                            </li>
                        ))}
                    </ul>
                ) : (
                    <p className="text-gray-600 italic">
                        You do not have any purchased books.
                    </p>
                )}
            </div>
        </div>
    );
}