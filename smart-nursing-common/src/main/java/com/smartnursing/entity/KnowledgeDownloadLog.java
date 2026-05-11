package com.smartnursing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("knowledge_download_log")
public class KnowledgeDownloadLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long resourceId;
    private Long userId;
    private LocalDateTime downloadTime;
}
