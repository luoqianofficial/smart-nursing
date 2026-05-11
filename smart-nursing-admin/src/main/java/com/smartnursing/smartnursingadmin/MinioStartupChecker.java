package com.smartnursing.smartnursingadmin;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class MinioStartupChecker implements CommandLineRunner {
    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

    @Override
    public void run(String... args) throws Exception {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());

            if (!exists) {
                System.out.println("Bucket 不存在，正在创建: " + bucketName);
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
                System.out.println("Bucket 创建成功: " + bucketName);
            } else {
                System.out.println("Bucket 已存在: " + bucketName);
            }
        } catch (Exception e) {
            System.err.println("MinIO 初始化失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("MinIO 连接失败，请检查 MinIO 服务是否正常运行", e);
        }
    }
}
