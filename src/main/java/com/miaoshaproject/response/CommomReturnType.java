package com.miaoshaproject.response;

public class CommomReturnType {
    //表明对应请求的返回结果为success或者fail
    private String status;

    private Object data;


    //定义一个通用的创建方法
    public static CommomReturnType create(Object result) {
        return CommomReturnType.create(result, "success");
    }

    public static CommomReturnType create(Object result, String status) {
        CommomReturnType type = new CommomReturnType();
        type.setStatus(status);
        type.setData(result);
        return type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
