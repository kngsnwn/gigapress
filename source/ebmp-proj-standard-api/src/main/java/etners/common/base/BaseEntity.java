package etners.common.base;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Convert;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.YesNoConverter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity {

  @Column(name = "SEQ_NO")
  @Schema(description = "조회순서")
  private long seqNo = 1;

  @Column(name = "USE_YN")
  @Schema(description = "사용여부")
  @Convert(converter = YesNoConverter.class)
  private boolean useYn = true;

  @CreatedBy
  @Column(name = "FRST_RGST_ID", updatable = false)
  @Schema(description = "최초등록자")
  private String frstRgstId;

  @CreationTimestamp
  @Column(name = "FRST_RGST_DT", updatable = false)
  @Schema(description = "최초등록일시")
  private LocalDateTime frstRgstDt;

  @LastModifiedBy
  @Column(name = "LAST_MDFY_ID")
  @Schema(description = "최종수정자")
  private String lastMdfyId;

  @UpdateTimestamp
  @Column(name = "LAST_MDFY_DT")
  @Schema(description = "최종수정일시")
  private LocalDateTime lastMdfyDt;


  public void enable() {
    this.useYn = true;
  }
  public void disable() {
    this.useYn = false;
  }

  public void updateUseYn(boolean useYn) {
    this.useYn = useYn;
  }
}
