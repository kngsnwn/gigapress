package etners.standard.mvc.jpa.epcUserMst.repo;


import etners.standard.mvc.jpa.epcUserMst.entity.epcUserMst.EpcUserMst;
import etners.standard.mvc.jpa.epcUserMst.entity.epcUserMst.EpcUserMstPK;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EpcUserMstRepository extends JpaRepository<EpcUserMst, EpcUserMstPK> {

  EpcUserMst findByUnqUserId(String unqUserId);
}
