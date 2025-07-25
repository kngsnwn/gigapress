package etners.ebmp.lib.customdomain.code;

import java.util.List;
import lombok.Data;

@Data
public class CommonRuntimeCodeParam {

  /**
   * 공통코드 디테일 목록 중에서 필요로 하는 cdId값만 배열 형태로 전달받았을 경우, 이 값만을 리턴한다.
   */
  private List<String> filterCdId;
}
