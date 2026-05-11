package com.smartnursing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("knowledge_category")
public class KnowledgeCategory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String categoryName;
    private Integer sort;
}
