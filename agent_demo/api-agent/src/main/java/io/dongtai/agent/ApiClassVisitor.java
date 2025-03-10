package io.dongtai.agent;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.AdviceAdapter;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * API类访问器，负责访问和处理类中的API相关信息
 */
public class ApiClassVisitor extends ClassVisitor {
    private final String className;
    private String baseUrl = "";
    private static final boolean DEBUG = Boolean.parseBoolean(System.getProperty("api.debug", "false"));
    
    public ApiClassVisitor(ClassVisitor cv, String className) {
        super(Opcodes.ASM9, cv);
        this.className = className;
    }
    
    @Override
    public void visit(int version, int access, String name, String signature,
                      String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
    }
    
    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        // 处理类级别的RequestMapping注解
        if (isRequestMappingAnnotation(descriptor)) {
            return new RequestMappingAnnotationVisitor(super.visitAnnotation(descriptor, visible), 
                                                     annotation -> this.baseUrl = annotation);
        }
        return super.visitAnnotation(descriptor, visible);
    }
    
    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor,
                                   String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        if (mv != null && !name.equals("<init>") && !name.equals("<clinit>")) {
            return new ApiMethodVisitor(mv, access, name, descriptor, className, baseUrl);
        }
        return mv;
    }
    
    /**
     * 判断是否为RequestMapping相关注解
     */
    private boolean isRequestMappingAnnotation(String descriptor) {
        return descriptor.contains("RequestMapping") || 
               descriptor.contains("GetMapping") || 
               descriptor.contains("PostMapping") || 
               descriptor.contains("PutMapping") || 
               descriptor.contains("DeleteMapping") || 
               descriptor.contains("PatchMapping");
    }
    
    /**
     * RequestMapping注解访问器
     */
    private static class RequestMappingAnnotationVisitor extends AnnotationVisitor {
        private final AnnotationValueConsumer consumer;
        private String path = "";
        
        public RequestMappingAnnotationVisitor(AnnotationVisitor av, AnnotationValueConsumer consumer) {
            super(Opcodes.ASM9, av);
            this.consumer = consumer;
        }
        
        @Override
        public AnnotationVisitor visitArray(String name) {
            if (name.equals("value") || name.equals("path")) {
                return new AnnotationVisitor(Opcodes.ASM9, super.visitArray(name)) {
                    @Override
                    public void visit(String name, Object value) {
                        path = value.toString();
                        super.visit(name, value);
                    }
                    
                    @Override
                    public void visitEnd() {
                        super.visitEnd();
                        if (!path.isEmpty()) {
                            consumer.accept(path);
                        }
                    }
                };
            }
            return super.visitArray(name);
        }
        
        @Override
        public void visit(String name, Object value) {
            if ((name.equals("value") || name.equals("path")) && value instanceof String) {
                path = (String) value;
            }
            super.visit(name, value);
        }
        
        @Override
        public void visitEnd() {
            super.visitEnd();
            if (!path.isEmpty()) {
                consumer.accept(path);
            }
        }
    }
    
    /**
     * 注解值消费者接口
     */
    private interface AnnotationValueConsumer {
        void accept(String value);
    }
}