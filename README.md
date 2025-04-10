# NBE4-5-2-Team10
프로그래머스 백엔드 데브코스 4기 5회차 10팀 TenTen의 3차 팀 프로젝트입니다.</br>
3차 팀 프로젝트에서는 기존에 Java와 Spring Boot로 개발했던 2차 프로젝트를 Kotlin으로 마이그레이션하고, 성능 최적화와 기능 고도화를 진행했습니다.

<br/>
<br/>

## TenTen

|                                           김경래                                           |                                          김채은                                           |                                                        박현모                                                        |                                          신윤호                                           |                                                        정회찬                                                        
|:---------------------------------------------------------------------------------------:|:--------------------------------------------------------------------------------------:|:-----------------------------------------------------------------------------------------------------------------:|:--------------------------------------------------------------------------------------:|:-----------------------------------------------------------------------------------------------------------------:|
| <img src="https://avatars.githubusercontent.com/u/15260002?v=4" alt="김경래" width="150"> | <img src="https://avatars.githubusercontent.com/u/160405935?v=4" alt="김채은" width="150"> | <img src="https://avatars.githubusercontent.com/u/39055629?v=4" alt="박현모" width="150"> | <img src="https://avatars.githubusercontent.com/u/115200471?v=4" alt="신윤호" width="150"> | <img src="https://avatars.githubusercontent.com/u/154239980?v=4" alt="정회찬" width="150"> |
|                                         PM/TL                                         |                                           TM                                          |                                                        TM                                                         |                                           TM                                          |                                                       TM                                                         |
|                          [GitHub](https://github.com/godaos)                          |                         [GitHub](https://github.com/huipadyam)                          |                                        [GitHub](https://github.com/Emokido)                                        |                         [GitHub](https://github.com/messiteacher)                          |                                                    [GitHub](https://github.com/hoechanj)                                                     |

<br/>
<br/>

# ☕ Project Overview

## 1. 프로젝트 명
### 여행친구 - TripFriend
<br/>

## 2. 프로젝트 소개
- 여행 일정 기반 동행 매칭 시스템
  - 사용자가 여행 일정(날짜, 장소, 관심사)을 등록, 비슷한 일정을 가진 여행자와 동행 가능
  - 사용자는 원하는 일정만 동행 신청 가능
- 일정 변경 & 조율 가능
    - 동행이 확정된 후에 일정 조율을 통해 일정을 수정 가능
    - 변경 된 일정을 쉽게 공유
- 여행지 추천 & 후기 공유
    - 사용자가 방문한 장소에 대한 후기를 남기게 하여, 해당 여행지에 대한 정보를 제공한다.
    - 후기에 ‘평점’을 남기는 방식으로 인기 여행지에 대한 정보를 수집하고 인기 여행지는 상위 게시글로 올라가게 된다.
    - 해당 후기를 통해 여행지를 정할 때 도움이 되게 한다.
 

## 3. 작업 및 역할 분담
   |     |                                                                                         |                                                                                                  |
   |-----|-----------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------|
   | 김경래 | <img src="https://avatars.githubusercontent.com/u/15260002?v=4" alt="김경래" width="100"> | <ul><li>프로젝트 관리 및 문서화</li><li>팀 리딩 및 커뮤니케이션</li><li>여행지 관리</li><li>여행 일정 관리</li></ul> |
   | 김채은 | <img src="https://avatars.githubusercontent.com/u/160405935?v=4" alt="김채은" width="100">  | <ul><li>동행 게시판</li><li>동행 검색 및 정렬</li><li>동행 모집</li><li>동행 신청</li></ul>|
   | 박현모 | <img src="https://avatars.githubusercontent.com/u/39055629?v=4" alt="박현모" width="100"> | <ul><li>여행 후기 게시판</li><li>후기 게시판 댓글 관리</li><li>평점/조회수 기능</li></ul>|
   | 신윤호 | <img src="https://avatars.githubusercontent.com/u/115200471?v=4" alt="신윤호" width="100">  | <ul><li>회원관리</li><li>JWT/시큐리티</li><li>OAuth2.0</li><li>AWS 배포</li></ul>|
   | 정회찬 | <img src="https://avatars.githubusercontent.com/u/154239980?v=4" alt="정회찬" width="100">  | <ul><li>관리자 페이지</li><li>관리자 공지&이벤트 관리</li><li>이벤트 관리</li></ul> |

<br/>
<br/>

# 🛠️ Tech
## 기술 스택
### 언어
- JAVA 17
- TypeScript
### 프레임워크 및 라이브러리
- Spring 3.4.2
- Spring Security
- React 19.0.0
- Next.js 15.2.0
### 데이터베이스
- MySQL
- H2
- Redis
### IED 및 개발 도구
- IntelliJ IDEA
- Visual Studio Code
- AWS EC2
- AWS RDS
- VERCEL
### 버전 관리 및 협업 도구
- Git
- GitHub
- Slack
- Notion
- FigJam

## ERD
![여행동행리뉴얼](https://github.com/user-attachments/assets/ae2605d0-339b-4461-b22d-ccefa664c3c2)

## System Architeture
<img width="912" alt="시스템아키텍쳐" src="https://github.com/user-attachments/assets/8dcd3d40-1efa-4443-a990-07a506e7db0e" />

## 브랜치 전략
**GitHub Flow** 전략 사용
- **Main Branch**
  - 배포 가능한 상태의 코드를 유지
  - 모든 배포는 해당 브랜치에서 이루어집니다.
- **develop Branch**
  - 개발이 진행되는 브랜치입니다.
  - 기능 개발 후 해당 브랜치로 병합됩니다.
  - 모든 변경 사항은 develop에서 분기하여 개발/병합됩니다.
- **{name} / {목적}-{이슈번호} Branch**
  - 팀원 각자의 개발 브랜치입니다.
  - 닉네임과 목적 이슈번호를 조합하여 브랜치 명을 작성합니다.
  - 모든 기능 개발은 해당 브랜치에서 이루어집니다.
- 테스트가 완료되면, Pull Request를 생성하여 Review를 요청합니다. 이 때 타겟은 ```develop``` 브랜치입니다.
- Review가 완료되고, 피드백이 모두 반영돠면 해당 ```feature```브랜치를 ```develop```브랜치로 **Merge**합니다.
- ```develop```브랜치의 내용이 정상적으로 동작하면 팀원들과 상의 후 ```main```브랜치로 **Merge**합니다.

## 컨벤션
[Commit Convention](https://github.com/prgrms-be-devcourse/NBE4-5-2-Team10/wiki/Code-Convention)
<br/>
[Code Convention](https://github.com/prgrms-be-devcourse/NBE4-5-2-Team10/wiki/Git-Commit-Convention)
