'use client';
import React, { useState, useEffect, useContext } from 'react';
import { Button } from "@/components/ui/button";
import { AuthContext } from '../../../context/AuthContext';
import { useRouter } from 'next/navigation'; // Import useRouter

export default function WishlistPage() {
    const [wishlistItems, setWishlistItems] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const { userData } = useContext(AuthContext);
    const userId = userData?.uid || userData?.userId;
    const router = useRouter(); // Initialize useRouter

    useEffect(() => {
        const fetchWishlist = async () => {
            setLoading(true);
            setError(null);

            if (!userId) {
                setError('User not authenticated.');
                setLoading(false);
                return;
            }

            try {
                const response = await fetch(`http://localhost:8080/wishlist/user/${userId}/books`);
                if (!response.ok) {
                    const errorData = await response.json();
                    setError(`Failed to fetch wishlist IDs: ${errorData?.message || response.statusText}`);
                    setLoading(false);
                    return;
                }

                const data = await response.json();
                const bookIds = data?.data?.bookRequests || [];

                const fetchBookDetails = async (bookId) => {
                    console.log(`Fetching details for book ID: ${bookId}`);
                    try {
                        const bookResponse = await fetch(`http://localhost:8080/Books/${bookId}`);
                        console.log("Book Response Status:", bookResponse.status);
                        if (!bookResponse.ok) {
                            console.error(`Failed to fetch details for book ID ${bookId}: ${bookResponse.statusText}`);
                            return null;
                        }
                        const bookData = await bookResponse.json();
                        console.log("Book Data:", bookData);
                        return bookData; // Return the bookData object directly
                    } catch (err) {
                        console.error(`Error fetching details for book ID ${bookId}: ${err.message}`);
                        return null;
                    }
                };

                const bookDetailsPromises = bookIds.map(fetchBookDetails);
                const resolvedBookDetails = await Promise.all(bookDetailsPromises);

                const validBookDetails = resolvedBookDetails.filter(detail => detail);
                setWishlistItems(validBookDetails);

            } catch (e) {
                setError(`An unexpected error occurred: ${e.message}`);
            } finally {
                setLoading(false);
            }
        };

        fetchWishlist();
    }, [userId]);

    const handleRemoveBook = async (bookId) => {
        if (!userId) {
            setError('User not authenticated.');
            return;
        }

        setLoading(true);
        setError(null);
        try {
            const response = await fetch(`http://localhost:8080/wishlist/removeBook/${userId}/${bookId}`, {
                method: 'DELETE',
            });
            if (!response.ok) {
                const errorData = await response.json();
                setError(`Failed to remove book: ${errorData?.message || response.statusText}`);
            } else {
                setWishlistItems(prevItems => prevItems.filter(item => item?.bookId !== bookId));
            }
        } catch (e) {
            setError(`An unexpected error occurred: ${e.message}`);
        } finally {
            setLoading(false);
        }
    };

    const handleBackToDashboard = () => {
        router.push('/student-dashboard'); // Use router.push to navigate
    };

    if (loading) {
        return <div className="p-6">Loading your wishlist...</div>;
    }

    if (error) {
        return <div className="p-6 text-red-500">Error: {error}</div>;
    }

    if (wishlistItems.length === 0) {
        return (
            <div className="p-6">
                Your wishlist is empty.
                <div className="mt-4">
                    <Button onClick={handleBackToDashboard}>Back to Dashboard</Button>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gray-100 py-6">
            <div className="max-w-3xl mx-auto bg-white rounded-lg shadow-md p-6">
                <div className="flex justify-between items-center mb-4">
                    <h2 className="text-2xl font-semibold text-orange-600">Your Wishlist</h2>
                    <Button onClick={handleBackToDashboard}>Back to Dashboard</Button>
                </div>
                <ul>
                    {wishlistItems.map((book) => (
                        <li key={book?.bookId} className="flex items-center justify-between py-2 border-b">
                            <div>
                                <h3 className="font-semibold">{book?.title || 'Untitled'}</h3>
                                <p className="text-sm text-gray-500">{book?.author || 'Unknown Author'}</p>
                                {/* Add more book details as needed */}
                            </div>
                            <Button
                                onClick={() => handleRemoveBook(book?.bookId)}
                                variant="destructive"
                                size="sm"
                            >
                                Remove
                            </Button>
                        </li>
                    ))}
                </ul>
            </div>
        </div>
    );
}