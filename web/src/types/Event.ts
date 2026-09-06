/*export type EventType =
  | 'LOGIN_SUCCESS'
  | 'LOGIN_FAILURE'
  | 'ACCESS_DENIED'
  | 'SUSPICIOUS_ACTIVITY'

export type Severity =
  | 'LOW'
  | 'MEDIUM'
  | 'HIGH'
  | 'CRITICAL'
*/

export type Event = {
  id: number
  source: string
  eventType: string
  severity: string
  message: string
  ipAddress: string
  timestamp: string
  status: EventStatus
}

export type EventStatus =
  | 'NEW'
  | 'REVIEWED'
  | 'ESCALATED'