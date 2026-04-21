package org.dieschnittstelle.ess.ser.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.logging.log4j.Logger;
import org.dieschnittstelle.ess.entities.crm.AbstractTouchpoint;
import org.dieschnittstelle.ess.entities.crm.Address;
import org.dieschnittstelle.ess.entities.crm.StationaryTouchpoint;
import org.dieschnittstelle.ess.utils.Http;

import static org.dieschnittstelle.ess.utils.Utils.*;

public class ShowTouchpointService {

    protected static Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(ShowTouchpointService.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        ShowTouchpointService service = new ShowTouchpointService();
        service.run();
    }

    /**
     * the http client that can be used for accessing the service on tomcat - note that we are usying an async client here
     */
    private CloseableHttpAsyncClient client;

    /**
     * the attribute that controls whether we are running through (when called from the junit test) or not
     */
    private boolean stepwise = true;

    /**
     * constructor
     */
    public ShowTouchpointService() {

    }

    /*
     * create the http client - this will be done for each request
     */
    public void createClient() {
        if (client != null && client.isRunning()) {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        client = Http.createAsyncClient();
        client.start();
    }

    /**
     * run
     */
    public void run() {

        // 1) read out all touchpoints
        List<AbstractTouchpoint> touchpoints = readAllTouchpoints();

        // 2) delete the touchpoint after next console input
        if (touchpoints != null && touchpoints.size() > 0) {
            if (stepwise)
                step();

            deleteTouchpoint(touchpoints.get(0));
        }

        // 3) wait for input and create a new touchpoint
        if (stepwise) {
            step();
        }

        Address addr = new Address("Luxemburger Strasse", "10", "13353",
                "Berlin");
        StationaryTouchpoint tp = new StationaryTouchpoint(-1,
                "BHT Verkaufsstand", addr);

        createNewTouchpoint(tp);

        try {
            client.close();
        } catch (IOException ioe) {
            logger.error("got IOException trying to close client: " + ioe, ioe);
        }

        show("TestTouchpointService: done.\n");
    }

    /**
     * read all touchpoints
     *
     * @return
     */
    public List<AbstractTouchpoint> readAllTouchpoints() {

        logger.info("readAllTouchpoints()");

        createClient();

        logger.debug("client running: {}", client.isRunning());

        // demonstrate access to the asynchronously running servlet (client-side access is asynchronous in any case)
        boolean async = false;

        try {

            // create a GetMethod

            // UE SER1: Aendern Sie die URL von api->gui
            HttpGet get = new HttpGet(
                    "http://localhost:8080/api/" + (async ? "async/touchpoints" : "touchpoints"));

            logger.info("readAllTouchpoints(): about to execute request: " + get);

            // mittels der <request>.setHeader() Methode koennen Header-Felder
            // gesetzt werden

            // execute the method and obtain the response - for AsyncClient this will be a future from
            // which the response object can be obtained synchronously calling get() - alternatively, a FutureCallback can
            // be passed to the execute() method
            Future<HttpResponse> responseFuture = client.execute(get, null);
            logger.info("readAllTouchpoints(): received response future...");

            HttpResponse response = responseFuture.get();
            logger.info("readAllTouchpoints(): received response value");

            // check the response status
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

                // try to read out an object from the response entity
                ObjectInputStream ois = new ObjectInputStream(response
                        .getEntity().getContent());

                List<AbstractTouchpoint> touchpoints = (List<AbstractTouchpoint>) ois
                        .readObject();

                logger.info("read touchpoints: " + touchpoints);

                return touchpoints;

            } else {
                String err = "could not successfully execute request. Got status code: "
                        + response.getStatusLine().getStatusCode();
                logger.error(err);
                throw new RuntimeException(err);
            }

        } catch (Exception e) {
            String err = "got exception: " + e;
            logger.error(err, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * TODO SER4
     *
     * @param touchpoint
     */
    public void deleteTouchpoint(AbstractTouchpoint touchpoint) {
        logger.info("deleteTouchpoint(): will delete: " + touchpoint);

        createClient();

        logger.debug("client running: {}", client.isRunning());

        var request = new HttpDelete("http://localhost:8080/api/touchpoints/" + touchpoint.getId());
        var execution = client.execute(request, null);


        HttpResponse response;
        try {
            response = execution.get();
        } catch (InterruptedException | ExecutionException exception) {
            logger.error("got exception while executing touchpoint delete request: " + exception, exception);
            throw new RuntimeException(exception);
        }

        logger.debug("RESPONSE {}", response);

        var statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != HttpStatus.SC_NO_CONTENT)
            throw new RuntimeException("Touchpoint delete was not successful. Expected status code 204 but status code was" + statusCode);
    }

    /**
     * TODO SER3
     * <p>
     * fuer das Schreiben des zu erzeugenden Objekts als Request Body siehe die
     * Hinweise auf:
     * http://stackoverflow.com/questions/10146692/how-do-i-write-to
     * -an-outpustream-using-defaulthttpclient
     *
     * @param touchpoint
     */
    public AbstractTouchpoint createNewTouchpoint(AbstractTouchpoint touchpoint) {
        logger.info("createNewTouchpoint(): will create: " + touchpoint);

        createClient();

        logger.debug("client running: {}", client.isRunning());

        try {

            // create post request for the api/touchpoints uri
            var request = new HttpPost("http://localhost:8080/api/touchpoints");

            // create an ObjectOutputStream from a ByteArrayOutputStream - the
            // latter must be accessible via a variable
            var byteStream = new ByteArrayOutputStream();
            var objectStream = new ObjectOutputStream(byteStream);

            // write the object to the output stream
            objectStream.writeObject(touchpoint);

            // create a ByteArrayEntity and pass it the byte array from the
            // output stream
            var entity = new ByteArrayEntity(byteStream.toByteArray());


            // set the entity on the request
            request.setEntity(entity);

            // execute the request, which will return a Future<HttpResponse> object
            Future<HttpResponse> execution = client.execute(request, null);

            // get the response from the Future object
            var response = execution.get();

            // log the status line
            show("Got response: %s", response);

            // evaluate the result using getStatusLine(), use constants in
            // HttpStatus
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) return null;

            /* if successful: */

            // create an object input stream using getContent() from the
            // response entity (accessible via getEntity())
            var inputStream = new ObjectInputStream(response.getEntity().getContent());

            // read the touchpoint object from the input stream
            AbstractTouchpoint newTouchPoint = (AbstractTouchpoint) inputStream.readObject();

            show("RECEIVED TOUCHPOINT %s", newTouchPoint);
            // return the object that you have read from the response
            return newTouchPoint;
        } catch (Exception e) {
            logger.error("got exception: " + e, e);
            throw new RuntimeException(e);
        }

    }

    /**
     *
     * @param stepwise
     */
    public void setStepwise(boolean stepwise) {
        this.stepwise = stepwise;
    }

}
