'use client';
import React, { useState, useEffect } from 'react';
import axios from '@/lib/axiosConfig';

export default function ManageUsers() {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [isAdding, setIsAdding] = useState(false);
    const [newUser, setNewUser] = useState({ email: '', password: '', role: '', major: '', profilePicture: '' });
    const [isEditing, setIsEditing] = useState(false);
    const [editingUser, setEditingUser] = useState(null);
    const [message, setMessage] = useState(null);

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

                {isAdding && (
                    <div className="mb-4">
                        <h3 className="text-lg font-semibold mb-2">Add New User</h3>
                        <div className="mb-2">
                            <label className="block text-sm font-medium text-gray-700">Email</label>
                            <input
                                type="email"
                                value={newUser.email}
                                onChange={(e) => setNewUser({ ...newUser, email: e.target.value })}
                                className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
                            />
                        </div>
                        <div className="mb-2">
                            <label className="block text-sm font-medium text-gray-700">Password</label>
                            <input
                                type="password"
                                value={newUser.password}
                                onChange={(e) => setNewUser({ ...newUser, password: e.target.value })}
                                className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
                            />
                        </div>
                        <div className="mb-2">
                            <label className="block text-sm font-medium text-gray-700">Role</label>
                            <input
                                type="text"
                                value={newUser.role}
                                onChange={(e) => setNewUser({ ...newUser, role: e.target.value })}
                                className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
                            />
                        </div>
                        <div className="mb-2">
                            <label className="block text-sm font-medium text-gray-700">Major</label>
                            <input
                                type="text"
                                value={newUser.major}
                                onChange={(e) => setNewUser({ ...newUser, major: e.target.value })}
                                className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
                            />
                        </div>
                        <div className="mb-2">
                            <label className="block text-sm font-medium text-gray-700">Profile Picture URL</label>
                            <input
                                type="text"
                                value={newUser.profilePicture}
                                onChange={(e) => setNewUser({ ...newUser, profilePicture: e.target.value })}
                                className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
                            />
                        </div>
                        <div className="flex justify-end">
                            <button className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded mr-2" onClick={handleAddUser}>Add</button>
                            <button className="bg-gray-400 hover:bg-gray-600 text-white font-bold py-2 px-4 rounded" onClick={() => setIsAdding(false)}>Cancel</button>
                        </div>
                    </div>
                )}

                <div className="overflow-x-auto">
                    <table className="min-w-full border border-gray-200">
                        <thead>
                        <tr className="bg-gray-100">
                            <th className="border border-gray-200 p-2 text-left">Email</th>
                            <th className="border border-gray-200 p-2 text-left">Role</th>
                            <th className="border border-gray-200 p-2 text-left">Actions</th>
                        </tr>
                        </thead>
                        <tbody>
                        {users.map((user) => (
                            <tr key={user.email} className="border-b border-gray-200">
                                <td className="p-2">{user.email}</td>
                                <td className="p-2">{user.role}</td>
                                <td className="p-2">
                                    <button className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-1 px-2 rounded mr-2" onClick={() => handleEdit(user)}>Edit</button>
                                    <button className="bg-red-500 hover:bg-red-700 text-white font-bold py-1 px-2 rounded mr-2" onClick={() => handleDelete(user)}>Delete</button>
                                    <button className="bg-green-500 hover:bg-green-700 text-white font-bold py-1 px-2 rounded mr-2" onClick={() => handleActivate(user)}>Activate</button>
                                    <button className="bg-orange-500 hover:bg-orange-700 text-white font-bold py-1 px-2 rounded mr-2" onClick={() => handleDeactivate(user)}>Deactivate</button>
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>

                {isEditing && editingUser && (
                    <div className="mt-4">
                        <h3 className="text-lg font-semibold mb-2">Edit User</h3>
                        <div className="mb-2">
                            <label className="block text-sm font-medium text-gray-700">Email</label>
                            <input
                                type="email"
                                value={editingUser.email}
                                onChange={(e) => setEditingUser({ ...editingUser, email: e.target.value })}
                                className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
                            />
                        </div>
                        <div className="mb-2">
                            <label className="block text-sm font-medium text-gray-700">Role</label>
                            <input
                                type="text"
                                value={editingUser.role}
                                onChange={(e) => setEditingUser({ ...editingUser, role: e.target.value })}
                                className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
                            />
                        </div>
                        <div className="mb-2">
                            <label className="block text-sm font-medium text-gray-700">Major</label>
                            <input
                                type="text"
                                value={editingUser.major}
                                onChange={(e) => setEditingUser({ ...editingUser, major: e.target.value })}
                                className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
                            />
                        </div>
                        <div className="mb-2">
                            <label className="block text-sm font-medium text-gray-700">Profile Picture URL</label>
                            <input
                                type="text"
                                value={editingUser.profilePicture}
                                onChange={(e) => setEditingUser({ ...editingUser, profilePicture: e.target.value })}
                                className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
                            />
                        </div>
                        <div className="flex justify-end">
                            <button className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded mr-2" onClick={handleUpdateUser}>Update</button>
                            <button className="bg-gray-400 hover:bg-gray-600 text-white font-bold py-2 px-4 rounded" onClick={() => setIsEditing(false)}>Cancel</button>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
}