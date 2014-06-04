package pl.idedyk.japanese.dictionary.web.mysql.model;

public class GenericLogOperationStat {

	private GenericLogOperationEnum operation;
	
	private long stat;

	public GenericLogOperationEnum getOperation() {
		return operation;
	}

	public void setOperation(GenericLogOperationEnum operation) {
		this.operation = operation;
	}

	public long getStat() {
		return stat;
	}

	public void setStat(long stat) {
		this.stat = stat;
	}
}
