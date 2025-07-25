package etners.common.basetype;

import common.util.string.StringUtil;
import java.io.Serializable;
import lombok.Data;


@Data
public class CommonPagingParam implements Serializable {

  private static final long serialVersionUID = 7663723321132318683L;

  /**
   * 요청 페이지 번호.
   * <p>
   * 페이지 번호는 1부터 시작되어야 한다.
   */
  protected int pageNo;

  /**
   * 한 페이지당 게시물 갯수
   */
  protected int countPerPage = 10;

  public CommonPagingParam(int pageNo, int countPerPage) {
    this.pageNo = pageNo;
    this.countPerPage = countPerPage;
  }

  public CommonPagingParam(String pageNoStr, int countPerPage) {
    this(pageNoToInt(pageNoStr), countPerPage);
  }

  private static int pageNoToInt(String pageNoStr) {
    int pageNo = -1;

    if (StringUtil.isNotEmpty(pageNoStr) && StringUtil.isNumberFormat(pageNoStr)) {
      pageNo = Integer.parseInt(pageNoStr);
    } else {
      pageNo = 1;
    }

    return pageNo;
  }

  public int getOffset() {
    return (this.pageNo - 1) * countPerPage;
  }
}
