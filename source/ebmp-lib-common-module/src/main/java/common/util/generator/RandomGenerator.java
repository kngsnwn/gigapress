package common.util.generator;

public class RandomGenerator {

  private static final char[] upperCase = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

  private static final char[] lowerCase = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

  private static final char[] spacialCharacter = {33, 35, 36, 37, 38, 40, 41, 42, 64, 94}; //!, #, $, %, &, (, ), *, @, ^

  public static String generateRandomText(int textLength) {
    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < textLength; i++) {

      int randomNum = generateRandomNumberWithinSpecifiedRange(1, 3);

      switch (randomNum) {
        case 1:
          sb.append(upperCase[generateRandomNumberWithinSpecifiedRange(1, upperCase.length - 1)]);
          break;
        case 2:
          sb.append(lowerCase[generateRandomNumberWithinSpecifiedRange(1, lowerCase.length - 1)]);
          break;
        case 3:
          sb.append(generateRandomNumberWithinSpecifiedRange(0, 9));
          break;
        default:
          throw new IllegalArgumentException("잘못된 값입니다.");
      }
    }

    String firstRandomText = sb.toString();

    return firstRandomText;
  }

  public static String generateRandomTextIncludeSpecialCharacter(int textLength) {
    String randomText = generateRandomText(textLength - 1) + generateRandomSpecialCharacter();

    return suffleText(randomText);
  }

  /**
   * 설명	: 지정된 범위의 정수값을 랜덤하게 생성하는 메소드
   */
  public static int generateRandomNumberWithinSpecifiedRange(int minRangeNum, int maxRangeNum) {
    return (int) (Math.random() * (maxRangeNum - minRangeNum + 1)) + minRangeNum;
  }

  public static String generateRandomSpecialCharacter() {
    int randomNum = generateRandomNumberWithinSpecifiedRange(0, spacialCharacter.length - 1);

    char specialChar = spacialCharacter[randomNum];

    return String.valueOf(specialChar);
  }

  public static String suffleText(String text) {
    char[] charArr = text.toCharArray();

    for (int i = 0; i < 100; i++) {
      int randomIndex = generateRandomNumberWithinSpecifiedRange(0, text.length() - 1);

      char temp = charArr[randomIndex];

      charArr[randomIndex] = charArr[0];

      charArr[0] = temp;
    }

    return new String(charArr);
  }

}
