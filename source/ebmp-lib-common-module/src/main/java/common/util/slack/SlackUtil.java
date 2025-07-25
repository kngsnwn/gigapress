package common.util.slack;


import static com.slack.api.model.block.Blocks.section;
import static java.util.Objects.nonNull;

import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.model.block.Blocks;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.RichTextBlock;
import com.slack.api.model.block.composition.BlockCompositions;
import com.slack.api.model.block.composition.MarkdownTextObject;
import com.slack.api.model.block.element.RichTextPreformattedElement;
import com.slack.api.model.block.element.RichTextSectionElement;
import common.config.springprofile.DetectProductionModeProfile;
import common.util.date.LocalDateTimeUtil;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class SlackUtil {

  @Value("${slack.webhook.id}")
  private String CHANNEL_ID;

  @Value("${server.title}")
  private String serverTitle;

  private static final Logger LOGGER = LoggerFactory.getLogger(SlackUtil.class);
  private final MethodsClient methodsClient;


  /**
   * <pre>
   * 설명 : 에러 정보를 LayoutBlock에 추가하는 메서드
   *  </pre>
   *
   * @param layoutBlocks  - Slack LayoutBlock 리스트
   * @param schedulerName - 스케줄러 이름
   * @param desc          - 스케줄러 설명 텍스트
   */
  public void addErrorInfo(List<LayoutBlock> layoutBlocks, String schedulerName, String desc) {
    String leftMd = "*Mode:* " + getMode() + "\n*Name:* " + schedulerName + "\n*Desc:* " + desc;
    LocalDateTime var10000 = LocalDateTime.now();
    String rightMd = "*CurrentTime:* " + LocalDateTimeUtil.convertLocalDateTimeToString(var10000, "yyyy-MM-dd HH:mm:ss") + "\n*Result: Error*";
    MarkdownTextObject modeNameDescMarkdown = this.makeMarkdown(leftMd);
    MarkdownTextObject timeResultMarkdown = this.makeMarkdown(rightMd);
    this.addSectionDivide(layoutBlocks, List.of(modeNameDescMarkdown, timeResultMarkdown));
  }

  /**
   * <pre>
   * 설명 : 구분선을 LayoutBlock에 추가하는 메서드
   * </pre>
   *
   * @param layoutBlocks - Slack LayoutBlock 리스트
   */
  public void addDivider(List<LayoutBlock> layoutBlocks) {
    layoutBlocks.add(Blocks.divider());
  }


  /**
   * <pre>
   * 설명 : 에러 메시지를 LayoutBlock에 추가하는 메서드
   * </pre>
   *
   * @param layoutBlocks - Slack LayoutBlock 리스트
   * @param message      - 에러 메시지 텍스트
   * @param subTitle     - 에러 메시지 타이틀
   */
  public void addErrorMessage(List<LayoutBlock> layoutBlocks, String message, String subTitle) {
    MarkdownTextObject errorMessageMarkDown = MarkdownTextObject.builder().text("* " + subTitle + " :*\n").build();
    RichTextBlock errorMessageRichTextBlock = this.makeCodeBlock(message);
    layoutBlocks.add(Blocks.section((section) -> {
      return section.text(errorMessageMarkDown);
    }));
    layoutBlocks.add(errorMessageRichTextBlock);
  }

  /**
   * <pre>
   * 설명 : 주어진 MarkdownTextObject를 LayoutBlock에 추가하는 메서드
   * </pre>
   *
   * @param layoutBlocks   - Slack LayoutBlock 리스트
   * @param customMarkDown - MarkdownTextObject 인스턴스
   */
  public void addSection(List<LayoutBlock> layoutBlocks, MarkdownTextObject customMarkDown) {
    layoutBlocks.add(Blocks.section((section) -> {
      return section.text(customMarkDown);
    }));
  }

  /**
   * <pre>
   * 설명 : 메시지를 코드 블록으로 만들어 LayoutBlock에 추가하는 메서드
   * </pre>
   *
   * @param layoutBlocks - Slack LayoutBlock 리스트
   * @param message      - 메시지 텍스트
   */
  public void addCodeBlock(List<LayoutBlock> layoutBlocks, String message) {
    RichTextBlock richTextBlock = this.makeCodeBlock(message);
    layoutBlocks.add(richTextBlock);
  }

  /**
   * <pre>
   * 설명 : 서브 타이틀을 포함한 코드 블록을 LayoutBlock에 추가하는 메서드
   * </pre>
   *
   * @param layoutBlocks - Slack LayoutBlock 리스트
   * @param message      - 메시지 텍스트
   * @param subTitle     - 서브 타이틀
   */
  public void addCodeBlock(List<LayoutBlock> layoutBlocks, String message, String subTitle) {
    MarkdownTextObject codeBlockTitle = this.makeMarkdownBold(subTitle);
    this.addSection(layoutBlocks, codeBlockTitle);
    RichTextBlock richTextBlock = this.makeCodeBlock(message);
    layoutBlocks.add(richTextBlock);
  }


  /**
   * <pre>
   * 설명 : 헤더를 LayoutBlock에 추가하는 메서드
   * </pre>
   *
   * @param layoutBlocks - Slack LayoutBlock 리스트
   * @param headerText   - 헤더 텍스트
   */
  public void addHeader(List<LayoutBlock> layoutBlocks, String headerText) {
    layoutBlocks.add(Blocks.header((headerBlockBuilder) -> {
      return headerBlockBuilder.text(BlockCompositions.plainText(headerText));
    }));
    layoutBlocks.add(Blocks.divider());
  }

  /**
   * <pre>
   * 설명 : 서버 타이틀을 포함한 헤더를 LayoutBlock에 추가하는 메서드
   * </pre>
   *
   * @param layoutBlocks - Slack LayoutBlock 리스트
   * @param isError      - 에러 여부
   */
  public void addHeaderServerTitle(List<LayoutBlock> layoutBlocks, boolean isError) {
    if (isError) {
      this.serverTitle = this.serverTitle + " Error";
    }

    layoutBlocks.add(Blocks.header((headerBlockBuilder) -> {
      return headerBlockBuilder.text(BlockCompositions.plainText(this.serverTitle));
    }));
    layoutBlocks.add(Blocks.divider());
  }


  /**
   * <pre>
   * 설명 : 주어진 메시지를 코드 블록으로 만드는 메서드
   * </pre>
   *
   * @param message - 메시지 텍스트
   * @return RichTextBlock 인스턴스
   */
  public RichTextBlock makeCodeBlock(String message) {
    return Blocks.richText((richText) -> {
      return richText.elements(List.of(RichTextPreformattedElement.builder().elements(List.of(RichTextSectionElement.Text.builder().text(message).build())).build()));
    });
  }

  /**
   * <pre>
   * 설명 : 주어진 텍스트를 MarkdownTextObject로 만드는 메서드
   * </pre>
   *
   * @param mdObject - 텍스트
   * @return MarkdownTextObject 인스턴스
   */
  public MarkdownTextObject makeMarkdown(String mdObject) {
    return MarkdownTextObject.builder().text(mdObject).build();
  }

  /**
   * <pre>
   * 설명 : 주어진 타이틀을 볼드 처리한 MarkdownTextObject로 만드는 메서드
   * </pre>
   *
   * @param title - 타이틀 텍스트
   * @return MarkdownTextObject 인스턴스
   */
  public MarkdownTextObject makeMarkdownBold(String title) {
    return MarkdownTextObject.builder().text("* " + title + " :*\n").build();
  }


  /**
   * <pre>
   * 설명 : 주어진 LayoutBlock 리스트를 Slack 채널에 메시지로 전송하는 메서드
   * </pre>
   *
   * @param layoutBlocks - Slack LayoutBlock 리스트
   */
  public void sendMessage(List<LayoutBlock> layoutBlocks) {
    ChatPostMessageRequest chatPostMessageRequest = ChatPostMessageRequest.builder().channel(this.CHANNEL_ID).text("").blocks(layoutBlocks).build();

    try {
      this.methodsClient.chatPostMessage(chatPostMessageRequest);
    } catch (IOException | SlackApiException var4) {
      Exception slackApiException = var4;
      LOGGER.error(slackApiException.toString());
    }
  }

  /**
   * <pre>
   * 설명 : 현재 모드를 반환하는 메서드
   * </pre>
   *
   * @return 현재 모드 (PROD, DEV, LOCAL)
   */
  public static String getMode() {
    String mode = "";
    if (DetectProductionModeProfile.isProductionMode()) {
      mode = DetectProductionModeProfile.RuntimeMode.PROD.getRuntimeMode();
    } else if (DetectProductionModeProfile.isDevelopmentMode()) {
      mode = DetectProductionModeProfile.RuntimeMode.DEV.getRuntimeMode();
    } else {
      mode = DetectProductionModeProfile.RuntimeMode.LOCAL.getRuntimeMode();
    }

    return mode;
  }


  /**
   * <pre>
   * 설명 : 주어진 객체 리스트를 나누어 LayoutBlock에 추가하는 메서드  객체 2개 이후로 줄바꿈 후 추가됨
   * </pre>
   *
   * @param layoutBlocks - Slack LayoutBlock 리스트
   * @param objectList   - 객체 리스트
   */
  public void addSectionDivide(List<LayoutBlock> layoutBlocks, List<?> objectList) {
    if (nonNull(objectList) && !objectList.isEmpty()) {
      Object firstElement = objectList.get(0);
      if (firstElement instanceof MarkdownTextObject) {
        addSectionDivideMarkdownList(layoutBlocks, (List<MarkdownTextObject>) objectList);
      } else if (firstElement instanceof String) {
        addSectionDivideStrList(layoutBlocks, (List<String>) objectList);
      }
    } else {

    }
  }

  /**
   * <pre>
   * 설명 : 주어진 MarkdownTextObject 리스트를 나누어 LayoutBlock에 추가하는 메서드
   * </pre>
   *
   * @param layoutBlocks           - Slack LayoutBlock 리스트
   * @param markdownTextObjectList - MarkdownTextObject 리스트
   */
  public void addSectionDivideMarkdownList(List<LayoutBlock> layoutBlocks, List<MarkdownTextObject> markdownTextObjectList) {
    long cnt = Math.max(markdownTextObjectList.size() / 2, 1);
    List<MarkdownTextObject> fields = new ArrayList<>();
    for (int i = 0; i < cnt; i += 2) {
      int finalI = i;
      for (int j = 0; j < 2; j++) {
        fields.add(markdownTextObjectList.get(finalI + j));
      }
      layoutBlocks.add(section(section -> section.fields(List.of(fields.toArray(new MarkdownTextObject[0])))));
      fields.clear();
    }
  }

  /**
   * <pre>
   * 설명 : 주어진 문자열 리스트를 나누어 LayoutBlock에 추가하는 메서드
   * </pre>
   *
   * @param layoutBlocks   - Slack LayoutBlock 리스트
   * @param textObjectList - 문자열 리스트
   */
  public void addSectionDivideStrList(List<LayoutBlock> layoutBlocks, List<String> textObjectList) {
    long cnt = Math.max(textObjectList.size() / 2, 1);
    List<MarkdownTextObject> fields = new ArrayList<>();
    for (int i = 0; i < cnt; i += 2) {
      int finalI = i;
      for (int j = 0; j < 2; j++) {
        fields.add(makeMarkdown(textObjectList.get(finalI + j)));
      }
      layoutBlocks.add(section(section -> section.fields(List.of(fields.toArray(new MarkdownTextObject[0])))));
      fields.clear();
    }
  }

  /**
   * <pre>
   * 설명 : 이모지를 포함한 텍스트를 생성하여 LayoutBlock에 추가하는 메서드  slackUtil.addEmojiMessage(layoutBlocks, "Hello, World!", ":넵:");
   * </pre>
   *
   * @param layoutBlocks - Slack LayoutBlock 리스트
   * @param message      - 메시지 텍스트
   * @param emoji        - 추가할 이모지 (예: ":smile:")
   */
  public void addEmojiMessage(List<LayoutBlock> layoutBlocks, String message, String emoji) {
    String emojiMessage = message + " " + emoji;
    MarkdownTextObject markdownTextObject = makeMarkdown(emojiMessage);
    layoutBlocks.add(Blocks.section(section -> section.text(markdownTextObject)));
  }

  /**
   * <pre>
   * 이모지를  LayoutBlock에 추가하는 메서드
   * slackUtil.addEmojiMessage(layoutBlocks, " : 넵 : ");
   * </pre>
   *
   * @param layoutBlocks - Slack LayoutBlock 리스트
   * @param emoji        - 추가할 이모지 (예: ":smile:")
   */
  public void addEmoji(List<LayoutBlock> layoutBlocks, String emoji) {
    MarkdownTextObject markdownTextObject = makeMarkdown(emoji);
    layoutBlocks.add(Blocks.section(section -> section.text(markdownTextObject)));
  }

  /**
   * <pre>
   * 설명 : 기본 SystemInfo 전달용 클래스
   *
   * 예시	: SlackUtil.SystemInfo systemInfo = SlackUtil.SystemInfo.builder()
   *                  .mode(slackUtil.getMode())
   *                  .scheduleName("single_updateUtopiaUserInfo")
   *                  .description("유토피아 사용자 정보 업데이트")
   *                  .result("SUCCESS")
   *                  .startTime(LocalDateTimeUtil.convertLocalDateTimeToString(startTime, "yyyy-MM-dd HH:mm:ss"))
   *                  .endTime(LocalDateTimeUtil.convertLocalDateTimeToString(LocalDateTime.now(), "yyyy-MM-dd HH:mm:ss"))
   *                  .duration(duration)
   *                  .build();
   *  </pre>
   */
  @Getter
  public static class SystemInfo {

    private final String mode;
    private final String scheduleName;
    private final String description;
    private final String result;
    private final String startTime;
    private final String endTime;
    private final long duration;

    @Builder
    public SystemInfo(String mode, String scheduleName, String description, String result, String startTime, String endTime, long duration) {
      this.mode = mode;
      this.scheduleName = scheduleName;
      this.description = description;
      this.result = result;
      this.startTime = startTime;
      this.endTime = endTime;
      this.duration = duration;
    }

    public String toString() {
      return "*Mode:* " + this.mode + "\n*Name:* " + this.scheduleName + "\n*Desc:* " + this.description + "\n*Result:* " + this.result + "\n*Start Time:* " + this.startTime + "\n*End Time:* " + this.endTime + "\n*Duration:* " + (double) this.duration / 1000.0 + "초(" + this.duration + "ms)";
    }
  }


  /**
   * <pre>
   * 설명 : 기본 ErrorInfo 전달용 클래스
   * 예시	:SlackUtil.ErrorInfo.builder()
   *         .schedulerName("single_updateUtopiaUserInfo")
   *         .description("유토피아 사용자 정보 업데이트")
   *         .errorMessage(e.getMessage())
   *         .errorStack(getErrorStack(e))
   *         .build();
   *  </pre>
   */
  @Getter
  public static class ErrorInfo {

    private final String schedulerName;
    private final String description;
    private final String errorMessage;
    private final String errorStack;

    @Builder
    public ErrorInfo(String schedulerName, String description, String errorMessage, String errorStack) {
      this.schedulerName = schedulerName;
      this.description = description;
      this.errorMessage = errorMessage;
      this.errorStack = errorStack;
    }
  }

}

