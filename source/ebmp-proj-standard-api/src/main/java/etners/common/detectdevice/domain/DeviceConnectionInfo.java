package etners.common.detectdevice.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class DeviceConnectionInfo implements Serializable {

  private static final long serialVersionUID = -2197817524051186585L;

  private String uId;

  private String uCompCode;

  private String uCountryCode;

  private String connectDeviceType;

  private String userAgent;

  private String mobile;

  private String tablet;

  private String phone;

  private String os;

  private String isIPhone;

  private String isBot;

  private String webkit;

  private String build;

  public DeviceConnectionInfo() {
  }

  public DeviceConnectionInfo(String connectDeviceType, String userAgent, String mobile,
      String tablet, String phone
      , String os, String isIPhone, String isBot, String webkit, String build) {
    this.connectDeviceType = connectDeviceType;
    this.userAgent = userAgent;
    this.mobile = mobile;
    this.tablet = tablet;
    this.phone = phone;
    this.os = os;
    this.isIPhone = isIPhone;
    this.isBot = isBot;
    this.webkit = webkit;
    this.build = build;
  }

  public boolean isMobile() {
    return !isDesktop();
  }

  public boolean isDesktop() {
    return mobile == null && tablet == null;
  }
}
