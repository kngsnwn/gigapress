# Ebmp Proj Standard API

이트너스 Standard API 프로젝트입니다.

### 새 프로젝트 생성 가이드
- File - Project Structure - Project sdk
- File - Settings - Build, Execution, Deployment - Build Tools - Gradle - Gradle JVM JDK 버전을 맞춰주세요.
- Standard API copy -> 새 프로젝트 생성하기
  - GitLab - New Project
  - Clone HTTP
  - 새로 생성된 클론 프로젝트 폴더로 이동
  - ebmp-proj-standard-api 폴더 속 .git 제외 전부 복사 → 새로 생성된 클론 프로젝트 폴더에 붙여넣기
  - IntelliJ에서 해당 프로젝트 Open
  - 프로젝트 속 'standard' -> '원하는 프로젝트명' 전부 변경
  - 로컬 환경에서 톰캣 실행
  - Swagger API 정상 접속 확인
  - 개인 브랜치 생성 후 커밋, 원격 푸시

자세한 가이드는 [노션](https://etners-rnd.notion.site/Standard-API-copy-a53a66327608471c98209e5eda137402?pvs=4)을 참고해 주세요.


### 모듈 단위 패키지 구조 가이드
<pre>
└── src
    └── main
          ├── java
          │   └── etners
          │          └──── standard
          │          │        ├── ddd
          │          │        └── mvc
          │          │        
          │          │        
          │          ├──── domain package         # 프로젝트명
          │          │        ├── controller        
          │          │        ├── domain            
          │          │        └── service           
          │          │          
          │          │        
          │          └──── common                 # 공통 영역
          │                   ├── config
          │                   ├── interceptor 
          │                   ├── util
          │                   └── ...
          │
          │
          └── resources      # 정적 리소스
                 ├── application-dev.properties
                 ├── application-local.properties
                 ├── application-prod.properties
                 └── application.properties
</pre>
<br>

## Backend Code Convention

- Project setting

```
font -> D2Coding
codeStyle -> Google Style,
tab size는 2/ indent 2 적용
```

- Naming

```
변수, 함수, 인스턴스 -> Lower Camel Case
Class, Constructor  -> Upper Camel Case
상수 -> 대문자 표기 (ex) REGIST ( 권고 : ENUM 정의 )
```

- Package 구성

```
도메인 중심 구성.
```

- URL 구성

```
REST API 방식 구성 (Kebab case)
GET /check or /check-name
PUT
POST
DELETE
```

- DI 방식

```java
@RequiredArgsConstructor

private final cdMstRepository cdMstRepository;
private final cdDtlRepository cdDtlRepository;
```
- 로그 설정

```
P6spySqlFormatConfiguration.java
* p6spy 쿼리 콘솔 로그 2번 출력 되는 오류 수정 및 sql date param값 ISO 8601 포멧으로 찍히는 걸 Oracle 친화적으로 변경. 
* 다른 DB를 사용하는 Project는 제거 필요

P6SpyOptions.getActiveInstance().setAppender("com.p6spy.engine.spy.appender.StdoutLogger");
sql = convertDateForOracle(sql);
```

- 로깅 방식

```
AOP 기반 전역 예외 로깅 (LoggingAspect class)

@AfterThrowing(pointcut = "execution(프로젝트 구조에 맞춰 설정)", throwing = "exception")
public void logAfterThrowing(JoinPoint joinPoint, Throwable exception) {
  if (skipLogging(exception)) {
    return;
  }
  logger.error("Method {} threw exception {}", joinPoint.getSignature().toShortString(), exception.getMessage());
  logger.error(StringUtil.extractStackTrace(exception));
}

이 때 개발자에 의해 의도된 custom exception인 Application Exception은 skipLogging을 통해 생략
```

- BaseEntity 공통화

1) BaseEntity

```java

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity {

  @Column(name = "SEQ_NO")
  @Schema(description = "조회순서")
  private long seqNo = 1;
  @Column(name = "USE_YN")
  @Schema(description = "사용여부")
  private String useYn = "Y";
  @CreatedBy
  @Column(name = "FRST_RGST_ID", updatable = false)
  @Schema(description = "최초등록자")
  private String frstRgstId;
  @CreationTimestamp
  @Column(name = "FRST_RGST_DT", updatable = false)
  @Schema(description = "최초등록일시")
  private LocalDateTime frstRgstDt;
  @LastModifiedBy
  @Column(name = "LAST_MDFY_ID")
  @Schema(description = "최종수정자")
  private String lastMdfyId;
  @UpdateTimestamp
  @Column(name = "LAST_MDFY_DT")
  @Schema(description = "최종수정일시")
  private LocalDateTime lastMdfyDt;

  public void updateUseYn(String useYn) {
    this.useYn = useYn;
  }

  @PrePersist
  public void prePersist() {
  }

  @PreUpdate
  public void preUpdate() {
  }
}
```

2) Entity적용

```java

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@IdClass(LocaMstPK.class)
@Table(name = "LOCA_MST")
public class EfsLocaWh extends BaseEntity {

  @Id
  @Column(name = "CMPY_CD")
  @Schema(description = "회사코드", nullable = false)
  private String cmpyCd;
  @Id
  @Column(name = "LOCA_CD")
  @Schema(description = "로케이션코드", nullable = false)
  private String locaCd;

  @Builder
  public EfsLocaWh(String cmpyCd, String locaCd) {
    this.cmpyCd = cmpyCd;
    this.locaCd = locaCd;
  }

  public static EfsLocaWh create(String cmpyCd, String locaCd) {
    return EfsLocaWh.builder()
        .cmpyCd(cmpyCd)
        .locaCd(locaCd)
        .build();
  }

  public void update(String cmpyCd, String locaCd) {
    this.cmpyCd = cmpyCd;
    this.locaCd = locaCd;
  }

  @Override
  public void updateUseYn(String useYn) {
    super.updateUseYn(useYn);
  }
```

- 공통 API 적용 사항은 @Scope로 공통화

ex) locale, cmpyCd, unqUserId

```java

@Component
@Getter
@Setter
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CurrentUserData {

  private String cmpyCd
  private List<String> currentMngCmpyList;
}
```

- 실제 적용 interceptor

```java
currentUserData.setCmpyCd(loginUserTokenContainer.getCompanyCode());
currentUserData.setUnqUserId(loginUserTokenContainer.getUniqueUserId());
```

- swagger 적용

1) request

```java
@Schema(
    description = "회사",
    example = "1234"
)
@NotEmpty
private String cmpyCd;
```

2) response

```java
@Schema(
    description = "구분명"
    , example = "상품"
)
```

3) controller

```java
@GetMapping("/info")
@Operation(summary = "내역 조회 API"
    , responses = {
    @ApiResponse(responseCode = "200"
        , description = "성공"
        , content = @Content(mediaType = "application/json"
        , schema = @Schema(implementation = InfoResponse.class)))
})
public @ResponseBody
ResultModel<List<InfoResponse>>getInfo(
@Valid InfoRequestParam onfoRequestParam){
    return infoService.getInfo(infoRequestParam);
    }
```

- Git commit convention

```
header - feat : ~~~에 대한 조회 API ( JIRA 티켓명)
body - 상세 내용
footer -  #Jira Ticket번호
```

- Commit header keyword

```
feat : 새로운 기능 추가
fix : 버그 수정
change : 버그가 아닌 어떠한 사유로 인한 코드 변경
docs : 문서 수정
style : 코드 formatting, 세미콜론(;) 누락, 코드 변경이 없는 경우
refactor : 코드 리펙토링
test : 테스트코드, 리펙토링 테스트 코드 추가(프로덕션 코드 변경 X)
chore : 빌드 업무 수정, 패키지 매니저 수정(프로덕션 코드 변경 X)
design : CSS 등 사용자 UI 디자인 변경
comment : 필요한 주석 추가 및 변경
rename : 파일 혹은 폴더명을 수정하거나 옮기는 작업만인 경우
remove : 파일을 삭제하는 작업만 수행한 경우
!BREAKING CHANGE : 커다란 API 변경의 경우
!HOTFIX : 급하게 치명적인 버그를 고쳐야 하는 경우
```

- Project Version ( ~ LTS 2025.08.18 )

```
Springboot 2.5.14 => 3.1.2
Gradle 7.3.1 => 8.2.1
```
20240206 Update
- Project Version ( ~ LTS 2026.02.23 )

```
Springboot 3.1.2 => 3.2.2
Gradle 8.2.1 => 8.6

lombok 1.18.22 => 1.18.30
openapi 2.0.2 => 2.3.0
shedlock 5.6.0 => 5.10.2
httpclient 4.5.14 => httpclient5 5.3.1
ojdbc10 19.19.0.0 => 19.21.0.0
joda-time 2.12.5 => 2.12.6
spring-cloud-dependencies 2022.0.4 => 2023.0.0

```
- 불필요한 로직 삭제
```
CoolSms 관련 로직 삭제
EventLog 관련 로직 삭제
NetworkUtil 관련 로직 삭제
Mobile Device 관련 로직 삭제
```

- 암호화 관련 파일 lib 이동
```
BouncyModule.java
SecurityAES256.java
```
