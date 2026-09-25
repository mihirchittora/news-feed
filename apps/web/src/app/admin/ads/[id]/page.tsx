"use client";

import { AdminRoute, PermissionRoute } from "@/components/auth/ProtectedRoute";
import { AdvertisementForm } from "@/components/admin/AdvertisementForm";
import { Alert } from "@/components/ui/Alert";
import { LoadingState } from "@/components/ui/LoadingState";
import { adminApi } from "@/lib/api/admin";
import { ApiClientError } from "@/lib/api/client";
import type { AdminAdvertisement } from "@/lib/types";
import { useParams } from "next/navigation";
import { useEffect, useState } from "react";
import { useAuth } from "@/components/auth/AuthProvider";

function EditAdvertisementContent({ id }: { id: string }) {
  const { token } = useAuth();
  const [advertisement, setAdvertisement] = useState<AdminAdvertisement | null>(null);
  const [error, setError] = useState("");
  useEffect(() => { if (!token) return; adminApi.advertisement(token, id).then(setAdvertisement).catch((reason) => setError(reason instanceof ApiClientError ? reason.message : "Could not load the advertisement.")); }, [id, token]);
  if (error) return <Alert>{error}</Alert>;
  if (!advertisement) return <LoadingState label="Loading advertisement" />;
  return <AdvertisementForm initialAdvertisement={advertisement} />;
}

export default function EditAdvertisementPage() {
  const params = useParams<{ id: string }>();
  return <AdminRoute><PermissionRoute permission="AD_VIEW_ADMIN"><EditAdvertisementContent id={params.id} /></PermissionRoute></AdminRoute>;
}
