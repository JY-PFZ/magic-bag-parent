package nus.iss.se.product.enums;

/**
 * 文件类型枚举
 */
public enum FileType {
    MERCHANT_BUSINESS_LICENSE("merchants/business-license"),
    MERCHANT_AVATAR("merchants/avatars"),
    MAGIC_BAG_IMAGE("magic-bags/product-images");
    
    private final String pathPrefix;
    
    FileType(String pathPrefix) {
        this.pathPrefix = pathPrefix;
    }
    
    public String getPathPrefix() {
        return pathPrefix;
    }
}
