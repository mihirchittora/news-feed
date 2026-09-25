"use client";

import { useAuth } from "@/components/auth/AuthProvider";
import { likeApi } from "@/lib/api/likes";
import { ApiClientError } from "@/lib/api/client";
import { Heart } from "lucide-react";
import Link from "next/link";
import { useState } from "react";

type LikeButtonProps = {
  storyId: string;
  initialLikeCount: number;
  initialLiked: boolean;
  loginHref: string;
};

export function LikeButton({ storyId, initialLikeCount, initialLiked, loginHref }: LikeButtonProps) {
  const { user, token } = useAuth();
  const [likeCount, setLikeCount] = useState(initialLikeCount);
  const [liked, setLiked] = useState(initialLiked);
  const [pending, setPending] = useState(false);
  const [loginPrompt, setLoginPrompt] = useState(false);
  const [error, setError] = useState("");

  async function toggleLike() {
    setError("");
    if (!user || !token) {
      setLoginPrompt(true);
      return;
    }
    if (pending) return;

    const previousCount = likeCount;
    const previousLiked = liked;
    setLiked(!previousLiked);
    setLikeCount(Math.max(0, previousCount + (previousLiked ? -1 : 1)));
    setPending(true);
    try {
      const response = previousLiked
        ? await likeApi.unlikeStory(token, storyId)
        : await likeApi.likeStory(token, storyId);
      setLikeCount(response.likeCount);
      setLiked(response.likedByCurrentUser);
    } catch (reason) {
      setLikeCount(previousCount);
      setLiked(previousLiked);
      setError(reason instanceof ApiClientError ? reason.message : "Could not update your like.");
    } finally {
      setPending(false);
    }
  }

  return <div className="relative">
    <button
      type="button"
      onClick={() => void toggleLike()}
      disabled={pending}
      aria-pressed={liked}
      aria-label={liked ? "Unlike this story" : "Like this story"}
      className={`inline-flex min-h-10 items-center gap-2 rounded-xl px-3 text-sm font-bold transition focus:outline-none focus:ring-4 focus:ring-coral/15 disabled:cursor-wait disabled:opacity-60 ${liked ? "bg-coral/10 text-coral" : "text-slate hover:bg-mist hover:text-ink"}`}
    >
      <Heart size={17} fill={liked ? "currentColor" : "none"} aria-hidden="true" />
      <span>{likeCount}</span>
    </button>
    {loginPrompt ? <div className="absolute left-0 top-full z-10 mt-2 w-64 rounded-2xl border border-line bg-white p-4 text-xs leading-5 text-slate shadow-soft">
      <p>Sign in to like this story.</p>
      <div className="mt-3 flex items-center gap-3">
        <Link href={loginHref} className="rounded-lg bg-ink px-3 py-2 font-bold text-white hover:bg-ink/90">Sign in</Link>
        <button type="button" onClick={() => setLoginPrompt(false)} className="font-semibold text-slate hover:text-ink">Not now</button>
      </div>
    </div> : null}
    {error ? <p className="absolute left-0 top-full z-10 mt-2 w-64 text-xs font-medium text-red-700" role="alert">{error}</p> : null}
  </div>;
}
