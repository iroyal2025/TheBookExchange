import React from 'react';

const Textarea = React.forwardRef(({ className, ...props }, ref) => {
    return (
        <textarea
            className={`border rounded-md p-2 w-full resize-vertical focus:outline-none focus:ring focus:border-blue-300 ${className}`}
            ref={ref}
            {...props}
        />
    );
});

Textarea.displayName = 'Textarea';

export { Textarea };