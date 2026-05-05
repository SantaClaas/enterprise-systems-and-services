package org.dieschnittstelle.ess.jrs;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.dieschnittstelle.ess.entities.erp.AbstractProduct;
import org.dieschnittstelle.ess.entities.erp.IndividualisedProductItem;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.util.List;

/*
 * UE JRS2:
 * deklarieren Sie hier Methoden fuer:
 * - die Erstellung eines Produkts
 * - das Auslesen aller Produkte
 * - das Auslesen eines Produkts
 * - die Aktualisierung eines Produkts
 * - das Loeschen eines Produkts
 * und machen Sie diese Methoden mittels JAX-RS Annotationen als WebService verfuegbar
 */

/*
 * TODO JRS3: aendern Sie Argument- und Rueckgabetypen der Methoden von IndividualisedProductItem auf AbstractProduct
 */
@Path("/products")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
public interface IProductCRUDService {

    @POST
    @Operation(operationId = "createProduct")
    public AbstractProduct createProduct(AbstractProduct product);

    @GET
    @Operation(operationId = "readAllProducts")
    public List<AbstractProduct> readAllProducts();

    @PUT
    @Path("/{id}")
    @Operation(operationId = "updateProduct")
    public AbstractProduct updateProduct(@PathParam("id") long id,
                                         AbstractProduct update);

    @DELETE
    @Path("/{id}")
    @Operation(operationId = "deleteProduct")
    boolean deleteProduct(@PathParam("id") long id);

    @GET
    @Path("/{id}")
    @Operation(operationId = "readProduct")
    public AbstractProduct readProduct(@PathParam("id") long id);

}
