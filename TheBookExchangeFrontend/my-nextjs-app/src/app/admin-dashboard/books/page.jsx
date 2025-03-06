'use client';
import React, { useState, useEffect } from 'react';
import axios from 'axios';

export default function ManageBooks() {
    const [books, setBooks] = useState([]); // Correct initial state
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

    if (loading) return <div>Loading books...</div>;

    return (
        <div style={{ padding: '20px' }}>
            <h2>Manage Books</h2>
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                <thead>
                <tr>
                    <th style={{ border: '1px solid #ddd', padding: '8px' }}>Title</th>
                    <th style={{ border: '1px solid #ddd', padding: '8px' }}>Author</th>
                    <th style={{ border: '1px solid #ddd', padding: '8px' }}>Actions</th>
                </tr>
                </thead>
                <tbody>
                {books.map((book) => (
                    <tr key={book.bookId.id}>
                        <td style={{ border: '1px solid #ddd', padding: '8px' }}>{book.title}</td>
                        <td style={{ border: '1px solid #ddd', padding: '8px' }}>{book.author}</td>
                        <td style={{ border: '1px solid #ddd', padding: '8px' }}>
                            <button style={{ marginRight: '5px' }}>Edit</button>
                            <button>Delete</button>
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
}