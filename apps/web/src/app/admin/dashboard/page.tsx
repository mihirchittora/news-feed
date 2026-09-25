"use client";

import { AdminRoute } from "@/components/auth/ProtectedRoute";
import { AdminShell } from "@/components/layout/AdminShell";
import { useAuth } from "@/components/auth/AuthProvider";
import { CategoryChart, PublishingTrendChart } from "@/components/admin/dashboard/DashboardCharts";
import { DashboardMetricCard } from "@/components/admin/dashboard/DashboardMetricCard";
import { DashboardSection } from "@/components/admin/dashboard/DashboardSection";
import { ApiClientError } from "@/lib/api/client";
import { adminApi } from "@/lib/api/admin";
import type { DashboardPeriodType, DashboardResponse } from "@/lib/types";
import { Card } from "@/components/ui/Card";
import { BookOpen, CheckCircle2, FileText, MessageSquare, Megaphone, Radio, RefreshCw, ShieldAlert, Users } from "lucide-react";
import Link from "next/link";
import { useCallback, useEffect, useMemo, useState } from "react";

const numberFormat = new Intl.NumberFormat("en-IN");
const periodOptions: Array<{ value: DashboardPeriodType; label: string }> = [
  { value: "TODAY", label: "Today" },
  { value: "LAST_7_DAYS", label: "7 Days" },
  { value: "LAST_30_DAYS", label: "30 Days" },
];

function formatNumber(value: number) { return numberFormat.format(value); }

function formatTime(value: string | null | undefined, timezone: string) {
  if (!value) return "—";
  return new Intl.DateTimeFormat(undefined, { hour: "numeric", minute: "2-digit", timeZone: timezone }).format(new Date(value));
}

function timeUntil(value: string) {
  const minutes = Math.max(0, Math.round((new Date(value).getTime() - Date.now()) / 60000));
  if (minutes < 60) return `${minutes}m`;
  const hours = Math.floor(minutes / 60);
  const remaining = minutes % 60;
  return remaining ? `${hours}h ${remaining}m` : `${hours}h`;
}

function activityVerb(action: string) {
  const labels: Record<string, string> = {
    STORY_PUBLISHED: "published story",
    STORY_UNPUBLISHED: "unpublished story",
    BREAKING_NEWS_ENABLED: "marked story as Breaking",
    BREAKING_NEWS_UPDATED: "updated Breaking News",
    BREAKING_NEWS_DISABLED: "removed Breaking News",
    COMMENT_HIDDEN: "hid comment",
    COMMENT_RESTORED: "restored comment",
    COMMENT_DELETED_BY_MODERATOR: "deleted comment",
    NEWSPAPER_PUBLISHED: "published newspaper",
    AD_PUBLISHED: "published advertisement",
    AD_PAUSED: "paused advertisement",
    AD_RESUMED: "resumed advertisement",
    STAFF_CREATED: "created staff user",
    STAFF_DISABLED: "disabled staff user",
    STAFF_ENABLED: "enabled staff user",
    ROLE_PERMISSIONS_CHANGED: "changed role permissions",
  };
  return labels[action] ?? action.toLowerCase().replaceAll("_", " ");
}

function DashboardSkeleton() {
  return (
    <div className="animate-pulse space-y-10" aria-label="Loading dashboard" role="status">
      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">{Array.from({ length: 8 }).map((_, index) => <div key={index} className="h-36 rounded-2xl bg-white/80" />)}</div>
      <div className="grid gap-5 xl:grid-cols-2"><div className="h-64 rounded-3xl bg-white/80" /><div className="h-64 rounded-3xl bg-white/80" /></div>
      <div className="h-72 rounded-3xl bg-white/80" />
    </div>
  );
}

function EmptyPanel({ children }: { children: React.ReactNode }) {
  return <div className="rounded-2xl border border-dashed border-line bg-white/60 px-5 py-8 text-center text-sm text-slate">{children}</div>;
}

function DashboardContent() {
  const { user, token, hasPermission } = useAuth();
  const [period, setPeriod] = useState<DashboardPeriodType>("TODAY");
  const [dashboard, setDashboard] = useState<DashboardResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [retryKey, setRetryKey] = useState(0);

  const loadDashboard = useCallback(async () => {
    if (!token) return;
    setLoading(true);
    setError(null);
    try {
      setDashboard(await adminApi.dashboard(token, period));
    } catch (requestError) {
      setDashboard(null);
      setError(requestError instanceof ApiClientError ? requestError.message : "Unable to load dashboard.");
    } finally {
      setLoading(false);
    }
  }, [period, retryKey, token]);

  useEffect(() => { void loadDashboard(); }, [loadDashboard]);

  const quickActions = useMemo(() => [
    hasPermission("STORY_CREATE") ? { label: "Create Story", href: "/admin/stories/new", icon: FileText } : null,
    hasPermission("NEWSPAPER_UPLOAD") ? { label: "Upload Newspaper", href: "/admin/newspapers/new", icon: BookOpen } : null,
    hasPermission("AD_CREATE") ? { label: "Create Advertisement", href: "/admin/ads/new", icon: Megaphone } : null,
  ].filter(Boolean) as Array<{ label: string; href: string; icon: typeof FileText }>, [hasPermission]);

  if (!user) return null;
  return (
    <AdminShell>
      <div className="border-b border-line pb-8">
        <div className="flex flex-wrap items-end justify-between gap-6">
          <div>
            <p className="text-xs font-bold uppercase tracking-[0.22em] text-coral">Admin workspace</p>
            <h1 className="mt-3 font-display text-5xl font-bold tracking-[-0.05em] text-ink">Dashboard</h1>
            <p className="mt-4 max-w-2xl text-base leading-7 text-slate">Welcome, {user.name.split(" ")[0]}. A live operational view of publishing, engagement, and the work that needs attention.</p>
          </div>
          <div className="rounded-2xl border border-line bg-white p-1" aria-label="Dashboard period">
            <div className="flex gap-1" role="tablist">
              {periodOptions.map((option) => (
                <button key={option.value} type="button" role="tab" aria-selected={period === option.value} onClick={() => setPeriod(option.value)} className={`rounded-xl px-3 py-2 text-xs font-bold transition sm:px-4 ${period === option.value ? "bg-ink text-white" : "text-slate hover:bg-mist hover:text-ink"}`}>
                  {option.label}
                </button>
              ))}
            </div>
          </div>
        </div>
        {dashboard ? <p className="mt-5 text-xs text-slate">Reporting {period === "TODAY" ? "today" : period === "LAST_7_DAYS" ? "the last 7 calendar days" : "the last 30 calendar days"} · {dashboard.period.timezone}</p> : null}
      </div>

      <div className="mt-8">
        {loading ? <DashboardSkeleton /> : error ? (
          <Card className="flex flex-col items-start gap-4 border-coral/30 bg-[#fff8f6] p-6 sm:flex-row sm:items-center">
            <ShieldAlert className="shrink-0 text-coral" size={22} aria-hidden="true" />
            <div className="flex-1"><p className="font-bold text-ink">Unable to load dashboard.</p><p className="mt-1 text-sm text-slate">{error}</p></div>
            <button type="button" onClick={() => setRetryKey((value) => value + 1)} className="inline-flex min-h-10 items-center gap-2 rounded-xl bg-ink px-4 text-sm font-bold text-white hover:bg-coral-dark"><RefreshCw size={15} aria-hidden="true" /> Retry</button>
          </Card>
        ) : dashboard ? <DashboardData dashboard={dashboard} quickActions={quickActions} /> : null}
      </div>
    </AdminShell>
  );
}

function DashboardData({ dashboard, quickActions }: { dashboard: DashboardResponse; quickActions: Array<{ label: string; href: string; icon: typeof FileText }> }) {
  const { content, engagement, moderation, users, staff, newspaper, advertising } = dashboard;
  const metrics = [
    content ? { label: "Published stories", value: formatNumber(content.publishedStories), detail: "Published in this period", icon: FileText, tone: "bg-[#eaf1f4]", href: "/admin/stories?status=PUBLISHED" } : null,
    content ? { label: "Draft stories", value: formatNumber(content.draftStories), detail: "Current editorial state", icon: FileText, tone: "bg-[#fff0e7]", href: "/admin/stories?status=DRAFT" } : null,
    engagement ? { label: "Comments", value: formatNumber(engagement.comments), detail: "Created in this period", icon: MessageSquare, tone: "bg-[#eef3eb]", href: "/admin/comments" } : null,
    users ? { label: "New users", value: formatNumber(users.newUsers), detail: `${formatNumber(users.totalRegisteredUsers)} registered users total`, icon: Users, tone: "bg-[#f0edf5]", href: "/admin/staff" } : null,
    content && content.activeBreakingNews !== null ? { label: "Active Breaking", value: formatNumber(content.activeBreakingNews), detail: "Currently active stories", icon: Radio, tone: "bg-[#fff0e7]", href: "/admin/breaking-news" } : null,
    advertising ? { label: "Active ads", value: formatNumber(advertising.active), detail: `${formatNumber(advertising.scheduled)} scheduled`, icon: Megaphone, tone: "bg-[#f0edf5]", href: "/admin/ads?status=ACTIVE" } : null,
    newspaper ? { label: "Today's newspaper", value: newspaper.todayPublished ? "Published" : "Missing", detail: `${formatNumber(newspaper.publishedEditionCount)} published edition${newspaper.publishedEditionCount === 1 ? "" : "s"}`, icon: BookOpen, tone: newspaper.todayPublished ? "bg-[#eef3eb]" : "bg-[#fff0e7]", href: "/admin/newspapers" } : null,
    moderation ? { label: "Hidden comments", value: formatNumber(moderation.hiddenComments), detail: `${formatNumber(moderation.moderatedComments)} moderated in period`, icon: ShieldAlert, tone: "bg-[#fff0e7]", href: "/admin/comments?status=HIDDEN" } : null,
  ].filter(Boolean) as Array<{ label: string; value: string; detail: string; icon: typeof FileText; tone: string; href: string }>;

  return (
    <div className="space-y-12">
      <section aria-labelledby="dashboard-period-summary">
        <div className="mb-4 flex items-end justify-between gap-4"><div><h2 id="dashboard-period-summary" className="font-display text-3xl font-bold tracking-[-0.04em] text-ink">Period summary</h2><p className="mt-1 text-sm text-slate">Period activity and current operational state.</p></div></div>
        {metrics.length ? <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">{metrics.map((metric) => <DashboardMetricCard key={metric.label} {...metric} />)}</div> : <EmptyPanel>No dashboard sections are available for your current permissions.</EmptyPanel>}
      </section>

      <div className="grid gap-8 xl:grid-cols-[1.15fr_0.85fr]">
        <DashboardSection title="Needs attention" description="Small, actionable items from the current operational state.">
          {dashboard.attention.items.length ? <div className="grid gap-3">{dashboard.attention.items.map((item) => <Link key={item.key} href={item.href} className="flex items-start gap-3 rounded-2xl border border-line bg-white px-4 py-4 transition hover:border-coral/50 hover:shadow-sm"><div className={`mt-0.5 grid h-8 w-8 shrink-0 place-items-center rounded-lg ${item.severity === "WARNING" ? "bg-[#fff0e7] text-coral-dark" : "bg-[#eaf1f4] text-ink"}`}><ShieldAlert size={16} aria-hidden="true" /></div><div className="min-w-0"><div className="flex flex-wrap items-center gap-2"><p className="text-sm font-bold text-ink">{item.label}</p><span className="text-[10px] font-bold uppercase tracking-[0.14em] text-slate">{item.severity}</span></div><p className="mt-1 text-sm leading-5 text-slate">{item.detail}</p></div><span className="ml-auto pt-1 text-coral" aria-hidden="true">→</span></Link>)}</div> : <EmptyPanel><CheckCircle2 className="mx-auto mb-2 text-[#5b8c67]" size={22} aria-hidden="true" />Nothing needs attention right now.</EmptyPanel>}
        </DashboardSection>
        <DashboardSection title="Quick actions" description="Create or manage work you are allowed to perform.">
          {quickActions.length ? <div className="grid gap-3 sm:grid-cols-3 xl:grid-cols-1">{quickActions.map(({ label, href, icon: Icon }) => <Link key={label} href={href} className="flex min-h-14 items-center gap-3 rounded-2xl bg-ink px-4 text-sm font-bold text-white transition hover:bg-coral-dark"><span className="grid h-8 w-8 place-items-center rounded-lg bg-white/10"><Icon size={16} aria-hidden="true" /></span>{label}<span className="ml-auto text-coral">↗</span></Link>)}</div> : <EmptyPanel>No quick actions are available for your permissions.</EmptyPanel>}
        </DashboardSection>
      </div>

      {content ? <DashboardSection title="Content" description="Publishing output for the selected period, with current workflow state alongside it.">
        <div className="grid gap-5 xl:grid-cols-2"><div><p className="mb-3 text-xs font-bold uppercase tracking-[0.17em] text-slate">Stories by top-level category</p><CategoryChart data={dashboard.categoryBreakdown ?? []} /></div><div><p className="mb-3 text-xs font-bold uppercase tracking-[0.17em] text-slate">Publishing trend</p><PublishingTrendChart data={dashboard.publishingTrend ?? []} /></div></div>
      </DashboardSection> : null}

      {engagement ? <DashboardSection title="Engagement" description="Actual likes and comments created during the selected period. Engagement score is a transparent count, not a ranking model.">
        <div className="grid gap-4 sm:grid-cols-3"><DashboardMetricCard label="Likes" value={formatNumber(engagement.likes)} detail="Story likes created" icon={CheckCircle2} tone="bg-[#eef3eb]" /><DashboardMetricCard label="Comments" value={formatNumber(engagement.comments)} detail="Comments created" icon={MessageSquare} tone="bg-[#eaf1f4]" /><DashboardMetricCard label="Comment likes" value={formatNumber(engagement.commentLikes)} detail="Comment likes created" icon={CheckCircle2} tone="bg-[#f0edf5]" /></div>
        <div className="mt-8"><p className="mb-3 text-xs font-bold uppercase tracking-[0.17em] text-slate">Most engaged stories</p>{dashboard.topStories?.length ? <div className="overflow-hidden rounded-3xl border border-line bg-white">{dashboard.topStories.map((story, index) => <div key={story.storyId} className="flex items-start gap-4 border-b border-line px-5 py-4 last:border-b-0"><span className="grid h-8 w-8 shrink-0 place-items-center rounded-lg bg-mist text-sm font-bold text-ink">{index + 1}</span><div className="min-w-0 flex-1"><p className="truncate font-bold text-ink">{story.title}</p><p className="mt-1 text-xs text-slate">{story.category} · {formatNumber(story.likeCount)} likes · {formatNumber(story.commentCount)} comments · {formatNumber(story.commentLikeCount)} comment likes</p></div><span className="shrink-0 text-sm font-bold text-coral">{formatNumber(story.engagementCount)}</span></div>)}</div> : <EmptyPanel>No engagement data for this period.</EmptyPanel>}</div>
      </DashboardSection> : null}

      {(newspaper || advertising || staff) ? <DashboardSection title="Operations" description="Current-state checks for the modules you can access.">
        <div className="grid gap-5 lg:grid-cols-2 xl:grid-cols-3">
          {newspaper ? <Card className="p-6"><div className="flex items-start justify-between"><div><p className="text-xs font-bold uppercase tracking-[0.17em] text-slate">Today's newspaper</p><p className={`mt-3 font-display text-3xl font-bold ${newspaper.todayPublished ? "text-ink" : "text-coral-dark"}`}>{newspaper.todayPublished ? "Published" : "Not published"}</p></div><BookOpen className={newspaper.todayPublished ? "text-[#5b8c67]" : "text-coral"} size={22} aria-hidden="true" /></div><div className="mt-5 space-y-2 text-sm text-slate">{newspaper.editions.length ? newspaper.editions.map((edition) => <div key={edition.editionId} className="flex items-center justify-between gap-3"><span className="truncate font-semibold text-ink">{edition.editionName}</span><span>{edition.status === "PUBLISHED" ? `Published ${formatTime(edition.publishedAt, dashboard.period.timezone)}` : edition.status.toLowerCase()}</span></div>) : <p>No edition has been created for today.</p>}</div><Link href="/admin/newspapers" className="mt-5 inline-block text-sm font-bold text-coral hover:text-coral-dark">Open newspapers →</Link></Card> : null}
          {advertising ? <Card className="p-6"><div className="flex items-start justify-between"><div><p className="text-xs font-bold uppercase tracking-[0.17em] text-slate">Advertising</p><p className="mt-3 font-display text-3xl font-bold text-ink">{formatNumber(advertising.active)} active</p></div><Megaphone className="text-coral" size={22} aria-hidden="true" /></div><div className="mt-5 grid grid-cols-3 gap-2 text-center text-xs"><div className="rounded-xl bg-mist p-3"><p className="font-bold text-ink">{advertising.scheduled}</p><p className="mt-1 text-slate">Scheduled</p></div><div className="rounded-xl bg-mist p-3"><p className="font-bold text-ink">{advertising.paused}</p><p className="mt-1 text-slate">Paused</p></div><div className="rounded-xl bg-mist p-3"><p className="font-bold text-ink">{advertising.expiringSoon}</p><p className="mt-1 text-slate">Expiring</p></div></div>{advertising.expiringAds.length ? <div className="mt-5 space-y-3">{advertising.expiringAds.map((ad) => <div key={ad.advertisementId} className="flex items-start justify-between gap-3 text-sm"><div className="min-w-0"><p className="truncate font-bold text-ink">{ad.advertiserName}</p><p className="truncate text-xs text-slate">{ad.title} · {ad.placement}</p></div><span className="shrink-0 font-bold text-coral-dark">{timeUntil(ad.endAt)}</span></div>)}</div> : <p className="mt-5 text-sm text-slate">No active ads expire within the configured window.</p>}<Link href="/admin/ads" className="mt-5 inline-block text-sm font-bold text-coral hover:text-coral-dark">Open advertisements →</Link></Card> : null}
          {staff ? <Card className="p-6"><div className="flex items-start justify-between"><div><p className="text-xs font-bold uppercase tracking-[0.17em] text-slate">Staff</p><p className="mt-3 font-display text-3xl font-bold text-ink">{formatNumber(staff.activeStaff)} active</p></div><Users className="text-coral" size={22} aria-hidden="true" /></div><div className="mt-5 space-y-3 text-sm"><div className="flex justify-between gap-3"><span className="text-slate">Disabled</span><span className="font-bold text-ink">{formatNumber(staff.disabledStaff)}</span></div><div className="flex justify-between gap-3"><span className="text-slate">Pending setup</span><span className="font-bold text-ink">{formatNumber(staff.pendingSetupStaff)}</span></div></div><Link href="/admin/staff" className="mt-5 inline-block text-sm font-bold text-coral hover:text-coral-dark">Open staff →</Link></Card> : null}
        </div>
      </DashboardSection> : null}

      <DashboardSection title="Recent activity" description="The latest authorized business-changing actions.">
        {dashboard.recentActivity.length ? <div className="overflow-hidden rounded-3xl border border-line bg-white">{dashboard.recentActivity.map((activity) => <div key={activity.activityId} className="flex items-start gap-4 border-b border-line px-5 py-4 last:border-b-0"><div className="mt-1 h-2.5 w-2.5 shrink-0 rounded-full bg-coral" /><div className="min-w-0 flex-1"><p className="text-sm text-ink"><span className="font-bold">{activity.actorName ?? "System"}</span> {activityVerb(activity.action)}{activity.targetLabel ? <> <span className="font-semibold">“{activity.targetLabel}”</span></> : null}</p><p className="mt-1 text-xs text-slate">{formatTime(activity.createdAt, dashboard.period.timezone)} · {activity.action}</p></div></div>)}</div> : <EmptyPanel>No recent administrative activity.</EmptyPanel>}
      </DashboardSection>
    </div>
  );
}

export default function AdminDashboardPage() {
  return <AdminRoute><DashboardContent /></AdminRoute>;
}
