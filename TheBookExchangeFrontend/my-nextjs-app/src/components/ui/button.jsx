// src/components/ui/button.jsx
import React from 'react';

export const Button = ({ children, variant = 'default', className = '', ...props }) => {
    let buttonClasses = 'px-4 py-2 rounded'; // Base button styles

    // Apply variant-specific styles
    switch (variant) {
        case 'outline':
            buttonClasses += ' border border-gray-300 bg-transparent hover:bg-gray-100';
            break;
        case 'default':
        default:
            buttonClasses += ' bg-blue-500 text-white hover:bg-blue-600';
            break;
    }

    // Apply additional classNames
    buttonClasses += ' ' + className;

    return (
        <button className={buttonClasses} {...props}>
            {children}
        </button>
    );
};