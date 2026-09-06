export type CaseStatus =
  | 'OPEN'
  | 'CLOSED'

export type InvestigationCase = {
  id: number
  status: CaseStatus
  createdAt: string
  eventId: number
  eventType: string
  severity: string
  eventStatus: string
  source: string
  message: string
  ipAddress: string
  eventTimestamp: string
}