package ${packageName}.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
<#list fields as field>
<#if field.type == "LocalDate" || field.type == "LocalDateTime">
import java.time.${field.type};
</#if>
</#list>

@Entity
@Table(name = "${entityName?lower_case}s")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"createdAt", "updatedAt"})
@EqualsAndHashCode(of = "id")
public class ${entityName} {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

<#list fields as field>
    <#if field.unique>
    @Column(unique = true<#if field.required>, nullable = false</#if>)
    <#elseif field.required>
    @Column(nullable = false)
    <#else>
    @Column
    </#if>
    private ${field.type} ${field.name};

</#list>
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version;
}
