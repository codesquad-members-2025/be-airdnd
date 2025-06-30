import React, { createContext, useContext, useState, useEffect } from 'react';
import { authAPI } from '../api/auth';

const AuthContext = createContext();

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export const AuthProvider = ({ children }) => {
  const [token, setToken] = useState(localStorage.getItem('token'));
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (token) {
      localStorage.setItem('token', token);
    } else {
      localStorage.removeItem('token');
    }
  }, [token]);

  const login = async (loginData) => {
    setLoading(true);
    try {
      const response = await authAPI.login(loginData);
      if (response.success) {
        setToken(response.data);
        return { success: true };
      } else {
        return { success: false, error: response.error };
      }
    } catch (error) {
      return { 
        success: false, 
        error: { 
          message: '로그인 중 오류가 발생했습니다.', 
          code: 'LOGIN_ERROR' 
        } 
      };
    } finally {
      setLoading(false);
    }
  };

  const signup = async (userData) => {
    setLoading(true);
    try {
      const response = await authAPI.signup(userData);
      if (response.success) {
        return { success: true };
      } else {
        return { success: false, error: response.error };
      }
    } catch (error) {
      return { 
        success: false, 
        error: { 
          message: '회원가입 중 오류가 발생했습니다.', 
          code: 'SIGNUP_ERROR' 
        } 
      };
    } finally {
      setLoading(false);
    }
  };

  const logout = () => {
    setToken(null);
  };

  const value = {
    token,
    loading,
    login,
    signup,
    logout,
    isAuthenticated: !!token
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}; 