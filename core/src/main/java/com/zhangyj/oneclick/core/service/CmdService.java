package com.zhangyj.oneclick.core.service;


import com.zhangyj.oneclick.core.common.config.AbstractCmdConfig;

/**
 * @author zhangyj
 */
public interface CmdService<T extends AbstractCmdConfig> {

    /**
     * 获取配置类
     * @return 配置类
     */
    T getConfig();

    /**
     * 设置配置类
     * @param c 配置类
     */
    void setConfig(T c);
    /**
     * 执行命令
     * @throws Exception 异常
     */
    void exec() throws Exception;

    /**
     * 获取描述
     * @return 描述
     */
    String getDesc();
}
