import { AdminRoute, PermissionRoute } from "@/components/auth/ProtectedRoute";
import { AdvertisementForm } from "@/components/admin/AdvertisementForm";

export default function NewAdvertisementPage() { return <AdminRoute><PermissionRoute permission="AD_CREATE"><AdvertisementForm /></PermissionRoute></AdminRoute>; }
