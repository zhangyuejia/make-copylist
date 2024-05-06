package com.zhangyj.oneclick.component.service.impl;

import cn.hutool.core.io.FileUtil;
import com.zhangyj.oneclick.component.business.xydecrypt.XyDecryptProcessor;
import com.zhangyj.oneclick.component.common.config.CmdXyDecryptConfig;
import com.zhangyj.oneclick.core.service.AbstractCmdService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author zhangyj
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CmdXyDecryptServiceImpl extends AbstractCmdService<CmdXyDecryptConfig> {

    private final List<XyDecryptProcessor> xyDecryptProcessors;

    @Override
    public void exec() throws Exception {
        if (checkParam()){
            return;
        }
        printInfo();
        mkTempDir();
        processFiles(new File(config.getPath()));
    }

    private void printInfo() {
        List<String> fileExtensions = xyDecryptProcessors.stream()
                .map(XyDecryptProcessor::getFileExtensions)
                .flatMap(List::stream).collect(Collectors.toList());
        log.info("支持的文件类型：{}", String.join(",", fileExtensions));
    }

    private void processFiles(File dirFile) {
        if (dirFile.isDirectory()) {
            processDir(dirFile);
        }else {
            processFile(dirFile);
        }
    }

    private void processFile(File dirFile) {
        for (XyDecryptProcessor xyDecryptProcessor : xyDecryptProcessors) {
            if (xyDecryptProcessor.isMatch(dirFile)) {
                log.info("匹配到文件处理器：{}", dirFile.getPath());
                xyDecryptProcessor.processDecrypt(config, dirFile);
                break;
            }
        }
    }

    private void processDir(File dirFile) {
        File[] files = dirFile.listFiles();
        if(files == null){
            return;
        }
        for (File file : files) {
            processFiles(file);
        }
    }

    private static void mkTempDir() {
        String tempDir = XyDecryptProcessor.TEMP_DIR;
        if (FileUtil.exist(tempDir)) {
            FileUtil.del(tempDir);
        }
        FileUtil.mkdir(tempDir);
    }

    private boolean checkParam() {
        if (StringUtils.isBlank(config.getPath()) || !FileUtil.exist(config.getPath())) {
            return true;
        }
        File[] files = new File(config.getPath()).listFiles();
        if (files == null) {
            log.info("文件夹无文件：" + config.getPath());
            return true;
        }
        return false;
    }

    @Override
    public String getDesc() {
        return "解密功能";
    }
}
