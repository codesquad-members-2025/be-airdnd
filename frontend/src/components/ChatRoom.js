import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Client } from '@stomp/stompjs';
import { chatAPI } from '../api/chat';
import { useAuth } from '../context/AuthContext';
import styled from 'styled-components';

const Container = styled.div`
  display: flex;
  flex-direction: column;
  height: calc(100vh - 80px);
  max-width: 800px;
  margin: 0 auto;
  background: white;
  border-radius: 12px 12px 0 0;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.1);
`;

const ChatHeader = styled.div`
  padding: 1rem 2rem;
  border-bottom: 1px solid #e9ecef;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: white;
  border-radius: 12px 12px 0 0;
`;

const RoomTitle = styled.h2`
  color: #333;
  font-size: 1.5rem;
`;

const BackButton = styled.button`
  padding: 0.5rem 1rem;
  background-color: #f8f9fa;
  color: #666;
  border: 1px solid #ddd;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  
  &:hover {
    background-color: #e9ecef;
    border-color: #adb5bd;
  }
`;

const MessagesContainer = styled.div`
  flex: 1;
  overflow-y: auto;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
`;

const MessageWrapper = styled.div`
  display: flex;
  flex-direction: column;
  ${props => props.isOwn ? 'align-items: flex-end;' : 'align-items: flex-start;'}
`;

const SenderName = styled.div`
  font-size: 0.8rem;
  color: #666;
  font-weight: 500;
  margin-bottom: 0.25rem;
  ${props => props.isOwn ? 'display: none;' : ''}
`;

const MessageBubble = styled.div`
  max-width: 70%;
  padding: 0.75rem 1rem;
  border-radius: 18px;
  word-wrap: break-word;
  
  ${props => props.isOwn ? `
    align-self: flex-end;
    background-color: #ff385c;
    color: white;
    border-bottom-right-radius: 4px;
  ` : `
    align-self: flex-start;
    background-color: #f1f3f4;
    color: #333;
    border-bottom-left-radius: 4px;
  `}
`;

const MessageInfo = styled.div`
  font-size: 0.75rem;
  color: ${props => props.isOwn ? 'rgba(255,255,255,0.8)' : '#666'};
  margin-top: 0.25rem;
  display: flex;
  justify-content: ${props => props.isOwn ? 'flex-end' : 'flex-start'};
  align-items: center;
`;

const MessageTime = styled.span`
  font-size: 0.7rem;
`;

const InputContainer = styled.div`
  padding: 1rem;
  border-top: 1px solid #e9ecef;
  background: white;
  display: flex;
  gap: 1rem;
`;

const MessageInput = styled.input`
  flex: 1;
  padding: 0.75rem 1rem;
  border: 1px solid #ddd;
  border-radius: 25px;
  font-size: 1rem;
  outline: none;
  
  &:focus {
    border-color: #ff385c;
    box-shadow: 0 0 0 2px rgba(255, 56, 92, 0.1);
  }
  
  &::placeholder {
    color: #aaa;
  }
`;

const SendButton = styled.button`
  padding: 0.75rem 1.5rem;
  background-color: #ff385c;
  color: white;
  border: none;
  border-radius: 25px;
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

const ConnectionStatus = styled.div`
  padding: 0.5rem 1rem;
  text-align: center;
  font-size: 0.875rem;
  color: ${props => props.connected ? '#28a745' : '#dc3545'};
  background-color: ${props => props.connected ? '#d4edda' : '#f8d7da'};
  border-bottom: 1px solid #e9ecef;
`;

const LoadingContainer = styled.div`
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
  color: #666;
`;

const ErrorContainer = styled.div`
  padding: 2rem;
  text-align: center;
  color: #dc3545;
`;

const ChatRoom = () => {
  const { roomId } = useParams();
  const navigate = useNavigate();
  const { token } = useAuth();
  const [roomInfo, setRoomInfo] = useState(null);
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState('');
  const [connected, setConnected] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [currentUserId, setCurrentUserId] = useState(null);
  
  const stompClientRef = useRef(null);
  const messagesEndRef = useRef(null);

  useEffect(() => {
    if (token) {
      // JWT 토큰에서 사용자 ID 추출 (실제로는 백엔드에서 사용자 정보를 가져와야 함)
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        setCurrentUserId(parseInt(payload.sub));
      } catch (e) {
        console.error('토큰 파싱 오류:', e);
      }
    }
  }, [token]);

  useEffect(() => {
    loadRoomInfo();
    connectWebSocket();
    
    return () => {
      if (stompClientRef.current && stompClientRef.current.active) {
        stompClientRef.current.deactivate();
      }
    };
  }, [roomId, token]);

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const loadRoomInfo = async () => {
    try {
      const response = await chatAPI.getChatRoom(roomId);
      if (response.success) {
        setRoomInfo(response.data);
        // 기존 채팅 메시지들을 설정
        if (response.data.messages) {
          setMessages(response.data.messages);
        }
      } else {
        setError('채팅방 정보를 불러올 수 없습니다.');
      }
    } catch (error) {
      setError('채팅방 정보를 불러올 수 없습니다.');
    } finally {
      setLoading(false);
    }
  };

  const connectWebSocket = () => {
    if (!token) return;

    const client = new Client({
      brokerURL: `ws://54.180.106.158/ws/chat?token=${token}`,
      debug: (str) => {
        console.log('STOMP Debug:', str);
      },
      onConnect: () => {
        console.log('WebSocket 연결됨');
        setConnected(true);
        
        // 채팅방 구독
        client.subscribe(`/topic/chatroom/${roomId}`, (message) => {
          const newMessage = JSON.parse(message.body);
          setMessages(prev => [...prev, newMessage]);
        });
      },
      onDisconnect: () => {
        console.log('WebSocket 연결 해제됨');
        setConnected(false);
      },
      onStompError: (frame) => {
        console.error('STOMP 오류:', frame);
        setConnected(false);
      },
    });

    client.activate();
    stompClientRef.current = client;
  };

  const sendMessage = () => {
    if (!newMessage.trim() || !connected || !stompClientRef.current) {
      return;
    }

    const messageData = {
      roomId: parseInt(roomId),
      content: newMessage.trim()
    };

    stompClientRef.current.publish({
      destination: '/app/chat.send',
      body: JSON.stringify(messageData)
    });

    setNewMessage('');
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const formatTime = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleTimeString('ko-KR', {
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  if (loading) {
    return (
      <Container>
        <LoadingContainer>
          채팅방을 불러오는 중...
        </LoadingContainer>
      </Container>
    );
  }

  if (error) {
    return (
      <Container>
        <ErrorContainer>
          <p>{error}</p>
          <BackButton onClick={() => navigate('/chatrooms')}>
            채팅방 목록으로 돌아가기
          </BackButton>
        </ErrorContainer>
      </Container>
    );
  }

  return (
    <Container>
      <ChatHeader>
        <RoomTitle>{roomInfo?.name || '채팅방'}</RoomTitle>
        <BackButton onClick={() => navigate('/chatrooms')}>
          목록으로
        </BackButton>
      </ChatHeader>

      <ConnectionStatus connected={connected}>
        {connected ? '🟢 연결됨' : '🔴 연결 끊김'}
      </ConnectionStatus>

      <MessagesContainer>
        {messages.map((message, index) => {
          const isOwn = message.senderId === currentUserId;
          
          return (
            <MessageWrapper key={index} isOwn={isOwn}>
              {!isOwn && (
                <SenderName isOwn={isOwn}>
                  {message.senderName}
                </SenderName>
              )}
              
              <MessageBubble isOwn={isOwn}>
                {message.content}
              </MessageBubble>
              
              <MessageInfo isOwn={isOwn}>
                <MessageTime>{formatTime(message.createdAt)}</MessageTime>
              </MessageInfo>
            </MessageWrapper>
          );
        })}
        <div ref={messagesEndRef} />
      </MessagesContainer>

      <InputContainer>
        <MessageInput
          type="text"
          placeholder="메시지를 입력하세요..."
          value={newMessage}
          onChange={(e) => setNewMessage(e.target.value)}
          onKeyPress={handleKeyPress}
          disabled={!connected}
        />
        <SendButton 
          onClick={sendMessage}
          disabled={!connected || !newMessage.trim()}
        >
          전송
        </SendButton>
      </InputContainer>
    </Container>
  );
};

export default ChatRoom; 