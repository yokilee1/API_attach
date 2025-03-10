package io.dongtai.agent;

import java.lang.instrument.Instrumentation;
import java.io.File;

public class AgentMain {
    // 配置项
    private static final String API_OUTPUT_DIR = System.getProperty("api.output.dir", "./api-output");
    private static final String API_OUTPUT_FILE = System.getProperty("api.output.file", "api-collection.json");
    private static final boolean DEBUG = Boolean.parseBoolean(System.getProperty("api.debug", "true"));
    
    // 启动时加载
    public static void premain(String args, Instrumentation inst) {
        System.out.println("[API Collector] Agent starting in premain mode");
        init(args, inst);
    }

    // 运行时加载
    public static void agentmain(String args, Instrumentation inst) {
        System.out.println("[API Collector] Agent starting in agentmain mode");
        init(args, inst);
    }

    private static void init(String args, Instrumentation inst) {
        try {
            // 确保输出目录存在
            File outputDir = new File(API_OUTPUT_DIR);
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            
            // 初始化API收集器
            ApiCollector.init(new File(outputDir, API_OUTPUT_FILE).getAbsolutePath());
            
            // 注册类转换器
            inst.addTransformer(new ApiTransformer(), true);
            
            if (DEBUG) {
                System.out.println("[API Collector] Initialized successfully");
                System.out.println("[API Collector] Output file: " + new File(outputDir, API_OUTPUT_FILE).getAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("[API Collector] Failed to initialize agent: " + e.getMessage());
            e.printStackTrace();
        }
    }
}