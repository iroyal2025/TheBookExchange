// src/components/ui/input/Input.js
import React from 'react';

const Input = ({ ...props }) => {
    return <input {...props} className="border p-2 rounded" />;
};

export { Input };