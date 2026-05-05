package org.dieschnittstelle.ess.jrs.client.junit;

import java.util.List;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import org.apache.logging.log4j.Logger;
import org.dieschnittstelle.ess.entities.erp.AbstractProduct;
import org.dieschnittstelle.ess.entities.erp.IndividualisedProductItem;

import org.dieschnittstelle.ess.jrs.IProductCRUDService;
import org.dieschnittstelle.ess.jrs.ITouchpointCRUDService;
import org.dieschnittstelle.ess.jrs.client.jackson.LaissezFairePolymorphicJacksonProvider;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;

public class ProductCRUDRESTClient {

    private IProductCRUDService serviceProxy;

    protected static Logger logger = org.apache.logging.log4j.LogManager.getLogger(ProductCRUDRESTClient.class);

    public ProductCRUDRESTClient() throws Exception {

        Client client = ClientBuilder.newBuilder()
                .build()
                // use our custom jackson provider that is less restrictive if it comes to dealing with polymorphic types
                // handled using @JsonTypeInfo
                .register(LaissezFairePolymorphicJacksonProvider.class);

        ResteasyWebTarget target = (ResteasyWebTarget) client.target("http://localhost:8080/api/");

//        ITouchpointCRUDService serviceProxy = target.proxy(ITouchpointCRUDService.class);
        IProductCRUDService service = target.proxy(IProductCRUDService.class);
        /*
         * TODO: JRS2: create a client for the web service using ResteasyClientBuilder and ResteasyWebTarget
         */
        serviceProxy = service;
    }

    public AbstractProduct createProduct(IndividualisedProductItem prod) {
        AbstractProduct created = serviceProxy.createProduct(prod);
        // as a side-effect we set the id of the created product on the argument before returning
        prod.setId(created.getId());
        return created;
    }

    // TODO: activate this method for testing JRS3
    public AbstractProduct createCampaign(AbstractProduct prod) {
        AbstractProduct created = serviceProxy.createProduct(prod);
        // as a side-effect we set the id of the created product on the argument before returning
        prod.setId(created.getId());
        return created;
    }

    public List<?> readAllProducts() {
        return serviceProxy.readAllProducts();
    }

    public AbstractProduct updateProduct(AbstractProduct update) {
        return serviceProxy.updateProduct(update.getId(), (IndividualisedProductItem) update);
    }

    public boolean deleteProduct(long id) {
        return serviceProxy.deleteProduct(id);
    }

    public AbstractProduct readProduct(long id) {
        return serviceProxy.readProduct(id);
    }

}
