package org.dieschnittstelle.ess.mip.components.erp.crud.api;

import java.util.List;

import org.dieschnittstelle.ess.entities.erp.AbstractProduct;
import org.dieschnittstelle.ess.entities.erp.Campaign;

/*
 * TODO MIP+JPA1/2/5:
 * this interface shall be implemented using an ApplicationScoped CDI bean with an EntityManager.
 * See TouchpointCRUDImpl for an example bean with a similar scope of functionality
 */

public interface ProductCRUD {

    public AbstractProduct createProduct(AbstractProduct product);

    public List<AbstractProduct> readAllProducts();

    public AbstractProduct updateProduct(AbstractProduct update);

    public AbstractProduct readProduct(long productId);

    public boolean deleteProduct(long productId);

    public List<Campaign> getCampaignsForProduct(long productId);

}
