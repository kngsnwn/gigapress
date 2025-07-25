package etners.standard.mvc.jpa.epcMenuMst.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import etners.standard.mvc.jpa.epcMenuMst.entity.EpcMenuMst;
import etners.standard.mvc.jpa.epcMenuMst.entity.EpcMenuMstPK;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;


public interface EpcMenuMstRepository extends JpaRepository<EpcMenuMst, EpcMenuMstPK>, QuerydslPredicateExecutor<EpcMenuMst> {
}
