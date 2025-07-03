package ${packageName}.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class ${entityName}${dtoType?cap_first} {
<#list fields as field>
    <#if field.required>
    @NotNull(message = "${field.name} is required")
    </#if>
    <#if field.validation??>
    ${field.validation}
    </#if>
    private ${field.type} ${field.name};
</#list>
}
