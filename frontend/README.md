# AirDND Frontend

AirDND 백엔드 API를 사용하는 React 기반 프론트엔드 애플리케이션입니다.

## 주요 기능

### ✅ 구현된 기능
- 🔐 **회원가입**: 사용자 등록 기능 (아이디, 비밀번호, 이메일, 전화번호)
- 🔑 **로그인**: JWT 토큰 기반 인증
- 💬 **실시간 채팅**: WebSocket 기반 채팅 시스템
  - 채팅방 생성 및 목록 조회
  - 실시간 메시지 송수신
  - STOMP 프로토콜을 통한 WebSocket 통신
- 📱 **반응형 디자인**: 모바일과 데스크톱 지원
- 🎨 **현대적인 UI**: Styled Components를 사용한 깔끔한 디자인

### 🚧 예정된 기능
- 🏠 **숙박 예약**: 숙박 시설 검색 및 예약
- ⭐ **리뷰 시스템**: 숙박 후기 작성 및 조회
- 📷 **이미지 업로드**: 프로필 이미지 및 숙박 사진
- 🗂️ **채팅 메시지 기록**: 과거 대화 내역 조회

## 기술 스택

- **React 18**: 사용자 인터페이스 구축
- **React Router v6**: 클라이언트 사이드 라우팅
- **Styled Components**: CSS-in-JS 스타일링
- **Axios**: HTTP 클라이언트
- **Context API**: 전역 상태 관리
- **STOMP.js**: WebSocket 통신
- **SockJS**: WebSocket 호환성 지원

## 시작하기

### 사전 요구사항
- Node.js 16.x 이상
- npm 또는 yarn
- 백엔드 서버 실행 중 (Spring Boot)

### 설치 및 실행

1. 의존성 설치:
```bash
cd frontend
npm install
```

2. 개발 서버 시작:
```bash
npm start
```

3. 브라우저에서 `http://localhost:3000` 접속

### 백엔드 연동

프로젝트는 `http://localhost:8080`에서 실행되는 백엔드 서버와 통신하도록 설정되어 있습니다.
백엔드 서버가 다른 포트에서 실행되는 경우 다음 파일들을 수정하세요:

- `package.json`의 `proxy` 설정
- `src/components/ChatRoom.js`의 WebSocket 연결 URL

## 프로젝트 구조

```
frontend/
├── public/
│   ├── index.html          # HTML 템플릿
│   └── manifest.json       # PWA 매니페스트
├── src/
│   ├── api/
│   │   ├── auth.js         # 인증 API 함수
│   │   └── chat.js         # 채팅 API 함수
│   ├── components/
│   │   ├── Header.js       # 공통 헤더 컴포넌트
│   │   ├── Login.js        # 로그인 페이지
│   │   ├── Signup.js       # 회원가입 페이지
│   │   ├── ChatRoomList.js # 채팅방 목록 페이지
│   │   └── ChatRoom.js     # 채팅방 페이지
│   ├── context/
│   │   └── AuthContext.js  # 인증 상태 관리
│   ├── App.js              # 메인 앱 컴포넌트
│   ├── index.js            # 앱 진입점
│   └── index.css           # 전역 스타일
├── package.json
└── README.md
```

## API 연동

### 인증 API
- **POST /api/auth/signup**: 회원가입
- **POST /api/auth/login**: 로그인

### 채팅 API
- **GET /api/chatrooms**: 채팅방 목록 조회
- **POST /api/chatrooms**: 채팅방 생성
- **GET /api/chatrooms/{id}**: 특정 채팅방 조회

### WebSocket 연결
- **엔드포인트**: `/ws/chat?token={JWT_TOKEN}`
- **메시지 전송**: `/app/chat.send`
- **메시지 수신**: `/topic/chatroom/{roomId}`

## 사용 방법

### 1. 회원가입/로그인
1. `/signup` 페이지에서 계정 생성
2. `/login` 페이지에서 로그인
3. 로그인 성공 시 자동으로 채팅방 목록으로 이동

### 2. 채팅 기능
1. **채팅방 생성**: "새 채팅방 만들기" 버튼 클릭
2. **채팅방 입장**: 채팅방 카드 클릭
3. **실시간 채팅**: 메시지 입력 후 엔터키 또는 전송 버튼
4. **연결 상태**: 상단에 WebSocket 연결 상태 표시

## 빌드 및 배포

### 프로덕션 빌드
```bash
npm run build
```

빌드된 파일은 `build/` 폴더에 생성됩니다.

### 환경 변수 설정
프로덕션 환경에서는 다음과 같이 환경 변수를 설정할 수 있습니다:

```bash
# .env.production
REACT_APP_API_URL=https://your-api-domain.com
REACT_APP_WS_URL=wss://your-websocket-domain.com
```

## 개발 가이드

### 새로운 컴포넌트 추가
1. `src/components/` 폴더에 새 컴포넌트 파일 생성
2. Styled Components를 사용하여 스타일 정의
3. 필요한 경우 Context API를 통해 전역 상태 접근

### API 함수 추가
1. `src/api/` 폴더에 새 API 모듈 생성
2. axios 인스턴스를 사용하여 HTTP 요청 함수 정의
3. 필요한 경우 인터셉터를 통해 토큰 관리

### WebSocket 기능 확장
1. `src/components/ChatRoom.js`에서 STOMP 클라이언트 설정 참고
2. 새로운 메시지 타입 또는 이벤트 추가
3. 백엔드의 WebSocket 설정과 동기화

## 문제 해결

### WebSocket 연결 실패
- 백엔드 서버가 실행 중인지 확인
- JWT 토큰이 유효한지 확인
- 브라우저 개발자 도구에서 WebSocket 연결 상태 확인

### CORS 오류
- 백엔드에서 CORS 설정 확인
- `package.json`의 proxy 설정 확인

## 라이센스

이 프로젝트는 MIT 라이센스 하에 있습니다. 