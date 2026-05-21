/**
 * ChatPage — real-time in-ride messaging between rider and driver.
 */
import React, { useState, useRef, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ChatBubble } from '@/components/ChatBubble';
import { LoadingSpinner } from '@/components/LoadingSpinner';
import { useChat } from '@/hooks/useChat';
import { useAuthStore } from '@/store/authStore';

/** Full-screen chat interface for an active ride. */
const ChatPage: React.FC = () => {
  const { rideId } = useParams<{ rideId: string }>();
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const { messages, loading, error, sendMessage } = useChat(rideId ?? '');
  const [text, setText] = useState('');
  const [sending, setSending] = useState(false);
  const [sendError, setSendError] = useState<string | null>(null);
  const bottomRef = useRef<HTMLDivElement>(null);

  // Auto-scroll to bottom on new messages
  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const handleSend = async () => {
    if (!text.trim()) return;
    setSending(true);
    setSendError(null);
    const ok = await sendMessage(text);
    setSending(false);
    if (ok) {
      setText('');
    } else {
      setSendError('Failed to send message. Please try again.');
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  return (
    <div className="flex flex-col h-screen bg-gray-100">
      {/* Header */}
      <div className="bg-white px-4 py-3 flex items-center gap-3 shadow-sm">
        <button onClick={() => navigate(`/ride/${rideId}`)} aria-label="Back to ride" className="text-gray-500 text-xl">
          ←
        </button>
        <div>
          <p className="font-semibold text-gray-900 text-sm">Ride Chat</p>
          <p className="text-xs text-gray-400">Ride #{rideId?.slice(-6)}</p>
        </div>
      </div>

      {/* Messages */}
      <div className="flex-1 overflow-y-auto px-4 py-4">
        {loading && (
          <div className="flex justify-center py-8">
            <LoadingSpinner label="Loading messages…" />
          </div>
        )}

        {error && (
          <div className="rounded-lg bg-red-50 border border-red-200 p-3 text-sm text-red-700 mb-3">
            {error}
          </div>
        )}

        {!loading && !error && messages.length === 0 && (
          <div className="flex justify-center py-12 text-sm text-gray-400">
            No messages yet. Say hello! 👋
          </div>
        )}

        {sendError && (
          <div className="rounded-lg bg-red-50 border border-red-200 p-3 text-sm text-red-700 mb-3">
            {sendError}
          </div>
        )}

        {messages.map((msg) => (
          <ChatBubble
            key={msg.$id}
            message={msg}
            isSelf={msg.senderId === user?.$id}
          />
        ))}
        <div ref={bottomRef} />
      </div>

      {/* Input */}
      <div className="bg-white border-t border-gray-200 px-4 py-3 flex gap-2 items-end">
        <textarea
          value={text}
          onChange={(e) => setText(e.target.value)}
          onKeyDown={handleKeyDown}
          rows={1}
          placeholder="Type a message…"
          className="flex-1 resize-none rounded-xl border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-400 max-h-28"
        />
        <button
          onClick={handleSend}
          disabled={sending || !text.trim()}
          className="flex h-10 w-10 items-center justify-center rounded-xl bg-brand-500 text-white hover:bg-brand-600 disabled:opacity-50 transition-colors"
        >
          {sending ? <LoadingSpinner size="sm" /> : '➤'}
        </button>
      </div>
    </div>
  );
};

export default ChatPage;
