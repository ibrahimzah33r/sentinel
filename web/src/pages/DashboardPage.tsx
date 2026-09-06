import { useEffect, useMemo, useState } from 'react'
import { getCases } from '../api/cases'
import { getEvents } from '../api/events'
import type { InvestigationCase } from '../types/Case'
import type { Event } from '../types/Event'

function DashboardPage() {
  const [events, setEvents] = useState<Event[]>([])
  const [cases, setCases] = useState<InvestigationCase[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    async function loadDashboard() {
      try {
        const [eventData, caseData] = await Promise.all([
          getEvents(),
          getCases(),
        ])

        setEvents(eventData)
        setCases(caseData)
      } catch {
        setError('Unable to load dashboard.')
      } finally {
        setLoading(false)
      }
    }

    loadDashboard()
  }, [])

  const escalatedEvents = useMemo(
    () =>
      events.filter(
        (event) => event.status === 'ESCALATED',
      ),
    [events],
  )

  const openCases = useMemo(
    () =>
      cases.filter(
        (investigationCase) =>
          investigationCase.status === 'OPEN',
      ),
    [cases],
  )

  const closedCases = useMemo(
    () =>
      cases.filter(
        (investigationCase) =>
          investigationCase.status === 'CLOSED',
      ),
    [cases],
  )

  const recentEvents = useMemo(
    () =>
      [...events]
        .sort(
          (firstEvent, secondEvent) =>
            new Date(secondEvent.timestamp).getTime() -
            new Date(firstEvent.timestamp).getTime(),
        )
        .slice(0, 5),
    [events],
  )

  const recentCases = useMemo(
    () =>
      [...cases]
        .sort(
          (firstCase, secondCase) =>
            new Date(secondCase.createdAt).getTime() -
            new Date(firstCase.createdAt).getTime(),
        )
        .slice(0, 5),
    [cases],
  )

  if (loading) {
    return <p>Loading dashboard...</p>
  }

  if (error) {
    return <p>{error}</p>
  }

  return (
    <section className="dashboard-page">
      <div>
        <h2>Dashboard</h2>
        <p>Current Sentinel security overview.</p>
      </div>

      <div className="dashboard-stats">
        <article className="dashboard-stat">
          <span>Total events</span>
          <strong>{events.length}</strong>
        </article>

        <article className="dashboard-stat">
          <span>Escalated events</span>
          <strong>{escalatedEvents.length}</strong>
        </article>

        <article className="dashboard-stat">
          <span>Open cases</span>
          <strong>{openCases.length}</strong>
        </article>

        <article className="dashboard-stat">
          <span>Closed cases</span>
          <strong>{closedCases.length}</strong>
        </article>
      </div>

      <div className="dashboard-content">
        <section className="dashboard-panel">
          <h3>Recent events</h3>

          {recentEvents.length === 0 ? (
            <p>No events found.</p>
          ) : (
            <div className="dashboard-list">
              {recentEvents.map((event) => (
                <article
                  key={event.id}
                  className="dashboard-list-item"
                >
                  <div>
                    <strong>{event.eventType}</strong>
                    <span>{event.source}</span>
                  </div>

                  <div>
                    <span>{event.severity}</span>
                    <span>{event.status}</span>
                  </div>
                </article>
              ))}
            </div>
          )}
        </section>

        <section className="dashboard-panel">
          <h3>Recent cases</h3>

          {recentCases.length === 0 ? (
            <p>No cases found.</p>
          ) : (
            <div className="dashboard-list">
              {recentCases.map((investigationCase) => (
                <article
                  key={investigationCase.id}
                  className="dashboard-list-item"
                >
                  <div>
                    <strong>
                      Case #{investigationCase.id}
                    </strong>

                    <span>
                      {investigationCase.eventType}
                    </span>
                  </div>

                  <span>
                    {investigationCase.status}
                  </span>
                </article>
              ))}
            </div>
          )}
        </section>
      </div>
    </section>
  )
}

export default DashboardPage