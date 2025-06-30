import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { chatAPI } from '../api/chat';
import styled from 'styled-components';

const Container = styled.div`
  padding: 2rem;
  max-width: 800px;
  margin: 0 auto;
`;

const Header = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
`;

const Title = styled.h1`
  color: #333;
  font-size: 2rem;
`;

const CreateButton = styled.button`
  padding: 0.75rem 1.5rem;
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

const ChatRoomGrid = styled.div`
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
`;

const ChatRoomCard = styled.div`
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  padding: 1.5rem;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 25px rgba(0, 0, 0, 0.15);
  }
`;

const RoomName = styled.h3`
  color: #333;
  margin-bottom: 0.5rem;
  font-size: 1.2rem;
`;

const RoomInfo = styled.p`
  color: #666;
  font-size: 0.9rem;
`;

const EmptyState = styled.div`
  text-align: center;
  padding: 4rem 2rem;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
`;

const EmptyTitle = styled.h3`
  color: #666;
  margin-bottom: 1rem;
  font-size: 1.5rem;
`;

const EmptyDescription = styled.p`
  color: #888;
  margin-bottom: 2rem;
`;

const Modal = styled.div`
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
`;

const ModalContent = styled.div`
  background: white;
  border-radius: 12px;
  padding: 2rem;
  width: 90%;
  max-width: 400px;
`;

const ModalTitle = styled.h2`
  margin-bottom: 1.5rem;
  color: #333;
`;

const Input = styled.input`
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 1rem;
  margin-bottom: 1.5rem;
  
  &:focus {
    outline: none;
    border-color: #ff385c;
    box-shadow: 0 0 0 2px rgba(255, 56, 92, 0.1);
  }
`;

const ModalButtons = styled.div`
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
`;

const Button = styled.button`
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.2s;
`;

const CancelButton = styled(Button)`
  background-color: #f8f9fa;
  color: #666;
  
  &:hover {
    background-color: #e9ecef;
  }
`;

const ConfirmButton = styled(Button)`
  background-color: #ff385c;
  color: white;
  
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
  margin-bottom: 1rem;
  text-align: center;
`;

const LoadingSpinner = styled.div`
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 4rem;
  color: #666;
`;

const ChatRoomList = () => {
  const [chatRooms, setChatRooms] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [newRoomName, setNewRoomName] = useState('');
  const [error, setError] = useState('');
  const [creating, setCreating] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    loadChatRooms();
  }, []);

  const loadChatRooms = async () => {
    try {
      const response = await chatAPI.getChatRooms();
      if (response.success) {
        setChatRooms(response.data || []);
      } else {
        setError('채팅방 목록을 불러오는데 실패했습니다.');
      }
    } catch (error) {
      setError('채팅방 목록을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleCreateRoom = async () => {
    if (!newRoomName.trim()) {
      setError('채팅방 이름을 입력해주세요.');
      return;
    }

    setCreating(true);
    setError('');

    try {
      const response = await chatAPI.createChatRoom(newRoomName.trim());
      if (response.success) {
        setShowModal(false);
        setNewRoomName('');
        await loadChatRooms(); // 목록 새로고침
      } else {
        setError(response.error?.message || '채팅방 생성에 실패했습니다.');
      }
    } catch (error) {
      setError('채팅방 생성에 실패했습니다.');
    } finally {
      setCreating(false);
    }
  };

  const handleRoomClick = (roomId) => {
    navigate(`/chatrooms/${roomId}`);
  };

  const handleModalClose = () => {
    setShowModal(false);
    setNewRoomName('');
    setError('');
  };

  const formatDate = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  if (loading) {
    return (
      <Container>
        <LoadingSpinner>
          채팅방 목록을 불러오는 중...
        </LoadingSpinner>
      </Container>
    );
  }

  return (
    <Container>
      <Header>
        <Title>채팅방</Title>
        <CreateButton onClick={() => setShowModal(true)}>
          새 채팅방 만들기
        </CreateButton>
      </Header>

      {chatRooms.length === 0 ? (
        <EmptyState>
          <EmptyTitle>채팅방이 없습니다</EmptyTitle>
          <EmptyDescription>
            새로운 채팅방을 만들어 대화를 시작해보세요!
          </EmptyDescription>
          <CreateButton onClick={() => setShowModal(true)}>
            첫 번째 채팅방 만들기
          </CreateButton>
        </EmptyState>
      ) : (
        <ChatRoomGrid>
          {chatRooms.map((room) => (
            <ChatRoomCard 
              key={room.id} 
              onClick={() => handleRoomClick(room.id)}
            >
              <RoomName>{room.name}</RoomName>
              <RoomInfo>
                생성일: {formatDate(room.createdAt)}
              </RoomInfo>
            </ChatRoomCard>
          ))}
        </ChatRoomGrid>
      )}

      {showModal && (
        <Modal onClick={handleModalClose}>
          <ModalContent onClick={(e) => e.stopPropagation()}>
            <ModalTitle>새 채팅방 만들기</ModalTitle>
            <Input
              type="text"
              placeholder="채팅방 이름을 입력하세요"
              value={newRoomName}
              onChange={(e) => setNewRoomName(e.target.value)}
              onKeyPress={(e) => e.key === 'Enter' && handleCreateRoom()}
              autoFocus
            />
            {error && <ErrorMessage>{error}</ErrorMessage>}
            <ModalButtons>
              <CancelButton onClick={handleModalClose}>
                취소
              </CancelButton>
              <ConfirmButton 
                onClick={handleCreateRoom}
                disabled={creating}
              >
                {creating ? '생성 중...' : '생성'}
              </ConfirmButton>
            </ModalButtons>
          </ModalContent>
        </Modal>
      )}
    </Container>
  );
};

export default ChatRoomList; 