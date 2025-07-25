package etners.ebmp.lib.jpa.customdomain.code;

import java.util.List;
import lombok.Data;

/**
 * @author oxide
 * @version 1.0
 * @Class Name : CommonRuntimeCodeParam.java
 * @Description :
 * @Modification Information
 * @
 * @ 수정일      수정자              수정내용
 * @ ---------   ---------   -------------------------------
 * @ 2019. 4. 1.  oxide     최초생성
 * @see
 * @since 2019. 4. 1.
 */
@Data
public class CommonRuntimeCodeParam {

  /**
   * 공통코드 디테일 목록 중에서 필요로 하는 cdId값만 배열 형태로 전달받았을 경우, 이 값만을 리턴한다.
   */
  private List<String> filterCdId;
}
