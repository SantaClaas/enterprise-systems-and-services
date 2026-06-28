package org.dieschnittstelle.ess.mip.components.erp.api;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.dieschnittstelle.ess.entities.erp.IndividualisedProductItem;

import java.util.List;
import java.util.Optional;

/**
 * TODO MIP3/4/6:
 * - declare the web api for this interface using JAX-RS
 * - implement the interface as a CDI Bean
 * - in the Bean implementation, delegate method invocations to the corresponding methods of the StockSystem Bean
 * - let the StockSystemClient in the client project access the web api via this interface - see ShoppingCartClient for an example
 */
@Path("/stock")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface StockSystemService {

    /**
     * adds some units of a product to the stock of a point of sale
     */
    @Path("/points-of-sale/{pointOfSaleId}/products/{productId}/units")
    // Using post as recommended in exercise. I would redesign this in a real scenario.
    @POST
    void addToStock(@PathParam("pointOfSaleId") long pointOfSaleId, @PathParam("productId") long productId, @QueryParam("units") int units);

    /**
     * removes some units of a product from the stock of a point of sale
     */

    @Path("/points-of-sale/{pointOfSaleId}/products/{productId}/units")
    // Using post as recommended in exercise. I would redesign this in a real scenario.
    @PATCH
    void removeFromStock(@PathParam("pointOfSaleId") long pointOfSaleId, @PathParam("productId") long productId, @QueryParam("units") int units);

    /**
     * returns all products on stock across all points of sale
     */
    @Path("/products")
    @GET
    List<IndividualisedProductItem> getAllProductsOnStock();

    /**
     * returns the products on stock for a specific point of sale
     */
    @Path("/points-of-sale/{pointOfSaleId}/products")
    @GET
    List<IndividualisedProductItem> getProductsOnStock(@PathParam("pointOfSaleId") long pointOfSaleId);

    /**
     * returns the units on stock for a given product at a specific point of sale
     */
    @Path("/products/{productId}/units/")
    @GET
    int getUnitsOnStock(@PathParam("productId") long productId, @QueryParam("pointOfSaleId") long pointOfSaleId);

    /**
     * returns the total units on stock for a given product across all points of sale
     */
    @Path("/products/{productId}/units/total")
    @GET
    int getTotalUnitsOnStock(@PathParam("productId") long productId);

    /**
     * returns the points of sale where some product is available
     */
    @Path("/products/{productId}/points-of-sale/")
    @GET
    List<Long> getPointsOfSale(@PathParam("productId") long productId);

}
