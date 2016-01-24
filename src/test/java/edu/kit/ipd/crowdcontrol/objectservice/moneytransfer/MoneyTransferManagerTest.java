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

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.tls", "true");
        props.put("mail.smtp.ssl.checkserveridentity", "true");
        props.put("mail.store.protocol", "imap");
        props.put("mail.imap.host", "imap.gmail.com");
        props.put("mail.imap.port", "993");
        props.put("mail.imap.ssl", "true");
        props.put("mail.imap.ssl.enable", "true");
        java.security.Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
        props.put("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        Properties properties = new Properties();
        BufferedInputStream stream = new BufferedInputStream(new FileInputStream("src/test/resources/gmailLogin.properties"));
        properties.load(stream);
        stream.close();
        Authenticator auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(properties.getProperty("username"), properties.getProperty("password"));
            }
        };
        props.put("sender", properties.getProperty("username"));
        handler = new MailHandler(props, auth);
        payops = mock(PaymentOperations.class);
        workerops = mock(WokerOperations.class);
        String notificationMailAddress = properties.getProperty("username");
        mng = new MoneyTransferManager(handler, payops, workerops, notificationMailAddress);
    }

    @Test
    public void testPayOff() throws Exception {
        WorkerRecord worker0 = mock(WorkerRecord.class);
        WorkerRecord worker1 = mock(WorkerRecord.class);
        List<WorkerRecord> workerlist = new LinkedList<WorkerRecord>();
        workerlist.add(worker0);
        workerlist.add(worker1);
        when(workerops.getWorkersWithCreditBalanceGreaterThan(anyInt())).thenReturn(workerlist);
        GiftCodeRecord code0 = mock(GiftCodeRecord.class);
        GiftCodeRecord code1 = mock(GiftCodeRecord.class);
        GiftCodeRecord code2 = mock(GiftCodeRecord.class);
        //TODO: GiftCodeRecord.getCode();
        when(code0.getAmount()).thenReturn(30);
        when(code1.getAmount()).thenReturn(25);
        when(code2.getAmount()).thenReturn(10);
        LinkedList<GiftCodeRecord> codeList = new LinkedList<GiftCodeRecord>();
        codeList.addLast(code0);
        codeList.addLast(code1);
        codeList.addLast(code2);
        when(payops.getUnusedGiftCodesDescending()).thenReturn(codeList);


        mng.logMoneyTransfer(0, 30);

    }
}
