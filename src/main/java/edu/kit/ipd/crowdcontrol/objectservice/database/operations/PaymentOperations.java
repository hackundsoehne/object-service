package edu.kit.ipd.crowdcontrol.objectservice.database.operations;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.GiftCodeRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.PaymentRecord;

import org.jooq.DSLContext;

import java.util.List;

/**
 * @author LeanderK
 * @version 1.0
 */
public class PaymentOperations extends AbstractOperations {
    protected PaymentOperations(DSLContext create) {
        super(create);
    }

    public List<GiftCodeRecord> getAllGiftCodes() {
        //TODO
        return null;
    }


}