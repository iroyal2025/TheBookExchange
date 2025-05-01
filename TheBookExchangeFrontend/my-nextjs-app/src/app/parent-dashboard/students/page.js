'use client';
import React, { useState, useEffect, useContext } from 'react';
import axios from '@/lib/axiosConfig';
import { AuthContext } from '../../../context/AuthContext';
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Spinner } from "@/components/ui/spinner";
import { useRouter } from 'next/navigation';
import MainLayout from '@/components/MainLayout';

export default function ViewStudents() {
    const [students, setStudents] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [studentTransactions, setStudentTransactions] = useState({});
    const [transactionLoading, setTransactionLoading] = useState({});
    const { currentUser } = useContext(AuthContext);
    const [editBalance, setEditBalance] = useState({ studentId: null, balance: '' });
    const [editBalanceLoading, setEditBalanceLoading] = useState(false);
    const [editBalanceError, setEditBalanceError] = useState(null);
    const [removeLoading, setRemoveLoading] = useState(null);
    const [addStudentEmail, setAddStudentEmail] = useState('');
    const [addStudentLoading, setAddStudentLoading] = useState(false);
    const [addStudentError, setAddStudentError] = useState(null);
    const [addStudentSuccess, setAddStudentSuccess] = useState(null);
    const [studentBooks, setStudentBooks] = useState({});
    const router = useRouter();

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

    const fetchStudentTransactions = async (studentEmail) => {
        setTransactionLoading(prev => ({ ...prev, [studentEmail]: true }));
        try {
            const response = await axios.get(`http://localhost:8080/Transactions/student/email/${studentEmail}`);
            if (response.data) {
                setStudentTransactions(prev => ({ ...prev, [studentEmail]: response.data }));
            }
        } catch (err) {
            setError('Error fetching transactions: ' + err.message);
        } finally {
            setTransactionLoading(prev => ({ ...prev, [studentEmail]: false }));
        }
    };

    const handleEditBalanceClick = (studentId) => { // Renamed for clarity
        setEditBalance({ studentId, balance: '' });
    };

    const handleEditBalanceInputChange = (e) => {
        setEditBalance(prev => ({ ...prev, balance: e.target.value }));
    };

    const handleSaveBalance = async () => {
        setEditBalanceLoading(true);
        setEditBalanceError(null);
        try {
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

    const handleCancelEditBalance = () => {
        setEditBalance({ studentId: null, balance: '' });
        setEditBalanceError(null);
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
        setAddStudentSuccess(null);
        try {
            if (!addStudentEmail.trim()) {
                setAddStudentError("Please enter a student email.");
                return;
            }

            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (!emailRegex.test(addStudentEmail)) {
                setAddStudentError("Invalid email address.");
                return;
            }

            const response = await axios.post(`http://localhost:8080/Users/students/email/${currentUser.email}/student/${addStudentEmail}`);
            if (response.data.success) {
                setStudents([...students, { email: addStudentEmail, balance: 0 }]);
                setAddStudentEmail('');
                setAddStudentSuccess("Student added successfully!");
            } else {
                if (response.data.message.includes("Student not found")) {
                    setAddStudentError("Student not found.");
                } else {
                    setAddStudentError(response.data.message);
                }
            }
        } catch (err) {
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

    const handleBuyBookForStudent = (studentEmail) => {
        router.push(`/parent-dashboard/students/textbooks?studentEmail=${studentEmail}`);
    };

    if (loading) return <div className="p-4 flex justify-center items-center"><Spinner /></div>;
    if (error) return <div className="p-4 text-red-600">Error loading students: {error}</div>;

    return (
        <MainLayout>
            <div className="p-6 space-y-4">
                <h2 className="text-2xl font-semibold">View Students</h2>

                <div className="mb-4">
                    <h3 className="text-lg font-semibold mb-2">Add Student</h3>
                    <Input
                        type="email"
                        placeholder="Student Email"
                        value={addStudentEmail}
                        onChange={(e) => setAddStudentEmail(e.target.value)}
                        className="mb-2"
                    />
                    <Button onClick={handleAddStudent} disabled={addStudentLoading}>
                        {addStudentLoading ? <Spinner size="sm" /> : "Add Student"}
                    </Button>
                    {addStudentError && <p className="text-red-500 mt-1">{addStudentError}</p>}
                    {addStudentSuccess && <p className="text-green-500 mt-1">{addStudentSuccess}</p>}
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
                                <React.Fragment key={student.email}>
                                    <tr className="border-b border-gray-200">
                                        <td className="p-2">{student.email}</td>
                                        <td className="p-2">
                                            {editBalance.studentId === student.email ? (
                                                <div className="flex items-center space-x-2">
                                                    <Input
                                                        type="number"
                                                        value={editBalance.balance}
                                                        onChange={handleEditBalanceInputChange}
                                                        className="w-24"
                                                    />
                                                    <Button onClick={handleSaveBalance} disabled={editBalanceLoading}>
                                                        {editBalanceLoading ? <Spinner size="sm" /> : "Save"}
                                                    </Button>
                                                    <Button onClick={handleCancelEditBalance} variant="secondary">
                                                        Cancel
                                                    </Button>
                                                    {editBalanceError && <p className="text-red-500 mt-1">{editBalanceError}</p>}
                                                </div>
                                            ) : (
                                                <span>${student.balance}</span>
                                            )}
                                        </td>
                                        <td className="p-2 space-x-2">
                                            {editBalance.studentId !== student.email && (
                                                <Button onClick={() => handleEditBalanceClick(student.email)}>Edit Balance</Button>
                                            )}
                                            <Button onClick={() => handleRemoveStudent(student.email)} disabled={removeLoading === student.email}>
                                                {removeLoading === student.email ? <Spinner size="sm" /> : "Remove"}
                                            </Button>
                                            <Button onClick={() => handleBuyBookForStudent(student.email)}>Buy Book</Button>
                                            <Button onClick={() => fetchStudentTransactions(student.email)}>View Transactions</Button>
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

                                    {studentTransactions[student.email] && (
                                        <tr className="bg-gray-50">
                                            <td colSpan={4} className="p-4">
                                                <h4 className="font-semibold mb-2">Transactions</h4>
                                                {transactionLoading[student.email] ? (
                                                    <Spinner />
                                                ) : (
                                                    <table className="w-full text-sm border">
                                                        <thead>
                                                        <tr className="bg-gray-200">
                                                            <th className="p-2 text-left">Book ID</th>
                                                            <th className="p-2 text-left">Order Status</th>
                                                        </tr>
                                                        </thead>
                                                        <tbody>
                                                        {studentTransactions[student.email].map((tx, i) => (
                                                            <tr key={i}>
                                                                <td className="p-2">{tx.bookId}</td>
                                                                <td className="p-2">{tx["order status"]}</td>
                                                            </tr>
                                                        ))}
                                                        </tbody>
                                                    </table>
                                                )}
                                            </td>
                                        </tr>
                                    )}
                                </React.Fragment>
                            ))}
                            </tbody>
                        </table>
                    </div>
                ) : (
                    <p>No students found.</p>
                )}
            </div>
        </MainLayout>
    );
}