package etners.ebmp.lib.customdomain.dept;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrganizationDept {

  private String deptCd;

  private String deptNm;

  private String displayYn;

  private String superDeptCd;

  private int deptLevel;

  private String deptNmPath;

  private String deptCdPath;

  private int joinUserCount = 0;

  private int childDeptCount = 0;

  private List<OrganizationUser> userList = null;

}
