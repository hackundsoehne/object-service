package edu.ipd.kit.crowdcontrol.objectservice.database.operations;

import edu.ipd.kit.crowdcontrol.objectservice.database.model.Tables;
import edu.ipd.kit.crowdcontrol.objectservice.database.model.tables.records.PaymentRecord;
import org.jooq.DSLContext;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * @author LeanderK
 * @version 1.0
 */
public class PaymentOperation extends AbstractOperation {
    protected PaymentOperation(DSLContext create) {
        super(create);
    }


    public int preparePayment(int workerID, int experimentId, LocalDateTime lastPayment) {
        //TODO: check for for no other payments after last
        return create.insertInto(Tables.PAYMENT)
                .set(new PaymentRecord(null, workerID, experimentId, null))
                .returning()
                .fetchOne()
                .getIdpayment();
    }

    public boolean completedPayment(int paymentID) {
        return create.executeUpdate(
                new PaymentRecord(paymentID, null, null, Timestamp.valueOf(LocalDateTime.now()))) == 1;
    }
}
