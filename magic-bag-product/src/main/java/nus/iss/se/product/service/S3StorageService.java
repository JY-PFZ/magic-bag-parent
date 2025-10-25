package nus.iss.se.product.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.iss.se.product.config.S3Properties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

/**
 * S3存储服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class S3StorageService {

    private final S3Client s3Client;
    private final S3Properties s3Properties;

    /**
     * 上传文件到 S3
     *
     * @param key      S3 对象键（如 "folder/file.xlsx"）
     * @param filePath 本地文件路径
     */
    public void upload(String key, Path filePath) {
        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(s3Properties.getBucketName())
                    .key(key)
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromFile(filePath));
            log.info("File uploaded to S3 successfully: {}", key);
        } catch (Exception e) {
            log.error("Failed to upload file to S3: {}", key, e);
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }

    /**
     * 上传字节数组（适用于小文件或内存数据）
     */
    public void upload(String key, byte[] data) {
        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(s3Properties.getBucketName())
                    .key(key)
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(data));
            log.info("File uploaded to S3 successfully: {}", key);
        } catch (Exception e) {
            log.error("Failed to upload file to S3: {}", key, e);
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }

    /**
     * 上传字节数组并设置内容类型
     */
    public void upload(String key, byte[] data, String contentType) {
        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(s3Properties.getBucketName())
                    .key(key)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(data));
            log.info("File uploaded to S3 successfully: {}", key);
        } catch (Exception e) {
            log.error("Failed to upload file to S3: {}", key, e);
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }

    /**
     * 下载文件到本地路径
     *
     * @param key      S3 对象键
     * @param filePath 本地保存路径
     */
    public void download(String key, Path filePath) {
        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(s3Properties.getBucketName())
                    .key(key)
                    .build();

            s3Client.getObject(getRequest, filePath);
            log.info("File downloaded from S3 successfully: {}", key);
        } catch (Exception e) {
            log.error("Failed to download file from S3: {}", key, e);
            throw new RuntimeException("Failed to download file from S3", e);
        }
    }

    /**
     * 下载为字节数组（适用于小文件）
     */
    public byte[] downloadAsBytes(String key) throws IOException {
        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(s3Properties.getBucketName())
                    .key(key)
                    .build();

            try (ResponseInputStream<GetObjectResponse> response = s3Client.getObject(getRequest)) {
                byte[] data = response.readAllBytes();
                log.info("File downloaded from S3 successfully: {}", key);
                return data;
            }
        } catch (Exception e) {
            log.error("Failed to download file from S3: {}", key, e);
            throw new IOException("Failed to download file from S3", e);
        }
    }

    /**
     * 删除 S3 对象
     */
    public void delete(String key) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(s3Properties.getBucketName())
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteRequest);
            log.info("File deleted from S3 successfully: {}", key);
        } catch (Exception e) {
            log.error("Failed to delete file from S3: {}", key, e);
            throw new RuntimeException("Failed to delete file from S3", e);
        }
    }

    /**
     * 判断对象是否存在
     */
    public boolean exists(String key) {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(s3Properties.getBucketName())
                    .key(key)
                    .build();
            s3Client.headObject(headRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            log.error("Failed to check if file exists in S3: {}", key, e);
            return false;
        }
    }

    /**
     * 获取对象元数据（可选）
     */
    public Optional<HeadObjectResponse> getMetadata(String key) {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(s3Properties.getBucketName())
                    .key(key)
                    .build();
            return Optional.of(s3Client.headObject(headRequest));
        } catch (NoSuchKeyException e) {
            return Optional.empty();
        } catch (Exception e) {
            log.error("Failed to get metadata from S3: {}", key, e);
            return Optional.empty();
        }
    }

    /**
     * 获取文件的公开URL
     */
    public String getPublicUrl(String key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", 
                s3Properties.getBucketName(), 
                s3Properties.getRegion(), 
                key);
    }
}
