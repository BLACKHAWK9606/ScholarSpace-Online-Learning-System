import axios from 'axios';

const API_URL = 'http://localhost:9090/auth/';

const instance = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  }
});

// Add interceptor to include token in all requests
instance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

class AuthService {
  async login(email, password) {
    try {
      const response = await instance.post('login', { email, password });
      
      if (response.data.token) {
        localStorage.setItem('user', JSON.stringify(response.data));
        localStorage.setItem('token', response.data.token);
      }
      return response.data;
    } catch (error) {
      console.error('Login error', error.response ? error.response.data : error.message);
      throw error;
    }
  }

  async register(name, email, password, role = 'STUDENT') {
    try {
      const response = await instance.post('register', {
        name,
        email,
        password,
        role
      });
      return response.data;
    } catch (error) {
      console.error('Registration error', error.response ? error.response.data : error.message);
      throw error;
    }
  }

  logout() {
    localStorage.removeItem('user');
    localStorage.removeItem('token');
  }

  getCurrentUser() {
    return JSON.parse(localStorage.getItem('user'));
  }

  getToken() {
    return localStorage.getItem('token');
  }

  isAuthenticated() {
    return !!this.getToken();
  }

  async sendPasswordResetEmail(email) {
    try {
      const response = await instance.post('forgot-password', { email });
      return response.data;
    } catch (error) {
      console.error('Send password reset email error', error.response ? error.response.data : error.message);
      throw error;
    }
  }

  async resetPassword(token, newPassword) {
    try {
      const response = await instance.post('reset-password', { token, newPassword });
      return response.data;
    } catch (error) {
      console.error('Reset password error', error.response ? error.response.data : error.message);
      throw error;
    }
  }
}

// eslint-disable-next-line import/no-anonymous-default-export
export default new AuthService();