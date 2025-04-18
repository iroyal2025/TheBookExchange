'use client';
import React, { useState, useEffect } from 'react';
import axios from '@/lib/axiosConfig';
import { useRouter } from 'next/navigation';

export default function ManageUsers() {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [isAdding, setIsAdding] = useState(false);
    const [newUser, setNewUser] = useState({ email: '', password: '', role: '', major: '', profilePicture: '' });
    const [isEditing, setIsEditing] = useState(false);
    const [editingUser, setEditingUser] = useState(null);
    const [isUpdatingPassword, setIsUpdatingPassword] = useState(false);
    const [passwordUpdateUser, setPasswordUpdateUser] = useState(null);
    const [newPassword, setNewPassword] = useState('');
    const [message, setMessage] = useState(null);
    const router = useRouter();

    useEffect(() => {
        fetchUsers();
    }, []);

    useEffect(() => {
        if (message) {
            const timer = setTimeout(() => {
                setMessage(null);
            }, 10000);
            return () => clearTimeout(timer);
        }
    }, [message]);

    const fetchUsers = async () => {
        try {
            const response = await axios.get('http://localhost:8080/Users/');
            if (response.data.success) {
                setUsers(response.data.data);
                setLoading(false);
            } else {
                setError(response.data.message);
                setLoading(false);
            }
        } catch (err) {
            setError(err.message);
            setLoading(false);
        }
    };

    const handleDelete = async (user) => {
        try {
            const response = await axios.delete(`http://localhost:8080/Users/${user.userId}`);
            if (response.data.success) {
                fetchUsers();
                setMessage("User deleted successfully.");
            } else {
                setError(response.data.message);
            }
        } catch (err) {
            setError(err.message);
        }
    };

    const handleAddUser = async () => {
        try {
            const response = await axios.post('http://localhost:8080/Users/add', newUser);
            if (response.data.success) {
                fetchUsers();
                setIsAdding(false);
                setNewUser({ email: '', password: '', role: '', major: '', profilePicture: '' });
                setMessage("User added successfully.");
            } else {
                setError(response.data.message);
            }
        } catch (err) {
            setError(err.message);
        }
    };

    const handleEdit = (user) => {
        setEditingUser({
            ...user,
            role: user.role || "",
            major: user.major || "",
            profilePicture: user.profilePicture || "",
            email: user.email || "",
            userId: user.userId,
        });
        setIsEditing(true);
    };

    const handleUpdateUser = async () => {
        try {
            if (editingUser && editingUser.userId) {
                const response = await axios.put(`http://localhost:8080/Users/${editingUser.userId}`, editingUser);
                if (response.data.success) {
                    fetchUsers();
                    setIsEditing(false);
                    setEditingUser(null);
                    setMessage("User updated successfully.");
                } else {
                    setError(response.data.message);
                }
            } else {
                setError("Editing user userId is missing.");
            }
        } catch (err) {
            setError(err.message);
        }
    };

    const handleActivate = async (user) => {
        try {
            const response = await axios.put(`http://localhost:8080/Users/${user.userId}/activate`);
            if (response.data.success) {
                fetchUsers();
                setMessage("User activation successful.");
            } else {
                setError(response.data.message);
            }
        } catch (err) {
            setError(err.message);
        }
    };

    const handleDeactivate = async (user) => {
        try {
            const response = await axios.put(`http://localhost:8080/Users/${user.userId}/deactivate`);
            if (response.data.success) {
                fetchUsers();
                setMessage("User deactivation successful.");
            } else {
                setError(response.data.message);
            }
        } catch (err) {
            setError(err.message);
        }
    };

    const handleUpdatePasswordRequest = (user) => {
        setPasswordUpdateUser(user);
        setIsUpdatingPassword(true);
        setNewPassword('');
    };

    const handleUpdatePassword = async () => {
        if (!passwordUpdateUser?.userId) {
            setError("User ID for password update is missing.");
            return;
        }
        if (!newPassword) {
            setError("New password cannot be empty.");
            return;
        }
        try {
            const response = await axios.put(
                `http://localhost:8080/Users/${passwordUpdateUser.userId}/password`,
                { newPassword: newPassword } // Changed key to 'newPassword'
            );
            if (response.data.success) {
                fetchUsers();
                setIsUpdatingPassword(false);
                setPasswordUpdateUser(null);
                setNewPassword('');
                setMessage("User password updated successfully.");
            } else {
                setError(response.data.message);
            }
        } catch (err) {
            setError(err.message);
        }
    };

    const handleCancelPasswordUpdate = () => {
        setIsUpdatingPassword(false);
        setPasswordUpdateUser(null);
        setNewPassword('');
    };

    const handleBackToDashboard = () => {
        router.push('/admin-dashboard');
    };

    if (loading) return <div className="p-4">Loading users...</div>;
    if (error) return <div className="p-4 text-red-600">Error loading users: {error}</div>;
    return (
        <div className="min-h-screen bg-gradient-to-r from-orange-500 to-green-500 flex flex-col items-center justify-center p-4">
            <div className="bg-white p-10 rounded-2xl shadow-2xl w-full max-w-4xl">
                <h2 className="text-2xl font-semibold mb-6 text-orange-600">Manage Users</h2>

                {message && <div className="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded relative mb-4" role="alert">
                    <span className="block sm:inline">{message}</span>
                </div>}

                <button className="bg-green-500 hover:bg-green-700 text-white font-bold py-2 px-4 rounded mb-4" onClick={() => setIsAdding(true)}>Add User</button>
                <button className="bg-gray-500 hover:bg-gray-700 text-white font-bold py-2 px-4 rounded mb-4" onClick={handleBackToDashboard}>Back to Dashboard</button>

                {isAdding && (
                    <div className="mb-4">
                        <h3 className="text-lg font-semibold mb-2">Add New User</h3>
                        <div className="flex flex-col gap-2">
                            <input type="email" placeholder="Email" value={newUser.email} onChange={e => setNewUser({ ...newUser, email: e.target.value })} className="border p-2 rounded" />
                            <input type="password" placeholder="Password" value={newUser.password} onChange={e => setNewUser({ ...newUser, password: e.target.value })} className="border p-2 rounded" />
                            <select value={newUser.role} onChange={e => setNewUser({ ...newUser, role: e.target.value })} className="border p-2 rounded">
                                <option value="">Select Role</option>
                                <option value="student">Student</option>
                                <option value="teacher">Teacher</option>
                                <option value="parent">Parent</option>
                                <option value="admin">Admin</option>
                            </select>
                            <input type="text" placeholder="Major" value={newUser.major} onChange={e => setNewUser({ ...newUser, major: e.target.value })} className="border p-2 rounded" />
                            <input type="text" placeholder="Profile Picture URL" value={newUser.profilePicture} onChange={e => setNewUser({ ...newUser, profilePicture: e.target.value })} className="border p-2 rounded" />
                            <button onClick={handleAddUser} className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded">Add</button>
                            <button onClick={() => setIsAdding(false)} className="bg-gray-400 hover:bg-gray-600 text-white font-bold py-2 px-4 rounded">Cancel</button>
                        </div>
                    </div>
                )}

                <div className="overflow-x-auto">
                    <table className="min-w-full bg-white border border-gray-200">
                        <thead>
                        <tr>
                            <th className="border p-2">Email</th>
                            <th className="border p-2">Role</th>
                            <th className="border p-2">Actions</th>
                        </tr>
                        </thead>
                        <tbody>
                        {users.map(user => (
                            <tr key={user.userId}>
                                <td className="border p-2">{user.email}</td>
                                <td className="border p-2">{user.role}</td>
                                <td className="border p-2 flex justify-center">
                                    <div className="flex flex-wrap justify-center gap-1">
                                        <button onClick={() => handleEdit(user)}
                                                className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-1 px-2 rounded">Edit
                                        </button>
                                        <button onClick={() => handleDelete(user)}
                                                className="bg-red-500 hover:bg-red-700 text-white font-bold py-1 px-2 rounded">Delete
                                        </button>
                                        <button
                                            onClick={() => user.isActive ? handleDeactivate(user) : handleActivate(user)}
                                            className={`font-bold py-1 px-2 rounded ${user.isActive ? 'bg-yellow-500 hover:bg-yellow-700 text-white' : 'bg-green-500 hover:bg-green-700 text-white'}`}>
                                            {user.isActive ? 'Deactivate' : 'Activate'}
                                        </button>
                                        <button

                                            className="bg-red-500 hover:bg-red-700 text-white font-bold py-1 px-2 rounded mr-2"

                                            onClick={() => handleDeactivate(user)}>Deactivate

                                        </button>
                                        <button
                                            className="bg-orange-500 hover:bg-orange-700 text-white font-bold py-1 px-2 rounded"
                                            onClick={() => handleUpdatePasswordRequest(user)}>Reset Password
                                        </button>
                                    </div>
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
                {isEditing && editingUser && (
                    <div className="mt-4">
                        <h3 className="text-lg font-semibold mb-2">Edit User</h3>
                        <div className="flex flex-col gap-2">
                            <input type="email" placeholder="Email" value={editingUser.email} onChange={e => setEditingUser({ ...editingUser, email: e.target.value })} className="border p-2 rounded" />
                            <select value={editingUser.role} onChange={e => setEditingUser({ ...editingUser, role: e.target.value })} className="border p-2 rounded">
                                <option value="">Select Role</option>
                                <option value="student">Student</option>
                                <option value="teacher">Teacher</option>
                                <option value="parent">Parent</option>
                                <option value="admin">Admin</option>
                            </select>
                            <input type="text" placeholder="Major" value={editingUser.major} onChange={e => setEditingUser({ ...editingUser, major: e.target.value })} className="border p-2 rounded" />
                            <input type="text" placeholder="Profile Picture URL" value={editingUser.profilePicture} onChange={e => setEditingUser({ ...editingUser, profilePicture: e.target.value })} className="border p-2 rounded" />
                            <button onClick={handleUpdateUser} className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded">Update</button>
                            <button onClick={() => setIsEditing(false)} className="bg-gray-400 hover:bg-gray-600 text-white font-bold py-2 px-4 rounded">Cancel</button>
                        </div>
                    </div>
                )}

                {isUpdatingPassword && passwordUpdateUser && (
                    <div className="mt-4">
                        <h3 className="text-lg font-semibold mb-2">Reset Password for {passwordUpdateUser.email}</h3>
                        <div className="flex flex-col gap-2">
                            <input
                                type="password"
                                placeholder="New Password"
                                value={newPassword}
                                onChange={e => setNewPassword(e.target.value)}
                                className="border p-2 rounded"
                            />
                            <button onClick={handleUpdatePassword} className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded">Update Password</button>
                            <button onClick={handleCancelPasswordUpdate} className="bg-gray-400 hover:bg-gray-600 text-white font-bold py-2 px-4 rounded">Cancel</button>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
}