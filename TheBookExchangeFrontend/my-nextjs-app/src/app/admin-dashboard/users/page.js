'use client';
import React, { useState, useEffect } from 'react';
import axios from '@/lib/axiosConfig';
import { Button } from "@/components/ui/button";
import { Spinner } from "@/components/ui/spinner";
import { getAuth } from "firebase/auth";

export default function BrowseTextbooks() {
    const [textbooks, setTextbooks] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [purchaseLoading, setPurchaseLoading] = useState(false);
    const [purchaseError, setPurchaseError] = useState(null);
    const [message, setMessage] = useState(null);

    useEffect(() => {
        fetchTextbooks();
    }, []);

    useEffect(() => {
        if (message) {
            const timer = setTimeout(() => {
                setMessage(null);
            }, 10000);
            return () => clearTimeout(timer);
        }
    }, [message]);

    const fetchTextbooks = async () => {
        try {
            const response = await axios.get('http://localhost:8080/Books/');
            if (response.data.success) {
                setTextbooks(response.data.data);
                setLoading(false);
            } else {
                setError(response.data.message);
                setLoading(false);
            }
        } catch (err) {
            setError(err.message);
            setLoading(false);
        }
    };

    const handleBuy = async (bookId) => {
        setPurchaseLoading(true);
        setPurchaseError(null);
        try {
            const auth = getAuth();
            const user = auth.currentUser;

            if (user) {
                const userId = user.uid;
                const response = await axios.post(`http://localhost:8080/Books/purchase/${bookId}/${userId}`);

                if (response.data.success) {
                    setMessage(response.data.message);
                    fetchTextbooks(); // Refresh the list after purchase.
                } else {
                    setPurchaseError(response.data.message);
                }
            } else {
                setPurchaseError("User not authenticated.");
            }
        } catch (err) {
            console.error("Purchase error:", err);
            if (err.response && err.response.data && err.response.data.message) {
                setPurchaseError(err.response.data.message);
            } else {
                setPurchaseError("An error occurred during purchase.");
            }
        } finally {
            setPurchaseLoading(false);
        }
    };

    if (loading) return <div className="p-4 flex justify-center items-center"><Spinner /></div>;
    if (error) return <div className="p-4 text-red-600">Error loading textbooks: {error}</div>;

    return (
        <div className="min-h-screen bg-gradient-to-r from-orange-500 to-green-500 flex flex-col items-center justify-center p-4">
            <div className="bg-white p-10 rounded-2xl shadow-2xl w-full max-w-4xl">
                <h2 className="text-2xl font-semibold mb-6 text-orange-600">Browse Textbooks</h2>

                {message && <div className="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded relative mb-4" role="alert">
                    <span className="block sm:inline">{message}</span>
                </div>}

                <div className="overflow-x-auto">
                    <table className="min-w-full border border-gray-200">
                        <thead>
                        <tr className="bg-gray-100">
                            <th className="border border-gray-200 p-2 text-left">Title</th>
                            <th className="border border-gray-200 p-2 text-left">Author</th>
                            <th className="border border-gray-200 p-2 text-left">Price</th>
                            <th className="border border-gray-200 p-2 text-left">Actions</th>
                        </tr>
                        </thead>
                        <tbody>
                        {textbooks.map((book) => (
                            <tr key={book.bookId} className="border-b border-gray-200">
                                <td className="p-2">{book.title}</td>
                                <td className="p-2">{book.author}</td>
                                <td className="p-2">${book.price}</td>
                                <td className="p-2">
                                    <Button onClick={() => handleBuy(book.bookId)} disabled={purchaseLoading}>
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