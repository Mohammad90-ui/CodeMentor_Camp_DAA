import { useEffect, useState } from 'react';
import { NavLink, Navigate, Route, Routes, useNavigate } from 'react-router-dom';
import { api } from './api';
import { AuthCtx } from './auth';
import { Loading } from './ui';
import Auth from './pages/Auth';
import Dashboard from './pages/Dashboard';
import { Catalogue, ProblemDetails, RecordAttempt } from './pages/Problems';
import { Queue, Topics, Bookmarks } from './pages/Practice';
import Profile from './pages/Profile';

export default function App() {
  const [user, setUser] = useState(null);
  const [ready, setReady] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    if (!localStorage.getItem('token')) { setReady(true); return; }
    api('/me').then(setUser).catch(() => localStorage.removeItem('token')).finally(() => setReady(true));
  }, []);

  const login = ({ token, user }) => { localStorage.setItem('token', token); setUser(user); navigate('/'); };
  const logout = () => { localStorage.removeItem('token'); setUser(null); navigate('/login'); };

  if (!ready) return <div className="page"><Loading /></div>;
  if (!user) {
    return (
      <Routes>
        <Route path="/login" element={<Auth onAuth={login} />} />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    );
  }

  const links = [['/', 'Dashboard'], ['/problems', 'Problems'], ['/queue', 'Practice Queue'], ['/topics', 'Topics'], ['/bookmarks', 'Bookmarks'], ['/profile', 'Profile']];
  return (
    <AuthCtx.Provider value={{ user, setUser }}>
      <header className="nav">
        <span className="brand">⌘ CodeMentor Campus</span>
        <nav>
          {links.map(([to, label]) => (
            <NavLink key={to} to={to} end={to === '/'} className={({ isActive }) => (isActive ? 'active' : '')}>{label}</NavLink>
          ))}
        </nav>
        <span className="grow" />
        <span className="muted">{user.name}</span>
        <button className="btn small ghost" onClick={logout}>Log out</button>
      </header>
      <main className="page">
        <Routes>
          <Route path="/" element={<Dashboard />} />
          <Route path="/problems" element={<Catalogue />} />
          <Route path="/problems/:id" element={<ProblemDetails />} />
          <Route path="/problems/:id/attempt" element={<RecordAttempt />} />
          <Route path="/queue" element={<Queue />} />
          <Route path="/topics" element={<Topics />} />
          <Route path="/bookmarks" element={<Bookmarks />} />
          <Route path="/profile" element={<Profile />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
    </AuthCtx.Provider>
  );
}
