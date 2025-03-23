'use client';

import React, { useState, useEffect, useContext } from 'react';
import axios from 'axios';
import { AuthContext } from '../../../context/AuthContext';
import { useRouter } from 'next/navigation'; // Import useRouter
import { Button } from "@/components/ui/button"; // Import Button component

export default function MyBooks() {
    const [ownedBooks, setOwnedBooks] = useState([]);
    const { currentUser, loading } = useContext(AuthContext);
    const [pageLoading, setPageLoading] = useState(true);
    const [renderTrigger, setRenderTrigger] = useState(0); // Force re-render
    const router = useRouter(); // Initialize router

    useEffect(() => {
        if (!loading) {
            setPageLoading(false);
        }
    }, [loading]);

    useEffect(() => {
        const fetchOwnedBooks = async () => {
            if (currentUser?.email) {
                try {
                    const response = await axios.get(`http://localhost:8080/Books/owned/email/${currentUser.email}`);
                    console.log("MyBooks: fetchOwnedBooks response:", response);
                    if (response.data.success) {
                        setOwnedBooks(response.data.data);
                    } else {
                        // Handle the case where the user doesn't own books
                        console.log('User does not own any books.');
                        setOwnedBooks([]); // Set ownedBooks to an empty array
                    }
                } catch (error) {
                    console.error('Error fetching owned books:', error);
                }
            } else {
                console.log("CurrentUser or email is missing");
            }
        };

        if (!pageLoading && currentUser) {
            fetchOwnedBooks();
        }
        setRenderTrigger(prev => prev + 1); // Force re-render
    }, [currentUser, pageLoading]);

    const handleBackToDashboard = () => {
        router.push('/student-dashboard'); // Navigate back to the student dashboard
    };

    if (pageLoading) return <div>Loading...</div>;

    return (
        <div className="min-h-screen bg-gradient-to-r from-orange-500 to-green-500 flex flex-col items-center justify-center">
            <div className="bg-white p-10 rounded-2xl shadow-2xl text-center w-full max-w-4xl">
                <h2 className="text-4xl font-bold text-orange-600 mb-6">My Owned Books</h2>
                <img src="/Book Exchange side photo.jpg" alt="Book Exchange side photo" className="w-48 mx-auto mb-6" />

                {ownedBooks.length > 0 ? (
                    <ul className="space-y-4">
                        {ownedBooks.map((book) => (
                            <li key={book.bookId} className="border p-4 rounded-lg shadow-md">
                                <div className="grid grid-cols-2 gap-4">
                                    <div><strong className="text-blue-600">Title:</strong> {book.title}</div>
                                    <div><strong className="text-blue-600">Author:</strong> {book.author}</div>
                                    <div><strong className="text-blue-600">Edition:</strong> {book.edition}</div>
                                    <div><strong className="text-blue-600">ISBN:</strong> {book.isbn}</div>
                                    <div><strong className="text-blue-600">UserID:</strong> {book.userId}</div>
                                    <div><strong className="text-blue-600">CourseID:</strong> {book.courseId}</div>
                                    <div><strong className="text-blue-600">Digital:</strong> {book.digital ? 'Yes' : 'No'}</div>
                                </div>
                            </li>
                        ))}
                    </ul>
                ) : (
                    <p className="text-gray-600">You don't own any books yet.</p>
                )}
                {/* Back to Dashboard Button */}
                <div className="mt-8">
                    <Button onClick={handleBackToDashboard} variant="outline">
                        Back to Dashboard
                    </Button>
                </div>
            </div>
        </div>
    );
}