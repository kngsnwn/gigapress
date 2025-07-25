package etners.common.config.springprofile;

import java.io.File;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;


@Configuration
public class DetectProductionModeProfile {

  private final Environment environment;

  @Value("${spring.datasource.url}")
  private String dbConnectUrlDepandancy;

  private static String currentRuntimeMode;

  private static String dbConnectUrl;

  public DetectProductionModeProfile(Environment environment) {
    this.environment = environment;
  }

  @PostConstruct
  public void initialConfigure() {
    currentRuntimeMode = environment.getProperty("spring.profiles.active");
    dbConnectUrl = this.dbConnectUrlDepandancy;
  }

  public enum RuntimeMode {
    PROD("prod", "운영 모드. 운영 서버에서 동작 중일 때", "220"),
    DEV("dev", "개발 모드. 개발 서버에서 동작 중일 때", "219"),
    LOCAL("local", "로컬 모드. 로컬에서 테스트 중일 때", "218");

    private String runtimeMode;

    private String description;

    private String cdGrup;

    RuntimeMode(String runtimeMode, String description, String cdGrup) {
      this.runtimeMode = runtimeMode;
      this.description = description;
      this.cdGrup = cdGrup;
    }

    public String getRuntimeMode() {
      return runtimeMode;
    }

    public void setRuntimeMode(String runtimeMode) {
      this.runtimeMode = runtimeMode;
    }

    public String getDescription() {
      return description;
    }

    public void setDescription(String description) {
      this.description = description;
    }

    public String getCdGrup() {
      return cdGrup;
    }

    public static RuntimeMode getRuntimeMode(String runtimeMode) {
      for (RuntimeMode mode : values()) {
        if (mode.getRuntimeMode().equals(runtimeMode)) {
          return mode;
        }
      }

      return null;
    }

  }

  public enum EnvironmentOsType {
    WINDOWS("PRODUCTION", "윈도우 환경"), LINUX("LINUX", "리눅스 환경"), MAC("MAC", "맥 환경");

    private String ostype;

    private String description;

    EnvironmentOsType(String ostype, String description) {
      this.ostype = ostype;
      this.description = description;
    }


    public String getOstype() {
      return ostype;
    }


    public String getDescription() {
      return description;
    }
  }

  /**
   * <pre>
   * 메소드명 : getDBConnectMode
   * 작성자  : oxide
   * 작성일  : 2018. 4. 18.
   * 설명   :
   * </pre>
   *
   * @return
   */
  public static String getDBConnectMode() {
    if (dbConnectUrl == null) {
      return "DB Connect Error";
    }

    if (dbConnectUrl.indexOf("211.115.91.119") != -1) {
      return "PRODUCTION";
    } else if (dbConnectUrl.indexOf("182.162.136.31") != -1) {
      return "DEV";
    }

    return "UNKNOWN_DB_CONNECT_MODE : " + dbConnectUrl;
  }

  /**
   * <pre>
   * 메소드명 : getCurrentRuntimeMode
   * 작성자  : oxide
   * 작성일  : 2018. 4. 14.
   * 설명   : /home/Upload/smile/properties/file.properties 파일에 현재 운영/개발/로컬 중 어느 모드로 되어있는지 확인한다.
   *        이 프로퍼티의 값은 프로젝트 외부 경로에 존재하여 빌드된 소스와 상관없이 서버나 로컬에 따라 값이 달라진다.
   * </pre>
   *
   * @return
   */
  public static RuntimeMode getCurrentRuntimeAndDBMode() {
    RuntimeMode mode = RuntimeMode.getRuntimeMode(currentRuntimeMode);

    if (mode == null) {
      throw new IllegalArgumentException("런타임 모드가 제대로 지정되지 않았습니다.");
    }

    return mode;
  }


  /**
   * <pre>
   * 메소드명 : isExecutionEnvironmentWindows
   * 작성자  : oxide
   * 작성일  : 2018. 3. 29.
   * 설명   : 윈도우 환경인지 디렉터리 분석을 통해 확인하고,
   *        맞는 경우엔 true,
   *        아니면 false를 리턴한다.
   *
   *        스케줄러가 오로지 리눅스 서버에서만 동작하고,
   *        로컬에서 개발 테스트할 때는 중복에서 동작하지 않도록 처리하기 위함이다.
   * </pre>
   *
   * @return
   */
  public static String getExecutionEnvironmentOsSystem() {
    File windowFolder = new File("C:\\Windows");
    File programFilesFolder = new File("C:\\Program Files");

    /** 둘 중 하나의 폴더만 존재해도 윈도우 환경의 컴퓨터로 간주한다. */
    if (windowFolder.isDirectory()
        || programFilesFolder.isDirectory()) {
      return "WINDOW";
    }

    File linuxUsrFolder = new File("/usr");
    File linuxEtcFolder = new File("/etc");
    File linuxBinFolder = new File("/bin");

    /** 리눅스 표준 폴더 구성이므로 셋 다 존재할 때 리눅스 환경의 컴퓨터로 간주한다. */
    if (linuxUsrFolder.isDirectory()
        && linuxEtcFolder.isDirectory()
        && linuxBinFolder.isDirectory()) {
      return "LINUX";
    }

    File maxOsApplicationFolder = new File("/Applications");

    /** 맥 표준 폴더 구성이므로 Applications 폴더가 존재할 때 맥 환경의 컴퓨터로 간주한다. */
    if (maxOsApplicationFolder.isDirectory()) {
      return "MAC";
    }

    return "UNKNOWN";
  }

  /**
   * <pre>
   * 메소드명	: isProductionMode
   * 작성자	: oxide
   * 작성일	: 2019. 6. 3.
   * 설명	: 운영 환경으로 세팅되어 있는지를 체크한다. (자바 실행 시점에 -Dspring.profiles.active로 넘어온 인자값이 'prod'이면 운영 환경)
   *        운영 환경인 경우 true 리턴.
   * </pre>
   *
   * @return
   */
  public static boolean isProductionMode() {
    return RuntimeMode.PROD.equals(getCurrentRuntimeAndDBMode());
  }

  /**
   * <pre>
   * 메소드명	: isDevelopmentMode
   * 작성자	: oxide
   * 작성일	: 2019. 6. 3.
   * 설명	: 운영 환경으로 세팅되어 있는지를 체크한다. (자바 실행 시점에 -Dspring.profiles.active로 넘어온 인자값이 'dev'이면 개발 환경)
   *        운영 환경인 경우 true 리턴.
   * </pre>
   *
   * @return
   */
  public static boolean isDevelopmentMode() {
    return RuntimeMode.DEV.equals(getCurrentRuntimeAndDBMode());
  }

  /**
   * <pre>
   * 메소드명	: isDevelopmentModeOrProductionMode
   * 작성자	: oxide
   * 작성일	: 2019. 6. 3.
   * 설명	: 운영 환경 또는 개발 환경인지를 체크한다.
   *        둘 중 하나라도 true라면 true를 리턴함.
   * </pre>
   *
   * @return
   */
  public static boolean isDevelopmentModeOrProductionMode() {
    return isDevelopmentMode() || isProductionMode();
  }

  /**
   * <pre>
   * 메소드명	: isRunOnLinuxServer
   * 작성자	: oxide
   * 작성일	: 2019. 6. 3.
   * 설명	: 현재 자바가 실행된 OS가 리눅스 환경이라면 true를 리턴한다.
   * </pre>
   *
   * @return
   */
  public static boolean isRunOnLinuxServer() {
    return "LINUX".equals(getExecutionEnvironmentOsSystem());
  }
}
