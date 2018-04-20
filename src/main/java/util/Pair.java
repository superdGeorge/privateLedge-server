package util;

public class Pair<T, U>{
	private T first;
	
	private U second;
	
	public Pair(T t, U u) {
		first = t;
		second = u;
	}
	
	public T getFirst() {
		return first;
	}
	
	public U getSecond() {
		return second;
	}
}
