// components/ui/dialog.jsx
import React, { useState, useEffect, useRef } from 'react';
import ReactDOM from 'react-dom';

const Dialog = ({ open, onOpenChange, children }) => {
    const [isBrowser, setIsBrowser] = useState(false);
    const dialogRef = useRef(null);

    useEffect(() => {
        setIsBrowser(typeof window !== 'undefined');
    }, []);

    useEffect(() => {
        const handleOutsideClick = (event) => {
            if (open && dialogRef.current && !dialogRef.current.contains(event.target)) {
                onOpenChange(false);
            }
        };

        if (open) {
            document.addEventListener('mousedown', handleOutsideClick);
        }

        return () => {
            document.removeEventListener('mousedown', handleOutsideClick);
        };
    }, [open, onOpenChange]);

    if (!isBrowser) return null;

    const dialogContent = open ? (
        ReactDOM.createPortal(
            <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
                <div ref={dialogRef} className="bg-white rounded-lg shadow-lg p-6 max-w-md">
                    {children}
                </div>
            </div>,
            document.body
        )
    ) : null;

    return dialogContent;
};

const DialogTrigger = ({ children, ...props }) => {
    const [open, setOpen] = useState(false);

    return (
        <>
            <button {...props} onClick={() => setOpen(true)}>
                {children}
            </button>
            <Dialog open={open} onOpenChange={setOpen}>
                {props.children}
            </Dialog>
        </>
    );
};

const DialogContent = ({ children, className }) => {
    return <div className={className}>{children}</div>;
};

const DialogHeader = ({ children }) => {
    return <div>{children}</div>;
};

const DialogTitle = ({ children }) => {
    return <h2 className="text-lg font-semibold">{children}</h2>;
};

const DialogDescription = ({ children }) => {
    return <p className="text-sm text-gray-500">{children}</p>;
};

const DialogFooter = ({ children, className, ...props }) => {
    return <div className={`flex justify-end space-x-2 ${className}`} {...props}>{children}</div>;
};

export { Dialog, DialogTrigger, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter };