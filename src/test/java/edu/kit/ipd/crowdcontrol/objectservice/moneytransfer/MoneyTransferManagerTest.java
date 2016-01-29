package edu.kit.ipd.crowdcontrol.objectservice.moneytransfer;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.Worker;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.GiftCodeRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.WorkerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.WorkerBalanceOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.WorkerOperations;
import edu.kit.ipd.crowdcontrol.objectservice.mail.MailHandler;
import org.jooq.Result;
import org.junit.Before;
import org.junit.Test;
import org.mockito.stubbing.Answer;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Tests the MoneyTransferManager
 * @author Felix Rittler
 */
public class MoneyTransferManagerTest {

    MoneyTransferManager mng;
    MailHandler handler;
    WorkerBalanceOperations payops;
    WorkerOperations workerops;

    @Before
    public void setUp() throws Exception{

        handler = mock(MailHandler.class);
        payops = mock(WorkerBalanceOperations.class);
        workerops = mock(WorkerOperations.class);
        mng = new MoneyTransferManager(handler, payops, workerops, null);
    }

    @Test
    public void testPayOff1Worker() throws Exception {
        WorkerRecord worker0 = mock(WorkerRecord.class);
        WorkerRecord worker1 = mock(WorkerRecord.class);

        doReturn("pseipd@gmail.com").when(worker0).getEmail();
        doReturn("pseipd@gmail.com").when(worker1).getEmail();

        doReturn(0).when(worker0).getIdWorker();
        doReturn(1).when(worker1).getIdWorker();

        doReturn(30).when(payops).getBalance(anyInt());

        Result<WorkerRecord> workerList = mock(Result.class);
        Iterator<WorkerRecord> it = mock(Iterator.class);
        when(it.hasNext()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(it.next()).thenReturn(worker0).thenReturn(worker1);
        when(workerList.iterator()).thenReturn(it);

        workerList.add(worker0);
        workerList.add(worker1);

        doReturn(workerList).when(workerops).getWorkerWithCreditBalanceGreaterOrEqual(anyInt());

        GiftCodeRecord code0 = new GiftCodeRecord();
        GiftCodeRecord code1 = new GiftCodeRecord();
        GiftCodeRecord code2 = new GiftCodeRecord();

        code0.setCode("qwer");
        code1.setCode("asdf");
        code2.setCode("yxcv");

        code0.setAmount(30);
        code1.setAmount(25);
        code2.setAmount(10);

        LinkedList<GiftCodeRecord> codeList = new LinkedList<>();

        codeList.addLast(code0);
        codeList.addLast(code1);
        codeList.addLast(code2);

        Answer answer0 = invocation -> {
            codeList.remove(code0);
            return null;
        };

        Answer answer1 = invocation -> {
            codeList.remove(code0);
            return null;
        };

        Answer answer2 = invocation -> {
            codeList.remove(code0);
            return null;
        };

        doAnswer(answer0).when(payops).addDebit(any(), any(), any(), code0.getIdGiftCode());
        doAnswer(answer1).when(payops).addDebit(any(), any(), any(), code1.getIdGiftCode());
        doAnswer(answer2).when(payops).addDebit(any(), any(), any(), code2.getIdGiftCode());

        String message = "Dear Worker, <br/>We thank you for your work and send you in this mail the the Amazon giftcodes you earned. " +
                "You can redeem them <a href=\"https://www.amazon.de/gc/redeem/ref=gc_redeem_new_exp\">here!</a>" +
                "Please note, that the amount of the giftcodes can be under the amount of money you earned. " +
                "The giftcodes with corresponding amount of money first have to be bought, or if the amount of money missing is below 15ct, you have to complete more tasks to get the complete amount of money.<br/>" +
                "qwer</br>";
        verify(handler).sendMail(null,"Your Payment for your Crowdworking", message);
        mng.payOff();
    }
}
