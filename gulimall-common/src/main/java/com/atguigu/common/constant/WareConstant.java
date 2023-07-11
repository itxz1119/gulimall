package com.atguigu.common.constant;

public class WareConstant {


    /**
     * 采购单枚举
     */
    public enum PurchaseEnum{
        CREATE(0, "新建"),
        ASSIGNED(1, "已分配"),
        RECEIVE(2, "已领取"),
        FINISH(3, "已完成"),
        HASERROR(4, "有异常");


        private Integer code;
        private String message;

        PurchaseEnum(Integer code, String message) {
            this.code = code;
            this.message = message;
        }

        public Integer getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }


    /**
     * 采购需求枚举
     */
    public enum PurchaseDetailEnum{
        CREATE(0, "新建"),
        ASSIGNED(1, "已分配"),
        BUYING(2, "正在采购"),
        FINISH(3, "已完成"),
        HASERROR(4, "采购失败");

        private Integer code;
        private String message;

        PurchaseDetailEnum(Integer code, String message) {
            this.code = code;
            this.message = message;
        }

        public Integer getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }
}
