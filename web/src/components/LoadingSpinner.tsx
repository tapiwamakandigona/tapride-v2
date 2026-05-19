/**
 * LoadingSpinner — full-screen or inline animated spinner.
 */
import React from 'react';

interface Props {
  fullScreen?: boolean;
  size?: 'sm' | 'md' | 'lg';
  label?: string;
}

/** Displays a spinning indicator. Set fullScreen to center it in the viewport. */
export const LoadingSpinner: React.FC<Props> = ({ fullScreen, size = 'md', label }) => {
  const sizeClasses = { sm: 'h-5 w-5', md: 'h-10 w-10', lg: 'h-16 w-16' };

  const spinner = (
    <div className="flex flex-col items-center gap-3">
      <div
        className={`${sizeClasses[size]} animate-spin rounded-full border-4 border-brand-200 border-t-brand-500`}
      />
      {label && <p className="text-sm text-gray-500">{label}</p>}
    </div>
  );

  if (fullScreen) {
    return (
      <div className="fixed inset-0 flex items-center justify-center bg-white/80 z-50">
        {spinner}
      </div>
    );
  }
  return spinner;
};
