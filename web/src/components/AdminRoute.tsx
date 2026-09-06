import { Navigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

import type { ReactNode } from "react";

type AdminRouteProps = {
  children: ReactNode;
};

function AdminRoute({ children }: AdminRouteProps) {
  const { user } = useAuth();

  if (user?.role !== "ADMIN") {
    return <Navigate to="/" replace />;
  }

  return children;
}

export default AdminRoute;
