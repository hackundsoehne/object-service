package edu.kit.ipd.crowdcontrol.objectservice.moneytransfer;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.Payment;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.Worker;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.GiftCodeRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.PaymentRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.WorkerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.PaymentOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.WokerOperations;
import edu.kit.ipd.crowdcontrol.objectservice.mail.MailHandler;
import org.junit.Before;
import org.junit.Test;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import static org.mockito.Mockito.*;

/**
 * Tests the MoneyTransferManager
 * @author Felix Rittler
 */
public class MoneyTransferManagerTest {

    MoneyTransferManager mng;
    MailHandler handler;
    PaymentOperations payops;
    WokerOperations workerops;

    @Before
    public void setUp() throws Exception{

        handler = mock(MailHandler.class);
        payops = mock(PaymentOperations.class);
        workerops = mock(WokerOperations.class);
        mng = new MoneyTransferManager(handler, payops, workerops, null);
    }

    @Test
    public void testPayOff() throws Exception {
        WorkerRecord worker0 = mock(WorkerRecord.class);
        WorkerRecord worker1 = mock(WorkerRecord.class);
        when(worker0.getEmail()).thenReturn("pseipd@gmail.com");
        when(worker1.getEmail()).thenReturn("pseipd@gmail.com");
        when(worker0.getCreditBalance()).thenReturn(30);
        when(worker1.getCreditBalance()).thenReturn(30);
        List<WorkerRecord> workerList = new LinkedList<WorkerRecord>();
        workerList.add(worker0);
        workerList.add(worker1);
        when(workerops.getWorkersWithCreditBalanceGreaterThan(anyInt())).thenReturn(workerList);
        GiftCodeRecord code0 = mock(GiftCodeRecord.class);
        GiftCodeRecord code1 = mock(GiftCodeRecord.class);
        GiftCodeRecord code2 = mock(GiftCodeRecord.class);
        when(code0.getCode()).thenReturn("qwer");
        when(code1.getCode()).thenReturn("asdf");
        when(code2.getCode()).thenReturn("yxcv");
        when(code0.getAmount()).thenReturn(30);
        when(code1.getAmount()).thenReturn(25);
        when(code2.getAmount()).thenReturn(10);
        LinkedList<GiftCodeRecord> codeList = new LinkedList<GiftCodeRecord>();
        codeList.addLast(code0);
        codeList.addLast(code1);
        codeList.addLast(code2);
        doReturn(codeList.remove(code0)).when(payops).markGiftCodeAsUsed(code0, any());
        doReturn(codeList.remove(code1)).when(payops).markGiftCodeAsUsed(code1, any());
        doReturn(codeList.remove(code2)).when(payops).markGiftCodeAsUsed(code2, any());
        String message = "Dear Worker, <br/>We thank you for your work and send you in this mail the the Amazon giftcodes you earned. " +
                "You can redeem them <a href=\"https://www.amazon.de/gc/redeem/ref=gc_redeem_new_exp\">here!</a>" +
                "Please note, that the amount of the giftcodes can be under the amount of money you earned. " +
                "The giftcodes with corresponding amount of money first have to be bought, or if the amount of money missing is below 15ct, you have to complete more tasks to get the complete amount of money.<br/>" +
                "qwer</br>";
        verify(handler).sendMail(null,"Your Payment for your Crowdworking");
        mng.payOff();



    }
}
