import Keycloak from 'keycloak-js';

const keycloakConfig = {
  url: import.meta.env.VITE_KEYCLOAK_URL || 'http://localhost:8080',
  realm: import.meta.env.VITE_KEYCLOAK_REALM || 'fitness-realm',
  clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID || 'fitness-app',
};

const keycloak = new Keycloak(keycloakConfig);

export const initKeycloak = () => {
  return new Promise((resolve, reject) => {
    keycloak
      .init({
        onLoad: 'login-required',
        checkLoginIframe: false,
        pkceMethod: 'S256',
      })
      .then((authenticated) => {
        if (authenticated) {
          localStorage.setItem('access_token', keycloak.token);
          localStorage.setItem('refresh_token', keycloak.refreshToken);
          resolve(keycloak);
        } else {
          reject(new Error('Not authenticated'));
        }
      })
      .catch((error) => {
        reject(error);
      });
  });
};

export const logoutKeycloak = () => {
  keycloak.logout();
  localStorage.removeItem('access_token');
  localStorage.removeItem('refresh_token');
};

export const getKeycloakToken = () => {
  return keycloak.token;
};

export const getKeycloakUser = () => {
  return keycloak.tokenParsed;
};

export default keycloak;
