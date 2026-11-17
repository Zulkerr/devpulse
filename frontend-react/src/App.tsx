import { useState, useEffect } from 'react'
import './App.css'

interface MonitoredService {
  id: number;
  name: string;
  url: string;
  status: 'UP' | 'DOWN' | 'UNKNOWN';
  lastChecked: string | null;
}

interface Incident {
  id: number;
  serviceId: number;
  title: string;
  severity: string;
  resolved: boolean;
  createdAt: string;
}

function App() {
  const [services, setServices] = useState<MonitoredService[]>([]);
  const [incidents, setIncidents] = useState<Incident[]>([]);
  const [loading, setLoading] = useState(false);
  const [newSvc, setNewSvc] = useState({ name: '', url: '' });

  const fetchData = async () => {
    try {
      const sResp = await fetch('http://localhost:8080/api/services');
      setServices(await sResp.json());
      const iResp = await fetch('http://localhost:8080/api/incidents');
      setIncidents(await iResp.json());
    } catch (e) {
      console.error("Fetch failed", e);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const addService = async (e: React.FormEvent) => {
    e.preventDefault();
    await fetch('http://localhost:8080/api/services', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(newSvc)
    });
    setNewSvc({ name: '', url: '' });
    fetchData();
  };

  const runCheck = async () => {
    setLoading(true);
    try {
      await fetch('http://localhost:8000/check-all', { method: 'POST' });
    } catch (e) {
      console.error(e);
    }
    fetchData();
    setLoading(false);
  };

  const resolveIncident = async (id: number) => {
    await fetch(`http://localhost:8080/api/incidents/${id}/resolve`, { method: 'PATCH' });
    fetchData();
  };

  return (
    <div>
      <h1>DevPulse Dashboard</h1>

      <div className="container">
        <div>
          <div className="card">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <h2>Monitored Services</h2>
              <button onClick={runCheck} disabled={loading} className="btn-secondary">
                {loading ? 'Checking...' : 'Run Health Check Now'}
              </button>
            </div>
            <table>
              <thead>
                <tr>
                  <th>Name</th>
                  <th>URL</th>
                  <th>Status</th>
                  <th>Last Checked</th>
                </tr>
              </thead>
              <tbody>
                {services.map(s => (
                  <tr key={s.id}>
                    <td>{s.name}</td>
                    <td><code>{s.url}</code></td>
                    <td><span className={`badge badge-${s.status}`}>{s.status}</span></td>
                    <td>{s.lastChecked ? new Date(s.lastChecked).toLocaleString() : '-'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="card">
            <h2>Add New Service</h2>
            <form onSubmit={addService}>
              <div className="form-group">
                <input
                  placeholder="Service Name (e.g. Google)"
                  value={newSvc.name}
                  onChange={e => setNewSvc({...newSvc, name: e.target.value})}
                  required
                />
                <input
                  placeholder="URL (https://...)"
                  value={newSvc.url}
                  onChange={e => setNewSvc({...newSvc, url: e.target.value})}
                  required
                />
              </div>
              <button type="submit" className="btn-success">Register Service</button>
            </form>
          </div>
        </div>

        <div>
          <div className="card">
            <h2>Incidents</h2>
            {incidents.filter(i => !i.resolved).length === 0 ? (
              <p style={{color: '#94a3b8'}}>No active incidents. System healthy.</p>
            ) : (
              incidents.filter(i => !i.resolved).map(i => (
                <div key={i.id} style={{borderBottom: '1px solid #334155', padding: '10px 0'}}>
                  <div style={{display: 'flex', justifyContent: 'space-between'}}>
                    <strong>{i.title}</strong>
                    <span style={{color: '#f87171'}}>{i.severity}</span>
                  </div>
                  <p style={{fontSize: '0.8rem', margin: '5px 0'}}>Created: {new Date(i.createdAt).toLocaleString()}</p>
                  <button onClick={() => resolveIncident(i.id)} style={{fontSize: '0.7rem', padding: '4px 8px'}}>Resolve</button>
                </div>
              ))
            )}

            <h3 style={{marginTop: '2rem', fontSize: '1rem'}}>History</h3>
            {incidents.filter(i => i.resolved).map(i => (
              <div key={i.id} style={{opacity: 0.5, fontSize: '0.9rem'}}>
                <s>{i.title}</s> (Resolved)
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  )
}

export default App
