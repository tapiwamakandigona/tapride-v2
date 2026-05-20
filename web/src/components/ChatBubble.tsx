/**
 * ChatBubble — renders a single chat message aligned left (other)
 * or right (self).
 */
import React from 'react';
import type { Message } from '@/types';

interface Props {
  message: Message;
  isSelf: boolean;
}

/** Renders a chat bubble with sender name, message content, and timestamp. */
export const ChatBubble: React.FC<Props> = ({ message, isSelf }) => {
  const rawTime = message.$createdAt ?? message.createdAt;
  const time = rawTime ? new Date(rawTime as string).toLocaleTimeString([], {
    hour: '2-digit',
    minute: '2-digit',
  }) : '';

  return (
    <div className={`flex flex-col ${isSelf ? 'items-end' : 'items-start'} mb-3`}>
      {!isSelf && (
        <span className="mb-1 text-xs font-medium text-gray-500">{message.senderName}</span>
      )}
      <div
        className={`max-w-xs rounded-2xl px-4 py-2 text-sm shadow-sm ${
          isSelf
            ? 'rounded-br-none bg-brand-500 text-white'
            : 'rounded-bl-none bg-white text-gray-800'
        }`}
      >
        {message.content}
      </div>
      <span className="mt-1 text-xs text-gray-400">{time}</span>
    </div>
  );
};
