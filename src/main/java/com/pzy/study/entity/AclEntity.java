package com.pzy.study.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * Destription:部门表
 * Author: pengzuyao
 * Time: 2019-05-21
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AclEntity implements Serializable {

    private static final long serialVersionUID = -6422797252112215849L;

    private Integer id; //权限id

    private Integer code; //权限编码

    private String name; //部门名称

    private Integer aclModuleId; //权限所在模块id

    private String url; //请求的url,可以填正则表达式

    private Integer type; //类型：1：菜单，2：按钮，3：其它

    private Integer status; //状态，1：正常，0：冻结

    private Integer seq; //权限在当前模块下的顺序，由小到大

    private String remark;

    private String operator;

    private Date operatorTime;

    private String operatorIp;
}
