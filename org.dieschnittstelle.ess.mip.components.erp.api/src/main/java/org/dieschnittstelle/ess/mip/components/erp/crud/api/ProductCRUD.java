package org.dieschnittstelle.ess.mip.components.erp.crud.api;

import java.util.List;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.dieschnittstelle.ess.entities.erp.AbstractProduct;
import org.dieschnittstelle.ess.entities.erp.Campaign;

/*
 * TODO MIP+JPA1/2/5:
 * this interface shall be implemented using an ApplicationScoped CDI bean with an EntityManager.
 * See TouchpointCRUDImpl for an example bean with a similar scope of functionality
 */
@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface ProductCRUD {

    @POST
    public AbstractProduct createProduct(AbstractProduct product);

    @GET
    public List<AbstractProduct> readAllProducts();

    @PUT
    // This should contain an id in the path parameter and not depend on the id in the body JSON
    public AbstractProduct updateProduct(AbstractProduct update);

    @GET
    @Path("/{productId}")
    public AbstractProduct readProduct(@PathParam("productId") long productId);

    @DELETE
    @Path("/{productId}")
    public boolean deleteProduct(@PathParam("productId") long productId);

    @GET
    @Path("/{productId}/campaigns")
    public List<Campaign> getCampaignsForProduct(@PathParam("productId") long productId);

}
