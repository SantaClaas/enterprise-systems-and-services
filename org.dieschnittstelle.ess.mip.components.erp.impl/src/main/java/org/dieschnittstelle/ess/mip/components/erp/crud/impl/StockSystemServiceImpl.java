package org.dieschnittstelle.ess.mip.components.erp.crud.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.dieschnittstelle.ess.entities.erp.IndividualisedProductItem;
import org.dieschnittstelle.ess.mip.components.erp.api.StockSystem;
import org.dieschnittstelle.ess.mip.components.erp.api.StockSystemService;
import org.dieschnittstelle.ess.mip.components.erp.crud.api.ProductCRUD;
import org.dieschnittstelle.ess.utils.interceptors.Logged;

import java.util.List;

@ApplicationScoped
@Transactional
@Logged
public class StockSystemServiceImpl implements StockSystemService {

    @Inject
    private StockSystem stockSystem;

    @Inject
    private ProductCRUD productService;

    private IndividualisedProductItem resolveProduct(long productId) {
        return (IndividualisedProductItem) productService.readProduct(productId);
    }

    @Override
    public void addToStock(long pointOfSaleId, long productId, int units) {
        stockSystem.addToStock(resolveProduct(productId), pointOfSaleId, units);
    }

    @Override
    public void removeFromStock(long pointOfSaleId, long productId, int units) {
        stockSystem.removeFromStock(resolveProduct(productId), pointOfSaleId, units);
    }

    @Override
    public List<IndividualisedProductItem> getAllProductsOnStock() {
        return stockSystem.getAllProductsOnStock();
    }

    @Override
    public List<IndividualisedProductItem> getProductsOnStock(long pointOfSaleId) {
        return stockSystem.getProductsOnStock(pointOfSaleId);
    }

    @Override
    public int getUnitsOnStock(long productId, long pointOfSaleId) {
        return stockSystem.getUnitsOnStock(resolveProduct(productId), pointOfSaleId);
    }

    @Override
    public int getTotalUnitsOnStock(long productId) {
        return stockSystem.getTotalUnitsOnStock(resolveProduct(productId));
    }

    @Override
    public List<Long> getPointsOfSale(long productId) {
        return stockSystem.getPointsOfSale(resolveProduct(productId));
    }
}
