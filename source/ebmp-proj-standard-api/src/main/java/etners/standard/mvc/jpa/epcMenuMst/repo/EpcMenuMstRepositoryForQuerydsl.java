package etners.standard.mvc.jpa.epcMenuMst.repo;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;


@Repository
@RequiredArgsConstructor
public class EpcMenuMstRepositoryForQuerydsl {

   private final JPAQueryFactory queryFactory;

}
