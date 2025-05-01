'use client';
import React, { useState, useEffect, useContext } from 'react';
import axios from '@/lib/axiosConfig';
import { Button } from "@/components/ui/button";
import { Spinner } from "@/components/ui/spinner";
import { AuthContext } from '../../../../context/AuthContext';
import { useRouter, useSearchParams } from 'next/navigation';
import MainLayout from '@/components/MainLayout';

export default function Transactions() {
    const [transactions, setTransactions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [transactionLoading, setTransactionLoading] = useState(false);
    const { currentUser } = useContext(AuthContext);
    const searchParams = useSearchParams();
    const studentEmail = searchParams.get('studentEmail');

    useEffect(() => {
        if (!studentEmail) {
            setError("No student email provided.");
            setLoading(false);
            return;
        }

        const fetchTransactions = async () => {
            setTransactionLoading(true);
            setError(null);
            try {
                const response = await axios.get(`http://localhost:8080/Transactions/student/email/${studentEmail}`);

                if (response.data && Array.isArray(response.data)) {
                    setTransactions(response.data);
                } else {
                    setError("No transactions found or invalid data structure.");
                }
            } catch (err) {
                setError('Error fetching transactions: ' + err.message);
            } finally {
                setTransactionLoading(false);
            }
        };

        fetchTransactions();
    }, [studentEmail]);

    if (loading) return <div className="p-4 flex justify-center items-center"><Spinner /></div>;
    if (error) return <div className="p-4 text-red-600">Error: {error}</div>;

    return (
        <MainLayout>
            <div className="p-6 space-y-4">
                <h2 className="text-2xl font-semibold">Transactions for {studentEmail}</h2>

                {transactionLoading ? (
                    <div className="flex justify-center items-center">
                        <Spinner />
                    </div>
                ) : (
                    <div className="overflow-x-auto">
                        <table className="min-w-full border border-gray-200">
                            <thead>
                            <tr>
                                <th className="p-2 text-left">Transaction ID</th>
                                <th className="p-2 text-left">Book Title</th>
                                <th className="p-2 text-left">Order Status</th>
                                <th className="p-2 text-left">Transaction Date</th>
                            </tr>
                            </thead>
                            <tbody>
                            {transactions.length > 0 ? (
                                transactions.map((transaction, index) => {
                                    const bookDetails = transaction.bookDetails || {};
                                    const bookTitle = bookDetails.title || 'No Title Available';
                                    const transactionDate = new Date(transaction.date).toLocaleDateString();

                                    return (
                                        <tr key={index} className="border-b border-gray-200">
                                            <td className="p-2">{transaction.bookId}</td>
                                            <td className="p-2">{bookTitle}</td>
                                            <td className="p-2">{transaction.orderStatus}</td>
                                            <td className="p-2">{transactionDate}</td>
                                        </tr>
                                    );
                                })
                            ) : (
                                <tr>
                                    <td colSpan="4" className="p-2 text-center">No transactions found.</td>
                                </tr>
                            )}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>
        </MainLayout>
    );
}
