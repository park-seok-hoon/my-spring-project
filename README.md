# 🛍️ Mini Shop (Spring Boot CRUD Project)

간단한 상품 관리용 Spring Boot 미니 프로젝트입니다.  
상품 정보를 **등록(Create), 조회(Read), 수정(Update), 삭제(Delete)** 할 수 있습니다.  

해당 프로젝트는 **예외 처리 구조 개선**을 중심으로 두 가지 버전으로 구성되어 있습니다.  
(Controller 계층 → Service 계층으로 예외 처리 책임을 이전)

---

## ⚙️ 기술 스택
- **Language**: Java 17  
- **Framework**: Spring Boot  
- **Database**: H2 (or MySQL)  
- **ORM / Mapper**: MyBatis  
- **Build Tool**: Gradle  
- **IDE**: IntelliJ IDEA  

---

## 📚 브랜치 목록

| 브랜치명                                                                                                                      | 설명                                             |
| ------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------- |
| [`controller-exception`](https://github.com/park-seok-hoon/my-spring-project/tree/controller-exception)                   | Controller 계층에서 예외 처리한 초기 버전                   |
| [`service-exception`](https://github.com/park-seok-hoon/my-spring-project/tree/service-exception)                         | Service 계층으로 예외 처리 및 검증 책임을 이전한 개선 버전  |
| [`refactor/unified-api-response`](https://github.com/park-seok-hoon/my-spring-project/tree/refactor/unified-api-response) | API 응답 구조 통일 및 Item,user 모듈 리팩토링 버전    (**최신**)        |


---

## 🧩 개선 히스토리
| 날짜             | 변경 내용                                  | 상세 설명                                                                                                                                       |
| -------------- | -------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------- |
| **2025.11.07** | 예외 처리 위치를 **Controller → Service**로 이전 | Controller는 요청/응답만 담당하고, Service에서 비즈니스 예외를 처리하도록 구조 개선                                                                                     |
| **2025.11.10** | ✅ **Item 모듈 리팩토링 완료**                  | `ItemService`로 예외 처리 및 검증 책임 이전 → Controller 단 SRP(단일 책임 원칙) 적용                                                                             |
| **2025.11.12** | ✅ **User 모듈 리팩토링 완료**                  | `UserService`에서 예외 처리 및 검증 통합 <br> - 중복 이메일 검증 추가<br> - `@Valid` 기반 유효성 검사 추가<br> - `ErrorCode` Enum 확장 (USER_NOT_FOUND, DUPLICATE_EMAIL 등) |
| **2025.11.12** | ✅ **API 응답 통일화**                       | `ApiResponse<T>`를 도입하여 모든 응답 구조를 `{ code, message, data }` 형태로 일원화                                                                          |

---

## 💡 개선 요약

| 항목 | Controller 예외 처리 | Service 예외 처리 |
|------|----------------------|--------------------|
| 예외 위치 | Controller | Service |
| 처리 방식 | try-catch 직접 처리 | AppException + GlobalExceptionHandler |
| HTTP 상태 코드 | Controller 내부에서 지정 | ErrorCode Enum으로 통일 관리 |
| 유지보수성 | 낮음 | 높음 |
| 실무 적용성 | ❌ 비권장 | ✅ 표준적인 구조 |

---

## 📁 프로젝트 구조
```
com.minishop
┣ controller → 요청 처리 및 HTTP 응답 반환
┣ service → 비즈니스 로직 및 예외 처리 (Service Layer 중심 구조)
┣ repository → MyBatis Mapper 연동
┣ domain → Entity 클래스
┗ exception → 공통 예외 클래스 및 GlobalExceptionHandler
```

## ✅ 프로젝트 목적

- Spring Boot의 CRUD 기본 동작 학습  
- Controller와 Service의 역할 구분 이해  
- 실무에서 사용되는 **Service Layer 기반 예외 처리 구조** 학습 및 적용  
