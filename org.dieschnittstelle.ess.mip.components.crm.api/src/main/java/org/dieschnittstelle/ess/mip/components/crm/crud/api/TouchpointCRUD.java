package org.dieschnittstelle.ess.mip.components.crm.crud.api;

import java.util.List;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.dieschnittstelle.ess.mip.components.crm.api.CrmException;
import org.dieschnittstelle.ess.entities.crm.AbstractTouchpoint;

@Path("/touchpoints")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface TouchpointCRUD {

    @POST
    public AbstractTouchpoint createTouchpoint(AbstractTouchpoint Touchpoint) throws CrmException;

    @GET
    @Path("/{id}")
    public AbstractTouchpoint readTouchpoint(@PathParam("id") long id);

    @GET
    public List<AbstractTouchpoint> readAllTouchpoints();

    @PUT
    // This should contain an id in the path parameter and not depend on the id in the body JSON
    public AbstractTouchpoint updateTouchpoint(AbstractTouchpoint Touchpoint);

    @DELETE
    @Path("/{id}")
    public boolean deleteTouchpoint(@PathParam("id") int id);

}
