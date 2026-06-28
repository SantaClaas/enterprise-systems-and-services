package org.dieschnittstelle.ess.mip.components.erp.crud.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.dieschnittstelle.ess.entities.erp.IndividualisedProductItem;
import org.dieschnittstelle.ess.entities.erp.PointOfSale;
import org.dieschnittstelle.ess.entities.erp.StockItem;
import org.dieschnittstelle.ess.utils.interceptors.Logged;

import java.util.List;

@ApplicationScoped
@Transactional
@Logged
public class StockItemCrudImplementation implements StockItemCRUD {

    @Inject
    @EntityManagerProvider.ERPDataAccessor
    private EntityManager entityManager;

    @Override
    public StockItem createStockItem(StockItem item) {
        // merge() re-attaches the detached product entity (via cascade MERGE on StockItem.product)
        // and persists item as a new entity if it has no id yet
        return entityManager.merge(item);
    }

    @Override
    public StockItem readStockItem(IndividualisedProductItem product, PointOfSale pointOfSale) {
        var results = entityManager.createQuery(
                        "SELECT item FROM StockItem item WHERE item.product.id = :productId AND item.pos.id = :pointOfSaleId",
                        StockItem.class)
                .setParameter("productId", product.getId())
                .setParameter("pointOfSaleId", pointOfSale.getId())
                .getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public StockItem updateStockItem(StockItem item) {
        return entityManager.merge(item);
    }

    @Override
    public List<StockItem> readStockItemsForProduct(IndividualisedProductItem prod) {
        return entityManager.createQuery(
                        "SELECT item FROM StockItem item WHERE item.product.id = :productId",
                        StockItem.class)
                .setParameter("productId", prod.getId())
                .getResultList();
    }

    @Override
    public List<StockItem> readStockItemsForPointOfSale(PointOfSale pointOfSale) {
        return entityManager.createQuery(
                        "SELECT item FROM StockItem item WHERE item.pos.id = :pointOfSaleId",
                        StockItem.class)
                .setParameter("pointOfSaleId", pointOfSale.getId())
                .getResultList();
    }
}
