package edu.kit.ipd.crowdcontrol.objectservice.moneytransfer;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.Worker;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.GiftCodeRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.WorkerRecord;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.WorkerBalanceOperations;
import edu.kit.ipd.crowdcontrol.objectservice.database.operations.WorkerOperations;
import edu.kit.ipd.crowdcontrol.objectservice.mail.MailHandler;
import edu.kit.ipd.crowdcontrol.objectservice.template.Template;
import org.jooq.Result;
import org.jooq.util.derby.sys.Sys;
import org.junit.Before;
import org.junit.Test;
import org.mockito.stubbing.Answer;

import javax.mail.Message;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

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
        WorkerRecord worker2 = mock(WorkerRecord.class);

        doReturn("pseipd@gmail.com").when(worker0).getEmail();
        doReturn("pse2016@web.de").when(worker1).getEmail();
        doReturn("pseipd@web.de").when(worker2).getEmail();

        doReturn(0).when(worker0).getIdWorker();
        doReturn(1).when(worker1).getIdWorker();
        doReturn(2).when(worker2).getIdWorker();

        doReturn(30).when(payops).getBalance(anyInt());

        Result<WorkerRecord> workerList = mock(Result.class);
        Iterator<WorkerRecord> it = mock(Iterator.class);
        when(it.hasNext()).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(false);
        when(it.next()).thenReturn(worker0).thenReturn(worker1).thenReturn(worker2);
        when(workerList.iterator()).thenReturn(it);

        doReturn(workerList).when(workerops).getWorkerWithCreditBalanceGreaterOrEqual(anyInt());

        GiftCodeRecord code0 = new GiftCodeRecord();
        GiftCodeRecord code1 = new GiftCodeRecord();
        GiftCodeRecord code2 = new GiftCodeRecord();
        GiftCodeRecord code3 = new GiftCodeRecord();
        GiftCodeRecord code4 = new GiftCodeRecord();


        code0.setCode("QWER-TZUI");
        code1.setCode("ASDF-GHJK");
        code2.setCode("FOOBAR-BAZ");
        code3.setCode("FOO-BAR");
        code4.setCode("YXCV-BNM");


        code0.setAmount(30);
        code1.setAmount(25);
        code2.setAmount(15);
        code3.setAmount(15);
        code4.setAmount(10);

        code0.setIdGiftCode(0);
        code1.setIdGiftCode(1);
        code2.setIdGiftCode(2);
        code3.setIdGiftCode(3);
        code4.setIdGiftCode(4);

        LinkedList<GiftCodeRecord> codeList = new LinkedList<>();
        doReturn(codeList).when(payops).getUnusedGiftCodes();

        codeList.addLast(code0);
        codeList.addLast(code1);
        codeList.addLast(code2);
        codeList.addLast(code3);
        codeList.addLast(code4);

        Answer answer0 = invocation -> {
            codeList.remove(code0);
            return true;
        };

        Answer answer1 = invocation -> {
            codeList.remove(code1);
            return true;
        };

        Answer answer2 = invocation -> {
            codeList.remove(code2);
            return true;
        };

        Answer answer3 = invocation -> {
            codeList.remove(code3);
            return true;
        };

        Answer answer4 = invocation -> {
            codeList.remove(code4);
            return true;
        };

        doReturn(new Message[0]).when(handler).fetchUnseen(any());

        doAnswer(answer0).when(payops).addDebit(anyInt(), anyInt(), eq(code0.getIdGiftCode()));
        doAnswer(answer1).when(payops).addDebit(anyInt(), anyInt(), eq(code1.getIdGiftCode()));
        doAnswer(answer2).when(payops).addDebit(anyInt(), anyInt(), eq(code2.getIdGiftCode()));
        doAnswer(answer3).when(payops).addDebit(anyInt(), anyInt(), eq(code3.getIdGiftCode()));
        doAnswer(answer4).when(payops).addDebit(anyInt(), anyInt(), eq(code4.getIdGiftCode()));

        StringBuilder content = new StringBuilder();
        try {
            FileReader file = new FileReader("src/main/resources/PaymentMessage.txt");
            BufferedReader reader = new BufferedReader(file);

            String messageLine;
            while ((messageLine = reader.readLine()) != null) {
                content.append(messageLine);
                content.append(System.getProperty("line.separator"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map<String, String> map1 = new HashMap<>();
        map1.put("GiftCodes", "QWER-TZUI" + System.getProperty("line.separator"));

        Map<String, String> map2 = new HashMap<>();
        map2.put("GiftCodes", "ASDF-GHJK" + System.getProperty("line.separator"));

        Map<String, String> map3 = new HashMap<>();
        map3.put("GiftCodes", "FOOBAR-BAZ" + System.getProperty("line.separator") + "FOO-BAR" + System.getProperty("line.separator"));

        String codesWorker1 = Template.apply(content.toString(), map1);
        String codesWorker2 = Template.apply(content.toString(), map2);
        String codesWorker3 = Template.apply(content.toString(), map3);

        mng.submitGiftCodes();

        verify(handler).sendMail("pseipd@gmail.com","Your payment for your Crowdworking", codesWorker1);
        verify(handler).sendMail("pse2016@web.de", "Your payment for your Crowdworking", codesWorker2);
        verify(handler).sendMail("pseipd@web.de", "Your payment for your Crowdworking", codesWorker3);
    }
}
