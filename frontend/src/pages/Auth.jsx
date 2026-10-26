import { useState } from 'react';
import { api } from '../api';
import { ErrorMsg } from '../ui';

export default function Auth({ onAuth }) {
  const [mode, setMode] = useState('login');
  const [form, setForm] = useState({ name: '', email: '', password: '' });
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    setError(''); setBusy(true);
    try {
      onAuth(await api(`/auth/${mode}`, { method: 'POST', body: form }));
    } catch (err) { setError(err.message); } finally { setBusy(false); }
  };

  return (
    <div className="auth">
      <form className="card" onSubmit={submit}>
        <h1>CodeMentor Campus</h1>
        <p className="muted">Track your coding practice and find out what to solve next.</p>
        {mode === 'register' && <label>Name<input value={form.name} onChange={set('name')} required /></label>}
        <label>Email<input type="email" value={form.email} onChange={set('email')} required /></label>
        <label>Password<input type="password" minLength={mode === 'register' ? 6 : 1} value={form.password} onChange={set('password')} required /></label>
        <ErrorMsg error={error} />
        <button className="btn" disabled={busy}>{mode === 'login' ? 'Log in' : 'Create account'}</button>
        <p className="muted center">
          {mode === 'login' ? 'New here? ' : 'Already registered? '}
          <a href="#" onClick={(e) => { e.preventDefault(); setMode(mode === 'login' ? 'register' : 'login'); setError(''); }}>
            {mode === 'login' ? 'Create an account' : 'Log in'}
          </a>
        </p>
        {mode === 'login' && (
          <p className="hint">Demo: <a href="#" onClick={(e) => { e.preventDefault(); setForm({ ...form, email: 'demo@campus.edu', password: 'demo1234' }); }}>fill demo@campus.edu / demo1234</a></p>
        )}
      </form>
    </div>
  );
}
