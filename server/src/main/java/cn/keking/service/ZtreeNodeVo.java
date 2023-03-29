package cn.keking.service;
import lombok.Data;
import java.util.List;
@Data
public class ZtreeNodeVo {
    private String id;
    private String pid;
    private String name;
    private List<ZtreeNodeVo> children;
}