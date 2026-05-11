package com.smartnursing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("digital_play_log")
public class DigitalPlayLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long resourceId;
    private Long userId;
    private LocalDateTime playTime;
}
