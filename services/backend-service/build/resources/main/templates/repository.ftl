package ${packageName}.repository;

import ${packageName}.entity.${entityName};
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ${entityName}Repository extends JpaRepository<${entityName}, Long> {
    // Custom query methods can be added here
}
