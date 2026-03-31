# 설문조사 (Java + React, 아임웹 연동용)

Java(Spring Boot) 백엔드와 React 프론트엔드로 구성된 설문조사 페이지입니다.  
배포 후 **아임웹** 사이트에 iframe 또는 외부 페이지로 연동할 수 있습니다.

## 프로젝트 구조

- **backend/** – Spring Boot 3, JPA, H2 DB, REST API
- **frontend/** – React 18, Vite, TypeScript

## 기능

- 설문 목록 조회 및 설문 상세(문항) 조회
- 문항 유형: 단일 선택, 복수 선택, 단답, 장문
- 설문 제출 API (아임웹 회원 ID 등 식별자 선택 저장 가능)
- CORS 설정으로 외부 도메인(아임웹)에서 API 호출 가능

## 로컬 실행

### 1. 백엔드 (Java 17+, Maven 3.6+)

```bash
cd backend
mvn spring-boot:run
```

- API: http://localhost:8080  
- H2 콘솔: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:file:./data/survey`)

### 2. 프론트엔드

```bash
cd frontend
npm install
npm run dev
```

- 화면: http://localhost:3000  
- 개발 서버가 `/api`를 8080으로 프록시하므로 별도 CORS 설정 없이 동작합니다.

첫 실행 시 **서비스 만족도 설문** 샘플 1개가 자동 생성됩니다.

---

## 무료 배포(운영) 가이드

이 프로젝트는 **프론트(React/Vite)** 와 **백엔드(Spring Boot API)** 가 분리되어 있으므로, 무료로 운영하려면 보통 아래처럼 배포합니다.

- **프론트(정적)**: GitHub Pages (무료)
- **백엔드(API)**: Render 같은 무료 컨테이너 호스팅 (무료 플랜은 “슬립/콜드스타트”가 있을 수 있음)

### 1) 백엔드 먼저 배포 (예: Render)

1. GitHub에 이 레포를 푸시합니다.
2. Render에서 이 레포를 연결합니다.
   - 가장 쉬운 방법: 레포 루트의 `render.yaml`을 사용(“Blueprint”/IaC 방식)
   - 수동으로 할 경우: “New Web Service” → GitHub 레포 연결 → Root Directory를 `backend`로 설정
3. 환경 변수 설정(필수)
   - `IMWEB_API_KEY`
   - `IMWEB_API_SECRET`

백엔드가 배포되면 Render가 API URL을 줍니다. 예: `https://your-api.onrender.com`

> 주의: 현재 DB는 H2(file)라서 무료 호스팅에서는 재시작/재배포 시 데이터가 사라질 수 있습니다.
> 완전 운영(데이터 영구 보존)이 필요하면 PostgreSQL 같은 외부 DB로 바꾸는 것을 권장합니다.

### 2) 프론트 GitHub Pages 배포

이 레포에는 GitHub Pages 자동 배포 워크플로우가 포함되어 있습니다: `.github/workflows/deploy-frontend-pages.yml`

1. GitHub 레포 Settings → Pages
   - Build and deployment: **GitHub Actions**
2. GitHub 레포 Settings → Secrets and variables → Actions → **Variables** 에 아래를 추가
   - `VITE_API_URL` = (위에서 받은 백엔드 주소) 예: `https://your-api.onrender.com`
   - (선택) `VITE_IMWEB_ADMIN_MEMBER_ID` = 관리자용 아임웹 member id(테스트용)
3. `main` 브랜치에 push 하면 자동으로 Pages에 배포됩니다.

배포된 프론트 주소 예: `https://<github-아이디>.github.io/<repo명>/`

### 3) 아임웹 “앱 등록”에 넣는 값(일반적)

- **서비스 URL**: 배포된 프론트 주소 (예: `https://<github-아이디>.github.io/<repo명>/`)
- **리다이렉트 URI**: (OAuth 흐름을 실제로 쓸 때) `https://<your-api-domain>/api/imweb/oauth/callback`

현재 백엔드는 OAuth 콜백 엔드포인트가 구현되어 있지 않으므로, OAuth가 꼭 필요하면 콜백 API를 추가 구현해야 합니다.

## 아임웹 연동 방법

설문 페이지를 아임웹에 넣는 대표적인 방법은 두 가지입니다.

### 방법 1: iframe으로 삽입 (가장 간단)

1. **설문 프로젝트 배포**  
   - 백엔드: 서버 또는 클라우드에 Spring Boot 배포 (예: your-api.com)  
   - 프론트엔드: `npm run build` 후 정적 호스팅(Netlify, Vercel, S3 등)에 배포 (예: survey.yourdomain.com)

2. **프론트엔드 API 주소 설정**  
   - 배포된 백엔드 주소를 사용하도록 환경 변수 설정 후 재빌드  
   - 예: `VITE_API_URL=https://your-api.com`  
   - 빌드: `npm run build`

3. **아임웹 페이지에 iframe 추가**  
   - 아임웹 관리자 → 페이지 편집 → **블록 추가** → **맞춤 HTML** 또는 **위젯/임베드**  
   - 아래처럼 iframe 코드 삽입 (실제 설문 페이지 URL로 교체):

```html
<iframe
  src="https://survey.yourdomain.com"
  width="100%"
  height="800"
  frameborder="0"
  title="설문조사"
></iframe>
```

- 모바일에서는 `height`를 더 크게 하거나 `min-height`로 조정해 보시면 됩니다.

### 방법 2: 아임웹 API 연동 (데이터 연동이 필요할 때)

- 아임웹 **개발자 센터**: https://developers.imweb.me/  
- **환경설정 > 외부 서비스 연동(API)** 에서 API Key/Secret 발급  
- 설문 제출 시 `respondentId`에 아임웹 회원 ID 등을 넣어 두면, 나중에 아임웹 쪽 데이터와 매칭할 수 있습니다.

### CORS (백엔드 배포 시)

- 아임웹 도메인에서 API를 직접 호출할 경우, 백엔드 `application.yml`에 아임웹 사이트 도메인을 허용해 두세요.

```yaml
cors:
  allowed-origins: "https://your-imweb-site.imweb.me,https://yourdomain.com"
```

- `*`로 두면 모든 도메인 허용이지만, 보안상 실제 운영 시에는 필요한 도메인만 적는 것을 권장합니다.

---

## API 요약

| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | /api/surveys | 활성 설문 목록 |
| GET | /api/surveys/{id} | 설문 상세 + 문항 |
| POST | /api/surveys/submit | 설문 제출 (JSON body) |

제출 body 예시:

```json
{
  "surveyId": 1,
  "respondentId": "optional-imweb-member-id",
  "answers": [
    { "questionId": 1, "value": "매우 만족" },
    { "questionId": 2, "value": "개선할 점 텍스트" }
  ]
}
```

---

## 라이선스

MIT
