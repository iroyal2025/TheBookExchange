'use client';
import React, { useState, useEffect, useContext } from 'react';
import axios from '@/lib/axiosConfig';
import { useRouter } from 'next/navigation';
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Spinner } from "@/components/ui/spinner";
import { toast } from 'react-hot-toast';
import { AuthContext } from '@/context/AuthContext';
import Link from 'next/link';

export default function SellerListings() {
    const router = useRouter();
    const [sellerBooks, setSellerBooks] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [isDeletingBook, setIsDeletingBook] = useState({});
    const [isUpdatingBook, setIsUpdatingBook] = useState({});
    const [editingBookId, setEditingBookId] = useState(null);
    const [editedBookData, setEditedBookData] = useState({});
    const { currentUser } = useContext(AuthContext);

    useEffect(() => {
        if (currentUser?.email) {
            fetchSellerBooks();
        }
    }, [currentUser]);

    const fetchSellerBooks = async () => {
        if (currentUser?.email) {
            setLoading(true);
            setError(null);
            try {
                const response = await axios.get(`http://localhost:8080/Books/${currentUser.email}/listings`);
                if (response.data?.success) {
                    setSellerBooks(response.data.data);
                    if (response.data.data.length === 0) {
                        setError('No books are in your listings.');
                    }
                } else {
                    setError(response.data?.message || 'No books are in your listings');
                }
            } catch (err) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        }
    };

    const handleDeleteBook = async (book) => {
        if (window.confirm(`Are you sure you want to remove "${book.title}" from your listing?`)) {
            setIsDeletingBook(prev => ({ ...prev, [book.bookId]: true }));
            setError(null);
            try {
                const response = await axios.delete(`http://localhost:8080/Books/${book.bookId}`);
                if (response.data?.success) { // Assuming your ApiResponse has a success field
                    toast.success(`"${book.title}" removed from your listing!`);
                    // Optimistic update: Remove the book from the local state immediately
                    setSellerBooks(prevBooks => prevBooks.filter(b => b.bookId !== book.bookId));
                } else {
                    const errorMessage = response.data?.message || 'Failed to remove book from listing.';
                    setError(errorMessage);
                    toast.error(errorMessage);
                    // Optionally, revert the optimistic update if the backend fails
                    fetchSellerBooks(); // Re-fetch to ensure consistency
                }
            } catch (error) {
                setError(error.message);
                toast.error('Failed to remove book from listing.');
                fetchSellerBooks(); // Re-fetch on error to ensure consistency
            } finally {
                setIsDeletingBook(prev => ({ ...prev, [book.bookId]: false }));
            }
        }
    };

    const handleEditBook = (book) => {
        setEditingBookId(book.bookId);
        setEditedBookData({ ...book }); // Initialize edit form with book data
    };

    const handleCancelEdit = () => {
        setEditingBookId(null);
        setEditedBookData({});
    };

    const handleUpdateInputChange = (e) => {
        const { name, value, type, checked } = e.target;
        setEditedBookData(prev => ({
            ...prev,
            [name]: type === 'checkbox' ? checked : value,
        }));
    };

    const handleUpdateBook = async () => {
        if (!editingBookId) return;
        setIsUpdatingBook(prev => ({ ...prev, [editingBookId]: true }));
        setError(null);
        try {
            const response = await axios.put(`http://localhost:8080/Books/${editingBookId}`, editedBookData);
            if (response.data?.success) { // Assuming your ApiResponse has a success field
                toast.success('Book updated successfully!');
                setEditingBookId(null);
                setEditedBookData({});
                fetchSellerBooks(); // Refresh the list
            } else {
                setError(response.data?.message || 'Failed to update book.');
                toast.error(response.data?.message || 'Failed to update book.');
            }
        } catch (error) {
            setError(error.message);
            toast.error('Failed to update book.');
        } finally {
            setIsUpdatingBook(prev => ({ ...prev, [editingBookId]: false }));
        }
    };

    if (loading) return <div className="p-4 flex justify-center items-center"><Spinner /></div>;

    return (
        <div className="min-h-screen bg-gray-100 py-6">
            <div className="max-w-3xl mx-auto bg-white shadow-md rounded-lg p-6">
                <h2 className="text-2xl font-semibold mb-4 text-gray-800">My Book Listings</h2>
                <Link href="/seller-dashboard" className="inline-block mb-4 text-blue-500 hover:underline">
                    Back to Dashboard
                </Link>
                {sellerBooks.length > 0 ? (
                    <ul className="divide-y divide-gray-200">
                        {sellerBooks.map(book => (
                            <li key={book.bookId} className="py-4">
                                {editingBookId === book.bookId ? (
                                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
                                        <Input type="text" name="title" placeholder="Title" value={editedBookData.title || ''} onChange={handleUpdateInputChange} />
                                        <Input type="text" name="author" placeholder="Author" value={editedBookData.author || ''} onChange={handleUpdateInputChange} />
                                        <Input type="number" name="price" placeholder="Price" value={editedBookData.price || ''} onChange={handleUpdateInputChange} />
                                        <Input type="text" name="ISBN" placeholder="ISBN" value={editedBookData.ISBN || ''} onChange={handleUpdateInputChange} />
                                        <Input type="text" name="condition" placeholder="Condition" value={editedBookData.condition || ''} onChange={handleUpdateInputChange} />
                                        <Input type="text" name="description" placeholder="Description" value={editedBookData.description || ''} onChange={handleUpdateInputChange} />
                                        <div>
                                            <label className="inline-flex items-center">
                                                <input type="checkbox" className="form-checkbox h-5 w-5 text-green-600" name="isDigital" checked={editedBookData.isDigital || false} onChange={handleUpdateInputChange} />
                                                <span className="ml-2 text-gray-700">Digital Copy</span>
                                            </label>
                                            {editedBookData.isDigital && <Input type="text" name="digitalCopyPath" placeholder="Digital Copy Path" value={editedBookData.digitalCopyPath || ''} onChange={handleUpdateInputChange} />}
                                        </div>
                                        <div className="flex space-x-2">
                                            <Button onClick={handleUpdateBook} disabled={isUpdatingBook[book.bookId]}>
                                                {isUpdatingBook[book.bookId] ? <Spinner size="sm" /> : 'Update'}
                                            </Button>
                                            <Button variant="outline" onClick={handleCancelEdit}>Cancel</Button>
                                        </div>
                                    </div>
                                ) : (
                                    <div className="flex items-center justify-between">
                                        <div>
                                            <h3 className="text-lg font-medium text-gray-700">{book.title}</h3>
                                            <p className="text-sm text-gray-500">Author: {book.author}</p>
                                            <p className="text-sm text-gray-500">Price: ${book.price}</p>
                                        </div>
                                        <div className="flex space-x-2">
                                            <Button onClick={() => handleEditBook(book)} className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded text-sm">
                                                Edit
                                            </Button>
                                            <Button
                                                onClick={() => handleDeleteBook(book)}
                                                className="bg-red-500 hover:bg-red-700 text-white font-bold py-2 px-4 rounded text-sm"
                                                disabled={isDeletingBook[book.bookId]}
                                            >
                                                {isDeletingBook[book.bookId] ? <Spinner size="sm" /> : 'Delete'}
                                            </Button>
                                        </div>
                                    </div>
                                )}
                            </li>
                        ))}
                    </ul>
                ) : (
                    !loading && <p className="text-gray-500">{error || "No books are in your listings."}</p>
                )}
            </div>
        </div>
    );
}