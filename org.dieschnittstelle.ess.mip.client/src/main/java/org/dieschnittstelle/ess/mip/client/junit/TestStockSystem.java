package org.dieschnittstelle.ess.mip.client.junit;

import org.dieschnittstelle.ess.entities.erp.IndividualisedProductItem;
import org.dieschnittstelle.ess.mip.client.apiclients.ProductCRUDClient;
import org.dieschnittstelle.ess.mip.client.apiclients.ServiceProxyFactory;
import org.dieschnittstelle.ess.mip.client.apiclients.StockSystemClient;
import org.dieschnittstelle.ess.mip.client.apiclients.TouchpointAccessClient;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.List;

import static org.dieschnittstelle.ess.mip.client.Constants.*;
import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestStockSystem {

    private ProductCRUDClient productCRUDClient;
    private StockSystemClient stockSystemClient;
    private TouchpointAccessClient touchpointCRUDClient;

    // we use a static attribute for tracking the initial state of the system
    private static List<IndividualisedProductItem> initialProductsOnStock;

    @Before
    public void prepareClients() throws Exception {
        ServiceProxyFactory.initialise();

        productCRUDClient = new ProductCRUDClient();
        stockSystemClient = new StockSystemClient();
        touchpointCRUDClient = new TouchpointAccessClient();
    }

    private void createProductsAndPointsOfSale() throws Exception {
        // we create the touchpoints and products before creating the stock items as the actual subject of testing
        productCRUDClient.createProduct(PRODUCT_1);
        productCRUDClient.createProduct(PRODUCT_2);

        touchpointCRUDClient.createTouchpointAndPointOfSale(TOUCHPOINT_1);
        touchpointCRUDClient.createTouchpointAndPointOfSale(TOUCHPOINT_2);
    }

    @Test
    public void a_addToStockAsCreateAndGetUnitsOnStock() throws Exception {
        // initialize the test cases
        // we reset the ids on the entities
        resetEntities();
        // and prepare the constituent entities for the stocks
        createProductsAndPointsOfSale();

        // determine initial number of products on stock from previous run
        initialProductsOnStock = stockSystemClient.getAllProductsOnStock();
        assertNotNull("all products on stock is not null", initialProductsOnStock);

        // add to stock
        stockSystemClient.addToStock(PRODUCT_1, TOUCHPOINT_1.getErpPointOfSaleId(), 100);
        stockSystemClient.addToStock(PRODUCT_2, TOUCHPOINT_1.getErpPointOfSaleId(), 50);
        stockSystemClient.addToStock(PRODUCT_1, TOUCHPOINT_2.getErpPointOfSaleId(), 75);

        assertEquals("create/read correct for p1/tp1", 100, stockSystemClient.getUnitsOnStock(PRODUCT_1, TOUCHPOINT_1.getErpPointOfSaleId()));
        assertEquals("create/read correct for p2/tp1", 50, stockSystemClient.getUnitsOnStock(PRODUCT_2, TOUCHPOINT_1.getErpPointOfSaleId()));
        assertEquals("create/read correct for p1/tp2", 75, stockSystemClient.getUnitsOnStock(PRODUCT_1, TOUCHPOINT_2.getErpPointOfSaleId()));
    }

    @Test
    public void b_addToStockAsUpdate() {
        stockSystemClient.addToStock(PRODUCT_1, TOUCHPOINT_1.getErpPointOfSaleId(), 5);
        assertEquals("update/read correct for p1/tp1", 105, stockSystemClient.getUnitsOnStock(PRODUCT_1, TOUCHPOINT_1.getErpPointOfSaleId()));
    }

    @Test
    public void c_removeFromStock() {
        stockSystemClient.removeFromStock(PRODUCT_1, TOUCHPOINT_1.getErpPointOfSaleId(), 10);
        assertEquals("remove correct for p1/tp1", 95, stockSystemClient.getUnitsOnStock(PRODUCT_1, TOUCHPOINT_1.getErpPointOfSaleId()));
    }

    @Test
    public void d_getProductsOnStock() {
        // read out all products on stock for tp1 and tp2 and check whether size and content is correct
        List products_tp1 = stockSystemClient.getProductsOnStock(TOUCHPOINT_1.getErpPointOfSaleId());
        assertEquals("size of products at touchpoint correct for tp1", 2, products_tp1.size());

        List products_tp2 = stockSystemClient.getProductsOnStock(TOUCHPOINT_2.getErpPointOfSaleId());
        assertEquals("size of products at touchpoint correct for tp2", 1, products_tp2.size());
        assertEquals("returned product for touchpoint correct for tp2", PRODUCT_1.getName(), ((IndividualisedProductItem) products_tp2.get(0)).getName());
    }

    @Test
    public void e_getAllProductsOnStock() {
        assertEquals("all products on stock correct", 2, stockSystemClient.getAllProductsOnStock().size() - initialProductsOnStock.size());
    }

    @Test
    public void f_getTotalUnitsOnStock() {
        // check that total number of units is ok
        assertEquals("total units on stock correct for p1", 170, stockSystemClient.getTotalUnitsOnStock(PRODUCT_1));
    }

    @Test
    public void g_getPointsOfSale() {
        // check that we get the correct touchpoints for the products
        List<Long> touchpoints_p1 = stockSystemClient.getPointsOfSale(PRODUCT_1);
        List<Long> touchpoints_p2 = stockSystemClient.getPointsOfSale(PRODUCT_2);

        assertEquals("number of touchpoints correct for p1", 2, touchpoints_p1.size());
        assertEquals("number of touchpoints correct for p2", 1, touchpoints_p2.size());
        assertTrue("touchpoint correct for p2", touchpoints_p2.contains(new Long(TOUCHPOINT_1.getErpPointOfSaleId())));
    }


}
