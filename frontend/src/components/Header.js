import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import styled from 'styled-components';

const HeaderContainer = styled.header`
  background-color: #ffffff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  padding: 1rem 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
`;

const Logo = styled(Link)`
  font-size: 1.5rem;
  font-weight: bold;
  color: #ff385c;
  text-decoration: none;
  
  &:hover {
    color: #e31c5f;
  }
`;

const NavButtons = styled.div`
  display: flex;
  gap: 1rem;
  align-items: center;
`;

const Button = styled.button`
  padding: 0.5rem 1rem;
  border: 1px solid #ddd;
  border-radius: 8px;
  background-color: white;
  color: #333;
  cursor: pointer;
  text-decoration: none;
  font-size: 0.9rem;
  transition: all 0.2s;
  
  &:hover {
    background-color: #f8f9fa;
    border-color: #adb5bd;
  }
`;

const LogoutButton = styled(Button)`
  background-color: #ff385c;
  color: white;
  border-color: #ff385c;
  
  &:hover {
    background-color: #e31c5f;
    border-color: #e31c5f;
  }
`;

const StyledLink = styled(Link)`
  padding: 0.5rem 1rem;
  border: 1px solid #ddd;
  border-radius: 8px;
  background-color: white;
  color: #333;
  cursor: pointer;
  text-decoration: none;
  font-size: 0.9rem;
  transition: all 0.2s;
  
  &:hover {
    background-color: #f8f9fa;
    border-color: #adb5bd;
  }
`;

const Header = () => {
  const { isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <HeaderContainer>
      <Logo to="/">AirDND</Logo>
      <NavButtons>
        {isAuthenticated ? (
          <LogoutButton onClick={handleLogout}>
            로그아웃
          </LogoutButton>
        ) : (
          <>
            <StyledLink to="/login">로그인</StyledLink>
            <StyledLink to="/signup">회원가입</StyledLink>
          </>
        )}
      </NavButtons>
    </HeaderContainer>
  );
};

export default Header; 