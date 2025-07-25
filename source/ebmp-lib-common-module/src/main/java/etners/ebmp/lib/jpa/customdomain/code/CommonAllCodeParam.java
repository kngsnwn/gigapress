package etners.ebmp.lib.jpa.customdomain.code;

import lombok.Data;

/**
 * @author oxide
 * @version 1.0
 * @Class Name : CommonAllCodeParam.java
 * @Description :
 * @Modification Information
 * @
 * @ 수정일      수정자              수정내용
 * @ ---------   ---------   -------------------------------
 * @ 2019. 3. 11.  oxide     최초생성
 * @see Copyright (C) by etners All Rights Reserved.
 * @since 2019. 3. 11.
 */
@Data
public class CommonAllCodeParam {

  private String[] cmpyCd;

  public CommonAllCodeParam() {
  }

  public CommonAllCodeParam(String... cmpyCdArray) {
    this.cmpyCd = cmpyCdArray;
  }

}
