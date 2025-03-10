package io.dongtai.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * API收集器，负责收集和存储API信息
 */
public class ApiCollector {
    private static final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private static final Map<String, ObjectNode> apiRegistry = new ConcurrentHashMap<>();
    private static final AtomicInteger apiCounter = new AtomicInteger(0);
    private static String outputFilePath;
    private static boolean initialized = false;
    
    /**
     * 初始化API收集器
     * @param filePath 输出文件路径
     */
    public static synchronized void init(String filePath) {
        if (!initialized) {
            outputFilePath = filePath;
            initialized = true;
            // 创建一个空的API集合文件
            saveApiCollection();
            System.out.println("[API Collector] Initialized with output file: " + outputFilePath);
        }
    }
    
    /**
     * 注册一个API
     * @param className 类名
     * @param methodName 方法名
     * @param httpMethod HTTP方法（GET, POST等）
     * @param path 请求路径
     * @param parameterInfo 参数信息
     */
    public static void registerApi(String className, String methodName, String httpMethod, 
                                  String path, Map<String, String> parameterInfo) {
        if (!initialized) {
            System.err.println("[API Collector] Not initialized yet");
            return;
        }
        
        String apiKey = className + "#" + methodName;
        ObjectNode apiNode = apiRegistry.computeIfAbsent(apiKey, k -> {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("id", apiCounter.incrementAndGet());
            node.put("className", className);
            node.put("methodName", methodName);
            return node;
        });
        
        apiNode.put("httpMethod", httpMethod);
        apiNode.put("path", path);
        
        // 添加参数信息
        ObjectNode paramsNode = objectMapper.createObjectNode();
        if (parameterInfo != null && !parameterInfo.isEmpty()) {
            parameterInfo.forEach(paramsNode::put);
        }
        apiNode.set("parameters", paramsNode);
        
        // 保存到文件
        saveApiCollection();
    }
    
    /**
     * 保存API集合到文件
     */
    private static synchronized void saveApiCollection() {
        try {
            ArrayNode apiArray = objectMapper.createArrayNode();
            apiRegistry.values().forEach(apiArray::add);
            
            ObjectNode rootNode = objectMapper.createObjectNode();
            rootNode.put("totalCount", apiRegistry.size());
            rootNode.set("apis", apiArray);
            
            objectMapper.writeValue(new File(outputFilePath), rootNode);
        } catch (IOException e) {
            System.err.println("[API Collector] Failed to save API collection: " + e.getMessage());
            e.printStackTrace();
        }
    }
}