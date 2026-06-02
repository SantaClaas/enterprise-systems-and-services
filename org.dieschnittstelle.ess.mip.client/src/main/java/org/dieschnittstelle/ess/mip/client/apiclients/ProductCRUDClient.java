package org.dieschnittstelle.ess.mip.client.apiclients;

import java.util.List;

import org.dieschnittstelle.ess.entities.erp.Campaign;
import org.dieschnittstelle.ess.mip.client.Constants;
import org.dieschnittstelle.ess.mip.components.erp.crud.api.ProductCRUD;
import org.dieschnittstelle.ess.entities.erp.AbstractProduct;

public class ProductCRUDClient implements ProductCRUD {

    private ProductCRUD serviceProxy;

    public ProductCRUDClient() throws Exception {
        // TODONE: obtain a proxy specifying the service interface. Let all subsequent methods use the proxy.
        this.serviceProxy = ServiceProxyFactory.getInstance().getProxy(ProductCRUD.class);
    }

    public AbstractProduct createProduct(AbstractProduct product) {

        // TODONE KOMMENTIEREN SIE DIE FOLGENDE ZUWEISUNG VON IDs UND DIE RETURN-ANWEISUNG AUS
//        product.setId(Constants.nextId());
//        return product;

        // TODONE: KOMMENTIEREN SIE DEN FOLGENDEN CODE, INKLUSIVE DER ID ZUWEISUNG, EIN
        AbstractProduct created = serviceProxy.createProduct(product);
        // as a side-effect we set the id of the created product on the argument before returning
        product.setId(created.getId());
        return created;
    }

    public List<AbstractProduct> readAllProducts() {
        return serviceProxy.readAllProducts();
//        return null;
    }

    public AbstractProduct updateProduct(AbstractProduct update) {
        return serviceProxy.updateProduct(update);
//        return null;/
    }

    public AbstractProduct readProduct(long productId) {
        return serviceProxy.readProduct(productId);
//        return null;
    }

    public boolean deleteProduct(long productId) {
        return serviceProxy.deleteProduct(productId);
//        return false;
    }

    @Override
    public List<Campaign> getCampaignsForProduct(long productId) {
        return serviceProxy.getCampaignsForProduct(productId);
//        return null;
    }

}
