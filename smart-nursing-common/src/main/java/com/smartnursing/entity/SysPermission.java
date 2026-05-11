package com.smartnursing.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 权限
 * @TableName sys_permission
 */
@TableName(value ="sys_permission")
@Data
public class SysPermission {
    /**
     * 权限ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 权限名
     */
    private String permName;

    /**
     * 权限标识
     */
    private String permCode;

    /**
     * 1菜单 2按钮 3接口
     */
    private Integer permType;

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
        SysPermission other = (SysPermission) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getPermName() == null ? other.getPermName() == null : this.getPermName().equals(other.getPermName()))
            && (this.getPermCode() == null ? other.getPermCode() == null : this.getPermCode().equals(other.getPermCode()))
            && (this.getPermType() == null ? other.getPermType() == null : this.getPermType().equals(other.getPermType()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getPermName() == null) ? 0 : getPermName().hashCode());
        result = prime * result + ((getPermCode() == null) ? 0 : getPermCode().hashCode());
        result = prime * result + ((getPermType() == null) ? 0 : getPermType().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", permName=").append(permName);
        sb.append(", permCode=").append(permCode);
        sb.append(", permType=").append(permType);
        sb.append("]");
        return sb.toString();
    }
}