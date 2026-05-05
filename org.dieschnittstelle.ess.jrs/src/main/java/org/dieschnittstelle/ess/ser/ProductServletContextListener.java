package org.dieschnittstelle.ess.ser;

import java.io.File;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import org.dieschnittstelle.ess.entities.GenericCRUDExecutor;
import org.dieschnittstelle.ess.entities.erp.AbstractProduct;
import org.dieschnittstelle.ess.entities.erp.IndividualisedProductItem;
import org.dieschnittstelle.ess.entities.erp.ProductType;
import org.apache.logging.log4j.Logger;

/*
 * this listener manages a crud executor for products in the same way as the TouchpointsServletContextListener for touchpoints
 */
public class ProductServletContextListener implements ServletContextListener {

    protected static Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(ProductServletContextListener.class);

    @Override
    public void contextDestroyed(ServletContextEvent evt) {
        logger.info("contextDestroyed()");

        // we read out the CRUDExecutor for products and let it store its content
        GenericCRUDExecutor<AbstractProduct> exec = (GenericCRUDExecutor<AbstractProduct>) evt
                .getServletContext().getAttribute("productCRUD");

        logger.info("contextDestroyed(): loaded executor from context: " + exec);

        if (exec == null) {
            logger.warn("contextDestroyed(): no executor found in context. Ignore.");
        } else {
            exec.store();
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent evt) {

        logger.info("contextInitialised()");

        // we create a new executor for a file to be stored in the context root
        String rootPath = evt.getServletContext().getRealPath("../");

        GenericCRUDExecutor<AbstractProduct> exec = new GenericCRUDExecutor<AbstractProduct>(new File(
                rootPath, "products.data"));

        // we call load() on the executor to load any exsisting data (if there
        // are any)
        exec.load();

        // check whether we are empty - add a product in this case
        if (exec.readAllObjects().size() == 0) {
            IndividualisedProductItem prod1 = new IndividualisedProductItem("Schusterjunge", ProductType.ROLL, 720);
            exec.createObject(prod1);
        }

        // then we put the executor into the context to make it available to the
        // other components
        evt.getServletContext().setAttribute("productCRUD", exec);
    }

}
