package etners.standard;

import com.querydsl.core.BooleanBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EbmpWebStandardApplicationTests {


  @Test
  @DisplayName("예상값 결과값 일치시 true")
  void staticsTest() {
    String workspaceCd = "21";
    System.out.println("workspaceCd : "+ workspaceCd);
    Assertions.assertEquals(workspaceCd,"21");
  }


}
