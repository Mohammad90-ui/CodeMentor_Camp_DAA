import { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom';
import { api } from '../api';
import { BookmarkButton, Difficulty, ErrorMsg, Loading, MISTAKES, ProblemRow, RESULTS, Status, Tags, fmtDate } from '../ui';

export function Catalogue() {
  const location = useLocation();   // the Topics page links here with a pre-selected topic
  const [filters, setFilters] = useState({ q: '', topic: location.state?.topic || '', difficulty: '', sort: 'title' });
  const [topics, setTopics] = useState([]);
  const [list, setList] = useState(null);
  const [error, setError] = useState('');
  const set = (k) => (e) => setFilters({ ...filters, [k]: e.target.value });

  useEffect(() => { api('/topics').then(setTopics).catch((e) => setError(e.message)); }, []);
  useEffect(() => {
    const t = setTimeout(() => {   // small delay so we do not call the API on every keystroke
      const qs = new URLSearchParams(Object.entries(filters).filter(([, v]) => v)).toString();
      api('/problems?' + qs).then(setList).catch((e) => setError(e.message));
    }, 200);
    return () => clearTimeout(t);
  }, [filters]);

  const updateBookmark = (id) => (value) => setList(list.map((p) => (p.id === id ? { ...p, bookmarked: value } : p)));

  return (
    <>
      <h2>Problem Catalogue</h2>
      <div className="card filters">
        <input placeholder="Search by title or topic…" value={filters.q} onChange={set('q')} />
        <select value={filters.topic} onChange={set('topic')}>
          <option value="">All topics</option>{topics.map((t) => <option key={t}>{t}</option>)}
        </select>
        <select value={filters.difficulty} onChange={set('difficulty')}>
          <option value="">All difficulties</option><option value="EASY">Easy</option><option value="MEDIUM">Medium</option><option value="HARD">Hard</option>
        </select>
        <select value={filters.sort} onChange={set('sort')}>
          <option value="title">Sort: Title</option><option value="difficulty">Sort: Difficulty</option><option value="time">Sort: Time</option>
        </select>
      </div>
      <ErrorMsg error={error} />
      {!list ? <Loading /> : (
        <div className="card">
          {list.length === 0 && <p className="muted">No problems match your search.</p>}
          {list.map((p) => <ProblemRow key={p.id} p={p} onBookmark={updateBookmark(p.id)} />)}
        </div>
      )}
    </>
  );
}

export function ProblemDetails() {
  const { id } = useParams();
  const [p, setP] = useState(null);
  const [error, setError] = useState('');
  useEffect(() => { api(`/problems/${id}`).then(setP).catch((e) => setError(e.message)); }, [id]);
  if (error) return <ErrorMsg error={error} />;
  if (!p) return <Loading />;

  return (
    <>
      <Link to="/problems">← Back to problems</Link>
      <div className="card">
        <div className="between">
          <h2>{p.title}</h2>
          <BookmarkButton problemId={p.id} bookmarked={p.bookmarked} onChange={(v) => setP({ ...p, bookmarked: v })} />
        </div>
        <p><Difficulty value={p.difficulty} /> <Status value={p.status} /> <span className="muted">~{p.estimatedMinutes} min</span></p>
        <p><Tags topics={p.topics} /></p>
        <p>{p.description}</p>
        <div className="actions">
          <a className="btn ghost" href={p.externalUrl} target="_blank" rel="noreferrer">Solve on an external site ↗</a>
          <Link className="btn" to={`/problems/${p.id}/attempt`}>Record an attempt</Link>
        </div>
      </div>
      <div className="card">
        <h3>Your attempt history</h3>
        {p.attempts.length === 0 && <p className="muted">You have not attempted this problem yet.</p>}
        {p.attempts.map((a) => (
          <div key={a.id} className="row">
            <span className={`badge ${a.result.toLowerCase()}`}>{RESULTS[a.result]}</span>
            <span className="muted">{a.timeSpentMinutes != null ? `${a.timeSpentMinutes} min` : '—'}</span>
            <span className="grow">{a.mistakeType ? <b>{MISTAKES[a.mistakeType]}</b> : null} {a.notes}</span>
            <span className="muted nowrap">{fmtDate(a.createdAt)}</span>
          </div>
        ))}
      </div>
    </>
  );
}

export function RecordAttempt() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [title, setTitle] = useState('');
  const [form, setForm] = useState({ result: 'SOLVED', timeSpentMinutes: '', mistakeType: '', notes: '' });
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });
  useEffect(() => { api(`/problems/${id}`).then((p) => setTitle(p.title)).catch((e) => setError(e.message)); }, [id]);

  const submit = async (e) => {
    e.preventDefault();
    setError(''); setBusy(true);
    try {
      await api('/attempts', {
        method: 'POST',
        body: { problemId: Number(id), result: form.result, mistakeType: form.mistakeType, notes: form.notes,
                timeSpentMinutes: form.timeSpentMinutes === '' ? null : Number(form.timeSpentMinutes) },
      });
      navigate(`/problems/${id}`);
    } catch (err) { setError(err.message); setBusy(false); }
  };

  return (
    <>
      <Link to={`/problems/${id}`}>← Back to {title || 'problem'}</Link>
      <form className="card narrow" onSubmit={submit}>
        <h2>Record attempt{title && `: ${title}`}</h2>
        <fieldset>
          <legend>How did it go?</legend>
          {Object.entries(RESULTS).map(([k, label]) => (
            <label key={k} className="radio"><input type="radio" name="result" checked={form.result === k} onChange={() => setForm({ ...form, result: k })} /> {label}</label>
          ))}
        </fieldset>
        <label>Time spent (minutes)<input type="number" min="0" max="1440" value={form.timeSpentMinutes} onChange={set('timeSpentMinutes')} /></label>
        <label>Main mistake
          <select value={form.mistakeType} onChange={set('mistakeType')}>
            {Object.entries(MISTAKES).map(([k, label]) => <option key={k} value={k}>{label}</option>)}
          </select>
        </label>
        <label>Notes<textarea rows="4" maxLength="1000" value={form.notes} onChange={set('notes')} placeholder="What went wrong? What would you try next time?" /></label>
        <ErrorMsg error={error} />
        <button className="btn" disabled={busy}>Save attempt</button>
      </form>
    </>
  );
}
