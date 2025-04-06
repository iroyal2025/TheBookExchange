'use client';
import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { Button } from "@/components/ui/button";

export default function ReportsPage() {
    const [reports, setReports] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [deleteSuccess, setDeleteSuccess] = useState(null);

    useEffect(() => {
        const fetchReports = async () => {
            setLoading(true);
            try {
                const response = await axios.get('http://localhost:8080/Reports/');
                setReports(response.data.data);
                setLoading(false);
            } catch (err) {
                console.error('Error fetching reports:', err);
                setError('Failed to fetch reports.');
                setLoading(false);
            }
        };
        fetchReports();
    }, []);

    const handleDeleteReport = async (content) => {
        try {
            await axios.delete(`http://localhost:8080/Reports/delete?content=${content}`);
            setReports(reports.filter(report => report.content !== content));
            setDeleteSuccess("Report successfully deleted!");
            setTimeout(() => setDeleteSuccess(null), 3000); // Clear after 3 seconds
        } catch (err) {
            console.error('Error deleting report:', err);
            setError('Failed to delete report.');
        }
    };

    if (loading) return <div>Loading reports...</div>;
    if (error) return <div>{error}</div>;

    return (
        <div className="min-h-screen bg-gradient-to-r from-orange-500 to-green-500 flex flex-col items-center justify-center">
            {deleteSuccess && (
                <div className="fixed top-4 left-1/2 transform -translate-x-1/2 bg-green-500 text-white p-4 rounded-md z-50">
                    {deleteSuccess}
                </div>
            )}
            <div className="bg-white p-10 rounded-2xl shadow-2xl text-center w-full max-w-4xl">
                <h2 className="text-4xl font-bold text-orange-600 mb-6">View Reports</h2>

                {reports.length > 0 ? (
                    <ul className="space-y-4">
                        {reports.map((report) => (
                            <li key={report.content} className="border p-4 rounded-lg shadow-md">
                                <div className="grid grid-cols-3 gap-4">
                                    <div><strong className="text-blue-600">Book Title:</strong> {report.bookTitle}</div>
                                    <div><strong className="text-blue-600">User Email:</strong> {report.userEmail}</div>
                                    <div><strong className="text-blue-600">Content:</strong> {report.content}</div>
                                </div>
                                <div className="mt-4">
                                    <Button onClick={() => handleDeleteReport(report.content)} className="bg-red-500 text-white hover:bg-red-600">
                                        Delete Report
                                    </Button>
                                </div>
                            </li>
                        ))}
                    </ul>
                ) : (
                    <p className="text-gray-600">No reports found.</p>
                )}
            </div>
        </div>
    );
}