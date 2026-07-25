package org.dieschnittstelle.ess.mip.components.erp.crud.impl;

import java.util.List;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;
import jakarta.interceptor.Interceptor;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import org.dieschnittstelle.ess.entities.erp.PointOfSale;
import org.apache.logging.log4j.Logger;
import org.dieschnittstelle.ess.mip.components.erp.crud.api.PointOfSaleCRUD;
import org.dieschnittstelle.ess.utils.interceptors.Logged;

@ApplicationScoped
@Logged
@Transactional
/*
 * note in an all-in-one monolithic deployment the registered REST client
 * conflicts with the interfaces actual bean implementation. For this
 * reason, the implementation needs to be assigned a higher priority by
 * using @Alternative and @Priority
 */
@Alternative
@Priority(Interceptor.Priority.APPLICATION+10)
public class PointOfSaleCRUDImpl implements PointOfSaleCRUD {

	protected static Logger logger = org.apache.logging.log4j.LogManager.getLogger(PointOfSaleCRUDImpl.class);

	@Inject
	@EntityManagerProvider.ERPDataAccessor
	private EntityManager em;
	
	/*
	 * TODO ADD1: comment in/out @TransactionAttribute
	 */
	@Override
	//@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public PointOfSale createPointOfSale(PointOfSale pos) {
		logger.info("createPointOfSale(): " + pos);

		em.persist(pos);
		// force the generated id to be assigned now, since the caller
		// needs pos.getId() right away (e.g. to link it to a touchpoint)
		// and it would otherwise stay 0 until the transaction commits
		em.flush();

		return pos;
	}

	@Override
	public PointOfSale readPointOfSale(long posId) {
		return em.find(PointOfSale.class,posId);
	}

	@Override
	public boolean deletePointOfSale(long posId) {
		em.remove(em.find(PointOfSale.class,posId));
		return true;
	}

	@Override
	public List<PointOfSale> readAllPointsOfSale() {
		return em.createQuery("SELECT DISTINCT p FROM PointOfSale AS p").getResultList();
	}

}
