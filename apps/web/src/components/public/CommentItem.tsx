"use client";

import { Button } from "@/components/ui/Button";
import type { Comment } from "@/lib/types";
import { Check, Heart, Pencil, Reply, Trash2, X } from "lucide-react";

function createdLabel(value: string) {
  return new Intl.DateTimeFormat("en", { dateStyle: "medium", timeStyle: "short" }).format(new Date(value));
}

type CommentItemProps = {
  comment: Comment;
  onDelete?: (comment: Comment) => void;
  deleting?: boolean;
  onEdit?: (comment: Comment) => void;
  editing?: boolean;
  editBody?: string;
  onEditBodyChange?: (value: string) => void;
  onSaveEdit?: (comment: Comment) => void;
  onCancelEdit?: () => void;
  savingEdit?: boolean;
  onReply?: (comment: Comment) => void;
  replying?: boolean;
  replyBody?: string;
  onReplyBodyChange?: (value: string) => void;
  onSaveReply?: (comment: Comment) => void;
  onCancelReply?: () => void;
  postingReply?: boolean;
  onToggleLike?: (comment: Comment) => void;
  liking?: boolean;
  showReplyAction?: boolean;
};

export function CommentItem({
  comment,
  onDelete,
  deleting = false,
  onEdit,
  editing = false,
  editBody = "",
  onEditBodyChange,
  onSaveEdit,
  onCancelEdit,
  savingEdit = false,
  onReply,
  replying = false,
  replyBody = "",
  onReplyBodyChange,
  onSaveReply,
  onCancelReply,
  postingReply = false,
  onToggleLike,
  liking = false,
  showReplyAction = true,
}: CommentItemProps) {
  return <article className="border-b border-line py-6 last:border-0">
    <div className="flex items-start justify-between gap-4">
      <div>
        <p className="font-bold text-ink">{comment.author.name}</p>
        <time dateTime={comment.createdAt} className="mt-1 block text-xs text-slate">{createdLabel(comment.createdAt)}{comment.edited ? " · edited" : ""}</time>
      </div>
      {comment.ownedByCurrentUser && (onEdit || onDelete) ? <div className="flex items-center gap-1">
        {onEdit ? <button type="button" onClick={() => onEdit(comment)} disabled={editing || savingEdit} className="inline-flex min-h-8 items-center gap-1 rounded-lg px-2 py-1 text-xs font-semibold text-slate hover:bg-mist hover:text-ink disabled:opacity-50"><Pencil size={13} aria-hidden="true" /> Edit</button> : null}
        {onDelete ? <button type="button" onClick={() => onDelete(comment)} disabled={deleting || editing} className="inline-flex min-h-8 items-center gap-1 rounded-lg px-2 py-1 text-xs font-semibold text-slate hover:bg-red-50 hover:text-red-700 disabled:opacity-50">{deleting ? null : <Trash2 size={13} aria-hidden="true" />} {deleting ? "Deleting…" : "Delete"}</button> : null}
      </div> : null}
    </div>

    {editing ? <div className="mt-4 rounded-xl border border-line bg-mist/50 p-3">
      <label htmlFor={`edit-comment-${comment.id}`} className="sr-only">Edit comment</label>
      <textarea id={`edit-comment-${comment.id}`} value={editBody} onChange={(event) => onEditBodyChange?.(event.target.value)} rows={3} maxLength={2000} className="w-full resize-y rounded-lg border border-line bg-white p-3 text-sm leading-7 text-ink outline-none focus:border-coral" disabled={savingEdit} />
      <div className="mt-2 flex justify-end gap-2">
        <button type="button" onClick={onCancelEdit} disabled={savingEdit} className="inline-flex min-h-9 items-center gap-1 rounded-lg px-3 text-xs font-semibold text-slate hover:bg-white"><X size={14} aria-hidden="true" /> Cancel</button>
        <Button type="button" onClick={() => onSaveEdit?.(comment)} loading={savingEdit} className="min-h-9 rounded-lg px-3 text-xs"><Check size={14} aria-hidden="true" /> Save</Button>
      </div>
    </div> : <p className="mt-4 whitespace-pre-wrap text-sm leading-7 text-slate">{comment.body}</p>}

    <div className="mt-4 flex flex-wrap items-center gap-1 text-xs font-semibold">
      {onToggleLike ? <button type="button" onClick={() => onToggleLike(comment)} disabled={liking} aria-label={comment.likedByCurrentUser ? "Unlike comment" : "Like comment"} className={`inline-flex min-h-8 items-center gap-1.5 rounded-lg px-2.5 transition disabled:opacity-50 ${comment.likedByCurrentUser ? "bg-coral/10 text-coral" : "text-slate hover:bg-mist hover:text-ink"}`}><Heart size={14} fill={comment.likedByCurrentUser ? "currentColor" : "none"} aria-hidden="true" /> {comment.likeCount}</button> : <span className="inline-flex min-h-8 items-center gap-1.5 rounded-lg px-2.5 text-slate"><Heart size={14} aria-hidden="true" /> {comment.likeCount}</span>}
      {showReplyAction && onReply ? <button type="button" onClick={() => onReply(comment)} disabled={replying || postingReply} className="inline-flex min-h-8 items-center gap-1.5 rounded-lg px-2.5 text-slate hover:bg-mist hover:text-ink disabled:opacity-50"><Reply size={14} aria-hidden="true" /> Reply</button> : null}
    </div>

    {replying ? <div className="mt-3 rounded-xl border border-line bg-mist/50 p-3">
      <label htmlFor={`reply-comment-${comment.id}`} className="sr-only">Reply to {comment.author.name}</label>
      <textarea id={`reply-comment-${comment.id}`} value={replyBody} onChange={(event) => onReplyBodyChange?.(event.target.value)} rows={3} maxLength={2000} placeholder={`Reply to ${comment.author.name}…`} className="w-full resize-y rounded-lg border border-line bg-white p-3 text-sm leading-7 text-ink outline-none focus:border-coral" disabled={postingReply} />
      <div className="mt-2 flex justify-end gap-2">
        <button type="button" onClick={onCancelReply} disabled={postingReply} className="inline-flex min-h-9 items-center gap-1 rounded-lg px-3 text-xs font-semibold text-slate hover:bg-white"><X size={14} aria-hidden="true" /> Cancel</button>
        <Button type="button" onClick={() => onSaveReply?.(comment)} loading={postingReply} className="min-h-9 rounded-lg px-3 text-xs"><Reply size={14} aria-hidden="true" /> Reply</Button>
      </div>
    </div> : null}
  </article>;
}
