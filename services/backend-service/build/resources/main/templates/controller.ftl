package ${packageName}.controller;

import ${packageName}.dto.${entityName}Request;
import ${packageName}.dto.${entityName}Response;
import ${packageName}.service.${entityName}Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${apiPath}")
@RequiredArgsConstructor
@Tag(name = "${entityName}", description = "${entityName} management APIs")
public class ${entityName}Controller {

    private final ${entityName}Service ${entityName?uncap_first}Service;

    @GetMapping
    @Operation(summary = "Get all ${entityName}s")
    public ResponseEntity<List<${entityName}Response>> getAll() {
        return ResponseEntity.ok(${entityName?uncap_first}Service.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get ${entityName} by ID")
    public ResponseEntity<${entityName}Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(${entityName?uncap_first}Service.findById(id));
    }

    @PostMapping
    @Operation(summary = "Create new ${entityName}")
    public ResponseEntity<${entityName}Response> create(@RequestBody ${entityName}Request request) {
        return ResponseEntity.ok(${entityName?uncap_first}Service.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update ${entityName}")
    public ResponseEntity<${entityName}Response> update(@PathVariable Long id, @RequestBody ${entityName}Request request) {
        return ResponseEntity.ok(${entityName?uncap_first}Service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete ${entityName}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ${entityName?uncap_first}Service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
