package com.zhangyj.cmdexecutor.core.common.handler.impl;

import com.zhangyj.cmdexecutor.core.common.handler.StringHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zhangyj
 */
@Slf4j
public class DefaultStringHandler implements StringHandler {

    @Override
    public void handle(String str) {
        log.info(str);
    }
}
