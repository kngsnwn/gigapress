package etners.ebmp.lib.jpa.entity.epc.epcMailHis;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(
  name = "EPC_MAIL_HIS"
)
public class EpcMailHis implements Serializable {

  private static final long serialVersionUID = -7671422125912364240L;
  @Id
  @GenericGenerator(
    name = "MAIL_HIS_SEQ",
    strategy = "etners.ebmp.lib.jpa.generator.SendEmailAndSmsIdGenerator"
  )
  @GeneratedValue(
    generator = "MAIL_HIS_SEQ"
  )
  @SequenceGenerator(
    name = "MAIL_HIS_SEQ",
    sequenceName = "mail_his_seq",
    allocationSize = 1
  )
  @Column(
    name = "SN",
    nullable = false
  )
  private String sn;
  @Column(
    name = "SOL_CD",
    nullable = false
  )
  private String solCd;
  @Column(
    name = "SEND_EMAIL"
  )
  private String sendEmail;
  @Column(
    name = "RECV_EMAIL"
  )
  private String recvEmail;
  @Column(
    name = "MAIL_TITLE"
  )
  private String mailTitle;
  @Column(
    name = "MAIL_DESC"
  )
  private String mailDesc;
  @Column(
    name = "SEND_DT"
  )
  private LocalDateTime sendDt;
  @Column(
    name = "CUSTOM_KEY"
  )
  private String customKey;
  @Column(
    name = "LAST_MDFY_ID"
  )
  private String lastMdfyId;
  @CreationTimestamp
  @Column(
    name = "LAST_MDFY_DT"
  )
  private LocalDateTime lastMdfyDt;

  public EpcMailHis() {
  }

  public String getSn() {
    return this.sn;
  }

  public String getSolCd() {
    return this.solCd;
  }

  public String getSendEmail() {
    return this.sendEmail;
  }

  public String getRecvEmail() {
    return this.recvEmail;
  }

  public String getMailTitle() {
    return this.mailTitle;
  }

  public String getMailDesc() {
    return this.mailDesc;
  }

  public LocalDateTime getSendDt() {
    return this.sendDt;
  }

  public String getCustomKey() {
    return this.customKey;
  }

  public String getLastMdfyId() {
    return this.lastMdfyId;
  }

  public LocalDateTime getLastMdfyDt() {
    return this.lastMdfyDt;
  }

  public void setSn(String sn) {
    this.sn = sn;
  }

  public void setSolCd(String solCd) {
    this.solCd = solCd;
  }

  public void setSendEmail(String sendEmail) {
    this.sendEmail = sendEmail;
  }

  public void setRecvEmail(String recvEmail) {
    this.recvEmail = recvEmail;
  }

  public void setMailTitle(String mailTitle) {
    this.mailTitle = mailTitle;
  }

  public void setMailDesc(String mailDesc) {
    this.mailDesc = mailDesc;
  }

  public void setSendDt(LocalDateTime sendDt) {
    this.sendDt = sendDt;
  }

  public void setCustomKey(String customKey) {
    this.customKey = customKey;
  }

  public void setLastMdfyId(String lastMdfyId) {
    this.lastMdfyId = lastMdfyId;
  }

  public void setLastMdfyDt(LocalDateTime lastMdfyDt) {
    this.lastMdfyDt = lastMdfyDt;
  }

  public String toString() {
    return "EpcMailHis(sn=" + this.getSn() + ", solCd=" + this.getSolCd() + ", sendEmail=" + this.getSendEmail() + ", recvEmail=" + this.getRecvEmail() + ", mailTitle=" + this.getMailTitle() + ", mailDesc=" + this.getMailDesc() + ", sendDt=" + this.getSendDt() + ", customKey=" + this.getCustomKey() + ", lastMdfyId=" + this.getLastMdfyId() + ", lastMdfyDt=" + this.getLastMdfyDt() + ")";
  }

  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof EpcMailHis other)) {
      return false;
    } else {
      if (!other.canEqual(this)) {
        return false;
      } else {
        Object this$sn = this.getSn();
        Object other$sn = other.getSn();
        if (this$sn == null) {
          if (other$sn != null) {
            return false;
          }
        } else if (!this$sn.equals(other$sn)) {
          return false;
        }

        Object this$solCd = this.getSolCd();
        Object other$solCd = other.getSolCd();
        if (this$solCd == null) {
          if (other$solCd != null) {
            return false;
          }
        } else if (!this$solCd.equals(other$solCd)) {
          return false;
        }

        Object this$sendEmail = this.getSendEmail();
        Object other$sendEmail = other.getSendEmail();
        if (this$sendEmail == null) {
          if (other$sendEmail != null) {
            return false;
          }
        } else if (!this$sendEmail.equals(other$sendEmail)) {
          return false;
        }

        Object this$recvEmail = this.getRecvEmail();
        Object other$recvEmail = other.getRecvEmail();
        if (this$recvEmail == null) {
          if (other$recvEmail != null) {
            return false;
          }
        } else if (!this$recvEmail.equals(other$recvEmail)) {
          return false;
        }

        Object this$mailTitle = this.getMailTitle();
        Object other$mailTitle = other.getMailTitle();
        if (this$mailTitle == null) {
          if (other$mailTitle != null) {
            return false;
          }
        } else if (!this$mailTitle.equals(other$mailTitle)) {
          return false;
        }

        Object this$mailDesc = this.getMailDesc();
        Object other$mailDesc = other.getMailDesc();
        if (this$mailDesc == null) {
          if (other$mailDesc != null) {
            return false;
          }
        } else if (!this$mailDesc.equals(other$mailDesc)) {
          return false;
        }

        Object this$sendDt = this.getSendDt();
        Object other$sendDt = other.getSendDt();
        if (this$sendDt == null) {
          if (other$sendDt != null) {
            return false;
          }
        } else if (!this$sendDt.equals(other$sendDt)) {
          return false;
        }

        Object this$customKey = this.getCustomKey();
        Object other$customKey = other.getCustomKey();
        if (this$customKey == null) {
          if (other$customKey != null) {
            return false;
          }
        } else if (!this$customKey.equals(other$customKey)) {
          return false;
        }

        Object this$lastMdfyId = this.getLastMdfyId();
        Object other$lastMdfyId = other.getLastMdfyId();
        if (this$lastMdfyId == null) {
          if (other$lastMdfyId != null) {
            return false;
          }
        } else if (!this$lastMdfyId.equals(other$lastMdfyId)) {
          return false;
        }

        Object this$lastMdfyDt = this.getLastMdfyDt();
        Object other$lastMdfyDt = other.getLastMdfyDt();
        if (this$lastMdfyDt == null) {
          return other$lastMdfyDt == null;
        } else {
          return this$lastMdfyDt.equals(other$lastMdfyDt);
        }
      }
    }
  }

  protected boolean canEqual(Object other) {
    return other instanceof EpcMailHis;
  }

  public int hashCode() {
    int PRIME = 59;
    int result = 1;
    Object $sn = this.getSn();
    result = result * 59 + ($sn == null ? 43 : $sn.hashCode());
    Object $solCd = this.getSolCd();
    result = result * 59 + ($solCd == null ? 43 : $solCd.hashCode());
    Object $sendEmail = this.getSendEmail();
    result = result * 59 + ($sendEmail == null ? 43 : $sendEmail.hashCode());
    Object $recvEmail = this.getRecvEmail();
    result = result * 59 + ($recvEmail == null ? 43 : $recvEmail.hashCode());
    Object $mailTitle = this.getMailTitle();
    result = result * 59 + ($mailTitle == null ? 43 : $mailTitle.hashCode());
    Object $mailDesc = this.getMailDesc();
    result = result * 59 + ($mailDesc == null ? 43 : $mailDesc.hashCode());
    Object $sendDt = this.getSendDt();
    result = result * 59 + ($sendDt == null ? 43 : $sendDt.hashCode());
    Object $customKey = this.getCustomKey();
    result = result * 59 + ($customKey == null ? 43 : $customKey.hashCode());
    Object $lastMdfyId = this.getLastMdfyId();
    result = result * 59 + ($lastMdfyId == null ? 43 : $lastMdfyId.hashCode());
    Object $lastMdfyDt = this.getLastMdfyDt();
    result = result * 59 + ($lastMdfyDt == null ? 43 : $lastMdfyDt.hashCode());
    return result;
  }
}