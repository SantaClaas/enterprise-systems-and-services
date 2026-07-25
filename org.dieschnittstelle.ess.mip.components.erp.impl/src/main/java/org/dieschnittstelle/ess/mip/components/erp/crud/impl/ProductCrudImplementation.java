package org.dieschnittstelle.ess.mip.components.erp.crud.impl;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;
import jakarta.interceptor.Interceptor;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.Logger;
import org.dieschnittstelle.ess.entities.erp.AbstractProduct;
import org.dieschnittstelle.ess.entities.erp.Campaign;
import org.dieschnittstelle.ess.mip.components.erp.crud.api.ProductCRUD;

import java.util.List;

@ApplicationScoped
@Transactional
/*
 * Work around for priority conflict. Using higher priority. This deviates from the demo implementation that did not work for me
 */
@Alternative
@Priority(Interceptor.Priority.APPLICATION+10)
public class ProductCrudImplementation implements ProductCRUD {

    private static Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(ProductCrudImplementation.class);

    //TODO can I make this read-only
    @Inject
    @EntityManagerProvider.ERPDataAccessor
    private EntityManager entityManager;

    @Override
    public AbstractProduct createProduct(AbstractProduct product) {
        entityManager.persist(product);
        return product;
    }

    @Override
    public List<AbstractProduct> readAllProducts() {
        var query = entityManager.createQuery("SELECT DISTINCT product FROM AbstractProduct product", AbstractProduct.class);
        return query.getResultList();
    }

    @Override
    public AbstractProduct updateProduct(AbstractProduct update) {
        return entityManager.merge(update);
    }

    @Override
    public AbstractProduct readProduct(long productId) {
        return entityManager.find(AbstractProduct.class, productId);
    }

    @Override
    public boolean deleteProduct(long productId) {
        entityManager.remove(this.readProduct(productId));
        // This value is useless the method either completes or throws an exception
        // Running without an exception is equivalent to
        return true;
    }

    @Override
    public List<Campaign> getCampaignsForProduct(long productId) {
        var query = entityManager.createQuery("SELECT DISTINCT campaign FROM Campaign campaign JOIN campaign.bundles bundle WHERE bundle.product.id = :productId", Campaign.class);
        query.setParameter("productId", productId);
        return query.getResultList();
    }
}
