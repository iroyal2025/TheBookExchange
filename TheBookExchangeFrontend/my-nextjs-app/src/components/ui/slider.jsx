"use client";

import * as React from "react";
import * as SliderPrimitives from "@radix-ui/react-slider";

import { cn } from "@/lib/utils";

const Slider = React.forwardRef(
    ({ className, ...props }, ref) => (
        <SliderPrimitives.Root
            ref={ref}
            className={cn(
                "relative flex w-full touch-none select-none items-center",
                className
            )}
            {...props}
        >
            <SliderPrimitives.Track className="relative h-2 w-full grow overflow-hidden rounded-full bg-gray-300">
                <SliderPrimitives.Range className="absolute h-full bg-blue-500" />
            </SliderPrimitives.Track>
            <SliderPrimitives.Thumb className="block h-5 w-5 rounded-full bg-blue-500 ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50" />
        </SliderPrimitives.Root>
    )
);
Slider.displayName = SliderPrimitives.Root.displayName;

export { Slider };