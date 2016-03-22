package edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.stream.events.Namespace;
import javax.xml.transform.sax.SAXSource;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This class abstracts a unirest request to mturk
 *
 * if a unirest exception happens a (timeout or something like that)
 * the resulting exception will contain the unique rest token and the url to call again
 * @param <T> The result of the request
 * @param <K> The Internal value which is returned by the request
 */
public abstract class MturkRestCommand<T,K> extends CompletableFuture<T> implements Callback<String> {
    private final Function<K,T> transformer;
    private final HttpRequest request;
    private final Class<K> klass;
    private final String uniqueRestToken;

    /**
     * Creates a new command
     * @param con connection to use
     * @param operation name of the operation to call
     * @param responseGroup the group which should be returned by mturk
     * @param version version of the api to use
     * @param klass the class of the resulting structure
     * @param supplier method to generate the parameters for the operation
     * @param transformer the transformer which transforms the instance of klass into T
     */
    public MturkRestCommand(MTurkConnection con, String operation,
                            String responseGroup, String version, Class<K> klass,
                            Supplier<Map<String, Object>> supplier, Function<K, T> transformer) {
        this.uniqueRestToken = UUID.randomUUID().toString().substring(0,20);
        this.klass = klass;
        this.transformer = transformer;

        request = Unirest.get(con.url)
                .queryString("UniqueRequestToken", uniqueRestToken)
                .queryString(con.getCallParameter(operation,responseGroup,version))
                .queryString(supplier.get());
        request.asStringAsync(this);

        LogManager.getLogger("MTurk Platform").log(Level.INFO, request.getUrl());
    }
    @Override
    public void completed(HttpResponse<String> response) {
        JAXBContext context;
        try {
            //FIXME we should append a version, because amazon does not append there own standard namespace
            final String version = "http://requester.mturk.amazonaws.com/doc/2014-08-15";

            NamespaceFilter filter =
                    new NamespaceFilter(version,true);
            XMLReader reader
                    = XMLReaderFactory.createXMLReader();
            filter.setParent(reader);

            context = JAXBContext.newInstance(klass);
            SAXSource source = new SAXSource(filter, new InputSource(response.getRawBody()));

            @SuppressWarnings("unchecked") K k = (K) context.createUnmarshaller().unmarshal(source);
            complete(transformer.apply(k));
        } catch (Exception e) {
            completeExceptionally(new RuntimeException(e));
        }
    }

    @Override
    public void failed(UnirestException e) {
        completeExceptionally(
                getDetailedException(e)
        );
    }

    /**
     * Generates a detailed exception with unique id + the command
     * @param e The exception which caused the problem
     * @return returns a exception with unique id and command
     */
    private RuntimeException getDetailedException(UnirestException e) {
        return new RuntimeException("Call "+uniqueRestToken+ " failed\n" +
                "Command: "+request.getUrl(), e);
        //for complaining people - getUrl is returning the complete command
    }

    @Override
    public void cancelled() {
        completeExceptionally(getDetailedException(null));
    }
}
