import { apiDelete, apiGet, apiPatch, apiPost } from "./client";

export type AnalystRole = "ADMIN" | "ANALYST";

export type AnalystResponse = {
  id: number;
  username: string;
  role: AnalystRole;
  enabled: boolean;
};

export type CreateAnalystRequest = {
  username: string;
  password: string;
};

export type ResetAnalystPasswordRequest = {
  password: string;
};

export function getAnalysts(): Promise<AnalystResponse[]> {
  return apiGet<AnalystResponse[]>("/api/admin/analysts");
}

export function createAnalyst(
  request: CreateAnalystRequest,
): Promise<AnalystResponse> {
  return apiPost<AnalystResponse>("/api/admin/analysts", request);
}

export function resetAnalystPassword(
  id: number,
  request: ResetAnalystPasswordRequest,
): Promise<AnalystResponse> {
  return apiPatch<AnalystResponse>(
    `/api/admin/analysts/${id}/password`,
    request,
  );
}

export function setAnalystEnabled(
  id: number,
  enabled: boolean,
): Promise<AnalystResponse> {
  return apiPatch<AnalystResponse>(
    `/api/admin/analysts/${id}/enabled?enabled=${enabled}`,
  );
}

export function deleteAnalyst(id: number): Promise<void> {
  return apiDelete<void>(`/api/admin/analysts/${id}`);
}

export function setAnalystRole(id: number, role: AnalystRole,): Promise<AnalystResponse> {
  return apiPatch<AnalystResponse>(
    `/api/admin/analysts/${id}/role?role=${role}`,
  );
}
