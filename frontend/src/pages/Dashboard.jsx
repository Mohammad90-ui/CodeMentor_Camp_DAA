import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { api } from '../api';
import { Difficulty, ErrorMsg, Loading, RESULTS, MISTAKES, Tags, fmtDate } from '../ui';

export default function Dashboard() {
  const [d, setD] = useState(null);
  const [error, setError] = useState('');
  useEffect(() => { api('/dashboard').then(setD).catch((e) => setError(e.message)); }, []);
  if (error) return <ErrorMsg error={error} />;
  if (!d) return <Loading />;

  const goalPct = d.weeklyGoal ? Math.min(100, Math.round((100 * d.weeklyAttempts) / d.weeklyGoal)) : null;
  const days = d.weeklyActivity.map((x) => ({ ...x, day: new Date(x.date + 'T00:00').toLocaleDateString(undefined, { weekday: 'short' }) }));

  return (
    <>
      <h2>Dashboard</h2>
      <div className="stats">
        <div className="card stat"><b>{d.totalAttempts}</b><span>Total attempts</span></div>
        <div className="card stat"><b>{d.solvedProblems}<small> / {d.totalProblems}</small></b><span>Problems solved</span></div>
        <div className="card stat"><b>{d.weeklyAttempts}</b><span>Attempts in last 7 days</span></div>
        <div className="card stat">
          <b>{goalPct === null ? '—' : `${goalPct}%`}</b>
          <span>{d.weeklyGoal ? `Weekly goal (${d.weeklyGoal} attempts)` : 'No weekly goal set'}</span>
          {goalPct !== null && <div className="bar"><i style={{ width: `${goalPct}%` }} /></div>}
        </div>
      </div>

      <div className="grid2">
        <div className="card">
          <h3>Weekly activity</h3>
          <ResponsiveContainer width="100%" height={220}>
            <BarChart data={days}>
              <CartesianGrid strokeDasharray="3 3" vertical={false} />
              <XAxis dataKey="day" /><YAxis allowDecimals={false} /><Tooltip />
              <Bar dataKey="attempts" fill="#6366f1" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
        <div className="card">
          <h3>Topics that need practice</h3>
          {d.weakTopics.length === 0 && <p className="muted">Record some attempts to see your weak topics.</p>}
          {d.weakTopics.map((t) => (
            <div key={t.topic} className="topicline">
              <span>{t.topic}</span>
              <div className="bar"><i className={t.successRate < 50 ? 'low' : ''} style={{ width: `${t.successRate}%` }} /></div>
              <span className="muted nowrap">{t.successRate}% · {t.attempts} tries</span>
            </div>
          ))}
          <Link to="/topics">All topic progress →</Link>
        </div>
      </div>

      <div className="card">
        <div className="between"><h3>Recommended next</h3><Link to="/queue">Full practice queue →</Link></div>
        {d.recommendations.map((r) => (
          <div key={r.problemId} className="rec">
            <div className="between">
              <Link to={`/problems/${r.problemId}`} className="title">{r.title}</Link>
              <span><Difficulty value={r.difficulty} /> <Tags topics={r.topics} /></span>
            </div>
            <p className="reason">{r.reason}</p>
          </div>
        ))}
      </div>

      <div className="card">
        <h3>Recent activity</h3>
        {d.recent.length === 0 && <p className="muted">No attempts yet. Open a problem and record your first attempt.</p>}
        {d.recent.map((a) => (
          <div key={a.id} className="row">
            <Link to={`/problems/${a.problemId}`} className="title grow">{a.problemTitle}</Link>
            <span className={`badge ${a.result.toLowerCase()}`}>{RESULTS[a.result]}</span>
            {a.mistakeType && <span className="muted">{MISTAKES[a.mistakeType]}</span>}
            <span className="muted nowrap">{fmtDate(a.createdAt)}</span>
          </div>
        ))}
      </div>
    </>
  );
}
