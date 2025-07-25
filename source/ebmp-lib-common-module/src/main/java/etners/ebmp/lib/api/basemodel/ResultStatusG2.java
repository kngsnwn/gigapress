package etners.ebmp.lib.api.basemodel;

import etners.ebmp.lib.enums.lang.EbmpLang;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.ObjectUtils;

/**
 * : 초기 설계와 달리 메시지 데이터를 공통 코드 테이블에서 관리하게 됨에 따라 관련 필드 내용이 바뀌어서 2세대 ResultStatus 모델을 구현하게 되었다. 하위 호환성을 유지하기 위해 기존 ResultStatus와 호환이 되도록 만들었으나 장기적으로는 모든 응답 데이터를 ResultStatusG2로 바꾸어야만 한다.
 */
@Schema(description = "API 요청 처리에 따른 결과코드값을 담은 도메인 클래스. 스마일에서 사용되던 ResultStatus 클래스를 ebmp DB 중 공통 코드 쪽에 맞게 재설계된 것입니다. 이 클래스의 데이터는 모두 ResultModel에 담겨 전달됩니다.")
public class ResultStatusG2 {

  /**
   * 응답 상태 메시지에 부여된 코드값.
   * <p>
   * CPC_CODE_DTL의 CD_ID 컬럼이 대응됨.
   */
  @Schema(
    example = "4400",
    requiredMode = RequiredMode.REQUIRED,
    description = "응답 상태 메시지에 부여된 코드값. 정상 코드는 2000번으로 지정되어 있으며, 에러 메시지의 경우에는 4000번대 대역에 고유한 번호값으로 정의되어 있습니다. 자세한 것은 CPC_CODE_DTL 테이블의 CD_GRUP 217번 항목을 참조하세요.",
    hidden = false
  )
  private final String messageCode;


  /**
   * 다국어 처리시 다국어 데이터를 포함하게 될 자료 구조. CPC_CODE_DTL_LAN 테이블의 각 언어별 컬럼을 key:value 구조로 담는다. key로 언어명이 들어오고, value로 그 언어에 해당하는 메시지가 들어오는 형식.
   */
  @Schema(
    requiredMode = RequiredMode.REQUIRED,
    description = "해당 에러 메시지에 대한 다국어 처리 내용이 있을 때 그 값을 key:value 형태로 가지고 있는 맵입니다. 다국어 형태의 처리가 필요한 경우 여기서 데이터를 가져올 수 있습니다. 한국어를 포함한 나머지 언어가 여기 추가됩니다.",
    hidden = false
  )
  private final HashMap<EbmpLang, String> messageTextI18N;

  /**
   * 메시지가 백엔드 쪽에서 동적으로 처리되어 전달되어야 할 때 이 List의 데이터를 messageText와 동적으로 매핑한다. 이 때 messageText의 문자열에는 ##숫자##로 감싸진 구분자가 존재해야한다. (예: 요청하신 내용을 처리하여 결재 목록 중 ##0##건을 반려하였습니다)
   * <p>
   * ##숫자##로 매핑된 부분의 내용을 숫자 순서에 맞게 값을 변경 처리한다.
   */
  @Schema(
    example = "요청하신 내용을 처리하여 결재 목록 중 ##0##건을 반려하였습니다",
    requiredMode = RequiredMode.REQUIRED,
    description = "간혹 프론트로 전달해야하는 메시지 내용 중 비즈니스 로직의 처리 결과가 메시지에 함께 포함되어야 하는 경우가 있습니다. 이 경우 여기에 리스트 형태로 데이터를 추가하여, 추가된 순서대로 메시지 내에 동적으로 결과값을 바인딩하여 프론트로 전달해주기 위한 리스트 자료구조입니다. 반드시 필요한 부분이지만, 현재는 미구현 상태이므로 추후 설계 변경 여지가 있습니다.",
    hidden = false
  )
  private List<String> dynamicMessageDataList;

  /**
   * 메시지가 무엇을 의미하는 데이터인지에 대한 부연설명.
   * <p>
   * CD_DESC 컬럼 대응.
   */
  @Schema(
    example = "백엔드에서 검증시 사용자가 존재하지 않을 때 사용하는 에러 코드입니다.",
    requiredMode = RequiredMode.REQUIRED,
    description = "CPC_CODE_DTL 테이블의 CD_DESC 값을 따릅니다. 이 값은 해당 응답 상태에 대한 개발자 관점에서의 설명이나 전문적인 부연 설명이 작성되는 부분입니다.",
    hidden = false
  )
  private final String messageDescription;

  /**
   * 응답 상태 메시지에 부여된 풀네임스페이스. 메시지별로 고유한 네이밍 값을 부여받는다. 매핑된 데이터를 꺼낼 때 사용됨.
   * <p>
   * CD_DESC1 컬럼 대응.
   */
  @Schema(
    example = "ebmp.nk.join.already.signed.up.email",
    requiredMode = RequiredMode.REQUIRED,
    description = "응답 상태 메시지에 부여된 풀네임스페이스. 메시지별로 고유한 네이밍 값을 부여받는다. 매핑된 데이터를 꺼낼 때 사용됨. (현재 사용 안함)",
    hidden = false
  )
  private final String keyNamespace;

  /**
   * 현재 요청받은 언어 코드값. 이 값을 기준으로 getMessageText() 호출시 다국어 값을 꺼내어 표시한다.
   */
  @Schema(
    example = "EbmpLang.KO",
    allowableValues = "EbmpLang.KO/EbmpLang.EN/EbmpLang.ZH/EbmpLang.VN/EbmpLang.JP",
    requiredMode = RequiredMode.REQUIRED,
    description = "현재 요청받은 언어 코드값. 이 값을 기준으로 getMessageText() 호출시 다국어 값을 꺼내어 표시한다. 프론트나 모바일에서는 locale값만 전달하면 이 enum 클래스는 자동으로 세팅된다.",
    hidden = true
  )
  private EbmpLang ebmpLang;


  public ResultStatusG2(String messageCode, HashMap<EbmpLang, String> messageTextI18N,
    String messageDescription, String keyNamespace, EbmpLang ebmpLang) {
    if (ebmpLang == null) {
      throw new IllegalArgumentException("다국어 정보는 반드시 존재해야만 합니다.");
    }

    if (messageCode == null) {
      throw new IllegalArgumentException("표시할 메시지 코드는 반드시 존재해야만 합니다.");
    }

    if (ObjectUtils.isEmpty(messageTextI18N.size())) {
      throw new IllegalArgumentException("다국어 데이터를 가지는 맵이 반드시 필요합니다.");
    }

    this.messageCode = messageCode;
    this.messageTextI18N = messageTextI18N;
    this.messageDescription = messageDescription;
    this.keyNamespace = keyNamespace;

    this.ebmpLang = ebmpLang;
  }

  public ResultStatusG2(String messageCode, HashMap<EbmpLang, String> messageTextI18N,
    EbmpLang ebmpLang, String keyNamespace) {
    if (ebmpLang == null) {
      throw new IllegalArgumentException("다국어 정보는 반드시 존재해야만 합니다.");
    }

    if (messageCode == null) {
      throw new IllegalArgumentException("표시할 메시지 코드는 반드시 존재해야만 합니다.");
    }

    if (ObjectUtils.isEmpty(messageTextI18N)) {
      throw new IllegalArgumentException("다국어 데이터를 가지는 맵이 반드시 필요합니다.");
    }

    this.messageCode = messageCode;
    this.messageDescription = "";
    this.messageTextI18N = messageTextI18N;

    this.ebmpLang = ebmpLang;

    if (ObjectUtils.isEmpty(keyNamespace)) {
      this.keyNamespace = "test.message." + messageCode;
    } else {
      this.keyNamespace = keyNamespace;
    }
  }

  public ResultStatusG2(String messageCode, HashMap<EbmpLang, String> messageTextI18N,
    EbmpLang ebmpLang) {
    this(messageCode, messageTextI18N, ebmpLang, null);
  }

  /**
   * 다국어 처리가 되지 않는 생성자이므로 가급적 사용을 지양합니다.
   */
  @Deprecated
  public ResultStatusG2(String messageCode, String messageText) {
    if (messageCode == null) {
      throw new IllegalArgumentException("표시할 메시지 코드는 반드시 존재해야만 합니다.");
    }

    if (messageText == null) {
      throw new IllegalArgumentException("표시할 메시지 내용은 반드시 존재해야만 합니다.");
    }

    this.messageCode = messageCode;
    this.messageDescription = "";
    this.keyNamespace = "test.message." + messageCode;

    this.messageTextI18N = new HashMap<EbmpLang, String>();
    messageTextI18N.put(EbmpLang.KO, messageText);

    this.ebmpLang = EbmpLang.KO;
  }


  /**
   * <pre>
   * 메소드명	: setDynamicMessageDataList
   * 작성자	: oxide
   * 작성일	: 2019. 4. 8.
   * 설명	: 동적으로 바꿔야만 하는 데이터를 setter로 받는다.
   *        이 데이터는 비즈니스 로직에서 로직 처리시 받는 데이터이므로 반드시 setter를 통해서만 주입받아야 한다.
   *        리스트에 저장된 순서대로 기존 messageText 내에 존재하는 구분자를 치환한다.
   *
   *        예를 들어
   *        messageText의 값이 '문자 발송을 ##0##건 처리했습니다.' 이고, 넘어온 dynamicMessageDataList의 첫번째 값으로 11을 넣었다면
   *        getMessageText() 호출시 '문자 발송을 11건 처리했습니다.'로 변경되어 출력된다.
   * </pre>
   *
   * @param dynamicMessages
   */
  public void setDynamicMessageDataList(String... dynamicMessages) {
    this.dynamicMessageDataList = Arrays.asList(dynamicMessages);
  }


  @Schema(
    example = "이미 가입된 이메일 계정입니다.",
    requiredMode = RequiredMode.REQUIRED,
    description = "이 값은 해당 응답 상태에 대해 기본적으로 사용되는 메시지 내용입니다. 프론트에서 화면에 표시할 때 이 텍스트 값을 표시합니다.",
    hidden = false
  )
  public String getMessageText() {
    //해당 다국어에 맞는 값이 존재하지 않으면 한국어를 기본으로 하는 메시지를 dynamicText 조합하도록 assemblyMessageText()를 호출한다.
    if (!this.messageTextI18N.containsKey(ebmpLang)) {
      return assemblyMessageText(this.messageTextI18N.get(EbmpLang.KO));
    }

    //다국어에 대한 값을 꺼내서 dynamicText가 있는 경우 조합하도록 assemblyMessageText()를 호출한다.
    return assemblyMessageText(this.messageTextI18N.get(ebmpLang));
  }

  /**
   * <pre>
   * 메소드명	: assemblyMessageText
   * 작성자	: oxide
   * 작성일	: 2019. 4. 8.
   * 설명	: 메시지 내용 안에  다이나믹 텍스트로 치환 가능한 구분자가 존재하는지 확인하여 해당 구간이 존재할 경우,
   *        구분자를 메시지로 치환해줄 assemblyDynamicTextList()를 호출하는 함수.
   *
   *        별다른 구분자가 존재하지 않으면 메시지 그대로를 전달한다.
   *        구분자는 ##숫자##로 되어있으며 반드시 ##0##번부터 순서대로 존재해야만 치환이 가능하다.
   * </pre>
   *
   * @param currentMessageText
   * @return
   */
  private String assemblyMessageText(String currentMessageText) {
    if (ObjectUtils.isEmpty(dynamicMessageDataList)) {
      return currentMessageText; // 다이나믹 텍스트 데이터가 없으면 바로 반환
    }

    //다이나믹 텍스트 구간이 존재하는지 확인
    Pattern legacyPattern = Pattern.compile("##(\\d+)##");
    Matcher legacyMatcher = legacyPattern.matcher(currentMessageText);
    if (legacyMatcher.find() && ObjectUtils.isNotEmpty(dynamicMessageDataList)) {
      return assemblyDynamicTextList(currentMessageText);
    }

    Pattern pattern = Pattern.compile("\\{\\d+\\}");
    Matcher matcher = pattern.matcher(currentMessageText);
    if (matcher.find() && ObjectUtils.isNotEmpty(dynamicMessageDataList)) {
      return formatMessageText(currentMessageText);
    }

    //다이나믹 텍스트가 존재하지 않으면 그대로 전달.
    return currentMessageText;
  }

  private String formatMessageText(String currentMessageText) {
    return MessageFormat.format(currentMessageText, dynamicMessageDataList.toArray());
  }

  /**
   * <pre>
   * 메소드명	: assemblyDynamicTextList
   * 작성자	: oxide
   * 작성일	: 2019. 4. 8.
   * 설명	: 메시지 내용 안에 다이나믹 텍스트 구간이 존재할 경우, 구분자로 세팅된 부분에 대해 문자값을 치환하여 전달하도록 처리하는 메서드.
   *        구분자는 ##숫자## 형태로 들어와있어야 한다.
   *
   * </pre>
   *
   * @param currentMessageText
   * @return
   */
  private String assemblyDynamicTextList(String currentMessageText) {
    StringBuilder tempDynamicMessageText = new StringBuilder(currentMessageText);
    Pattern pattern = Pattern.compile("##(\\d+)##");
    Matcher matcher = pattern.matcher(currentMessageText);
    StringBuilder result = new StringBuilder();
    int lastIndex = 0;

    while (matcher.find()) {
      int index = Integer.parseInt(matcher.group(1));
      result.append(tempDynamicMessageText, lastIndex, matcher.start());
      if (index < dynamicMessageDataList.size()) {
        result.append(dynamicMessageDataList.get(index));
      }
      lastIndex = matcher.end();
    }

    result.append(tempDynamicMessageText.substring(lastIndex));
    return result.toString();
  }

  public ResultStatusG2 changeLang(EbmpLang ebmpLang) {
    this.ebmpLang = ebmpLang;

    return this;
  }


  /*****************************************
   * Simple Getter                         *
   *****************************************/

  public String getMessageCode() {
    return messageCode;
  }

  public String getMessageDescription() {
    return messageDescription;
  }

  public String getKeyNamespace() {
    return keyNamespace;
  }


  /*****************************************
   * toString                              *
   *****************************************/

  @Override
  public String toString() {
    return "ResultStatusG2 [" + (messageCode != null ? "messageCode=" + messageCode + ", " : "") + (
      messageTextI18N != null ? "messageTextI18N=" + messageTextI18N + ", " : "") + (
      dynamicMessageDataList != null ? "dynamicMessageDataList=" + dynamicMessageDataList + ", "
        : "")
      + (messageDescription != null ? "messageDescription=" + messageDescription + ", " : "") + (
      keyNamespace != null ? "keyNamespace=" + keyNamespace + ", " : "") + (ebmpLang != null ?
      "ebmpLang=" + ebmpLang : "") + "]";
  }

}
