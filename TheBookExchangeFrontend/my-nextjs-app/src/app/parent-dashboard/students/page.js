'use client';
import React, { useState, useEffect, useContext } from 'react';
import axios from '@/lib/axiosConfig';
import { AuthContext } from '../../../context/AuthContext';
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Spinner } from "@/components/ui/spinner";

// Assuming you have a layout component like MainLayout
import MainLayout from '@/components/MainLayout'; // Adjust path as needed

export default function ViewStudents() {
    const [students, setStudents] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const { currentUser } = useContext(AuthContext);
    const [editBalance, setEditBalance] = useState({
        studentId: null,
        balance: '',
    });
    const [editBalanceLoading, setEditBalanceLoading] = useState(false);
    const [editBalanceError, setEditBalanceError] = useState(null);
    const [removeLoading, setRemoveLoading] = useState(null);
    const [addStudentEmail, setAddStudentEmail] = useState('');
    const [addStudentLoading, setAddStudentLoading] = useState(false);
    const [addStudentError, setAddStudentError] = useState(null);
    const [studentBooks, setStudentBooks] = useState({});

    useEffect(() => {
        const fetchStudents = async () => {
            setLoading(true);
            setError(null);
            try {
                if (currentUser?.email) {
                    const response = await axios.get(`http://localhost:8080/Users/students/email/${currentUser.email}`);
                    if (response.data.success) {
                        setStudents(response.data.data);
                    } else {
                        setError(response.data.message);
                    }
                }
            } catch (err) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };

        fetchStudents();
    }, [currentUser]);

    const handleEditBalance = (studentId) => {
        setEditBalance({ studentId, balance: '' });
    };

    const handleSaveBalance = async () => {
        setEditBalanceLoading(true);
        setEditBalanceError(null);
        try {
            // Corrected URL to use student's email as path variable
            const response = await axios.put(`http://localhost:8080/Users/balance/email/${editBalance.studentId}?balance=${editBalance.balance}`);

            if (response.data.success) {
                setStudents(students.map(student =>
                    student.email === editBalance.studentId ? { ...student, balance: editBalance.balance } : student
                ));
                setEditBalance({ studentId: null, balance: '' });
            } else {
                setEditBalanceError(response.data.message);
            }
        } catch (err) {
            setEditBalanceError(err.message);
        } finally {
            setEditBalanceLoading(false);
        }
    };

    const handleRemoveStudent = async (studentId) => {
        setRemoveLoading(studentId);
        try {
            const response = await axios.delete(`http://localhost:8080/Users/students/email/${currentUser.email}/student/${studentId}`);
            if (response.data.success) {
                setStudents(students.filter(student => student.email !== studentId));
            } else {
                setError(response.data.message);
            }
        } catch (err) {
            setError(err.message);
        } finally {
            setRemoveLoading(null);
        }
    };


    const handleAddStudent = async () => {
        setAddStudentLoading(true);
        setAddStudentError(null);
        try {
            if (!addStudentEmail.trim()) {
                setAddStudentError("Please enter a student email.");
                return; // Stop the function if the input is empty
            }

            // Basic email validation using a regular expression
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (!emailRegex.test(addStudentEmail)) {
                setAddStudentError("Invalid email address.");
                return; // Stop the function if the email is invalid
            }

            const response = await axios.post(`http://localhost:8080/Users/students/email/${currentUser.email}/student/${addStudentEmail}`);

            if (response.data.success) {
                setStudents([...students, { email: addStudentEmail, balance: 0 }]);
                setAddStudentEmail('');
            } else {
                // Check for specific error messages from the backend
                if (response.data.message.includes("Student not found")) {
                    setAddStudentError("Student not found.");
                } else {
                    setAddStudentError(response.data.message); // Display generic error if no specific message
                }
            }
        } catch (err) {
            // Check for 404 error or 500 with "Student not found"
            if (err.response) {
                if (err.response.status === 404 || (err.response.status === 500 && err.response.data.message.includes("Student not found"))) {
                    setAddStudentError("Student not found.");
                } else {
                    setAddStudentError(err.message);
                }
            } else {
                setAddStudentError(err.message);
            }
        } finally {
            setAddStudentLoading(false);
        }
    };

    const fetchStudentBooks = async (studentEmail) => {
        try {
            const response = await axios.get(`http://localhost:8080/Books/owned/email/${studentEmail}`);
            if (response.data.success) {
                setStudentBooks({ ...studentBooks, [studentEmail]: response.data.data });
            } else {
                setStudentBooks({ ...studentBooks, [studentEmail]: [] });
            }
        } catch (err) {
            console.error('Error fetching student books:', err);
            setStudentBooks({ ...studentBooks, [studentEmail]: [] });
        }
    };

    if (loading) return <div className="p-4 flex justify-center items-center"><Spinner /></div>;
    if (error) return <div className="p-4 text-red-600">Error loading students: {error}</div>;

    return (
        <MainLayout> {/* Wrap with the layout component */}
            <div className="p-6 space-y-4"> {/* Consistent padding and spacing */}
                <h2 className="text-2xl font-semibold">View Students</h2>

                {/* Add Student Section */}
                <div className="mb-4">
                    <h3 className="text-lg font-semibold mb-2">Add Student</h3>
                    <Input type="email" placeholder="Student Email" value={addStudentEmail}
                           onChange={(e) => setAddStudentEmail(e.target.value)} className="mb-2" />
                    <Button onClick={handleAddStudent} disabled={addStudentLoading}>
                        {addStudentLoading ? <Spinner size="sm" /> : "Add Student"}
                    </Button>
                    {addStudentError && <p className="text-red-500 mt-1">{addStudentError}</p>}
                </div>

                {students.length > 0 ? (
                    <div className="overflow-x-auto">
                        <table className="min-w-full border border-gray-200">
                            <thead>
                            <tr>
                                <th className="p-2 text-left">Email</th>
                                <th className="p-2 text-left">Balance</th>
                                <th className="p-2 text-left">Actions</th>
                                <th className="p-2 text-left">Books</th>
                            </tr>
                            </thead>
                            <tbody>
                            {students.map(student => (
                                <tr key={student.email} className="border-b border-gray-200">
                                    <td className="p-2">{student.email}</td>
                                    <td className="p-2">${student.balance}</td>
                                    <td className="p-2">
                                        <Button onClick={() => handleEditBalance(student.email)} className="mr-2">Edit Balance</Button>
                                        <Button onClick={() => handleRemoveStudent(student.email)} disabled={removeLoading === student.email}>
                                            {removeLoading === student.email ? <Spinner size="sm" /> : "Remove"}
                                        </Button>
                                    </td>
                                    <td className="p-2">
                                        <Button onClick={() => fetchStudentBooks(student.email)}>View Books</Button>
                                        {studentBooks[student.email] && (
                                            <ul className="mt-2">
                                                {studentBooks[student.email].map(book => (
                                                    <li key={book.bookId}>{book.title}</li>
                                                ))}
                                            </ul>
                                        )}
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>
                ) : (
                    <p>No students found.</p>
                )}

                {editBalance.studentId && (
                    <div className="mt-4">
                        <h3 className="text-lg font-semibold mb-2">Edit Balance for {editBalance.studentId}</h3>
                        <Input type="number" placeholder="New Balance" value={editBalance.balance}
                               onChange={(e) => setEditBalance({ ...editBalance, balance: e.target.value })}
                               className="mb-2" />
                        <Button onClick={handleSaveBalance} disabled={editBalanceLoading}>
                            {editBalanceLoading ? <Spinner size="sm" /> : "Save"}
                        </Button>
                        {editBalanceError && <p className="text-red-500 mt-1">{editBalanceError}</p>}
                    </div>
                )}
            </div>
        </MainLayout>
    );
}