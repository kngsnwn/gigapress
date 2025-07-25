package etners.common;


import etners.common.config.springprofile.DetectProductionModeProfile;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

  private static final Logger LOGGER = LoggerFactory.getLogger(CustomErrorController.class);

  @RequestMapping("/error")
  public String handleError(HttpServletRequest request, Model model) {
    LOGGER.info("/error!!!!");
    if (DetectProductionModeProfile.isDevelopmentModeOrProductionMode()) {
      return "/error";
    } else {
      return "/error";
    }
  }

}
