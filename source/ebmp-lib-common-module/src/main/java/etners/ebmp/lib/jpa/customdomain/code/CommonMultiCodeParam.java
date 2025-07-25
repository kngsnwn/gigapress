package etners.ebmp.lib.jpa.customdomain.code;

import lombok.Data;

/**
 * @author oxide
 * @version 1.0
 * @Class Name : CommonMultiSolutionCodeParam.java
 * @Description :
 * @Modification Information
 * @
 * @ 수정일      수정자              수정내용
 * @ ---------   ---------   -------------------------------
 * @ 2019. 3. 9.  oxide     최초생성
 * @see
 * @since 2019. 3. 9.
 */
@Data
public class CommonMultiCodeParam {

  private CommonCodeParam[] codes;
}
