package org.dieschnittstelle.ess.mip.components.shopping.api;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

// PAT1: this is the interface to be provided as a rest service if rest service access is used
@Path("/purchases")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface PurchaseService {

	public static record CreatePurchaseRequest(long shoppingCartId, long touchPointId, long customerId) {}

	@POST
	void purchaseCartAtTouchpointForCustomer(CreatePurchaseRequest request) throws ShoppingException;
	
}
