'use client';

import React, { useState, useEffect, useContext } from 'react';
import axios from 'axios';
import { AuthContext } from '../../../context/AuthContext';
import { useRouter } from 'next/navigation';
import { Button } from "@/components/ui/button";

// Define a TypeScript interface for the transaction data for better type safety
interface Transaction {
    title?: string | null;
    author?: string | null;
    price?: number | null;
    // Add other relevant properties of your transaction object
}

// Define a TypeScript interface for the API response
interface ApiResponse<T> {
    success: boolean;
    message?: string | null;
    data?: T;
    error?: string | null;
}

export default function TransactionsPage() {
    const [transactions, setTransactions] = useState<Transaction[]>([]);
    const { currentUser, loading: authLoading } = useContext(AuthContext);
    const [pageLoading, setPageLoading] = useState(true);
    const router = useRouter();
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (!authLoading) {
            setPageLoading(false);
        }
    }, [authLoading]);

    useEffect(() => {
        const fetchTransactions = async () => {
            if (currentUser?.uid) {
                console.log("Fetching Transactions for UID:", currentUser.uid);
                try {
                    const response = await axios.get<ApiResponse<Transaction[]>>(`http://localhost:8080/Transactions/purchased/${currentUser.uid}`);
                    if (response.data?.success) {
                        setTransactions(response.data.data || []);
                    } else {
                        console.log('TransactionsPage: No purchased books found.');
                        setTransactions([]);
                        setError(response.data?.message || 'Failed to fetch purchase history.');
                    }
                    setError(null);
                } catch (error: any) {
                    console.error('TransactionsPage: Error fetching transactions:', error);
                    setError('Failed to fetch purchase history.');
                    setTransactions([]);
                }
            }
        };

        if (!pageLoading && currentUser) {
            fetchTransactions();
        }
    }, [currentUser, pageLoading]);

    if (pageLoading) {
        return <div>Loading purchase history...</div>;
    }

    if (error) {
        return <div>Error: {error}</div>;
    }

    return (
        <div className="min-h-screen bg-gray-100 py-6">
            <div className="max-w-7xl mx-auto sm:px-6 lg:px-8">
                <h2 className="text-2xl font-semibold text-gray-800 mb-4">My Purchase History</h2>
                {transactions.length > 0 ? (
                    <ul className="space-y-4">
                        {transactions.map((transaction, index) => (
                            <li key={index} className="bg-white shadow overflow-hidden rounded-md p-4">
                                <p><strong className="text-gray-600">Title:</strong> {transaction?.title || 'N/A'}</p>
                                <p><strong className="text-gray-600">Author:</strong> {transaction?.author || 'N/A'}</p>
                                <p><strong className="text-gray-600">Price:</strong> {transaction?.price !== null ? `$${transaction.price?.toFixed(2)}` : 'N/A'}</p>
                                {/* You can add more details here if needed */}
                            </li>
                        ))}
                    </ul>
                ) : (
                    <p className="text-gray-600">You haven't made any purchases yet.</p>
                )}
                <div className="mt-6">
                    <Button onClick={() => router.back()} variant="outline">
                        Back to Dashboard
                    </Button>
                </div>
            </div>
        </div>
    );
}