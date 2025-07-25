package etners.ebmp.lib.jpa.generator;


public class SendEmailAndSmsIdGenerator extends AbstractHibernateIdGenerator {

  @Override
  protected String getOracleSequenceName() {
    return "MAIL_HIS_SEQ";
  }

}
