package nus.iss.se.product.validator;

import lombok.extern.slf4j.Slf4j;
import nus.iss.se.common.cache.UserContext;
import nus.iss.se.common.constant.ResultStatus;
import nus.iss.se.common.exception.BusinessException;
import nus.iss.se.product.enums.FileType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

/**
 * 文件验证器
 */
@Component
@Slf4j
public class FileValidator {
    
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );
    
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; 
    
    public void validateFile(MultipartFile file, FileType type, UserContext currentUser) {
        // 权限验证
        validatePermission(currentUser, type);
        
        // 文件类型验证
        if (!ALLOWED_IMAGE_TYPES.contains(file.getContentType())) {
            throw new BusinessException(ResultStatus.FILE_ACCEPT_NOT_SUPPORT);
        }
        
        // 文件大小验证
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ResultStatus.FILE_MAX_SIZE_OVERFLOW);
        }
        
        // 文件内容验证
        validateFileContent(file);
        
        log.info("文件验证通过: {}, 大小: {} bytes", file.getOriginalFilename(), file.getSize());
    }
    
    private void validatePermission(UserContext currentUser, FileType type) {
        switch (type) {
            case MERCHANT_BUSINESS_LICENSE:
            case MERCHANT_AVATAR:
            case MAGIC_BAG_IMAGE:
                if (!"MERCHANT".equals(currentUser.getRole())) {
                    throw new BusinessException(ResultStatus.USER_ACCOUNT_FORBIDDEN);
                }
                break;
        }
    }
    
    private void validateFileContent(MultipartFile file) {
        try {
            // 检查文件头，防止恶意文件
            byte[] header = new byte[4];
            file.getInputStream().read(header);
            
            // 检查常见图片格式的文件头
            if (!isValidImageHeader(header)) {
                throw new BusinessException(ResultStatus.FILE_ACCEPT_NOT_SUPPORT);
            }
        } catch (IOException e) {
            log.error("文件内容验证失败: {}", file.getOriginalFilename(), e);
            throw new BusinessException(ResultStatus.FAIL, "文件内容验证失败");
        }
    }
    
    private boolean isValidImageHeader(byte[] header) {
        // JPEG: FF D8 FF
        if (header[0] == (byte) 0xFF && header[1] == (byte) 0xD8 && header[2] == (byte) 0xFF) {
            return true;
        }
        
        // PNG: 89 50 4E 47
        if (header[0] == (byte) 0x89 && header[1] == (byte) 0x50 && 
            header[2] == (byte) 0x4E && header[3] == (byte) 0x47) {
            return true;
        }
        
        // GIF: 47 49 46 38
        if (header[0] == (byte) 0x47 && header[1] == (byte) 0x49 && 
            header[2] == (byte) 0x46 && header[3] == (byte) 0x38) {
            return true;
        }
        
        return false;
    }
}
