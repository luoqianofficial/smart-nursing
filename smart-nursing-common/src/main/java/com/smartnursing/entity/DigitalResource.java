package com.smartnursing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("digital_resource")
public class DigitalResource {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String resourceName;
    private Integer mediaType;
    private Integer duration;
    private Long fileId;
    private Integer status;
    private Long visitCount;
    private Long createUserId;
    private LocalDateTime createTime;
}
