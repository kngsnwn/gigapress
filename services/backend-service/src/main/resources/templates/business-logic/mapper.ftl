package ${packageName}.mapper;

import ${packageName}.entity.${entityName};
import ${packageName}.dto.${entityName}CreateDto;
import ${packageName}.dto.${entityName}UpdateDto;
import ${packageName}.dto.${entityName}ResponseDto;
import org.springframework.stereotype.Component;

@Component
public class ${entityName}Mapper {

    public ${entityName} toEntity(${entityName}CreateDto dto) {
        if (dto == null) {
            return null;
        }

        return ${entityName}.builder()
<#list fields as field>
                .${field.name}(dto.get${field.name?cap_first}())
</#list>
                .build();
    }

    public ${entityName}ResponseDto toResponseDto(${entityName} entity) {
        if (entity == null) {
            return null;
        }

        ${entityName}ResponseDto dto = new ${entityName}ResponseDto();
        dto.setId(entity.getId());
<#list fields as field>
        dto.set${field.name?cap_first}(entity.get${field.name?cap_first}());
</#list>
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        
        return dto;
    }

    public void updateEntityFromDto(${entityName}UpdateDto dto, ${entityName} entity) {
        if (dto == null || entity == null) {
            return;
        }

<#list fields as field>
        if (dto.get${field.name?cap_first}() != null) {
            entity.set${field.name?cap_first}(dto.get${field.name?cap_first}());
        }
</#list>
    }
}
