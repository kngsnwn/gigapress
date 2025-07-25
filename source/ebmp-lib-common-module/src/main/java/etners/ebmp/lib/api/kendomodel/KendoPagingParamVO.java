package etners.ebmp.lib.api.kendomodel;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.Arrays;
import java.util.stream.Stream;

@Schema(description = "Kendo Grid에서 페이지 요청을 할 때 필요한 파라미터를 정의해둔 도메인 클래스입니다.")
public class KendoPagingParamVO implements Serializable {

  private static final long serialVersionUID = 3047395672582709234L;

  @Schema(
    example = "0",
    required = true,
    description = "필수값. 쿼리문 내의 offset과 동일한 의미로 kendo에서 활용됩니다. 현재 백엔드 로직에서는 별도로 활용하지는 않고 있습니다.")
  protected int skip;

  @Schema(
    example = "10",
    required = true,
    description = "필수값. 한 페이지당 몇 개의 데이터 목록을 요청할 것인지를 나타냅니다. pageSize와 동일한 값으로 보이며, skip과 한쌍으로 활용되는 것으로 추측됩니다. 현재 백엔드 로직에서는 별도로 활용하지는 않고 있습니다.")
  protected int take;

  @Schema(
    example = "1",
    required = true,
    description = "필수값. 몇번째 페이지를 요청할 것인지를 나타냅니다. 이 값은 반드시 1 이상이어야 합니다.")
  protected int page;

  @Schema(
    example = "10",
    required = true,
    description = "필수값. 한 페이지당 불러올 데이터의 갯수를 나타냅니다. 10이면 10개의 항목이 1개의 페이지에 나올 수 있는 최대 갯수로 보고 리스트를 가져옵니다.")
  protected int pageSize;


  protected KendoFilterModule[] filter;

  protected KendoSort[] sort;

  @Schema(
    example = "PAGING",
    allowableValues = "PAGING/EXPORT",
    required = true)
  protected String requestType = "PAGING"; //PAGING or EXPORT

  public KendoPagingParamVO() {
    super();
  }

  public int getSkip() {
    return skip;
  }

  public void setSkip(int skip) {
    this.skip = skip;
  }

  public int getTake() {
    return take;
  }

  public void setTake(int take) {
    this.take = take;
  }

  public int getPage() {
    return page;
  }

  public void setPage(int page) {
    this.page = page;
  }

  public int getPageSize() {
    return pageSize;
  }

  public void setPageSize(int pageSize) {
    this.pageSize = pageSize;
  }

  public KendoFilterModule[] getFilter() {
    return filter;
  }

  public void setFilter(KendoFilterModule[] filter) {
    if (this.filter == null) {
      this.filter = filter;
    } else {
      appendFilter(filter);
    }
  }

  public void appendFilter(KendoFilterModule... filters) {
    if (this.filter == null) {
      this.filter = filters;
    } else {
      this.filter = Stream.of(this.filter, filters).flatMap(Stream::of)
        .toArray(KendoFilterModule[]::new);
    }

  }

  public KendoSort[] getSort() {
    return sort;
  }

  public void setSort(KendoSort[] sort) {
    this.sort = sort;
  }

  public String getRequestType() {
    return requestType;
  }

  public void setRequestType(String requestType) {
    this.requestType = requestType;
  }

  @Override
  public String toString() {
    return "KendoPagingParamVO [skip=" + skip + ", take=" + take + ", page=" + page + ", pageSize="
      + pageSize + ", " + (filter != null ? "filter=" + Arrays.toString(filter) + ", " : "") + (
      sort != null ? "sort=" + Arrays.toString(sort) + ", " : "")
      + (requestType != null ? "requestType=" + requestType : "") + "]";
  }

}
