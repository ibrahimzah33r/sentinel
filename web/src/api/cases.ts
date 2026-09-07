import type { InvestigationCase } from "../types/Case";
import { apiDelete, apiGet, apiPatch, apiPost } from "./client";

export function getCases(): Promise<InvestigationCase[]> {
  return apiGet<InvestigationCase[]>("/api/cases");
}

export function createCaseFromEvent(
  eventId: number,
): Promise<InvestigationCase> {
  return apiPost<InvestigationCase>(`/api/cases/from-event/${eventId}`);
}

export function updateCaseStatus(
  id: number,
  status: "OPEN" | "CLOSED",
): Promise<InvestigationCase> {
  return apiPatch<InvestigationCase>(
    `/api/cases/${id}/status?status=${status}`,
  );
}

export function deleteCase(id: number): Promise<void> {
  return apiDelete<void>(`/api/cases/${id}`);
}
