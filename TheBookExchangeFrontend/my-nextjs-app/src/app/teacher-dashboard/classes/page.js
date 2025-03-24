'use client';
import React, { useState, useEffect, useContext } from 'react';
import axios from '@/lib/axiosConfig';
import { AuthContext } from '../../../context/AuthContext';
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Spinner } from "@/components/ui/spinner";
import { useRouter } from 'next/navigation';

export default function ManageCourses() {
    const [courses, setCourses] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const { currentUser } = useContext(AuthContext);
    const [loadingBooks, setLoadingBooks] = useState(false);
    const [selectedCourseBooks, setSelectedCourseBooks] = useState([]);
    const [showCourseBooks, setShowCourseBooks] = useState(false);
    const [showAddBookForm, setShowAddBookForm] = useState(false);
    const [newBook, setNewBook] = useState({
        title: '',
        author: '',
        edition: '',
        ISBN: '',
        condition: '',
        description: '',
        isDigital: false,
        digitalCopyPath: '',
    });
    const [addBookLoading, setAddBookLoading] = useState(false);
    const [addBookError, setAddBookError] = useState(null);
    const router = useRouter();
    const [successMessage, setSuccessMessage] = useState(null);

    useEffect(() => {
        const fetchCourses = async () => {
            setLoading(true);
            setError(null);
            try {
                if (currentUser?.email) {
                    const response = await axios.get(`http://localhost:8080/Courses/teacher/${currentUser.email}`);
                    if (response.data && response.data.success) {
                        setCourses(response.data.data);
                    } else {
                        setError("Failed to load courses");
                    }
                }
            } catch (err) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };

        fetchCourses();
    }, [currentUser]);

    const handleRemoveBook = async (bookTitle) => {
        setLoading(true);
        console.log("handleRemoveBook called for:", bookTitle);
        try {
            const response = await axios.delete(`http://localhost:8080/Books/remove/title/${bookTitle}`);
            console.log("Remove book response:", response);
            if (response.data && response.data.success) {
                console.log("Book removed successfully");
                setSuccessMessage("Book removed successfully!");
                setTimeout(() => setSuccessMessage(null), 3000);

                if (showCourseBooks && selectedCourseBooks.length > 0 && selectedCourseBooks[0].courseId && selectedCourseBooks[0].courseId.id) {
                    const course = courses.find(course => course.courseId === selectedCourseBooks[0].courseId.id);
                    if (course) {
                        console.log("Refetching course books for:", course.courseName);
                        await fetchCourseBooks(course.courseName);
                        console.log("Course books refetched");
                    }
                }
            } else {
                setError("Failed to remove book");
                console.error("Failed to remove book");
            }
        } catch (err) {
            setError(err.message);
            console.error("Error removing book:", err);
        } finally {
            setLoading(false);
            console.log("handleRemoveBook finished");
        }
    };

    const fetchCourseBooks = async (courseName) => {
        setLoadingBooks(true);
        console.log("fetchCourseBooks called for:", courseName);
        try {
            const response = await axios.get(`http://localhost:8080/Courses/books/name/${courseName}`);
            console.log("fetchCourseBooks response:", response);
            if (response.data && response.data.success) {
                setSelectedCourseBooks(response.data.data);
                setShowCourseBooks(true);
                console.log("Course books fetched successfully");
            } else {
                setError("Failed to fetch course books");
                console.error("Failed to fetch course books");
            }
        } catch (err) {
            console.error("Failed to fetch books for course:", err);
            setError(err.message);
        } finally {
            setLoadingBooks(false);
            console.log("fetchCourseBooks finished");
        }
    };

    const handleAddBook = async (courseId) => {
        setAddBookLoading(true);
        setAddBookError(null);
        try {
            const response = await axios.post(`http://localhost:8080/Books/add`, {
                ...newBook,
                courseId: courseId,
            });

            if (response.data && response.data.success) {
                const courseName = courses.find(course => course.courseId === courseId).courseName;

                await axios.post(`http://localhost:8080/Courses/addTextbook`, null, {
                    params: {
                        courseName: courseName,
                        textbook: newBook.title,
                    },
                });

                fetchCourseBooks(courseName);
                setNewBook({
                    title: '',
                    author: '',
                    edition: '',
                    ISBN: '',
                    condition: '',
                    description: '',
                    isDigital: false,
                    digitalCopyPath: '',
                });
                setShowAddBookForm(false);
            } else {
                setAddBookError(response.data.message);
            }
        } catch (err) {
            setAddBookError(err.message);
        } finally {
            setAddBookLoading(false);
        }
    };

    if (loading) return <div className="p-4 flex justify-center items-center"><Spinner /></div>;
    if (error) return <div className="p-4 text-red-600">Error loading courses: {error}</div>;

    return (
        <div className="min-h-screen bg-gradient-to-r from-blue-100 to-purple-100 p-6 space-y-6">
            <div className="bg-white p-6 rounded-lg shadow-md">
                <h2 className="text-3xl font-bold mb-4 text-gray-800">Manage Courses</h2>

                <Button onClick={() => router.push('/teacher-dashboard')}
                        className="mb-4 bg-blue-500 text-white hover:bg-blue-600">
                    Teacher Dashboard
                </Button>

                {courses.length > 0 && (
                    <div className="overflow-x-auto">
                        <table className="min-w-full border border-gray-200 rounded-md shadow-sm">
                            <thead>
                            <tr>
                                <th className="p-3 text-left bg-gray-100 text-gray-700">Course Name</th>
                                <th className="p-3 text-left bg-gray-100 text-gray-700">Actions</th>
                            </tr>
                            </thead>
                            <tbody>
                            {courses.map(course => (
                                <tr key={course.courseId} className="border-b border-gray-200 hover:bg-gray-50">
                                    <td className="p-3 text-gray-800">{course.courseName}</td>
                                    <td className="p-3">
                                        <Button onClick={() => fetchCourseBooks(course.courseName)} variant="outline"
                                                className="text-blue-600 hover:bg-blue-50">
                                            View Books
                                        </Button>
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>
            {showCourseBooks && (
                <div className="bg-white p-6 rounded-lg shadow-md">
                    <h3 className="text-xl font-semibold mb-4 text-gray-800">Books
                        for {courses.find(course => course.courseId === selectedCourseBooks[0]?.courseId?.id)?.courseName}</h3>
                    {successMessage && <p className="text-green-600">{successMessage}</p>}
                    {selectedCourseBooks.map(book => (
                        <div key={book.bookId} className="border-b border-gray-200 p-3">
                            <p><strong>Title:</strong> {book.title}</p>
                            <p><strong>Author:</strong> {book.author}</p>
                            <p><strong>Edition:</strong> {book.edition}</p>
                            <p><strong>ISBN:</strong> {book.ISBN}</p>
                            <p><strong>Condition:</strong> {book.condition}</p>
                            <p><strong>Description:</strong> {book.description}</p>
                            <p><strong>Is Digital:</strong> {book.isDigital ? 'Yes' : 'No'}</p>
                            {book.isDigital && <p><strong>Digital Copy Path:</strong> {book.digitalCopyPath}</p>}
                            {book.textbooks && book.textbooks.length > 0 && (
                                <div>
                                    <p><strong>Textbooks:</strong></p>
                                    <ul>
                                        {book.textbooks.map((textbook) => (
                                            <li key={textbook}>{textbook}</li>
                                        ))}
                                    </ul>
                                </div>
                            )}
                            <Button onClick={() => handleRemoveBook(book.title)} variant="outline" size="icon">
                                X
                            </Button>
                        </div>
                    ))}
                    <Button onClick={() => setShowAddBookForm(true)} variant="outline" className="mt-4">
                        Add Book
                    </Button>
                    {showAddBookForm && (
                        <div className="mt-4">
                            <Input placeholder="Title" value={newBook.title}
                                   onChange={(e) => setNewBook({ ...newBook, title: e.target.value })} className="mb-2" />
                            <Input placeholder="Author" value={newBook.author}
                                   onChange={(e) => setNewBook({ ...newBook, author: e.target.value })} className="mb-2" />
                            <Input placeholder="Edition" value={newBook.edition}
                                   onChange={(e) => setNewBook({ ...newBook, edition: e.target.value })}
                                   className="mb-2" />
                            <Input placeholder="ISBN" value={newBook.ISBN}
                                   onChange={(e) => setNewBook({ ...newBook, ISBN: e.target.value })} className="mb-2" />
                            <Input placeholder="Condition" value={newBook.condition}
                                   onChange={(e) => setNewBook({ ...newBook, condition: e.target.value })}
                                   className="mb-2" />
                            <Input placeholder="Description" value={newBook.description}
                                   onChange={(e) => setNewBook({ ...newBook, description: e.target.value })}
                                   className="mb-2" />
                            <div className="mb-2">
                                <label className="block text-sm font-medium text-gray-700">Is Digital</label>
                                <input
                                    type="checkbox"
                                    checked={newBook.isDigital}
                                    onChange={(e) => setNewBook({ ...newBook, isDigital: e.target.checked })}
                                    className="mt-1"
                                />
                            </div>
                            {newBook.isDigital && (
                                <Input placeholder="Digital Copy Path" value={newBook.digitalCopyPath}
                                       onChange={(e) => setNewBook({ ...newBook, digitalCopyPath: e.target.value })}
                                       className="mb-2" />
                            )}
                            <Button onClick={() => handleAddBook(selectedCourseBooks[0].courseId.id)}
                                    disabled={addBookLoading} className="mt-2">
                                {addBookLoading ? <Spinner /> : "Submit"}
                            </Button>
                            {addBookError && <p className="text-red-500 mt-2">{addBookError}</p>}
                        </div>
                    )}

                    <Button onClick={() => setShowCourseBooks(false)} variant="outline" className="mt-4">
                        Close
                    </Button>
                </div>
            )}
        </div>
    );
}