'use client';
import React, { useState, useEffect } from 'react';
import axios from '@/lib/axiosConfig'; // Updated import path

export default function ManageUsers() {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [isAdding, setIsAdding] = useState(false);
    const [newUser, setNewUser] = useState({ email: '', password: '', role: '', major: '', profilePicture: '' });
    const [isEditing, setIsEditing] = useState(false);
    const [editingUser, setEditingUser] = useState(null);

    useEffect(() => {
        fetchUsers();
    }, []);

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

    const handleDelete = async (email) => {
        try {
            const response = await axios.delete(`http://localhost:8080/Users/delete?email=${email}`);
            if (response.data.success) {
                fetchUsers();
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
            } else {
                setError(response.data.message);
            }
        } catch (err) {
            setError(err.message);
        }
    };

    const handleEdit = (user) => {
        setEditingUser(user);
        setIsEditing(true);
    };

    const handleUpdateUser = async () => {
        try {
            const response = await axios.put(`http://localhost:8080/Users/${editingUser.reference.id}`, editingUser);
            if (response.data.success) {
                fetchUsers();
                setIsEditing(false);
                setEditingUser(null);
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

                <button className="bg-green-500 hover:bg-green-700 text-white font-bold py-2 px-4 rounded mb-4" onClick={() => setIsAdding(true)}>Add User</button>

                {isAdding && (
                    <div className="mb-4">
                        <h3 className="text-lg font-semibold mb-2">Add New User</h3>
                        <input className="border p-2 mb-2 w-full" type="email" placeholder="Email" value={newUser.email} onChange={(e) => setNewUser({ ...newUser, email: e.target.value })} />
                        <input className="border p-2 mb-2 w-full" type="password" placeholder="Password" value={newUser.password} onChange={(e) => setNewUser({ ...newUser, password: e.target.value })} />
                        <input className="border p-2 mb-2 w-full" type="text" placeholder="Role" value={newUser.role} onChange={(e) => setNewUser({ ...newUser, role: e.target.value })} />
                        <input className="border p-2 mb-2 w-full" type="text" placeholder="Major" value={newUser.major} onChange={(e) => setNewUser({ ...newUser, major: e.target.value })} />
                        <input className="border p-2 mb-2 w-full" type="text" placeholder="Profile Picture URL" value={newUser.profilePicture} onChange={(e) => setNewUser({ ...newUser, profilePicture: e.target.value })} />
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
                                    <button className="bg-red-500 hover:bg-red-700 text-white font-bold py-1 px-2 rounded" onClick={() => handleDelete(user.email)}>Delete</button>
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>

                {isEditing && editingUser && (
                    <div className="mt-4">
                        <h3 className="text-lg font-semibold mb-2">Edit User</h3>
                        <input className="border p-2 mb-2 w-full" type="email" placeholder="Email" value={editingUser.email} onChange={(e) => setEditingUser({ ...editingUser, email: e.target.value })} />
                        <input className="border p-2 mb-2 w-full" type="text" placeholder="Role" value={editingUser.role} onChange={(e) => setEditingUser({ ...editingUser, role: e.target.value })} />
                        <input className="border p-2 mb-2 w-full" type="text" placeholder="Major" value={editingUser.major} onChange={(e) => setEditingUser({ ...editingUser, major: e.target.value })} />
                        <input className="border p-2 mb-2 w-full" type="text" placeholder="Profile Picture URL" value={editingUser.profilePicture} onChange={(e) => setEditingUser({ ...editingUser, profilePicture: e.target.value })} />
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