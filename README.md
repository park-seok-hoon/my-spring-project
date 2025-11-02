# 🛍️ Mini Shop (Spring Boot CRUD Project)

간단한 상품 관리용 Spring Boot 미니 프로젝트입니다.  
상품 정보를 **등록(Create), 조회(Read), 수정(Update), 삭제(Delete)** 할 수 있습니다.

---

## ⚙️ 기술 스택
- **Language**: Java 17  
- **Framework**: Spring Boot  
- **Database**: H2 (or MySQL)  
- **ORM / Mapper**: MyBatis  
- **Build Tool**: Gradle  
- **IDE**: IntelliJ IDEA  

---

## 📁 프로젝트 구조
1. 기능
상품 생성(Create)
상품 조회(Read)
상품 수정(Update)
상품 삭제(Delete)
컨트롤러에서 예외 처리 후 HTTP 상태 코드 반환

## 2. 패키지 구조
com.minishop <br>
- controller 요청 처리, HTTP 응답 반환 <br>
- service  비즈니스 로직 <br>
- mapper  DB 쿼리 <br>
- domain  Entity 클래스 <br>
 
## 3. Controller 예외 처리 예시

### 3.1 상품 생성 (Create)
```java
@PostMapping
public ResponseEntity<Items> createItem(@RequestBody Items item) {
    try{
        Items savedItem = itemService.save(item);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedItem.getId())
                .toUri();
        return ResponseEntity.created(location).body(savedItem); // 201 Created
    } catch (RuntimeException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 500 error
    }
}
```
 - 서비스에서 예외 발생 시, 500 Internal Server Error 반환
 - 성공 시, 생성된 상품 정보와 201 Created 반환

### 3.2 상품 수정 (Update)
```java
@PutMapping("/{id}")
public ResponseEntity<Void> updateItem(@PathVariable("id") Long id, @RequestBody Items items) {
    items.setId(id);
    int updateRows = itemService.update(id, items);
    log.info("바뀐 행의 수= {}", updateRows);

    if(updateRows == 0){
        return ResponseEntity.notFound().build(); // 404 Not Found
    }

    return ResponseEntity.noContent().build(); // 204 No Content
}
```
- 수정 실패 시 404 Not Found, 성공 시 204 No Content

###3.3 상품 삭제 (Delete)
```java
  @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteItem(@PathVariable("id") Long id) {
        try{
           itemService.delete(id);
           return ResponseEntity
                    .status(HttpStatus.OK)
                    .body("상품이 삭제되었습니다."); // 200 OK
        }catch(RuntimeException e) {
            return ResponseEntity.notFound().build();   //실패 204
        }
    }
```
- 서비스에서 삭제할 아이템이 없거나 예외 발생 시 404 반환

### 3.4 상품 조회 (Read)
```java
   @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable("id") Long id) {
       try {
           Items item = itemService.findById(id);
           return ResponseEntity.ok(item); //200 OK
       } catch (IllegalArgumentException e) {
           return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); //실패 404
       }
    }
```
- 존재하지 않는 아이템 조회 시 404 Not Found 반환

### 3.5 전체 상품 조회
```java
  @GetMapping
    public ResponseEntity<?> getAllItems() {
        try{
            List<Items> items = itemService.findAll();
            //아무것도 들어있지 않은 경우
            if( items.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body("조회된 상품이 없습니다."); // 200 OK
            }
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("상품 목록 조회 중 오류 발생" + e.getMessage());  //예외 발생 시 500 Internal Server Error
        }
    }
```
- 상품이 없을 때는 메시지 반환, 예외 발생 시 500 Internal Server Error

