export type Role = "USER" | "ADMIN" | string;

export type RoleSummary = {
  id: string;
  code: string;
  name: string;
};

export type User = {
  id: string;
  name: string;
  email: string;
  role: Role;
  roles?: RoleSummary[];
  permissions?: string[];
  status?: "ACTIVE" | "DISABLED" | "PENDING_SETUP" | string;
};

export type RegisterRequest = {
  name: string;
  email: string;
  password: string;
};

export type LoginRequest = {
  email: string;
  password: string;
};

export type AuthResponse = {
  accessToken: string;
  tokenType: "Bearer";
  expiresIn: number;
  user: User;
};

export type ApiError = {
  timestamp?: string;
  status?: number;
  code?: string;
  message: string;
  errors?: Record<string, string>;
  path?: string;
};

export type Permission = {
  code: string;
  name: string;
  description: string;
  category: string;
};

export type PermissionGroup = {
  category: string;
  permissions: Permission[];
};

export type AdminRole = {
  id: string;
  name: string;
  code: string;
  description?: string;
  systemRole: boolean;
  status: "ACTIVE" | "INACTIVE";
  permissionCodes: string[];
};

export type StaffUser = {
  id: string;
  name: string;
  email: string;
  status: "ACTIVE" | "DISABLED" | "PENDING_SETUP" | string;
  roles: RoleSummary[];
  setupLink?: string | null;
};
