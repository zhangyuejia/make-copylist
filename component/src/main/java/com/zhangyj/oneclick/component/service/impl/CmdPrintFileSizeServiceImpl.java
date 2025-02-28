package com.zhangyj.oneclick.component.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.zhangyj.oneclick.component.common.config.CmdPrintFileSizeConfig;
import com.zhangyj.oneclick.component.entity.bo.FileInfoBO;
import com.zhangyj.oneclick.core.common.handler.ChainHandler;
import com.zhangyj.oneclick.core.service.AbstractCmdService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 打印文件大小功能
 * @author zhangyj
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CmdPrintFileSizeServiceImpl extends AbstractCmdService<CmdPrintFileSizeConfig> {

    private final Map<String, Long> fileSizeMap = new HashMap<>();

    private final ExecutorService executorService  = new ThreadPoolExecutor(
            10, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(), Executors.defaultThreadFactory(), new ThreadPoolExecutor.CallerRunsPolicy());

    private List<FileInfoBO> fileInfoList;

    private int depth;

    @Resource
    private ChainHandler<Long, String> fileSizeHandler;

    @Override
    public void exec() throws Exception {
        initConfig();
        for (int i = 0; i < this.depth; i++) {
            if (CollectionUtil.isNotEmpty(fileInfoList)) {
                Optional<FileInfoBO> firstFolderOp = fileInfoList.stream().filter(v -> !v.getIsFile()).findFirst();
                if (!firstFolderOp.isPresent()) {
                    break;
                }
                this.config.setDir(firstFolderOp.get().getFileName());
            }
            log.info("开始计算{}级文件夹{}", i + 1, this.config.getDir());
            loadFileInfoList();
            printFileInfoList();
        }
        executorService.shutdownNow();
    }

    private void initConfig() {
        if (StringUtils.isBlank(config.getDir())) {
            config.setDir(cmdExecConfig.getDir());
        }
        this.depth = ObjectUtil.isNotNull(config.getDepth())?
                config.getDepth(): 1;
    }

    private void printFileInfoList() {
        for (FileInfoBO fileInfo : fileInfoList) {
            log.info("{}：{} 大小：{}", fileInfo.getIsFile()? "单文件":"文件夹", fileInfo.getFileName(), getFileSizeDesc(fileInfo.getSize()));
        }
        long sum = fileInfoList.stream().mapToLong(FileInfoBO::getSize).sum();
        log.info("路径:{} 总大小:{}", config.getDir(), fileSizeHandler.handle(sum));
    }

    private String getFileSizeDesc(Long size) {
        if(size == null || size == 0){
            return "0";
        }
        return fileSizeHandler.handle(size);
    }

    private void loadFileInfoList() throws InterruptedException {
        File file = new File(config.getDir());
        if(!file.exists()){
            throw new RuntimeException("文件路径不存在，" + config.getDir());
        }
        File[] files = file.listFiles(File::exists);
        if(files == null){
            throw new RuntimeException("文件路径内容为空，" + config.getDir());
        }
        // 计算文件大小
        calculateFileSize(files);
        // 转换对象
        transferToFileInfoList(files);
    }

    private void transferToFileInfoList(File[] files) {
        this.fileInfoList = Stream.of(files)
                .map(f -> FileInfoBO.builder()
                        .fileName(f.getAbsolutePath())
                        .isFile(f.isFile())
                        .size(fileSizeMap.get(f.getName())).build())
                // 按大小倒序打印
                .sorted(Comparator.comparing(FileInfoBO::getSize).reversed())
                .collect(Collectors.toList());
    }

    private void calculateFileSize(File[] files) throws InterruptedException {
        // 多线程计算文件大小
        CountDownLatch latch = new CountDownLatch(files.length);
        for (File file : files) {
            executorService.execute(() -> {
                long fileSize = file.isFile() ? file.length() : FileUtils.sizeOfDirectory(file);
                fileSizeMap.put(file.getName(), fileSize);
                latch.countDown();
            });
        }
        latch.await();
    }

    @Override
    public String getDesc() {
        return "打印文件大小功能";
    }
}
