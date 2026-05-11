package com.smartnursing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 文件信息
 * @TableName file_info
 */
@TableName(value ="file_info")
@Data
public class FileInfo {
    /**
     * 文件ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 原始文件名
     */
    private String fileOriginalName;

    /**
     * 文件后缀
     */
    private String fileSuffix;

    /**
     * 文件大小(B)
     */
    private Long fileSize;

    /**
     * 文件访问URL
     */
    private String fileUrl;

    /**
     * 存储桶
     */
    private String bucket;

    /**
     * 1知识文档 2数字多媒体
     */
    private Integer fileType;

    /**
     * 上传人ID
     */
    private Long uploadUserId;

    /**
     * 
     */
    private Date uploadTime;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        FileInfo other = (FileInfo) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getFileName() == null ? other.getFileName() == null : this.getFileName().equals(other.getFileName()))
            && (this.getFileOriginalName() == null ? other.getFileOriginalName() == null : this.getFileOriginalName().equals(other.getFileOriginalName()))
            && (this.getFileSuffix() == null ? other.getFileSuffix() == null : this.getFileSuffix().equals(other.getFileSuffix()))
            && (this.getFileSize() == null ? other.getFileSize() == null : this.getFileSize().equals(other.getFileSize()))
            && (this.getFileUrl() == null ? other.getFileUrl() == null : this.getFileUrl().equals(other.getFileUrl()))
            && (this.getBucket() == null ? other.getBucket() == null : this.getBucket().equals(other.getBucket()))
            && (this.getFileType() == null ? other.getFileType() == null : this.getFileType().equals(other.getFileType()))
            && (this.getUploadUserId() == null ? other.getUploadUserId() == null : this.getUploadUserId().equals(other.getUploadUserId()))
            && (this.getUploadTime() == null ? other.getUploadTime() == null : this.getUploadTime().equals(other.getUploadTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getFileName() == null) ? 0 : getFileName().hashCode());
        result = prime * result + ((getFileOriginalName() == null) ? 0 : getFileOriginalName().hashCode());
        result = prime * result + ((getFileSuffix() == null) ? 0 : getFileSuffix().hashCode());
        result = prime * result + ((getFileSize() == null) ? 0 : getFileSize().hashCode());
        result = prime * result + ((getFileUrl() == null) ? 0 : getFileUrl().hashCode());
        result = prime * result + ((getBucket() == null) ? 0 : getBucket().hashCode());
        result = prime * result + ((getFileType() == null) ? 0 : getFileType().hashCode());
        result = prime * result + ((getUploadUserId() == null) ? 0 : getUploadUserId().hashCode());
        result = prime * result + ((getUploadTime() == null) ? 0 : getUploadTime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", fileName=").append(fileName);
        sb.append(", fileOriginalName=").append(fileOriginalName);
        sb.append(", fileSuffix=").append(fileSuffix);
        sb.append(", fileSize=").append(fileSize);
        sb.append(", fileUrl=").append(fileUrl);
        sb.append(", bucket=").append(bucket);
        sb.append(", fileType=").append(fileType);
        sb.append(", uploadUserId=").append(uploadUserId);
        sb.append(", uploadTime=").append(uploadTime);
        sb.append("]");
        return sb.toString();
    }
}