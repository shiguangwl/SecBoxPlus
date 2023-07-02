package com.xxhoz.secbox.parserCore.bean;

import java.util.List;

/**
 * <ConfigList>
 * <ConfigList>
 *
 * @author DengNanYu
 * @version 1.0_2023/7/1
 * @date 2023/7/1 16:53
 */
public class ConfigBeanList {

    private List<SourceBean> sites;

    // TODO lives

    private List<ParseBean> parses;

    // TODO rules

    private List<String> flags;

    private String spider;
}
