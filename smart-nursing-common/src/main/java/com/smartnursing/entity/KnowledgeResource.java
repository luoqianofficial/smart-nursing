package com.smartnursing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("knowledge_resource")
public class KnowledgeResource {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String resourceName;
    private Long categoryId;
    private Long fileId;
    private String coverUrl;
    private Integer status;
    private Long auditUserId;
    private LocalDateTime auditTime;
    private String auditRemark;
    private Long visitCount;
    private Long downloadCount;
    private Long createUserId;
    private LocalDateTime createTime;
}
