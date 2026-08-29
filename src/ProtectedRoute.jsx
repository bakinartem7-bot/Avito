import React from 'react';
import { Route, Redirect } from 'react-router-dom';
import api from '../config/api'; // Убедитесь, что путь к api правильный

const ProtectedRoute = ({ element, ...rest }) => {
  const [isAuthenticated, setIsAuthenticated] = React.useState(false);

  // Проверка аутентификации при монтировании компонента
  React.useEffect(() => {
    const accessToken = localStorage.getItem('accessToken');
    if (accessToken) {
      api.defaults.headers.common['Authorization'] = `Bearer ${accessToken}`;
      setIsAuthenticated(true);
    } else {
      setIsAuthenticated(false);
    }
  }, []);

  // Логика перенаправления
  const redirectToLogin = () => {
    localStorage.removeItem('accessToken');
    return <Redirect to="/" />;
  };

  return (
    <Route
      {...rest}
      render={(props) =>
        isAuthenticated ? (
          element
        ) : (
          redirectToLogin()
        )
      }
    />
  );
};

export default ProtectedRoute;
