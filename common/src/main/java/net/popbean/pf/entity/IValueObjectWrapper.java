package net.popbean.pf.entity;

public interface IValueObjectWrapper<T> {
	/**
	 * 
	 * @param target
	 * @param key
	 * @param value
	 */
	public void set(T target, String key, Object value);
	/**
	 * 
	 * @param target
	 * @param key
	 * @return
	 */
	public Object get(T target, String key);
}
