import { useEffect, useMemo, useState } from "react";
import { getEvents, updateEventStatus } from "../api/events";
import type { Event, EventStatus } from "../types/Event";
import { createCaseFromEvent } from "../api/cases";
import { ApiError } from "../api/client";

function EventsPage() {
  const [events, setEvents] = useState<Event[]>([]);
  const [selectedEvent, setSelectedEvent] = useState<Event | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [search, setSearch] = useState("");
  const [severity, setSeverity] = useState("ALL");
  const [eventType, setEventType] = useState("ALL");
  const [source, setSource] = useState("ALL");

  useEffect(() => {
    async function loadEvents() {
      try {
        const data = await getEvents();
        setEvents(data);
      } catch {
        setError("Unable to load events.");
      } finally {
        setLoading(false);
      }
    }

    loadEvents();
  }, []);

  const severities = useMemo(
    () => [...new Set(events.map((event) => event.severity))],
    [events],
  );

  const eventTypes = useMemo(
    () => [...new Set(events.map((event) => event.eventType))],
    [events],
  );

  const sources = useMemo(
    () => [...new Set(events.map((event) => event.source))],
    [events],
  );

  const filteredEvents = useMemo(() => {
    const normalizedSearch = search.trim().toLowerCase();

    return events.filter((event) => {
      const matchesSeverity = severity === "ALL" || event.severity === severity;

      const matchesEventType =
        eventType === "ALL" || event.eventType === eventType;

      const matchesSource = source === "ALL" || event.source === source;

      const matchesSearch =
        normalizedSearch === "" ||
        event.message.toLowerCase().includes(normalizedSearch) ||
        event.ipAddress.toLowerCase().includes(normalizedSearch) ||
        event.source.toLowerCase().includes(normalizedSearch) ||
        event.eventType.toLowerCase().includes(normalizedSearch);

      return (
        matchesSeverity && matchesEventType && matchesSource && matchesSearch
      );
    });
  }, [events, search, severity, eventType, source]);

  async function handleStatusChange(status: EventStatus) {
    if (!selectedEvent) {
      return;
    }

    try {
      const updatedEvent = await updateEventStatus(selectedEvent.id, status);

      setSelectedEvent(updatedEvent);

      setEvents((currentEvents) =>
        currentEvents.map((event) =>
          event.id === updatedEvent.id ? updatedEvent : event,
        ),
      );

      setError(null);
    } catch {
      setError("Unable to update event status.");
    }
  }

  async function handleCreateCase() {
    if (!selectedEvent) {
      return;
    }

    try {
      await createCaseFromEvent(selectedEvent.id);
      setError(null);
    } catch (exception) {
      if (exception instanceof ApiError && exception.status === 409) {
        setError("A case already exists for this event.");
        return;
      }

      setError("Unable to create case.");
    }
  }

  if (loading) {
    return <p>Loading events...</p>;
  }

  if (error) {
    return <p>{error}</p>;
  }

  return (
    <section className="events-page">
      <div>
        <h2>Events</h2>
        <p>Review and investigate security events.</p>
      </div>

      <div className="event-filters">
        <input
          type="search"
          placeholder="Search events..."
          value={search}
          onChange={(event) => setSearch(event.target.value)}
        />

        <select
          value={severity}
          onChange={(event) => setSeverity(event.target.value)}
        >
          <option value="ALL">All severities</option>

          {severities.map((value) => (
            <option key={value} value={value}>
              {value}
            </option>
          ))}
        </select>

        <select
          value={eventType}
          onChange={(event) => setEventType(event.target.value)}
        >
          <option value="ALL">All event types</option>

          {eventTypes.map((value) => (
            <option key={value} value={value}>
              {value}
            </option>
          ))}
        </select>

        <select
          value={source}
          onChange={(event) => setSource(event.target.value)}
        >
          <option value="ALL">All sources</option>

          {sources.map((value) => (
            <option key={value} value={value}>
              {value}
            </option>
          ))}
        </select>
      </div>

      {filteredEvents.length === 0 ? (
        <p>No matching events found.</p>
      ) : (
        <div className="events-layout">
          <div className="events-list">
            {filteredEvents.map((event) => (
              <button
                key={event.id}
                type="button"
                className="event-row"
                onClick={() => setSelectedEvent(event)}
              >
                <div>
                  <strong>{event.eventType}</strong>
                  <span>{event.source}</span>
                </div>

                <span
                  className={`severity severity-${event.severity.toLowerCase()}`}
                >
                  {event.severity}
                </span>
              </button>
            ))}
          </div>

          <aside className="event-details">
            {selectedEvent ? (
              <>
                <h3>{selectedEvent.eventType}</h3>

                <dl>
                  <dt>Status</dt>
                  <dd>{selectedEvent.status}</dd>

                  <dt>Severity</dt>
                  <dd>{selectedEvent.severity}</dd>

                  <dt>Source</dt>
                  <dd>{selectedEvent.source}</dd>

                  <dt>Message</dt>
                  <dd>{selectedEvent.message}</dd>

                  <dt>IP address</dt>
                  <dd>{selectedEvent.ipAddress}</dd>

                  <dt>Timestamp</dt>
                  <dd>{new Date(selectedEvent.timestamp).toLocaleString()}</dd>
                </dl>

                <div className="event-actions">
                  <button
                    type="button"
                    onClick={() => handleStatusChange("REVIEWED")}
                  >
                    Mark reviewed
                  </button>

                  <button
                    type="button"
                    onClick={() => handleStatusChange("ESCALATED")}
                  >
                    Escalate
                  </button>
                  {selectedEvent.status === "ESCALATED" && (
                    <button type="button" onClick={handleCreateCase}>
                      Create case
                    </button>
                  )}
                </div>
              </>
            ) : (
              <p>Select an event to view its details.</p>
            )}
          </aside>
        </div>
      )}
    </section>
  );
}

export default EventsPage;
