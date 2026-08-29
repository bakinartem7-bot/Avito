import { useState } from 'react';
import api from '../api/api';

function SignIn() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    try {
      const response = await api.post('/api/auth/login', {
        email,
        password,
      });

      const { accessToken } = response.data;
      localStorage.setItem('accessToken', accessToken);

      // Перенаправление на главную страницу
      window.location.href = '/';
    } catch (err) {
      const message = err.response?.data?.message || 'Ошибка входа. Проверьте логин и пароль.';
      setError(message);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <label>Email:</label>
      <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
      <label>Пароль:</label>
      <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
      <button type="submit">Войти</button>
      {error && <p style={{ color: 'red' }}>{error}</p>}
    </form>
  );
}

export default SignIn;
