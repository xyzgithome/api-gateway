package com.wym.common.rule;

import lombok.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Rule implements Comparable<Rule>, Serializable {
    // 全局唯一的规则ID
    private String id;

    // 规则名称
    private String name;

    // 规则对应的协议
    private String protocol;

    // 规则优先级
    private Integer order;

    // 规则配置
    private Set<FilterConfig> filterConfigs = new HashSet<>();

    @Getter
    @Setter
    @EqualsAndHashCode(of = "id")
    public static class FilterConfig {
        // 规则配置id
        private String id;

        // 配置信息
        private String config;
    }

    // 向规则里面提供新增配置的方法
    public boolean addFilterConfig(FilterConfig filterConfig) {
        return filterConfigs.add(filterConfig);
    }

    // 通过指定的ID获取指定的配置信息
    public FilterConfig getFilterConfig(String id) {
        for (FilterConfig filterConfig : filterConfigs) {
            if (filterConfig.getId().equalsIgnoreCase(id)) {
                return filterConfig;
            }
        }
        return null;
    }

    // 通过传入的filterId判断配置是否存在
    public boolean hashId(String filterId) {
        for (FilterConfig filterConfig : filterConfigs) {
            if (filterConfig.getId().equalsIgnoreCase(filterId)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public int compareTo(Rule that) {
        int compare = Integer.compare(this.getOrder(), that.getOrder());

        if (compare == 0) {
            return this.getId().compareTo(that.getId());
        }

        return compare;
    }
}
