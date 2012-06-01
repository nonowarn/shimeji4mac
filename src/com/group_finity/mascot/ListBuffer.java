package com.group_finity.mascot;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class ListBuffer<T> {

	private List<T> list;

	ListBuffer(List<T> list) {
		this.list = list;
	}

	public String toString() {
		return "ListBuffer(" + this.list + ")";
	}

	public List<T> buffer(int length) {
		List<T> ret = new ArrayList<T>();
		for (int i = 0; i < length; ++i) {
			if (i >= this.list.size()) {
				break;
			}
			T value = this.list.get(0);
			this.list.remove(0);
			ret.add(value);
			this.list.add(value);
		}
		return ret;
	}

	public static void main(String args[]) {
		List<Integer> list = new ArrayList<Integer>();
		ListBuffer<Integer> lb;

		for (int i = 0; i < 5; ++i) {
			list.add(i);
		}
		lb = new ListBuffer<Integer>(list);

		System.out.println(lb);
		System.out.println(lb.buffer(2));
		System.out.println(lb);
		System.out.println(lb.buffer(2));
		System.out.println(lb);
	}
}
