import { useState } from 'react';
import api from '../api/api';

function Register() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    try {
      await api.post('/api/auth/register', {
        email,
        password,
      });
      alert('Регистрация успешна! Теперь войдите.');
      // window.location.href = '/'; // или navigate('/')
    } catch (err) {
      const message =
        err.response?.data?.message ||
        err.message ||
        'Ошибка регистрации. Проверьте данные.';
      setError(message);
    }
  };

  return (
    <form onSubmit={handleSubmit} style={{ maxWidth: '400px', margin: '2rem auto' }}>
      <h2>Регистрация</h2>

      <label style={{ display: 'block', marginTop: '1rem' }}>Email</label>
      <input
        type="email"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
        required
        style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }}
      />

      <label style={{ display: 'block', marginTop: '1rem' }}>Пароль</label>
      <input
        type="password"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        required
        style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }}
      />

      <button
        type="submit"
        style={{
          marginTop: '1.5rem',
          padding: '0.75rem 1.5rem',
          backgroundColor: '#007bff',
          color: '#fff',
          border: 'none',
          cursor: 'pointer',
        }}
      >
        Зарегистрироваться
      </button>

      {error && (
        <p style={{ color: 'red', marginTop: '1rem', fontSize: '0.9rem' }}>
          {error}
        </p>
      )}
    </form>
  );
}

export default Register;
