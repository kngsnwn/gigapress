package etners.common.basetype;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public class CommonPagingResult<T> implements Serializable {

  private static final long serialVersionUID = 7324791718555118525L;

  /**
   * 현재 페이지 번호
   */
  private int currentPageNo = 1;

  /**
   * 현재 페이지에 표시할 게시물 갯수
   */
  private int countPerPage;

  /**
   * DB 테이블에 존재하는 전체 row 갯수
   */
  private int totalCount = -1;

  private List<T> list;

  public CommonPagingResult(int currentPageNo, int countPerPage, int total, List<T> list) {
    this.currentPageNo = currentPageNo;
    this.countPerPage = countPerPage;
    this.totalCount = total;
    this.list = list;
  }

  public CommonPagingResult(CommonPagingParam pagingParam, int total, List<T> list) {
    this.currentPageNo = pagingParam.getPageNo();
    this.countPerPage = pagingParam.getCountPerPage();
    this.totalCount = total;
    this.list = list;
  }
}
