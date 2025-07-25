package etners.common.basetype;

import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(BaseController.class);

  /**
   * <pre>
   * 메소드명	: getStackTrace
   * 작성자	: oxide
   * 작성일	: 2018. 7. 9.
   * 설명	: 자바 리플렉션 API를 활용해서 현재 실행 중인 메서드에 대한 로그를 출력하는 메소드.
   *        인터넷에 공유된 소스를 가져왔다.
   * </pre>
   *
   * @param className       YourClassName.class.getName()
   * @param classSimpleName YourClassName.class.getSimpleName()
   */
  public void getStackTrace(final String className, final String classSimpleName) {
    final StackTraceElement[] steArray = Thread.currentThread().getStackTrace();
    int index = 0;

    for (StackTraceElement ste : steArray) {
      if (ste.getClassName().equals(className)) {
        break;
      }

      index++;
    }

    if (index >= steArray.length) {
      // Little Hacky
      LOGGER.debug(classSimpleName, Arrays.toString(
          new String[]{steArray[3].getMethodName(), String.valueOf(steArray[3].getLineNumber())}));
    } else {
      // Legitimate
      LOGGER.debug(classSimpleName, Arrays.toString(new String[]{steArray[index].getMethodName(),
          String.valueOf(steArray[index].getLineNumber())}));
    }
  }

}
