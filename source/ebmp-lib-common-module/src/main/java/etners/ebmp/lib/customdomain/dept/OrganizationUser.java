package etners.ebmp.lib.customdomain.dept;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrganizationUser {

  private String unqUserId;

  private String empNm;

  private String deptCd;

  private String deptNm;

  private String deptNmPath;

  private String deptCdPath;

  private String psnScnNm;

  private String dutyScnNm;

  private String jobScnNm;
}
