package cn.zynworld.lightmerge.common;

import lombok.ToString;

import java.io.Serializable;

/**
 * @author zhaoyuening
 * @date 2019/8/3.
 */
@ToString
public class Result<T> implements Serializable {

	public final static Integer DEFAULT_SUCCESS_CODE = 200;
	public final static Integer DEFAULT_FAIL_CODE = 500;

	private Integer code;
	private String msg;
	private T data;



	public Integer getCode() {
		return code;
	}

	public Result<T> setCode(Integer code) {
		this.code = code;
		return this;
	}

	public String getMsg() {
		return msg;
	}

	public Result<T> setMsg(String msg) {
		this.msg = msg;
		return this;
	}

	public T getData() {
		return data;
	}

	public Result<T> setData(T data) {
		this.data = data;
		return this;
	}

	public boolean isSuccess() {
		return DEFAULT_SUCCESS_CODE.equals(code);
	}

	public static Result success() {
		return new Result().setCode(DEFAULT_SUCCESS_CODE);
	}

	public static Result fail() {
		return new Result().setCode(DEFAULT_FAIL_CODE);
	}

	public static Result build(boolean isSuccess) {
		return isSuccess ? success() : fail();
	}
}
