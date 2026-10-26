import { Link } from 'react-router-dom';
import { api } from './api';

export const RESULTS = { SOLVED: 'Solved', PARTIAL: 'Partially Solved', UNSOLVED: 'Unsolved' };
export const MISTAKES = {
  '': 'None / not sure', LOGIC_ERROR: 'Logic error', EDGE_CASE: 'Missed edge case', TIME_LIMIT: 'Too slow (time limit)',
  SYNTAX_ERROR: 'Syntax / language error', CONCEPT_GAP: 'Did not know the concept',
};
export const LANGUAGES = ['Java', 'Python', 'C++', 'JavaScript', 'C', 'Go'];
const DIFF = { EASY: 'Easy', MEDIUM: 'Medium', HARD: 'Hard' };

export const Loading = () => <p className="muted">Loading…</p>;
export const ErrorMsg = ({ error }) => (error ? <p className="error">{error}</p> : null);
export const Difficulty = ({ value }) => <span className={`badge ${value.toLowerCase()}`}>{DIFF[value]}</span>;
export const Status = ({ value }) =>
  value === 'SOLVED' ? <span className="badge solved">Solved</span>
  : value === 'ATTEMPTED' ? <span className="badge attempted">Attempted</span>
  : <span className="badge new">New</span>;
export const Tags = ({ topics }) => <>{topics.map((t) => <span key={t} className="tag">{t}</span>)}</>;
export const fmtDate = (s) => new Date(s).toLocaleDateString(undefined, { day: 'numeric', month: 'short', year: 'numeric' });

export function BookmarkButton({ problemId, bookmarked, onChange }) {
  const toggle = async () => {
    await api(`/bookmarks/${problemId}`, { method: bookmarked ? 'DELETE' : 'POST' });
    onChange(!bookmarked);
  };
  return (
    <button className="btn small ghost" onClick={toggle} title={bookmarked ? 'Remove bookmark' : 'Bookmark'}>
      {bookmarked ? '★ Saved' : '☆ Save'}
    </button>
  );
}

/** One row used by the catalogue and bookmarks pages. */
export function ProblemRow({ p, onBookmark }) {
  return (
    <div className="row">
      <div className="grow">
        <Link to={`/problems/${p.id}`} className="title">{p.title}</Link>
        <div><Tags topics={p.topics} /></div>
      </div>
      <Difficulty value={p.difficulty} />
      <span className="muted nowrap">{p.estimatedMinutes} min</span>
      <Status value={p.status} />
      <BookmarkButton problemId={p.id} bookmarked={p.bookmarked} onChange={onBookmark} />
    </div>
  );
}
