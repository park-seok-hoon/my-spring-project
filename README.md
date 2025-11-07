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

| 브랜치명 | 설명 |
|-----------|------|
| [`controller-exception`](https://github.com/park-seok-hoon/my-spring-project/tree/controller-exception) | Controller 계층에서 예외 처리한 초기 버전 |
| [`service-exception`](https://github.com/park-seok-hoon/my-spring-project/tree/service-exception) | Service 계층으로 예외 처리 및 검증 책임을 이전한 개선 버전 (**최신**) |

---

## 🧩 개선 히스토리
| 날짜 | 변경 내용 | 설명 |
|------|------------|------|
| **2025.11.07** | 예외 처리 위치를 **Controller → Service** 로 이동 | 실무에서는 Controller가 요청/응답만 담당하고, Service가 비즈니스 로직 단위 예외를 관리하는 구조를 사용함. 따라서 실무 구조에 맞게 개선함. |

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
