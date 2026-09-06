import { useEffect, useState } from "react";
import { getCases, updateCaseStatus } from "../api/cases";
import type { InvestigationCase } from "../types/Case";

function CasesPage() {
  const [cases, setCases] = useState<InvestigationCase[]>([]);

  const [selectedCase, setSelectedCase] = useState<InvestigationCase | null>(
    null,
  );

  const [loading, setLoading] = useState(true);

  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    async function loadCases() {
      try {
        const data = await getCases();
        setCases(data);
      } catch {
        setError("Unable to load cases.");
      } finally {
        setLoading(false);
      }
    }

    loadCases();
  }, []);

  async function handleStatusChange(status: "OPEN" | "CLOSED") {
    if (!selectedCase) {
      return;
    }

    try {
      const updatedCase = await updateCaseStatus(selectedCase.id, status);

      setSelectedCase(updatedCase);

      setCases((currentCases) =>
        currentCases.map((investigationCase) =>
          investigationCase.id === updatedCase.id
            ? updatedCase
            : investigationCase,
        ),
      );

      setError(null);
    } catch {
      setError("Unable to update case status.");
    }
  }

  if (loading) {
    return <p>Loading cases...</p>;
  }

  if (error) {
    return <p>{error}</p>;
  }

  return (
    <section>
      <h2>Cases</h2>

      <p>Security investigations requiring analyst attention.</p>

      {cases.length === 0 ? (
        <p>No cases found.</p>
      ) : (
        <div className="cases-layout">
          <div className="cases-list">
            {cases.map((investigationCase) => (
              <button
                key={investigationCase.id}
                type="button"
                className="case-card"
                onClick={() => setSelectedCase(investigationCase)}
              >
                <div>
                  <strong>Case #{investigationCase.id}</strong>

                  <span>{investigationCase.status}</span>
                </div>

                <h3>{investigationCase.eventType}</h3>

                <p>{investigationCase.message}</p>
              </button>
            ))}
          </div>

          <aside className="case-details">
            {selectedCase ? (
              <>
                <h3>Case #{selectedCase.id}</h3>

                <dl>
                  <dt>Status</dt>
                  <dd>{selectedCase.status}</dd>

                  <dt>Severity</dt>
                  <dd>{selectedCase.severity}</dd>

                  <dt>Source</dt>
                  <dd>{selectedCase.source}</dd>

                  <dt>IP address</dt>
                  <dd>{selectedCase.ipAddress}</dd>

                  <dt>Message</dt>
                  <dd>{selectedCase.message}</dd>

                  <dt>Event</dt>
                  <dd>#{selectedCase.eventId}</dd>

                  <dt>Created</dt>
                  <dd>{new Date(selectedCase.createdAt).toLocaleString()}</dd>
                </dl>

                <div className="case-actions">
                  {selectedCase.status === "OPEN" ? (
                    <button
                      type="button"
                      onClick={() => handleStatusChange("CLOSED")}
                    >
                      Close case
                    </button>
                  ) : (
                    <button
                      type="button"
                      onClick={() => handleStatusChange("OPEN")}
                    >
                      Reopen case
                    </button>
                  )}
                </div>
              </>
            ) : (
              <p>Select a case to view its details.</p>
            )}
          </aside>
        </div>
      )}
    </section>
  );
}

export default CasesPage;
