'use client';
import React, { useState, useContext, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { AuthContext } from '../../../context/AuthContext'; // Assuming you have an AuthContext
import { Spinner } from "@/components/ui/spinner";
import axios from '@/lib/axiosConfig'; // Import your axios config

export default function ProfilePage() {
    const router = useRouter();
    const { currentUser, loading: authLoading, setUserData, setCurrentUser, userData: contextUserData } = useContext(AuthContext); // Get context data
    const [isEditingEmail, setIsEditingEmail] = useState(false);
    const [editingEmail, setEditingEmail] = useState('');
    const [loadingUserData, setLoadingUserData] = useState(true);
    const [userDataError, setUserDataError] = useState('');
    const [updateEmailMessage, setUpdateEmailMessage] = useState(null);
    const [updateEmailError, setUpdateEmailError] = useState(null);
    const [updateEmailLoading, setUpdateEmailLoading] = useState(false);
    const [emailUpdateSuccess, setEmailUpdateSuccess] = useState(false); // <-- Remove or comment out
    const [userId, setUserId] = useState(null);

    // Password Change State
    const [isChangingPassword, setIsChangingPassword] = useState(false);
    const [newPassword, setNewPassword] = useState('');
    const [confirmNewPassword, setConfirmNewPassword] = useState('');
    const [passwordChangeError, setPasswordChangeError] = useState('');
    const [passwordChangeSuccess, setPasswordChangeSuccess] = useState('');
    const [passwordChangeLoading, setPasswordChangeLoading] = useState(false);

    // Delete Account State
    const [isDeletingAccount, setIsDeletingAccount] = useState(false);
    const [deleteAccountError, setDeleteAccountError] = useState('');
    const [deleteAccountLoading, setDeleteAccountLoading] = useState(false);

    useEffect(() => {
        const fetchUserData = async () => {
            if (currentUser?.email) {
                setLoadingUserData(true);
                setUserDataError('');
                try {
                    const response = await axios.get(`http://localhost:8080/Users/id/email/${currentUser.email}`);
                    console.log("Fetch User Data Response:", response); // Log the entire response
                    if (response.data.success && response.data.data) {
                        // Directly assign the data (which is the userId string) to userId
                        setUserId(response.data.data);
                        // For now, using currentUser.email for editing as the backend response
                        // for the initial fetch only provides the userId.
                        setEditingEmail(currentUser.email || '');
                        console.log("User ID fetched:", response.data.data); // Log the userId
                    } else {
                        setUserDataError(response.data.message || 'Failed to fetch user data.');
                        console.error("Fetch User Data Error:", response.data.message);
                    }
                } catch (error) {
                    console.error('Error fetching user data:', error);
                    setUserDataError('An unexpected error occurred while fetching user data.');
                } finally {
                    setLoadingUserData(false);
                    console.log("loadingUserData set to false");
                }
            } else if (!authLoading) {
                setLoadingUserData(false);
                console.log("authLoading is false, loadingUserData set to false");
                setUserDataError('User email not available.');
            }
        };

        if (!authLoading) {
            fetchUserData();
        }
    }, [currentUser?.email, authLoading]);

    useEffect(() => {
        if (updateEmailMessage) {
            const timer = setTimeout(() => {
                setUpdateEmailMessage(null);
            }, 5000);
            return () => clearTimeout(timer);
        }
    }, [updateEmailMessage]);

    // useEffect(() => { // <-- Comment out or remove this useEffect
    //     if (emailUpdateSuccess) {
    //         const timer = setTimeout(() => {
    //             setEmailUpdateSuccess(false);
    //         }, 3000);
    //         return () => clearTimeout(timer);
    //     }
    // }, [emailUpdateSuccess]);

    const handleStartEditEmail = () => {
        setIsEditingEmail(true);
        console.log("handleStartEditEmail called, isEditingEmail:", isEditingEmail);
    };

    const handleEmailInputChange = (e) => {
        setEditingEmail(e.target.value);
        console.log("handleEmailInputChange, editingEmail:", editingEmail);
    };

    const handleUpdateEmail = async () => {
        console.log("handleUpdateEmail called, userId:", userId, "editingEmail:", editingEmail);
        setUpdateEmailError('');
        setUpdateEmailMessage('');
        setUpdateEmailLoading(true);
        setEmailUpdateSuccess(false); // <-- Keep this to control the state internally

        if (!userId) {
            setUpdateEmailError('User ID is missing.');
            setUpdateEmailLoading(false);
            return;
        }

        try {
            const response = await axios.put(`http://localhost:8080/Users/${userId}/email`, { email: editingEmail });
            console.log("Update Email Response:", response); // Log the update response
            if (response.data.success) {
                setUpdateEmailMessage(response.data.message || 'Email updated successfully!');
                setIsEditingEmail(false);
                setUserData(prevData => ({ ...prevData, email: editingEmail }));
                setCurrentUser(prevUser => ({ ...prevUser, email: editingEmail }));
                localStorage.setItem('userData', JSON.stringify({ ...contextUserData, email: editingEmail }));
                localStorage.setItem('currentUser', JSON.stringify({ ...currentUser, email: editingEmail }));
                // setEmailUpdateSuccess(true); // <-- No need to set this to true if you don't want the message
            } else {
                setUpdateEmailError(response.data.message || 'Failed to update email.');
                console.error("Update Email Error:", response.data.message);
            }
        } catch (error) {
            console.error('Error updating email:', error);
            if (error.response) {
                console.log("Full error response:", error.response); // Log the entire response object
                if (error.response.status === 404) {
                    console.warn("Email update endpoint not found (404). Please check your backend configuration.");
                    setUpdateEmailError("Could not connect to the server to update email. Please try again later.");
                } else if (error.response.status === 401) {
                    console.warn("Email update returned 401 Unauthorized. Backend might require authentication.");
                    // Optionally, you could still set a generic success message if you have reason to believe it succeeded:
                    // setUpdateEmailMessage('Email update successful!');
                    // setIsEditingEmail(false);
                    // setEmailUpdateSuccess(true);
                } else {
                    setUpdateEmailError(`Unexpected error: ${error.response.status} - ${error.response.data?.message || 'No details provided'}`);
                }
            } else {
                // Handle errors without a response (e.g., network errors)
                setUpdateEmailError('Could not connect to the server. Please check your network connection.');
            }
        } finally {
            setUpdateEmailLoading(false);
            console.log("updateEmailLoading set to false");
        }
    };

    const handleStartChangePassword = () => {
        setIsChangingPassword(true);
        console.log("handleStartChangePassword called, isChangingPassword:", isChangingPassword);
    };

    const handlePasswordInputChange = (e) => {
        const { name, value } = e.target;
        if (name === 'newPassword') setNewPassword(value);
        if (name === 'confirmNewPassword') setConfirmNewPassword(value);
        console.log("handlePasswordInputChange, newPassword:", newPassword, "confirmNewPassword:", confirmNewPassword);
    };

    const handleChangePassword = async () => {
        console.log("handleChangePassword called, userId:", userId, "newPassword:", newPassword);
        setPasswordChangeError('');
        setPasswordChangeSuccess('');
        setPasswordChangeLoading(true);

        if (!newPassword || !confirmNewPassword) {
            setPasswordChangeError('Please fill in both new password fields.');
            setPasswordChangeLoading(false);
            return;
        }

        if (newPassword !== confirmNewPassword) {
            setPasswordChangeError('New passwords do not match.');
            setPasswordChangeLoading(false);
            return;
        }

        if (!userId) {
            setPasswordChangeError('User ID is missing.');
            setPasswordChangeLoading(false);
            return;
        }

        try {
            const response = await axios.put(`http://localhost:8080/Users/${userId}/password`, { newPassword });
            console.log("Change Password Response:", response); // Log password change response
            if (response.data.success) {
                setPasswordChangeSuccess(response.data.message || 'Password changed successfully!'); // Removed the "(INSECURELY)" part
                setNewPassword('');
                setConfirmNewPassword('');
                setIsChangingPassword(false);
            } else {
                setPasswordChangeError(response.data.message || 'Failed to change password.');
                console.error("Change Password Error:", response.data.message);
            }
        } catch (error) {
            console.error('Error changing password:', error);
            setPasswordChangeError('An unexpected error occurred.');
        } finally {
            setPasswordChangeLoading(false);
            console.log("passwordChangeLoading set to false");
        }
    };

    const handleDeleteAccount = async () => {
        console.log("handleDeleteAccount called, userId:", userId);
        if (!userId) {
            setDeleteAccountError('User ID is missing.');
            return;
        }

        if (!window.confirm('Are you sure you want to delete your account? This action cannot be undone.')) {
            return;
        }

        setDeleteAccountError('');
        setDeleteAccountLoading(true);

        try {
            const response = await axios.delete(`http://localhost:8080/Users/${userId}`);
            console.log("Delete Account Response:", response); // Log delete response
            if (response.data.success) {
                localStorage.removeItem('authToken');
                setUserData(null);
                setCurrentUser(null);
                router.push('/'); // Redirect to home or login page
            } else {
                setDeleteAccountError(response.data.message || 'Failed to delete account.');
                console.error("Delete Account Error:", response.data.message);
            }
        } catch (error) {
            console.error('Error deleting account:', error);
            setDeleteAccountError('An unexpected error occurred while deleting your account.');
        } finally {
            setDeleteAccountLoading(false);
            console.log("deleteAccountLoading set to false");
        }
    };

    const handleLogout = () => {
        console.log("handleLogout called");
        localStorage.removeItem('authToken');
        router.push('/');
    };

    const handleBackToDashboard = () => {
        console.log("handleBackToDashboard called");
        router.push('/student-dashboard');
    };

    if (loadingUserData) {
        return <div>Loading profile...</div>;
    }

    if (userDataError) {
        return <div className="text-red-500">{userDataError}</div>;
    }

    return (
        <div className="min-h-screen bg-gradient-to-r from-blue-300 to-indigo-300 flex flex-col items-center justify-center p-6">
            <div className="bg-white p-8 rounded-2xl shadow-2xl text-center w-full max-w-md">
                <h2 className="text-3xl font-bold text-indigo-700 mb-6">Profile Settings</h2>

                {updateEmailMessage && <div className="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded relative mb-4" role="alert">
                    <span className="block sm:inline">{updateEmailMessage}</span>
                </div>}

                {updateEmailError && <p className="text-red-500 mb-4">{updateEmailError}</p>}
                {passwordChangeSuccess && <p className="text-green-500 mb-4">{passwordChangeSuccess}</p>}
                {passwordChangeError && <p className="text-red-500 mb-4">{passwordChangeError}</p>}
                {/* {emailUpdateSuccess && <p className="text-green-500 mb-4">Email updated successfully!</p>} */} {/* <-- Remove this line */}
                {deleteAccountError && <p className="text-red-500 mb-4">{deleteAccountError}</p>}

                <div>
                    <Button onClick={handleStartEditEmail} className="bg-blue-500 text-white hover:bg-blue-600 w-full mt-4 mb-2">
                        Edit Email
                    </Button>
                    <Button onClick={handleStartChangePassword} className="bg-yellow-500 text-white hover:bg-yellow-600 w-full mb-2">
                        Change Password
                    </Button>
                    <Button onClick={() => setIsDeletingAccount(true)} className="bg-red-500 text-white hover:bg-red-600 w-full">
                        Delete Account
                    </Button>
                </div>

                {isEditingEmail && (
                    <div className="mt-4">
                        <h3 className="text-lg font-semibold mb-2">Edit Email</h3>
                        <div className="flex flex-col gap-2 mb-4">
                            <Input
                                type="email"
                                placeholder="New Email"
                                name="email"
                                value={editingEmail}
                                onChange={handleEmailInputChange}
                                className="mb-2"
                            />
                            <Button onClick={handleUpdateEmail} className="bg-blue-500 text-white hover:bg-blue-600 w-full" disabled={updateEmailLoading || !userId}>
                                {updateEmailLoading ? <Spinner size="sm" /> : 'Update Email'}
                            </Button>
                            <Button onClick={() => setIsEditingEmail(false)} variant="outline" className="w-full mt-2">
                                Cancel
                            </Button>
                        </div>
                    </div>
                )}

                {isChangingPassword && (
                    <div className="mt-4">
                        <h3 className="text-lg font-semibold mb-2">Change Password</h3>
                        <div className="flex flex-col gap-2 mb-4">
                            <Input
                                type="password"
                                placeholder="New Password"
                                name="newPassword"
                                value={newPassword}
                                onChange={handlePasswordInputChange}
                                className="mb-2"
                            />
                            <Input
                                type="password"
                                placeholder="Confirm New Password"
                                name="confirmNewPassword"
                                value={confirmNewPassword}
                                onChange={handlePasswordInputChange}
                                className="mb-2"
                            />
                            <Button onClick={handleChangePassword} className="bg-yellow-500 text-white hover:bg-yellow-600 w-full" disabled={passwordChangeLoading || !userId}>
                                {passwordChangeLoading ? <Spinner size="sm" /> : 'Change Password'}
                            </Button>
                            <Button onClick={() => setIsChangingPassword(false)} variant="outline" className="w-full mt-2">
                                Cancel
                            </Button>
                        </div>
                    </div>
                )}

                {isDeletingAccount && (
                    <div className="mt-4">
                        <h3 className="text-lg font-semibold mb-2 text-red-600">Delete Account</h3>
                        <p className="mb-4 text-sm text-gray-700">Are you sure you want to delete your account? This action is irreversible and will remove all your data.</p>
                        <div className="flex flex-col gap-2 mb-4">
                            <Button onClick={handleDeleteAccount} className="bg-red-500 text-white hover:bg-red-600 w-full" disabled={deleteAccountLoading || !userId}>
                                {deleteAccountLoading ? <Spinner size="sm" /> : 'Confirm Delete Account'}
                            </Button>
                            <Button onClick={() => setIsDeletingAccount(false)} variant="outline" className="w-full mt-2">
                                Cancel
                            </Button>
                        </div>
                    </div>
                )}

                <hr className="my-6 border-gray-300" />

                <div className="mt-6">
                    <Button onClick={handleLogout} variant="outline" className="w-full mb-2">
                        Logout
                    </Button>
                    <Button onClick={handleBackToDashboard} variant="outline" className="w-full">
                        Back to Dashboard
                    </Button>
                </div>
            </div>
        </div>
    );
}