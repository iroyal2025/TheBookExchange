'use client';

import React, { useState, useEffect, useContext } from 'react';
import axios from 'axios';
import { AuthContext } from '../../../context/AuthContext';
import { useRouter } from 'next/navigation';
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { Slider } from "@/components/ui/slider";

export default function MyBooks({ onRatingSubmitted }) { // Add onRatingSubmitted prop
    const [ownedBooks, setOwnedBooks] = useState([]);
    const { currentUser, loading } = useContext(AuthContext);
    const [pageLoading, setPageLoading] = useState(true);
    const [renderTrigger, setRenderTrigger] = useState(0);
    const router = useRouter();
    const [reportModalOpen, setReportModalOpen] = useState(false);
    const [reportBookTitle, setReportBookTitle] = useState('');
    const [reportDescription, setReportDescription] = useState('');
    const [reportError, setReportError] = useState(null);
    const [reportSuccess, setReportSuccess] = useState(null);
    const [ratingModalOpen, setRatingModalOpen] = useState(false);
    const [ratingBookTitle, setRatingBookTitle] = useState('');
    const [bookRating, setBookRating] = useState(5);
    const [ratingSuccess, setRatingSuccess] = useState(null);

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
                        console.log('User does not own any books.');
                        setOwnedBooks([]);
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
        setRenderTrigger(prev => prev + 1);
    }, [currentUser, pageLoading]);

    const handleBackToDashboard = () => {
        router.push('/student-dashboard');
    };

    const handleReportIssue = async () => {
        setReportError(null);
        setReportSuccess(null);
        try {
            const response = await axios.post('http://localhost:8080/Reports/add', {
                bookTitle: reportBookTitle,
                userEmail: currentUser.email,
                content: reportDescription,
            });
            if (response.data) {
                setReportSuccess('Report submitted successfully!');
                setTimeout(() => setReportSuccess(null), 5000);
                setReportModalOpen(false);
                setReportBookTitle('');
                setReportDescription('');
            } else {
                setReportError('Failed to submit report.');
            }
        } catch (error) {
            console.error('Error submitting report:', error);
            setReportError('An error occurred while submitting the report.');
        }
    };

    const handleRateBook = async () => {
        try {
            await axios.post('http://localhost:8080/Books/rate', {
                bookTitle: ratingBookTitle,
                userEmail: currentUser.email,
                rating: bookRating,
            });
            setRatingModalOpen(false);
            setBookRating(5);
            setRatingSuccess('Rating submitted successfully!');
            setTimeout(() => setRatingSuccess(null), 5000);
            if (onRatingSubmitted) { // Call the prop function
                onRatingSubmitted();
            }
        } catch (error) {
            console.error('Error rating book:', error);
        }
    };

    if (pageLoading) return <div>Loading...</div>;

    return (
        <div className="min-h-screen bg-gradient-to-r from-orange-500 to-green-500 flex flex-col items-center justify-center">
            {reportSuccess && (
                <div className="fixed top-4 left-1/2 transform -translate-x-1/2 bg-green-500 text-white p-4 rounded-md z-50">
                    {reportSuccess}
                </div>
            )}
            {ratingSuccess && (
                <div className="fixed top-12 left-1/2 transform -translate-x-1/2 bg-green-500 text-white p-4 rounded-md z-50">
                    {ratingSuccess}
                </div>
            )}
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
                                <div className="mt-4 flex justify-between">
                                    <Button onClick={() => { setReportBookTitle(book.title); setReportModalOpen(true); }}>Report Issue</Button>
                                    <Button onClick={() => { setRatingBookTitle(book.title); setRatingModalOpen(true); }}>Rate Book</Button>
                                </div>
                            </li>
                        ))}
                    </ul>
                ) : (
                    <p className="text-gray-600">You don't own any books yet.</p>
                )}
                <div className="mt-8">
                    <Button onClick={handleBackToDashboard} variant="outline">
                        Back to Dashboard
                    </Button>
                </div>
            </div>
            <Dialog open={reportModalOpen} onOpenChange={setReportModalOpen}>
                <DialogContent className="sm:max-w-[425px]">
                    <DialogHeader>
                        <DialogTitle>Report an Issue</DialogTitle>
                        <DialogDescription>
                            Please enter the details of the issue you are experiencing.
                        </DialogDescription>
                    </DialogHeader>
                    <div className="grid gap-4 py-4">
                        <div className="grid grid-cols-4 items-center gap-4">
                            <Input
                                type="text"
                                placeholder="Book Title"
                                value={reportBookTitle}
                                onChange={(e) => setReportBookTitle(e.target.value)}
                                disabled
                            />
                        </div>
                        <div className="grid gap-2">
                            <Textarea
                                placeholder="Description of the issue"
                                value={reportDescription}
                                onChange={(e) => setReportDescription(e.target.value)}
                            />
                        </div>
                    </div>
                    {reportError && <p className="text-red-500 mt-2">{reportError}</p>}
                    <div className="flex justify-end">
                        <Button type="button" onClick={handleReportIssue}>Submit Report</Button>
                    </div>
                </DialogContent>
            </Dialog>

            <Dialog
                open={ratingModalOpen}
                onOpenChange={(open) => {
                    setRatingModalOpen(open);
                    if (open) {
                        setBookRating(5);
                    }
                }}
            >
                <DialogContent className="sm:max-w-[425px]">
                    <DialogHeader>
                        <DialogTitle>Rate Book</DialogTitle>
                        <DialogDescription>
                            Please rate the book.
                        </DialogDescription>
                    </DialogHeader>
                    <div className="grid gap-4 py-4">
                        <div className="grid grid-cols-4 items-center gap-4">
                            <Input
                                type="text"
                                placeholder="Book Title"
                                value={ratingBookTitle}
                                onChange={(e) => setRatingBookTitle(e.target.value)}
                                disabled
                            />
                        </div>
                        <div className="grid gap-2">
                            <Slider
                                value={[bookRating]}
                                max={5}
                                step={1}
                                onValueChange={(value) => setBookRating(value[0])}
                            />
                            <p>Rating: {bookRating} / 5</p>
                        </div>
                    </div>
                    <div className="flex justify-end">
                        <Button type="button" onClick={handleRateBook}>Submit Rating</Button>
                    </div>
                </DialogContent>
            </Dialog>
        </div>
    );
}