'use client';
import React, { useState, useEffect, useContext } from 'react';
import axios from '@/lib/axiosConfig';
import { AuthContext } from '../../../context/AuthContext';
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Spinner } from "@/components/ui/spinner";
import { useRouter } from 'next/navigation';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import Label from "@/components/ui/label"; // Import the default export directly

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
    const [feedback, setFeedback] = useState({ bookId: null, feedbackText: '' });
    const [feedbackLoading, setFeedbackLoading] = useState(false);
    const [feedbackError, setFeedbackError] = useState(null);
    const [bookFeedback, setBookFeedback] = useState({});
    const [editBookDialogOpen, setEditBookDialogOpen] = useState(false);
    const [editingBook, setEditingBook] = useState(null);
    const [editBookLoading, setEditBookLoading] = useState(false);
    const [editBookError, setEditBookError] = useState(null);
    const [editedBookData, setEditedBookData] = useState({
        title: '',
        author: '',
        edition: '',
        ISBN: '',
        condition: '',
        description: '',
        isDigital: false,
        digitalCopyPath: '',
    });

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

                await fetchCourseBooks(courseName);
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
                setSuccessMessage("Book added successfully!");
                setTimeout(() => setSuccessMessage(null), 3000);
            } else {
                setAddBookError(response.data.message);
            }
        } catch (err) {
            setAddBookError(err.message);
        } finally {
            setAddBookLoading(false);
        }
    };

    const handleFeedback = async (bookId, feedbackText) => {
        if (!bookId) return;
        setFeedbackLoading(true);
        setFeedbackError(null);
        try {
            const response = await axios.post(`http://localhost:8080/Forums/feedback/book/${bookId}`, {
                feedback: feedbackText,
            });

            if (response.data && response.data.success) {
                setSuccessMessage("Feedback added successfully!");
                setTimeout(() => setSuccessMessage(null), 3000);
                setFeedback({ bookId: null, feedbackText: '' });
                await fetchFeedback(bookId);
            } else {
                setFeedbackError("Failed to add feedback");
            }
        } catch (err) {
            setFeedbackError(err.message);
        } finally {
            setFeedbackLoading(false);
        }
    };

    const handleRemoveFeedback = async (feedbackId, bookId) => {
        try {
            const response = await axios.delete(`http://localhost:8080/Forums/feedback/${feedbackId}`);
            if (response.data && response.data.success) {
                await fetchFeedback(bookId);
            } else {
                console.error('Failed to delete feedback');
            }
        } catch (err) {
            console.error('Error deleting feedback:', err);
        }
    };

    const fetchFeedback = async (bookId) => {
        try {
            const response = await axios.get(`http://localhost:8080/Forums/feedback/book/${bookId}`);
            if (response.data && response.data.success) {
                setBookFeedback({ ...bookFeedback, [bookId]: response.data.data });
            }
        } catch (err) {
            console.error("Failed to fetch feedback:", err);
        }
    };

    const handleOpenEditBookDialog = (book) => {
        setEditingBook(book);
        setEditedBookData({
            title: book.title,
            author: book.author,
            edition: book.edition,
            ISBN: book.ISBN,
            condition: book.condition,
            description: book.description,
            isDigital: book.isDigital,
            digitalCopyPath: book.digitalCopyPath || '',
        });
        setEditBookDialogOpen(true);
    };

    const handleCloseEditBookDialog = () => {
        setEditBookDialogOpen(false);
        setEditingBook(null);
        setEditBookError(null); // Clear any previous edit errors
    };

    const handleEditBookInputChange = (e) => {
        const { name, value, type, checked } = e.target;
        setEditedBookData(prev => ({
            ...prev,
            [name]: type === 'checkbox' ? checked : value,
        }));
    };

    const handleEditBookSubmit = async () => {
        if (!editingBook?.bookId) return;
        setEditBookLoading(true);
        setEditBookError(null);
        try {
            const response = await axios.put(`http://localhost:8080/Books/${editingBook.bookId}`, editedBookData);
            if (response.data && response.data.success) {
                setSuccessMessage("Book updated successfully!");
                setTimeout(() => setSuccessMessage(null), 3000);
                await fetchCourseBooks(courses.find(c => c.courseId === editingBook.courseId?.id).courseName);
                handleCloseEditBookDialog();
            } else {
                setEditBookError(response.data?.message || "Failed to update book");
            }
        } catch (err) {
            setEditBookError(err.message);
        } finally {
            setEditBookLoading(false);
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
                        <div key={book.bookId} className="border-b border-gray-200 p-3 flex items-center justify-between">
                            <div>
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
                                <Button onClick={() => setFeedback({ bookId: book.bookId, feedbackText: '' })} variant="outline" className="mt-2">
                                    Add Feedback
                                </Button>
                                <Button onClick={() => fetchFeedback(book.bookId)} variant="outline" className="mt-2">
                                    View Feedback
                                </Button>
                                {bookFeedback[book.bookId] && (
                                    <div className="mt-2">
                                        <p><strong>Feedback:</strong></p>
                                        <ul>
                                            {bookFeedback[book.bookId].map(fb => (
                                                <li key={fb.feedbackId}>
                                                    {fb.feedback}
                                                    <Button onClick={() => handleRemoveFeedback(fb.feedbackId, book.bookId)} variant="outline" size="icon" className="ml-2">
                                                        X
                                                    </Button>
                                                </li>
                                            ))}
                                        </ul>
                                    </div>
                                )}
                                {feedback.bookId === book.bookId && (
                                    <div className="mt-2">
                                        <Input
                                            placeholder="Feedback"
                                            value={feedback.feedbackText}
                                            onChange={(e) => setFeedback({ ...feedback, feedbackText: e.target.value })}
                                            className="mb-2"
                                        />
                                        <Button onClick={() => handleFeedback(book.bookId, feedback.feedbackText)} disabled={feedbackLoading}>
                                            {feedbackLoading ? <Spinner /> : "Submit Feedback"}
                                        </Button>
                                        {feedbackError && <p className="text-red-500 mt-2">{feedbackError}</p>}
                                    </div>
                                )}
                            </div>
                            <div className="space-x-2">
                                <Button onClick={() => handleOpenEditBookDialog(book)} variant="outline" size="icon">
                                    Edit
                                </Button>
                                <Button onClick={() => handleRemoveBook(book.title)} variant="outline" size="icon">
                                    X
                                </Button>
                            </div>
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

            <Dialog open={editBookDialogOpen} onOpenChange={setEditBookDialogOpen}>
                <DialogContent className="sm:max-w-[425px]">
                    <DialogHeader>
                        <DialogTitle>Edit Book</DialogTitle>
                        <DialogDescription>
                            Edit the details of the selected book.
                        </DialogDescription>
                    </DialogHeader>
                    {editBookError && <p className="text-red-500 mb-4">{editBookError}</p>}
                    <div className="grid gap-4 py-4">
                        <div className="grid grid-cols-4 items-center gap-4">
                            <Label htmlFor="title" className="text-right">
                                Title
                            </Label>
                            <Input id="title" name="title" value={editedBookData.title} onChange={handleEditBookInputChange} className="col-span-3" />
                        </div>
                        <div className="grid grid-cols-4 items-center gap-4">
                            <Label htmlFor="author" className="text-right">
                                Author
                            </Label>
                            <Input id="author" name="author" value={editedBookData.author} onChange={handleEditBookInputChange} className="col-span-3" />
                        </div>
                        <div className="grid grid-cols-4 items-center gap-4">
                            <Label htmlFor="edition" className="text-right">
                                Edition
                            </Label>
                            <Input id="edition" name="edition" value={editedBookData.edition} onChange={handleEditBookInputChange} className="col-span-3" />
                        </div>
                        <div className="grid grid-cols-4 items-center gap-4">
                            <Label htmlFor="isbn" className="text-right">
                                ISBN
                            </Label>
                            <Input id="isbn" name="ISBN" value={editedBookData.ISBN} onChange={handleEditBookInputChange} className="col-span-3" />
                        </div>
                        <div className="grid grid-cols-4 items-center gap-4">
                            <Label htmlFor="condition" className="text-right">
                                Condition
                            </Label>
                            <Input id="condition" name="condition" value={editedBookData.condition} onChange={handleEditBookInputChange} className="col-span-3" />
                        </div>
                        <div className="grid grid-cols-4 items-center gap-4">
                            <Label htmlFor="description" className="text-right">
                                Description
                            </Label>
                            <Input id="description" name="description" value={editedBookData.description} onChange={handleEditBookInputChange} className="col-span-3" />
                        </div>
                        <div className="grid grid-cols-4 items-center gap-4">
                            <Label htmlFor="isDigital" className="text-right">
                                Is Digital
                            </Label>
                            <input
                                type="checkbox"
                                id="isDigital"
                                name="isDigital"
                                checked={editedBookData.isDigital}
                                onChange={handleEditBookInputChange}
                                className="col-span-3"
                            />
                        </div>
                        {editedBookData.isDigital && (
                            <div className="grid grid-cols-4 items-center gap-4">
                                <Label htmlFor="digitalCopyPath" className="text-right">
                                    Digital Path
                                </Label>
                                <Input
                                    id="digitalCopyPath"
                                    name="digitalCopyPath"
                                    value={editedBookData.digitalCopyPath}
                                    onChange={handleEditBookInputChange}
                                    className="col-span-3"
                                />
                            </div>
                        )}
                    </div>
                    <DialogFooter>
                        <Button type="button" variant="secondary" onClick={handleCloseEditBookDialog}>
                            Cancel
                        </Button>
                        <Button type="button" onClick={handleEditBookSubmit} disabled={editBookLoading}>
                            {editBookLoading ? <Spinner /> : "Save Changes"}
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>
        </div>
    );
}