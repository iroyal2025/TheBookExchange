'use client';
import React, { useState, useEffect, useContext } from 'react';
import axios from '@/lib/axiosConfig';
import { Button } from "@/components/ui/button";
import { Spinner } from "@/components/ui/spinner";
import { AuthContext } from '../../../context/AuthContext';
import { useRouter } from 'next/navigation';
import { Input } from "@/components/ui/input";
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Textarea } from "@/components/ui/textarea";
import { toast } from 'react-hot-toast';
import { Slider } from "@/components/ui/slider"; // Import Slider

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
    const [bookFeedback, setBookFeedback] = useState({});
    const [feedbackMessage, setFeedbackMessage] = useState({});
    const [reportSellerModalOpen, setReportSellerModalOpen] = useState(false);
    const [reportSellerEmail, setReportSellerEmail] = useState('');
    const [reportSellerDescription, setReportSellerDescription] = useState('');
    const [reportSellerError, setReportSellerError] = useState(null);
    const [reportSellerSuccessMessage, setReportSellerSuccessMessage] = useState(null);
    const [rateSellerModalOpen, setRateSellerModalOpen] = useState(false);
    const [ratingSellerEmail, setRatingSellerEmail] = useState('');
    const [sellerRating, setSellerRating] = useState(5);
    const [rateSellerSuccessMessage, setRateSellerSuccessMessage] = useState(null);
    const [rateSellerError, setRateSellerError] = useState(null);
    const [addingToWishlist, setAddingToWishlist] = useState({}); // Track loading state for each book's wishlist button

    const refetchBooks = async () => {
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

    const fetchFeedback = async (bookId) => {
        try {
            const response = await axios.get(`http://localhost:8080/Forums/feedback/book/${bookId}`);
            if (response.data && response.data.success) {
                setBookFeedback({ ...bookFeedback, [bookId]: response.data.data });
                setFeedbackMessage({ ...feedbackMessage, [bookId]: response.data.data.length === 0 ? "No feedback available." : "" });
            } else {
                setFeedbackMessage({ ...feedbackMessage, [bookId]: "Failed to fetch feedback." });
                console.error("Failed to fetch feedback:", response.data.message);
            }
        } catch (err) {
            setFeedbackMessage({ ...feedbackMessage, [bookId]: "Error fetching feedback." });
            console.error("Failed to fetch feedback:", err);
        }
    };

    useEffect(() => {
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
            refetchBooks();
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
                const purchaseEmail = currentUser.email;
                if (userBalance >= price) {
                    const url = `http://localhost:8080/Books/${bookId}/purchase/email/${purchaseEmail}`;
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

    const handleReportSeller = async () => {
        setReportSellerError(null);
        if (!reportSellerEmail) {
            setReportSellerError('Please enter the seller\'s email.');
            return;
        }
        if (!reportSellerDescription) {
            setReportSellerError('Please describe the issue.');
            return;
        }
        try {
            const response = await axios.post('http://localhost:8080/Reports/add/seller', {
                reportedBy: currentUser?.email,
                sellerEmail: reportSellerEmail,
                content: reportSellerDescription,
            });
            if (response.data.success) {
                toast.success('Seller report submitted successfully!');
                setReportSellerSuccessMessage('Report successfully submitted!');
                setReportSellerModalOpen(false);
                setReportSellerEmail('');
                setReportSellerDescription('');
                setTimeout(() => {
                    setReportSellerSuccessMessage(null);
                }, 3000);
            } else {
                setReportSellerError(response.data.message || 'Failed to submit seller report.');
            }
        } catch (error) {
            console.error('Error submitting seller report:', error);
            setReportSellerError('An error occurred while submitting the seller report.');
        }
    };

    const handleRateSellerSubmit = async () => {
        setRateSellerError(null);
        try {
            const response = await axios.post('http://localhost:8080/Users/rate/seller', {
                sellerEmail: ratingSellerEmail,
                raterEmail: currentUser.email,
                rating: sellerRating,
            });
            if (response.data.success) {
                toast.success('Seller rated successfully!');
                setRateSellerSuccessMessage('Seller rated successfully!');
                setRateSellerModalOpen(false);
                setSellerRating(5);
                // Consider a more targeted update if needed
                // if (onRatingSubmitted) {
                //     onRatingSubmitted();
                // }
            } else {
                setRateSellerError(response.data.message || 'Failed to rate seller.');
            }
        } catch (error) {
            console.error('Error rating seller:', error);
            setRateSellerError('An error occurred while rating the seller.');
        }
    };

    const handleAddToWishlist = async (bookId) => {
        if (!currentUser?.uid) {
            toast.error('Please log in to add to your wishlist.');
            return;
        }

        setAddingToWishlist(prevState => ({ ...prevState, [bookId]: true }));
        try {
            const response = await axios.post(`http://localhost:8080/wishlist/addBook/${currentUser.uid}/${bookId}`);            if (response.data.success) {
                toast.success('Book added to your wishlist!');
            } else {
                toast.error(response.data.message || 'Failed to add book to wishlist.');
            }
        } catch (error) {
            console.error('Error adding book to wishlist:', error);
            toast.error('Error adding book to wishlist.');
        } finally {
            setAddingToWishlist(prevState => ({ ...prevState, [bookId]: false }));
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
                            <th className="p-2 text-left">Book Rating</th>
                            <th className="p-2 text-left">Seller</th>
                            <th className="p-2 text-left">Actions</th>
                        </tr>
                        </thead>
                        <tbody>
                        {books.map((book) => (
                            <tr key={book.bookId} className="border-b border-gray-200">
                                <td className="p-2">{book.title}</td>
                                <td className="p-2">{book.author}</td>
                                <td className="p-2">${book.price}</td>
                                <td className="p-2">
                                    {book.ratingCount > 0 ? `${(book.rating).toFixed(2)} (${book.ratingCount} ratings)` : 'No ratings'}
                                </td>
                                <td className="p-2">
                                    {book.sellerId}
                                    {book.sellerEmail && (
                                        <Button
                                            onClick={() => {
                                                setRatingSellerEmail(book.sellerEmail);
                                                setRateSellerModalOpen(true);
                                            }}
                                            variant="outline"
                                            className="ml-2"
                                            size="sm"
                                        >
                                            Rate Seller
                                        </Button>
                                    )}
                                    {book.sellerRatingCount > 0 ? `(Rating: ${(book.sellerRating)?.toFixed(2) || 'N/A'} - ${book.sellerRatingCount} ratings)` : '(No seller ratings)'}
                                </td>
                                <td className="p-2 flex space-x-2">
                                    <Button
                                        onClick={(event) => handleBuy(book.bookId, book.price, event)}
                                        disabled={purchaseLoading || !currentUser?.email}
                                    >
                                        {purchaseLoading ? <Spinner size="sm" /> : "Buy"}
                                    </Button>
                                    <Button onClick={() => fetchFeedback(book.bookId)} variant="outline">
                                        View Feedback
                                    </Button>
                                    {currentUser?.uid && (
                                        <Button
                                            onClick={() => handleAddToWishlist(book.bookId)}
                                            disabled={addingToWishlist[book.bookId]}
                                            variant="secondary"
                                        >
                                            {addingToWishlist[book.bookId] ? <Spinner size="sm" /> : "Wishlist"}
                                        </Button>
                                    )}
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>

                <div className="mt-8 flex justify-between">
                    <Button onClick={handleBackToDashboard} variant="outline">
                        Back to Dashboard
                    </Button>
                    <Button onClick={() => setReportSellerModalOpen(true)} variant="outline">
                        Report Seller
                    </Button>
                    <Dialog open={reportSellerModalOpen} onOpenChange={setReportSellerModalOpen}>
                        <DialogContent className="sm:max-w-[425px]">
                            <DialogHeader>
                                <DialogTitle>Report Issue with a Seller</DialogTitle>
                                <DialogDescription>
                                    Please enter the seller's email and describe the issue you are experiencing. This report will be sent to the administrator.
                                </DialogDescription>
                            </DialogHeader>
                            <div className="grid gap-4 py-4">
                                <div className="grid grid-cols-4 items-center gap-4">
                                    <label htmlFor="sellerEmail" className="text-right">
                                        Seller Email
                                    </label>
                                    <Input
                                        type="email"
                                        id="sellerEmail"
                                        placeholder="seller@example.com"
                                        value={reportSellerEmail}
                                        onChange={(e) => setReportSellerEmail(e.target.value)}
                                    />
                                </div>
                                <div className="grid gap-2">
                                    <label htmlFor="issueDescription">Issue Description</label>
                                    <Textarea
                                        id="issueDescription"
                                        placeholder="Describe the issue you are having with the seller."
                                        value={reportSellerDescription}
                                        onChange={(e) => setReportSellerDescription(e.target.value)}
                                    />
                                </div>
                            </div>
                            {reportSellerError && <p className="text-red-500 mt-2">{reportSellerError}</p>}
                            <div className="flex justify-end">
                                <Button type="button" onClick={handleReportSeller}>Submit Report</Button>
                            </div>
                        </DialogContent>
                    </Dialog>
                </div>

                {rateSellerSuccessMessage && (
                    <div className="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded relative mt-4" role="alert">
                        <span className="block sm:inline">{rateSellerSuccessMessage}</span>
                    </div>
                )}

                {reportSellerSuccessMessage && (
                    <div className="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded relative mt-4" role="alert">
                        <span className="block sm:inline">{reportSellerSuccessMessage}</span>
                    </div>
                )}

            </div>

            {/* Rate Seller Modal */}
            <Dialog open={rateSellerModalOpen} onOpenChange={setRateSellerModalOpen}>
                <DialogContent className="sm:max-w-[425px]">
                    <DialogHeader>
                        <DialogTitle>Rate Seller</DialogTitle>
                        <DialogDescription>
                            Please rate the seller: {ratingSellerEmail}
                        </DialogDescription>
                    </DialogHeader>
                    <div className="grid gap-4 py-4">
                        <div className="grid gap-2">
                            <Slider
                                value={[sellerRating]}
                                max={5}
                                step={1}
                                onValueChange={(value) => setSellerRating(value[0])}
                            />
                            <p>Rating: {sellerRating} / 5</p>
                        </div>
                    </div>
                    {rateSellerError && <p className="text-red-500 mt-2">{rateSellerError}</p>}
                    <div className="flex justify-end">
                        <Button type="button" onClick={handleRateSellerSubmit}>Submit Rating</Button>
                    </div>
                </DialogContent>
            </Dialog>
        </div>
    );
}