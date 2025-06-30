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

const SuccessMessage = styled.div`
  color: #28a745;
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

const ValidationMessage = styled.div`
  color: #dc3545;
  font-size: 0.75rem;
  margin-top: 0.25rem;
`;

const Signup = () => {
  const [formData, setFormData] = useState({
    loginId: '',
    password: '',
    email: '',
    phone: ''
  });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);
  const [validationErrors, setValidationErrors] = useState({});
  const { signup, loading } = useAuth();
  const navigate = useNavigate();

  const validateField = (name, value) => {
    const errors = { ...validationErrors };
    
    switch (name) {
      case 'loginId':
        if (value.length < 5 || value.length > 25) {
          errors.loginId = '로그인 아이디는 5~25자 이내여야 합니다.';
        } else {
          delete errors.loginId;
        }
        break;
      case 'password':
        if (value.length < 5 || value.length > 25) {
          errors.password = '비밀번호는 5~25자 이내여야 합니다.';
        } else {
          delete errors.password;
        }
        break;
      case 'email':
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(value)) {
          errors.email = '올바른 이메일 형식이어야 합니다.';
        } else if (value.length < 6 || value.length > 25) {
          errors.email = '이메일은 6~25자 이내여야 합니다.';
        } else {
          delete errors.email;
        }
        break;
      case 'phone':
        if (value.length < 8 || value.length > 25) {
          errors.phone = '전화번호는 8~25자 이내여야 합니다.';
        } else {
          delete errors.phone;
        }
        break;
      default:
        break;
    }
    
    setValidationErrors(errors);
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value
    });
    setError('');
    validateField(name, value);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // 모든 필드 유효성 검사
    const requiredFields = ['loginId', 'password', 'email', 'phone'];
    const emptyFields = requiredFields.filter(field => !formData[field]);
    
    if (emptyFields.length > 0) {
      setError('모든 필드를 입력해주세요.');
      return;
    }
    
    if (Object.keys(validationErrors).length > 0) {
      setError('입력 형식을 확인해주세요.');
      return;
    }

    const result = await signup(formData);
    if (result.success) {
      setSuccess(true);
      setTimeout(() => {
        navigate('/login');
      }, 2000);
    } else {
      setError(result.error?.message || '회원가입에 실패했습니다.');
    }
  };

  if (success) {
    return (
      <Container>
        <FormContainer>
          <Title>회원가입 완료!</Title>
          <SuccessMessage>
            회원가입이 완료되었습니다. 로그인 페이지로 이동합니다...
          </SuccessMessage>
        </FormContainer>
      </Container>
    );
  }

  return (
    <Container>
      <FormContainer>
        <Title>회원가입</Title>
        <Form onSubmit={handleSubmit}>
          <InputGroup>
            <Label htmlFor="loginId">아이디</Label>
            <Input
              type="text"
              id="loginId"
              name="loginId"
              placeholder="5~25자 이내로 입력하세요"
              value={formData.loginId}
              onChange={handleChange}
            />
            {validationErrors.loginId && (
              <ValidationMessage>{validationErrors.loginId}</ValidationMessage>
            )}
          </InputGroup>
          
          <InputGroup>
            <Label htmlFor="password">비밀번호</Label>
            <Input
              type="password"
              id="password"
              name="password"
              placeholder="5~25자 이내로 입력하세요"
              value={formData.password}
              onChange={handleChange}
            />
            {validationErrors.password && (
              <ValidationMessage>{validationErrors.password}</ValidationMessage>
            )}
          </InputGroup>
          
          <InputGroup>
            <Label htmlFor="email">이메일</Label>
            <Input
              type="email"
              id="email"
              name="email"
              placeholder="example@email.com"
              value={formData.email}
              onChange={handleChange}
            />
            {validationErrors.email && (
              <ValidationMessage>{validationErrors.email}</ValidationMessage>
            )}
          </InputGroup>
          
          <InputGroup>
            <Label htmlFor="phone">전화번호</Label>
            <Input
              type="tel"
              id="phone"
              name="phone"
              placeholder="전화번호를 입력하세요"
              value={formData.phone}
              onChange={handleChange}
            />
            {validationErrors.phone && (
              <ValidationMessage>{validationErrors.phone}</ValidationMessage>
            )}
          </InputGroup>
          
          <Button type="submit" disabled={loading}>
            {loading ? '가입 중...' : '회원가입'}
          </Button>
          
          {error && <ErrorMessage>{error}</ErrorMessage>}
        </Form>
        
        <LinkContainer>
          이미 계정이 있으신가요? <StyledLink to="/login">로그인</StyledLink>
        </LinkContainer>
      </FormContainer>
    </Container>
  );
};

export default Signup; 