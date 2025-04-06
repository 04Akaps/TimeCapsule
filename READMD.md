# 타임캡슐 API

![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![MySQL](https://img.shields.io/badge/mysql-%234479A1.svg?style=for-the-badge&logo=mysql&logoColor=white)
![Ktor](https://img.shields.io/badge/ktor-%23E34F26.svg?style=for-the-badge&logo=ktor&logoColor=white)
![MinIO](https://img.shields.io/badge/MinIO-%23C72E49.svg?style=for-the-badge&logo=minio&logoColor=white)
![PASETO](https://img.shields.io/badge/PASETO-%232B3784.svg?style=for-the-badge&logoColor=white)
![PBKDF2](https://img.shields.io/badge/PBKDF2-%23000000.svg?style=for-the-badge&logoColor=white)
![AES-GCM](https://img.shields.io/badge/AES--GCM-%234285F4.svg?style=for-the-badge&logoColor=white)

Kotlin과 Ktor를 사용하여 구축된 백엔드 API 서비스로, 사용자가 다양한 콘텐츠 유형을 포함한 디지털 타임캡슐을 생성하고 특정 날짜에 열릴 수 있도록 예약할 수 있습니다.

## 📋 개요

이 서비스는 사용자에게 다음과 같은 기능을 제공합니다:
- 디지털 타임캡슐 생성 및 관리
- 다양한 유형의 콘텐츠(텍스트, 이미지, 비디오, 오디오, 파일) 추가
- 미래 특정 날짜에 "오픈"되도록 타임캡슐 예약
- 타임캡슐이 열릴 준비가 되었을 때 알림 수신

## 🛠️ 기술 스택

- **언어**: Kotlin 2.0.21
- **프레임워크**: Ktor 2.3.7
- **데이터베이스**: MySQL 8.0.33
- **ORM**: Jetbrains Exposed 0.45.0
- **커넥션 풀**: HikariCP 5.0.1
- **의존성 주입**: Koin 3.5.0
- **인증**: PASETO 토큰을 사용한 커스텀 인증
- **스토리지**: MinIO (S3 호환성 제공)
- **직렬화**: Jackson

## 🔧 필수 조건

- JDK 17 이상
- MySQL 8.0+
- MinIO 서버 (또는 S3 호환 스토리지)
- Gradle 7.0+ (또는 포함된 Gradle 래퍼 사용)

## 🚀 설정 및 설치

1. **데이터베이스 구성**
    - `app` 이라는 이름의 MySQL 데이터베이스 생성
    - 스키마 생성 스크립트 실행:
   ```sql
   CREATE TABLE users (
     id VARCHAR(100) PRIMARY KEY,
     email VARCHAR(100) UNIQUE NOT NULL,
     password_hash VARCHAR(255) NOT NULL,
     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
     last_login TIMESTAMP,
     is_active BOOLEAN DEFAULT TRUE
   );

   CREATE TABLE user_token_mapper(
     user_id VARCHAR(100) primary key,
     token VARCHAR(500)
   );

   CREATE TABLE time_capsules (
     id VARCHAR(100) PRIMARY KEY,
     creator_id VARCHAR(100) NOT NULL,
     title VARCHAR(100) NOT NULL,
     description TEXT,
     creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
     scheduled_open_date TIMESTAMP NOT NULL,
     status ENUM('sealed', 'opened') DEFAULT 'sealed',
     location_lat DECIMAL(10, 8),
     location_lng DECIMAL(11, 8)
   );

   CREATE TABLE capsule_contents (
     id VARCHAR(100) PRIMARY KEY,
     capsule_id VARCHAR(100) NOT NULL,
     content_type ENUM('text', 'image', 'video', 'audio', 'file') NOT NULL,
     content TEXT,
     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   );

   CREATE TABLE recipients (
     id VARCHAR(100) PRIMARY KEY,
     capsule_id VARCHAR(100) NOT NULL,
     user_id VARCHAR(100),
     has_viewed BOOLEAN DEFAULT FALSE,
     notification_sent BOOLEAN DEFAULT FALSE
   );
   ```

3. **애플리케이션 구성**
    - `application.yaml`을 특정 설정으로 수정:
   ```yaml
   ktor:
     application:
       modules:
         - com.example.MainKt.module
     deployment:
       port: 9090
   
   database:
     driverClassName: "com.mysql.cj.jdbc.Driver"
     jdbcURL: "jdbc:mysql://localhost:3306/app?useUnicode=true&characterEncoding=utf8&useSSL=false"
     username: "root"
     password: ""
     maximumPoolSize: 10
   
   security:
     password:
       pbkdf2:
         algorithm: "PBKDF2WithHmacSHA256"
         iterations: "120000"
         keyLength: "256"
         saltLength: "16"
     paseto:
       issuer: "time-capsule-alpha"
       privateKey: "your-private-key"
       publicKey: "your-public-key"
   
   storage:
     type: "minio"  # "minio" 또는 "s3"
     minio:
       endpoint: "http://localhost:9000"
       accessKey: "minioadmin"
       secretKey: "minioadmin"
     s3:
       endpoint: "https://s3.amazonaws.com"
       region: "us-east-1"
       accessKey: "access"
       secretKey: "secret"
   ```
   
## 📁 프로젝트 구조

```
src/
├── main/
│   ├── kotlin/
│   │   └── com/
│   │       └── example/
│   │           ├── Main.kt              # 애플리케이션 진입점
│   │           ├── common/              # 공통 common
│   │           ├── plugins/             # custom plugin
│   │           ├── repository/          # MySQL
│   │           ├── routes/              # router
│   │           └── security/            # 암호
│   │           └── types/               # 공통 타입
│   └── resources/
│       └── application.yaml             # 설정 파일
```

## 🔐 보안

- 사용자 비밀번호는 SHA-256을 사용한 PBKDF2로 해싱됨
- 인증은 PASETO 토큰을 통해 처리
- 모든 API 엔드포인트(등록 및 로그인 제외)는 인증 필요

## 📦 스토리지

이 애플리케이션은 두 가지 스토리지 백엔드를 지원합니다:
1. **MinIO** - 자체 호스팅 배포용
2. **Amazon S3** - 클라우드 배포용

둘 다 `application.yaml` 파일에서 구성됩니다.

## 🗄️ 데이터베이스 스키마

### Users 테이블
사용자 계정 정보를 저장합니다.

### User Token Mapper 테이블
사용자를 인증 토큰에 매핑합니다.

### Time Capsules 테이블
타임캡슐에 관한 메타데이터를 저장합니다. 예약된 오픈 날짜 및 위치 좌표를 포함합니다.

### Capsule Contents 테이블
타임캡슐의 실제 내용을 다양한 콘텐츠 유형(텍스트, 이미지, 비디오, 오디오, 파일)과 함께 저장합니다.

### Recipients 테이블
공유된 타임캡슐의 수신자와 그들의 상호작용 상태를 추적합니다.