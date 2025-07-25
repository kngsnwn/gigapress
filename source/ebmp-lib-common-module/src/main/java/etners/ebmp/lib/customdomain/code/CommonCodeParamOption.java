package etners.ebmp.lib.customdomain.code;

import common.util.string.StringUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CommonCodeParamOption {

  /**
   * 회사 코드값
   */
  private String cmpyCd;

  /**
   * 공통코드 디테일 목록 중에서 필요로 하는 cdId값만 배열 형태로 전달받았을 경우, 이 값만을 리턴한다.
   */
  private List<String> filterCdId;

  /**
   * displayAll 값이 true면 기존 공통 코드 목록과 별개로 '전체'라는 항목을 추가하여 리턴한다.
   */
  private Boolean displayAll = false;

  /**
   * displayAll 값이 true면 여기에 있는 텍스트 값을 세팅하는 항목이 추가된다. (기본값 : 전체)
   */
  private String displayAllText = "전체";

  /**
   * displayAll 값이 true면 여기에 있는 Value 값을 세팅하는 항목이 추가된다. (기본값 : 전체)
   */
  private String displayAlldescription = "ALL";

  /**
   * 이 값이 존재하는 경우 해당 CdId 코드에 대해 selected 필드를 true로 리턴한다.
   */
  private String selectedCdId;

  public CommonCodeParamOption(String displayAllText, String displayAlldescription, List<String> filterCdId) {

    if (StringUtil.isNotEmpty(displayAllText) && StringUtil.isNotEmpty(displayAlldescription)) {
      this.displayAll = true;
      this.displayAllText = displayAllText;
      this.displayAlldescription = displayAlldescription;
    }

    this.filterCdId = filterCdId;
  }

  public CommonCodeParamOption(String displayAllText, String displayAllValue, String... filterCdIdArray) {
    this(displayAllText, displayAllValue, new ArrayList<String>(Arrays.asList(filterCdIdArray)));
  }

  public CommonCodeParamOption(String displayAllText, String displayAlldescription) {
    if (StringUtil.isNotEmpty(displayAllText) && StringUtil.isNotEmpty(displayAlldescription)) {
      this.displayAll = true;
      this.displayAllText = displayAllText;
      this.displayAlldescription = displayAlldescription;
    }
  }

  public CommonCodeParamOption(String... filterCdIdArray) {
    this(null, null, filterCdIdArray);
  }

}
