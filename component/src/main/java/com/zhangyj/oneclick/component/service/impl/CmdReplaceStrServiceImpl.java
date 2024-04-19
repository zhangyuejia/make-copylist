package com.zhangyj.oneclick.component.service.impl;

import com.zhangyj.oneclick.component.common.config.CmdReplaceStrConfig;
import com.zhangyj.oneclick.component.service.AbstractCmdReplaceServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * @author zhangyj
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CmdReplaceStrServiceImpl extends AbstractCmdReplaceServiceImpl<CmdReplaceStrConfig> {

    @Override
    protected void writePropertyFile(String filePath) throws IOException {
        log.info("读取文件：{}", filePath);
        Path path = Paths.get(filePath);

        List<String> lines = Files.readAllLines(path);
        try (BufferedWriter writer = Files.newBufferedWriter(path)){
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                String placedLine = line;
                boolean isMatch = false;
                for (Map.Entry<String, String> entry : this.currentPropertiesMap.entrySet()) {
                    String key = entry.getKey();
                    if (placedLine.contains(key)) {
                        isMatch = true;
                        String value = entry.getValue();
                        placedLine = placedLine.replace(key, value);
                    }
                }
                if (isMatch && !line.equals(placedLine)) {
                    log.info("替换结果:{} -> {}", line, placedLine);
                }
                writer.write(placedLine);
                if (i != lines.size() - 1) {
                    writer.newLine();
                }
            }
        }
    }

    @Override
    protected void writeLeftPropertiesFile(List<String> filePaths) throws IOException {

    }

    @Override
    public String getDesc() {
        return "文本替换功能";
    }
}
