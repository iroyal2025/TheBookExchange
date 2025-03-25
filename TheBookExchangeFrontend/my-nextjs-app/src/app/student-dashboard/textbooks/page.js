'use client';
import React, { useState, useEffect, useContext } from 'react';
import axios from '@/lib/axiosConfig';
import { Button } from "@/components/ui/button";
import { Spinner } from "@/components/ui/spinner";
import { AuthContext } from '../../../context/AuthContext';
import { useRouter } from 'next/navigation';
import { Input } from "@/components/ui/input";

export default function BrowseTextbooks() {
    const [books, setBooks] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [purchaseLoading, setPurchaseLoading] = useState(false);
    const [purchaseError, setPurchaseError] = useState(null);
    const [message, setMessage] = useState(null);
    const [userBalance, setUserBalance] = useState(0);
    const { currentUser } = useContext(AuthContext);
    const [renderTrigger, setRenderTrigger] = useState(0);
    const router = useRouter();
    const [showBalanceEdit, setShowBalanceEdit] = useState(false);
    const [newBalance, setNewBalance] = useState('');
    const [balanceEditLoading, setBalanceEditLoading] = useState(false);
    const [balanceEditError, setBalanceEditError] = useState(null);
    const [balanceEditSuccess, setBalanceEditSuccess] = useState(null);

    useEffect(() => {
        const fetchBooks = async () => {
            setLoading(true);
            setError(null);
            try {
                const response = await axios.get('http://localhost:8080/Books/');
                console.log("BrowseTextbooks: fetchBooks response:", response);
                if (response.data.success) {
                    setBooks(response.data.data);
                } else {
                    setError(response.data.message);
                }
            } catch (err) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };

        const fetchUserBalance = async () => {
            if (currentUser?.email) {
                try {
                    const response = await axios.get(`http://localhost:8080/Users/balance/email/${currentUser.email}`);
                    console.log("BrowseTextbooks: fetchUserBalance response:", response);
                    if (response.data.success) {
                        setUserBalance(response.data.data);
                    } else {
                        console.error('Failed to fetch user balance:', response.data.message);
                    }
                } catch (err) {
                    console.error('Error fetching user balance:', err);
                }
            }
        };

        if (currentUser?.email) {
            fetchBooks();
            fetchUserBalance();
        }
        console.log("BrowseTextbooks, current user: ", currentUser);
        setRenderTrigger(prev => prev + 1);
    }, [currentUser]);

    useEffect(() => {
        if (message) {
            const timer = setTimeout(() => {
                setMessage(null);
            }, 10000);
            return () => clearTimeout(timer);
        }
    }, [message]);

    const handleBuy = async (bookId, price, event) => {
        if (event) {
            event.stopPropagation();
        }
        setPurchaseLoading(true);
        setPurchaseError(null);
        try {
            if (currentUser?.email) {
                if (userBalance >= price) {
                    const url = `http://localhost:8080/Books/<span class="math-inline">\{bookId\}/purchase/email/</span>{currentUser.email}`;
                    const response = await axios.put(url);
                    console.log("BrowseTextbooks: handleBuy response:", response);
                    if (response.data.success) {
                        setMessage(response.data.message);
                        setUserBalance(response.data.data);
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
            setPurchaseError(err.message || 'An error occurred during purchase.');
        } finally {
            setPurchaseLoading(false);
        }
    };

    const handleBackToDashboard = () => {
        router.push('/student-dashboard');
    };

    const handleUpdateBalance = async () => {
        setBalanceEditLoading(true);
        setBalanceEditError(null);
        setBalanceEditSuccess(null);

        try {
            const email = currentUser.email;
            const balance = parseFloat(newBalance);

            // Correct URL construction using template literals
            const url = `http://localhost:8080/Users/balance/email/${encodeURIComponent(email)}?balance=${encodeURIComponent(balance)}`;

            const response = await axios.put(url);

            if (response.data && response.data.success) {
                setBalanceEditSuccess('Balance updated successfully!');
                setUserBalance(balance);
                setShowBalanceEdit(false);
            } else {
                setBalanceEditError('Failed to update balance.');
            }
        } catch (err) {
            setBalanceEditError(err.message);
        } finally {
            setBalanceEditLoading(false);
        }
    };

    if (loading) return <div className="p-4 flex justify-center items-center"><Spinner /></div>;
    if (error) return <div className="p-4 text-red-600">Error loading textbooks: {error}</div>;
    return (
        <div className="min-h-screen bg-gradient-to-r from-orange-500 to-green-500 flex flex-col items-center justify-center p-4">
            <div className="bg-white p-10 rounded-2xl shadow-2xl w-full max-w-4xl">
                <div className="flex justify-between items-center mb-6">
                    <h2 className="text-2xl font-semibold text-orange-600">Browse Textbooks</h2>
                    {currentUser?.email && (
                        <div>
                            <p style={{ color: 'red', fontSize: '20px' }}>Your Balance: ${userBalance}</p>
                            <Button onClick={() => setShowBalanceEdit(true)} variant="outline" className="ml-4">
                                Edit Balance
                            </Button>
                        </div>
                    )}
                </div>

                {showBalanceEdit && (
                    <div className="mt-4">
                        <Input
                            type="number"
                            placeholder="New Balance"
                            value={newBalance}
                            onChange={(e) => setNewBalance(e.target.value)}
                            className="mb-2"
                        />
                        <Button onClick={handleUpdateBalance} disabled={balanceEditLoading} className="mt-2">
                            {balanceEditLoading ? <Spinner /> : 'Update Balance'}
                        </Button>
                        {balanceEditError && <p className="text-red-500 mt-2">{balanceEditError}</p>}
                        {balanceEditSuccess && <p className="text-green-500 mt-2">{balanceEditSuccess}</p>}
                    </div>
                )}

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
                                        onClick={(event) => handleBuy(book.bookId, book.price, event)}
                                        disabled={purchaseLoading || !currentUser?.email}
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

                <div className="mt-8">
                    <Button onClick={handleBackToDashboard} variant="outline">
                        Back to Dashboard
                    </Button>
                </div>
            </div>
        </div>
    );
}