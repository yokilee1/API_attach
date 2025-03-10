package io.dongtai.agent;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

/**
 * API转换器，负责拦截和转换Java类
 */
public class ApiTransformer implements ClassFileTransformer {
    private static final boolean DEBUG = Boolean.parseBoolean(System.getProperty("api.debug", "false"));
    
    @Override
    public byte[] transform(ClassLoader loader, String className, 
                          Class<?> classBeingRedefined,
                          ProtectionDomain protectionDomain, 
                          byte[] classfileBuffer) {
        // 过滤不需要处理的类
        if (className == null) {
            return classfileBuffer;
        }
        
        // 转换类名格式
        String javaClassName = className.replace('/', '.');
        
        // 只处理控制器类和带有RequestMapping注解的类
        if (!shouldTransform(javaClassName)) {
            return classfileBuffer;
        }
        
        try {
            if (DEBUG) {
                System.out.println("[API Collector] Transforming class: " + javaClassName);
            }
            
            // 使用ASM进行字节码转换
            ClassReader cr = new ClassReader(classfileBuffer);
            ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
            ClassVisitor cv = new ApiClassVisitor(cw, javaClassName);
            cr.accept(cv, ClassReader.EXPAND_FRAMES);
            return cw.toByteArray();
        } catch (Exception e) {
            System.err.println("[API Collector] Failed to transform class " + javaClassName + ": " + e.getMessage());
            if (DEBUG) {
                e.printStackTrace();
            }
            return classfileBuffer;
        }
    }
    
    /**
     * 判断是否需要转换该类
     */
    private boolean shouldTransform(String className) {
        // 排除系统类和第三方库
        if (className.startsWith("java.") || 
            className.startsWith("javax.") || 
            className.startsWith("sun.") || 
            className.startsWith("com.sun.") ||
            className.startsWith("io.dongtai.agent.")) {
            return false;
        }
        
        // 处理控制器类和可能包含API的类
        return className.endsWith("Controller") || 
               className.contains(".controller.") || 
               className.contains(".web.") || 
               className.contains(".api.") || 
               className.contains(".rest.");
    }
}