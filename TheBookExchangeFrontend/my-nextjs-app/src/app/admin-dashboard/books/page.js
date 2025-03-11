'use client';
import React, { useState, useEffect } from 'react';
import axios from 'axios';

export default function ManageBooks() {
    const [books, setBooks] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchBooks = async () => {
            try {
                const response = await axios.get('http://localhost:8080/Books/');
                setBooks(response.data.data);
                setLoading(false);
            } catch (error) {
                console.error('Error fetching books:', error);
                if (axios.isAxiosError(error)) {
                    console.error('Axios Error Details:', error.toJSON());
                }
                setLoading(false);
            }
        };
        fetchBooks();
    }, []);

    if (loading) return <div className="p-4">Loading books...</div>;

    return (
        <div className="min-h-screen bg-gradient-to-r from-orange-500 to-green-500 flex items-center justify-center p-4">
            <div className="bg-white p-10 rounded-2xl shadow-2xl w-full max-w-4xl">
                <h2 className="text-2xl font-semibold mb-6 text-orange-600">Manage Books</h2>
                <div className="overflow-x-auto">
                    <table className="min-w-full border border-gray-200">
                        <thead>
                        <tr className="bg-gray-100">
                            <th className="border border-gray-200 p-2 text-left">Title</th>
                            <th className="border border-gray-200 p-2 text-left">Author</th>
                            <th className="border border-gray-200 p-2 text-left">Actions</th>
                        </tr>
                        </thead>
                        <tbody>
                        {books.map((book) => (
                            <tr key={book.bookId.id} className="border-b border-gray-200">
                                <td className="p-2">{book.title}</td>
                                <td className="p-2">{book.author}</td>
                                <td className="p-2">
                                    <button className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-1 px-2 rounded mr-2">Edit</button>
                                    <button className="bg-red-500 hover:bg-red-700 text-white font-bold py-1 px-2 rounded">Delete</button>
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
}