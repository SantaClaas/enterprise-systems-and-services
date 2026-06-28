package org.dieschnittstelle.ess.mip.components.erp.crud.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.dieschnittstelle.ess.entities.erp.IndividualisedProductItem;
import org.dieschnittstelle.ess.entities.erp.PointOfSale;
import org.dieschnittstelle.ess.entities.erp.StockItem;
import org.dieschnittstelle.ess.mip.components.erp.api.StockSystem;
import org.dieschnittstelle.ess.mip.components.erp.crud.api.PointOfSaleCRUD;
import org.dieschnittstelle.ess.utils.interceptors.Logged;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Logged
@Transactional
@ApplicationScoped
public class StockSystemImplementation implements StockSystem {

    @Inject
    private StockItemCRUD stockItemService;

    @Inject
    private PointOfSaleCRUD pointOfSaleService;

    @Override
    public void addToStock(IndividualisedProductItem product, long pointOfSaleId, int units) {
        PointOfSale pointOfSale = pointOfSaleService.readPointOfSale(pointOfSaleId);
        StockItem existing = stockItemService.readStockItem(product, pointOfSale);
        if (existing == null) {
            stockItemService.createStockItem(new StockItem(product, pointOfSale, units));
            return;
        }

        existing.setUnits(existing.getUnits() + units);
        stockItemService.updateStockItem(existing);
    }

    @Override
    public void removeFromStock(IndividualisedProductItem product, long pointOfSaleId, int units) {
        addToStock(product, pointOfSaleId, -units);
    }

    @Override
    public List<IndividualisedProductItem> getProductsOnStock(long pointOfSaleId) {
        PointOfSale pointOfSale = pointOfSaleService.readPointOfSale(pointOfSaleId);
        return stockItemService.readStockItemsForPointOfSale(pointOfSale).stream()
                .map(StockItem::getProduct)
                .collect(Collectors.toList());
    }

    @Override
    public List<IndividualisedProductItem> getAllProductsOnStock() {
        Set<IndividualisedProductItem> products = new HashSet<>();
        for (PointOfSale pointOfSale : pointOfSaleService.readAllPointsOfSale()) {
            products.addAll(getProductsOnStock(pointOfSale.getId()));
        }
        return new ArrayList<>(products);
    }

    @Override
    public int getUnitsOnStock(IndividualisedProductItem product, long pointOfSaleId) {
        PointOfSale pointOfSale = pointOfSaleService.readPointOfSale(pointOfSaleId);
        StockItem item = stockItemService.readStockItem(product, pointOfSale);
        return item == null ? 0 : item.getUnits();
    }

    @Override
    public int getTotalUnitsOnStock(IndividualisedProductItem product) {
        return stockItemService.readStockItemsForProduct(product).stream()
                .mapToInt(StockItem::getUnits)
                .sum();
    }

    @Override
    public List<Long> getPointsOfSale(IndividualisedProductItem product) {
        return stockItemService.readStockItemsForProduct(product).stream()
                .map(item -> item.getPos().getId())
                .collect(Collectors.toList());
    }
}
