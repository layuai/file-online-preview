package cn.keking.service;

import cn.keking.config.ConfigConstants;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


@Service
public class TaskDesignService {
    private static final String fileDir = ConfigConstants.getFileDir();
    public static List<ZtreeNodeVo> getTree(String rootPath) {
        List<ZtreeNodeVo> nodes = new ArrayList<>();
        File file = new File(fileDir+rootPath);
        ZtreeNodeVo node = traverse(file);
        nodes.add(node);
        return nodes;
    }

    private static ZtreeNodeVo traverse(File file) {
        ZtreeNodeVo pathNodeVo = new ZtreeNodeVo();
        pathNodeVo.setId(file.getAbsolutePath().replace(fileDir, "").replace("\\", "/"));
        pathNodeVo.setName(file.getName());
        pathNodeVo.setPid(file.getParent().replace(fileDir, "").replace("\\", "/"));
        if (file.isDirectory()) {
            List<ZtreeNodeVo> subNodeVos = new ArrayList<>();
            File[] subFiles = file.listFiles();
            if (subFiles == null) {
                return pathNodeVo;
            }
            for (File subFile : subFiles) {
                ZtreeNodeVo subNodeVo = traverse(subFile);
                subNodeVos.add(subNodeVo);
            }
            pathNodeVo.setChildren(subNodeVos);
        }
        return pathNodeVo;
    }
}
