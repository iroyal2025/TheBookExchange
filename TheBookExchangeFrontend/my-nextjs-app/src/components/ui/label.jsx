// src/components/ui/label.jsx
import React from 'react';

function Label({ htmlFor, children, className, ...props }) {
    return (
        <label htmlFor={htmlFor} className={className} {...props}>
            {children}
        </label>
    );
}

export default Label; // This is the key - it's a default export