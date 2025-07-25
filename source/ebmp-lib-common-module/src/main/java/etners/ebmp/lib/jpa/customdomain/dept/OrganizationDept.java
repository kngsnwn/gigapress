package etners.ebmp.lib.jpa.customdomain.dept;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author oxide
 * @version 1.0
 * @Class Name : OrganizationDept.java
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
public class OrganizationDept {

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
  private String displayYn;

  /**
   *
   */
  private String superDeptCd;

  /**
   *
   */
  private int deptLevel;

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
  private int joinUserCount = 0;

  /**
   * displayRootFl
   */
  private int childDeptCount = 0;

  /**
   *
   */
  private List<OrganizationUser> userList = null;

}
