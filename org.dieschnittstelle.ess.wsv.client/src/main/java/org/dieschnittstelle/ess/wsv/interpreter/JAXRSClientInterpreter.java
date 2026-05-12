package org.dieschnittstelle.ess.wsv.interpreter;


import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import jakarta.ws.rs.*;
import org.apache.http.client.methods.*;
import org.apache.logging.log4j.Logger;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.ByteArrayEntity;

import org.dieschnittstelle.ess.utils.Http;
import org.dieschnittstelle.ess.wsv.interpreter.json.JSONObjectSerialiser;

import static org.dieschnittstelle.ess.utils.Utils.*;


/*
 * TODO x  WSV1: implement this class such that the crud operations declared on ITouchpointCRUDService in .ess.wsv can be successfully called from the class AccessRESTServiceWithInterpreter in the .esa.wsv.client project
 */
public class JAXRSClientInterpreter implements InvocationHandler {

    // use a logger
    protected static Logger logger = org.apache.logging.log4j.LogManager.getLogger(JAXRSClientInterpreter.class);

    // declare a baseurl
    private String baseurl;

    // declare a common path segment
    private String commonPath;

    // use our own implementation JSONObjectSerialiser
    private JSONObjectSerialiser jsonSerialiser = new JSONObjectSerialiser();

    // use an attribute that holds the serviceInterface (useful, e.g. for providing a toString() method)
    private Class serviceInterface;

    // use a constructor that takes an annotated service interface and a baseurl. the implementation should read out the path annotation, we assume we produce and consume json, i.e. the @Produces and @Consumes annotations will not be considered here
    public JAXRSClientInterpreter(Class serviceInterface, String baseurl) {

        // TODO: implement the constructor!

        logger.info("<constructor>: " + serviceInterface + " / " + baseurl + " / " + commonPath);

        this.serviceInterface = serviceInterface;
        this.baseurl = baseurl;
        if (this.serviceInterface.isAnnotationPresent(Path.class)) {
            Path pathAnnotation = (Path) this.serviceInterface.getAnnotation(Path.class);
            this.commonPath = pathAnnotation.value();
        }
    }

    // TODO: implement this method interpreting jax-rs annotations on the meth argument
    @Override
    public Object invoke(Object proxy, Method method, Object[] arguments)
            throws Throwable {

        // TODO check whether we handle the toString method and give some appropriate return value
        if (method.getName().equals("toString"))
            return "JAXRSClientInterpreter for " + serviceInterface.getName();

        // use a default http client
        HttpClient client = Http.createSyncClient();

        // TODO: create the requestUrl using baseurl and commonpath (further segments may be added if the method has an own @Path annotation)

        var url = this.baseurl + this.commonPath;

        // TODO: check whether we have a path annotation and append the requestUrl (path params will be handled when looking at the method arguments)
        if (method.isAnnotationPresent(Path.class)) {
            Path pathAnnotation = method.getAnnotation(Path.class);
            // Assume the path is only present if the endpoint requires a parameter and the first argument is the parameter
            url += pathAnnotation.value();
        }
        // a value that needs to be sent via the http request body
        Object requestBodyData = null;

        // TODO: check whether we have method arguments - only consider pathparam annotations (if any) on the first argument here - if no args are passed, the value of args is null! if no pathparam annotation is present assume that the argument value is passed via the body of the http request
        if (arguments != null && arguments.length > 0) {
            // Assume if at least one argument is present, the first argument has the path param or is the body
            var isFirstArgumentPathParameter =
                    method.getParameterAnnotations()[0].length > 0
                            && method.getParameterAnnotations()[0][0].annotationType() == PathParam.class;

            if (isFirstArgumentPathParameter) {
                // TODO: handle PathParam on the first argument - do not forget that in this case we might have a second argument providing a requestBodyData
                var beforeLastParameter = url.lastIndexOf('/');
                if (beforeLastParameter == -1)
                    throw new IllegalArgumentException("URL does not contain path parameters");

                url = url.substring(0, beforeLastParameter) + "/" + arguments[0];
                // TODO: if we have a path param, we need to replace the corresponding pattern in the requestUrl with the parameter value
                if (arguments.length > 1)
                    requestBodyData = arguments[1];
            } else {
                // if we do not have a path param, we assume the argument value will be sent via the body of the request
                requestBodyData = arguments[0];
            }
        }

        // declare a HttpUriRequest variable
        HttpUriRequest request;

        // TODO: check which of the http method annotation is present and instantiate request accordingly passing the requestUrl
        if (method.isAnnotationPresent(GET.class))
            request = new HttpGet(url);
        else if (method.isAnnotationPresent(POST.class))
            request = new HttpPost(url);
        else if (method.isAnnotationPresent(PUT.class))
            request = new HttpPut(url);
        else if (method.isAnnotationPresent(DELETE.class))
            request = new HttpDelete(url);
        else
            throw new IllegalArgumentException("Unsupported or no HTTP method annotation on method: " + method.getName() + ". Please use one of @GET, @POST, @PUT or @DELETE");

        // TODO: add a header on the request declaring that we accept json (for header names, you can use the constants declared in jakarta.ws.rs.core.HttpHeaders, for content types use the constants from jakarta.ws.rs.core.MediaType;)
        request.addHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);

        // if a body shall be sent, convert the requestBodyData to json, create an entity from it and set it on the request
        if (requestBodyData != null) {
            // TODO: use a ByteArrayOutputStream for writing json
            var stream = new ByteArrayOutputStream();
            // TODO: write the object to the stream using the jsonSerialiser
            jsonSerialiser.writeObject(requestBodyData, stream);
            // TODO: create an ByteArrayEntity from the stream's content, assigning it to requestBodyDataAsJson
            var requestBodyDataAsJson = new ByteArrayEntity(stream.toByteArray());
            // TODO: set the entity on the request, which must be cast to HttpEntityEnclosingRequest
            ((HttpEntityEnclosingRequest) request).setEntity(requestBodyDataAsJson);
            // TODO: and add a content type header for the request
            request.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        }

        logger.info("invoke(): executing request: " + request);

        // then send the request to the server and get the response
        HttpResponse response = client.execute(request);

        logger.info("invoke(): received response: " + response);

        // check the response code
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
            throw new RuntimeException("Got unexpected status from server: " + response.getStatusLine());

        // TODO: convert the response body to a java object of an appropriate type considering the return type of the method as returned by getGenericReturnType() and set the object as value of returnValue
        var returnType = method.getGenericReturnType();

        if (returnType.getTypeName().equals("void")) {
            logger.info("invoke(): returning null as return value");
            return null;
        }

        // declare a variable for the return value
        var returnValue = jsonSerialiser.readObject(response.getEntity().getContent(), returnType);
        logger.info("invoke(): returning value: " + returnValue);
        // and return the return value
        return returnValue;
    }

}