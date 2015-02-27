package net.popbean.pf.lock;

import net.popbean.pf.exception.BusinessError;

/**
 * 业务锁服务
 * @author to0ld
 *
 */
public interface LockService {
	/**
	 * 
	 * @param business_type
	 * @param lock_id
	 * @throws BusinessError
	 */
	public void lock(String business_type,String lock_id)throws BusinessError;
	/**
	 * 
	 * @param business_type
	 * @param lock_id
	 * @throws BusinessError
	 */
	public void unlock(String business_type,String lock_id)throws BusinessError;
}
