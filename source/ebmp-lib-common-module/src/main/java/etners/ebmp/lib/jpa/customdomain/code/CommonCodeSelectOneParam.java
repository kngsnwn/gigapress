package etners.ebmp.lib.jpa.customdomain.code;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author oxide
 * @version 1.0
 * @Class Name : CommonCodeSelectOneParam.java
 * @Description :
 * @Modification Information
 * @
 * @ 수정일      수정자              수정내용
 * @ ---------   ---------   -------------------------------
 * @ 2019. 8. 2.  oxide     최초생성
 * @see
 * @since 2019. 8. 2.
 */
@Data
@NoArgsConstructor
public class CommonCodeSelectOneParam {

  private String cmpyCd;
  private String cdGrup;
  private String cdId;

}
