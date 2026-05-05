package org.dieschnittstelle.ess.jrs;

import jakarta.servlet.ServletContext;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Context;
import org.apache.logging.log4j.Logger;
import org.dieschnittstelle.ess.entities.GenericCRUDExecutor;
import org.dieschnittstelle.ess.entities.erp.AbstractProduct;
import org.dieschnittstelle.ess.entities.erp.IndividualisedProductItem;

import java.util.List;

/*
 * TODO JRS2: implementieren Sie hier die im Interface deklarierten Methoden
 */

public class ProductCRUDServiceImpl implements IProductCRUDService {

    protected static Logger logger = org.apache.logging.log4j.LogManager.getLogger(TouchpointCRUDServiceImpl.class);

    private GenericCRUDExecutor<AbstractProduct> executor;

    public ProductCRUDServiceImpl(@Context ServletContext context) {
        logger.debug("ProductCRUDServiceImpl() constructor invoked");
        this.executor = (GenericCRUDExecutor<AbstractProduct>) context.getAttribute("productCRUD");
        logger.debug("ProductCRUDServiceImpl() got executor: " + executor);
    }

    @Override
    public AbstractProduct createProduct(
            AbstractProduct product) {
        // Unsafe cast because we haven't checked if it is this instance. But there is only one implementation at the time of writing
        return this.executor.createObject(product);
    }

    @Override
    public List<AbstractProduct> readAllProducts() {
        // So dirty...
        return this.executor.readAllObjects();
    }

    @Override
    public AbstractProduct updateProduct(long id,
                                         AbstractProduct update) {
        //TODO id is unused
        // Unsafe cast
        return this.executor.updateObject(update);
    }

    @Override
    public boolean deleteProduct(long id) {
        logger.debug("deleteProduct() invoked with id: " + id);
        var isDeleted = this.executor.deleteObject(id);
        // Not sure if this is the right place/layer for not found status codes
        logger.debug("deleteProduct() returned: " + isDeleted);

        return isDeleted;
    }

    @Override
    public AbstractProduct readProduct(long id) {
        var value = this.executor.readObject(id);
        if (value == null) throw new NotFoundException();
        return value;
    }

}
