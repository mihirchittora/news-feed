"use client";

import { useAuth } from "@/components/auth/AuthProvider";
import { CommentItem } from "@/components/public/CommentItem";
import { commentsApi } from "@/lib/api/comments";
import { ApiClientError } from "@/lib/api/client";
import type { Comment } from "@/lib/types";
import Link from "next/link";
import { useEffect, useMemo, useState } from "react";
import { Alert } from "@/components/ui/Alert";
import { Button } from "@/components/ui/Button";
import { EmptyState } from "@/components/ui/EmptyState";
import { LoadingState } from "@/components/ui/LoadingState";

const MAX_COMMENT_LENGTH = 2000;

export function CommentsSection({ storyId, loginHref, initialCommentCount, onCommentCountChange }: { storyId: string; loginHref: string; initialCommentCount: number; onCommentCountChange?: (count: number) => void }) {
  const { user, token } = useAuth();
  const [comments, setComments] = useState<Comment[]>([]);
  const [cursor, setCursor] = useState<string | null>(null);
  const [hasMore, setHasMore] = useState(false);
  const [count, setCount] = useState(initialCommentCount);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [error, setError] = useState("");
  const [body, setBody] = useState("");
  const [composerError, setComposerError] = useState("");
  const [posting, setPosting] = useState(false);
  const [deletingId, setDeletingId] = useState<string | null>(null);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [editBody, setEditBody] = useState("");
  const [savingEditId, setSavingEditId] = useState<string | null>(null);
  const [replyingId, setReplyingId] = useState<string | null>(null);
  const [replyBody, setReplyBody] = useState("");
  const [postingReplyId, setPostingReplyId] = useState<string | null>(null);
  const [likingId, setLikingId] = useState<string | null>(null);

  useEffect(() => {
    let active = true;
    setLoading(true);
    setError("");
    commentsApi.list(storyId, "limit=20", token ?? undefined)
      .then((page) => {
        if (!active) return;
        setComments(page.items);
        setCursor(page.nextCursor ?? null);
        setHasMore(page.hasMore);
      })
      .catch((reason) => active && setError(reason instanceof ApiClientError ? reason.message : "We could not load comments."))
      .finally(() => active && setLoading(false));
    return () => { active = false; };
  }, [storyId, token]);

  useEffect(() => {
    setCount(initialCommentCount);
  }, [initialCommentCount]);

  const repliesByParent = useMemo(() => {
    const groups = new Map<string, Comment[]>();
    for (const comment of comments) {
      if (!comment.parentCommentId) continue;
      groups.set(comment.parentCommentId, [...(groups.get(comment.parentCommentId) ?? []), comment]);
    }
    return groups;
  }, [comments]);

  const roots = useMemo(() => {
    const ids = new Set(comments.map((comment) => comment.id));
    return comments.filter((comment) => !comment.parentCommentId || !ids.has(comment.parentCommentId));
  }, [comments]);

  async function loadMore() {
    if (!cursor || loadingMore) return;
    setLoadingMore(true);
    try {
      const page = await commentsApi.list(storyId, `limit=20&cursor=${encodeURIComponent(cursor)}`, token ?? undefined);
      setComments((current) => [...current, ...page.items]);
      setCursor(page.nextCursor ?? null);
      setHasMore(page.hasMore);
    } catch (reason) {
      setError(reason instanceof ApiClientError ? reason.message : "We could not load more comments.");
    } finally {
      setLoadingMore(false);
    }
  }

  function updateCount(nextCount: number) {
    setCount(nextCount);
    onCommentCountChange?.(nextCount);
  }

  async function postComment(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const normalizedBody = body.trim();
    setComposerError("");
    if (!normalizedBody) {
      setComposerError("Write something before posting.");
      return;
    }
    if (normalizedBody.length > MAX_COMMENT_LENGTH) {
      setComposerError(`Comments must be ${MAX_COMMENT_LENGTH} characters or fewer.`);
      return;
    }
    if (!token) return;
    setPosting(true);
    try {
      const comment = await commentsApi.create(token, storyId, { body: normalizedBody });
      setComments((current) => [...current, comment]);
      updateCount(count + 1);
      setBody("");
    } catch (reason) {
      setComposerError(reason instanceof ApiClientError ? reason.message : "We could not post your comment.");
    } finally {
      setPosting(false);
    }
  }

  async function deleteComment(comment: Comment) {
    if (!token || !window.confirm("Delete your comment? This cannot be undone.")) return;
    setDeletingId(comment.id);
    try {
      await commentsApi.deleteOwn(token, comment.id);
      setComments((current) => current.filter((item) => item.id !== comment.id));
      updateCount(Math.max(0, count - 1));
    } catch (reason) {
      setError(reason instanceof ApiClientError ? reason.message : "We could not delete your comment.");
    } finally {
      setDeletingId(null);
    }
  }

  function beginEdit(comment: Comment) {
    setReplyingId(null);
    setEditingId(comment.id);
    setEditBody(comment.body);
  }

  async function saveEdit(comment: Comment) {
    const normalizedBody = editBody.trim();
    if (!token) return;
    if (!normalizedBody) {
      setError("A comment cannot be empty.");
      return;
    }
    setSavingEditId(comment.id);
    try {
      const updated = await commentsApi.update(token, comment.id, { body: normalizedBody });
      setComments((current) => current.map((item) => item.id === updated.id ? updated : item));
      setEditingId(null);
      setEditBody("");
    } catch (reason) {
      setError(reason instanceof ApiClientError ? reason.message : "We could not update your comment.");
    } finally {
      setSavingEditId(null);
    }
  }

  function beginReply(comment: Comment) {
    setEditingId(null);
    setReplyingId(comment.id);
    setReplyBody("");
  }

  async function saveReply(parent: Comment) {
    const normalizedBody = replyBody.trim();
    if (!token) {
      window.location.assign(loginHref);
      return;
    }
    if (!normalizedBody) {
      setError("Write something before replying.");
      return;
    }
    setPostingReplyId(parent.id);
    try {
      const reply = await commentsApi.create(token, storyId, { body: normalizedBody, parentCommentId: parent.id });
      setComments((current) => [...current, reply]);
      updateCount(count + 1);
      setReplyingId(null);
      setReplyBody("");
    } catch (reason) {
      setError(reason instanceof ApiClientError ? reason.message : "We could not post your reply.");
    } finally {
      setPostingReplyId(null);
    }
  }

  async function toggleLike(comment: Comment) {
    if (!token) {
      window.location.assign(loginHref);
      return;
    }
    setLikingId(comment.id);
    try {
      const engagement = comment.likedByCurrentUser
        ? await commentsApi.unlike(token, comment.id)
        : await commentsApi.like(token, comment.id);
      setComments((current) => current.map((item) => item.id === comment.id ? { ...item, ...engagement } : item));
    } catch (reason) {
      setError(reason instanceof ApiClientError ? reason.message : "We could not update that like.");
    } finally {
      setLikingId(null);
    }
  }

  function renderComment(comment: Comment, nested = false) {
    const childReplies = repliesByParent.get(comment.id) ?? [];
    return <div key={comment.id} className={nested ? "border-l-2 border-line pl-4 sm:ml-6 sm:pl-5" : ""}>
      <CommentItem
        comment={comment}
        onDelete={user && comment.ownedByCurrentUser ? deleteComment : undefined}
        deleting={deletingId === comment.id}
        onEdit={user && comment.ownedByCurrentUser ? beginEdit : undefined}
        editing={editingId === comment.id}
        editBody={editBody}
        onEditBodyChange={setEditBody}
        onSaveEdit={saveEdit}
        onCancelEdit={() => setEditingId(null)}
        savingEdit={savingEditId === comment.id}
        onReply={beginReply}
        replying={replyingId === comment.id}
        replyBody={replyBody}
        onReplyBodyChange={setReplyBody}
        onSaveReply={saveReply}
        onCancelReply={() => setReplyingId(null)}
        postingReply={postingReplyId === comment.id}
        onToggleLike={toggleLike}
        liking={likingId === comment.id}
      />
      {childReplies.length > 0 ? <div>{childReplies.map((reply) => renderComment(reply, true))}</div> : null}
    </div>;
  }

  return <section id="comments" className="mt-16 max-w-3xl scroll-mt-8 border-t border-line pt-10">
    <div className="flex items-baseline justify-between gap-4">
      <h2 className="font-display text-3xl font-bold tracking-[-0.04em] text-ink">Comments <span className="text-slate">({count})</span></h2>
    </div>
    {user ? <form onSubmit={(event) => void postComment(event)} className="mt-7 rounded-2xl border border-line bg-white p-4 sm:p-5">
      <label htmlFor="comment-body" className="sr-only">Write a comment</label>
      <textarea id="comment-body" value={body} onChange={(event) => setBody(event.target.value)} maxLength={MAX_COMMENT_LENGTH} rows={4} placeholder="Write a comment…" className="w-full resize-y border-0 bg-transparent text-sm leading-7 text-ink outline-none placeholder:text-slate/60" disabled={posting} />
      <div className="mt-3 flex flex-col gap-3 border-t border-line pt-3 sm:flex-row sm:items-center sm:justify-between"><span className="text-xs text-slate">{body.length}/{MAX_COMMENT_LENGTH}</span><Button type="submit" loading={posting}>{posting ? "Posting…" : "Post"}</Button></div>
      {composerError ? <p className="mt-3 text-sm text-red-700" role="alert">{composerError}</p> : null}
    </form> : <div className="mt-7 rounded-2xl border border-line bg-white p-5"><p className="text-sm text-slate">Sign in to join the conversation.</p><Link href={loginHref} className="mt-4 inline-flex min-h-10 items-center rounded-xl bg-ink px-4 text-sm font-semibold text-white hover:bg-ink/90">Sign in</Link></div>}
    {error ? <div className="mt-6"><Alert>{error}</Alert></div> : null}
    {loading ? <LoadingState label="Loading comments" /> : comments.length === 0 ? <div className="mt-7"><EmptyState title="No comments yet." description={user ? "Be the first to comment." : "Sign in to join the conversation."} /></div> : <div className="mt-5 rounded-2xl border border-line bg-white px-5 sm:px-7">{roots.map((comment) => renderComment(comment))}</div>}
    {hasMore ? <div className="mt-6 flex justify-center"><Button type="button" onClick={() => void loadMore()} loading={loadingMore}>Load more comments</Button></div> : null}
  </section>;
}
