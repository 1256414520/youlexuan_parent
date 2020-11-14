package com.offcn.entity;

import java.io.Serializable;

/**
 * 结果封装
 */
public class Result implements Serializable {

    //处理结果 true 成功  false 失败
    private boolean success;

    //返回成功或者失败消息
    private String message;

    public Result() {
    }

    public Result(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
