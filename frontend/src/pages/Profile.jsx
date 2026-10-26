import { useState } from 'react';
import { api } from '../api';
import { useAuth } from '../auth';
import { ErrorMsg, LANGUAGES } from '../ui';

export default function Profile() {
  const { user, setUser } = useAuth();
  const [form, setForm] = useState({ name: user.name, preferredLanguage: user.preferredLanguage || 'Java', weeklyGoal: user.weeklyGoal ?? '' });
  const [msg, setMsg] = useState('');
  const [error, setError] = useState('');
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const save = async (e) => {
    e.preventDefault();
    setMsg(''); setError('');
    try {
      setUser(await api('/me', { method: 'PUT', body: { ...form, weeklyGoal: form.weeklyGoal === '' ? null : Number(form.weeklyGoal) } }));
      setMsg('Profile saved.');
    } catch (err) { setError(err.message); }
  };

  return (
    <>
      <h2>Profile</h2>
      <form className="card narrow" onSubmit={save}>
        <label>Email<input value={user.email} disabled /></label>
        <label>Name<input value={form.name} onChange={set('name')} required /></label>
        <label>Preferred programming language
          <select value={form.preferredLanguage} onChange={set('preferredLanguage')}>{LANGUAGES.map((l) => <option key={l}>{l}</option>)}</select>
        </label>
        <label>Weekly practice goal (attempts per week, optional)
          <input type="number" min="0" max="100" value={form.weeklyGoal} onChange={set('weeklyGoal')} />
        </label>
        <ErrorMsg error={error} />
        {msg && <p className="ok">{msg}</p>}
        <button className="btn">Save profile</button>
      </form>
    </>
  );
}
