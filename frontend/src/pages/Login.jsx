import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/useAuthStore';
import { initKeycloak } from '../config/keycloak';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card';
import { Dumbbell, Loader2 } from 'lucide-react';

const Login = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const { setUser, setToken } = useAuthStore();
  const navigate = useNavigate();

  const handleKeycloakLogin = async () => {
    setLoading(true);
    setError('');
    
    try {
      const keycloak = await initKeycloak();
      const userProfile = {
        id: keycloak.tokenParsed?.sub,
        email: keycloak.tokenParsed?.email,
        firstName: keycloak.tokenParsed?.given_name,
        lastName: keycloak.tokenParsed?.family_name,
        keycloakId: keycloak.tokenParsed?.sub,
      };
      
      setUser(userProfile);
      setToken(keycloak.token);
      navigate('/dashboard');
    } catch (err) {
      setError('Failed to authenticate with Keycloak');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const containerStyle = {
    minHeight: '100vh',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    background: 'linear-gradient(to bottom right, #eff6ff, #ffffff, #faf5ff)',
    padding: '16px'
  };

  const cardStyle = {
    width: '100%',
    maxWidth: '448px',
    boxShadow: '0 20px 25px -5px rgb(0 0 0 / 0.1)',
    borderRadius: '0.5rem',
    background: 'white',
    padding: '24px'
  };

  const headerStyle = {
    textAlign: 'center',
    marginBottom: '24px'
  };

  const inputStyle = {
    width: '100%',
    padding: '8px 12px',
    border: '1px solid #d1d5db',
    borderRadius: '0.375rem'
  };

  const buttonStyle = {
    width: '100%',
    padding: '12px',
    backgroundColor: '#2563eb',
    color: 'white',
    borderRadius: '0.375rem',
    fontWeight: '500',
    cursor: loading ? 'not-allowed' : 'pointer',
    opacity: loading ? 0.5 : 1
  };

  return (
    <div style={containerStyle}>
      <div style={cardStyle}>
        <div style={headerStyle}>
          <div style={{ display: 'flex', justifyContent: 'center', marginBottom: '16px' }}>
            <div style={{ color: '#2563eb', fontSize: '48px' }}>🏋️</div>
          </div>
          <h1 style={{ fontSize: '24px', fontWeight: 'bold', marginBottom: '8px' }}>Welcome Back</h1>
          <p style={{ color: '#6b7280' }}>Sign in to your AI Fitness account</p>
        </div>
        
        {error && (
          <div style={{ padding: '12px', marginBottom: '16px', backgroundColor: '#fef2f2', color: '#dc2626', borderRadius: '0.375rem', fontSize: '14px' }}>
            {error}
          </div>
        )}
        
        <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          <div>
            <label style={{ display: 'block', marginBottom: '8px', fontSize: '14px', fontWeight: '500' }}>Email</label>
            <input
              type="email"
              placeholder="you@example.com"
              disabled={loading}
              style={inputStyle}
            />
          </div>
          
          <div>
            <label style={{ display: 'block', marginBottom: '8px', fontSize: '14px', fontWeight: '500' }}>Password</label>
            <input
              type="password"
              placeholder="••••••••"
              disabled={loading}
              style={inputStyle}
            />
          </div>

          <button
            onClick={handleKeycloakLogin}
            disabled={loading}
            style={buttonStyle}
          >
            {loading ? 'Signing in...' : 'Sign in with Keycloak'}
          </button>
        </div>

        <div style={{ textAlign: 'center', marginTop: '24px', fontSize: '14px', color: '#6b7280' }}>
          Don't have an account?{' '}
          <Link to="/register" style={{ color: '#2563eb', textDecoration: 'underline' }}>
            Sign up
          </Link>
        </div>
      </div>
    </div>
  );
};

export default Login;
