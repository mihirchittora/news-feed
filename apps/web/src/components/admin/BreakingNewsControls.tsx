"use client";

import { BreakingBadge } from "@/components/public/BreakingBadge";
import { Alert } from "@/components/ui/Alert";
import { Badge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { FormField } from "@/components/ui/FormField";
import { breakingNewsApi } from "@/lib/api/breaking-news";
import { ApiClientError } from "@/lib/api/client";
import type { AdminStory } from "@/lib/types";
import { useEffect, useState } from "react";
import { useAuth } from "@/components/auth/AuthProvider";

function toLocalDateTime(value?: string | null) {
  if (!value) return "";
  const date = new Date(value);
  const offset = date.getTimezoneOffset() * 60000;
  return new Date(date.getTime() - offset).toISOString().slice(0, 16);
}

function toIso(value: string) {
  return new Date(value).toISOString();
}

export function BreakingNewsControls({ initialStory, status, onNotice, onError }: {
  initialStory?: AdminStory;
  status?: AdminStory["status"];
  onNotice: (message: string) => void;
  onError: (message: string) => void;
}) {
  const { token, hasPermission } = useAuth();
  const [isBreaking, setIsBreaking] = useState(initialStory?.isBreaking ?? false);
  const [isActive, setIsActive] = useState(initialStory?.breakingActive ?? false);
  const [expiryChoice, setExpiryChoice] = useState(initialStory?.breakingActive ? (initialStory.breakingUntil ? "custom" : "manual") : "default");
  const [customUntil, setCustomUntil] = useState(toLocalDateTime(initialStory?.breakingUntil));
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    setIsBreaking(initialStory?.isBreaking ?? false);
    setIsActive(initialStory?.breakingActive ?? false);
    setExpiryChoice(initialStory?.breakingActive ? (initialStory.breakingUntil ? "custom" : "manual") : "default");
    setCustomUntil(toLocalDateTime(initialStory?.breakingUntil));
  }, [initialStory]);

  const canManage = hasPermission("BREAKING_NEWS_MANAGE");
  const isPublished = Boolean(initialStory && (status ?? initialStory.status) === "PUBLISHED");

  function payload() {
    if (expiryChoice === "manual") return { untilManuallyRemoved: true };
    if (expiryChoice === "custom") return { breakingUntil: toIso(customUntil) };
    if (expiryChoice === "1h") return { breakingUntil: new Date(Date.now() + 60 * 60 * 1000).toISOString() };
    if (expiryChoice === "2h") return { breakingUntil: new Date(Date.now() + 2 * 60 * 60 * 1000).toISOString() };
    if (expiryChoice === "4h") return { breakingUntil: new Date(Date.now() + 4 * 60 * 60 * 1000).toISOString() };
    return {};
  }

  async function save() {
    if (!token || !initialStory || !canManage || !isPublished) return;
    if (expiryChoice === "custom" && !customUntil) {
      onError("Choose an expiry date and time.");
      return;
    }
    setSaving(true);
    onError("");
    try {
      const updated = isBreaking && isActive
        ? await breakingNewsApi.updateExpiry(token, initialStory.id, payload())
        : await breakingNewsApi.enable(token, initialStory.id, payload());
      setIsBreaking(updated.isBreaking);
      setIsActive(updated.breakingActive);
      setCustomUntil(toLocalDateTime(updated.breakingUntil));
      setExpiryChoice(updated.breakingUntil ? "custom" : "manual");
      onNotice(isBreaking && isActive ? "Breaking News expiry updated." : "Story marked as Breaking News.");
    } catch (reason) {
      onError(reason instanceof ApiClientError ? reason.message : "Could not update Breaking News status.");
    } finally {
      setSaving(false);
    }
  }

  async function remove() {
    if (!token || !initialStory || !canManage) return;
    setSaving(true);
    onError("");
    try {
      const updated = await breakingNewsApi.disable(token, initialStory.id);
      setIsBreaking(updated.isBreaking);
      setIsActive(updated.breakingActive);
      setExpiryChoice("default");
      onNotice("Breaking News status removed.");
    } catch (reason) {
      onError(reason instanceof ApiClientError ? reason.message : "Could not remove Breaking News status.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <Card className="mt-6 p-5 sm:p-8">
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div><p className="text-sm font-bold text-ink">Breaking News</p><p className="mt-1 text-sm text-slate">Promote this published story in the public Breaking News area.</p></div>
        {isBreaking && isActive ? <BreakingBadge /> : isBreaking ? <Badge className="bg-amber-50 text-amber-700">Breaking expired</Badge> : null}
      </div>
      {!isPublished ? <p className="mt-5 rounded-xl bg-mist px-4 py-3 text-sm font-semibold text-slate">Available after publication.</p> : !canManage ? <p className="mt-5 text-sm text-slate">Breaking News status is managed by users with the Breaking News permission.</p> : (
        <div className="mt-6 grid gap-5">
          <FormField id="breaking-expiry" label="Breaking until">
            <select id="breaking-expiry" value={expiryChoice} onChange={(event) => setExpiryChoice(event.target.value)} className="min-h-12 w-full rounded-xl border border-line bg-white px-4 text-[15px] text-ink outline-none focus:border-coral focus:ring-4 focus:ring-coral/10">
              <option value="default">{isBreaking && isActive ? "Keep current expiry" : "Use configured default duration"}</option>
              <option value="1h">1 hour</option>
              <option value="2h">2 hours</option>
              <option value="4h">4 hours</option>
              <option value="manual">Until manually removed</option>
              <option value="custom">Custom date and time</option>
            </select>
          </FormField>
          {expiryChoice === "custom" ? <FormField id="breaking-custom-until" label="Custom expiry"><input id="breaking-custom-until" type="datetime-local" value={customUntil} onChange={(event) => setCustomUntil(event.target.value)} className="min-h-12 w-full rounded-xl border border-line bg-white px-4 text-[15px] text-ink outline-none focus:border-coral focus:ring-4 focus:ring-coral/10" /></FormField> : null}
          <div className="flex flex-wrap gap-3">
            <Button type="button" loading={saving} onClick={() => void save()}>{isBreaking && isActive ? "Update expiry" : "Mark as Breaking"}</Button>
            {isBreaking ? <Button type="button" disabled={saving} onClick={() => void remove()} className="bg-white !text-red-700 ring-1 ring-red-200 hover:bg-red-50">Remove Breaking Status</Button> : null}
          </div>
        </div>
      )}
    </Card>
  );
}
