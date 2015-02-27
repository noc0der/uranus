package net.popbean.pf.entity.field;




/**
 * 参考RoundingMode的实现方式
 * @author to0ld
 *
 */
public enum Domain{
	PK(60), Code(60), Date(5), TimeStamp(7), Memo(1000), Money(12),Seriescode(320),Stat(19),Clob(23),Int(29),Ref(120);
	int _value;
	private Domain(int value){
		this._value = value;
	}
	public static Domain valueOf(int rm){
		return Domain.PK;
	}
	public int getLength(){
		return _value;
	}
}
