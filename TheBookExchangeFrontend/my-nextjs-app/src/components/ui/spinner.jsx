// components/ui/Spinner.jsx

import React from 'react';

const Spinner = ({ size = "md" }) => {
    let spinnerSize = "w-6 h-6"; // Default size
    if (size === "sm") {
        spinnerSize = "w-4 h-4";
    } else if (size === "lg") {
        spinnerSize = "w-8 h-8";
    }

    return (
        <div className={`animate-spin rounded-full border-t-2 border-b-2 border-orange-500 ${spinnerSize}`}></div>
    );
};

export { Spinner };