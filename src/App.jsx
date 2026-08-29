import React, { useEffect } from 'react';
import { BrowserRouter, Route, Routes } from 'react-router-dom';
import SignIn from './SignIn';
import ProtectedRoute from './ProtectedRoute';

function App() {
  useEffect(() => {
    // Проверка наличия токена при загрузке приложения
    const accessToken = localStorage.getItem('accessToken');
    if (accessToken) {
      // Перенаправление на защищённую страницу
      window.location.href = '/dashboard';
    }
  }, []);

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<SignIn />} />
        <ProtectedRoute path="/dashboard" element={<Dashboard />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
