package etners.ebmp.lib.jpa.customdomain.dept;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author oxide
 * @version 1.0
 * @Class Name : OrganizationUser.java
 * @Description :
 * @Modification Information
 * @
 * @ 수정일      수정자              수정내용
 * @ ---------   ---------   -------------------------------
 * @ 2019. 8. 28.  oxide     최초생성
 * @see Copyright (C) by etners All Rights Reserved.
 * @since 2019. 8. 28.
 */
@Data
@NoArgsConstructor
public class OrganizationUser {

  /**
   *
   */
  private String unqUserId;

  /**
   *
   */
  private String empNm;

  /**
   *
   */
  private String deptCd;

  /**
   *
   */
  private String deptNm;

  /**
   *
   */
  private String deptNmPath;

  /**
   *
   */
  private String deptCdPath;

  /**
   *
   */
  private String psnScnNm;

  /**
   *
   */
  private String dutyScnNm;

  /**
   *
   */
  private String jobScnNm;
}
