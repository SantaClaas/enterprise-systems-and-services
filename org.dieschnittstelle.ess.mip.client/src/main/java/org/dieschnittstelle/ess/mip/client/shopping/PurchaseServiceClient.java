package org.dieschnittstelle.ess.mip.client.shopping;

import org.apache.logging.log4j.Logger;
import org.dieschnittstelle.ess.entities.erp.Campaign;
import org.dieschnittstelle.ess.entities.shopping.ShoppingCartItem;
import org.dieschnittstelle.ess.mip.client.apiclients.ServiceProxyFactory;
import org.dieschnittstelle.ess.mip.client.apiclients.ShoppingCartClient;
import org.dieschnittstelle.ess.mip.components.shopping.api.PurchaseService;
import org.dieschnittstelle.ess.mip.components.shopping.api.ShoppingException;
import org.dieschnittstelle.ess.entities.crm.AbstractTouchpoint;
import org.dieschnittstelle.ess.entities.crm.Customer;
import org.dieschnittstelle.ess.entities.erp.AbstractProduct;

public class PurchaseServiceClient implements ShoppingBusinessDelegate {

    protected static Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(PurchaseServiceClient.class);

    /*
     * TODO PAT1: use an appropriate proxy for the server-side purchase service
     *  Note that touchpoint and customer need to be stored locally
     *  before purchase() is invoked. For accessing shopping cart data use a local ShoppingCartClient
     *  in this case and access the shopping cart using the provided getter method
     */
    private PurchaseService purchaseServiceProxy;

    private ShoppingCartClient shoppingCartClient;

    private AbstractTouchpoint touchPoint;
    private Customer customer;

    public PurchaseServiceClient() {
        /* TODO: instantiate the proxy using the ServiceProxyFactory (see the other client classes) */
        this.purchaseServiceProxy = ServiceProxyFactory.getInstance().getProxy(PurchaseService.class);

        try {
            this.shoppingCartClient = new ShoppingCartClient();
        } catch (Exception exception) {
            throw new RuntimeException("Error instantiating shopping cart client: " + exception, exception);
        }
    }

    /* TODO: implement the following methods s */

    @Override
    public void setTouchpoint(AbstractTouchpoint touchpoint) {
        this.touchPoint = touchpoint;
    }

    @Override
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    @Override
    public void addProduct(AbstractProduct product, int units) {
        this.shoppingCartClient.addItem(new ShoppingCartItem(product.getId(), units, product instanceof Campaign));
    }

    @Override
    public void purchase() throws ShoppingException {
        this.purchaseServiceProxy.purchaseCartAtTouchpointForCustomer(
                new PurchaseService.CreatePurchaseRequest(
                        this.shoppingCartClient.getShoppingCartEntityId(),
                        this.touchPoint.getId(),
                        this.customer.getId()
                )
        );
    }

}
