import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import Login from './components/Login';
import Signup from './components/Signup';
import ChatRoomList from './components/ChatRoomList';
import ChatRoom from './components/ChatRoom';
import Header from './components/Header';
import styled from 'styled-components';

const AppContainer = styled.div`
  min-height: 100vh;
  background-color: #f8f9fa;
`;

const ProtectedRoute = ({ children }) => {
  const { token } = useAuth();
  return token ? children : <Navigate to="/login" />;
};

const PublicRoute = ({ children }) => {
  const { token } = useAuth();
  return !token ? children : <Navigate to="/chatrooms" />;
};

function App() {
  return (
    <AuthProvider>
      <Router>
        <AppContainer>
          <Header />
          <Routes>
            <Route 
              path="/login" 
              element={
                <PublicRoute>
                  <Login />
                </PublicRoute>
              } 
            />
            <Route 
              path="/signup" 
              element={
                <PublicRoute>
                  <Signup />
                </PublicRoute>
              } 
            />
            <Route 
              path="/chatrooms" 
              element={
                <ProtectedRoute>
                  <ChatRoomList />
                </ProtectedRoute>
              } 
            />
            <Route 
              path="/chatrooms/:roomId" 
              element={
                <ProtectedRoute>
                  <ChatRoom />
                </ProtectedRoute>
              } 
            />
            <Route 
              path="/" 
              element={<Navigate to="/chatrooms" />}
            />
          </Routes>
        </AppContainer>
      </Router>
    </AuthProvider>
  );
}

export default App; 