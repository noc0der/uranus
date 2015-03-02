package net.popbean.pf.security.service;

import java.util.List;

import net.popbean.pf.exception.BusinessError;

/**
 * 用户服务管理的接口
 * - [] 基本信息(本人及运维主管可见)
 * - 
 * @author to0ld
 */
public interface AccountBusinessService {
	/**
	 * 单个更改用户状态（发布、封存、解封、删除）
	 * TODO 对越级更改状态的判断
	 * @param account 用户对象
	 * @param newStat 更改后状态值
	 * @return 
	 * @throws BuzException
	 */
	Integer changeStat(List<String> pk_account_list, Integer newStat)throws BusinessError;
	/**
	 * 检查用户名是否被占用,确保用户名唯一
	 * @param account_code
	 * @return 是/否
	 * @throws BuzException
	 */
	Boolean validateAccountCode(String account_code) throws BusinessError;
}
