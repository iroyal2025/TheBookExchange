'use client';

import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { Button } from '@/components/ui/button';
import { useRouter } from 'next/navigation';

export default function TeacherDashboard() {
    const [books, setBooks] = useState([]);
    const [loading, setLoading] = useState(false);
    const [showBooks, setShowBooks] = useState(false);
    const router = useRouter(); // Use router to navigate back to the dashboard

    const fetchBooks = async () => {
        setLoading(true);
        try {
            const response = await axios.get('http://localhost:8080/Books/');
            if (response.data.success) {
                setBooks(response.data.data);
            } else {
                setBooks([]);
            }
        } catch (error) {
            console.error('Error fetching books:', error);
            setBooks([]);
        }
        setLoading(false);
    };

    const handleViewBooks = () => {
        if (!showBooks) {
            fetchBooks();
        }
        setShowBooks((prev) => !prev);
    };

    const handleBackToDashboard = () => {
        router.push('/teacher-dashboard'); // Navigate back to the teacher dashboard
    };

    return (
        <div className="min-h-screen bg-gray-100 p-6">
            <div className="max-w-5xl mx-auto bg-white p-8 rounded-xl shadow-md">
                <h1 className="text-3xl font-bold text-center mb-6">Teacher Dashboard</h1>

                {/* Back to Dashboard Button */}
                <div className="flex justify-center mb-4">
                    <Button onClick={handleBackToDashboard} className="bg-blue-500 text-white hover:bg-blue-600">
                        Back to Dashboard
                    </Button>
                </div>

                <div className="flex justify-center mb-4">
                    <Button onClick={handleViewBooks}>
                        {showBooks ? 'Hide Books' : 'View Books'}
                    </Button>
                </div>

                {loading && <p className="text-center text-gray-500">Loading books...</p>}

                {showBooks && !loading && (
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 max-h-[500px] overflow-y-auto">
                        {books.map((book) => (
                            <div
                                key={book.bookId}
                                className="border p-4 rounded-lg shadow hover:shadow-lg transition-shadow duration-300"
                            >
                                <h2 className="text-lg font-semibold text-orange-600">{book.title}</h2>
                                <p className="text-sm text-gray-700">Author: {book.author}</p>
                                <p className="text-sm text-gray-700">Edition: {book.edition}</p>
                                <p className="text-sm text-gray-700">ISBN: {book.isbn}</p>
                                <p className="text-sm text-gray-700">Seller ID: {book.userId}</p>
                                <p className="text-sm text-gray-700">Course ID: {book.courseId}</p>
                                <p className="text-sm text-gray-700">Digital: {book.digital ? 'Yes' : 'No'}</p>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
}
