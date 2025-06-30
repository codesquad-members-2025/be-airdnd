import axios from 'axios';

const API_BASE_URL = 'http://54.180.106.158';

// axios 인스턴스 생성
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 요청 인터셉터
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 응답 인터셉터
api.interceptors.response.use(
  (response) => {
    return response.data;
  },
  (error) => {
    if (error.response?.data) {
      return Promise.resolve(error.response.data);
    }
    return Promise.reject(error);
  }
);

export const authAPI = {
  // 로그인
  login: async (loginData) => {
    return await api.post('/api/auth/login', loginData);
  },

  // 회원가입
  signup: async (userData) => {
    return await api.post('/api/auth/signup', userData);
  }
}; 