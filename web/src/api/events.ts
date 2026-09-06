import { apiGet, apiPatch } from './client'
import type { Event, EventStatus } from '../types/Event'

export function getEvents(): Promise<Event[]> {
  return apiGet<Event[]>('/api/events')
}

export function updateEventStatus(
  id: number,
  status: EventStatus,
): Promise<Event> {
  return apiPatch<Event>(
    `/api/events/${id}/status?status=${status}`,
  )
}