package net.popbean.pf.mvc.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
/**
 * 
 * @author to0ld
 *
 */
public class BusinessErrorModel implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2949276649492807829L;
	public String buz_code;//错误的编码
	public String buz_type;//错误类型:sys error;param check;op check
	public String buz_error_msg;//业务提示
	public String tech_error_msg;//技术提示
	public int status;
	public List<String> ste_list = new ArrayList<String>();//错误栈
	public String flag = "test";
	public Object client_ip ;//客户ip
}
