package nus.iss.se.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author mijiupro
 */
@Getter
@AllArgsConstructor
public enum ResultStatus {

    /* 成功状态码 */
    SUCCESS(1, "SUCCESS"),

    /* 错误状态码 */
    FAIL(0, "FAIL"),

    /* 参数错误：10001-19999 */
    PARAM_IS_INVALID(10001, "参数无效"),
    PARAM_IS_BLANK(10002, "参数为空"),
    PARAM_TYPE_BIND_ERROR(10003, "Parameter Format Error"),
    PARAM_NOT_COMPLETE(10004, "参数缺失"),

    /* 用户错误：20001-29999*/
    USER_NOT_LOGGED_IN(20001, "用户未登录，请先登录"),
    USER_LOGIN_ERROR(20002, "账号不存在或密码错误"),
    USER_ACCOUNT_FORBIDDEN(20003, "账号已被禁用"),
    USER_NOT_FOUND(20004, "User Not Found"),
    USER_HAS_EXISTED(20005, "User Has Existed"),
    USER_ROLE_NOT_FOUND(20006,"User Role Not Found"),
    USER_ACTIVATE_TOKEN_EXPIRE(20007,"Activate Token Expired"),
    USER_ACCOUNT_NOT_ACTIVATE(20008,"User Account Not Activate"),
    /* 订单错误：30001-39999 */ // 新增：订单相关错误
    ORDER_NOT_FOUND(30001, "Order Not Found"),
    ORDER_CANNOT_CANCEL(30002, "Order Cannot Be Cancelled"),
    ORDER_ALREADY_CANCELLED(30003, "Order Already Cancelled"),
    ORDER_CANNOT_VERIFY(30004, "Order Cannot Be Verified"),
    ORDER_ALREADY_PAID(30005, "Order Already Paid"),
    ORDER_INVALID_AMOUNT(30006, "Invalid Order Amount"),
    
    /* 产品错误：31001-31999 */ // 新增：产品相关错误
    PRODUCT_NOT_FOUND(31001, "Product Not Found"),
    PRODUCT_OUT_OF_STOCK(31002, "Product Out Of Stock"),
    
    /* 购物车错误：32001-32999 */ // 新增：购物车相关错误
    CART_NOT_FOUND(32001, "Cart Not Found"),
    CART_ITEM_NOT_FOUND(32002, "Cart Item Not Found"),
    
    /* 商户错误：33001-33999 */ // 新增：商户相关错误
    MERCHANT_NOT_FOUND(33001, "Merchant Not Found"),
    MERCHANT_HAS_EXISTED(33002, "Merchant Has Existed"),
    ACCESS_DENIED(33003, "Access Denied"),
    
    /* 系统错误：40001-49999 */
    FILE_MAX_SIZE_OVERFLOW(40003, "上传尺寸过大"),
    FILE_ACCEPT_NOT_SUPPORT(40004, "上传文件格式不支持"),

    /* 数据错误：50001-599999 */
    RESULT_DATA_NONE(50001, "数据未找到"),
    DATA_IS_WRONG(50002, "数据有误"),
    DATA_ALREADY_EXISTED(50003, "数据已存在"),
    AUTH_CODE_ERROR(50004, "验证码错误"),

    /* 微服务错误：60001-69999 */
    SERVICE_UNAVAILABLE(60001, "Service Temporarily Unavailable"),
    SERVICE_ERROR(60002, "Service Error"),
    SERVICE_TIMEOUT(60003, "Service Timeout"),
    
    /* 权限错误：70001-79999 */
    PERMISSION_UNAUTHENTICATED(70001, "此操作需要登陆系统！"),

    PERMISSION_UNAUTHORIZED(70002, "权限不足，无权操作！"),

    PERMISSION_EXPIRE(70003, "登录状态过期！"),

    PERMISSION_TOKEN_EXPIRED(70004, "token已过期"),

    PERMISSION_LIMIT(70005, "访问次数受限制"),

    PERMISSION_TOKEN_INVALID(70006, "Invalid Token"),

    PERMISSION_SIGNATURE_ERROR(70007, "签名失败"),
	
    /* 支付错误：80001-89999 */ 
    PAYMENT_FAILED(80001, "Payment Failed"),
    PAYMENT_VERIFICATION_FAILED(80002, "Payment Verification Failed"),
    PAYMENT_SESSION_INVALID(80003, "Payment Session Invalid");
    // 状态码
    private final int code;
    // 提示信息
    private final String message;
}
