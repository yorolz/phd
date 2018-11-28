import java.io.Serializable;

public final class Holder<T> implements Serializable {

	private static final long serialVersionUID = 1557280221193478606L;
	public T value;

	public Holder() {
	}

	public Holder(T value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "" + value;
	}

}