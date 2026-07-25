package org.dieschnittstelle.ess.mip.components.shopping.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.Logger;
import org.dieschnittstelle.ess.entities.crm.AbstractTouchpoint;
import org.dieschnittstelle.ess.entities.crm.Customer;
import org.dieschnittstelle.ess.entities.crm.CustomerTransaction;
import org.dieschnittstelle.ess.entities.crm.CustomerTransactionShoppingCartItem;
import org.dieschnittstelle.ess.entities.erp.AbstractProduct;
import org.dieschnittstelle.ess.entities.erp.Campaign;
import org.dieschnittstelle.ess.entities.erp.IndividualisedProductItem;
import org.dieschnittstelle.ess.entities.erp.ProductBundle;
import org.dieschnittstelle.ess.entities.shopping.ShoppingCartItem;
import org.dieschnittstelle.ess.mip.components.crm.api.CampaignTracking;
import org.dieschnittstelle.ess.mip.components.crm.api.CustomerTracking;
import org.dieschnittstelle.ess.mip.components.crm.api.TouchpointAccess;
import org.dieschnittstelle.ess.mip.components.crm.crud.api.CustomerCRUD;
import org.dieschnittstelle.ess.mip.components.erp.api.StockSystem;
import org.dieschnittstelle.ess.mip.components.erp.crud.api.ProductCRUD;
import org.dieschnittstelle.ess.mip.components.shopping.api.PurchaseService;
import org.dieschnittstelle.ess.mip.components.shopping.api.ShoppingException;
import org.dieschnittstelle.ess.mip.components.shopping.cart.api.ShoppingCart;
import org.dieschnittstelle.ess.mip.components.shopping.cart.api.ShoppingCartService;
import org.dieschnittstelle.ess.mip.components.shopping.cart.impl.ShoppingCartEntity;
import org.dieschnittstelle.ess.utils.interceptors.Logged;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Transactional
@Logged
@RequestScoped
public class PurchaseServiceImplementation implements PurchaseService {
    protected static Logger logger = org.apache.logging.log4j.LogManager.getLogger(PurchaseService.class);

//    /*
//     * the three beans that are used
//     */
//    private ShoppingCart shoppingCart;

    @Inject
    private CustomerTracking customerTracking;

    @Inject
    private CampaignTracking campaignTracking;

    @Inject
    private CustomerCRUD customerService;

    @Inject
    private TouchpointAccess touchpointAccess;

    @Inject
    private ShoppingCartService shoppingCartService;

    @Inject
    private StockSystem stockSystem;

    @Inject
    private ProductCRUD productService;

//    /**
//     * the customer
//     */
//    private Customer customer;
//
//    /**
//     * the touchpoint
//     */
//    private AbstractTouchpoint touchpoint;

//    public PurchaseServiceImplementation() {
//        logger.info("<constructor>");
//        try {
//            this.campaignTracking = new CampaignTrackingClient();
//            this.customerTracking = new CustomerTrackingClient();
//            this.shoppingCart = new ShoppingCartClient();
//        } catch (Exception e) {
//            throw new RuntimeException("initialise() failed: " + e, e);
//        }
//    }

//    public void setTouchpoint(AbstractTouchpoint touchpoint) {
//        this.touchpoint = touchpoint;
//    }

//    public void setCustomer(Customer customer) {
//        this.customer = customer;
//    }

    public void addProduct(ShoppingCart shoppingCart, AbstractProduct product, int units) {
        shoppingCart.addItem(new ShoppingCartItem(product.getId(), units, product instanceof Campaign));
    }

    /*
     * verify whether campaigns are still valid
     */
    public void verifyCampaigns(Customer customer, AbstractTouchpoint touchpoint, ShoppingCart shoppingCart) throws ShoppingException {
        if (customer == null || touchpoint == null) {
            throw new RuntimeException("cannot verify campaigns! No touchpoint has been set!");
        }

        for (ShoppingCartItem item : shoppingCart.getItems()) {
            if (item.isCampaign()) {
                int availableCampaigns = this.campaignTracking.existsValidCampaignExecutionAtTouchpoint(
                        item.getErpProductId(), touchpoint);
                logger.info("got available campaigns for product " + item.getErpProductId() + ": "
                        + availableCampaigns);
                // we check whether we have sufficient campaign items available
                if (availableCampaigns < item.getUnits()) {
                    throw new ShoppingException("verifyCampaigns() failed for productBundle " + item
                            + " at touchpoint " + touchpoint + "! Need " + item.getUnits()
                            + " instances of campaign, but only got: " + availableCampaigns);
                }
            }
        }
    }

    public void purchase(Customer customer, AbstractTouchpoint touchpoint, ShoppingCart shoppingCart) throws ShoppingException {
        logger.info("purchase()");

        if (customer == null || touchpoint == null) {
            throw new RuntimeException(
                    "cannot commit shopping session! Either customer or touchpoint has not been set: " + customer
                            + "/" + touchpoint);
        }

        // verify the campaigns
        verifyCampaigns(customer, touchpoint, shoppingCart);

        // remove the products from stock
        checkAndRemoveProductsFromStock(shoppingCart, touchpoint);

        // then we add a new customer transaction for the current purchase
        // TODO PAT1: once this functionality has been moved to the server side components, make sure
        //  that the ShoppingCartItem instances will be cloned/copied by constructing new items before
        //  using them for creating the CustomerTransaction object.
        List<ShoppingCartItem> productsInCart = shoppingCart.getItems();
        List<CustomerTransactionShoppingCartItem> productsInCartForTransaction = productsInCart
                .stream()
                .map(item -> new CustomerTransactionShoppingCartItem(item.getErpProductId(), item.getUnits(), item.isCampaign()))
                .collect(Collectors.toList());
        CustomerTransaction transaction = new CustomerTransaction(customer, touchpoint,
                productsInCartForTransaction);
        transaction.setCompleted(true);
        customerTracking.createTransaction(transaction);

        logger.info("purchase(): done.\n");
    }

    /*
     * TODO PAT2: complete the method implementation in your server-side component for shopping / purchasing
     */
    private void checkAndRemoveProductsFromStock(ShoppingCart shoppingCart, AbstractTouchpoint touchpoint) {
        logger.info("checkAndRemoveProductsFromStock");

        for (ShoppingCartItem item : shoppingCart.getItems()) {

            // TODO: ermitteln Sie das AbstractProduct für das gegebene ShoppingCartItem. Nutzen Sie dafür dessen erpProductId und die ProductCRUD bean

            var productId = item.getErpProductId();
            var product = this.productService.readProduct(productId);
            var pointOfSaleId = touchpoint.getErpPointOfSaleId();

            BiConsumer<Integer, IndividualisedProductItem> assertIsAvailable = (requiredUnits, individualProduct) -> {
                var unitsOnStock = this.stockSystem.getUnitsOnStock(individualProduct, pointOfSaleId);

                if (unitsOnStock >= requiredUnits) return;

                // Undefined behavior on what should happen if it is not on stock so we assume we need to abort
                throw new RuntimeException("Product " + productId + " is not available because there is not enough stock. This is undefined behavior");
            };

            BiFunction<Integer, IndividualisedProductItem, Boolean> isAvailable = (requiredUnits, individualProduct) -> {
                var unitsOnStock = this.stockSystem.getUnitsOnStock(individualProduct, pointOfSaleId);

                return unitsOnStock >= requiredUnits;
            };

            // Could abstract the stock check but it is
            if (!item.isCampaign()) {
                // TODO: andernfalls (wenn keine Kampagne vorliegt) müssen Sie
                // 1) das Produkt in der in item.getUnits() angegebenen Anzahl hinsichtlich Verfügbarkeit überprüfen und
                // Putting in a lot of trust here that this cast is correct
                var individualProduct = (IndividualisedProductItem) product;

                // 2) das Produkt, falls verfügbar, in der entsprechenden Anzahl aus dem Warenlager entfernen
//                assertIsAvailable.accept(item.getUnits(), individualProduct);
                if(!isAvailable.apply(item.getUnits(), individualProduct)) continue;

                this.stockSystem.removeFromStock(individualProduct, pointOfSaleId, item.getUnits());
                continue;
            }

            this.campaignTracking.purchaseCampaignAtTouchpoint(item.getErpProductId(), touchpoint,
                    item.getUnits());
            // TODO: wenn Sie eine Kampagne haben, müssen Sie hier
            // 1) über die ProductBundle Objekte auf dem Campaign Objekt iterieren, und
            // Really putting a lot of faith into this cast
            var campaign = (Campaign) product;
            for (ProductBundle bundle : campaign.getBundles()) {
                // 2) für jedes ProductBundle das betreffende Produkt in der auf dem Bundle angegebenen Anzahl, multipliziert mit dem Wert von
                // item.getUnits() aus dem Warenkorb,
                // - hinsichtlich Verfügbarkeit überprüfen, und
                var units = bundle.getUnits() * item.getUnits();

//                assertIsAvailable.accept(units, bundle.getProduct());
                if(!isAvailable.apply(units, bundle.getProduct())) continue;

                // - falls verfügbar, aus dem Warenlager entfernen - nutzen Sie dafür die StockSystem bean
                stockSystem.removeFromStock(bundle.getProduct(), pointOfSaleId, units);

                // (Anm.: item.getUnits() gibt Ihnen Auskunft darüber, wie oft ein Produkt, im vorliegenden Fall eine Kampagne, im
                // Warenkorb liegt)
            }
        }
    }

    @Override
    public void purchaseCartAtTouchpointForCustomer(CreatePurchaseRequest request) throws ShoppingException {
        var customer = this.customerService.readCustomer(request.customerId());
        var touchPoint = this.touchpointAccess.readTouchpoint(request.touchPointId());
        var cart = new ShoppingCartEntity();
        var items = this.shoppingCartService.getItems(request.shoppingCartId());
        for (ShoppingCartItem item : items) {
            var copy = new ShoppingCartItem(item.getErpProductId(), item.getUnits(), item.isCampaign());
            cart.addItem(copy);
        }
        this.purchase(customer, touchPoint, cart);
    }

}
