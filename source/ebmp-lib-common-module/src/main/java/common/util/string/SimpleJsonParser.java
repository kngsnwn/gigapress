package common.util.string;

/**
 * <pre>
 *  간단한 JSON 파싱용 클래스.
 *                JSON 문자열에서 필요한 데이터를 확인하기 위해 사용한다.
 * </pre>
 */
public class SimpleJsonParser {

  public static String getJsonValue(String keyName, String jsonString) {
    int keyNameIndex = jsonString.indexOf(keyName + "\":");

    //키 네임을 찾지 못했다면 해당 키값이 존재하지 않는 것으로 간주한다.
    if (keyNameIndex == -1) {
      return null;
    }

    String tempValue = jsonString.substring(keyNameIndex + keyName.length() + 2);

    tempValue = tempValue.substring(0, getIndexAtLastSplitCharacter(tempValue));

    return removeAllPlainDoubleQuotations(tempValue);
  }

  private static int getIndexAtLastSplitCharacter(String tempValue) {
    int index1 = tempValue.indexOf("}");
    int index2 = tempValue.indexOf(",");
    int index3 = tempValue.indexOf("\"");

    int minIndex = 0;

    if (0 < index1) {
      minIndex = index1;
    }

    if (0 < index2 && minIndex > index2) {
      minIndex = index2;
    }

    if (0 < index3 && minIndex > index3) {
      minIndex = index3;
    }

    return minIndex;
  }

  private static String removeAllPlainDoubleQuotations(String tempValue) {
    String tempStr = tempValue;

    while (tempStr.indexOf("\"") != -1) {
      tempStr = tempStr.replace("\"", "");
    }

    return tempStr;
  }
}
