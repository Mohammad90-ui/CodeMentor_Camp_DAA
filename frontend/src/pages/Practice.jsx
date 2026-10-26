import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { api } from '../api';
import { Difficulty, ErrorMsg, Loading, ProblemRow, Tags } from '../ui';

function useLoad(path) {
  const [data, setData] = useState(null);
  const [error, setError] = useState('');
  useEffect(() => { api(path).then(setData).catch((e) => setError(e.message)); }, [path]);
  return [data, setData, error];
}

export function Queue() {
  const [recs, , error] = useLoad('/recommendations?limit=10');
  return (
    <>
      <h2>Practice Queue</h2>
      <p className="muted">Ranked by a score built from topic weakness, time since you last practised, your level, and unfinished problems. Highest priority first.</p>
      <ErrorMsg error={error} />
      {!recs ? <Loading /> : recs.length === 0 ? <p className="muted">You have solved everything. Impressive!</p> : (
        <div className="card">
          {recs.map((r, i) => (
            <div key={r.problemId} className="rec">
              <div className="between">
                <span><span className="rank">#{i + 1}</span> <Link to={`/problems/${r.problemId}`} className="title">{r.title}</Link></span>
                <span><Difficulty value={r.difficulty} /> <Tags topics={r.topics} /> <span className="score">score {r.score}</span></span>
              </div>
              <p className="reason">{r.reason}</p>
            </div>
          ))}
        </div>
      )}
    </>
  );
}

export function Topics() {
  const [rows, , error] = useLoad('/topics/progress');
  if (error) return <ErrorMsg error={error} />;
  if (!rows) return <Loading />;
  return (
    <>
      <h2>Topic Progress</h2>
      <div className="card">
        <h3>Success rate by topic (%)</h3>
        <ResponsiveContainer width="100%" height={260}>
          <BarChart data={rows}>
            <CartesianGrid strokeDasharray="3 3" vertical={false} />
            <XAxis dataKey="topic" interval={0} tick={{ fontSize: 12 }} /><YAxis domain={[0, 100]} /><Tooltip />
            <Bar dataKey="successRate" name="Success rate %" fill="#10b981" radius={[4, 4, 0, 0]} />
          </BarChart>
        </ResponsiveContainer>
      </div>
      <div className="card">
        <table>
          <thead><tr><th>Topic</th><th>Attempts</th><th>Solved attempts</th><th>Success rate</th><th>Problems solved</th></tr></thead>
          <tbody>
            {rows.map((t) => (
              <tr key={t.topic}>
                <td><Link to={`/problems`} state={{ topic: t.topic }}>{t.topic}</Link></td>
                <td>{t.attempts}</td><td>{t.solvedAttempts}</td>
                <td><div className="bar inline"><i className={t.attempts && t.successRate < 50 ? 'low' : ''} style={{ width: `${t.successRate}%` }} /></div> {t.attempts ? `${t.successRate}%` : '—'}</td>
                <td>{t.solvedProblems} / {t.totalProblems}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </>
  );
}

export function Bookmarks() {
  const [list, setList, error] = useLoad('/bookmarks');
  return (
    <>
      <h2>Bookmarks</h2>
      <ErrorMsg error={error} />
      {!list ? <Loading /> : (
        <div className="card">
          {list.length === 0 && <p className="muted">No bookmarks yet. Use “☆ Save” on any problem to revisit it later.</p>}
          {list.map((p) => <ProblemRow key={p.id} p={p} onBookmark={() => setList(list.filter((x) => x.id !== p.id))} />)}
        </div>
      )}
    </>
  );
}
