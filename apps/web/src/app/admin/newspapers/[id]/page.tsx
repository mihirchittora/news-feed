"use client";

import { NewspaperForm } from "@/components/admin/NewspaperForm";
import { LoadingState } from "@/components/ui/LoadingState";
import { adminApi } from "@/lib/api/admin";
import { ApiClientError } from "@/lib/api/client";
import { useAuth } from "@/components/auth/AuthProvider";
import { useParams } from "next/navigation";
import { useEffect, useState } from "react";
import type { AdminNewspaper } from "@/lib/types";

export default function EditNewspaperPage() {
  const { token } = useAuth();
  const params = useParams<{ id: string }>();
  const [item, setItem] = useState<AdminNewspaper | null>(null);
  const [error, setError] = useState("");
  useEffect(() => { if (!token || !params.id) return; adminApi.newspaper(token, params.id).then(setItem).catch((reason) => setError(reason instanceof ApiClientError ? reason.message : "Could not load the newspaper.")); }, [token, params.id]);
  if (error) return <div className="p-8 text-sm text-red-700">{error}</div>;
  if (!item) return <LoadingState label="Loading newspaper" />;
  return <NewspaperForm initialNewspaper={item} />;
}
