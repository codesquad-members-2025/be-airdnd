import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import styled from 'styled-components';

const Container = styled.div`
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 80px);
  padding: 2rem;
`;

const FormContainer = styled.div`
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  padding: 3rem;
  width: 100%;
  max-width: 400px;
`;

const Title = styled.h1`
  text-align: center;
  margin-bottom: 2rem;
  color: #333;
  font-size: 2rem;
`;

const Form = styled.form`
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
`;

const InputGroup = styled.div`
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
`;

const Label = styled.label`
  font-weight: 500;
  color: #333;
  font-size: 0.9rem;
`;

const Input = styled.input`
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.2s;
  
  &:focus {
    outline: none;
    border-color: #ff385c;
    box-shadow: 0 0 0 2px rgba(255, 56, 92, 0.1);
  }
  
  &::placeholder {
    color: #aaa;
  }
`;

const Button = styled.button`
  padding: 0.75rem;
  background-color: #ff385c;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
  
  &:hover {
    background-color: #e31c5f;
  }
  
  &:disabled {
    background-color: #ccc;
    cursor: not-allowed;
  }
`;

const ErrorMessage = styled.div`
  color: #dc3545;
  font-size: 0.875rem;
  text-align: center;
  margin-top: 1rem;
`;

const LinkContainer = styled.div`
  text-align: center;
  margin-top: 1.5rem;
  color: #666;
`;

const StyledLink = styled(Link)`
  color: #ff385c;
  text-decoration: none;
  font-weight: 500;
  
  &:hover {
    text-decoration: underline;
  }
`;

const Login = () => {
  const [formData, setFormData] = useState({
    loginId: '',
    password: ''
  });
  const [error, setError] = useState('');
  const { login, loading } = useAuth();
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
    setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!formData.loginId || !formData.password) {
      setError('모든 필드를 입력해주세요.');
      return;
    }

    const result = await login(formData);
    if (result.success) {
      navigate('/chatrooms');
    } else {
      setError(result.error?.message || '로그인에 실패했습니다.');
    }
  };

  return (
    <Container>
      <FormContainer>
        <Title>로그인</Title>
        <Form onSubmit={handleSubmit}>
          <InputGroup>
            <Label htmlFor="loginId">아이디</Label>
            <Input
              type="text"
              id="loginId"
              name="loginId"
              placeholder="아이디를 입력하세요"
              value={formData.loginId}
              onChange={handleChange}
            />
          </InputGroup>
          
          <InputGroup>
            <Label htmlFor="password">비밀번호</Label>
            <Input
              type="password"
              id="password"
              name="password"
              placeholder="비밀번호를 입력하세요"
              value={formData.password}
              onChange={handleChange}
            />
          </InputGroup>
          
          <Button type="submit" disabled={loading}>
            {loading ? '로그인 중...' : '로그인'}
          </Button>
          
          {error && <ErrorMessage>{error}</ErrorMessage>}
        </Form>
        
        <LinkContainer>
          계정이 없으신가요? <StyledLink to="/signup">회원가입</StyledLink>
        </LinkContainer>
      </FormContainer>
    </Container>
  );
};

export default Login; 