'use client';
import React, { useState, useEffect, useContext } from 'react';
import axios from '@/lib/axiosConfig';
import { Button } from "@/components/ui/button";
import { Spinner } from "@/components/ui/spinner";
import { AuthContext } from '../../../../context/AuthContext';
import { useRouter, useSearchParams } from 'next/navigation';

export default function ParentViewWishlist() {
    const [wishlistItems, setWishlistItems] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const { currentUser } = useContext(AuthContext);
    const router = useRouter();
    const searchParams = useSearchParams();
    const studentEmail = searchParams.get('studentEmail');

    useEffect(() => {
        const fetchWishlist = async () => {
            setLoading(true);
            setError(null);

            if (!currentUser?.email || !studentEmail) {
                setError('Parent or Student email not available.');
                setLoading(false);
                return;
            }

            try {
                const response = await axios.get(`http://localhost:8080/wishlist/user/email/${studentEmail}/books`);
                console.log("ParentViewWishlist: fetchWishlist response:", response);

                if (!response.data.success) {
                    setError(`Failed to fetch wishlist IDs: ${response.data?.message || 'Unknown error'}`);
                    setLoading(false);
                    return;
                }

                const bookIds = response.data?.data?.bookRequests || [];

                const fetchBookDetails = async (bookId) => {
                    try {
                        const bookResponse = await axios.get(`http://localhost:8080/Books/${bookId}`);
                        if (!bookResponse.data.success) {
                            console.error(`Failed to fetch details for book ID ${bookId}: ${bookResponse.data?.message || 'Unknown error'}`);
                            return null;
                        }
                        return bookResponse.data.data;
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
    }, [currentUser?.email, studentEmail]);

    const handleBackToDashboard = () => {
        router.push('/parent-dashboard');
    };

    if (loading) {
        return <div className="p-6 flex justify-center items-center"><Spinner /></div>;
    }

    if (error) {
        return <div className="p-6 text-red-500">Error: {error}</div>;
    }

    if (wishlistItems.length === 0) {
        return (
            <div className="p-6">
                {studentEmail ? `The wishlist for ${studentEmail} is empty.` : 'No student email provided.'}
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
                    <h2 className="text-2xl font-semibold text-orange-600">
                        Wishlist for {studentEmail}
                    </h2>
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
                        </li>
                    ))}
                </ul>
            </div>
        </div>
    );
}